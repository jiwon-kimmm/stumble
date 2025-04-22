package com.example.stumble;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<String[]> locationPermissionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        registerPermissionCallback();
        requestPermissions();
    }

    private void registerPermissionCallback() {
        locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.containsKey(android.Manifest.permission.ACCESS_FINE_LOCATION)
                            ? result.get(android.Manifest.permission.ACCESS_FINE_LOCATION)
                            : false;
                    Boolean coarseLocationGranted = result.containsKey(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            ? result.get(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            : false;
                    if (fineLocationGranted != null && fineLocationGranted) {
                        Log.d("Location", "Precise location access granted.");
                        startLocationService();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        Log.d("Location", "Approximate location access granted.");
                        startLocationService();
                    } else {
                        Log.d("Location", "No location access granted.");
                    }
                });
    }

    private void requestPermissions() {

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(new String[] {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void startLocationService() {
        Intent startIntent = new Intent(MainActivity.this, LocationService.class);
        ContextCompat.startForegroundService(MainActivity.this, startIntent);
    }
}