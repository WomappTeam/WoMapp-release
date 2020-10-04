package com.example.womapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.womapp.Objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LogInActivity extends AppCompatActivity {

    //Final var
    private static final String TAG = "LogInActivity";

    //Widgets Android
    private EditText EmailID, PasswordID;
    private Button LogInButton;
    private ProgressBar BarraDiProgresso;
    private TextView goToSignInActivity;
    private TextView ExceptionMessage;
    private TextView ForgotPassword;

    //Oggetti firebase
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(LogInActivity.this, MapActivity.class));
            firebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference userRef = firebaseFirestore.collection(getString(R.string.DataBaseUser)).document(firebaseAuth.getCurrentUser().getUid());
            userRef.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User currentUser = documentSnapshot.toObject(User.class);
                            if(currentUser.getSesso().equals("Donna"))
                                Toast.makeText(getApplicationContext(), "Accesso eseguito correttamente!\nBenvenuta su WoMapp",Toast.LENGTH_LONG).show();
                            else if(currentUser.getSesso().equals("Uomo") || currentUser.getSesso().equals("Altro"))
                                Toast.makeText(getApplicationContext(), "Accesso eseguito correttamente!\nBenvenuto su WoMapp",Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void initializeWidjets(){
        goToSignInActivity = findViewById(R.id.goToSignIn);
        LogInButton = findViewById(R.id.LogInButton);
        EmailID = findViewById(R.id.emailID);
        PasswordID = findViewById(R.id.passwordID);
        ExceptionMessage = findViewById(R.id.ExceptionMessageS);
        ForgotPassword = findViewById(R.id.forgotPassword);
        BarraDiProgresso = findViewById(R.id.ProgressBarDue);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initializeWidjets();

        //Inizializzazion Barra di progesso ---> INVISIBILE
        BarraDiProgresso.setVisibility(View.INVISIBLE);

        //Inizializzazione Firbase Authenticator
        firebaseAuth = FirebaseAuth.getInstance();

        //Evento click bottone --> log-in utente

        LogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //variables strings
                final String email = EmailID.getText().toString().trim();
                final String password = PasswordID.getText().toString().trim();

                if(validateInputs(email, password)) {
                    Log.d(TAG, "Dati input da tastiera verificati!");
                    BarraDiProgresso.setVisibility(View.VISIBLE);
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG,"Inizio sequenza di logIn");
                            if (task.isSuccessful()) {
                                Log.d(TAG,"Accesso eseguito, verifica indirizzo email");
                                //Se email utente è verificata procedo con log-in
                                if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                    Log.d(TAG,"indirizzo email verificato!");
                                    BarraDiProgresso.setVisibility(View.GONE);
                                   Toast.makeText(getApplicationContext(), "Accesso eseguito correttamente!\nBenvenuto\\a su Womapp", Toast.LENGTH_SHORT).show();
                                   //Collegamento attività Mappa
                                   startActivity(new Intent(LogInActivity.this, MapActivity.class));
                               }else{
                                    Log.d(TAG,"indirizzo email verificato!");
                                    BarraDiProgresso.setVisibility(View.GONE);
                                    ExceptionMessage.setText(R.string.VerificationEmail);
                                    ExceptionMessage.setVisibility(View.VISIBLE);

                                    //Gestione evento click textView -- > rinvio verifica indirizzo email
                                    ExceptionMessage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            firebaseAuth.getCurrentUser().sendEmailVerification()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(getApplicationContext(), "Verifica email inviata a \n"+email, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });
                               }
                            }else {
                                BarraDiProgresso.setVisibility(View.GONE);
                                Log.d(TAG,"Accesso non eseguito!");
                                Log.d(TAG, "onComplete: " + task.getException());
                                ExceptionMessage.setText(R.string.EmailPasswordException);
                                ExceptionMessage.setVisibility(View.VISIBLE);

                            }
                        }
                    });
                }
            }
        });
        //Evento click texView --> collegamento attività di sign-in
        goToSignInActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, SignInActivity.class));
            }
        });

        //Gestione forgot password dialog !

        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetPasswordEmail = new EditText(v.getContext());
                AlertDialog.Builder resetPasswordDialog = new AlertDialog.Builder(v.getContext());
                resetPasswordDialog.setTitle("Reimposta la tua password");
                resetPasswordDialog.setIcon(R.drawable.change_password_icon);
                resetPasswordDialog.setMessage("Inserisci il tuo indirizzo email per rimpostare la password!");
                resetPasswordDialog.setView(resetPasswordEmail);
                resetPasswordDialog.setPositiveButton("INVIA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String resetEmail = resetPasswordEmail.getText().toString();
                        firebaseAuth.sendPasswordResetEmail(resetEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"Reinposta password inviato\nControlla la tua casella postale!", Toast.LENGTH_SHORT).show();
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(),"Errore invio reimposta password!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

                resetPasswordDialog.setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close dialog
                    }
                });
                resetPasswordDialog.create().show();
            }
        });
    }

    private boolean validateInputs(String email, String password) {
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
        if (password.length() < 8) {
            PasswordID.setError("La password deve almeno essere di 8 caratteri");
            PasswordID.requestFocus();
            return false;
        }
        return true;
    }
}
