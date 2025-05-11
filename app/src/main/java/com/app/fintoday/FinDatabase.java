    package com.app.fintoday;

    import android.content.Context;
    import android.os.AsyncTask;
    import android.os.Environment;
    import android.util.Log;
    import androidx.annotation.NonNull;
    import androidx.room.Database;
    import androidx.room.Room;
    import androidx.room.RoomDatabase;
    import androidx.sqlite.db.SupportSQLiteDatabase;
    import java.io.File;

    @Database(entities = {FinModal.class}, version = 1)
    public abstract class FinDatabase extends RoomDatabase {
        //below line is to create instance for our databse class.
        private static FinDatabase instance;
        public abstract Dao Dao();

        //on below line we are getting instance for our database.
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
