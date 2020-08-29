package com.br.arley.tragetoriadoispontos;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    static int divider = 20;
    int i = 1;
    double initLat, initLng, finalLat, finalLng, ratioLat, ratioLng;
    private GoogleMap mMap;
    private EditText edtPartidaLat, edtPartidaLng, edtDestinoLat, edtDestinoLng;
    private Button btnTracarTrageto;
    private Handler handler;
    Bitmap carBitmap;
    Marker previousMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        handler = new Handler();

        edtPartidaLat = findViewById(R.id.edit_lat_partida);
        edtPartidaLng = findViewById(R.id.edit_lng_partida);

        edtDestinoLat = findViewById(R.id.edit_lat_destino);
        edtDestinoLng = findViewById(R.id.edit_lng_destino);

        btnTracarTrageto = findViewById(R.id.btn_criar_tragetoria);

        Drawable carDrawable = getDrawable(R.drawable.carro_klinsman);
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
    }

    void criarTragetoria() {
        i = 1;
        int divider = 20;

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

        LatLng carroMov = new LatLng(initLat + (ratioLat * (i)), initLng + (ratioLng * (i)));
        previousMarker = mMap.addMarker(new MarkerOptions().position(carroMov).title("Motorista")
                .icon(BitmapDescriptorFactory.fromBitmap(carBitmap)));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(partida));

        moveCarRunnable.run();
    }

    void pararTragetoria() {
        handler.removeCallbacks(moveCarRunnable);
    }

    void moveCar() {
        if (i <= divider) {
            if (previousMarker != null) previousMarker.remove();
            LatLng carroMov = new LatLng(initLat + (ratioLat * (i)), initLng + (ratioLng * (i)));
            previousMarker = mMap.addMarker(new MarkerOptions().position(carroMov).title("Motorista")
                    .icon(BitmapDescriptorFactory.fromBitmap(carBitmap)));
            if (i >= divider) previousMarker = null;
            i++;
        }else{
            pararTragetoria();
        }


    }

    private Runnable moveCarRunnable = new Runnable() {
        @Override
        public void run() {
            moveCar();
            handler.postDelayed(moveCarRunnable, 1000);
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