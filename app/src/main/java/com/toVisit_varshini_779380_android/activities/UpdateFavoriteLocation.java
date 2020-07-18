package com.toVisit_varshini_779380_android.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.toVisit_varshini_779380_android.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

public class UpdateFavoriteLocation extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Marker userMarker;
    private SQLiteDatabase sqLiteDatabase;
    Button update;
    double favoritePlaceLat, favoritePlaceLon;
    Location currentLocation;
    private ImageView zoomIn, zoomOut, animate;

    protected synchronized void buildGoogleApiClient() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ContextCompat.checkSelfPermission(UpdateFavoriteLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(UpdateFavoriteLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    mMap.setMyLocationEnabled(true);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(UpdateFavoriteLocation.this, 1000);
                    } catch (IntentSender.SendIntentException ignored) {
                    }
                }
            }
        });
    }

    private String getAddress(LatLng latLng) {
        Geocoder geocoder;
        String address = null;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (address == null || address.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            address = sdf.format(new Date());
        }


        return address;

    }

    private void initLocCallback() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                currentLocation = locationResult.getLocations().get(0);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(UpdateFavoriteLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(UpdateFavoriteLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                } else {
                    ActivityCompat.requestPermissions(UpdateFavoriteLocation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_favorite_location);

        favoritePlaceLat = Double.parseDouble(getIntent().getExtras().getString("Latitude"));
        favoritePlaceLon = Double.parseDouble(getIntent().getExtras().getString("Longitude"));


        sqLiteDatabase = this.openOrCreateDatabase("UserDB", MODE_PRIVATE, null);

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS FavoritePlaces (Title VARCHAR, Latitude VARCHAR, Longitude VARCHAR, Visited VARCHAR);");


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ConstraintLayout parent = findViewById(R.id.parent);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(parent);
        constraintSet.connect(R.id.animate, ConstraintSet.BOTTOM, R.id.update, ConstraintSet.TOP, 20);
        constraintSet.applyTo(parent);

        ImageView change_type = findViewById(R.id.change_type);
        update = findViewById(R.id.update);
        update.setVisibility(View.VISIBLE);

        zoomIn = findViewById(R.id.zoom_in);
        zoomOut = findViewById(R.id.zoom_out);

        animate = findViewById(R.id.animate);

        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        animate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
                }
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sqLiteDatabase.execSQL("UPDATE FavoritePlaces SET" +
                        " Title = '" + userMarker.getTitle().replaceAll("'", "''") + "', " +
                        " Latitude = '" + userMarker.getPosition().latitude + "', " +
                        " Longitude = '" + userMarker.getPosition().longitude + "', " +
                        " Visited = '" + "0" + "' WHERE Latitude = '" + favoritePlaceLat + "' AND Longitude = '" + favoritePlaceLon + "';");
                Toast.makeText(UpdateFavoriteLocation.this, "Update successful.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        change_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(UpdateFavoriteLocation.this);
                final View dialogView = layoutInflater.inflate(R.layout.dialog_change_map, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(UpdateFavoriteLocation.this).create();
                alertDialog.setView(dialogView);

                LinearLayout defaultMap = dialogView.findViewById(R.id.default_map);
                LinearLayout satelliteMap = dialogView.findViewById(R.id.satellite);
                LinearLayout terrainMap = dialogView.findViewById(R.id.terrain);

                final TextView defaultText = dialogView.findViewById(R.id.default_text);
                final TextView satelliteText = dialogView.findViewById(R.id.satellite_text);
                final TextView terrainText = dialogView.findViewById(R.id.terrainText);

                if (mMap.getMapType() == MAP_TYPE_NORMAL) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        defaultText.setTextColor(getColor(R.color.colorMapTypeSelected));
                    }
                } else if (mMap.getMapType() == MAP_TYPE_SATELLITE) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        satelliteText.setTextColor(getColor(R.color.colorMapTypeSelected));
                    }
                } else if (mMap.getMapType() == MAP_TYPE_TERRAIN) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        terrainText.setTextColor(getColor(R.color.colorMapTypeSelected));
                    }
                }


                defaultMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        mMap.setMapType(MAP_TYPE_NORMAL);
                    }
                });

                satelliteMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        mMap.setMapType(MAP_TYPE_SATELLITE);
                    }
                });
                terrainMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        mMap.setMapType(MAP_TYPE_TERRAIN);
                    }
                });
                alertDialog.show();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(this);

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(favoritePlaceLat, favoritePlaceLon));
        String address = getAddress(new LatLng(favoritePlaceLat, favoritePlaceLon));
        userMarker = mMap.addMarker(
                markerOptions
                        .title(address));
        userMarker.setDraggable(true);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(favoritePlaceLat, favoritePlaceLon), 15));

        initLocCallback();

        if (ContextCompat.checkSelfPermission(UpdateFavoriteLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(UpdateFavoriteLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(UpdateFavoriteLocation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        String apiKey = getString(R.string.api_key);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        PlacesClient placesClient = Places.createClient(this);


        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setHint("Search here");
        autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS);
        autocompleteFragment.setCountry("CA");
        final ImageView searchIcon = (ImageView) ((LinearLayout) autocompleteFragment.getView()).getChildAt(0);
        searchIcon.setVisibility(View.GONE);
        final EditText etPlace = autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlace.setHintTextColor(getResources().getColor(R.color.colorHint));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                MarkerOptions markerOptions = new MarkerOptions().position(place.getLatLng());
                String address = getAddress(place.getLatLng());
                userMarker = mMap.addMarker(
                        markerOptions
                                .title(address));
                userMarker.setDraggable(true);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            }

            @Override
            public void onError(Status status) {
            }
        });
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        String address = getAddress(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
        userMarker = marker;
        userMarker.setTitle(address);
        if (userMarker.isInfoWindowShown()) {
            userMarker.showInfoWindow();
        }
    }
}