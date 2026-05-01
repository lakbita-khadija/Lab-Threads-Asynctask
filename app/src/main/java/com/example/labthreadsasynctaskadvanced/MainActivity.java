package com.example.labthreadsasynctaskadvanced;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView txtStatus, txtProgress;
    private ProgressBar progressBar;
    private ImageView img;

    private Button btnLoadThread, btnCalcAsync, btnCancel, btnToast;

    private Handler mainHandler;
    private HeavyCalcTask heavyCalcTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);
        txtProgress = findViewById(R.id.txtProgress);
        progressBar = findViewById(R.id.progressBar);
        img = findViewById(R.id.img);

        btnLoadThread = findViewById(R.id.btnLoadThread);
        btnCalcAsync = findViewById(R.id.btnCalcAsync);
        btnCancel = findViewById(R.id.btnCancel);
        btnToast = findViewById(R.id.btnToast);

        mainHandler = new Handler(Looper.getMainLooper());

        img.setImageResource(R.drawable.ic_android_lab);
        btnCancel.setEnabled(false);

        btnToast.setOnClickListener(v ->
                Toast.makeText(this, "UI toujours réactive ✅", Toast.LENGTH_SHORT).show()
        );

        btnLoadThread.setOnClickListener(v -> loadImageWithThread());

        btnCalcAsync.setOnClickListener(v -> {
            heavyCalcTask = new HeavyCalcTask();
            heavyCalcTask.execute();
        });

        btnCancel.setOnClickListener(v -> {
            if (heavyCalcTask != null) {
                heavyCalcTask.cancel(true);
            }
        });
    }

    private void setLoadingState(boolean loading) {
        btnLoadThread.setEnabled(!loading);
        btnCalcAsync.setEnabled(!loading);
        btnCancel.setEnabled(loading);
    }

    private void resetProgress() {
        progressBar.setProgress(0);
        txtProgress.setText("Progression : 0%");
    }

    private void loadImageWithThread() {
        setLoadingState(true);
        progressBar.setVisibility(View.VISIBLE);
        resetProgress();

        txtStatus.setText("Statut : chargement image avec Thread...");

        new Thread(() -> {
            for (int i = 1; i <= 100; i++) {
                int progress = i;

                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mainHandler.post(() -> {
                    progressBar.setProgress(progress);
                    txtProgress.setText("Progression : " + progress + "%");
                });
            }

            mainHandler.post(() -> {
                img.setImageResource(R.drawable.ic_android_lab);
                txtStatus.setText("Statut : image chargée avec succès ✅");
                progressBar.setVisibility(View.INVISIBLE);
                setLoadingState(false);
            });

        }).start();
    }

    private class HeavyCalcTask extends AsyncTask<Void, Integer, Long> {

        @Override
        protected void onPreExecute() {
            setLoadingState(true);
            progressBar.setVisibility(View.VISIBLE);
            resetProgress();
            txtStatus.setText("Statut : calcul lourd en cours...");
        }

        @Override
        protected Long doInBackground(Void... voids) {
            long result = 0;

            for (int i = 1; i <= 100; i++) {
                if (isCancelled()) {
                    return result;
                }

                for (int k = 0; k < 300000; k++) {
                    result += (i * k) % 9;
                }

                publishProgress(i);

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            progressBar.setProgress(progress);
            txtProgress.setText("Progression : " + progress + "%");
        }

        @Override
        protected void onPostExecute(Long result) {
            progressBar.setVisibility(View.INVISIBLE);
            txtStatus.setText("Statut : calcul terminé ✅ Résultat = " + result);
            setLoadingState(false);
        }

        @Override
        protected void onCancelled(Long result) {
            progressBar.setVisibility(View.INVISIBLE);
            txtStatus.setText("Statut : calcul annulé ❌");
            txtProgress.setText("Progression : annulée");
            setLoadingState(false);
        }
    }
}