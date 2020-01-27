package com.example.remindex;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.lang.Object;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class NoviZadatakActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    //Date notifiDatum;
    EventiDBHelper dbHelper;
    int RBBrojac=1;
    public static final String SPREMANJE_RB = "SpremljeniRB";   //spremamo RB u lokalnu datoteku kako se ne bi resetirala pri gasenju aplikacije
    int notifiGodina;
    int notifiMjesec;
    int notifiDan;
    int notifiSat=0;
    int notifiMinute=0;
    String bojaFaba=""; //Kreiramo globalnu varijablu koja odreduje boju FAB-a kako bi ju mogli slati
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novi_zadatak);
        dbHelper=new EventiDBHelper(NoviZadatakActivity.this);
        SharedPreferences spremljeniRB = getSharedPreferences(SPREMANJE_RB,0);      //ucitavamo RB iz datoteke
        RBBrojac = spremljeniRB.getInt("spremljeniRB",RBBrojac);        //ucitavamo RB iz datoteke

        EditText editTextDatum = (EditText) findViewById(R.id.editTextDatum);
        EditText editTextVrijeme = (EditText) findViewById(R.id.editTextVrijeme);
        editTextDatum.setKeyListener(null);
        editTextVrijeme.setKeyListener(null);

        editTextDatum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment odabirDatuma = new OdabirDatuma();
                odabirDatuma.show(getSupportFragmentManager(), "Odabir datuma");
            }
        });

        editTextVrijeme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment odabirVremena = new OdabirVremena();
                odabirVremena.show(getSupportFragmentManager(), "Odabir vremena");
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        dbHelper.openDB();
    }
    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.clodeDB();
    }

    @Override
    public void onDateSet(DatePicker view, int godina, int mjesec, int dan) {
        Calendar kalendar = Calendar.getInstance();  //Trenutni datum
        kalendar.set(Calendar.YEAR, godina);
        kalendar.set(Calendar.MONTH, mjesec);
        kalendar.set(Calendar.DAY_OF_MONTH, dan);
        String datumString = DateFormat.getDateInstance(DateFormat.FULL).format(kalendar.getTime());
        //kalendar.add(Calendar.DAY_OF_MONTH,-1);                                                           //Oduzimamo jedan dan od odabranog datuma te
        //notifiDatum = kalendar.getTime();                                                                        //novi datum spremamo u notifiDatum (potrebno za notifikaciju) - drugu metodu koristim
        EditText editTextDatum= (EditText) findViewById(R.id.editTextDatum);
        editTextDatum.setText(datumString);
        notifiGodina=kalendar.get(Calendar.YEAR);
        notifiMjesec=kalendar.get(Calendar.MONTH);
        notifiDan=kalendar.get(Calendar.DAY_OF_MONTH);
        //Toast.makeText(getBaseContext(), "Notifi mjesec = "+(notifiMjesec) , Toast.LENGTH_LONG ).show();                //DONE
    }

    @Override
    public void onTimeSet(TimePicker view, int sati, int minute) {
        EditText EditTextVrijeme = (EditText)findViewById(R.id.editTextVrijeme);
        if(sati<10 && minute<10){
            EditTextVrijeme.setText("0"+sati+" : 0"+minute);   // Rijesavamo problem npr. 4:5 -> 04:05
        }
        else if(sati<10 && minute>9){
            EditTextVrijeme.setText("0"+sati+" : "+minute);
        }
        else if(sati>9 && minute<10){
            EditTextVrijeme.setText(""+sati+" : 0"+minute);
        }
        else{
            EditTextVrijeme.setText(""+sati+" : "+minute);
        }
        notifiSat=sati;
        notifiMinute=minute;
        //Toast.makeText(getBaseContext(), "Novo vrijeme= "+(notifiSat-1)+" : "+notifiMinute , Toast.LENGTH_LONG ).show();               //DONE
    }
    public void odaberiBoju(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.odabir_boja, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FloatingActionButton fabBoja = (FloatingActionButton) findViewById(R.id.fabBoja);
                switch (which) {
                    case 0:
                        fabBoja.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.Crvena)));
                        bojaFaba="Crvena";
                        break;
                    case 1:
                        fabBoja.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.Zelena)));
                        bojaFaba="Zelena";
                        break;
                    case 2:
                        fabBoja.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.Plava)));
                        bojaFaba="Plava";
                        break;
                    case 3:
                        fabBoja.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.Žuta)));
                        bojaFaba="Žuta";
                        break;
                }
            }
        });
        builder.show();
    }
    public void spremiZadatak(View view){

        EditText editTextNazivDogadaja = (EditText) findViewById(R.id.editTextNazivDogadaja);
        EditText editTextDatum = (EditText) findViewById(R.id.editTextDatum);
        EditText editTextVrijeme = (EditText) findViewById(R.id.editTextVrijeme);
        RadioButton radioBtn1Dan = (RadioButton) findViewById(R.id.radioBtn1dan);
        //RadioButton radioBtn1Sat = (RadioButton) findViewById(R.id.radioBtn1sat);
        String nazivDogadaja = editTextNazivDogadaja.getText().toString();
        String datum = editTextDatum.getText().toString();
        String vrijeme = editTextVrijeme.getText().toString();
        if(radioBtn1Dan.isChecked()){
            long unos = dbHelper.insert(RBBrojac, nazivDogadaja, datum, vrijeme, bojaFaba, notifiGodina, notifiMjesec, (notifiDan-1), notifiSat, notifiMinute);
            if(unos==-1){
                Toast.makeText(NoviZadatakActivity.this, "Greška kod spremanja! ID vec postoji, kreirajte događaj još jednom.", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(NoviZadatakActivity.this, "Uspjesno uneseno!, ID: "+RBBrojac, Toast.LENGTH_LONG).show();
            }
        }
        else{
            long unos = dbHelper.insert(RBBrojac, nazivDogadaja, datum, vrijeme, bojaFaba, notifiGodina, notifiMjesec, notifiDan, (notifiSat-1), notifiMinute);
            // -1 znaci da se nije insertalo
            if(unos==-1){
                Toast.makeText(NoviZadatakActivity.this, "Greška kod spremanja! ID vec postoji, kreirajte događaj još jednom.", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(NoviZadatakActivity.this, "Uspjesno uneseno!, ID: "+RBBrojac, Toast.LENGTH_LONG).show();
            }
        }
        RBBrojac++;
        SharedPreferences spremljeniRB = getSharedPreferences(SPREMANJE_RB,0);  //spremamo RB u datoteku
        SharedPreferences.Editor editor=spremljeniRB.edit();
        editor.putInt("spremljeniRB",RBBrojac);
        editor.commit();
        Intent intentVratiSe = new Intent(this, MainActivity.class);
        startActivity(intentVratiSe);
    }
}


