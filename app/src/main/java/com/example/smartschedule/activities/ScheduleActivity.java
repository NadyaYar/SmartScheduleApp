package com.example.smartschedule.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartschedule.R;
import com.example.smartschedule.adapter.ScheduleAdapter;
import com.example.smartschedule.api.ApiService;
import com.example.smartschedule.dto.ScheduleDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScheduleActivity extends AppCompatActivity {

    private TextView dateTextView;
    private RecyclerView scheduleRecyclerView;
    private ScheduleAdapter scheduleAdapter;
    private Retrofit retrofit;
    private ApiService apiService;
    private static final String TAG = "ScheduleActivity";
    private Long groupId;
    private String selectedPeriod = "day";
    private Button buttonDay, buttonWeek, buttonMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        dateTextView = findViewById(R.id.dateTextView);
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);
        ImageView backButton = findViewById(R.id.backButton);
        buttonDay = findViewById(R.id.buttonDay);
        buttonWeek = findViewById(R.id.buttonWeek);
        buttonMonth = findViewById(R.id.buttonMonth);

        groupId = getIntent().getLongExtra("groupId", -1);

        Date today = new Date();
        dateTextView.setText(getFormattedDate(today));

        backButton.setOnClickListener(v -> onBackPressed());

        buttonDay.setOnClickListener(v -> {
            selectedPeriod = "day";
            highlightSelectedButton(buttonDay);
            loadScheduleForDay(groupId, getFormattedDateForServer(today));
        });

        buttonWeek.setOnClickListener(v -> {
            selectedPeriod = "week";
            highlightSelectedButton(buttonWeek);
            loadScheduleForWeek(groupId, today);
        });

        buttonMonth.setOnClickListener(v -> {
            selectedPeriod = "month";
            highlightSelectedButton(buttonMonth);
            loadScheduleForMonth(groupId, today);
        });

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.0.244:8085/student/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        apiService = retrofit.create(ApiService.class);

        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleAdapter = new ScheduleAdapter(new ArrayList<>());
        scheduleRecyclerView.setAdapter(scheduleAdapter);

        highlightSelectedButton(buttonDay);
        loadScheduleForDay(groupId, getFormattedDateForServer(today));
    }

    private void highlightSelectedButton(Button selectedButton) {
        buttonDay.setTypeface(null, Typeface.NORMAL);
        buttonDay.setBackgroundColor(getResources().getColor(R.color.white));
        buttonWeek.setTypeface(null, Typeface.NORMAL);
        buttonWeek.setBackgroundColor(getResources().getColor(R.color.white));
        buttonMonth.setTypeface(null, Typeface.NORMAL);
        buttonMonth.setBackgroundColor(getResources().getColor(R.color.white));

        selectedButton.setTypeface(null, Typeface.BOLD);
        selectedButton.setBackgroundColor(getResources().getColor(R.color.blue));
    }

    private String getFormattedDate(Date date) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("uk"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return capitalizeFirstLetter(dayFormat.format(date)) + ", " + dateFormat.format(date);
    }

    private String getFormattedDateForServer(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(date);
    }

    private void loadScheduleForDay(Long groupId, String date) {
        Call<List<ScheduleDTO>> call = apiService.getScheduleForDay(groupId, date);
        call.enqueue(new Callback<List<ScheduleDTO>>() {
            @Override
            public void onResponse(Call<List<ScheduleDTO>> call, Response<List<ScheduleDTO>> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error: " + response.code());
                    Log.e(TAG, "Response: " + response.errorBody().toString());
                    Toast.makeText(ScheduleActivity.this, "Error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                List<ScheduleDTO> scheduleList = response.body();
                Log.d(TAG, "Schedule loaded: " + scheduleList);
                scheduleAdapter = new ScheduleAdapter(scheduleList);
                scheduleRecyclerView.setAdapter(scheduleAdapter);
            }

            @Override
            public void onFailure(Call<List<ScheduleDTO>> call, Throwable t) {
                Log.e(TAG, "Failed to load schedule: " + t.getMessage());
                Toast.makeText(ScheduleActivity.this, "Failed to load schedule",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadScheduleForWeek(Long groupId, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek();
        calendar.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        Date endDate = calendar.getTime();

        Call<List<ScheduleDTO>> call = apiService.getScheduleForWeek(groupId,
                getFormattedDateForServer(startDate));
        call.enqueue(new Callback<List<ScheduleDTO>>() {
            @Override
            public void onResponse(Call<List<ScheduleDTO>> call, Response<List<ScheduleDTO>> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error: " + response.code());
                    Log.e(TAG, "Response: " + response.errorBody().toString());
                    Toast.makeText(ScheduleActivity.this, "Error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                List<ScheduleDTO> scheduleList = response.body();
                Log.d(TAG, "Schedule loaded: " + scheduleList);
                scheduleAdapter = new ScheduleAdapter(scheduleList);
                scheduleRecyclerView.setAdapter(scheduleAdapter);
            }

            @Override
            public void onFailure(Call<List<ScheduleDTO>> call, Throwable t) {
                Log.e(TAG, "Failed to load schedule: " + t.getMessage());
                Toast.makeText(ScheduleActivity.this, "Failed to load schedule",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadScheduleForMonth(Long groupId, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date endDate = calendar.getTime();

        Call<List<ScheduleDTO>> call = apiService.getScheduleForMonth(groupId,
                getFormattedDateForServer(startDate));
        call.enqueue(new Callback<List<ScheduleDTO>>() {
            @Override
            public void onResponse(Call<List<ScheduleDTO>> call, Response<List<ScheduleDTO>> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error: " + response.code());
                    Log.e(TAG, "Response: " + response.errorBody().toString());
                    Toast.makeText(ScheduleActivity.this, "Error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                List<ScheduleDTO> scheduleList = response.body();
                Log.d(TAG, "Schedule loaded: " + scheduleList);
                scheduleAdapter = new ScheduleAdapter(scheduleList);
                scheduleRecyclerView.setAdapter(scheduleAdapter);
            }

            @Override
            public void onFailure(Call<List<ScheduleDTO>> call, Throwable t) {
                Log.e(TAG, "Failed to load schedule: " + t.getMessage());
                Toast.makeText(ScheduleActivity.this, "Failed to load schedule",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
