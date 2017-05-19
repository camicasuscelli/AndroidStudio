package com.example.cami.prueba1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;

public class botoneraManual extends AppCompatActivity {

    Button btnLedOn, btnLedOff, btnDis;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter btAdapter = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID miUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_botonera_manual);

        //linkeo las variables de xml:
        btnLedOn = (Button) findViewById(R.id.btnledon);
        btnLedOff = (Button) findViewById(R.id.btnledoff);
        btnDis = (Button) findViewById(R.id.btndisc);

        Toast.makeText(this, "On-create()",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart(){
        super.onStart();

        //recibo la direccion del dispositivo bluetooth.
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);
    }


    //Esta clase inicia la conexión:
    //ARREGLAR PROBLEMA.
    private class ConnectBT extends AsyncTask<void, void, void>{
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(botoneraManual.this,"Connecting","Please wait");
        }

        @Override
        protected void doInBarckground(Void...devices){
            try{
                if (btSocket == null || !isBtConnected){
                    btAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = btAdapter.getRemoteDevice(address):
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(miUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }catch (IOException e){
                ConnectSuccess = false;
            }
            //return null;
        }

        @Override
        protected void onPostExecute (Void result){
            super.onPostExecute(result);

            if(!ConnectSuccess){
                Toast.makeText(getApplicationContext(),"Conexión fallo, intente de nuevo",Toast.LENGTH_LONG).show();

                finish();
            }else {
                Toast.makeText(getApplicationContext(),"Conectado",Toast.LENGTH_LONG).show();
                isBtConnected = true;
            }
            progress.dismiss();

        }
    }

}
