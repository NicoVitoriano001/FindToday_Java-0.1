package com.gtappdevelopers.findtoday;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
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
    private static final String TAG = "DatabaseBackup";
    private static final String DB_NAME = "fin_db.db";
    private static final String BACKUP_FOLDER = "FIND_TODAY";

    public DatabaseBackupManager(Context context) {
        this.context = context;
    }

    public void performBackup() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        } else {
            showBackupConfirmationDialog();
        }
    }

    private void showBackupConfirmationDialog() {
        String dataHora = getDataHoraAtual();
        String nomeArquivoBKP = "fin_db_" + dataHora + ".db";

        // Caminho para /Download/FIND_TODAY/
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), BACKUP_FOLDER);
        File backupFile = new File(backupDir, nomeArquivoBKP);

        new AlertDialog.Builder(context)
                .setTitle("Confirmar Backup")
                .setMessage("Deseja fazer backup do banco de dados?\n\n" +
                        "Local: " + backupDir.getAbsolutePath() + "\n" + // /storage/emulated/0/Download/FIND_TODAY
                        "Nome: " + nomeArquivoBKP)
                .setPositiveButton("Sim", (dialog, which) -> executeBackup(backupFile))
                .setNegativeButton("Não", null)
                .show();
    }

    private void executeBackup(File backupFile) {
        try {
            // 1. Fecha a conexão com o banco de dados
            if (FinDatabase.getInstance(context) != null) {
                FinDatabase.getInstance(context).close();
            }

            // 2. Sincroniza WAL com o banco principal
            performWalCheckpoint();

            // 3. Faz o backup do arquivo
            if (copyDatabaseFile(backupFile)) {
                Toast.makeText(context,
                        "Backup salvo em: " + "\n" + backupFile.getAbsolutePath(),
                        Toast.LENGTH_LONG).show();
                Log.d(TAG, "Backup realizado com sucesso: " + backupFile.length() + " bytes");
            } else {
                Toast.makeText(context,
                        "Falha ao criar arquivo de backup",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro durante backup: " + e.getMessage());
            Toast.makeText(context,
                    "Erro durante backup: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean performWalCheckpoint() {
        SQLiteDatabase db = null;
        try {
            File dbFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), BACKUP_FOLDER + "/" + DB_NAME);

            db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);

            // Executa checkpoint FULL para sincronizar WAL
            db.execSQL("PRAGMA wal_checkpoint(FULL)");

            // Verifica o resultado
            db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).close();
            Log.d(TAG, "Checkpoint WAL executado com sucesso");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Falha no checkpoint WAL: " + e.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private boolean copyDatabaseFile(File backupFile) {
        File srcFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), BACKUP_FOLDER + "/" + DB_NAME);

        // Garante que o diretório de backup existe
        File backupDir = backupFile.getParentFile();
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            Log.e(TAG, "Falha ao criar diretório: " + backupDir.getAbsolutePath());
            return false;
        }

        try (FileChannel inChannel = new FileInputStream(srcFile).getChannel();
             FileChannel outChannel = new FileOutputStream(backupFile).getChannel()) {

            inChannel.transferTo(0, inChannel.size(), outChannel);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Erro ao copiar arquivo: " + e.getMessage());
            return false;
        }
    }

    private String getDataHoraAtual() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    public void handlePermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performBackup();
            } else {
                Toast.makeText(context,
                        "Permissão negada - backup não realizado",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}