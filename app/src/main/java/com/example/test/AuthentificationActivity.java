package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthentificationActivity extends AppCompatActivity {

    private FirebaseAuth auth; // FirebaseAuth pour gérer l'authentification
    private TextView compte, errorText; // TextViews pour "Créer un compte" et pour afficher des erreurs
    private Button connectButton; // Bouton pour se connecter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentification_home); // Définit le layout de cette activité

        // Ajuste les insets pour prendre en compte les barres de statut et de navigation (optionnel)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.authentification), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // Applique un padding pour éviter que l'interface soit cachée par les barres système
            return insets;
        });

        // Initialiser FirebaseAuth
        auth = FirebaseAuth.getInstance(); // Récupère l'instance de FirebaseAuth pour gérer les connexions

        // Récupérer les vues associées
        connectButton = findViewById(R.id.Connect); // Le bouton de connexion
        compte = findViewById(R.id.creercompte); // Le texte "Créer un compte"
        errorText = findViewById(R.id.errorText); // Affichage des erreurs

        // Gestion du clic sur le bouton "Se connecter"
        connectButton.setOnClickListener(v -> {
            // Récupérer les champs de texte pour l'email et le mot de passe
            TextView LoginField = findViewById(R.id.Email);
            TextView PasswordField = findViewById(R.id.Password);

            // Récupérer le texte des champs et le nettoyer des espaces superflus
            String email = LoginField.getText().toString().trim();
            String password = PasswordField.getText().toString().trim();

            // Vérifier si les champs sont vides
            if (email.isEmpty() || password.isEmpty()) {
                // Si l'un des champs est vide, afficher un message d'erreur
                errorText.setText("Veuillez remplir tous les champs !");
                errorText.setVisibility(View.VISIBLE); // Affiche l'erreur
                return; // Quitte la méthode si les champs ne sont pas remplis
            }

            // Tentative de connexion avec Firebase avec l'email et le mot de passe
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Connexion réussie, affichage d'un toast et redirection vers HomeActivity
                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(AuthentificationActivity.this, "Connecté en tant que " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AuthentificationActivity.this, HomeActivity.class);
                            startActivity(intent); // Ouvre HomeActivity
                            finish(); // Ferme l'activité actuelle
                        } else {
                            // Connexion échouée, affichage du message d'erreur
                            errorText.setText("Erreur de connexion : " + task.getException().getMessage());
                            errorText.setVisibility(View.VISIBLE); // Affiche l'erreur
                        }
                    });
        });

        // Gestion du clic sur "Créer un compte" pour rediriger vers la page d'inscription
        compte.setOnClickListener(v -> {
            Intent intent = new Intent(AuthentificationActivity.this, CompteActivity.class);
            startActivity(intent); // Ouvre l'activité de création de compte
        });
    }
}
