package com.example.test;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private EditText newMessage;
    private LinearLayout discussionContainer;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        newMessage = findViewById(R.id.NewMessage);
        discussionContainer = findViewById(R.id.discussionContainer);

        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            fetchUsername(); // Récupérer le username de l'utilisateur connecté
        } else {
            currentUserId = null;
        }

        findViewById(R.id.Envoi).setOnClickListener(v -> {
            String message = newMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                envoyerMessage(message);
            }
        });

        chargerMessages();
    }

    private void fetchUsername() {
        if (currentUserId != null) {
            firestore.collection("Users")
                    .document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUsername = documentSnapshot.getString("username");
                        }
                    })
                    .addOnFailureListener(e -> {
                        currentUsername = "Inconnu";
                    });
        }
    }

    private void envoyerMessage(String message) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", message);
        messageData.put("timestamp", System.currentTimeMillis());
        messageData.put("likes", 0);
        messageData.put("score", 0);
        messageData.put("username", currentUsername != null ? currentUsername : "Inconnu");

        firestore.collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    newMessage.setText("");
                });
    }

    private void chargerMessages() {
        firestore.collection("messages")
                .orderBy("score", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null) {
                        discussionContainer.removeAllViews();

                        for (QueryDocumentSnapshot doc : snapshots) {
                            String message = doc.getString("message");
                            String messageId = doc.getId();
                            long timestamp = doc.getLong("timestamp");
                            long currentTime = System.currentTimeMillis();
                            long ageInMillis = currentTime - timestamp;
                            long ageInDays = ageInMillis / (1000 * 60 * 60 * 24);

                            if (ageInDays > 30) {
                                firestore.collection("messages")
                                        .document(messageId)
                                        .delete();
                            } else {
                                int likes = doc.getLong("likes") != null ? doc.getLong("likes").intValue() : 0;
                                int score = doc.getLong("score") != null ? doc.getLong("score").intValue() : 0;
                                String username = doc.getString("username");
                                afficherMessageLocal(message, messageId, likes, score, username);
                            }
                        }
                    }
                });
    }
    private void afficherMessageLocal(String message, String messageId, int likes, int score, String username) {
        CardView cardView = new CardView(this);
        CardView.LayoutParams cardParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(20, 20, 20, 20);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(15);
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        cardView.setContentPadding(20, 20, 20, 0);
        cardView.setElevation(10);

        LinearLayout messageContainer = new LinearLayout(this);
        messageContainer.setOrientation(LinearLayout.VERTICAL);
        messageContainer.setGravity(Gravity.START); // Align the message to the left

        // TextView pour afficher le username avec coins arrondis
        TextView userNameView = new TextView(this);
        userNameView.setText(username != null ? username : "Inconnu");
        userNameView.setTextColor(getResources().getColor(android.R.color.white));
        userNameView.setTextSize(16); // Taille du texte du username
        userNameView.setPadding(10, 5, 10, 5);
        userNameView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        // Appliquer un fond arrondi au TextView du username
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(20); // Coins arrondis
        drawable.setColor(getResources().getColor(android.R.color.darker_gray)); // Couleur de fond du username
        userNameView.setBackground(drawable);

        // Définir la largeur de userNameView à "wrap_content" pour s'adapter au texte
        userNameView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // TextView pour afficher le message
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextSize(16);
        messageView.setTextColor(getResources().getColor(android.R.color.black));

        // Conteneur pour le bouton like et le compteur
        LinearLayout likeContainer = new LinearLayout(this);
        likeContainer.setOrientation(LinearLayout.HORIZONTAL);
        likeContainer.setGravity(Gravity.CENTER_VERTICAL | Gravity.END); // Aligner à droite et centrer verticalement

        // Bouton Like
        ImageButton likeButton = new ImageButton(this);
        likeButton.setImageResource(R.drawable.coeur_vide);
        likeButton.setBackground(null);
        LinearLayout.LayoutParams likeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        likeButton.setLayoutParams(likeParams);

        // Compteur de likes
        TextView likeCounter = new TextView(this);
        likeCounter.setText(String.valueOf(likes));
        likeCounter.setTextSize(16);

        firestore.collection("messages")
                .document(messageId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.contains("userLikes")) {
                        Map<String, Boolean> userLikes = (Map<String, Boolean>) documentSnapshot.get("userLikes");
                        if (userLikes != null && userLikes.containsKey(currentUserId) && userLikes.get(currentUserId)) {
                            likeButton.setImageResource(R.drawable.coeur_plein);
                        }
                    }
                });

        likeButton.setOnClickListener(v -> {
            firestore.collection("messages")
                    .document(messageId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        int currentLikes = documentSnapshot.getLong("likes").intValue();
                        Map<String, Boolean> userLikes = (Map<String, Boolean>) documentSnapshot.get("userLikes");
                        if (userLikes == null) userLikes = new HashMap<>();

                        boolean isLiked = userLikes.containsKey(currentUserId) && userLikes.get(currentUserId);

                        if (!isLiked) {
                            userLikes.put(currentUserId, true);
                            firestore.collection("messages")
                                    .document(messageId)
                                    .update("likes", currentLikes + 1,
                                            "userLikes", userLikes,
                                            "score", currentLikes + 1) // Incrémenter le score
                                    .addOnSuccessListener(aVoid -> {
                                        likeButton.setImageResource(R.drawable.coeur_plein);
                                        likeCounter.setText(String.valueOf(currentLikes + 1));
                                        chargerMessages(); // Rafraîchir la liste des messages après avoir mis à jour les likes et le score
                                    });
                        } else {
                            userLikes.put(currentUserId, false);
                            firestore.collection("messages")
                                    .document(messageId)
                                    .update("likes", currentLikes - 1,
                                            "userLikes", userLikes,
                                            "score", currentLikes - 1) // Décrémenter le score
                                    .addOnSuccessListener(aVoid -> {
                                        likeButton.setImageResource(R.drawable.coeur_vide);
                                        likeCounter.setText(String.valueOf(currentLikes - 1));
                                        chargerMessages(); // Rafraîchir la liste des messages après avoir mis à jour les likes et le score
                                    });
                        }
                    });
        });

        likeContainer.addView(likeButton);
        likeContainer.addView(likeCounter);

        messageContainer.addView(userNameView);
        messageContainer.addView(messageView);
        messageContainer.addView(likeContainer);

        cardView.addView(messageContainer);
        discussionContainer.addView(cardView);
    }

}
