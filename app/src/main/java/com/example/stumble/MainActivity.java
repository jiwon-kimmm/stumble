package com.example.stumble;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

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

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBP5b8GBIPKlRM7zVkvOkM42JnLE0izAeA");
        }

        PlacesClient placesClient = Places.createClient(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        registerPermissionCallback();
        requestPermissions();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ((Button) findViewById(R.id.SignUpLoginButton)).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class))
        );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkPermissions()) {
            getLocationAndAddMarker();
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void getLocationAndAddMarker() {
        if (!checkPermissions()) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Your Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));

                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        String apiKey = "AIzaSyBP5b8GBIPKlRM7zVkvOkM42JnLE0izAeA";
                        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + "?location=" + lat + "," + lng +
                                     "&radius=500" + "&type=restaurant" + "&key=" + apiKey;

                        RequestQueue queue = Volley.newRequestQueue(this);

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                                response -> {
                                    try {
                                        JSONArray results = response.getJSONArray("results");
                                        TextView restaurant1Text = findViewById(R.id.restaurant1Name);
                                        TextView restaurant2Text = findViewById(R.id.restaurant2Name);
                                        TextView restaurant3Text = findViewById(R.id.restaurant3Name);
                                        List<TextView> restaurantTextViews = Arrays.asList(restaurant1Text, restaurant2Text, restaurant3Text);

                                        int closedCount = 0;
                                        for (int i = 0; i < results.length() && closedCount < 3; i++) {
                                            JSONObject place = results.getJSONObject(i);
                                            String businessStatus = place.optString("business_status", "OPERATIONAL");
                                            if (!businessStatus.equals("CLOSED_TEMPORARILY") && !businessStatus.equals("CLOSED_PERMANENTLY")) {

                                                String name = place.getString("name");
                                                String address = place.getString("vicinity");

                                                JSONObject loc = place.getJSONObject("geometry").getJSONObject("location");
                                                LatLng latLng = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

                                                int priceLevel = place.optInt("price_level", -1);
                                                String price;
                                                if (priceLevel >= 1 && priceLevel <= 4) {
                                                    char[] dollars = new char[priceLevel];
                                                    Arrays.fill(dollars, '$');
                                                    price = new String(dollars);
                                                } else {
                                                    price = "N/A";
                                                }

                                                TextView tv = restaurantTextViews.get(closedCount);
                                                tv.setText(name + "  •  " + price);

                                                mMap.addMarker(new MarkerOptions().position(latLng).title(name)
                                                                                    .snippet("Address: " + address + "\n" +
                                                                                             "Price:   " + price   + "\n" +
                                                                                             "Status:  " + businessStatus)
                                                );

                                                closedCount++;
                                            }
                                        }
                                        for (int j = closedCount; j < restaurantTextViews.size(); j++) {
                                            restaurantTextViews.get(j).setText("No restaurants");
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                },
                                error -> Log.e("VOLLEY", "Error fetching places", error));

                        queue.add(request);

                    } else {
                        Log.e("Location", "Location not available");
                    }
                });
    }

    private void registerPermissionCallback() {
        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    if (fineGranted != null && fineGranted) {
                        Log.d("Location", "Precise access granted");
                        handleLocationAccessGranted();
                    } else if (coarseGranted != null && coarseGranted) {
                        Log.d("Location", "Approximate access granted");
                        handleLocationAccessGranted();
                    } else {
                        Log.d("Location", "No access granted");
                    }
                });
    }

    private void handleLocationAccessGranted() {
        startLocationService();
        if (mMap != null) getLocationAndAddMarker();
    }

    private void requestPermissions() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void startLocationService() {
        Intent startIntent = new Intent(this, LocationService.class);
        ContextCompat.startForegroundService(this, startIntent);
    }
}