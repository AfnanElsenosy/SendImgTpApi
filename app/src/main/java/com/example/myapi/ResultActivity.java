//package com.example.myapi;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.widget.ImageView;
//import android.widget.TextView;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class ResultActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_result);
//
//        // Retrieve data from intent
//        String imagePath = getIntent().getStringExtra("imagePath");
//        ApiResponse apiResponse = (ApiResponse) getIntent().getSerializableExtra("apiResponse");
//
//        // Display image
//        ImageView imageView = findViewById(R.id.imageView);
//        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
//        imageView.setImageBitmap(bitmap);
//
//        // Display API response values
//        TextView genderTextView = findViewById(R.id.genderTextView);
//        TextView ageTextView = findViewById(R.id.ageTextView);
//        TextView expressionsTextView = findViewById(R.id.expressionsTextView);
//        TextView ageGroupTextView = findViewById(R.id.ageGroupTextView);
//
//        genderTextView.setText("Gender: " + apiResponse.getGender().getValue());
//        ageTextView.setText("Age: " + apiResponse.getAge());
//        ageGroupTextView.setText("Age Group: " + apiResponse.getAgeGroup());
//
//        StringBuilder expressionsBuilder = new StringBuilder("Expressions: ");
//        for (ApiResponse.Expression expression : apiResponse.getExpressions()) {
//            expressionsBuilder.append(expression.getValue()).append(", ");
//        }
//        String expressions = expressionsBuilder.toString();
//        expressionsTextView.setText(expressions.substring(0, expressions.length() - 2)); // Remove the last comma
//    }
//}