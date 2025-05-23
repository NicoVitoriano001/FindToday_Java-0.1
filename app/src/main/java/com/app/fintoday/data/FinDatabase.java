package com.app.fintoday.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;

@Database(entities = {FinModal.class}, version = 2)  //16.05.25 modificado FinModal criação coluna lastUpdated no banco de dados, no contexto do Firebase, foi necessário modificar versao do Rom

public abstract class FinDatabase extends RoomDatabase {

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {  //16.05.25 contexto Firebase
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            //16.05.25 migraçao sucesso, comentado   database.execSQL("ALTER TABLE fin_table ADD COLUMN lastUpdated INTEGER NOT NULL DEFAULT 0");
        }
    };
    private static FinDatabase instance;
    public abstract Dao Dao();
    public static synchronized FinDatabase getInstance(Context context) {
        if (instance == null) {
            // Caminho para /Download/FIN_TODAY/
            File publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File appFolder = new File(publicDownloads, "FIN_TODAY");

            if (!appFolder.exists()) {
                boolean dirCreated = appFolder.mkdirs();
             //   Log.d("DB_DEBUG", "Pasta criada? " + dirCreated);
            }

            String dbPath = new File(appFolder, "finDB.db").getAbsolutePath();
         //   Log.d("DB_DEBUG", "Caminho do DB: " + dbPath); // Verifique no Logcat!

            instance = Room.databaseBuilder(context, FinDatabase.class, dbPath)
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        PopulateDbAsyncTask(FinDatabase instance) {
            Dao dao = instance.Dao();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}
