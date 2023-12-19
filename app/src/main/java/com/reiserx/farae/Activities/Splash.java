package com.reiserx.farae.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

    private String MESSAGE_1, MESSAGE_2, MESSAGE_3, MESSAGE_4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textView.setVisibility(View.GONE);

        Bundle extras = getIntent().getExtras();

        MESSAGE_1 = extras.getString("message_1");
        MESSAGE_2 = extras.getString("message_2");
        MESSAGE_3 = extras.getString("message_3");
        MESSAGE_4 = extras.getString("message_4");

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            binding.textView.setVisibility(View.VISIBLE);
            explode();
            binding.textView.setText(MESSAGE_1);
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
                parade();
            }
        });
    }

    public void parade() {
        binding.textView.setText(MESSAGE_2);
        binding.textView.setVisibility(View.VISIBLE);

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

            }

            @Override
            public void onParticleSystemEnded(@NonNull KonfettiView konfettiView, @NonNull Party party, int i) {
                binding.textView.setVisibility(View.INVISIBLE);
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    parade2();
                }, 100);
            }

        });
    }

    public void parade2() {
        binding.textView.setText(MESSAGE_3);
        binding.textView.setTextSize(18);
        binding.textView.setVisibility(View.VISIBLE);

        EmitterConfig emitterConfig = new Emitter(8, TimeUnit.SECONDS).perSecond(20);
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
            }

            @Override
            public void onParticleSystemEnded(@NonNull KonfettiView konfettiView, @NonNull Party party, int i) {
                binding.textView.setVisibility(View.INVISIBLE);
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    parade3();
                }, 200);
            }
        });
    }

    public void parade3() {
        binding.textView.setText(MESSAGE_4);
        binding.textView.setVisibility(View.VISIBLE);

        EmitterConfig emitterConfig = new Emitter(8, TimeUnit.SECONDS).perSecond(20);
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