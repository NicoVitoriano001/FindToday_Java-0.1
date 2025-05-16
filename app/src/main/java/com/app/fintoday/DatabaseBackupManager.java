package com.app.fintoday;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class DatabaseBackupManager {
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1001;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1002;
    private final FirebaseHelper firebaseHelper;
    private Context context;
    private static final String DB_NAME = "finDB.db";
    private static final String BACKUP_FOLDER = "FIN_TODAY";
    private static final File BACKUP_DIR = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS), BACKUP_FOLDER);
    public DatabaseBackupManager(Context context) {
        this.context = context;
        this.firebaseHelper = FirebaseHelper.getInstance(context); // Inicialização no construtor
    }


    //INICIO BLOCO BACKUP
    public void performBackup() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        } else {
            showBackupConfirmationDialog();
        }
    }

    private void executeBackup(File backupFile) {
        try {
            // 1. Fecha conexões do Room
            if (FinDatabase.getInstance(context) != null) {
                FinDatabase.getInstance(context).close();
            }

            // 2. Sincroniza WAL
            if (!performWalCheckpoint()) {
                throw new Exception("Falha ao sincronizar WAL");
            }

            // 3. Verifica/Cria diretório
            if (!BACKUP_DIR.exists() && !BACKUP_DIR.mkdirs()) {
                throw new Exception("Falha ao criar diretório de backup");
            }

            // 4. Copia arquivo
            if (copyDatabaseFile(backupFile)) {
                showToast("Backup salvo em:\n" + backupFile.getAbsolutePath(), Toast.LENGTH_LONG);
            } else {
                throw new Exception("Falha na cópia do arquivo");
            }
        } catch (Exception e) {
            showToast("Erro no backup: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    private boolean performWalCheckpoint() {
        SQLiteDatabase db = null;
        try {
            // 1. Verifica se o arquivo existe e é válido
            File dbFile = new File(BACKUP_DIR, DB_NAME);
            if (!dbFile.exists() || dbFile.length() == 0) {
                throw new Exception("Arquivo de banco de dados inválido ou vazio");
            }

            // 2. Abre o banco de dados corretamente
            db = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(),
                    null,
                    SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY
            );

            // 3. Executa o checkpoint de forma segura
            if (db.isOpen()) {
                db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).close();
                return true;
            }
            return false;

        } catch (Exception e) {
            showToast("Falha no WAL: " + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    private boolean copyDatabaseFile(File backupFile) {
        try {
            File srcFile = new File(BACKUP_DIR, DB_NAME); // Origem no diretório especificado

            if (!srcFile.exists()) {
                throw new IOException("Arquivo original não encontrado");
            }

            try (FileChannel in = new FileInputStream(srcFile).getChannel();
                 FileChannel out = new FileOutputStream(backupFile).getChannel()) {
                in.transferTo(0, in.size(), out);
                return true;
            }
        } catch (IOException e) {
            showToast("Erro na cópia: " + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        }
    }
    //FIM BLOCO BACKUP


    // INICIO BLOCO DE RESTORE
    public void performRestore() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity) context,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE);
        } else {
            executeRestore();
        }
    }

    private void executeRestore() {
        try {
            File[] backupFiles = BACKUP_DIR.listFiles((dir, nome) ->
                    nome.startsWith("finDB_") && nome.endsWith(".db"));

            if (backupFiles == null || backupFiles.length == 0) {
                showToast("Nenhum backup encontrado na pasta " + BACKUP_DIR.getAbsolutePath(), Toast.LENGTH_LONG);
                return;
            }

            // Ordenar por data (do mais recente para o mais antigo)
            Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            String[] fileNames = new String[backupFiles.length];
            for (int i = 0; i < backupFiles.length; i++) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                fileNames[i] = backupFiles[i].getName() + " - " + sdf.format(new Date(backupFiles[i].lastModified()));
            }

            new AlertDialog.Builder(context)
                    .setTitle("Selecione o backup para restaurar")
                    .setItems(fileNames, (dialog, which) -> {
                        confirmRestoration(backupFiles[which]);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

        } catch (Exception e) {
            showToast("Erro ao acessar backups: " + e.getMessage(), Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    private void confirmRestoration(File backupFile) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmar restauração")
                .setMessage("Deseja sobrescrever o banco de dados atual com o backup selecionado?\n\n" +
                        "Esta ação não pode ser desfeita!")
                .setPositiveButton("Sim", (dialog, which) -> {
                    try {
                        // 1. Fecha conexões do Room
                        if (FinDatabase.getInstance(context) != null) {
                            FinDatabase.getInstance(context).close();
                        }

                        // 2. Obtém o arquivo do banco de dados atual
                        File currentDbFile = context.getDatabasePath(DB_NAME);

                        // 3. Copia o arquivo de backup
                        if (copyDatabaseFile(backupFile, currentDbFile)) {
                            showToast("Banco de dados restaurado com sucesso!", Toast.LENGTH_LONG);

                            // 4. Recria a activity para aplicar as mudanças
                            if (context instanceof MainActivity) {
                                ((MainActivity) context).recreate();
                            }
                        } else {
                            throw new Exception("Falha na cópia do arquivo de backup");
                        }
                    } catch (Exception e) {
                        showToast("Erro ao restaurar backup: " + e.getMessage(), Toast.LENGTH_LONG);
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private boolean copyDatabaseFile(File source, File destination) {
        try {
            try (FileChannel in = new FileInputStream(source).getChannel();
                 FileChannel out = new FileOutputStream(destination).getChannel()) {
                in.transferTo(0, in.size(), out);
                return true;
            }
        } catch (IOException e) {
            showToast("Erro na cópia: " + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        }
    }

    public void handlePermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            performBackup();
        } else if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            performRestore();
        } else {
            showToast("Permissão negada - operação cancelada", Toast.LENGTH_SHORT);
        }
    }
    // FIM BLOCO DE RESTORE

    private void showBackupConfirmationDialog() {
        String dataHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomeArquivoBKP = "finDB_" + dataHora + ".db";
        File backupFile = new File(BACKUP_DIR, nomeArquivoBKP);

        new AlertDialog.Builder(context)
                .setTitle("Confirmar Backup")
                .setMessage("Deseja fazer backup do banco de dados?\n\n" +
                        "Local: " + BACKUP_DIR.getAbsolutePath() + "\n" +
                        "Nome: " + nomeArquivoBKP)
                .setPositiveButton("Sim", (dialog, which) -> executeBackup(backupFile))
                .setNegativeButton("Não", null)
                .show();



        // Adicionar nova opção para backup no Firebase
        new AlertDialog.Builder(context)
                .setTitle("Escolha o tipo de backup")
                .setItems(new String[]{"Backup Local", "Backup no Firebase"}, (dialog, which) -> {
                    if (which == 0) {
                        // Backup local
                        executeBackup(backupFile);
                    } else {
                        // Backup no Firebase
                        firebaseHelper.backupDatabaseToFirebase(context);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void showToast(String message, int duration) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, duration).show());
    }


}
