package com.example.cami.prueba1;

        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.util.Date;
        import java.util.UUID;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothSocket;
        import android.content.Intent;
        import android.hardware.Sensor;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.Bundle;
        import android.os.Handler;
        import android.util.Log;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.CheckBox;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.content.ActivityNotFoundException;
        import android.hardware.SensorEvent;
        import android.speech.RecognizerIntent;
        import android.support.v7.app.AppCompatActivity;
        import java.util.ArrayList;
        import java.util.Locale;


public class botoneraManual extends AppCompatActivity implements SensorEventListener {

    Button btnLedOn, btnLedOff, btnDis, btnServoDOn, btnServoDOff, btnServoTOn, btnServoTOff;
    CheckBox checkBox;

    TextView txtArduino, txtString, txtStringLength, sensorView0, sensorView1, sensorView2, sensorView3;
    TextView txtSendorLDR, txtDistancia, txtLuces, txtLluvia;
    Handler bluetoothIn;
    private int valueDistance;
    private boolean isRain, isLight;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    Date date, dateA;

    long lastupdate;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    boolean flagSH = true;
    boolean servoSensorMovimiento = true;
    // String for MAC address
    private static String address = null;

    private static final int SHAKE_THRESHOLD = 1500;

    //código para sensores de android
    private SensorManager sensorManager;
   // private ProgressDialog progress;
    ImageButton ibHablar;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    boolean lucesSensorProximidad = false;

    int flagLuces = 0;

    float last_x = 0;
    float last_y = 0;
    float last_z = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(getBaseContext(), "Conexión establecida", Toast.LENGTH_SHORT).show();

        setContentView(R.layout.activity_botonera_manual);
        btnLedOn = (Button) findViewById(R.id.btnledon);
        btnLedOff = (Button) findViewById(R.id.btnledoff);
        btnDis = (Button) findViewById(R.id.btndis);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        btnServoDOn = (Button) findViewById(R.id.btnservodon);
        btnServoDOff = (Button) findViewById(R.id.btnservodoff);
        btnServoTOn = (Button) findViewById(R.id.btnservoton);
        btnServoTOff = (Button) findViewById(R.id.btnservotoff);
        txtLuces = (TextView) findViewById(R.id.txtLuces);
        txtDistancia = (TextView)findViewById(R.id.txtDistancia);
        txtLluvia  = (TextView)findViewById(R.id.txtLluvia);

        //sensores:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //FALTA ENLAZAR IB
        ibHablar = (ImageButton) findViewById(R.id.ibhablar);

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        date = new Date();
        dateA = new Date();

