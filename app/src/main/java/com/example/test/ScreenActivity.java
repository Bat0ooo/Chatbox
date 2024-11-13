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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.screen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //delai de 3 secondes avant de passer à l'activité suivante
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(ScreenActivity.this, AuthentificationActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }




}