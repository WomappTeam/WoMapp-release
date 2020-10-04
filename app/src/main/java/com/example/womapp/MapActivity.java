package com.example.womapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.womapp.Objects.User;
import com.example.womapp.Objects.UserSegnalation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    //Firebase object
    FirebaseAuth mAuth;
    FirebaseFirestore mStore;

    SimpleDateFormat hours = new SimpleDateFormat("hh");
    SimpleDateFormat Time = new SimpleDateFormat("hh:mm");
    UserSegnalation mUserSegnalation = new UserSegnalation();
    User currentUser = new User();
    Calendar calendar = Calendar.getInstance();
    StorageReference storageReference;


    //Android Widgets
    private TextView displayName;
    private TextView showCurrentDate;
    private ImageButton getMyLocation;
    private ImageButton startAlertButton;
    private ImageButton refreshActivity;
    private ImageButton settingsActivity;
    private ImageView profileImage;
    private ImageButton changeMap;
    private ProgressBar refreshProgressBar;

    //final variables
    private static final String TAG = "MapActivity";
    private static final String DEBUG = "ore";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFALUT_ZOOM = 16f;
    private static final int MIN_HOUR_DAY = 6;
    private static final int MAX_HOUR_DAY = 19;

    //Variables
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedlocationProviderClient;


    private void initializeWidgets() {
        displayName = findViewById(R.id.displayName);
        getMyLocation = findViewById(R.id.getMyLocation);
        startAlertButton = findViewById(R.id.startAlertActivity);
        showCurrentDate = findViewById(R.id.showCurrentData);
        refreshActivity = findViewById(R.id.BtnRefresh);
        settingsActivity = findViewById(R.id.BtnSettings);
        changeMap = findViewById(R.id.BtnChangeMap);
        profileImage = findViewById(R.id.profileImg);
        refreshProgressBar = findViewById(R.id.refreshProgressBar);
        refreshProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        initializeWidgets();

        calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        ;
        showCurrentDate.setText(new String("Data: " + currentDate));

        final StorageReference fileRef = storageReference.child("profileImg/" + mAuth.getCurrentUser().getUid() + ".jpg");
        fileRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);
                    }
                });


        DocumentReference documentReference = mStore.collection(getString(R.string.DataBaseUser)).document(mAuth.getCurrentUser().getUid());
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    currentUser.setUsername(documentSnapshot.getString("username"));
                    displayName.setText(currentUser.getUsername());
                    Toast.makeText(getApplicationContext(), "Ciao " + currentUser.getUsername(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //inizialize toolbar
        Toolbar toolbar = findViewById(R.id.toolBarMappa);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Acquisizione permessi gelocalizzazione
        getLocationPermission();
    }

    private void getDeviceLocation() {
        Log.d(TAG, "Acquisendo la posizione corrente del dispositivo!");
        mFusedlocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedlocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d(TAG, "POSIZIONE TROVATA");
                            moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), DEFALUT_ZOOM);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "POZIONE NON TROVATA");
                    }
                });
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "Muovendo la camera a: lat: \" + latLng.latitude + \", lng: \"+ latLng.longitude");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initializeMap() {
        Log.d(TAG, "Inizializing map!");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "Acquisendo permessi di localizzazione...");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this,
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMESSI ACQUISITI");
            initializeMap();
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, FINE_LOCATION)
                && ActivityCompat.shouldShowRequestPermissionRationale(this, COARSE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("PERMESSI RICHIESTI")
                    .setMessage("Per usufruire del nostro servizio è necessario attivare i permessi di geo localizzazione!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MapActivity.this, new String[]{FINE_LOCATION, COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{FINE_LOCATION, COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    private void LoadAllUsersSegnalation() {
        CollectionReference UserSegnalationRef = mStore.collection(getString(R.string.DatabaseUserSegnalation));
        UserSegnalationRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null)
                    return;
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        UserSegnalation olderUserSegnalation = documentSnapshot.toObject(UserSegnalation.class);
                        LatLng position = new LatLng(olderUserSegnalation.getGeo_point().getLatitude(), olderUserSegnalation.getGeo_point().getLongitude());

                        if (olderUserSegnalation.getUser() != null) {
                            mMap.addMarker(new MarkerOptions()
                                    .title(olderUserSegnalation.getUser().getUsername())
                                    .snippet(olderUserSegnalation.getMotivation())
                                    .position(position)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.segnalation_marker_day)));
                        } else {
                            mMap.addMarker(new MarkerOptions()
                                    .title("Anonimo")
                                    .snippet(olderUserSegnalation.getMotivation())
                                    .position(position)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.segnalation_marker_night)));
                        }
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuLogOut:
                mAuth.signOut();
                finish();
                startActivity(new Intent(this, LogInActivity.class));
                break;
            case R.id.menuHelp:
                Intent getHelp = new Intent(Intent.ACTION_VIEW, Uri.parse("http://womapp.fauser.edu"));
                startActivity(getHelp);
                break;

            case R.id.menuContatti:
                startActivity(new Intent(MapActivity.this, ContactsActivity.class));
                break;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "Permesso NEGATO!");
                            return;
                        }
                    }
                    //Initialize map;
                    Log.d(TAG, "PERMESSI ACQUISITI CREO MAPPA");
                    initializeMap();
                    break;
                }
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "La mappa è pronta!");
        mMap = googleMap;

        LoadAllUsersSegnalation();

        Log.d(TAG, "Ricerca posizione...");
        getDeviceLocation();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);


        getMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        startAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapActivity.this, SegnalationActivity.class));
            }
        });

        refreshActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshProgressBar.setVisibility(View.VISIBLE);
                mMap.clear();
                LoadAllUsersSegnalation();
                refreshProgressBar.setVisibility(View.GONE);
            }
        });

        settingsActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new  Intent(MapActivity.this, ProfileActivity.class));
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profile = new Intent(MapActivity.this, ProfileActivity.class);
                startActivity(profile);
            }
        });

        changeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMapType();
            }
        });

    }

    private void changeMapType(){
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            showCurrentDate.setTextColor(getColor(R.color.white));
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            showCurrentDate.setTextColor(getColor((R.color.black)));
        }
    }
}
