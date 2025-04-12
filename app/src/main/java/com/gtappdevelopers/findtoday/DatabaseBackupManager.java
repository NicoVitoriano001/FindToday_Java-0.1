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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseBackupManager {
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1001;
    private Context context;
    private static final String TAG = "DatabaseBackupManager";

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

            // Novo caminho para a pasta FIND_TODAY dentro de Downloads
            File dstDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FIND_TODAY");
            if (!dstDir.exists()) {
                boolean dirCreated = dstDir.mkdirs();
                Log.d(TAG, "Pasta FIND_TODAY criada? " + dirCreated);
            }

            File dst = new File(dstDir, nomeArquivoBKP);
            String mensagem = "Deseja fazer backup do banco de dados?\n\n" +
                    "Local: " + dst.getParent() + "\n" +
                    "Nome do arquivo: " + nomeArquivoBKP;

            new AlertDialog.Builder(context)
                    .setTitle("Confirmar Backup")
                    .setMessage(mensagem)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean backupSuccess = backupDatabase();
                            if (backupSuccess) {
                                Toast.makeText(context, "Backup concluído em: " + dst.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Falha ao realizar backup.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        }
    }

    private boolean backupDatabase() {
        // Caminho de origem - agora em /Download/FIND_TODAY/
        File srcDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FIND_TODAY");
        File src = new File(srcDir, "fin_database.db");

        if (!src.exists()) {
            Log.e(TAG, "Arquivo de origem não encontrado: " + src.getAbsolutePath());
            return false;
        }

        String dataHora = getDataHoraAtual();
        String nomeArquivoBKP = "fin_database_" + dataHora + ".db";
        File dstDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FIND_TODAY");
        File dst = new File(dstDir, nomeArquivoBKP);

        try {
            // Verifica se a pasta de destino existe
            if (!dstDir.exists()) {
                boolean dirCreated = dstDir.mkdirs();
                Log.d(TAG, "Pasta de backup criada? " + dirCreated);
            }

            Log.d(TAG, "Iniciando backup de: " + src.getAbsolutePath());
            Log.d(TAG, "Para: " + dst.getAbsolutePath());

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

            // Verifica se o arquivo foi criado
            if (dst.exists()) {
                Log.d(TAG, "Backup criado com sucesso. Tamanho: " + dst.length() + " bytes");
                return true;
            } else {
                Log.e(TAG, "Backup falhou - arquivo não criado");
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro durante backup: " + e.getMessage());
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
                performBackup();
            } else {
                Toast.makeText(context, "Permissão negada. Backup não pode ser realizado.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}