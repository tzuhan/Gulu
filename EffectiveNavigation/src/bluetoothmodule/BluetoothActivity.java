package bluetoothmodule;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.effectivenavigation.DrinksInformation;
import com.example.android.effectivenavigation.R;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class BluetoothActivity extends Activity {

    private static final String noDataInformation = "currently no data to process";
    //private static final String dataCollectedTime = "data collected time:\n";
    //private static final String dataValue = "data value:";
    private static final int maxNumOfDataInQueue = 30; //keep how many training instance in data set
    private static BluetoothAdapter mBluetoothAdapter = null; // 用來搜尋、管理藍芽裝置
    private static BluetoothSocket mBluetoothSocket = null; // 用來連結藍芽裝置、以及傳送指令
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 一定要是這組
    private BufferedReader mBufferedReader;
    private final String mBluetoothThreadName = "bluetoothThread";
    private HandlerThread mBluetoothThread;
    private Handler mBluetoothHandler;
    private Handler mUIThreadHandler;
    private Queue<Pair<String,String []>> unlabeledData; //<time,Data>
    private class ArduinoBluetooth { //dont forget to fill it out
        static final String address = "20:12:05:27:03:20";
        static final String name = "Finger";
        static final String password = "1234";

        public BluetoothDevice device;

        public ArduinoBluetooth() {
            device = null;
        }
    }
    private Calendar calendar;
    private SimpleDateFormat formatter;
    private String[] currentData;
    private int currentLabel;
    private TextView informationText;

    private enum UIEventId {
        NoEvent,
        UpdateInformationText
    }
    UIEventId uiEventId;

    private ArduinoBluetooth mArduinoBluetooth;
    private final static int NUM_OF_VALUES_IN_BT_DATA = 9;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // 如果裝置不支援藍芽
            Toast.makeText(this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        //initialize thread and start it
        mBluetoothThread = new HandlerThread(mBluetoothThreadName);
        mBluetoothThread.start();

        //assign it to a handler
        mBluetoothHandler = new Handler(mBluetoothThread.getLooper());
        mUIThreadHandler = new Handler();

        mArduinoBluetooth = new ArduinoBluetooth();

        unlabeledData = new LinkedList<Pair<String, String[]>>();
        currentData = null;
        currentLabel = -1;
        uiEventId = UIEventId.NoEvent;

        calendar = Calendar.getInstance();
        formatter = new SimpleDateFormat("yyyy/MM/dd EE HH:mm:ss");

        //UI related
        Spinner spinner = (Spinner) findViewById(R.id.drinks_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, DrinksInformation.drinks_list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                currentLabel = position;
                Log.d(BluetoothConst.appTag,"selected position:" + currentLabel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                currentLabel = 0; //first one
            }
        });

        final Button labelButton = (Button)this.findViewById(R.id.send_training_data_button);
        labelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                labelButton.setClickable(false);
                if(currentData != null) {


                }
                labelButton.setClickable(true);
            }
        });

        informationText = (TextView) findViewById(R.id.InformationAboutDataText);
        informationText.setText(noDataInformation);
        Log.d(BluetoothConst.appTag,"initialize done");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(btEventHandler); //disable bluetooth discovering feature
        if(mBluetoothHandler != null) {
            mBluetoothHandler.removeCallbacks(connectWithBluetoothAndRead); //read data
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothHandler != null) {
            mBluetoothHandler.removeCallbacks(connectWithBluetoothAndRead);
        }

        if(mBluetoothThread != null) {
            mBluetoothThread.quit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.open_bluetooth) {
            if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                Log.d(BluetoothConst.appTag,"enable BT");
            }
        }
        else if(id == R.id.close_bluetooth) {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
                Log.d(BluetoothConst.appTag,"disable BT");
            }
        }
        else if(id == R.id.connect_bluetooth) {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(btEventHandler,filter);
                mBluetoothAdapter.startDiscovery();
                Log.d(BluetoothConst.appTag,"start discovering");
            }
        }
        else {
            Log.d(BluetoothConst.appTag, "unknown menu items in BluetoothActivity");
        }

        return super.onOptionsItemSelected(item);
    }


    private BroadcastReceiver btEventHandler = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
        @Override
        public void onReceive(Context context, Intent intent) {
            // 當收尋到裝置時
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                // 取得藍芽裝置這個物件
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(BluetoothConst.appTag,"find device:" + device.getAddress());
                // 判斷那個裝置是不是你要連結的裝置，根據藍芽裝置address判斷
                if (device.getAddress().equals(ArduinoBluetooth.address)){
                    Log.d(BluetoothConst.appTag,"find intended devices");
                    mBluetoothAdapter.cancelDiscovery();
                    mArduinoBluetooth.device = device;

                    //due to a IO operation, we need to do it asynchronously
                    mBluetoothHandler.post(connectWithBluetoothAndRead);

                }

            }
        }
    };

    private Runnable connectWithBluetoothAndRead = new Runnable() {
        @Override
        public void run() {
            try {

                // 連結到該裝置
                mBluetoothSocket = mArduinoBluetooth.device.createRfcommSocketToServiceRecord(MY_UUID);
                mBluetoothSocket.connect();

                Log.d(BluetoothConst.appTag,"connection successes");

                mBufferedReader = new BufferedReader(new InputStreamReader(mBluetoothSocket.getInputStream()));

                String lineBuffer;

                while((lineBuffer = mBufferedReader.readLine()) != null) {
                    //parse data and send to user to label
                    //append data in a list
                    if(unlabeledData.size() < maxNumOfDataInQueue) {
                        String []data = lineBuffer.split(" ");
                        if(data.length == NUM_OF_VALUES_IN_BT_DATA) { //2 * 4 + 1
                            //<time,sensor data>
                            Pair<String,String[]> timeAndData = new Pair<String, String[]>(formatter.format(calendar.getTime()),data);
                            unlabeledData.add(timeAndData);
                            uiEventId = UIEventId.UpdateInformationText;
                            mUIThreadHandler.post(uiEvents);
                            /* this should be done in main thread
                            if(unlabeledData.size() == 1 && currentData == null) {
                                timeAndData = unlabeledData.remove();
                                informationText.setText(timeAndData.first);
                                currentData = timeAndData.second;
                            }
                            */
                        }
                        else {
                            //do nothing
                            Log.d(BluetoothConst.appTag,"wrong number of values parsed from bluetooth data");
                        }
                    }
                }

                //Log.d(BluetoothConst.appTag,"device output:" + line);
                //Toast.makeText(BluetoothActivity.this,line,Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                //Toast.makeText(BluetoothActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                Log.d(BluetoothConst.appTag,e.getLocalizedMessage());
                Toast.makeText(BluetoothActivity.this,"Exception during connection with bottle:" + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        }
    };



    private Runnable uiEvents = new Runnable() {
        @Override
        public void run() {
            if(uiEventId.equals(UIEventId.UpdateInformationText)){
                if(unlabeledData.size() == 1 && currentData == null) {
                    Pair<String,String[]> timeAndData = unlabeledData.remove();
                    String message = timeAndData.first + "\n";
                    for(String subStr : timeAndData.second) {
                        message += subStr + " ";
                    }
                    informationText.setText(message);
                    currentData = timeAndData.second;
                }
            }
            else{
                //do nothing
            }
        }
    };


}
