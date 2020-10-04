package com.example.womapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.womapp.Objects.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class ProfileActivity extends AppCompatActivity {

    //Android Widgets
    private TextView showUsername, showEmail, showSex;
    private Button goToMapActivity, deleteAccountButton;
    private Toolbar toolbar;
    private ImageButton infoSttings;
    private ImageView profileImage;

    //FireBase obgects
    FirebaseAuth mAuth;
    FirebaseFirestore mStore;
    StorageReference storageReference;

    private static final String TAG = "ProfileActivity" ;

    @Override
    protected void onStart() {
        super.onStart();
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        final StorageReference fileRef = storageReference.child("profileImg/"+mAuth.getCurrentUser().getUid()+".jpg");
        fileRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializaWidgets();
        getUserDetails();

        setSupportActionBar(toolbar);

        goToMapActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MapActivity.class));
            }
        });

        showUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeUsername(v);
            }
        });

        showEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                changeEmailAddress(v);
            }
        });

        showSex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSex(v);
            }
        });

        infoSttings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("INFORMAZIONE PROFILO");
                builder.setMessage("In questa schermata puoi modificare o aggiungere informazioni relative al tuo account.\n" +
                        "Fai tap sull'informazione che vuoi modificare");
                builder.setIcon(R.drawable.ic_info);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close dialog
                    }
                });
                builder.create().show();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGallery, 1001);
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount(v);
            }
        });


    }

    private void deleteAccount(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("ELIMINAZIONE ACCOUNT WOMAPP!");
        builder.setIcon(R.drawable.ic_warning);
        builder.setMessage("Sei sicuro di eliminare il tuo account?");
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                //Delete Account from Firestore
                DocumentReference userRef = mStore.collection(getString(R.string.DataBaseUser)).document(mAuth.getCurrentUser().getUid());
                userRef.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: Account eliminato correttamente dal database!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: ERRORE ! " + e.getMessage());
                                dialog.dismiss();
                            }
                        });

                //Delete Account from Firebase Authenticator
                mAuth.getCurrentUser().delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: Account eliminato correttamente dal authenticator!");
                                startActivity(new Intent(ProfileActivity.this, LogInActivity.class));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: ERRORE! " + e.getMessage());
                                dialog.dismiss();
                            }
                        });

            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1001 && resultCode == Activity.RESULT_OK){
            Uri imageUri = data.getData();
            //profileImage.setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(final Uri imageUri) {
        //upload image to firebase storage
        final StorageReference fileRef = storageReference.child("profileImg/"+mAuth.getCurrentUser().getUid()+".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(),"Immagine caricata!", Toast.LENGTH_SHORT).show();
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"FAILED!",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuLogOutSettings:
                mAuth.signOut();
                finish();
                startActivity(new Intent(ProfileActivity.this, LogInActivity.class));
                break;
        }

        return true;
    }

    private void getUserDetails() {
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        DocumentReference userRef = mStore.collection(getString(R.string.DataBaseUser)).document(mAuth.getCurrentUser().getUid());
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        try {
                            User currentUser = documentSnapshot.toObject(User.class);
                            showUsername.setText(currentUser.getUsername());
                            showEmail.setText(currentUser.getEmail());
                            showSex.setText(currentUser.getSesso());
                        }catch (NullPointerException e){
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initializaWidgets() {
        showUsername = (TextView) findViewById(R.id.showUsername);
        showEmail = (TextView) findViewById(R.id.showEmail);
        showSex = (TextView) findViewById(R.id.showSex);
        goToMapActivity = (Button) findViewById(R.id.goToMap);
        toolbar = (Toolbar) findViewById(R.id.toolBarSettings);
        infoSttings = (ImageButton) findViewById(R.id.infoSettings);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        deleteAccountButton = (Button) findViewById(R.id.delete_account);
    }

    private void changeUsername(View v){
        final EditText newUsername = new EditText(v.getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("CAMBIA USERNAME");
        builder.setIcon(R.drawable.ic_info);
        builder.setMessage("Inserire nuovo username");
        builder.setView(newUsername);
        builder.setPositiveButton("CAMBIA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final DocumentReference userRef = mStore.collection(getString(R.string.DataBaseUser)).document(mAuth.getCurrentUser().getUid());
                userRef.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User currentUser = documentSnapshot.toObject(User.class);
                                currentUser.setUsername(newUsername.getText().toString());
                                userRef.set(currentUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Username cambiato!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });

            }
        });
        builder.setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //close dialog
            }
        });
        builder.create().show();
    }

    private void changeEmailAddress(View v){
        final EditText newEmail = new EditText(v.getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("CAMBIA EMAIL");
        builder.setIcon(R.drawable.ic_info);
        builder.setMessage("Inserire nuova email");
        builder.setView(newEmail);
        builder.setPositiveButton("CAMBIA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mAuth.getCurrentUser().updateEmail(newEmail.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"Indirizzo email cambiato! " +
                                        "Verifica il tuo nuovo indirizzo email",Toast.LENGTH_SHORT).show();
                            }
                        });

                mAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "verifica inviata");
                    }
                });

                final DocumentReference userRef = mStore.collection(getString(R.string.DataBaseUser)).document(mAuth.getCurrentUser().getUid());
                userRef.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User currentUser = documentSnapshot.toObject(User.class);
                                currentUser.setEmail(newEmail.getText().toString());
                                userRef.set(currentUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });
                            }
                        });

                startActivity(new Intent(ProfileActivity.this, LogInActivity.class));

            }
        });
        builder.setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //close dialog
            }
        });
        builder.create().show();
    }

    private void changeSex(View v){
        final RadioGroup sessoG = new RadioGroup(v.getContext());
        final RadioButton uomo = new RadioButton(v.getContext());
        final RadioButton donna = new RadioButton(v.getContext());
        final RadioButton altro = new RadioButton(v.getContext());
        uomo.setText("Uomo");
        donna.setText("Donna");
        altro.setText("Altro");
        sessoG.addView(uomo);
        sessoG.addView(donna);
        sessoG.addView(altro);
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("CAMBIA SESSO");
        builder.setIcon(R.drawable.ic_info);
        builder.setMessage("Inserire nuovo sesso");
        builder.setView(sessoG);
        builder.setPositiveButton("CAMBIA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(uomo.isChecked()){
                    Log.d(TAG,"Sesso selezionato " + uomo.getText().toString());
                    whatSexIsSelected(uomo);
                }
                else if(donna.isChecked()){
                    Log.d(TAG,"Sesso selezionato " + donna.getText().toString());
                    whatSexIsSelected(donna);
                }
                else if(altro.isChecked()){
                    Log.d(TAG,"Sesso selezionato " + altro.getText().toString());
                    whatSexIsSelected(altro);
                }
            }
        });
        builder.setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //close dialog
            }
        });
        builder.create().show();
    }

    private void whatSexIsSelected(@NotNull final RadioButton r){
        Log.d(TAG,"Funzione sesso: "+ r.getText().toString());
        final DocumentReference userRef = mStore.collection(getString(R.string.DataBaseUser)).document(mAuth.getCurrentUser().getUid());
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        currentUser.setSesso(r.getText().toString());;
                        userRef.set(currentUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Sesso Cambiato!", Toast.LENGTH_SHORT);
                            }
                        });
                    }
                });
    }
}