        lastupdate = System.currentTimeMillis();

    }

    @Override
    public void onStart(){
        super.onStart();

        inicializarListeners();
        desactivarBotones();

        Intent intent = getIntent();

        //Obtengo la dirección MAC de la lista de dispositivos de EXTRA
        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        ////VER DE HACER UN ASYNCTASK

        //una vez que obtengo la address, enlazo los dispositivos.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("Conectado");

    }

    @Override
    public void onResume() {
        super.onResume();

        registrarSensores();

    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            mConnectedThread.write("automatico");
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
        unregisterSenser();
    }

    @Override
    public void onStop(){
        unregisterSenser();
        super.onStop();
    }

    private void inicializarListeners(){
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msj) {
                if (msj.what == handlerState) {          //if message is what we want
                    String readMessage = (String) msj.obj; // msg.arg1 = bytes from connect thread
                    Log.d("readMessage:", readMessage);
                    String[] value_split = readMessage.split("\\|");
                    if(value_split[0].equals("1"))
                        txtLluvia.setText("Esta lloviendo");
                    else
                        txtLluvia.setText("No esta lloviendo");
                    if(value_split[1].equals("1"))
                        txtLuces.setText("Es de noche");
                    else
                        txtLuces.setText("Es de día");
                    if(value_split[2].equals("-1"))
                        txtDistancia.setText("No esta en marcha atras");
                    else
                        txtDistancia.setText(value_split[2].toString() + " Metros");
                }
            }
        };

        ibHablar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //empieza a funcionar el microfono:
                speechInput();
            }
        });

        btnDis.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("manual");
                desactivarBotones();
                onPause();
                Intent i = new Intent(botoneraManual.this, MainActivity.class);
                startActivity(i);
            }
        });

        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnServoTOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("servoTraseroOff");    // Send "0" via Bluetooth
                //Toast.makeText(getBaseContext(), "servoTraseroOff", Toast.LENGTH_SHORT).show();
            }
        });

        btnServoTOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("servoTraseroOn");    // Send "1" via Bluetooth
               // Toast.makeText(getBaseContext(), "servoTraseroOn", Toast.LENGTH_SHORT).show();
            }
        });
        btnServoDOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("servoDelanteroOff");    // Send "0" via Bluetooth
                servoSensorMovimiento = false;

            }
        });

        btnServoDOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("servoDelanteroOn");    // Send "1" via Bluetooth
                servoSensorMovimiento = true;

            }
        });
        btnLedOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("lucesDelanteroOff");    // Send "0" via Bluetooth
                lucesSensorProximidad = false;
                //Toast.makeText(getBaseContext(), "Apagar el LED", Toast.LENGTH_SHORT).show();
            }
        });

        btnLedOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("lucesDelanteroOn");    // Send "1" via Bluetooth
                lucesSensorProximidad = true;

            }
        });
        checkBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(checkBox.isChecked()){
                    //si está marcado es porque tengo que activar todos los
                    //botones.
                    mConnectedThread.write("manual");
                    activarBotones();
                }
                if(checkBox.isChecked()==false){
                    mConnectedThread.write("automatico");
                    desactivarBotones();
                }
            }

        });
    }

    private void activarBotones() {
        btnLedOn.setClickable(true);
        btnLedOff.setClickable(true);
        btnServoDOff.setClickable(true);
        btnServoDOn.setClickable(true);
        btnServoTOff.setClickable(true);
        btnServoTOn.setClickable(true);

    }

    private void desactivarBotones(){
        btnLedOn.setClickable(false);
        btnLedOff.setClickable(false);
        btnServoDOff.setClickable(false);
        btnServoDOn.setClickable(false);
        btnServoTOff.setClickable(false);
        btnServoTOn.setClickable(false);
    }

    private void registrarSensores() {
        boolean doneA, doneP, doneL;

        doneA = sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        doneP = sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
        doneL = sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        if(!doneA){
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.sensor_unsupported), Toast.LENGTH_SHORT).show();
        }
        if(!doneP){
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.sensor_unsupported), Toast.LENGTH_SHORT).show();
        }
        if(!doneL){
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.sensor_unsupported), Toast.LENGTH_SHORT).show();
        }
        Log.i("sensor", "register");
    }

    private void unregisterSenser(){
        sensorManager.unregisterListener((SensorEventListener) this);
        Log.i("sensor", "unregister");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();
        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER){

            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastupdate) > 100) {
                long diffTime = (curTime - lastupdate);
                lastupdate = curTime;

                float x = values[SensorManager.DATA_X];
                float y = values[SensorManager.DATA_Y];
                float z = values[SensorManager.DATA_Z];

                float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                Date dateSensedA = new Date();



                if (speed > SHAKE_THRESHOLD) {
                    if (dateSensedA.getTime() < dateA.getTime() + 500) {
                        Log.d("sensor", "shake detected w/ speed: " + speed);
                        Log.d("tiempo dateSensed", String.valueOf(dateSensedA.getTime()));
                        Log.d("tiempo date", String.valueOf(dateA.getTime()));

                        if (servoSensorMovimiento == true) {
                            mConnectedThread.write("servoDelanteroOff");
                            servoSensorMovimiento = false;
                            Log.d("Sensor servo then:",String.valueOf(servoSensorMovimiento));
                        } else {
                            mConnectedThread.write("servoDelanteroOn");
                            servoSensorMovimiento = true;
                            Log.d("Sensor servo else:",String.valueOf(servoSensorMovimiento));
                        }
                    } else {
                        dateA = dateSensedA;

                    }
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
            if (sensorType == Sensor.TYPE_PROXIMITY) {
                Date dateSensed = new Date();
                Log.d("SensorProximidad", String.valueOf(event.values[0]));
                Log.d("tiempo dateSensed", String.valueOf(dateSensed.getTime()));
                Log.d("tiempo date", String.valueOf(date.getTime()));
                if (dateSensed.getTime() < date.getTime() + 500) {
                    if (lucesSensorProximidad == true) {
                        mConnectedThread.write("sensarOff");
                        lucesSensorProximidad = false;
                    } else {
                        mConnectedThread.write("sensarOn");
                        lucesSensorProximidad = true;
                    }
                } else {
                    date = dateSensed;
                }
            }
        if (sensorType == Sensor.TYPE_LIGHT){
            if(Math.abs(values[0])<=50 && flagLuces==1){
                Log.i("sensor luz", "evento");
                //enviar luces on
                mConnectedThread.write("lucesDelanteroOn");
                flagLuces = 0;
            }else{
                if(Math.abs(values[0])>50 && flagLuces==0){
                    Log.i("sensor luz", "apagar");
                    mConnectedThread.write("lucesDelanteroOff");
                    flagLuces = 1;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            String bufferRead = "";
            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    char[] chars = readMessage.toCharArray();
                    for (int i = 0, n = chars.length; i < n; i++) {
                        if(chars[i] == '$')
                            bufferRead = "";
                        else if(chars[i] == '#'){
                            //Log.d("BufferRead:", bufferRead);
                            bluetoothIn.obtainMessage(handlerState, bytes, -1, bufferRead).sendToTarget();
                            bufferRead = "";
                        }else {
                            bufferRead += chars[i];
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Conexión Terminada", Toast.LENGTH_LONG).show();
                finish();

            }
        }

    }

    //Metodos para speechToText()//////////

    private void speechInput(){
        //muesttro un cuadro de dialogo para hablar y reconoce la voz.
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));

        try{
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        }catch (ActivityNotFoundException a){
            Log.e("Error: ", a.toString());
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
                    if(result.get(0).equalsIgnoreCase("activar marcha atrás")){
                        mConnectedThread.write("sensarOn");
                    }
                    else{
                        if(result.get(0).equalsIgnoreCase("desactivar marcha atrás")){
                            mConnectedThread.write("sensarOff");
                        }
                    }
                }
            }
        }
    }
}


