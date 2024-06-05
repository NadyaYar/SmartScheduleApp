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

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SelectGroupActivity extends AppCompatActivity {

    private Spinner groupSpinner;
    private ImageButton enterButton;
    private Retrofit retrofit;
    private ApiService apiService;
    private static final String TAG = "SelectGroupActivity";
    private Long facultyId;
    private List<String> groupNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group);

        groupSpinner = findViewById(R.id.groupSpinner);
        enterButton = findViewById(R.id.enterButton);

        facultyId = getIntent().getLongExtra("facultyId", -1);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.0.244:8085/student/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        apiService = retrofit.create(ApiService.class);

        loadGroups(facultyId);

        enterButton.setOnClickListener(v -> {
            if (groupSpinner.getSelectedItem() != null) {
                String selectedGroup = groupSpinner.getSelectedItem().toString();
                int selectedIndex = groupNames.indexOf(selectedGroup);
                Long selectedGroupId = (long) selectedIndex + 1;
                if (selectedGroupId != null) {
                    Intent intent = new Intent(SelectGroupActivity.this, ScheduleActivity.class);
                    intent.putExtra("groupId", selectedGroupId);
                    startActivity(intent);
                } else {
                    Toast.makeText(SelectGroupActivity.this,
                            "Не вдалося знайти ID для обраної групи", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SelectGroupActivity.this,
                        "Будь ласка, оберіть групу", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroups(Long facultyId) {
        Call<List<String>> call = apiService.getGroups(facultyId);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error: " + response.code());
                    Toast.makeText(SelectGroupActivity.this,
                            "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                groupNames = response.body();
                Log.d(TAG, "Groups loaded: " + groupNames);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SelectGroupActivity.this,
                        android.R.layout.simple_spinner_item, groupNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                groupSpinner.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e(TAG, "Failed to load groups: " + t.getMessage());
                Toast.makeText(SelectGroupActivity.this, "Failed to load groups",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
