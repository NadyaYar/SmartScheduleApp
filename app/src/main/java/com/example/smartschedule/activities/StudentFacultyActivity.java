package com.example.smartschedule.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartschedule.R;
import com.example.smartschedule.api.ApiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StudentFacultyActivity extends AppCompatActivity {

    private Spinner facultySpinner;
    private ImageButton nextButton;
    private Retrofit retrofit;
    private ApiService apiService;
    private static final String TAG = "StudentFacultyActivity";
    private Map<String, Long> facultyMap = new HashMap<>();
    private List<String> facultyNames;
    private boolean isStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_faculty);

        facultySpinner = findViewById(R.id.facultySpinner);
        nextButton = findViewById(R.id.nextButton);

        isStudent = getIntent().getBooleanExtra("isStudent", false);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.0.244:8085/student/faculty/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        apiService = retrofit.create(ApiService.class);

        loadFaculties();

        nextButton.setOnClickListener(v -> {
            if (facultySpinner.getSelectedItem() != null) {
                String selectedFaculty = facultySpinner.getSelectedItem().toString();
                Long selectedFacultyId = facultyMap.get(selectedFaculty);
                if (selectedFacultyId != null) {
                    Intent intent = new Intent(StudentFacultyActivity.this,
                            SelectGroupActivity.class);
                    intent.putExtra("facultyId", selectedFacultyId);
                    intent.putExtra("isStudent", isStudent);
                    startActivity(intent);
                } else {
                    Toast.makeText(StudentFacultyActivity.this,
                            "Не вдалося знайти ID для обраного факультету", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(StudentFacultyActivity.this, "Будь ласка, оберіть факультет",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFaculties() {
        Call<List<String>> call = apiService.getFaculties();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error: " + response.code());
                    Toast.makeText(StudentFacultyActivity.this,
                            "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                facultyNames = response.body();
                Log.d(TAG, "Faculties loaded: " + facultyNames);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(StudentFacultyActivity.this,
                        android.R.layout.simple_spinner_item, facultyNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                facultySpinner.setAdapter(adapter);

                for (int i = 0; i < facultyNames.size(); i++) {
                    facultyMap.put(facultyNames.get(i), (long) i + 1);
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e(TAG, "Failed to load faculties: " + t.getMessage());
                Toast.makeText(StudentFacultyActivity.this, "Failed to load faculties",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
