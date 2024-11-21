package com.example.test;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button envoyer = findViewById(R.id.Envoi);
        EditText newMessage = findViewById(R.id.NewMessage);
        LinearLayout discussionContainer = findViewById(R.id.discussionContainer);

        envoyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = newMessage.getText().toString().trim();
                    TextView messageView = new TextView(HomeActivity.this);
                    messageView.setText(message);
                    messageView.setBackgroundResource(R.drawable.message_background);
                    messageView.setPadding(30, 20, 20, 30);

                ImageButton like = new ImageButton(HomeActivity.this); //Création du bouton coeur
                like.setImageResource(R.drawable.coeur_vide); //Mettre le bouton à l'image "coeur"
                like.setBackground(null);


                TextView likeCompteur = new TextView(HomeActivity.this);
                likeCompteur.setText("0");
                likeCompteur.setPadding(16, 0, 0, 0);

                final int[] compteur = {0};

                like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        compteur[0]++;
                        like.setImageResource(R.drawable.coeur_plein);
                        likeCompteur.setText(String.valueOf(compteur[0]));
                    }
                });
                discussionContainer.addView(messageView);
                discussionContainer.addView(like);
                discussionContainer.addView(likeCompteur);

                newMessage.setText("");
                }
        });
    }
}