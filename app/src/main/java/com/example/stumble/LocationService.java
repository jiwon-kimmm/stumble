package com.example.stumble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.Manifest;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service {

    private FusedLocationProviderClient flpClient;
    private LocationCallback locationCallback;
    private Handler serviceHandler;
    private static final String CHANNEL_ID = "location_service_channel";
    private NotificationChannel channel;
    private NotificationManager notificationManager;
    private String notifContentTitle;
    private String notifContextText;
    private LocationRequest locationRequest;


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Location Service";
            String description = "Collecting location data";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String notifContentTitle, String notifContextText) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(notifContentTitle)
                .setContentText(notifContextText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        return notification;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        flpClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult.getLastLocation() == null) {
                    Log.d("Location", "NULL");
                    return;
                }
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);
            }
        };

        HandlerThread thread = new HandlerThread("Worker thread");
        thread.start();

        Looper serviceLooper = thread.getLooper();
        serviceHandler = new Handler(serviceLooper);

        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                2000
        ).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Location", "onStartCommand called");
        Notification notif;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            notifContentTitle = "Starting location tracking...";
            notifContextText = "";

            notif = createNotification(notifContentTitle, notifContextText);
            startForeground(1, notif);

            flpClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } else {
            Log.e("Location", "Location permission not granted.");
        }
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (flpClient != null && locationCallback != null) {
            flpClient.removeLocationUpdates(locationCallback);
        }
    }

}
