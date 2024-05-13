package com.example.myapi;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button captureButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureButton = findViewById(R.id.captureButton);

        captureButton.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            Uri uri = data.getData();
            uploadFile(uri);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(Uri fileUri) {
        try {
            File file = convertUriToFile(fileUri);
            uploadFileToServer(file);

        } catch (Exception e) {
            Log.e("TAG", "Error: " + e.getMessage());
        }
    }

    private File convertUriToFile(Uri fileUri) throws Exception {
        if (fileUri == null) {
            throw new Exception("Uri is null");
        }

        File file = new File(Objects.requireNonNull(fileUri.getPath()));

        if (!file.exists()) {
            throw new Exception("File does not exist: " + file.getPath());
        }

        return file;
    }

    private void uploadFileToServer(File file) {
        RequestBody requestBody = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), requestBody);
        LuxandApiService service = ServiceGenerator.createService(LuxandApiService.class);

        new Thread(() -> {
            try {
                Call<ResponseBody> call = service.detectFace("c4da8898b06a4c2b95d26c62caccd1fd", body);
                Response<ResponseBody> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    // Handle the response in the background thread
                    handleResponse(response.body(), file);
                } else {
                    // Handle the failure case
                    Log.e("TAG", "Server error: " + response.message());
                }
            } catch (IOException e) {
                Log.e("TAG", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void handleResponse(ResponseBody responseBody, File file) {
        Gson gson = new Gson();
        try {
            String responseString = responseBody.string();
            Log.i("TAG", "API response: " + responseString);

            // Pass fileUri and API response to DisplayImageActivity
            runOnUiThread(() -> openDisplayImageActivity(Uri.fromFile(file), responseString));
        } catch (IOException e) {
            Log.e("TAG", "Error reading response: " + e.getMessage());
        } finally {
            // Close the response body to release resources
            responseBody.close();
        }
    }
    private void openDisplayImageActivity(Uri fileUri, String response) {
        Intent intent = new Intent(MainActivity.this, DisplayImageActivity.class);
        intent.putExtra("fileUri", fileUri); // Correct key is "fileUri"
        intent.putExtra("response", response);
        startActivity(intent);
    }


}