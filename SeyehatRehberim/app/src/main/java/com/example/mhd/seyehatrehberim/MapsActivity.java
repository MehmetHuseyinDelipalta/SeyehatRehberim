package com.example.mhd.seyehatrehberim;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                SharedPreferences sharedPreferences= MapsActivity.this.getSharedPreferences("com.example.mhd.seyehatrehberim",MODE_PRIVATE);
                //kaydetmemiz gereken tek kelimelik şeyleri kaydetmeye yarar
                boolean firstTimeCheck= sharedPreferences.getBoolean("notFirstTime",false);
                //uygulamanın daha önce açılıp açılmadığınna bakmaya yarar açılmadıysa false olarak gelir
                if (!firstTimeCheck) {
                    //uygulamayı ilk defa kullanıyorsa

                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    //haritada yaklastirma yapmaya yarar
                    sharedPreferences.edit().putBoolean("notFirstTime",true).apply();
                    //ilk defa çalıştırılıp çalıştırılmadığına dair değeri true yapar ve kabul eder
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                //izin varmi kontrol et yoksa iste
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                //lokasyonlari almaya basla Sureler ne kadar az olursa sarji o kadar cok yer

                mMap.clear();
                //map bilgilerini silmeyey yarar
                Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation !=null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            //lokasyonlari almaya basla Sureler ne kadar az olursa sarji o kadar cok yer
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Kullanicinin izni yoksa verdiginde ne olacaginin secilecegi yer
        if (grantResults.length>0){
            if (requestCode==1){
                if (ContextCompat.checkSelfPermission( this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    //lokasyonlari almaya basla Sureler ne kadar az olursa sarji o kadar cok yer
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastLocation != null) {
                        LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                    }
                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        //adresleribizim verdiğimiz adres ile eşitler -
        String address="";
        //İşlemleri adrese ekleme
        try {
            List<Address> addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);

            if (addressList!=null && addressList.size()>0){
                if (addressList.get(0).getThoroughfare()!=null){
                    address+= addressList.get(0).getThoroughfare();

                    if(addressList.get(0).getSubThoroughfare()!=null){
                        address +=addressList.get(0).getSubThoroughfare();
                    }
                }
            }else{
                address="New Place";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        mMap.addMarker(new MarkerOptions().title(address).position(latLng));
        Toast.makeText(getApplicationContext(), "New Place OK!", Toast.LENGTH_SHORT).show();

        try{
            Double l1=latLng.latitude;
            Double l2=latLng.longitude;

            String coord1=l1.toString();
            String coord2=l2.toString();

            database=this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS places (name VARCHAR, latitude VARCHAR)");

            String toCompile="INSERT INTO places(name,latitude)VALUES (?,?,?)";

            SQLiteStatement sqLiteStatement=database.compileStatement(toCompile);

            sqLiteStatement.bindString(1,address);
            sqLiteStatement.bindString(2,coord1);
            sqLiteStatement.bindString(3,coord2);

            sqLiteStatement.execute();


        } catch (Exception e){

            }
    }
}
