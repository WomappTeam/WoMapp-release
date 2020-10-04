package com.example.womapp.Objects;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.womapp.R;

import java.util.Objects;

public class Dialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle("INFORMAZIONE SULLA SEGNALAZIONE")
                .setMessage("Per segnalare la zona in cui ti trovi devi selezionare il motivo e la modalità di segnalazione e premere su invia segnalazione. \n" +
                        "ATTENZIONE: bisogna fare solo una segnalazione al giorno, altrimenti, nel caso in cui vengono effettuate più segnalazioni, nello stesso giorno, da un solo account, verrà tenuta in considerazione solo l’ultima.")
                .setIcon(R.drawable.ic_info)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

}
