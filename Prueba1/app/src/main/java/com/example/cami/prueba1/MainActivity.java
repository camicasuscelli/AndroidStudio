package com.example.cami.prueba1;

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

    //creo las variables del xml.
    Button btnShowDevices;
    ListView lvListaDispositivos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ligo variables a xml:
        btnShowDevices = (Button) findViewById(R.id.btnShowDevices);
        lvListaDispositivos = (ListView) findViewById(R.id.lvListaDispositivos);

        //muestro por pantalla que está pasando al estado Created.
        Toast.makeText(this, "On-create()",Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onStart(){
        super.onStart();
        Toast.makeText(getApplicationContext(),"onStart()",Toast.LENGTH_LONG).show();
    }



    @Override
    protected void onResume(){
        super.onResume();

        //acá ejecuto una función para linkear el adaptador
        linkearBluetooth();

        btnShowDevices.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mostrarListaDispositivos();

            }
        });
    }

    void mostrarListaDispositivos(){

        //dispositivosVisibles = btAdapter.getBondedDevices();
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
            Intent i = new Intent(MainActivity.this, botoneraManual.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
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
