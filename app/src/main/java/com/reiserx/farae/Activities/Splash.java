package com.reiserx.farae.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.reiserx.farae.databinding.ActivitySplashBinding;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;
import nl.dionsegijn.konfetti.xml.listeners.OnParticleSystemUpdateListener;

public class Splash extends AppCompatActivity {

    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textView.setVisibility(View.GONE);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            binding.textView.setVisibility(View.VISIBLE);
            explode();
            binding.textView.setText("Welcome ms");
        }, 800);
    }
    public void explode() {
        EmitterConfig emitterConfig = new Emitter(100L, TimeUnit.MILLISECONDS).max(100);
        binding.konfettiView.start(
                new PartyFactory(emitterConfig)
                        .spread(360)
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(0f, 30f)
                        .position(new Position.Relative(0.5, 0.3))
                        .build()
        );
        binding.konfettiView.setOnParticleSystemUpdateListener(new OnParticleSystemUpdateListener() {
            @Override
            public void onParticleSystemStarted(@NonNull KonfettiView konfettiView, @NonNull Party party, int i) {
            }

            @Override
            public void onParticleSystemEnded(@NonNull KonfettiView konfettiView, @NonNull Party party, int i) {
                binding.textView.setText("Happy Birthday pgl!");
                binding.textView.setVisibility(View.VISIBLE);
                parade();
            }
        });
    }

    public void parade() {
        EmitterConfig emitterConfig = new Emitter(5, TimeUnit.SECONDS).perSecond(40);
        binding.konfettiView.start(
                new PartyFactory(emitterConfig)
                        .angle(Angle.RIGHT - 45)
                        .spread(Spread.SMALL)
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f)
                        .position(new Position.Relative(0.0, 0.5))
                        .build(),
                new PartyFactory(emitterConfig)
                        .angle(Angle.LEFT + 45)
                        .spread(Spread.SMALL)
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f)
                        .position(new Position.Relative(1.0, 0.5))
                        .build()
        );
        binding.konfettiView.setOnParticleSystemUpdateListener(new OnParticleSystemUpdateListener() {
            @Override
            public void onParticleSystemStarted(@NonNull KonfettiView konfettiView, @NonNull Party party, int i) {
                binding.textView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onParticleSystemEnded(@NonNull KonfettiView konfettiView, @NonNull Party party, int i) {
                binding.konfettiView.setVisibility(View.GONE);
                binding.textView.setVisibility(View.GONE);
                finish();
            }
        });
    }
}