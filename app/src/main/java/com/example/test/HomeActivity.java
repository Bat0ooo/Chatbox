package com.example.test;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


                discussionContainer.addView(messageView);

                    newMessage.setText("");
                }
        });
    }
}