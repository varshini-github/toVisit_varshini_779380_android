package com.toVisit_varshini_779380_android.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private boolean cameraAnimated;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Marker userMarker;
    private ImageView favorite;
    private SQLiteDatabase sqLiteDatabase;
    private HorizontalScrollView nearby;
    private Button btnCafes, btnRestaurants, btnMuseums;
    private boolean displayingNearby;
    private Location currentLocation;
    private RequestQueue requestQueue;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
            } else {
                finish();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (userMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
            String address = getAddress(latLng);
            userMarker = mMap.addMarker(
                    markerOptions
                            .title(address));
            userMarker.setTag("UserMarker");
            userMarker.setDraggable(true);
            showFavoriteOption();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (marker.getTag() != null && !marker.getTag().equals("UserMarker")) {
            marker.showInfoWindow();
            ArrayList<String> favoriteLat = new ArrayList<>();
            ArrayList<String> favoriteLon = new ArrayList<>();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + "FavoritePlaces", null);
            int latColumn = cursor.getColumnIndex("Latitude");
            int lonColumn = cursor.getColumnIndex("Longitude");
            cursor.moveToFirst();

            if (cursor != null && cursor.getCount() != 0) {
                do {
                    favoriteLat.add(cursor.getString(latColumn));
                    favoriteLon.add(cursor.getString(lonColumn));
                } while (cursor.moveToNext());
            }
            final String userLat = Double.toString(marker.getPosition().latitude);
            final String userLon = Double.toString(marker.getPosition().longitude);
            boolean sameFavoriteExists = false;
            for (int i = 0; i < favoriteLat.size(); i++) {
                if (favoriteLat.get(i).equals(userLat) &&
                        favoriteLon.get(i).equals(userLon)) {
                    sameFavoriteExists = true;
                }
            }
            if (sameFavoriteExists) {
                Toast.makeText(MapsActivity.this, "You have already marked this location as favorite", Toast.LENGTH_SHORT).show();
            } else {
                LayoutInflater layoutInflater = LayoutInflater.from(MapsActivity.this);
                final View dialogView = layoutInflater.inflate(R.layout.dialog_confirmation, null);
                final AlertDialog confirmSaveLocation = new AlertDialog.Builder(MapsActivity.this).create();
                confirmSaveLocation.setView(dialogView);
                confirmSaveLocation.setCancelable(false);

                TextView message = dialogView.findViewById(R.id.message);
                Button confirm_button = dialogView.findViewById(R.id.confirm_button);
                Button cancel_button = dialogView.findViewById(R.id.cancel_button);

                message.setText("Do you want to add " + marker.getTitle() + " to favorites?");
                confirm_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        confirmSaveLocation.dismiss();
                        sqLiteDatabase.execSQL("INSERT INTO FavoritePlaces VALUES " +
                                "('" + marker.getTitle().replaceAll("'", "''") + "'," +
                                "'" + marker.getPosition().latitude + "'," +
                                "'" + marker.getPosition().longitude + "'," +
                                "'" + "0" + "');");
                        Toast.makeText(MapsActivity.this, "Added to favorites.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        confirmSaveLocation.dismiss();
                    }
                });

                confirmSaveLocation.show();
            }
        }

        return true;
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
        userMarker.setTag("UserMarker");
        userMarker.setTitle(address);
        if (userMarker.isInfoWindowShown()) {
            userMarker.showInfoWindow();
        }
    }

    private void showFavoriteOption() {
        favorite.setVisibility(View.VISIBLE);
    }

    private void hideFavoriteOption() {
        favorite.setVisibility(View.GONE);
    }

    private String getAddress(LatLng latLng) {
        Geocoder geocoder;
        String address = null;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (address == null || address.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            address = sdf.format(new Date());
        }


        return address;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sqLiteDatabase = this.openOrCreateDatabase("UserDB", MODE_PRIVATE, null);

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS FavoritePlaces (Title VARCHAR, Latitude VARCHAR, Longitude VARCHAR, Visited VARCHAR);");

        requestQueue = Volley.newRequestQueue(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        displayingNearby = false;

        ImageView change_type = findViewById(R.id.change_type);
        favorite = findViewById(R.id.favorite);
        nearby = findViewById(R.id.nearby);
        btnCafes = findViewById(R.id.cafes);
        btnMuseums = findViewById(R.id.museums);
        btnRestaurants = findViewById(R.id.restaurants);

        nearby.setVisibility(View.VISIBLE);

        btnCafes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayingNearby = true;
                btnCafes.setBackground(getResources().getDrawable(R.drawable.bg_button_selected));
                btnCafes.setTextColor(getResources().getColor(R.color.colorButtonSelected));

                btnRestaurants.setBackground(getResources().getDrawable(R.drawable.bg_button_unselected));
                btnRestaurants.setTextColor(getResources().getColor(R.color.colorBlack));

                btnMuseums.setBackground(getResources().getDrawable(R.drawable.bg_button_unselected));
                btnMuseums.setTextColor(getResources().getColor(R.color.colorBlack));


                getNearbyPlace("cafe");
            }
        });

        btnRestaurants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayingNearby = true;
                btnRestaurants.setBackground(getResources().getDrawable(R.drawable.bg_button_selected));
                btnRestaurants.setTextColor(getResources().getColor(R.color.colorButtonSelected));

                btnCafes.setBackground(getResources().getDrawable(R.drawable.bg_button_unselected));
                btnCafes.setTextColor(getResources().getColor(R.color.colorBlack));

                btnMuseums.setBackground(getResources().getDrawable(R.drawable.bg_button_unselected));
                btnMuseums.setTextColor(getResources().getColor(R.color.colorBlack));


                getNearbyPlace("restaurant");
            }
        });

        btnMuseums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayingNearby = true;
                btnMuseums.setBackground(getResources().getDrawable(R.drawable.bg_button_selected));
                btnMuseums.setTextColor(getResources().getColor(R.color.colorButtonSelected));

                btnCafes.setBackground(getResources().getDrawable(R.drawable.bg_button_unselected));
                btnCafes.setTextColor(getResources().getColor(R.color.colorBlack));

                btnRestaurants.setBackground(getResources().getDrawable(R.drawable.bg_button_unselected));
                btnRestaurants.setTextColor(getResources().getColor(R.color.colorBlack));

                getNearbyPlace("museum");

            }
        });


        change_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(MapsActivity.this);
                final View dialogView = layoutInflater.inflate(R.layout.dialog_change_map, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
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

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> favoriteLat = new ArrayList<>();
                ArrayList<String> favoriteLon = new ArrayList<>();
                Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + "FavoritePlaces", null);
                int latColumn = cursor.getColumnIndex("Latitude");
                int lonColumn = cursor.getColumnIndex("Longitude");
                cursor.moveToFirst();

                if (cursor != null && cursor.getCount() != 0) {
                    do {
                        favoriteLat.add(cursor.getString(latColumn));
                        favoriteLon.add(cursor.getString(lonColumn));
                    } while (cursor.moveToNext());
                }
                final String userLat = Double.toString(userMarker.getPosition().latitude);
                final String userLon = Double.toString(userMarker.getPosition().longitude);
                boolean sameFavoriteExists = false;
                for (int i = 0; i < favoriteLat.size(); i++) {
                    if (favoriteLat.get(i).equals(userLat) &&
                            favoriteLon.get(i).equals(userLon)) {
                        sameFavoriteExists = true;
                    }
                }
                if (sameFavoriteExists) {
                    Toast.makeText(MapsActivity.this, "You have already marked this location as favorite", Toast.LENGTH_SHORT).show();
                } else {
                    LayoutInflater layoutInflater = LayoutInflater.from(MapsActivity.this);
                    final View dialogView = layoutInflater.inflate(R.layout.dialog_confirmation, null);
                    final AlertDialog confirmSaveLocation = new AlertDialog.Builder(MapsActivity.this).create();
                    confirmSaveLocation.setView(dialogView);
                    confirmSaveLocation.setCancelable(false);

                    TextView message = dialogView.findViewById(R.id.message);
                    Button confirm_button = dialogView.findViewById(R.id.confirm_button);
                    Button cancel_button = dialogView.findViewById(R.id.cancel_button);

                    message.setText("Do you want to add " + userMarker.getTitle() + " to favorites?");
                    confirm_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            confirmSaveLocation.dismiss();
                            sqLiteDatabase.execSQL("INSERT INTO FavoritePlaces VALUES " +
                                    "('" + userMarker.getTitle().replaceAll("'", "''") + "'," +
                                    "'" + userLat + "'," +
                                    "'" + userLon + "'," +
                                    "'" + "0" + "');");
                            Toast.makeText(MapsActivity.this, "Added to favorites.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    cancel_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            confirmSaveLocation.dismiss();
                        }
                    });

                    confirmSaveLocation.show();

                }


            }
        });

    }

    private void getNearbyPlace(String category) {

        mMap.clear();
        if (userMarker != null) {
            MarkerOptions markerOptions = new MarkerOptions().position(userMarker.getPosition()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
            userMarker = mMap.addMarker(markerOptions.title(userMarker.getTitle()));
            userMarker.setTag("UserMarker");
        }
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";

        url = url + "location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude();

        url = url + "&radius=1000&type=" + category + "&key=" + getString(R.string.google_maps_key);

        System.out.println("Clicked: " + url);


        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject;
                        JSONArray jResults;

                        String latitude = "";
                        String longitude = "";
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String name = sdf.format(new Date());

                        try {
                            jsonObject = new JSONObject(response);
                            jResults = jsonObject.getJSONArray("results");


                            for (int i = 0; i < jResults.length(); i++) {
                                if (jResults.getJSONObject(i).getString("name") != null || !jResults.getJSONObject(i).getString("name").equals("")) {
                                    name = jResults.getJSONObject(i).getString("name");
                                }
                                latitude = jResults.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                                longitude = jResults.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                                        .title(name)).setTag("Nearby");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MapsActivity.this, "There was an error getting nearby.", Toast.LENGTH_SHORT).show();
                    }
                });


        requestQueue.add(stringRequest);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        cameraAnimated = false;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        initLocCallback();

        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
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

        final ImageView clearIcon = (ImageView) ((LinearLayout) autocompleteFragment.getView()).findViewById(R.id.places_autocomplete_clear_button);
        clearIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userMarker != null) {
                    clearIcon.setVisibility(View.GONE);
                    etPlace.setText(null);
                    userMarker.remove();
                    userMarker = null;
                    hideFavoriteOption();
                }
            }
        });

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (userMarker != null) {
                    String address = getAddress(place.getLatLng());
                    userMarker.setPosition(place.getLatLng());
                    userMarker.setTitle(address);
                    if (userMarker.isInfoWindowShown()) {
                        userMarker.showInfoWindow();
                    }
                } else {

                    MarkerOptions markerOptions = new MarkerOptions().position(place.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
                    String address = getAddress(place.getLatLng());
                    userMarker = mMap.addMarker(
                            markerOptions
                                    .title(address));
                    userMarker.setDraggable(true);
                    userMarker.setTag("UserMarker");
                    showFavoriteOption();
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            }

            @Override
            public void onError(Status status) {
            }
        });
    }

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
                if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
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
                        resolvable.startResolutionForResult(MapsActivity.this, 1000);
                    } catch (IntentSender.SendIntentException ignored) {
                    }
                }
            }
        });
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
                if (!cameraAnimated) {
                    cameraAnimated = true;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(locationResult.getLocations().get(0).getLatitude(),
                                    locationResult.getLocations().get(0).getLongitude()), 15));
                }
            }
        };
    }

    @Override
    public void onBackPressed() {

        if (displayingNearby) {

            btnCafes.setBackground(getResources().getDrawable(R.drawable.bg_button_unselected));
            btnCafes.setTextColor(getResources().getColor(R.color.colorBlack));

            btnMuseums.setBackground(getResources().getDrawable(R.drawable.bg_button_unselected));
            btnMuseums.setTextColor(getResources().getColor(R.color.colorBlack));

            btnRestaurants.setBackground(getResources().getDrawable(R.drawable.bg_button_unselected));
            btnRestaurants.setTextColor(getResources().getColor(R.color.colorBlack));

            mMap.clear();
            displayingNearby = false;
            if (userMarker != null) {
                MarkerOptions markerOptions = new MarkerOptions().position(userMarker.getPosition()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
                userMarker = mMap.addMarker(markerOptions.title(userMarker.getTitle()));
                userMarker.setTag("UserMarker");
            }
        } else {
            if (userMarker != null) {
                mMap.clear();
                userMarker = null;
                hideFavoriteOption();
            } else {
                super.onBackPressed();
            }
        }
    }
}