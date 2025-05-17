package com.app.fintoday;
//Criado em 16.05.25

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
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
            FirebaseDatabase.getInstance().setPersistenceEnabled(true); // Habilita persistência offline do Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference();
            firebaseAuth = FirebaseAuth.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar FirebaseHelper", e);
        }
    }

    public static synchronized FirebaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseHelper(context);
        }
        return instance;
    }
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Metodo para sincronizar dados locais com o Firebase
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

    // Metodo para sincronizar um único item, verificar
    public void syncItemToFirebase(FinModal item) {
        executorService.execute(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null) {
                    Log.e(TAG, "Usuário não autenticado");
                    return;
                }

                item.setLastUpdated(System.currentTimeMillis());// Atualiza timestamp e envia apenas o item editado

                // Usando o mesmo caminho que syncAllItemsToFirebase
                databaseReference.child("finances")
                        .child(userId)
                        .child(String.valueOf(item.getId()))
                        .setValue(item)
                        .addOnSuccessListener(aVoid ->
                                Log.d(TAG, "Item sincronizado: " + item.getId()))
                        .addOnFailureListener(e ->
                                Log.e(TAG, "Erro ao sincronizar item", e));
            } catch (Exception e) {
                Log.e(TAG, "Erro geral", e);
            }
        });
    }
    public void syncAllItemsToFirebase(List<FinModal> items) {
        if (items == null || items.isEmpty()) return;

        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e("FirebaseHelper", "Usuário não autenticado");
            return;
        }

        DatabaseReference userRef = databaseReference.child("finances").child(userId);

        for (FinModal item : items) {
            userRef.child(String.valueOf(item.getId()))
                    .setValue(item)
                    .addOnFailureListener(e ->
                            Log.e("FirebaseSync", "Erro ao sincronizar item " + item.getId(), e));
        }
    }

    // Metodo para fazer backup do banco de dados SQLite para o Firebase Storage
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
    // No FirebaseHelper
    public DatabaseReference getUserFinancesReference() {
        String userId = getCurrentUserId();
        if (userId == null) return null;
        return databaseReference.child("users").child(userId).child("finances");
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }

    public void setupFirebaseListener(FinRepository repository) {
        DatabaseReference userFinancesRef = getUserFinancesReference();
        if (userFinancesRef != null) {
            userFinancesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Atualizar SQLite com dados do Firebase
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        FinModal modal = dataSnapshot.getValue(FinModal.class);
                        if (modal != null) {
                            repository.syncFromFirebase(modal);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase listener cancelled", error.toException());
                }
            });
        }
    }
}