package com.example.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class CompteActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText login, password, password2;
    private Button create;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compte);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        login = findViewById(R.id.Create_login);
        password = findViewById(R.id.Create_Password);
        password2 = findViewById(R.id.Create_Password2);
        create = findViewById(R.id.Create_button);


        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vérifier si les champs sont remplis
                if (login.getText().toString().isEmpty() || !(password.getText().toString().equals(password2.getText().toString())) || password.getText().toString().isEmpty() || password2.getText().toString().isEmpty()) {
                    TextView errorText = findViewById(R.id.errorText);
                    errorText.setText("Champs incorrect !");
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    Intent intent = new Intent(CompteActivity.this, AuthentificationActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}