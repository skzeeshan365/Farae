package com.reiserx.farae.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.reiserx.farae.databinding.ActivityPhoneAuthBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class PhoneAuthActivity extends AppCompatActivity {

    ActivityPhoneAuthBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() !=null) {
            Intent intent = new Intent(PhoneAuthActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        if (Objects.requireNonNull(getSupportActionBar()).isShowing()) {
            getSupportActionBar().hide();
        }

        binding.phoneNum.requestFocus();

        binding.continueBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PhoneAuthActivity.this, OTPActivity.class);
            intent.putExtra("PhoneNumber", binding.contrycode.getText().toString().concat(binding.phoneNum.getText().toString()));
            startActivity(intent);
        });
    }
}