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

public class CompteActivity extends AppCompatActivity {

    private FirebaseAuth auth; // Instance pour gérer l'authentification Firebase
    private EditText email, password, password2; // Champs pour l'email et les mots de passe
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

        // Initialisation de FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Associer les champs de l'interface utilisateur
        email = findViewById(R.id.Create_email);
        password = findViewById(R.id.Create_Password);
        password2 = findViewById(R.id.Create_Password2);
        create = findViewById(R.id.Create_button);

        // Ajouter un listener au bouton de création de compte
        create.setOnClickListener(v -> {
            // Récupérer les données des champs
            String mail = email.getText().toString().trim(); // Email entré par l'utilisateur - trim() pour supprimer les espaces inutiles
            String pass = password.getText().toString(); // Mot de passe
            String confirmPass = password2.getText().toString(); // Confirmation du mot de passe

            TextView errorText = findViewById(R.id.errorText); // Zone pour afficher des erreurs

            // Vérifier si les champs sont vides
            if (mail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
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
                            Toast.makeText(CompteActivity.this, "Compte créé : " + user.getEmail(), Toast.LENGTH_SHORT).show();

                            // Passer à la page d'authentification
                            Intent intent = new Intent(CompteActivity.this, AuthentificationActivity.class);
                            startActivity(intent);
                            finish(); // Fermer l'activité actuelle
                        } else {
                            // En cas d'échec, afficher l'erreur
                            errorText.setText("Erreur : " + task.getException().getMessage());
                            errorText.setVisibility(View.VISIBLE);
                        }
                    });
        });
    }
}