package com.example.test;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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

    private EditText newMessage;  // Champ de saisie pour le nouveau message
    private LinearLayout discussionContainer;  // Conteneur qui affiche les messages
    private FirebaseFirestore firestore;  // Instance Firestore pour interagir avec la base de données
    private String currentUserId;  // Identifiant de l'utilisateur actuel
    private String currentUsername;  // Nom d'utilisateur de l'utilisateur actuel
    private ImageView userMenuButton;  // Bouton pour afficher le menu utilisateur

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);  // Définir le layout pour l'activité

        // Initialiser les éléments de l'interface utilisateur
        newMessage = findViewById(R.id.NewMessage);  // Champ de texte pour le message
        discussionContainer = findViewById(R.id.discussionContainer);  // Conteneur pour afficher les messages
        userMenuButton = findViewById(R.id.Menu);  // Bouton pour afficher le menu utilisateur

        firestore = FirebaseFirestore.getInstance();  // Initialiser l'instance Firestore

        // Vérifier si l'utilisateur est authentifié via Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {  // Si l'utilisateur est connecté
            currentUserId = currentUser.getUid();  // Récupérer l'ID utilisateur
            leUsername();  // Récupérer le nom d'utilisateur de l'utilisateur connecté
        } else {
            currentUserId = null;  // Si aucun utilisateur n'est connecté, l'ID est nul
        }

        // Configurer l'événement du bouton pour envoyer un message
        findViewById(R.id.Envoi).setOnClickListener(v -> {
            String message = newMessage.getText().toString().trim();  // Récupérer le message tapé
            if (!TextUtils.isEmpty(message)) {  // Vérifier si le message n'est pas vide
                envoyerMessage(message);  // Envoyer le message à la base de données
            }
        });

        chargerMessages();  // Charger les messages existants de Firestore

        // Configuration du menu utilisateur pour afficher le nom d'utilisateur et la déconnexion
        userMenuButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(HomeActivity.this, view);  // Créer un menu popup
            popupMenu.getMenu().add(0, 0, 0, currentUsername);  // Ajouter le nom d'utilisateur dans le menu
            popupMenu.getMenu().add(0, 1, 1, "Se déconnecter");  // Ajouter l'option de déconnexion

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 1) {  // Si l'utilisateur choisit de se déconnecter
                    FirebaseAuth.getInstance().signOut();  // Déconnecter l'utilisateur
                    Intent intent = new Intent(HomeActivity.this, AuthentificationActivity.class);  // Créer un nouvel Intent pour l'écran de connexion
                    startActivity(intent);  // Lancer l'écran de connexion
                    finish();  // Fermer l'activité actuelle (HomeActivity)
                }
                return true;
            });

            popupMenu.show();  // Afficher le menu
        });
    }

    // Méthode pour récupérer le nom d'utilisateur de l'utilisateur connecté
    private void leUsername() {
        firestore.collection("Users")  // Accéder à la collection "Users" dans Firestore
                .document(currentUserId)  // Accéder au document de l'utilisateur actuel
                .get()  // Récupérer les données du document
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {  // Si le document existe
                        currentUsername = documentSnapshot.getString("username");  // Récupérer le nom d'utilisateur
                    }
                });
    }

    // Méthode pour envoyer un message à Firestore
    private void envoyerMessage(String message) {
        Map<String, Object> messageData = new HashMap<>();  // Créer une map pour stocker les données du message
        messageData.put("message", message);  // Ajouter le message
        messageData.put("timestamp", System.currentTimeMillis());  // Ajouter un timestamp du message
        messageData.put("likes", 0);  // Initialiser le nombre de likes à 0
        messageData.put("score", 0);  // Initialiser le score à 0
        messageData.put("username", currentUsername != null ? currentUsername : "Inconnu");  // Ajouter le nom d'utilisateur ou "Inconnu" si non défini

        firestore.collection("messages")  // Accéder à la collection "messages" de Firestore
                .add(messageData)  // Ajouter les données du message à la collection
                .addOnSuccessListener(documentReference -> {
                    newMessage.setText("");  // Réinitialiser le champ de saisie après l'envoi du message
                });
    }

    // Méthode pour charger les messages depuis Firestore
    private void chargerMessages() {
        firestore.collection("messages")  // Accéder à la collection "messages"
                .orderBy("score", Query.Direction.DESCENDING)  // Trier les messages par score de manière décroissante
                .addSnapshotListener((snapshots, e) -> {  // Observer les changements en temps réel
                    if (snapshots != null) {  // Si des documents sont récupérés
                        discussionContainer.removeAllViews();  // Supprimer tous les messages existants

                        for (QueryDocumentSnapshot doc : snapshots) {  // Pour chaque message dans la collection
                            String message = doc.getString("message");  // Récupérer le message
                            String messageId = doc.getId();  // Récupérer l'ID du message
                            long timestamp = doc.getLong("timestamp");  // Récupérer le timestamp du message
                            long currentTime = System.currentTimeMillis();  // Obtenir l'heure actuelle
                            long ageInMillis = currentTime - timestamp;  // Calculer l'âge du message en millisecondes
                            long ageInDays = ageInMillis / (1000 * 60 * 60 * 24);  // Convertir l'âge en jours

                            if (ageInDays > 30) {  // Si le message a plus de 30 jours
                                firestore.collection("messages")  // Supprimer le message de Firestore
                                        .document(messageId)
                                        .delete();
                            } else {
                                int likes = doc.getLong("likes") != null ? doc.getLong("likes").intValue() : 0;  // Récupérer le nombre de likes
                                int score = doc.getLong("score") != null ? doc.getLong("score").intValue() : 0;  // Récupérer le score du message
                                String username = doc.getString("username");  // Récupérer le nom d'utilisateur
                                afficherMessageLocal(message, messageId, likes, score, username, timestamp);  // Afficher le message localement
                            }
                        }
                    }
                });
    }

    // Méthode pour afficher un message dans l'interface
    private void afficherMessageLocal(String message, String messageId, int likes, int score, String username, long timestamp) {
        CardView cardView = new CardView(this);  // Créer une nouvelle CardView pour afficher le message
        CardView.LayoutParams cardParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(20, 20, 20, 20);  // Ajouter des marges autour de la CardView
        cardView.setLayoutParams(cardParams);  // Appliquer les paramètres de mise en page à la CardView
        cardView.setRadius(15);  // Arrondir les coins de la CardView
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));  // Définir la couleur de fond
        cardView.setContentPadding(20, 20, 20, 0);  // Ajouter du padding à l'intérieur de la CardView
        cardView.setElevation(10);  // Ajouter une élévation (ombre)

        LinearLayout messageContainer = new LinearLayout(this);  // Créer un conteneur pour le message
        messageContainer.setOrientation(LinearLayout.VERTICAL);  // Organiser les éléments de manière verticale
        messageContainer.setGravity(Gravity.START);  // Aligner le contenu à gauche

        // Créer un conteneur pour l'icône de l'utilisateur et son nom
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);  // Disposition horizontale
        headerLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);  // Aligner à gauche et centrer verticalement

        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        headerLayout.setLayoutParams(headerParams);  // Appliquer les paramètres au conteneur de l'en-tête

        // Création de l'arrière-plan avec coins arrondis
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);  // Définir la forme comme un rectangle
        drawable.setCornerRadius(30);  // Arrondir les coins
        drawable.setColor(getResources().getColor(android.R.color.darker_gray));  // Définir la couleur de fond
        headerLayout.setBackground(drawable);  // Appliquer l'arrière-plan

        // Créer et ajouter l'icône de l'utilisateur
        ImageView userIcon = new ImageView(this);
        userIcon.setImageResource(R.drawable.personne_rond);  // Définir l'icône de l'utilisateur
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(50, 50);  // Définir la taille de l'icône
        iconParams.setMargins(10, 10, 5, 10);  // Ajouter des marges autour de l'icône
        userIcon.setLayoutParams(iconParams);  // Appliquer les paramètres de mise en page à l'icône

        // Créer et ajouter le nom d'utilisateur
        TextView userNameView = new TextView(this);
        userNameView.setText(username != null ? username : "Inconnu");  // Afficher le nom d'utilisateur ou "Inconnu"
        userNameView.setTextColor(getResources().getColor(android.R.color.white));  // Définir la couleur du texte
        userNameView.setTextSize(16);  // Définir la taille du texte
        userNameView.setTypeface(null, Typeface.BOLD);  // Définir le texte en gras
        userNameView.setPadding(5, 10, 10, 10);  // Ajouter un padding autour du texte

        // Ajouter l'icône et le nom d'utilisateur dans le conteneur horizontal
        headerLayout.addView(userIcon);  // Ajouter l'icône
        headerLayout.addView(userNameView);  // Ajouter le nom d'utilisateur

        // Créer et ajouter le message
        TextView messageView = new TextView(this);
        messageView.setText(message);  // Afficher le message
        messageView.setTextSize(16);  // Définir la taille du texte
        messageView.setTextColor(getResources().getColor(android.R.color.black));  // Définir la couleur du texte

        // Créer un conteneur pour les boutons de likes
        LinearLayout likeContainer = new LinearLayout(this);
        likeContainer.setOrientation(LinearLayout.HORIZONTAL);  // Disposition horizontale
        likeContainer.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);  // Aligner à droite et centrer verticalement

        // Créer un bouton de like avec une image
        ImageButton likeButton = new ImageButton(this);
        likeButton.setImageResource(R.drawable.coeur_vide);  // Définir l'icône du bouton "like"
        likeButton.setBackground(null);  // Enlever le fond par défaut du bouton
        LinearLayout.LayoutParams likeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT  // Définir la taille du bouton "like"
        );
        likeButton.setLayoutParams(likeParams);  // Appliquer les paramètres au bouton de like

        // Créer un TextView pour afficher le nombre de likes
        TextView likeCounter = new TextView(this);
        likeCounter.setText(String.valueOf(likes));  // Afficher le nombre de likes
        likeCounter.setTextSize(16);  // Définir la taille du texte du compteur de likes

        // Vérification si l'utilisateur a déjà liké ce message
        firestore.collection("messages")
                .document(messageId)  // Accéder au document du message
                .get()  // Récupérer le document
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.contains("userLikes")) {  // Vérifier si le champ "userLikes" existe
                        Map<String, Boolean> userLikes = (Map<String, Boolean>) documentSnapshot.get("userLikes");
                        if (userLikes != null && userLikes.containsKey(currentUserId) && userLikes.get(currentUserId)) {
                            likeButton.setImageResource(R.drawable.coeur_plein);  // Si l'utilisateur a liké, changer l'icône
                        }
                    }
                });

        // Action lorsque l'utilisateur clique sur le bouton "like"
        likeButton.setOnClickListener(v -> {
            firestore.collection("messages")
                    .document(messageId)  // Accéder au document du message
                    .get()  // Récupérer le document
                    .addOnSuccessListener(documentSnapshot -> {
                        int currentLikes = documentSnapshot.getLong("likes").intValue();  // Récupérer le nombre actuel de likes
                        Map<String, Boolean> userLikes = (Map<String, Boolean>) documentSnapshot.get("userLikes");
                        if (userLikes == null) userLikes = new HashMap<>();  // Initialiser si "userLikes" est nul

                        // Vérifier si l'utilisateur a déjà liké ce message
                        boolean isLiked = userLikes.containsKey(currentUserId) && userLikes.get(currentUserId);

                        if (!isLiked) {  // Si l'utilisateur n'a pas encore liké
                            userLikes.put(currentUserId, true);  // Ajouter un like pour l'utilisateur
                            firestore.collection("messages")
                                    .document(messageId)  // Mettre à jour le message dans Firestore
                                    .update("likes", currentLikes + 1,
                                            "userLikes", userLikes,
                                            "score", currentLikes + 1)  // Incrémenter les likes et le score
                                    .addOnSuccessListener(aVoid -> {
                                        likeButton.setImageResource(R.drawable.coeur_plein);  // Changer l'icône pour un cœur plein
                                        likeCounter.setText(String.valueOf(currentLikes + 1));  // Mettre à jour le compteur de likes
                                        chargerMessages();  // Recharger les messages
                                    });
                        } else {  // Si l'utilisateur a déjà liké
                            userLikes.put(currentUserId, false);  // Retirer le like pour l'utilisateur
                            firestore.collection("messages")
                                    .document(messageId)  // Mettre à jour le message dans Firestore
                                    .update("likes", currentLikes - 1,
                                            "userLikes", userLikes,
                                            "score", currentLikes - 1)  // Décrémenter les likes et le score
                                    .addOnSuccessListener(aVoid -> {
                                        likeButton.setImageResource(R.drawable.coeur_vide);  // Changer l'icône pour un cœur vide
                                        likeCounter.setText(String.valueOf(currentLikes - 1));  // Mettre à jour le compteur de likes
                                        chargerMessages();  // Recharger les messages
                                    });
                        }
                    });
        });

        // Ajouter le bouton "like" et le compteur de likes dans le conteneur
        likeContainer.addView(likeButton);  // Ajouter le bouton "like"
        likeContainer.addView(likeCounter);  // Ajouter le compteur de likes

        // Ajouter l'en-tête, le message et les likes dans le conteneur du message
        messageContainer.addView(headerLayout);  // Ajouter l'en-tête avec l'icône et le nom
        messageContainer.addView(messageView);  // Ajouter le texte du message
        messageContainer.addView(likeContainer);  // Ajouter le conteneur des likes

        // Ajouter le conteneur du message dans la CardView
        cardView.addView(messageContainer);  // Ajouter le conteneur du message à la CardView
        discussionContainer.addView(cardView);  // Ajouter la CardView au conteneur principal des discussions
    }
}
