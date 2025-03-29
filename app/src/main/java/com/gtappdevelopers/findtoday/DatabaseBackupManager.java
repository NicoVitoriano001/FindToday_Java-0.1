package com.gtappdevelopers.findtoday;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseBackupManager {
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1001;
    private Context context;

    public DatabaseBackupManager(Context context) {
        this.context = context;
    }

    public void performBackup() {
        // Verifica se a permissão de escrita no armazenamento externo foi concedida
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        } else {
            // Exibe um diálogo de confirmação antes de realizar o backup
            String dataHora = getDataHoraAtual();
            String nomeArquivoBKP = "fin_database_" + dataHora + ".db";
            File dst = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nomeArquivoBKP);
            String mensagem = "Deseja fazer backup do banco de dados?\n\n" +
                    "Local: " + dst.getParent() + "\n" +
                    "Nome do arquivo: " + nomeArquivoBKP;

            new AlertDialog.Builder(context)
                    .setTitle("Confirmar Backup")
                    .setMessage(mensagem)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Chama o método de backup
                            boolean backupSuccess = backupDatabase();
                            if (backupSuccess) {
                                Toast.makeText(context, "Backup do banco de dados concluído com sucesso.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Falha ao realizar backup do banco de dados.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        }
    }

    private boolean backupDatabase() {
        File src = context.getDatabasePath("fin_database.db");
        String dataHora = getDataHoraAtual();
        String nomeArquivoBKP = "fin_database_" + dataHora + ".db";
        File dst = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nomeArquivoBKP);

        try {
            FileInputStream inputStream = new FileInputStream(src);
            FileOutputStream outputStream = new FileOutputStream(dst);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Após fazer o backup, forçar um checkpoint
            SQLiteDatabase db = SQLiteDatabase.openDatabase(src.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).close();
            db.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getDataHoraAtual() {
        LocalDateTime dataHoraAtual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return dataHoraAtual.format(formatter);
    }

    public void handlePermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performBackup(); // Chama o backup se a permissão for concedida
            } else {
                Toast.makeText(context, "Permissão de escrita no armazenamento externo negada.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
