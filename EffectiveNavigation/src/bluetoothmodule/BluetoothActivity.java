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
import com.example.android.effectivenavigation.MainActivity;
import com.example.android.effectivenavigation.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;


import mlmodule.ArffManager;
import mlmodule.DataConst;
import mlmodule.IncrementalClassifier;
import mlmodule.KnnClassifier;
import mlmodule.My1NN;
import weka.core.converters.ArffLoader;

public class BluetoothActivity extends Activity {

    private static final String noDataInformation = "currently no data to process";
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
    private SimpleDateFormat formatter;
    private String[] currentData;
    private int currentLabel;
    private TextView informationText;
    private IntentFilter intentFilter;

    private enum UIEventId {
        NoEvent,
        UpdateInformationText
    }
    UIEventId uiEventId;

    private ArduinoBluetooth mArduinoBluetooth;
    private KnnClassifier mClassifier;
    private String[] featureData;

    private My1NN simpleClassifier;

    private final static int NUM_OF_VALUES_IN_BT_DATA = DrinksInformation.NUM_DATA_VALUES;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        //mClassifier = IncrementalClassifier.getInstance(DrinksInformation.NUM_FEATURE_VALUES, DataConst.attNames, DrinksInformation.drinks_list);

        mClassifier = KnnClassifier.getInstance(DrinksInformation.NUM_FEATURE_VALUES, DataConst.attNames, DrinksInformation.drinks_list);

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
        currentLabel = 0;
        uiEventId = UIEventId.NoEvent;

        formatter = new SimpleDateFormat("yyyy/MM/dd EE HH:mm:ss.SSS");
        featureData = new String[DrinksInformation.NUM_FEATURE_VALUES];

