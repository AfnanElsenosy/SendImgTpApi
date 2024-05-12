package com.example.myapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.File;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity{
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;

    private Button captureButton;
    private TextView resultTextView;
    Bitmap imageBitmap ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureButton = findViewById(R.id.captureButton);
        resultTextView = findViewById(R.id.resultTextView);


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
        if (fileUri != null) {
            // Convert Uri to File
            File file = new File(Objects.requireNonNull(fileUri.getPath()));
            if (file.exists()) {
                // The file exists, proceed with upload
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("image/jpeg"), file);
                // Create MultipartBody.Part
                MultipartBody.Part body = MultipartBody.Part.createFormData("picture", file.getName(), requestFile);
                // Create description RequestBody (if needed)
                RequestBody description =
                        RequestBody.create(MediaType.parse("text/plain"), "Image description");

                // create upload service client
                LuxandApiService service =
                        ServiceGenerator.createService(LuxandApiService.class);
                // finally, execute the request
                Call<ResponseBody> call = service.detectFace(description, body);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            // Update UI elements here
                            runOnUiThread(() -> {
                                ResponseBody apiResponse = response.body();
                                Log.i("TAG", "apiResponse: " + apiResponse);
                                // Update UI elements based on the response
                                Log.v("Upload", "success");
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        runOnUiThread(() -> {
                            // Handle failure, update UI elements or show error message
                            Log.e("Upload error:", Objects.requireNonNull(t.getMessage()));
                        });
//

                    }
                });
            } else {
                // File does not exist
                Log.e("TAG", "File does not exist: " + file.getPath());
            }

        } else {
            // Uri is null
            Log.e("TAG", "Uri is null");
        }
    }
    private void goToResultActivity() {
        ApiResponse apiResponse = new ApiResponse();
        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);
    }



}