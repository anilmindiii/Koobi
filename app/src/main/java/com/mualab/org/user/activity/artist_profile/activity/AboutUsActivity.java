package com.mualab.org.user.activity.artist_profile.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mualab.org.user.R;

public class AboutUsActivity extends AppCompatActivity {
    private String bio;
    private TextView tv_about_us;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        bio = getIntent().getStringExtra("bio");

        tv_about_us = findViewById(R.id.tv_about_us);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(R.string.about_us_title);
        tv_about_us.setText(bio+"");

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
