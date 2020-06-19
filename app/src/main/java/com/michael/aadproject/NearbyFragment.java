package com.michael.aadproject;

import android.Manifest;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class NearbyFragment extends Fragment {
    private TextView textWelcome;
    private EditText editLocation;
    private TextView textAddress;
    private ImageView imageSearch;
    private ImageView imageCurrentLocation;
    private Location userLocation = null;

    private DatabaseReference mDatabase;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    private Date lastUpdate = null;

    private RecyclerView recyclerView;
    private RecyclerView recyclerViewBowl;
    private RecyclerView.Adapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerManager;
    private TextView textBlank;
    private TextView textBlankBowl;

    private final int PLAY_SERVICES_RESOLUTION_REQUEST = 1;
    private final int LOCATION_PERMISSION_REQUEST = 10;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int result = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (result != ConnectionResult.SUCCESS) {
            if (!apiAvailability.isUserResolvableError(result)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Error! Google Play Services not found");
                builder.setMessage("Google Play Services is unavailable on this device. " +
                        "Please install and enable Google Play Services, then try again.");
                builder.setCancelable(false);
                builder.setPositiveButton("Exit App", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().moveTaskToBack(true);
                        getActivity().finish();
                    }
                }).show();
            } else {
                apiAvailability.getErrorDialog(getActivity(), result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nearby, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textWelcome = getView().findViewById(R.id.textViewWelcome);
        editLocation = getView().findViewById(R.id.editTextLocation);
        textAddress = getView().findViewById(R.id.textViewAddress);
        imageSearch = getView().findViewById(R.id.imageSearch);
        imageCurrentLocation = getView().findViewById(R.id.imageCurrentLocation);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User retrievedUser = dataSnapshot.getValue(User.class);
                textWelcome.setText("Welcome back, " + retrievedUser.getName() + "!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error fetching data from database.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        imageCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Current Location");
                retrieveCurrentLocation();
            }
        });

        imageSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Venue Search");
                if (editLocation.getText().toString().equals("") &&
                        textAddress.getText().toString().equals("NONE")) {
                    Toast.makeText(getActivity(), "Location not found.", Toast.LENGTH_SHORT);
                } else {
                    retrieveNearbyVenues(userLocation);
                }
            }
        });

        editLocation.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    System.out.println("editLocation ACTION SEARCH");
                    return true;
                }
                System.out.println("editLocation NOT ACTION SEARCH");
                return false;
            }
        });
    }

    private void retrieveLastLocation() {
        if (checkLocationPermission()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location != null) {
                                updateAddress(location);
                            }
                        }
                    });
        }
    }

    private void retrieveCurrentLocation() {
        if (checkLocationPermission()) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.getFusedLocationProviderClient(getActivity()).
                    requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(getActivity()).
                                    removeLocationUpdates(this);
                            if (locationResult != null && locationResult.getLocations().size() > 0) {
                                updateAddress(locationResult.getLastLocation());
                            }
                        }
                    }, Looper.getMainLooper());
        }
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                return true;
            }
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] != PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Location permission denied by user. " +
                                "Please grant location permission to fetch current location.",
                        Toast.LENGTH_SHORT).show();
            } else {
                retrieveLastLocation();
            }
        }
    }

    private void updateAddress(Location location) {
        String addressString = reverseGeocode(location);
        textAddress.setText(addressString + timeUpdateInfo());
        userLocation = location;
    }

    private String timeUpdateInfo() {
        //DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat tf = new SimpleDateFormat("h:mm aa");
        Date updateDate = Calendar.getInstance().getTime();
        if (lastUpdate == null) {
            lastUpdate = Calendar.getInstance().getTime();
        }
        String daysAgo = "";

        long diffInMs = Math.abs(updateDate.getTime() - lastUpdate.getTime());
        long diffInDays = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);

        if (diffInDays == 0) {
            daysAgo = "today";
        } else if (diffInDays == 1) {
            daysAgo = "yesterday";
        } else {
            daysAgo = diffInDays + " days ago";
        }
        System.out.println(diffInMs);
        System.out.println(diffInDays);
        String message =  "  (last updated " + daysAgo + " at " + tf.format(updateDate) + ")";
        return message;
    }

    private String reverseGeocode(Location location) {
        String addressString = "";
        if (location != null && Geocoder.isPresent()) {
            try {
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                addressString = addressList.get(0).getAddressLine(0);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return addressString;
    }

    private void retrieveNearbyVenues(Location location) {
        textBlank = getView().findViewById(R.id.textViewBlank);
        textBlankBowl = getView().findViewById(R.id.textViewBlankBowl);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            String gymUrl = placesUrlBuilder(latitude, longitude, "gym");

            Object getNearbyArgs[] = new Object[2];
            getNearbyArgs[0] = gymUrl;
            getNearbyArgs[1] = location;

            GetNearbyVenues getNearbyVenues = new GetNearbyVenues(new GetNearbyVenues.taskResponse() {
                @Override
                public void transferResults(ArrayList venuesArray) {
                    ArrayList<Venue> finalVenueList = processVenuesResult(venuesArray);

                    recyclerView = Objects.requireNonNull(getView()).
                            findViewById(R.id.recyclerVenueDisplay);
                    recyclerView.setHasFixedSize(true);
                    recyclerManager = new LinearLayoutManager(getContext(),
                            LinearLayoutManager.HORIZONTAL, false);
                    recyclerAdapter = new VenueAdapter(finalVenueList);
                    recyclerView.setLayoutManager(recyclerManager);
                    recyclerView.setAdapter(recyclerAdapter);
                    textBlank.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
            getNearbyVenues.execute(getNearbyArgs);

            String bowlUrl = placesUrlBuilder(latitude, longitude, "bowling_alley");

            Object getNearbyArgsBowl[] = new Object[2];
            getNearbyArgsBowl[0] = bowlUrl;
            getNearbyArgsBowl[1] = location;

            GetNearbyVenues getNearbyVenuesBowl = new GetNearbyVenues(new GetNearbyVenues.taskResponse() {
                @Override
                public void transferResults(ArrayList venuesArray) {
                    ArrayList<Venue> finalVenueList = processVenuesResult(venuesArray);

                    recyclerViewBowl = Objects.requireNonNull(getView()).
                            findViewById(R.id.recyclerVenueDisplayBowl);
                    recyclerViewBowl.setHasFixedSize(true);
                    recyclerManager = new LinearLayoutManager(getContext(),
                            LinearLayoutManager.HORIZONTAL, false);
                    recyclerAdapter = new VenueAdapter(finalVenueList);
                    recyclerViewBowl.setLayoutManager(recyclerManager);
                    recyclerViewBowl.setAdapter(recyclerAdapter);
                    textBlankBowl.setVisibility(View.INVISIBLE);
                    recyclerViewBowl.setVisibility(View.VISIBLE);
                }
            });
            getNearbyVenuesBowl.execute(getNearbyArgsBowl);
        } else {
            Toast.makeText(getActivity(), "Location not found. Please enter or search " +
                    "current location, then try again.", Toast.LENGTH_LONG).show();
        }
    }

    private String placesUrlBuilder(double latitude, double longitude, String type) {
        StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/" +
                "nearbysearch/json?");
        urlBuilder.append("key=" + getString(R.string.API_KEY));
        urlBuilder.append("&location=" + latitude + "," + longitude);
        urlBuilder.append("&rankby=distance");
        urlBuilder.append("&type=" + type);
        String url = urlBuilder.toString();
        return url;
    }

    private ArrayList<Venue> processVenuesResult(ArrayList venuesArray) {
        ArrayList<Venue> finalVenueList = new ArrayList<>();
        for (int i = 0; i < venuesArray.size(); i++) {
            List venue = (List) venuesArray.get(i);
            String name = (String) venue.get(0);
            String address = (String) venue.get(1);
            float distance = Float.parseFloat((String) venue.get(2));
            String status = (String) venue.get(3);
            float rating = Float.parseFloat((String) venue.get(4));
            float count = Float.parseFloat((String) venue.get(5));
            String image = (String) venue.get(6);
            String photo = "no_image";

            if (image != "unknown") {
                final StringBuilder photoUrlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
                photoUrlBuilder.append("key=" + getString(R.string.API_KEY));
                photoUrlBuilder.append("&photoreference=" + image);
                photoUrlBuilder.append("&maxwidth=1800");
                photo = photoUrlBuilder.toString();
            }

            finalVenueList.add(new Venue(photo, name, address, distance, status,
                    rating, count));
        }
        return finalVenueList;
    }
}
