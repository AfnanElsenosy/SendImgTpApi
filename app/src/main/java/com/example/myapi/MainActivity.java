package com.example.myapi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);

            } else {
                dispatchTakePictureIntent();

            }
            
        });



    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        Log.i("TAG","2");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//            Log.i("TAG","3");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.i("TAG","4");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            assert extras != null;
            imageBitmap = (Bitmap) extras.get("data");
//            Log.i("TAG","5");
            if (imageBitmap != null) {
                // Execute the image compression task
                new ImageCompressionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageBitmap);
//                Log.i("TAG","6");

            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
//                Log.i("TAG","8");

            } else {
//                Log.i("TAG","9");

                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public class ImageCompressionTask extends AsyncTask<Bitmap, Void, byte[]> {

        @Override
        protected byte[] doInBackground(Bitmap... bitmaps) {
            // Compress the bitmap to a byte array in the background
            Bitmap bitmap = bitmaps[0];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        }

        @Override
        protected void onPostExecute(byte[] imageData) {
            // Create Retrofit instance
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.luxand.cloud/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
//            Log.i("TAG","22");
            // Create API service
            LuxandApiService apiService = retrofit.create(LuxandApiService.class);

            // Create RequestBody from byte array
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageData);

            // Make API call
            Call<ApiResponse> call = apiService.detectFace(requestBody);
//            Log.i("TAG","33");
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
//                    Log.i("TAG","88");
                    if (response.isSuccessful() && response.body() != null) {
//                        Log.i("TAG","44");
                        ApiResponse apiResponse = response.body();
                        // Handle successful response
                        Log.i("TAG","apiResponse"+apiResponse);
                        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                        intent.putExtra("imagePath", imageBitmap.isMutable()); // Pass the image path
                        intent.putExtra("apiResponse", (CharSequence) apiResponse); // Pass the ApiResponse object
                        startActivity(intent);
                    } else {
//                        Log.i("TAG","55");
                        // Handle unsuccessful response
                        Log.i("TAG","unsuccessful response");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                    // Handle network error
                    Log.i("TAG",":"+t.getMessage());

                }
            });
        }
    }

}
