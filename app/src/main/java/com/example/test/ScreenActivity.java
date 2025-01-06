package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ScreenActivity extends AppCompatActivity {

    // La méthode onCreate est appelée lorsque l'activité est créée pour la première fois
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen); // On définit la mise en page (layout) de l'activité

        // Appliquer un padding (espacement) pour tenir compte des barres système (barre d'état et barre de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.screen), (v, insets) -> {
            // On récupère les insets (marges) des barres système (barre d'état et de navigation)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // On applique le padding à la vue pour éviter que les éléments de l'interface soient cachés par les barres système
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets; // On retourne les insets pour terminer l'ajustement du padding
        });

        // Délai de 2 secondes (2000 millisecondes) avant de passer à l'activité suivante
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // On crée un intent pour démarrer l'activité AuthentificationActivity
            Intent intent = new Intent(ScreenActivity.this, AuthentificationActivity.class);
            startActivity(intent); // On démarre l'activité AuthentificationActivity
            finish(); // On ferme l'activité actuelle (ScreenActivity) pour la retirer de la pile des activités
        }, 2000); // Délai de 2 secondes
    }
}
