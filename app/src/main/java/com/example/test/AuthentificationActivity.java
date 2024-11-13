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

public class AuthentificationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentification_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.authentification), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // cherche le bouton connect et la creation de compte
        Button connectButton = findViewById(R.id.Connect);
        TextView compte = findViewById(R.id.creercompte);

        // quand le bouton est cliqué on passe à l'activité home
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vérifier si les champs sont remplis
                TextView LoginField = findViewById(R.id.Login);
                TextView PasswordField = findViewById(R.id.Password);
                if (LoginField.getText().toString().isEmpty() || PasswordField.getText().toString().isEmpty()) {
                TextView errorText = findViewById(R.id.errorText);
                errorText.setText("Veuillez remplir tous les champs !");
                errorText.setVisibility(View.VISIBLE);
                } else {
                    Intent intent = new Intent(AuthentificationActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        });

        // quand le texte est cliqué on passe à l'activité compte
        compte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthentificationActivity.this, CompteActivity.class);
                startActivity(intent);
            }
        });
    }
}