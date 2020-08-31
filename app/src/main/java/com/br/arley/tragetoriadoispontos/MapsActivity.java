package com.br.arley.tragetoriadoispontos;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    static int divider = 20;
    int i = 1;
    double initLat, initLng, finalLat, finalLng, ratioLat, ratioLng;
    private GoogleMap mMap;
    private EditText edtPartidaLat, edtPartidaLng, edtDestinoLat, edtDestinoLng;
    private Button btnTracarTrageto;
    private ImageView btnChoosePassenger;
    private Handler handler;
    Polyline polyline = null;
    Bitmap carBitmap;
    Marker previousMarker;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("preferences_key", Context.MODE_PRIVATE);

        if(sharedPreferences.getBoolean("first_time", true)){

            sharedPreferences = getSharedPreferences("preferences_key", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            showPassengerDialog(sharedPreferences, editor);

            editor.putBoolean("first_time", false);
            editor.apply();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        handler = new Handler();

        edtPartidaLat = findViewById(R.id.edit_lat_partida);
        edtPartidaLng = findViewById(R.id.edit_lng_partida);

        edtDestinoLat = findViewById(R.id.edit_lat_destino);
        edtDestinoLng = findViewById(R.id.edit_lng_destino);

        btnChoosePassenger = findViewById(R.id.bt_choose_passenger);
        btnTracarTrageto = findViewById(R.id.btn_criar_tragetoria);

        Drawable carDrawable = selecionarPassageiro(sharedPreferences.getInt("passageiro", 2));

        Bitmap carIcon = ((BitmapDrawable) carDrawable).getBitmap();
        carBitmap = Bitmap.createScaledBitmap(carIcon, 120, 120, false);

        btnTracarTrageto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!edtPartidaLat.getText().toString().isEmpty() && !edtPartidaLng.getText().toString().isEmpty() &&
                        !edtDestinoLat.getText().toString().isEmpty() && !edtDestinoLng.getText().toString().isEmpty()) {
                    criarTragetoria();
                    emptyFields();
                } else {
                    Toast.makeText(MapsActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnChoosePassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = getSharedPreferences("preferences_key", Context.MODE_PRIVATE);
                editor = sharedPreferences.edit();
                showPassengerDialog(sharedPreferences, editor);
            }
        });
    }

    Drawable selecionarPassageiro(int carNum){
        Drawable carDrawable = getDrawable(R.drawable.carro_klinsman);

        switch (carNum){
            case 1:
                carDrawable = getDrawable(R.drawable.carro_arley);
                break;
            case 2:
                carDrawable = getDrawable(R.drawable.carro_klinsman);
                break;
            case 3:
                carDrawable = getDrawable(R.drawable.carro_zeca);
                break;
        }

        return carDrawable;
    }


    void criarTragetoria() {
        i = 1;
        int divider = 20;

       /* Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<>*/


        initLat = Double.parseDouble(edtPartidaLat.getText().toString());
        initLng = Double.parseDouble(edtPartidaLng.getText().toString());

        finalLat = Double.parseDouble(edtDestinoLat.getText().toString());
        finalLng = Double.parseDouble(edtDestinoLng.getText().toString());

        ratioLat = (finalLat - initLat) / divider;
        ratioLng = (finalLng - initLng) / divider;

        LatLng partida = new LatLng(initLat, initLng);
        mMap.addMarker(new MarkerOptions().position(partida).title("Partida").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        LatLng destino = new LatLng(finalLat, finalLng);
        mMap.addMarker(new MarkerOptions().position(destino).title("Destino"));

        PolylineOptions polylineOptions = new PolylineOptions().add(partida, destino);
        polyline = mMap.addPolyline(polylineOptions);
        polyline.setColor(Color.rgb(80,80,80));
        polyline.setWidth(5);

        LatLng carroMov = new LatLng(initLat + (ratioLat * (i)), initLng + (ratioLng * (i)));
        previousMarker = mMap.addMarker(new MarkerOptions().position(carroMov).title("Motorista")
                .icon(BitmapDescriptorFactory.fromBitmap(carBitmap)));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(partida));

        moveCarRunnable.run();
    }

    void showPassengerDialog(final SharedPreferences sharedPreferences, final SharedPreferences.Editor editor){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        //mBuilder.setCancelable(false);

        View mView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        LinearLayout iv_arley = mView.findViewById(R.id.arley);
        LinearLayout iv_klinsman = mView.findViewById(R.id.klinsman);
        LinearLayout iv_zeca = mView.findViewById(R.id.zeca);

        mBuilder.setView(mView);

        final AlertDialog dialog = mBuilder.create();

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        iv_arley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("passageiro", 1);
                editor.apply();
                Drawable carDrawable = selecionarPassageiro(sharedPreferences.getInt("passageiro", 2));
                Bitmap carIcon = ((BitmapDrawable) carDrawable).getBitmap();
                carBitmap = Bitmap.createScaledBitmap(carIcon, 120, 120, false);
                dialog.dismiss();
            }
        });

        iv_klinsman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("passageiro", 2);
                editor.apply();
                Drawable carDrawable = selecionarPassageiro(sharedPreferences.getInt("passageiro", 2));
                Bitmap carIcon = ((BitmapDrawable) carDrawable).getBitmap();
                carBitmap = Bitmap.createScaledBitmap(carIcon, 120, 120, false);
                dialog.dismiss();
            }
        });

        iv_zeca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("passageiro", 3);
                editor.apply();
                Drawable carDrawable = selecionarPassageiro(sharedPreferences.getInt("passageiro", 2));
                Bitmap carIcon = ((BitmapDrawable) carDrawable).getBitmap();
                carBitmap = Bitmap.createScaledBitmap(carIcon, 120, 120, false);
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    void pararTragetoria() {
        handler.removeCallbacks(moveCarRunnable);
    }

    void moveCar() {
        if (previousMarker != null) previousMarker.remove();
        LatLng carroMov = new LatLng(initLat + (ratioLat * (i)), initLng + (ratioLng * (i)));
        previousMarker = mMap.addMarker(new MarkerOptions().position(carroMov).title("Motorista")
                .icon(BitmapDescriptorFactory.fromBitmap(carBitmap)));
        if (i >= divider) previousMarker = null;
        i++;
    }

    private Runnable moveCarRunnable = new Runnable() {
        @Override
        public void run() {
            if (i <= divider) {
                moveCar();
                handler.postDelayed(moveCarRunnable, 1000);
            }else{
                pararTragetoria();
            }

        }
    };

    void emptyFields() {
        edtPartidaLat.setText("");
        edtPartidaLng.setText("");
        edtDestinoLat.setText("");
        edtDestinoLng.setText("");
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }

        mMap.setMyLocationEnabled(true);

    }


    @Override
    protected void onResume() {
        super.onResume();
        int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        switch (errorCode) {
            case ConnectionResult.SERVICE_MISSING:

                break;

            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:

                break;

            case ConnectionResult.SERVICE_DISABLED:
                GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode,
                        0, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        }).show();
                break;

            case ConnectionResult.SUCCESS:
                Log.d("Teste", "Google play service is up-to-date");
                break;
        }
    }
}