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
// Déclare la classe AuthentificationActivity, qui hérite d'AppCompatActivity.

    private FirebaseAuth auth;
    private TextView compte, errorText;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Méthode appelée à la création de l'activité.
        super.onCreate(savedInstanceState);
        // Appelle la méthode de la superclasse pour initialiser l'activité.

        setContentView(R.layout.authentification_home);
        // Définit le fichier de mise en page associé à cette activité.

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.authentification), (v, insets) -> {
            // Gestion des marges pour les barres système (haut et bas).
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Récupère les marges des barres système.

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            // Applique un padding pour éviter que le contenu soit masqué par les barres système.

            return insets;
            // Retourne les marges pour maintenir leur état.
        });

        auth = FirebaseAuth.getInstance();
        // Initialise FirebaseAuth pour les opérations d'authentification.

        connectButton = findViewById(R.id.Connect);
        // Associe le bouton de connexion à son ID dans le fichier XML.

        compte = findViewById(R.id.creercompte);
        // Associe le texte "Créer un compte" à son ID dans le fichier XML.

        errorText = findViewById(R.id.errorText);
        // Associe le champ de texte pour afficher les messages d'erreur.

        connectButton.setOnClickListener(v -> {
            // Déclare une action à effectuer lors du clic sur le bouton de connexion.

            TextView LoginField = findViewById(R.id.Email);
            // Associe le champ d'email saisi par l'utilisateur.

            TextView PasswordField = findViewById(R.id.Password);
            // Associe le champ de mot de passe saisi par l'utilisateur.

            String email = LoginField.getText().toString().trim();
            // Récupère l'email saisi et supprime les espaces superflus.

            String password = PasswordField.getText().toString().trim();
            // Récupère le mot de passe saisi et supprime les espaces superflus.

            if (email.isEmpty() || password.isEmpty()) {
                // Vérifie si un des champs est vide.
                errorText.setText("Veuillez remplir tous les champs !");
                // Définit un message d'erreur pour les champs vides.

                errorText.setVisibility(View.VISIBLE);
                // Rendre le message d'erreur visible.

                return;
                // Interrompt l'exécution si les champs ne sont pas remplis.
            }

            auth.signInWithEmailAndPassword(email, password)
                    // Utilise Firebase pour tenter une connexion avec l'email et le mot de passe.
                    .addOnCompleteListener(task -> {
                        // Ajoute un listener pour capturer le résultat de la tentative.
                        if (task.isSuccessful()) {
                            // Vérifie si la connexion a réussi.
                            FirebaseUser user = auth.getCurrentUser();
                            // Récupère l'utilisateur actuellement connecté.

                            Toast.makeText(AuthentificationActivity.this,
                                    "Connecté en tant que " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            // Affiche un message de succès avec l'email de l'utilisateur.

                            Intent intent = new Intent(AuthentificationActivity.this, HomeActivity.class);
                            // Crée une intention pour rediriger l'utilisateur vers la page principale (HomeActivity).

                            startActivity(intent);
                            // Lance l'activité HomeActivity.

                            finish();
                            // Ferme l'activité actuelle pour éviter un retour en arrière.
                        } else {
                            // Si la connexion échoue.
                            errorText.setText("Erreur de connexion : " + task.getException().getMessage());
                            // Affiche l'erreur retournée par Firebase.

                            errorText.setVisibility(View.VISIBLE);
                            // Rendre visible le message d'erreur.
                        }
                    });
        });

        compte.setOnClickListener(v -> {
            // Déclare une action à effectuer lorsque l'utilisateur clique sur "Créer un compte".

            Intent intent = new Intent(AuthentificationActivity.this, CompteActivity.class);
            // Crée une intention pour rediriger l'utilisateur vers l'activité de création de compte.

            startActivity(intent);
            // Lance l'activité CompteActivity.
        });
    }
}
