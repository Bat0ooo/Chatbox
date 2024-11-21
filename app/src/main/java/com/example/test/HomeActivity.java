package com.example.test;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private EditText newMessage; // Champ de texte pour écrire un message
    private LinearLayout discussionContainer; // Conteneur pour afficher les messages
    private FirebaseFirestore firestore; // Instance Firestore pour gérer la base de données

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Lier les éléments de l'interface aux variables Java
        newMessage = findViewById(R.id.NewMessage);
        discussionContainer = findViewById(R.id.discussionContainer);

        // Initialiser Firestore
        firestore = FirebaseFirestore.getInstance();

        // Configurer le bouton d'envoi
        findViewById(R.id.Envoi).setOnClickListener(v -> {
            String message = newMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) { // Vérifier si le message n'est pas vide
                envoyerMessage(message); // Envoi du message à Firestore
            }
        });

        // Charger les messages existants dès le démarrage
        chargerMessages();
    }

    // Méthode pour envoyer un message à Firestore
    private void envoyerMessage(String message) {
        // Créer un objet contenant les données du message
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", message); // Le texte du message
        messageData.put("timestamp", System.currentTimeMillis()); // Timestamp pour trier les messages

        // Ajouter le message à la collection "messages" dans Firestore
        firestore.collection("messages")
                .add(messageData) // Ajouter les données à Firestore
                .addOnSuccessListener(documentReference -> newMessage.setText("")); // Vider le champ après envoi
    }

    // Méthode pour charger les messages depuis Firestore
    private void chargerMessages() {
        firestore.collection("messages")
                .orderBy("timestamp") // Trier les messages par ordre chronologique
                .addSnapshotListener((snapshots, e) -> { // Écouter les mises à jour en temps réel
                    if (snapshots != null) {
                        discussionContainer.removeAllViews(); // Effacer les anciens messages

                        // Parcourir les documents reçus
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String message = doc.getString("message"); // Récupérer le texte du message
                            afficherMessageLocal(message); // Afficher le message dans l'interface
                        }
                    }
                });
    }

    // Méthode pour afficher un message dans l'interface utilisateur
    private void afficherMessageLocal(String message) {
        TextView messageView = new TextView(this); // Créer un nouveau champ de texte
        messageView.setText(message); // Ajouter le contenu du message
        messageView.setBackgroundResource(R.drawable.message_background); // Ajouter un style (facultatif)
        discussionContainer.addView(messageView); // Ajouter le message à la vue
    }
}
