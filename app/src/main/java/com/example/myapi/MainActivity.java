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

import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;

    private Button captureButton;
    private TextView resultTextView;
    Bitmap imageBitmap;

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
                    handleResponse(response.body());
                    String jsonResponse = response.body().string();
                    // Pass fileUri and API response to DisplayImageActivity
                    openDisplayImageActivity(Uri.fromFile(file), jsonResponse); // Pass fileUri and API response to DisplayImageActivity
                }
            } catch (IOException e) {
                Log.e("TAG", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void handleResponse(ResponseBody responseBody) {
        Gson gson = new Gson();
        try {
            Type type = new TypeToken<List<ApiResponse>>(){}.getType();
            List<ApiResponse> apiResponses = gson.fromJson(responseBody.string(), type);


            if(apiResponses.size() == 0){
                Log.d("TAG", "No Faces Detected");
                return;
            }

            // Now you can use the getter methods to access the data
            for (ApiResponse apiResponse : apiResponses) {
                ApiResponse.Gender gender = apiResponse.getGender();
                if (gender != null) {

                    Log.i("TAG", "Gender: " + gender.getValue() + ", Probability: " + gender.getProbability());
                }

                double age = apiResponse.getAge();
                List<ApiResponse.Expression> expressions = apiResponse.getExpressions();
                String ageGroup = apiResponse.getAgeGroup();
                ApiResponse.Rectangle rectangle = apiResponse.getRectangle();

                // Log or use the data as needed
                Log.i("TAG", "Age: " + age);
                for (ApiResponse.Expression expression : expressions) {
                    Log.i("TAG", "Expression: " + expression.getValue() + ", Probability: " + expression.getProbability());
                }
                Log.i("TAG", "Age Group: " + ageGroup);
                if (rectangle != null) {
                    Log.i("TAG", "Rectangle: " + rectangle.getLeft() + ", " + rectangle.getTop() + ", " + rectangle.getRight() + ", " + rectangle.getBottom());
                }
            }

        } catch (JsonSyntaxException e) {
            Log.e("TAG", "Parsing Error: " + e.getMessage());
        } catch (IOException e) {
            Log.e("TAG", "Error: " + e.getMessage());
        }
    }
    private void openDisplayImageActivity(Uri fileUri, String response) {
        Intent intent = new Intent(MainActivity.this, DisplayImageActivity.class);
        intent.putExtra("fileUri", fileUri); // Correct key is "fileUri"
        intent.putExtra("response", response);
        startActivity(intent);
    }


}