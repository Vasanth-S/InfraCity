package com.infracity.android.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.infracity.android.R;
import com.infracity.android.model.Path;
import com.infracity.android.model.Step;
import com.infracity.android.rest.DirectionsApi;
import com.infracity.android.rest.PathCallback;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnPolylineClickListener,
        PathCallback, PlaceSelectionListener {

    private static final int PERMISSIONS_CODE = 1001;

    private static final int SETTINGS_ACTIVITY_RESULT = 1001;

    private GoogleMap mMap;

    private Polyline polyline;
    private LatLng startLocation;
    private LatLng endLocation;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        setupMapView();
        setupSearchView();
        setupActionbar();
    }

    private void showProgressBar(String message) {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(message);
        }
        progressDialog.show();
    }

    private void hideProgressBar() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void setupMapView() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        View mapView = mapFragment.getView();
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
    }

    private void setupSearchView() {
        SupportPlaceAutocompleteFragment placeAutocompleteFragment = (SupportPlaceAutocompleteFragment) getSupportFragmentManager()
                .findFragmentById(R.id.place_autocomplete_fragment);
        placeAutocompleteFragment.setOnPlaceSelectedListener(this);
    }

    private void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("");
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(mapClickListener);
        mMap.setOnPolylineClickListener(this);
        moveToCurrentLocation();
    }

    private boolean checkLocationPermission() {
        return checkLocationPermission(false);
    }

    private boolean checkLocationPermission(boolean fromCallback) {
        boolean permissionAvailable = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        if (permissionAvailable) {
            if (!fromCallback) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, PERMISSIONS_CODE);
            }
        } else {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
        }
        return !permissionAvailable;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_CODE) {
            boolean permissionGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    String permission = permissions[i];
                    System.out.println("Permission " + permission + " not granted");
                    permissionGranted = false;
                    break;
                }
            }
            if(permissionGranted) moveToCurrentLocation();
        }
        checkLocationPermission(true);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return checkLocationService();
    }

    private boolean checkLocationService() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        boolean enabled = gps_enabled || network_enabled;
        if (!enabled) {
            // notify user
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getString(R.string.alter_location_settings));
            dialog.setPositiveButton(getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int paramInt) {
                    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(settingsIntent, SETTINGS_ACTIVITY_RESULT);
                    dialogInterface.dismiss();
                }
            });
            dialog.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int paramInt) {
                    dialogInterface.dismiss();
                }
            });
            dialog.show();
        }
        return !enabled;
    }

    private void moveToCurrentLocation() {
        if(!checkLocationPermission()) return;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        boolean enabled = gps_enabled || network_enabled;
        if (enabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                List<String> providers = lm.getProviders(true);
                for(String provider : providers) {
                    Location currentLocation = lm.getLastKnownLocation(provider);
                    if(currentLocation != null) {
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SETTINGS_ACTIVITY_RESULT) {
            moveToCurrentLocation();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private GoogleMap.OnMapClickListener mapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if(startLocation == null) {
                startLocation = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).title("start").draggable(false).icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            } else if (endLocation == null) {
                endLocation = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).title("end").draggable(false).icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                showProgressBar("Fetching the road...");
                DirectionsApi.loadPaths(MapsActivity.this, startLocation, endLocation, MapsActivity.this);
            }
        }
    };

    @Override
    public void onLoadSuccess(Path path) {
        hideProgressBar();
        if(path != null) {
            PolylineOptions options = null;
            for(Step step : path.getSteps()) {
                if(options == null) {
                    options = new PolylineOptions();
                }
                if(step.getPolyline() != null) {
                    options.addAll(step.getPolyline().getPoints());
                }
            }
            if(options != null) {
                polyline = mMap.addPolyline(options.color(Color.BLUE).width(10));
                polyline.setClickable(true);
            }
        } else {
            onLoadFailure("No paths found");
        }
    }

    @Override
    public void onLoadFailure(String errorMessage) {
        hideProgressBar();
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if(startLocation != null || endLocation != null) {
            mMap.clear();
            startLocation = null;
            endLocation = null;
            polyline = null;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        String clickedPolylineId = polyline.getId();
        String drawnPolylineId = this.polyline == null ? "" : polyline.getId();
        if (clickedPolylineId.equalsIgnoreCase(drawnPolylineId)) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Add Review").setMessage("Do you want to add review").show();
            dialog.show();
        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        if(place != null) {
            LatLng latLng = place.getLatLng();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    @Override
    public void onError(Status status) {

    }
}
