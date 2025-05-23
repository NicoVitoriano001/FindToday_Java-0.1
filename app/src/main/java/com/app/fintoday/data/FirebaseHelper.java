package com.app.fintoday.data;
//Criado em 16.05.25

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
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
        executorService = Executors.newSingleThreadExecutor();

        try {
            FirebaseApp.initializeApp(context);
            FirebaseDatabase.getInstance().setPersistenceEnabled(false); // Habilita persistência offline do Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference();
            firebaseAuth = FirebaseAuth.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        } catch (Exception e) {
         //   Log.e(TAG, "Erro ao inicializar FirebaseHelper", e);
        }
    }

    public static synchronized FirebaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseHelper(context);
        }
        return instance;
    }

    public String getCurrentUserId() {
        // Retorna um UID fixo para todos os dispositivos
        return "pavyBQFB3oXKe2HpNgqZvPYcwTo1";
    }

    /**
    public void syncLocalDataWithFirebase(List<FinModal> finModals) {
        executorService.execute(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null) {
                    Log.e(TAG, "Usuário não autenticado");
                    return;
                }

                DatabaseReference userRef = databaseReference.child("finances").child(userId);

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
**/

    public void syncAllItemsToFirebase(List<FinModal> items) {
        if (items == null || items.isEmpty()) return;

        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e("FirebaseHelper", "Usuário não autenticado");
            return;
        }

        DatabaseReference userRef = databaseReference.child("finances").child(userId);

        for (FinModal item : items) {
            // Verificar se o item remoto é mais recente antes de sobrescrever
            userRef.child(String.valueOf(item.getId()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            FinModal remoteItem = snapshot.getValue(FinModal.class);
                            if (remoteItem == null || item.getLastUpdated() > remoteItem.getLastUpdated()) {
                                userRef.child(String.valueOf(item.getId()))
                                        .setValue(item)
                                        .addOnFailureListener(e ->
                                                Log.e("FirebaseSync", "Erro ao sincronizar item " + item.getId(), e));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e("FirebaseSync", "Erro ao verificar item remoto", error.toException());
                        }
                    });
        }
    }

    public void syncItemToFirebase(FinModal item) {
        executorService.execute(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null) {
                    Log.e(TAG, "Usuário não autenticado");
                    return;
                }

                DatabaseReference itemRef = databaseReference.child("finances")
                        .child(userId)
                        .child(String.valueOf(item.getId()));

                itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        FinModal remoteItem = snapshot.getValue(FinModal.class);
                        if (remoteItem == null || item.getLastUpdated() > remoteItem.getLastUpdated()) {
                            itemRef.setValue(item)
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "Item sincronizado: " + item.getId()))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Erro ao sincronizar item", e));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                       // Log.e(TAG, "Erro ao verificar item remoto", error.toException());
                    }
                });
            } catch (Exception e) {
               // Log.e(TAG, "Erro geral", e);
            }
        });
    }

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

    public DatabaseReference getUserFinancesReference(String userId) {
        return databaseReference
                .child("finances")
                .child(userId);
    }

}