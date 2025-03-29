package com.gtappdevelopers.findtoday;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {FinModal.class}, version = 1)
public abstract class FinDatabase extends RoomDatabase {
    //below line is to create instance for our databse class.
    private static FinDatabase instance;
    public abstract Dao Dao();

    //on below line we are getting instance for our database.
    public static synchronized FinDatabase getInstance(Context context) {
        //below line is to check if the instance is null or not.
        if (instance == null) {
            instance =
                    Room.databaseBuilder(context.getApplicationContext(),
                            FinDatabase.class, "fin_database.db")
                            .fallbackToDestructiveMigration()
                            .addCallback(roomCallback)
                            .build();
        }
        return instance;
    }

    //below line is to create a callback for our room database.
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    //we are creating an async task class to perform task in background.
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
