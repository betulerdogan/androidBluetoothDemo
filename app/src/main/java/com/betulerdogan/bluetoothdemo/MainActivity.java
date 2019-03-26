package com.betulerdogan.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    protected static final String TAG="bluetoothdemo";
    int REQUEST_ENABLE_BT=1;
    EditText main;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main=findViewById(R.id.mainTextArea);
        mNewDevicesArrayAdapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,R.string.app_name);


        Button discover=findViewById(R.id.discoverButton);
        discover.setOnClickListener(discoverButtonHandler);

        ListView lv=findViewById(R.id.pairedBtDevice);
        lv.setAdapter(mNewDevicesArrayAdapter);

        BluetoothAdapter BT=BluetoothAdapter.getDefaultAdapter();
        if(BT==null){
            String noDvMsg="Üzgünüm, bu cihazda bluetooth kullanılamıyor.";
            main.setText(noDvMsg);
            Toast.makeText(this,noDvMsg,Toast.LENGTH_LONG).show();
            return;
        }
        else if(!BT.isEnabled()){
            // bluetooth adapterını aktifleştirmek için kullanıcı izinlerini sorguladık
            Intent enableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
    }

    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQUEST_ENABLE_BT){
            if (resultCode==Activity.RESULT_OK){
                BluetoothAdapter BT=BluetoothAdapter.getDefaultAdapter();
                String address=BT.getAddress();
                String name=BT.getName();
                String connectedMsg="BT açık; cihazınız "+name+" : "+address;
                main.setText(connectedMsg);
                Toast.makeText(this,connectedMsg,Toast.LENGTH_LONG).show();
                Button discoverButton=findViewById(R.id.discoverButton);
                discoverButton.setOnClickListener(discoverButtonHandler);
            }else{
                Toast.makeText(this,"Bilinmeyen istek kodu "+requestCode,Toast.LENGTH_LONG).show();
            }
        }


    }
    /**Kullanıcı keşfet butonuna tıkladığında eşleşmiş cihazları listele*/

    View.OnClickListener discoverButtonHandler=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG,"in onClick("+v+")");
            IntentFilter foundFilter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
            MainActivity.this.registerReceiver(mReceiver,foundFilter);

            IntentFilter doneFilter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            MainActivity.this.registerReceiver(mReceiver,doneFilter);
        }
    };

    protected void onDestroy(){
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    /**Bluetooth bağlantısı alıcı servisimi başlatma ve eşleşen cihazları listeye yerleştir  */

    private final BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            Log.d(TAG,"in onReceive, action = "+action);

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice btDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(btDevice.getBondState() != BluetoothDevice.BOND_BONDED){

                    mNewDevicesArrayAdapter.add(btDevice.getName()+"\n"+btDevice.getAddress());
                }
            }
            else
                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                setTitle(R.string.select_device);
                if(mNewDevicesArrayAdapter.getCount()==0){
                    String noDevice=getResources().getText(R.string.none_paired).toString();
                    mNewDevicesArrayAdapter.add(noDevice);
                }
                }
        }
    };
}
