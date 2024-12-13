package com.example.test;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
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
                //envoyerLike(compteur);
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
        messageData.put("score", 0);
        // Ajouter le message à la collection "messages" dans Firestore
        firestore.collection("messages")
                .add(messageData) // Ajouter les données à Firestore
                .addOnSuccessListener(documentReference -> newMessage.setText("")); // Vider le champ après envoi
    }

    // Méthode pour charger les messages depuis Firestore
    private void chargerMessages() {
        firestore.collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Map<String, Object>> listeMessages = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String message = doc.getString("message");
                        long timestamp = doc.getLong("timestamp");
                        int count = doc.contains("count") ? doc.getLong("count").intValue() : 0;

                        double score = calculerScore(timestamp, count);

                        Map<String, Object> messageData = new HashMap<>();
                        messageData.put("message", message);
                        messageData.put("score", score);
                        messageData.put("docId", doc.getId());
                        listeMessages.add(messageData);

                        afficherMessageLocal(message, doc.getId(), count);
                    }

                    listeMessages.sort((m1, m2) -> Double.compare(
                            (double) m2.get("score"),
                            (double) m1.get("score")
                    ));
                    discussionContainer.removeAllViews();

                    for (Map<String, Object> msg : listeMessages) {
                        afficherMessageLocal(
                                (String) msg.get("message"),
                                (String) msg.get("docId"),
                                ((Double) msg.get("score")).intValue()
                        );
                    }
                });
}

    private double calculerScore(long timestamp, int likes) {
        long maintenant = System.currentTimeMillis();
        long deltaTemps = maintenant - timestamp; // Âge du message en millisecondes
        double deltaJours = deltaTemps / (1000.0 * 60 * 60 * 24); // Convertir en jours

        // Si le message a plus de 30 jours, on le classe en dernier
        if (deltaJours > 30) return 0;

        // Calcul du score d'actualité
        return 30 - deltaJours - likes;
    }
    private void aimerMessage(String messageId, int currentLikes) {
        // Mettre à jour le nombre de likes
        firestore.collection("messages")
                .document(messageId)
                .update("likes", currentLikes + 1) // Ajouter un like
                .addOnSuccessListener(aVoid -> {
                    // Optionnel : Afficher une notification ou un changement d'interface
                })
                .addOnFailureListener(e -> {
                    // Gérer les erreurs si nécessaire
                });
    }

    // Méthode pour afficher un message dans l'interface utilisateur
    private void afficherMessageLocal(String message, String docId, int count) {
        // Créer un conteneur vertical pour chaque message
        LinearLayout messageContainer = new LinearLayout(this);
        messageContainer.setOrientation(LinearLayout.VERTICAL); // Orientation verticale pour tout le message
        messageContainer.setPadding(0, 5, 0, 0);

        // Créer un conteneur horizontal pour le bouton "like" et le compteur
        LinearLayout likeContainer = new LinearLayout(this);
        likeContainer.setOrientation(LinearLayout.HORIZONTAL); // Orientation horizontale
        likeContainer.setPadding(10, 0, 0, 10);
        likeContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Créer un bouton "like"
        ImageButton like = new ImageButton(this);
        like.setImageResource(R.drawable.coeur_vide); // Image du cœur vide
        like.setBackground(null); // Supprimer le fond du bouton
        LinearLayout.LayoutParams likeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        like.setLayoutParams(likeParams);

        // Créer un compteur pour les likes
        TextView likeCompteur = new TextView(this);
        likeCompteur.setText("0");
        likeCompteur.setTextSize(16); // Taille du texte du compteur
        likeCompteur.setPadding(0, 19, 0, 0); // Espacement entre le cœur et le compteur

        // Gérer les clics sur le bouton "like"
        final int[] compteur = {0};
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compteur[0]++;
                aimerMessage(docId, compteur[0]);
                like.setImageResource(R.drawable.coeur_plein); // Change l'image à "coeur plein"
                likeCompteur.setText(String.valueOf(compteur[0])); // Met à jour le compteur
            }
        });

        // Ajouter le bouton et le compteur au conteneur horizontal
        likeContainer.addView(like);
        likeContainer.addView(likeCompteur);

        // Créer un champ de texte pour le message
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setBackgroundResource(R.drawable.message_background);
        messageView.setPadding(30, 20, 20, 30);

        // Ajouter les conteneurs au conteneur principal
        messageContainer.addView(messageView);
        messageContainer.addView(likeContainer);

        // Ajouter le tout au conteneur des discussions
        discussionContainer.addView(messageContainer);
    }
}
