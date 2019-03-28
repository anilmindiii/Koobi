package com.mualab.org.user.activity.artist_profile.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.model.Certificate;
import com.mualab.org.user.activity.feeds.activity.FeedSingleActivity;
import com.mualab.org.user.activity.feeds.activity.PreviewImageActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CertificateDescriptionActivity extends AppCompatActivity {
    private ImageView iv_certificate;
    private TextView tv_status, tv_title, tv_description;
    private Certificate certificate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_description);

        iv_certificate = findViewById(R.id.iv_certificate);
        tv_status = findViewById(R.id.tv_status);
        tv_title = findViewById(R.id.tv_title);
        tv_description = findViewById(R.id.tv_description);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.text_certificate));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        iv_certificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> tempList = new ArrayList<>();
                tempList.add(certificate.certificateImage);

                Intent intent = new Intent(CertificateDescriptionActivity.this, PreviewImageActivity.class);
                intent.putExtra("imageArray", tempList);
                intent.putExtra("startIndex", 0);
                startActivity(intent);
            }
        });

        if (getIntent().getParcelableExtra("certificate") != null) {
            certificate = getIntent().getParcelableExtra("certificate");

            if (certificate.status == 0) {
                tv_status.setText("Under Review");
            } else {
                tv_status.setText("Verified");
            }

            tv_title.setText(certificate.title);
            tv_description.setText(certificate.description);

            if (!certificate.certificateImage.equals("")){
                Picasso.with(this).load(certificate.certificateImage).placeholder(R.drawable.gallery_placeholder).fit().into(iv_certificate);
            }else {
                iv_certificate.setImageDrawable(getResources().getDrawable(R.drawable.gallery_placeholder));
            }

        }
    }
}
