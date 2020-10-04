package com.example.womapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.womapp.Objects.User;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;


public class SignInActivity extends AppCompatActivity {

    //Widgets
    private EditText EmailID, PasswordID, ConfirmPasswordID, UsernameID;
    private Button SignInButton;
    private RadioGroup sessoGroup;
    private RadioGroup ageGroup;
    private RadioButton sesso;
    private RadioButton age;
    private ProgressBar BarraDiProgresso;
    private TextView ExceptionMessage;


    //FireBase tools
     FirebaseFirestore db;
     FirebaseAuth firebaseAuth;

    //final vars
    private static final String TAG = "SignInActivity";

    private void initializeWidgets(){
        EmailID = (EditText) findViewById(R.id.EmailID);
        PasswordID = (EditText) findViewById(R.id.passwordID);
        ConfirmPasswordID = (EditText)findViewById(R.id.ConfirmPasswordID);
        BarraDiProgresso = (ProgressBar) findViewById(R.id.progressBar);
        SignInButton = (Button) findViewById(R.id.SignInButton);
        UsernameID = (EditText)findViewById(R.id.usernameID);
        sessoGroup = (RadioGroup) findViewById(R.id.SessoGroup);
        ageGroup = (RadioGroup) findViewById(R.id.EtaGroup);
        ExceptionMessage = (TextView) findViewById(R.id.ExceptionMessageS);
        BarraDiProgresso.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initializeWidgets();

        //Inizializzazione  Oggetti firebase
         firebaseAuth = FirebaseAuth.getInstance();
         db = FirebaseFirestore.getInstance();

        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //final variables strings
                final String email = EmailID.getText().toString().trim();
                final String password = PasswordID.getText().toString().trim();
                final String confirmpassword = ConfirmPasswordID.getText().toString().trim();
                final String username = UsernameID.getText().toString().trim();
                int sessoID = sessoGroup.getCheckedRadioButtonId();
                int ageID = ageGroup.getCheckedRadioButtonId();

                if(validateInputs(email, password, confirmpassword, username,sessoID) && validateAge(ageID)) {
                    BarraDiProgresso.setVisibility(View.VISIBLE);
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        //send verification EMAIL
                                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "ACCOUNT WOMAP CREATO!\nPERFAVORE VERIFICA IL TUO INDIRIZZO EMAIL!", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                ExceptionMessage.setText(R.string.VerificationExeption);
                                                ExceptionMessage.setVisibility(View.VISIBLE);
                                            }
                                        });

                                        BarraDiProgresso.setVisibility(View.GONE);
                                        //Add data to firebase
                                        User user = new User(email,firebaseAuth.getCurrentUser().getUid(),username,sesso.getText().toString(),password, age.getText().toString());

                                        db.collection(getString(R.string.DataBaseUser)).document(firebaseAuth.getCurrentUser().getUid())
                                                .set(user)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                        startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                                    }
                                     else {
                                        BarraDiProgresso.setVisibility(View.GONE);
                                        ExceptionMessage.setText(R.string.SignInException);
                                        ExceptionMessage.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                }
            }

        });
    }

    private boolean validateInputs(String email, String password, String confirmpassword, String Username, int sessoID) {

        if (email.isEmpty()) {
            EmailID.setError("Email Richiesta");
            EmailID.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            PasswordID.setError("Password Richiesta");
            PasswordID.requestFocus();
            return false;
        }
        if (confirmpassword.isEmpty()) {
            ConfirmPasswordID.setError("Confermare la password");
            ConfirmPasswordID.requestFocus();
            return false;
        }
        if (password.length() < 8) {
            PasswordID.setError("La password deve almeno essere di 8 caratteri");
            return false;
        }

        if (!(password.equals(confirmpassword))) {
            ConfirmPasswordID.setError("Le password non coincidono");
            return false;
        }

        switch (sessoID){
            case R.id.Uomo:{
                sesso = findViewById(R.id.Uomo);
                return true;
            }
            case R.id.Donna:{
                sesso = findViewById(R.id.Donna);
                return true;
            }
            case R.id.Altro:{
                sesso = findViewById(R.id.Altro);
                return true;
            }
            default: {
                Toast.makeText(SignInActivity.this, "Inserire il sesso per registrarti!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    private boolean validateAge(int ageID){
        switch (ageID){
            case R.id.a:
                age = findViewById(R.id.a);
                return true;
            case R.id.b:
                age = findViewById(R.id.b);
                return  true;
            case R.id.c:
                age = findViewById(R.id.c);
                return  true;
            case  R.id.over50:
                age = findViewById(R.id.over50);
                return  true;
            default:
                Toast.makeText(getApplicationContext(), "Inserire l'età per registrarti!", Toast.LENGTH_SHORT).show();
                return  false;
        }
    }

}



