package com.example.smartschedule.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartschedule.R;

public class MainActivity extends AppCompatActivity {

    private ImageView appLogo;
    private TextView appName;
    private ImageButton enterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        appLogo = findViewById(R.id.appLogo);
        appName = findViewById(R.id.appName);
        enterButton = findViewById(R.id.enterButton);

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StudentFacultyActivity.class);
                intent.putExtra("isStudent", true);
                startActivity(intent);
            }
        });
    }
}
