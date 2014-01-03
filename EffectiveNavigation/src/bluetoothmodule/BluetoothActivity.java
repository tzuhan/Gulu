package bluetoothmodule;

import android.annotation.TargetApi;
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
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.effectivenavigation.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class BluetoothActivity extends ActionBarActivity {

    private static BluetoothAdapter mBluetoothAdapter = null; // 用來搜尋、管理藍芽裝置
    private static BluetoothSocket mBluetoothSocket = null; // 用來連結藍芽裝置、以及傳送指令
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 一定要是這組
    //private static OutputStream mOutputStream = null;
    //private final int REQUEST_ENABLE_BT=1;
    private String InputAddress = null;
    private BufferedReader mBufferedReader;
    private final String mBluetoothThreadName = "bluetoothThread";
    private HandlerThread mBluetoothThread;
    private Handler mBluetoothHandler;

    private class ArduinoBluetooth { //dont forget to fill it out
        static final String address = "20:12:05:27:03:20";
        static final String name = "Finger";
        static final String password = "1234";

        public BluetoothDevice device;

        public ArduinoBluetooth() {
            device = null;
        }
    }

    private ArduinoBluetooth mArduinoBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

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

        mArduinoBluetooth = new ArduinoBluetooth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(btDevicesDiscoveredHandler);
        if(mBluetoothHandler != null) {
            mBluetoothHandler.removeCallbacks(connectWithBluetoothAndRead);
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
        getMenuInflater().inflate(R.menu.main, menu);
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
            }
        }
        else if(id == R.id.close_bluetooth) {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
        }
        else {
            Log.d(BluetoothConst.appTag, "unknown menu items in BluetoothActivity");
        }

        return super.onOptionsItemSelected(item);
    }


    private BroadcastReceiver btDevicesDiscoveredHandler = new BroadcastReceiver() {
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
                    Log.d(BluetoothConst.appTag,lineBuffer);
                }

                //Log.d(BluetoothConst.appTag,"device output:" + line);
                //Toast.makeText(BluetoothActivity.this,line,Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                //Toast.makeText(BluetoothActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                Log.d(BluetoothConst.appTag,e.getLocalizedMessage());
                Toast.makeText(BluetoothActivity.this,"IOException during connection with bottle:" + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        }
    };


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
