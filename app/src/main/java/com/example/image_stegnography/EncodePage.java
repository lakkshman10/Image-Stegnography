package com.example.image_stegnography;

import java.io.*;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EncodePage extends AppCompatActivity {

    private static final int SELECT_IMAGE_REQUEST = 1;

    private ImageView ivResult;
    private TextView tvInstructions;
    private EditText etInputText;
    private Button btnConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encode_page);

        ivResult = findViewById(R.id.ivResult);
        tvInstructions = findViewById(R.id.tvInstructions);
        etInputText = findViewById(R.id.etInputText);
        btnConfirm = findViewById(R.id.btnConfirm);

        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnSaveMessage = findViewById(R.id.btnSaveMessage);

        btnSelectImage.setOnClickListener(v -> {
            // Start activity to select image from gallery
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_IMAGE_REQUEST);
        });

        btnSaveMessage.setOnClickListener(v -> {
            // Execute functionality for Save Message button click
            // For example, you can save the entered message or perform another action
            String message = etInputText.getText().toString().trim();
            // Here you can implement code to save the message or perform any other action
            // For now, let's display the message using a Toast
            if (!message.isEmpty()) {
                Toast.makeText(EncodePage.this, "Message saved: " + message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EncodePage.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirm.setOnClickListener(v -> {
            String inputText = etInputText.getText().toString().trim();
            if (!inputText.isEmpty()) {
                // Hide the input text in the selected image
                Bitmap imageWithHiddenMessage = hideMessageInImage(inputText, ((BitmapDrawable) ivResult.getDrawable()).getBitmap());
                // Update the ImageView with the image containing the hidden message
                ivResult.setImageBitmap(imageWithHiddenMessage);
                saveImageToFile(imageWithHiddenMessage);
            } else {
                Toast.makeText(EncodePage.this, "Please enter a message to hide", Toast.LENGTH_SHORT).show();
            }
        });


        // If imageUri is passed from Enter Message screen, display result
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            displayResult(Uri.parse(imageUriString));
        }

        // Set click listener for the result image to allow adding an image
        ivResult.setOnClickListener(v -> {
            // Open gallery to select image
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_IMAGE_REQUEST);
        });

        btnSaveMessage.setOnClickListener(v -> {
            String inputText = etInputText.getText().toString().trim();
            // Save the entered text or perform any other action here
            // You can replace this with your desired functionality
            if (!inputText.isEmpty()) {

                saveTextToFile(inputText);
            } else {
                // If no text is entered, show a toast or perform any other action
                Toast.makeText(EncodePage.this, "Please enter some text", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTextToFile(String inputText) {
        // File name where you want to save the text
        String fileName = "saved_text.txt";

        try {
            // Get the path to the Documents directory
            File documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

            // Create a File object representing the file in the Documents directory
            File file = new File(documentsDirectory, fileName);

            // Create a FileWriter object to write to the file
            FileWriter writer = new FileWriter(file);

            // Write the input text to the file
            writer.write(inputText);

            // Close the FileWriter
            writer.close();

            // If the text is successfully saved, show a toast
            Toast.makeText(this, "Text saved to file: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // If an error occurs while saving the text, show an error message
            e.printStackTrace();
            Toast.makeText(this, "Error saving text", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveImageToFile(Bitmap image) {
        // Create a directory to store the image
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Steganography");

        // Make sure the directory exists; if not, create it
        if (!directory.exists()) {
            boolean directoriesCreated = directory.mkdirs();
            if (!directoriesCreated) {
                // Handle the case where directories couldn't be created
                Log.e("Save Image", "Failed to create directories");
                return;
            }
        }

        // Create a file to save the image in the directory
        File file = new File(directory, "steganography_image.png");

        try {
            // Create a FileOutputStream to write the image data to the file
            FileOutputStream fos = new FileOutputStream(file);

            // Compress the image and write it to the FileOutputStream
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);

            // Close the FileOutputStream
            fos.close();

            // Display a toast indicating that the image has been saved
            Toast.makeText(this, "Image saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap hideMessageInImage(String message, Bitmap image) {
        // Convert the message to binary
        StringBuilder binaryMessage = new StringBuilder();
        for (char c : message.toCharArray()) {
            binaryMessage.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }

        // Convert the image to mutable bitmap
        Bitmap mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);

        // Modify the color values of certain pixels in the image to hide the binary data
        int pixelIndex = 0;
        for (int i = 0; i < mutableBitmap.getWidth(); i++) {
            for (int j = 0; j < mutableBitmap.getHeight(); j++) {
                if (pixelIndex < binaryMessage.length()) {
                    int pixelColor = mutableBitmap.getPixel(i, j);
                    // Modify the least significant bit of each color channel (RGB) to hide the binary data
                    // Example: Red channel
                    int red = Color.red(pixelColor);
                    int newRed = (red & 0xFE) | (binaryMessage.charAt(pixelIndex) - '0');
                    int newPixelColor = Color.rgb(newRed, Color.green(pixelColor), Color.blue(pixelColor));
                    mutableBitmap.setPixel(i, j, newPixelColor);
                    pixelIndex++;
                }
            }
        }

        return mutableBitmap;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            // Display the selected image
            ivResult.setImageURI(selectedImageUri);
            // Show instructions and input field
            tvInstructions.setVisibility(View.VISIBLE);
            etInputText.setVisibility(View.VISIBLE);
            btnConfirm.setVisibility(View.VISIBLE);
        }
    }

    private void displayResult(Uri imageUri) {
        // Display the image with hidden message
        ivResult.setImageURI(imageUri);
        // Show instructions and input field
        tvInstructions.setVisibility(View.VISIBLE);
        etInputText.setVisibility(View.VISIBLE);
        btnConfirm.setVisibility(View.VISIBLE);
    }
}
