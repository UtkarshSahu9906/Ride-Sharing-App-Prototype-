package com.utkarsh.ridesharingappprototype.utils;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.utkarsh.ridesharingappprototype.R;

public class FirebaseManager {
    private static FirebaseManager instance;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public void registerUser(String email, String password, String userType,
                             OnRegistrationCompleteListener listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Save additional user info to database
                            DatabaseReference usersRef = database.getReference("users").child(user.getUid());
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("userType", userType);

                            usersRef.setValue(userData)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            listener.onSuccess(userType);
                                        } else {
                                            listener.onFailure(dbTask.getException());
                                        }
                                    });
                        }
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public interface OnRegistrationCompleteListener {
        void onSuccess(String userType);
        void onFailure(Exception e);
    }
}