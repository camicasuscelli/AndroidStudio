package com.example.cami.prueba1;

import android.content.ActivityNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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
    //static final UUID miUUID = UUID.randomUUID();
    private final int REQ_CODE_SPEECH_INPUT = 100;
    //declaro un sensor manager
    private SensorManager sensorManager;

    //creo variable para manejar comunicacion
    private Comunicacion comunicacion = null;

    private final static float ACC = 15;
    int flagLuces=0;

    public Handler handlerAct;

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
        //Log.i("dirección", address);
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

    ///////////////////fin fases de ciclo de vida/////////////////////

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
        //msg("sensor encendido");
    }


    private void unregisterSenser(){
        sensorManager.unregisterListener(this);
        Log.i("sensor", "unregister");
        msg("sensor apagado");
    }


    private void incializarListeners() {

        handlerAct = new Handler(){
            @Override
            public void handleMessage(android.os.Message msj){
                //supongamos que los primeros char son el tipo de mensaje y el resto es el valor.

                String mensajito = msj.obj.toString();
                String codigo = mensajito.substring(0,2);
                String valor = mensajito.substring(2);
                switch(codigo){
                    case "Luces":
                        //escribo lo que está en luces
                        break;
                    case "Lluvia":
                        //escribo lo que está en lluvia
                        break;
                    case "Distancia":
                        //escribo lo que está en distancia
                        break;
                }
            }
        };

        checkBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(checkBox.isChecked()){
                    //si está marcado es porque tengo que activar todos los
                    //botones.
                    activarBotones();
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

                comunicacion.write("lucesDelanteroOn".toString().getBytes());

            }
        });

        btnLedOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                comunicacion.write("lucesDelanteroOff".toString().getBytes());

            }

        });

        btnDis.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                desconectar();

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

    ////////////////////METODOS PARA SPEECHTOTEXT/////////////////////////

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

    //////////////FIN SPEECHTOTEXT////////////////////

    /*private void modoAutomatico(){

        if(btSocket!=null){
            try {
                btSocket.getOutputStream().write("automatico".toString().getBytes());
            } catch (IOException e){
                msg("Error");
            }
        }

    }*/

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
        BluetoothSocket tmp = null;

        @Override
        protected void onPreExecute(){

            //msg("Conectando bt");
            progress = ProgressDialog.show(botoneraManual.this, "conectando..", "Por favor espere!");
        }

        @Override
        protected Void doInBackground(Void... params) {

            try{

                if(btSocket == null || !isBtConnected){
                    Log.i("conexión", "Proceso de conexión");
                    btAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = btAdapter.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(miUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                    //una vez que conecto el socket, creo el hilo de mensajeria:
                    comunicacion = new Comunicacion(btSocket);
                }
            } catch(IOException e) {
                conexionExitosa = false;
                Log.i("error", e.toString());
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

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        //public static final int MESSAGE_TOAST = 2;
       /* public static final int MESSAGE_ENCENDERLUCES = 3;
        public static final int MESSAGE_APAGARLUCES = 4;
        public static final int MESSAGE_SERVODON = 5;
        public static final int MESSAGE_SERVODOFF = 6;
        public static final int MESSAGE_SERVOTON = 7;
        public static final int MESSAGE_SERVOTOFF = 8;
        public static final int MESSAGE_MARCHAATRAS = 9;
        public static final int MESSAGE_DISPLAY = 10;
        public static final int MESSAGE_MANUAL = 11;
        public static final int MESSAGE_AUTOMATICO = 12;
        public static final int MESSAGE_ILUCES = 13;
        public static final int MESSAGE_IDISTANCIA = 14;
        public static final int MESSAGE_LLUVIA = 15;*/
    }


    /*private void ejecutarComunicacion(BluetoothSocket socket){

        final Handler miHandler = new Handler();

        final BluetoothSocket thSocket = socket;
        final InputStream mInputStream = null;
        final OutputStream mOutputStream = null;


        try{
            mInputStream = socket.getInputStream();
        }catch(IOException e){
            Log.e("error:", "Error ocurrio creando input stream");
        }

        //creo output stream.
        try{
            mOutputStream = socket.getOutputStream();
        }catch(IOException e){
            Log.e("error", "error al crear un output stream");
        }

        //creo un thread que implementa un runnable para poder actualizar pantalla.
        new Thread(new Runnable(){

            @Override
            public void run(){

                byte[] miBuffer = new byte[1024];
                int numBytes;

                while(true){

                    try{

                        //leo input stream
                        numBytes = mInputStream.read(miBuffer);

                        Message leerMsj = miHandler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1, miBuffer);

                        leerMsj.sendToTarget();
                        //FALTA AGREGAR UN HANDLER EN EL LADO DE LA ACTIVITY PARA LEER LOS MENSAJES.
                    }catch(IOException e){
                        Log.e("error", "Input stream fue desconectado");
                        break;
                    }
                }

            }

        });

    }*/
    private class Comunicacion extends Thread{

        private Handler miHandler = new Handler();

        private final BluetoothSocket thSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;
        private byte[] miBuffer;

        public Comunicacion(BluetoothSocket socket){

            thSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //obtengo output e input streams, uso objetos temporales porque
            //los streams son finales.


            try{
                tmpIn = socket.getInputStream();
            }catch(IOException e){
                Log.e("error:", "Error ocurrio creando input stream");
            }

            //creo output stream.
            try{
                tmpOut = socket.getOutputStream();
            }catch(IOException e){
                Log.e("error", "error al crear un output stream");
            }

            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

        public void run(){
            //mientras se ejecuta lee los inputstream para ver si llegó algo.
            miBuffer = new byte[1024];
            int numBytes;

            while(true){

                try{

                    //leo input stream
                    numBytes = mInputStream.read(miBuffer);

                }catch(IOException e){
                    Log.e("error", "Input stream fue desconectado");
                    break;
                }


            }
        }

        public void write(byte[] bytes){
            try{
                mOutputStream.write(bytes);
                Log.i("envio:","entre a enviar mensaje");
                //muestra el mensaje enviado a la activity
                //no es necesario.
            }catch(IOException e){
                Log.e("error","sucedio un error enviando datos");
                Toast.makeText(getApplicationContext(),"no se pudo enviar el mensaje", Toast.LENGTH_LONG).show();
            }
        }

        public void cancel(){
            try{
                thSocket.close();
            }catch(IOException e){
                Log.e("error","no se pudo cerrar el socket");
            }
        }
   }
}
//aclaracion: los listener de los sensores deberian ir en onresume para evitar que se ejecuten cuando esta onpause
//los listener de botones pueden estar en start, no hay problema.
//tenemos que tener un servicio donde se hace la mensajería hacia y desde arduino, y tenemos que recibir datos de arduino a la app.
//