        simpleClassifier = new My1NN();

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
                //nothing
            }
        });

        final Button labelButton = (Button)this.findViewById(R.id.send_training_data_button);
        labelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                labelButton.setClickable(false);
                if(currentData != null) {
                    //do incremental training
                    //Log.d(BluetoothConst.appTag,"lastValue:" + currentData[currentData.length-1]);
                    String featureDataMessage = "";
                    for(int dim = 0;dim < DrinksInformation.NUM_FEATURE_VALUES;dim++) {
                        featureData[dim] = currentData[dim];
                        featureDataMessage += (featureData[dim] + " ");
                    }
                    Log.d(BluetoothConst.appTag,featureDataMessage);
                    mClassifier.addTrainingInstanceAndUpdateClassifier(featureData,
                                                                       currentLabel);
                    //Log.d(BluetoothConst.appTag,"update success");
                    Log.d(BluetoothConst.appTag, "predicted this training instance:" + DrinksInformation.drinks_list[mClassifier.predictInstance(featureData)]);
                    currentData = null;
                    if(unlabeledData.size() > 0){
                        UpdateInformationTextAndGetNextDataInstance();
                    }
                }
                labelButton.setClickable(true);
            }
        });

        final Button saveButton = (Button) findViewById(R.id.save_model_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveButton.setClickable(false);
                mClassifier.saveModel();
                saveButton.setClickable(true);
                Log.d(BluetoothConst.appTag,"save model done");
            }
        });


        final Button loadButton = (Button) findViewById(R.id.load_model_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadButton.setClickable(false);
                mClassifier.loadModel();
                loadButton.setClickable(true);
                Log.d(BluetoothConst.appTag,"load model done");
            }
        });

        final Button clearButton = (Button) findViewById(R.id.clear_model_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearButton.setClickable(false);
                mClassifier.clearModel();
                mClassifier.initializeClassifier();
                clearButton.setClickable(true);
                Log.d(BluetoothConst.appTag,"clear model done");
            }
        });

        final Button discardButton = (Button) findViewById(R.id.discard_data_button);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discardButton.setClickable(false);
                boolean success = UpdateInformationTextAndGetNextDataInstance();
                discardButton.setClickable(true);
                if(success) {
                    Log.d(BluetoothConst.appTag,"discard data");
                }
                else {
                    Log.d(BluetoothConst.appTag,"no data,discard failed");
                }
            }
        });

        final Button clearDataButton = (Button) findViewById(R.id.clear_data_button);
        clearDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearDataButton.setClickable(false);
                clearDataInQueueAndUpdateText();
                clearDataButton.setClickable(true);
                Log.d(BluetoothConst.appTag,"clear data done");
            }
        });

        final Button predictButton = (Button) findViewById(R.id.predict_button);
        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                predictButton.setClickable(false);
                for(int dim = 0;dim < DrinksInformation.NUM_FEATURE_VALUES;dim++) {
                    featureData[dim] = currentData[dim];
                }
                int indexValue = simpleClassifier.getNearestNeighborIndex(featureData);
                Log.d(BluetoothConst.appTag,"" + indexValue);
                String predictedName = DrinksInformation.drinks_list[indexValue];
                //String predictedName = DrinksInformation.drinks_list[mClassifier.predictInstance(featureData)];
                Log.d(BluetoothConst.appTag, "predicted this training instance:" + predictedName);
                Toast.makeText(BluetoothActivity.this,predictedName,Toast.LENGTH_SHORT).show();
                predictButton.setClickable(true);
            }
        });

        workerThread = new HandlerThread("worker1");
        workerThread.start();
        workerEventHandler = new Handler(workerThread.getLooper());

        loadDataButton = (Button) findViewById(R.id.load_data_button);
        loadDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDataButton.setClickable(false);
                workerEventHandler.post(readDataEvent);
            }
        });

        informationText = (TextView) findViewById(R.id.InformationAboutDataText);
        informationText.setText(noDataInformation);

        intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        //onResume will be run after on create so we register eventHandler there
        //registerReceiver(btEventHandler,intentFilter);
    }

    private Handler workerEventHandler;
    private HandlerThread workerThread;
    private Button loadDataButton;
    private Runnable readDataEvent = new Runnable() {
        @Override
        public void run() {
            ArffManager manager = new ArffManager();
            mClassifier.resetClassifier(manager.loadArff());
            loadDataButton.setClickable(true);
            Toast.makeText(BluetoothActivity.this,"loading complete",Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(btEventHandler); //disable bluetooth discovering feature
        if(mBluetoothHandler != null) {
            mBluetoothHandler.removeCallbacks(connectWithBluetoothAndRead); //read data
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(btEventHandler,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
            String actionStr = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(actionStr)) {
                // 取得藍芽裝置這個物件
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(BluetoothConst.appTag,"find device:" + device.getAddress());
                // 判斷那個裝置是不是你要連結的裝置，根據藍芽裝置address判斷
                if (device.getAddress().equals(ArduinoBluetooth.address)){
                    Log.d(BluetoothConst.appTag,"find wanted devices");
                    mBluetoothAdapter.cancelDiscovery();
                    mArduinoBluetooth.device = device;

                    //due to a IO operation, we need to do it asynchronously
                    mBluetoothHandler.post(connectWithBluetoothAndRead);

                }
            }
            else if(actionStr.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(BluetoothConst.appTag,"Disconnected with bluetooth device");
                mBluetoothHandler.removeCallbacks(connectWithBluetoothAndRead);
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
                    //parse data and send to user for labeling
                    //append data in a list
                    String []data = lineBuffer.split(" ");
                    if(data.length == NUM_OF_VALUES_IN_BT_DATA) { //2 * 3 + 1
                        //<time,sensor data>
                        if(unlabeledData.size() < maxNumOfDataInQueue) { //we will keep a max number of data instances in Queue
                            String timeInfo = formatter.format(Calendar.getInstance().getTime());
                            //Log.d(BluetoothConst.appTag,"Time:" + timeInfo);
                            Pair<String,String[]> timeAndData = new Pair<String, String[]>(timeInfo,data);
                            unlabeledData.add(timeAndData);
                            uiEventId = UIEventId.UpdateInformationText;
                            mUIThreadHandler.post(uiEvents); // use main thread to update UI
                            /* this should be done in main thread
                            if(unlabeledData.size() == 1 && currentData == null) {
                                timeAndData = unlabeledData.remove();
                                informationText.setText(timeAndData.first);
                                currentData = timeAndData.second;
                            }
                            */
                        }
                    }
                    else {
                        //do nothing
                        Log.d(BluetoothConst.appTag,"wrong number of values parsed from bluetooth data");
                    }

                }

                //Log.d(BluetoothConst.appTag,"device output:" + line);
                //Toast.makeText(BluetoothActivity.this,line,Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                //Toast.makeText(BluetoothActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                try {
                    mBufferedReader.close();
                }
                catch(IOException e2) {

                }
                Log.d(BluetoothConst.appTag,e.getLocalizedMessage());
                //Toast.makeText(BluetoothActivity.this,"Exception during connection with bottle:" + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        }
    };

    private boolean UpdateInformationTextAndGetNextDataInstance() {
        if(unlabeledData.size() > 0) {
            Pair<String,String[]> timeAndData = unlabeledData.remove();
            String message = timeAndData.first + "\n";
            for(String subStr : timeAndData.second) {
                message += subStr + " ";
            }
            informationText.setText(message);
            currentData = timeAndData.second;
            return true;
        }
        else {
            return false;
        }
    }

    private void clearDataInQueueAndUpdateText() {
        if(unlabeledData.size() > 0) {
            unlabeledData.clear();
        }
        informationText.setText(noDataInformation);
        currentData = null;

    }

    private Runnable uiEvents = new Runnable() {
        @Override
        public void run() {
            if(uiEventId.equals(UIEventId.UpdateInformationText)){
                if(unlabeledData.size() == 1 && currentData == null) {
                    UpdateInformationTextAndGetNextDataInstance();
                }
            }
            else{
                //do nothing
            }
        }
    };



}
