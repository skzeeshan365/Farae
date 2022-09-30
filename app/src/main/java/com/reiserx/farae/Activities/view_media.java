package com.reiserx.farae.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.reiserx.farae.R;
import com.reiserx.farae.databinding.ActivityViewMediaBinding;

public class view_media extends AppCompatActivity {

    ActivityViewMediaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewMediaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String url = getIntent().getStringExtra("url");

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.image_placeholder)
                .into(binding.imageView3);
    }
}