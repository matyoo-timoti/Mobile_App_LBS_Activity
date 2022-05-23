package com.asterisk.mobileapplbsactivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.asterisk.mobileapplbsactivity.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    final private int REQUEST_COURSE_ACCESS = 123;
    boolean permissionGranted = false;

    private GoogleMap mMap;

    private LocationManager locManager;
    private LocationListener locListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.asterisk.mobileapplbsactivity.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //---Obtain the SupportMapFragment and get notified when the map is ready to be used---
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //---remove the location listener---
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COURSE_ACCESS);
            return;
        } else {
            permissionGranted = true;
        }
        if (permissionGranted) {
            locManager.removeUpdates(locListener);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        checkAndRequestPermissions();
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COURSE_ACCESS);
            return;
        } else {
            permissionGranted = true;
        }

        if (permissionGranted) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
        }

        //---Changing Views---
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //---Specific Location---
        LatLng daet = new LatLng(14.124270825549074, 122.96277767324638);

        //---Add marker on map---
        //mMap.addMarker(new MarkerOptions().position(daet).title("Daet, Camarines Norte"));

        //---Start maps with Daet---
        mMap.moveCamera(CameraUpdateFactory.newLatLng(daet));

        //---Getting the Location That Was Tapped---
        mMap.setOnMapClickListener((point -> {
            LocationAdd loc = getAddressFromLatLng(point);

            Toast.makeText(getBaseContext(),
                    String.format("Tapped location details:%nLatitude: %s%nLongitude: %s%n%n%s", loc.getLatitude(), loc.getLongitude(), loc.Address),
                    Toast.LENGTH_LONG).show();

            Log.d("ADDRESS", loc.getAddress());
        }));
    }

    private LocationAdd getAddressFromLatLng(LatLng point) {
        //---Geocoder object converts the latitude and longitude into an address---
        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
        String finalAddress;

        double latitude = point.latitude;
        double longitude = point.longitude;

        LocationAdd locationAdd = null;

        try {
            List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);
            Address returnedAddress = addresses.get(0);
            StringBuilder strReturnedAddress = new StringBuilder();

            if (addresses.size() > 0) {

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i));
                }

                finalAddress = strReturnedAddress.toString();
                locationAdd = new LocationAdd(latitude, longitude, finalAddress);

                Log.w("CLICKED ADDRESS: ", strReturnedAddress.toString());

            } else {
                //---if you click on a ocean---
                Toast.makeText(this, "Unknown Location", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return locationAdd;
    }

    //---check and request permissions---
    public void checkAndRequestPermissions() {
        int internet = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        int CoarseLoc = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        int FineLoc = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (internet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (CoarseLoc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (FineLoc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) getBaseContext(), listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_COURSE_ACCESS) {
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        } else {
            super.onRequestPermissionsResult(requestCode, permissions,
                    grantResults);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_3:
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
                break;
            case KeyEvent.KEYCODE_1:
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
                break;
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    public void onMyLocationClick(@NonNull Location loc) {

        LatLng p = new LatLng(
                (int) (loc.getLatitude()),
                (int) (loc.getLongitude()));

        String add = getAddressFromLatLng(p).getAddress();

        Toast.makeText(getBaseContext(),
                String.format("Current location details:%nLatitude: %s%nLongitude: %s%n%n%s", loc.getLatitude(), loc.getLongitude(), add),
                Toast.LENGTH_LONG).show();

        //---Zoom in the current position---
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(p);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
                 /*
                 ---Zoom Levels---
                 1: World
                 5: Landmass/continent
                 10: City
                 15: Streets
                 20: Buildings
                 */
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Moving to current location", Toast.LENGTH_SHORT)
                .show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc) {
            if (loc != null) {

                LatLng p = new LatLng(
                        (int) (loc.getLatitude()),
                        (int) (loc.getLongitude()));

                String add = getAddressFromLatLng(p).getAddress();

                //---Display a toast containing coordinates and address of current location---
                Toast toast = Toast.makeText(getBaseContext(),
                        String.format("Current location update:%nLatitude: %s%nLongitude: %s%n%n%s", loc.getLatitude(), loc.getLongitude(), add),
                        Toast.LENGTH_LONG);

                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 20);
                toast.show();

            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
        }
    }

    //---Location Address---
    private static class LocationAdd {
        public double latitude;
        public double longitude;
        public String Address;

        public LocationAdd(double latitude, double longitude, String address) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.Address = address;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getAddress() {
            return Address;
        }


    }
}