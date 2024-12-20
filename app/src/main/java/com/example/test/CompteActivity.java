package com.example.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CompteActivity extends AppCompatActivity {

    private FirebaseAuth auth; // Instance pour gérer l'authentification Firebase
    private FirebaseFirestore firestore; // Instance pour Firestore
    private EditText email, password, password2, username; // Champs pour l'email, mot de passe et nom d'utilisateur
    private Button create; // Bouton pour créer un compte

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compte);

        // Gérer les barres système pour éviter que les éléments ne soient masqués
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation de FirebaseAuth et Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Associer les champs de l'interface utilisateur
        email = findViewById(R.id.Create_email);
        password = findViewById(R.id.Create_Password);
        password2 = findViewById(R.id.Create_Password2);
        username = findViewById(R.id.Create_Username);
        create = findViewById(R.id.Create_button);

        // Ajouter un listener au bouton de création de compte
        create.setOnClickListener(v -> {
            // Récupérer les données des champs
            String mail = email.getText().toString().trim(); // Email entré par l'utilisateur - trim() pour supprimer les espaces inutiles
            String pass = password.getText().toString(); // Mot de passe
            String confirmPass = password2.getText().toString(); // Confirmation du mot de passe
            String userName = username.getText().toString().trim(); // Nom d'utilisateur

            TextView errorText = findViewById(R.id.errorText); // Zone pour afficher des erreurs

            // Vérifier si les champs sont vides
            if (mail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() || userName.isEmpty()) {
                errorText.setText("Veuillez remplir tous les champs."); // Afficher un message d'erreur
                errorText.setVisibility(View.VISIBLE); // Rendre visible l'erreur
                return;
            }

            // Vérifier si les mots de passe correspondent
            if (!pass.equals(confirmPass)) {
                errorText.setText("Les mots de passe ne correspondent pas !"); // Afficher une erreur
                errorText.setVisibility(View.VISIBLE);
                return;
            }

            // Créer un utilisateur avec FirebaseAuth
            auth.createUserWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(CompteActivity.this, task -> {
                        if (task.isSuccessful()) {
                            // Si la création est un succès
                            FirebaseUser user = auth.getCurrentUser(); // Obtenir l'utilisateur connecté
                            if (user != null) {
                                // Enregistrer les informations utilisateur dans Firestore
                                String userId = user.getUid(); // Obtenir l'ID unique de l'utilisateur
                                saveUserToFirestore(userId, userName, mail); // Appel de la méthode pour enregistrer
                            }
                        } else {
                            // En cas d'échec, afficher l'erreur
                            errorText.setText("Erreur : " + task.getException().getMessage());
                            errorText.setVisibility(View.VISIBLE);
                        }
                    });
        });
    }

    // Méthode pour enregistrer l'utilisateur dans Firestore
    private void saveUserToFirestore(String userId, String username, String email) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username); // Ajouter le nom d'utilisateur
        userMap.put("email", email); // Ajouter l'email

        firestore.collection("Users").document(userId)
                .set(userMap) // Ajouter ou remplacer le document avec l'ID utilisateur
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CompteActivity.this, "Compte créé avec succès.", Toast.LENGTH_SHORT).show();
                        // Passer à la page d'authentification
                        Intent intent = new Intent(CompteActivity.this, AuthentificationActivity.class);
                        startActivity(intent);
                        finish(); // Fermer l'activité actuelle
                    } else {
                        TextView errorText = findViewById(R.id.errorText);
                        errorText.setText("Erreur Firestore : " + task.getException().getMessage());
                        errorText.setVisibility(View.VISIBLE);
                    }
                });
    }
}
