package com.toVisit_varshini_779380_android.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.toVisit_varshini_779380_android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

public class FavoriteLocationMap extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private LatLng favoritePlaceLatLng;
    private CardView search_place;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean cameraAnimated;
    private Location currentLocation;
    private RequestQueue requestQueue;
    private Polyline route;
    private TextView distanceText, durationText, directionText;
    private ImageView mapIcon;
    private CardView bottomView;
    private String title;
    CardView topView;
    private SQLiteDatabase sqLiteDatabase;
    boolean toastShown;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(FavoriteLocationMap.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(FavoriteLocationMap.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                } else {
                    ActivityCompat.requestPermissions(FavoriteLocationMap.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
            } else {
                finish();
            }
        }
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
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_location_map);

        ImageView change_type = findViewById(R.id.change_type);
        requestQueue = Volley.newRequestQueue(this);
        toastShown = false;

        title = Objects.requireNonNull(getIntent().getExtras()).getString("Title");
        double lat = Double.parseDouble(Objects.requireNonNull(getIntent().getExtras().getString("Latitude")));
        double lon = Double.parseDouble(Objects.requireNonNull(getIntent().getExtras().getString("Longitude")));
        sqLiteDatabase = this.openOrCreateDatabase("UserDB", MODE_PRIVATE, null);

        favoritePlaceLatLng = new LatLng(lat, lon);

        search_place = findViewById(R.id.search_place);
        search_place.setVisibility(View.GONE);

        mapIcon = findViewById(R.id.icon);
        bottomView = findViewById(R.id.bottomCard);
        topView = findViewById(R.id.topCard);
        durationText = findViewById(R.id.duration);
        distanceText = findViewById(R.id.distance);
        directionText = findViewById(R.id.directions);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        change_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(FavoriteLocationMap.this);
                final View dialogView = layoutInflater.inflate(R.layout.dialog_change_map, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(FavoriteLocationMap.this).create();
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        cameraAnimated = false;


        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        MarkerOptions markerOptions = new MarkerOptions().position(favoritePlaceLatLng);

        mMap.addMarker(markerOptions.title(title));

        initLocCallback();

        if (ContextCompat.checkSelfPermission(FavoriteLocationMap.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(FavoriteLocationMap.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(FavoriteLocationMap.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
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
                if (ContextCompat.checkSelfPermission(FavoriteLocationMap.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(FavoriteLocationMap.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
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
                        resolvable.startResolutionForResult(FavoriteLocationMap.this, 1000);
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
                if (!cameraAnimated) {
                    cameraAnimated = true;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(locationResult.getLocations().get(0).getLatitude(),
                                    locationResult.getLocations().get(0).getLongitude()), 15));
                }
                Location location = new Location("");
                location.setLatitude(favoritePlaceLatLng.latitude);
                location.setLongitude(favoritePlaceLatLng.longitude);
                if (locationResult.getLocations().get(0).distanceTo(location) <= 50) {
                    if (!toastShown) {
                        toastShown = true;
                        sqLiteDatabase.execSQL("UPDATE FavoritePlaces SET Visited = 1 " +
                                "WHERE Latitude = '" + favoritePlaceLatLng.latitude + "' AND Longitude = '" + favoritePlaceLatLng.longitude + "';");
                        Toast.makeText(FavoriteLocationMap.this, "You have reached your destination", Toast.LENGTH_SHORT).show();
                    }
                }


                if (currentLocation == null) {
                    currentLocation = locationResult.getLocations().get(0);
                    drawRoute(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), favoritePlaceLatLng);
                } else {
                    if (currentLocation.distanceTo(locationResult.getLocations().get(0)) >= 50) {
                        currentLocation = locationResult.getLocations().get(0);
                        drawRoute(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), favoritePlaceLatLng);
                    }
                }
            }
        };
    }

    private void drawRoute(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String sensor = "sensor=false";
        String mode = "mode=driving";
        String key = "key=" + getString(R.string.google_maps_key);
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + key;

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        System.out.println("Clicked: " + url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String _distance = "";
                        String _duration = "";
                        String _direction = "";
                        JSONObject jObject = null;
                        List<List<HashMap<String, String>>> routes = new ArrayList<>();
                        JSONArray jRoutes = null;
                        JSONArray jLegs = null;
                        JSONArray jSteps = null;

                        JSONObject jsonRespRouteDistance = null;
                        JSONObject jsonRespRouteDuration = null;
                        JSONObject jsonRespRouteDirection = null;

                        try {
                            jsonRespRouteDistance = new JSONObject(response)
                                    .getJSONArray("routes")
                                    .getJSONObject(0)
                                    .getJSONArray("legs")
                                    .getJSONObject(0)
                                    .getJSONObject("distance");

                            jsonRespRouteDuration = new JSONObject(response)
                                    .getJSONArray("routes")
                                    .getJSONObject(0)
                                    .getJSONArray("legs")
                                    .getJSONObject(0)
                                    .getJSONObject("duration");

                            jsonRespRouteDirection = new JSONObject(response)
                                    .getJSONArray("routes")
                                    .getJSONObject(0)
                                    .getJSONArray("legs")
                                    .getJSONObject(0)
                                    .getJSONArray("steps")
                                    .getJSONObject(0);


                            _distance = jsonRespRouteDistance.get("text").toString();
                            _duration = jsonRespRouteDuration.get("text").toString();
                            _direction = jsonRespRouteDirection.get("html_instructions").toString();


                            bottomView.setVisibility(View.VISIBLE);
                            topView.setVisibility(View.VISIBLE);
                            mapIcon.setVisibility(View.VISIBLE);
                            durationText.setText(_duration);
                            distanceText.setText(_distance);
                            directionText.setText(Html.fromHtml(_direction));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        try {

                            jObject = new JSONObject(response);
                            jRoutes = jObject.getJSONArray("routes");
                            for (int i = 0; i < jRoutes.length(); i++) {
                                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                                List path = new ArrayList<HashMap<String, String>>();

                                for (int j = 0; j < jLegs.length(); j++) {
                                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                                    for (int k = 0; k < jSteps.length(); k++) {
                                        String polyline = "";
                                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                                        List list = decodePoly(polyline);

                                        for (int l = 0; l < list.size(); l++) {
                                            HashMap<String, String> hm = new HashMap<String, String>();
                                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                                            path.add(hm);
                                        }
                                    }
                                    routes.add(path);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        if (routes.size() > 0) {
                            ArrayList points = null;
                            PolylineOptions lineOptions = null;
                            MarkerOptions markerOptions = new MarkerOptions();

                            for (int i = 0; i < routes.size(); i++) {
                                points = new ArrayList();
                                lineOptions = new PolylineOptions();

                                List<HashMap<String, String>> path = routes.get(i);

                                for (int j = 0; j < path.size(); j++) {
                                    HashMap<String, String> point = path.get(j);

                                    double lat = Double.parseDouble(point.get("lat"));
                                    double lng = Double.parseDouble(point.get("lng"));
                                    LatLng position = new LatLng(lat, lng);

                                    points.add(position);
                                }

                                lineOptions.addAll(points);
                                lineOptions.width(12);
                                lineOptions.color(Color.RED);
                                lineOptions.geodesic(true);

                            }
                            if (route != null) {
                                route.remove();
                            }
                            route = mMap.addPolyline(lineOptions);
                        } else {
                            Toast.makeText(FavoriteLocationMap.this, "There was an error drawing the path, Please try again later.", Toast.LENGTH_SHORT).show();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(FavoriteLocationMap.this, "There was an error drawing the path, Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });


        requestQueue.add(stringRequest);
    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

}