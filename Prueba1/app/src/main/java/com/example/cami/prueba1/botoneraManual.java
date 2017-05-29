package com.example.cami.prueba1;

import android.content.ActivityNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class botoneraManual extends AppCompatActivity implements SensorEventListener{

    Button btnLedOn, btnLedOff, btnDis, btnServoDOn, btnServoDOff, btnServoTOn, btnServoTOff;
    ImageButton ibHablar;
    CheckBox checkBox;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter btAdapter = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID miUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final int REQ_CODE_SPEECH_INPUT = 100;
    //declaro un sensor manager
    private SensorManager sensorManager;

    private final static float ACC = 15;
    int flagLuces=0;

    //////////////////fases del ciclo de vida de la activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_botonera_manual);

        //linkeo las variables de xml:
        btnLedOn = (Button) findViewById(R.id.btnledon);
        btnLedOff = (Button) findViewById(R.id.btnledoff);
        btnDis = (Button) findViewById(R.id.btndis);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        btnServoDOn = (Button) findViewById(R.id.btnservodon);
        btnServoDOff = (Button) findViewById(R.id.btnservodoff);
        btnServoTOn = (Button) findViewById(R.id.btnservoton);
        btnServoTOff = (Button) findViewById(R.id.btnservotoff);
       // btnMarchaAtras = (Button) findViewById(R.id.btnmarchaatras);
       // btnDisplay = (Button) findViewById(R.id.btndisplay);
        ibHablar = (ImageButton) findViewById(R.id.ibhablar);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Toast.makeText(this, "On-create()", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //recibo la direccion del dispositivo bluetooth.
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);

        checkBox.setChecked(false);
        desactivarBotones();

        incializarListeners();
        new ConexionBT().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //acá creo los servicios de escucha de los sensores del celular.
        //REGISTRAR SENSOR:
        registerSenser();

    }

    @Override
    protected void onStop(){
        unregisterSenser();
        super.onStop();
    }

    @Override
    protected void onPause(){
        unregisterSenser();
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();
        float[] values = event.values;

        if(sensorType == Sensor.TYPE_ACCELEROMETER){
            if(Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC){
                Log.i("sensor", "running");
                msg("Movimiento detectado");
                //acá enviaría un mensaje para encender el actuador correspondiente.
            }
        }else {
            if (sensorType == Sensor.TYPE_LIGHT){
                if(Math.abs(values[0])<=50 && flagLuces==1){
                    Log.i("sensor luz", "evento");
                    msg("Se encienden las luces del led");
                    flagLuces=0;
                }else{
                    if(Math.abs(values[0])>50 && flagLuces==0){
                        Log.i("sensor luz", "apagar");
                        msg("Se apagan las luces del led");
                        flagLuces =1;
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    ///////////////////Métodos custom//////////////////////////////

    private void registerSenser(){

        boolean done;
        done = sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);

        if(!done){
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.sensor_unsupported), Toast.LENGTH_SHORT).show();
        }
        Log.i("sensor", "register");
        msg("sensor encendido");
    }

    private void unregisterSenser(){
        sensorManager.unregisterListener(this);
        Log.i("sensor", "unregister");
        msg("sensor apagado");
    }

    private void incializarListeners() {
        checkBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(checkBox.isChecked()){
                    //si está marcado es porque tengo que activar todos los
                    //botones.
                    activarBotones();
                    msg("ModoManual");
                }
                if(checkBox.isChecked()==false){
                    desactivarBotones();
                    msg("modoAutomatico");
                    //modoAutomatico();

                }
            }
        });
        btnLedOn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //encenderLed();
                msg("encenderled");
            }
        });

        btnLedOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //apagarLed();
                msg("apagar led");
            }

        });

        btnDis.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                desconectar();
                msg("Desconectado");
            }
        });

        ibHablar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //empieza a funcionar el microfono:
                speechInput();
            }
        });

    }

    private void speechInput() {
        //voy a mostrar un cuadro de dialogo para hablar y debería reconocer la voz.
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));

        try{
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        }catch(ActivityNotFoundException a){
            msg("no soporta reconocimiento");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null!= data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //ACÁ VA A ENVIAR RESULT[0] POR LA MENSAJERIA
                    if(result.get(0).equalsIgnoreCase("marcha atrás")){
                        //aca enviaria el mensaje
                        msg(result.get(0));
                    }
                    else{
                        if(result.get(0).equalsIgnoreCase("mostrar display")){
                            msg(result.get(0));
                        }
                    }

                }
            }
        }
    }

    private void modoAutomatico(){

        if(btSocket!=null){
            try {
                btSocket.getOutputStream().write("automatico".toString().getBytes());
            } catch (IOException e){
                msg("Error");
            }
        }

    }

    private void activarBotones() {
        btnLedOn.setClickable(true);
        btnLedOff.setClickable(true);
        btnDis.setClickable(true);
    }
    private void desactivarBotones(){
        btnLedOn.setClickable(false);
        btnLedOff.setClickable(false);
        btnDis.setClickable(false);
    }

    private void msg(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

  /*  void encenderLed(){
        if(btSocket!=null){
            try {
                btSocket.getOutputStream().write("lucesDelanteroOn".toString().getBytes());
            } catch (IOException e){
                msg("Error");
            }
        }
    }*/

 /*   void apagarLed(){
        if(btSocket!=null){
            try {
                btSocket.getOutputStream().write("lucesDelanteroOff".toString().getBytes());
            } catch (IOException e){
                msg("Error");
            }
        }
    }*/

    void desconectar(){
        //si el socket esta ocupado
        if(btSocket!=null){
            try {
                btSocket.close();
            } catch (IOException e){
                msg("Error");
            }
        }
        finish(); //vuelve al primer layout.

    }

    private class ConexionBT extends AsyncTask<Void, Void, Void>{

        private boolean conexionExitosa = true;

        @Override
        protected void onPreExecute(){
            msg("Conectando bt");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                if(btSocket == null || !isBtConnected){
                    btAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = btAdapter.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(miUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch(IOException e) {
                conexionExitosa = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!conexionExitosa){
                msg("falló la conexión");
                finish();
            }
            else{
                msg("conectado");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}



    /*class ConnectBT extends AsyncTask<Void, Void, Void>{
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(botoneraManual.this,"Connecting","Please wait");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                if (btSocket == null || !isBtConnected){
                    btAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = btAdapter.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(miUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    //empezar la conexión
                    btSocket.connect();
                }
            }catch (IOException e){
                ConnectSuccess = false;
            }
            return null;
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
    }*/



//aclaracion: los listener de los sensores deberian ir en onresume para evitar que se ejecuten cuando esta onpause
//los listener de botones pueden estar en start, no hay problema.
//tenemos que tener un servicio donde se hace la mensajería hacia y desde arduino, y tenemos que recibir datos de arduino a la app.
//