package com.example.test;
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
// Déclare la classe CompteActivity, qui hérite d'AppCompatActivity.

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private EditText email, password, password2, username;
    private Button create;
    private TextView home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Méthode appelée à la création de l'activité.
        super.onCreate(savedInstanceState);
        // Appelle la méthode de la superclasse pour initialiser l'activité.

        setContentView(R.layout.activity_compte);
        // Définit le fichier de layout pour cette activité.

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Gère les marges liées aux barres système pour éviter qu'elles ne masquent le contenu.
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Obtient les marges des barres système.

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            // Applique les marges aux côtés correspondants de la vue.

            return insets;
            // Retourne les insets pour maintenir leur état.
        });

        auth = FirebaseAuth.getInstance();
        // Initialise FirebaseAuth pour gérer l'authentification.

        firestore = FirebaseFirestore.getInstance();
        // Initialise Firestore pour interagir avec la base de données.

        email = findViewById(R.id.Create_email);
        // Associe l'ID du champ email au composant du layout.

        password = findViewById(R.id.Create_Password);
        // Associe l'ID du champ mot de passe au composant du layout.

        password2 = findViewById(R.id.Create_Password2);
        // Associe l'ID du champ de confirmation du mot de passe.

        username = findViewById(R.id.Create_Username);
        // Associe l'ID du champ nom d'utilisateur au composant du layout.

        create = findViewById(R.id.Create_button);
        // Associe l'ID du bouton de création de compte.

        home = findViewById(R.id.Retour_home);
        // Associe l'ID du bouton pour retourner à l'accueil.

        create.setOnClickListener(v -> {
            // Définit une action à effectuer lors du clic sur le bouton.

            String mail = email.getText().toString().trim();
            // Récupère l'email saisi et supprime les espaces inutiles.

            String pass = password.getText().toString();
            // Récupère le mot de passe saisi.

            String confirmPass = password2.getText().toString();
            // Récupère le mot de passe de confirmation saisi.

            String userName = username.getText().toString().trim();
            // Récupère le nom d'utilisateur saisi et supprime les espaces.

            TextView errorText = findViewById(R.id.errorText);
            // Associe une zone pour afficher les erreurs.

            if (mail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() || userName.isEmpty()) {
                // Vérifie si un des champs est vide.
                errorText.setText("Veuillez remplir tous les champs.");
                // Affiche un message d'erreur.

                errorText.setVisibility(View.VISIBLE);
                // Rend le message d'erreur visible.

                return;
                // Arrête l'exécution si les champs ne sont pas remplis.
            }

            if (!pass.equals(confirmPass)) {
                // Vérifie si les mots de passe ne correspondent pas.
                errorText.setText("Les mots de passe ne correspondent pas !");
                // Affiche une erreur spécifique.

                errorText.setVisibility(View.VISIBLE);
                // Rend le message d'erreur visible.

                return;
                // Arrête l'exécution si les mots de passe ne correspondent pas.
            }

            auth.createUserWithEmailAndPassword(mail, pass)
                    // Demande à Firebase de créer un utilisateur avec email et mot de passe.
                    .addOnCompleteListener(CompteActivity.this, task -> {
                        // Ajoute un listener pour capturer le résultat.
                        if (task.isSuccessful()) {
                            // Si la création réussit.
                            FirebaseUser user = auth.getCurrentUser();
                            // Récupère l'utilisateur connecté.

                            if (user != null) {
                                // Vérifie si l'utilisateur existe.
                                String userId = user.getUid();
                                // Obtient l'ID unique de l'utilisateur.

                                saveUserToFirestore(userId, userName, mail);
                                // Appelle une méthode pour sauvegarder les informations dans Firestore.
                            }
                        } else {
                            // Si la création échoue.
                            errorText.setText("Erreur : " + task.getException().getMessage());
                            // Affiche l'erreur retournée par Firebase.

                            errorText.setVisibility(View.VISIBLE);
                            // Rend le message d'erreur visible.
                        }
                    });
        });

        home.setOnClickListener(v -> {
            // Ajoute un listener au bouton pour retourner à l'accueil.
            Intent intent = new Intent(CompteActivity.this, AuthentificationActivity.class);
            // Crée une intention pour passer à l'activité d'authentification.

            startActivity(intent);
            // Lance l'activité d'authentification.
        });
    }

    private void saveUserToFirestore(String userId, String username, String email) {
        // Déclare une méthode pour sauvegarder les informations dans Firestore.

        Map<String, Object> userMap = new HashMap<>();
        // Crée une Map pour stocker les données utilisateur.

        userMap.put("username", username);
        // Ajoute le nom d'utilisateur à la Map.

        userMap.put("email", email);
        // Ajoute l'email à la Map.

        firestore.collection("Users").document(userId)
                // Référence la collection "Users" dans Firestore et utilise l'ID utilisateur comme clé.
                .set(userMap)
                // Ajoute ou remplace le document utilisateur.
                .addOnCompleteListener(task -> {
                    // Ajoute un listener pour capturer le résultat.
                    if (task.isSuccessful()) {
                        // Si l'ajout réussit.
                        Toast.makeText(CompteActivity.this, "Compte créé avec succès.", Toast.LENGTH_SHORT).show();
                        // Affiche un message de succès.

                        Intent intent = new Intent(CompteActivity.this, AuthentificationActivity.class);
                        // Crée une intention pour passer à l'activité d'authentification.

                        startActivity(intent);
                        // Lance l'activité d'authentification.

                        finish();
                        // Ferme l'activité actuelle.
                    } else {
                        // Si l'ajout échoue.
                        TextView errorText = findViewById(R.id.errorText);
                        // Récupère la zone d'erreur.

                        errorText.setText("Erreur Firestore : " + task.getException().getMessage());
                        // Affiche l'erreur retournée par Firestore.

                        errorText.setVisibility(View.VISIBLE);
                        // Rend le message d'erreur visible.
                    }
                });
    }
}