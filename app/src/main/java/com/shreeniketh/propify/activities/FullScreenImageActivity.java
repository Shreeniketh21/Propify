package com.shreeniketh.propify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.adapters.FullScreenImageAdapter;

import java.util.ArrayList;

public class FullScreenImageActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private ArrayList<String> imageUrls;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        viewPager = findViewById(R.id.fullScreenViewPager);
        ImageView closeBtn = findViewById(R.id.closeBtn);

        // Get data from intent
        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        position = getIntent().getIntExtra("position", 0);

        // Set ViewPager Adapter
        FullScreenImageAdapter adapter = new FullScreenImageAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);

        // Close button
        closeBtn.setOnClickListener(v -> finish());
    }
}
