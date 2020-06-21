package com.michael.aadproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SensorEventListener {
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean lightSensorAvailability;
    private float initialLightReading;
    private boolean firstReading = true;

    private TextView textUserName;
    private TextView textUserEmail;
    private TextView textUserInitial;

    private NavigationView navigationView;
    private View relativeProfile;
    private View linearProfile;
    private DrawerLayout drawerLayout;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        if (lightSensor == null) {
            lightSensorAvailability = false;
        } else {
            lightSensorAvailability = true;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.navigationView);
        relativeProfile = navigationView.getHeaderView(0).findViewById(R.id.relativeProfile);
        textUserInitial = relativeProfile.findViewById(R.id.textProfilePicture);
        linearProfile = navigationView.getHeaderView(0).findViewById(R.id.linearProfile);
        textUserName = linearProfile.findViewById(R.id.textViewProfileName);
        textUserEmail = linearProfile.findViewById(R.id.textViewProfileEmail);

        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar, R.string.sidebar_open, R.string.sidebar_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User retrievedUser = dataSnapshot.getValue(User.class);
                textUserInitial.setText(retrievedUser.getName().substring(0, 1));
                textUserName.setText(retrievedUser.getName() + " " + retrievedUser.getSurname());
                textUserEmail.setText(retrievedUser.getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getBaseContext(), "Error fetching data from database.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                    new NearbyFragment()).commit();
            navigationView.setCheckedItem(R.id.nearbyMenu);
        }

        navigationView.getMenu().findItem(R.id.logoutMenu).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                return true;
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nearbyMenu:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                        new NearbyFragment()).commit();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lightSensorAvailability) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensorAvailability) {
            sensorManager.registerListener(this, lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (lightSensorAvailability && event.sensor.getType() == Sensor.TYPE_LIGHT) {
            if (firstReading) {
                initialLightReading = event.values[0];
                firstReading = false;
            }
            WindowManager.LayoutParams layoutParams= getWindow().getAttributes();
            float light = event.values[0];
            System.out.println("LIGHT VAL: " + light);
            if (light > 600) {
                layoutParams.screenBrightness = 1.0f;
            } else if (light < 300) {
                layoutParams.screenBrightness = 0.3f;
            }
            getWindow().setAttributes(layoutParams);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (lightSensorAvailability && sensor == lightSensor) {
            WindowManager.LayoutParams layoutParams= getWindow().getAttributes();
            if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                    accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                Toast.makeText(this, "Light sensor is unreliable. Auto " +
                        "brightness is disabled.", Toast.LENGTH_SHORT).show();
                sensorManager.unregisterListener(this);
                layoutParams.screenBrightness = initialLightReading;
            } else if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM ||
                    accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
                Toast.makeText(this, "Light sensor is reliable. Auto " +
                        "brightness is enabled.", Toast.LENGTH_SHORT).show();
                sensorManager.registerListener(this, lightSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }
}
