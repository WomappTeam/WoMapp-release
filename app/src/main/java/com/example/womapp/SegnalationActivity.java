package com.example.womapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.womapp.Objects.Dialog;
import com.example.womapp.Objects.User;
import com.example.womapp.Objects.UserSegnalation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.DateFormat;
import java.util.Calendar;

public class SegnalationActivity extends AppCompatActivity {

    private static final String TAG = "Segnalation";
    private String currentDate;
    private boolean anonima;

    FirebaseAuth mAuth;
    FirebaseFirestore mStore;
    UserSegnalation currentUserSegnalation;
    FusedLocationProviderClient mFusedlocationProviderClient;
    Calendar calendar;


    //widgets android
    private RadioGroup motivoSegnalazione;
    private RadioGroup modalitàSegnalazione;
    private RadioButton motivo;
    private RadioButton modalità;
    private Button InviaSegnalazione;
    private TextView Exception;
    private ImageButton infoButton;
    private CheckBox doNotShow;


    private static final String PREFS_NAME = "PrefsFile";
    private SharedPreferences userPrefs;

    private void  showDialog(){
        Dialog infoButtonDialog = new Dialog();
        infoButtonDialog.show(getSupportFragmentManager(), "InfoDialog");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        calendar = Calendar.getInstance();
        currentDate  = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        doNotShow = findViewById(R.id.DonotShow);

        userPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        getUserPreferencesData();
    }

    private void getUserPreferencesData(){
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if(sp.contains("prefs_check")) {
            Boolean b = sp.getBoolean("prefs_check", false);
            doNotShow.setChecked(b);
            return;
        }
        showDialog();
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segnalation);

        InviaSegnalazione = findViewById(R.id.InviaSegnalazioneButton);
        Exception = findViewById(R.id.ExceptionR);
        motivoSegnalazione = findViewById(R.id.motivoSegnalazioneGroup);
        modalitàSegnalazione = findViewById(R.id.modalitaSegnalazioneGroup);
        infoButton = findViewById(R.id.infoButton);



        InviaSegnalazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(doNotShow.isChecked()){
                    Boolean boolChecked = doNotShow.isChecked();
                    SharedPreferences.Editor editor = userPrefs.edit();
                    editor.putBoolean("prefs_check", boolChecked);
                    editor.apply();
                }else{
                    SharedPreferences.Editor editor = userPrefs.edit();
                    Log.d(TAG,"Editor pulito");
                    editor.clear();
                    editor.apply();
                }

                int motivazioneId = motivoSegnalazione.getCheckedRadioButtonId();
                int modalitàId = modalitàSegnalazione.getCheckedRadioButtonId();

                if(validateSegnalazione(motivazioneId ) && validateModalità(modalitàId) ){
                    getUserDetails();

                    startActivity(new Intent(SegnalationActivity.this, MapActivity.class));
                }
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

    }

    private boolean validateSegnalazione( int radioId){
        switch (radioId){
            case R.id.motivazione1:
                motivo = findViewById(R.id.motivazione1);
                return true;

            case R.id.motivazione2:
                motivo = findViewById(R.id.motivazione2);
                return true;


            case R.id.motivazione3:
                motivo = findViewById(R.id.motivazione3);
                return true;


            case R.id.motivazione4:
                motivo = findViewById(R.id.motivazione4);
                return true;


            case R.id.motivazione5:
                motivo = findViewById(R.id.motivazione5);
                return true;

            case R.id.motivazione6:
                motivo = findViewById(R.id.motivazione6);
                return true;

            case R.id.motivazione7:
                motivo = findViewById(R.id.motivazione7);
                return true;

            case  R.id.motivazione8:
                motivo = findViewById(R.id.motivazione8);
                return true;

            default:
                Exception.setText(R.string.MotivoException);
                Exception.setVisibility(View.VISIBLE);
                return false;
        }
    }

    private boolean validateModalità(int radioId){

        switch (radioId){
            case R.id.Segnalazione_Anonima:
                modalità = findViewById(R.id.Segnalazione_Anonima);
                anonima = true;
                return true;

            case R.id.Segnalazione_Username:
                modalità = findViewById(R.id.Segnalazione_Username);
                anonima = false;
                return true;

            default:
                Exception.setText(R.string.ModalitaException);
                Exception.setVisibility(View.VISIBLE);
                return false;
        }


    }

    private void getUserDetails(){
        if(currentUserSegnalation == null){
            currentUserSegnalation = new UserSegnalation();
            if(!anonima){
                DocumentReference userRef = mStore.collection(getString(R.string.DataBaseUser)).document(mAuth.getCurrentUser().getUid());
                userRef.get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG,"Dati utenti presi da FireBase");
                                    User currentUser = task.getResult().toObject(User.class);
                                    currentUserSegnalation.setUser(currentUser);
                                    getLastKnownLocation();
                                }
                            }
                        });
            }
            else{
                getLastKnownLocation();
            }


        }
    }

    private void getLastKnownLocation(){
        Log.d(TAG,"GetLastKnownLocation: inizio!");
        mFusedlocationProviderClient =  LocationServices.getFusedLocationProviderClient(this);

        mFusedlocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    if(location != null){
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "Latitudine: " + location.getLatitude() + "\nLongitudine: " + location.getLongitude());
                        currentUserSegnalation.setGeo_point(geoPoint);
                        currentUserSegnalation.setTimestamp(null);
                        currentUserSegnalation.setMotivation(motivo.getText().toString());
                        saveUserLocation();
                    }
                }
            }
        });

    }

    private void saveUserLocation(){
        if(currentUserSegnalation != null){
            DocumentReference locationReference = mStore.collection(getString(R.string.DatabaseUserSegnalation))
                    .document( currentDate + " "+ mAuth.getCurrentUser().getUid());
            locationReference.set(currentUserSegnalation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"GRAZIE PER AVERE SEGNALATO!", Toast.LENGTH_LONG).show();
                        Log.d(TAG,"saveUserLocation: \ninserted user location into database."+ "\n latitudine: "+ currentUserSegnalation.getGeo_point().getLatitude()+ "\n longitudine: "+ currentUserSegnalation.getGeo_point().getLongitude());
                    }
                }
            });
        }
    }

}
