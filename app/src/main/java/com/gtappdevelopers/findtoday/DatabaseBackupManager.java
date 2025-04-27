package com.gtappdevelopers.findtoday;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseBackupManager {
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1001;
    private Context context;
    private static final String DB_NAME = "finDB.db";
    private static final String BACKUP_FOLDER = "FIND_TODAY";
    private static final File BACKUP_DIR = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS), BACKUP_FOLDER);

    public DatabaseBackupManager(Context context) {
        this.context = context;
    }

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

    private void showToast(String message, int duration) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, duration).show());
    }

    public void handlePermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            performBackup();
        } else {
            showToast("Permissão negada - backup cancelado", Toast.LENGTH_SHORT);
        }
    }
}