package com.app.fintoday;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static FirebaseHelper instance;
    private final ExecutorService executorService;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;

    private FirebaseHelper(Context context) {
        // Inicializa o Firebase
        FirebaseApp.initializeApp(context);

        // Obtém as instâncias dos serviços do Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized FirebaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseHelper(context);
        }
        return instance;
    }

    // Método para sincronizar dados locais com o Firebase
    public void syncLocalDataWithFirebase(List<FinModal> finModals) {
        executorService.execute(() -> {
            try {
                String userId = firebaseAuth.getCurrentUser() != null ?
                        firebaseAuth.getCurrentUser().getUid() : "anonymous";

                DatabaseReference userRef = databaseReference.child("users").child(userId).child("finances");

                for (FinModal modal : finModals) {
                    String key = String.valueOf(modal.getId());
                    userRef.child(key).setValue(modal)
                            .addOnSuccessListener(aVoid ->
                                    Log.d(TAG, "Dados sincronizados com sucesso: " + key))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Erro ao sincronizar dados: " + key, e));
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro geral na sincronização", e);
            }
        });
    }

    // Método para fazer backup do banco de dados SQLite para o Firebase Storage
    public void backupDatabaseToFirebase(Context context) {
        executorService.execute(() -> {
            try {
                File dbFile = context.getDatabasePath("finDB.db");
                if (!dbFile.exists()) {
                    Log.e(TAG, "Arquivo de banco de dados não encontrado");
                    return;
                }

                String userId = firebaseAuth.getCurrentUser() != null ?
                        firebaseAuth.getCurrentUser().getUid() : "anonymous";

                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date());

                String backupName = "finDB_backup_" + timestamp + ".db";

                StorageReference backupRef = storageReference
                        .child("backups")
                        .child(userId)
                        .child(backupName);

                backupRef.putFile(Uri.fromFile(dbFile))
                        .addOnSuccessListener(taskSnapshot ->
                                Log.d(TAG, "Backup realizado com sucesso: " + backupName))
                        .addOnFailureListener(e ->
                                Log.e(TAG, "Erro ao fazer backup", e));
            } catch (Exception e) {
                Log.e(TAG, "Erro geral no backup", e);
            }
        });
    }

    // Métodos adicionais para autenticação, etc.
    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }
}