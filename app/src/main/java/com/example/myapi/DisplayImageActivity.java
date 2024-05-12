package com.example.myapi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class DisplayImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get the image URI and response from the intent
        Uri imageUri = getIntent().getParcelableExtra("fileUri"); // Correct key is "fileUri"
        String response = getIntent().getStringExtra("response");

        // Load and display the image
        displayImage(imageUri);

        // Display the response
        displayResponse(response);
    }

    private void displayImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(imageBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void displayResponse(String response) {
        TextView responseTextView = findViewById(R.id.responseTextView);
        responseTextView.setText(response);
    }
}