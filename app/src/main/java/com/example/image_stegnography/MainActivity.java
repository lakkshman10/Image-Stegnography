package com.example.image_stegnography;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEncode = findViewById(R.id.btnEncode);
        btnEncode.setOnClickListener(v -> {
            // Start the EncodingActivity when the Encode button is clicked
            Intent intent = new Intent(MainActivity.this, EncodePage.class);
            startActivity(intent);
        });
    }
}
