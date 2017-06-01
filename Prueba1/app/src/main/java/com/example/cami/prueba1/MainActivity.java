package com.example.cami.prueba1;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.Set;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


public class MainActivity extends AppCompatActivity {

    public static String EXTRA_ADDRESS = "device_address";
    //creo variables para Bluetooth:
    private BluetoothAdapter btAdapter=null;
    private Set<BluetoothDevice> dispositivosVisibles;

    //esta lista contiene los dispositivos visibles.
    final ArrayList lista = new ArrayList();

    //creo las variables del xml.
    Button btnShowDevices, btnSearchDev;
    ListView lvListaDispositivos, lvListaAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ligo variables a xml:
        btnShowDevices = (Button) findViewById(R.id.btnShowDevices);
        lvListaDispositivos = (ListView) findViewById(R.id.lvListaDispositivos);
        btnSearchDev = (Button) findViewById(R.id.btnbuscardisp);
        lvListaAvailable = (ListView) findViewById(R.id.lvDevDisp);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bReceiver, filter);
        msg("acabo de registrar el br");

        //muestro por pantalla que está pasando al estado Created.
        Toast.makeText(this, "On-create()",Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onStart(){
        super.onStart();
        //Toast.makeText(getApplicationContext(),"onStart()",Toast.LENGTH_LONG).show();
        btnShowDevices.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mostrarListaDispositivos();

            }
        });

        btnSearchDev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarDispositivos();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        //acá ejecuto una función para linkear el adaptador
        linkearBluetooth();
    }

    @Override
    protected void onPause(){
        super.onPause();
        btAdapter.cancelDiscovery();
    }


    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            msg("esta recibiendo cosas");
            String accion = intent.getAction();
            //cuando encuentra dispositivo:
            if(BluetoothDevice.ACTION_FOUND.equals(accion)){
                //obtengo el device:
                BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                lista.add(dispositivo.getName() + "\n" + dispositivo.getAddress());
            }
        }
    };
    private void buscarDispositivos() {

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(myIntent);
        }*/
        btAdapter.startDiscovery();
        msg("activo el boton");
        ArrayAdapter adaptador = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, lista);
        lvListaAvailable.setAdapter(adaptador);

        //creo un receptor de broadcast
        //final  ArrayList lista = new ArrayList();

        /*final BroadcastReceiver bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                msg("entra al broadcast receiver");
                String action = intent.getAction();
                //cuando encuentra un dispositivo:
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //obtengo el device del intent
                    BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //lo muestro en una lista de adaptadores.
                    //lista.add(dispositivo.getName() + "\n" + dispositivo.getAddress());
                    lista.add(dispositivo.getName() + "\n" + dispositivo.getAddress());
                    ArrayAdapter<String> adaptador = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, lista);
                    lvListaAvailable.setAdapter(adaptador);
                }

            }
        };*/
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(bReceiver, filter);
        //final ArrayAdapter adaptador = new ArrayAdapter(this, android.R.layout.simple_list_item_1, lista);
        //lvListaAvailable.setAdapter(adaptador);
        //lvListaAvailable.setOnItemClickListener(listener);
    }

    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView av, View v, int arg2, long arg3){

            //tengo que establecer la conexión
            //obtengo la mac del dispositivo, los ultimos 17 char
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Toast.makeText(getApplicationContext(),"Se selecciono un item", Toast.LENGTH_LONG).show();
            //una vez que obtengo los datos del dispositivo tengo que hacer que se conozcan:

            //FALTA EL SUBPROCESO QUE CONECTA EL BLUETOOTH CON EL OTRO DISPOSITIVO.

            //inicio nueva actividad:
            //tengo que hacerlo un método.
           // iniciarBotonera(address);
        }
    };
    void msg(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_LONG).show();
    }

    void iniciarBotonera(String address){

        Intent i = new Intent(MainActivity.this, botoneraManual.class);
        i.putExtra(EXTRA_ADDRESS, address);
        startActivity(i);
        btAdapter.cancelDiscovery();
    }

    void mostrarListaDispositivos(){


        dispositivosVisibles = btAdapter.getBondedDevices();
        ArrayList lista = new ArrayList();

        if(dispositivosVisibles.size() > 0){

            for ( BluetoothDevice bt: dispositivosVisibles) {
                lista.add(bt.getName() + "\n" + bt.getAddress());
            }
        }
        else
            Toast.makeText(getApplicationContext(), "No hay dispositivos enlazados",Toast.LENGTH_LONG).show();

        final ArrayAdapter adaptador = new ArrayAdapter(this, android.R.layout.simple_list_item_1, lista);
        lvListaDispositivos.setAdapter(adaptador);
        lvListaDispositivos.setOnItemClickListener(myListClickListener);
    }
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView av, View v, int arg2, long arg3){

            //obtengo la mac del dispositivo, los ultimos 17 char
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            //inicio nueva actividad:
           iniciarBotonera(address);
        }
    };

    void linkearBluetooth(){

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter == null){
            //si es nulo, el dispositivo no tiene adaptador de bluetooth.
            //lo muestro en un toast.
            Toast.makeText(getApplicationContext(), "bluetooth no disponible", Toast.LENGTH_LONG).show();
        }
        else {
            if(!btAdapter.isEnabled()){
                //si no está activado pido que se active:
                Intent intentActBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentActBT,1);
            }

        }
    }


}
