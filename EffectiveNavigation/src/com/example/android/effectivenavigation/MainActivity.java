/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.effectivenavigation;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import bluetoothmodule.ArduinoBluetooth;
import bluetoothmodule.BluetoothConst;
import database.DrinkRecordDataSource;
import mlmodule.My1NN;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    private static BluetoothAdapter mBluetoothAdapter = null; // 用來搜尋、管理藍芽裝置
    private static BluetoothSocket mBluetoothSocket = null; // 用來連結藍芽裝置、以及傳送指令
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 一定要是這組
    private BufferedReader mBufferedReader;
    private final String mBluetoothThreadName = "readBluetoothDataThread";
    private HandlerThread mIOThread;
    public Handler mIOThreadHandler;
    //private IncrementalClassifier mClassifier;
    private ArduinoBluetooth mArduinoBluetooth;
    private FragmentManager mFragmentManager;
    private String bluetoothMessage = null;
    private boolean bluetoothStateChange;
    private int predictedLabel;
    private int previousLabel;
    private float totalDeltaWeight;
    private float previousWeight;
    private float currentWeight;
    private boolean changeCurrentFragment;
    private enum FragmentClass {
        DefaultFragment,
        CurrentDrink,
    }
    private FragmentClass fragmentClass;
    private IntentFilter EventHandle;

    private My1NN simpleClassifier;

    private Random random;
    private boolean isBluetoothEnabled;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    private ViewPager mViewPager;

    private DrinkRecordDataSource mSource;

    private Handler mUIhandler = null;

    //private final int maxMemory = 1024;
    //public LruCache<Integer, Bitmap> mImageCache;
    public static final String activityTag = "MainActivity";
    public static final String startDiscoveringIntentFilterTag = MainActivity.class.getName() + ".startDiscovering";
    public static final String notifyAdapterIntentFilterTag = MainActivity.class.getName() + ".notifyAdapter";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothMessage = null;
        bluetoothStateChange = false;
        predictedLabel = -1;
        previousLabel = -1;
        currentWeight = 0;
        previousWeight = 0;
        totalDeltaWeight = 0;
        changeCurrentFragment = false;
        fragmentClass = FragmentClass.DefaultFragment;
        //mClassifier = IncrementalClassifier.getInstance(DrinksInformation.NUM_FEATURE_VALUES, DataConst.attNames, DrinksInformation.drinks_list);
        //mClassifier.loadModel();
        simpleClassifier = new My1NN();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // 如果裝置不支援藍芽
            Toast.makeText(this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mBluetoothAdapter.isEnabled()) {
            isBluetoothEnabled = true;
        }
        else {
            isBluetoothEnabled = false;
        }
        random = new Random();

        //initialize thread and start it
        mIOThread = new HandlerThread(mBluetoothThreadName);
        mIOThread.start();

        //assign it to a handler
        mIOThreadHandler = new Handler(mIOThread.getLooper());

        mArduinoBluetooth = new ArduinoBluetooth();

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mFragmentManager = getSupportFragmentManager();
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(mFragmentManager,this);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.MainActivityPager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        EventHandle = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        EventHandle.addAction(startDiscoveringIntentFilterTag);
        EventHandle.addAction(notifyAdapterIntentFilterTag);
        EventHandle.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //EventHandle.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        //onResume will be run after on create so we register eventHandler there

        //Database initialization
        mSource = new DrinkRecordDataSource(this);
        try {
            mSource.open();
        }
        catch(Exception e) {
            Log.d(activityTag,Log.getStackTraceString(e));
        }
        //mSource.doSomeInsertionTest();

        /*
        final int cacheSize = maxMemory/8;

        mImageCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount()/1024;
            }
        };
        */
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */



    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private MainActivity mainActivity;
        private FragmentManager mFragmentManager;

        public AppSectionsPagerAdapter(FragmentManager fm, MainActivity pActivity) {
            super(fm);
            mainActivity = pActivity;
            mFragmentManager = fm;
        }

        //be called when current_position = this_position +- 1
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.d(MainActivity.activityTag,"initial position:"+position);
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            if(fragment instanceof BTConfigFragment) {
                ((BTConfigFragment)fragment).setMessageToShow(mainActivity.bluetoothMessage);
            }
            return fragment;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            if(position == 0) {
                if(mainActivity.changeCurrentFragment) {
                    mainActivity.changeCurrentFragment = false;
                    removeFragment((Fragment) object);
                }
            }
            Log.d(MainActivity.activityTag, "destroy position:" + position);
        }

        //if we want to trigger getItem,we need to call removeFragment in destroyItem
        private void removeFragment(Fragment fragment) {
            android.support.v4.app.FragmentTransaction ft =mFragmentManager.beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }

        @Override
        public Fragment getItem(int i) {
            Log.d(MainActivity.activityTag,"getItem new Fragment for position:" + i);
            switch (i) {
                case 0:
                    if(mainActivity.fragmentClass == FragmentClass.CurrentDrink) {
                        Log.d(MainActivity.activityTag,"CurrentDrink Fragment");
                        return CurrentDrinkFragment.newInstance(mainActivity,mainActivity.predictedLabel,mainActivity.currentWeight,mainActivity.totalDeltaWeight);
                    }
                    else {
                        if(mBluetoothAdapter != null) {
                            if(mainActivity.isBluetoothEnabled){
                                mainActivity.bluetoothMessage = BTConfigFragment.btWaitForConnect;
                                Log.d(activityTag,"BT is enabled");
                            }
                            else {
                                mainActivity.bluetoothMessage = BTConfigFragment.btNotEnabled;
                                Log.d(activityTag,"BT isn't enabled");
                            }
                        }
                        else {
                            mainActivity.bluetoothMessage = BTConfigFragment.btNotAvailable;
                        }
                        return BTConfigFragment.newInstance(mainActivity.bluetoothMessage);
                    }


                case 1:
                    return DayDrinkFragment.newInstance(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));

                case 2:
                    return new WeekListFragment();

                default:
                    // The other sections of the app are dummy placeholders.
                    Fragment fragment = new DummySectionFragment();
                    Bundle args = new Bundle();
                    args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
                    fragment.setArguments(args);
                    return fragment;

            }
        }

        //triggered by this.notifyDataSetChanged()
        @Override
        public int getItemPosition(Object object) {
            if(mainActivity.bluetoothStateChange && object instanceof BTConfigFragment) {
                mainActivity.bluetoothStateChange = false;
                return POSITION_NONE;
            }
            else if(mainActivity.changeCurrentFragment && (object instanceof BTConfigFragment || object instanceof CurrentDrinkFragment)) {
                return POSITION_NONE;
            }

            return  POSITION_UNCHANGED;
        }

        @Override
        public int getCount() {
            return 3; //currently 3 tabs
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Current";
                case 1:
                    return "Today";
                case 2:
                    return "Week";
                default:
                    return "";
            }
        }
    }

    public Handler getUIHandler() {
        if(mUIhandler == null) {
            mUIhandler = new Handler();
        }
        return mUIhandler;
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(EventHandler);
        if(mIOThreadHandler != null) {
            mIOThreadHandler.removeCallbacks(connectWithBluetoothAndRead);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(EventHandler,EventHandle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mIOThread != null) {
            mIOThread.quit();
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
                isBluetoothEnabled = true;
                changeCurrentFragment = true;
                fragmentClass = FragmentClass.DefaultFragment;
                mAppSectionsPagerAdapter.notifyDataSetChanged();
            }

        }
        else if(id == R.id.close_bluetooth) {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
                isBluetoothEnabled = false;
                changeCurrentFragment = true;
                fragmentClass = FragmentClass.DefaultFragment;
                mAppSectionsPagerAdapter.notifyDataSetChanged();
            }
        }
        else {
            Log.d(BluetoothConst.appTag, "unknown menu items in BluetoothActivity");
        }

        return super.onOptionsItemSelected(item);
    }

    public BroadcastReceiver EventHandler = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
        @Override
        public void onReceive(Context context, Intent intent) {
            // 當收尋到裝置時
            String actionStr = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(actionStr)) {
                // 取得藍芽裝置這個物件
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(activityTag, "find device:" + device.getAddress());
                // 判斷那個裝置是不是你要連結的裝置，根據藍芽裝置address判斷
                if (device.getAddress().equals(ArduinoBluetooth.address)){
                    Log.d(activityTag,"find wanted devices");
                    //due to wanted device found
                    mBluetoothAdapter.cancelDiscovery();
                    mArduinoBluetooth.device = device;
                    //due to a IO operation, we need to do it asynchronously.That is,in another thread.
                    mIOThreadHandler.post(connectWithBluetoothAndRead);
                }
            }
            else if(actionStr.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(activityTag,"Disconnected with bluetooth device");
                /*
                try{
                    mIOThreadHandler.removeCallbacks(connectWithBluetoothAndRead);
                    fragmentClass = FragmentClass.DefaultFragment;
                    bluetoothStateChange = true;
                    bluetoothMessage = BTConfigFragment.btWaitForConnect;
                    changeCurrentFragment = true;
                    mAppSectionsPagerAdapter.notifyDataSetChanged();
                }
                catch(Exception e) {
                    Log.d(activityTag,Log.getStackTraceString(e));
                }
                */
            }
            else if(actionStr.equals(startDiscoveringIntentFilterTag)) {
                bluetoothStateChange = true;
                bluetoothMessage = BTConfigFragment.btDataLoading;
                mAppSectionsPagerAdapter.notifyDataSetChanged();
                mBluetoothAdapter.startDiscovery();
                Log.d(BluetoothConst.appTag, "start discovering");
            }
            else if(actionStr.equals(notifyAdapterIntentFilterTag)) {
                mAppSectionsPagerAdapter.notifyDataSetChanged();
            }
            else {
                Log.d(activityTag,"unknown action in EventHandler");
            }
        }
    };

    private Runnable connectWithBluetoothAndRead = new Runnable() {
        @Override
        public void run() {
            try {

                // 連結到該裝置
                mBluetoothSocket = mArduinoBluetooth.device.createRfcommSocketToServiceRecord(MY_UUID);
                //mBluetoothSocket = mArduinoBluetooth.device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                mBluetoothSocket.connect();

                Log.d(MainActivity.activityTag,"connection successes");

                mBufferedReader = new BufferedReader(new InputStreamReader(mBluetoothSocket.getInputStream()));

                String lineBuffer = null;
                String[] featureData = new String[DrinksInformation.NUM_FEATURE_VALUES];

                while((lineBuffer = mBufferedReader.readLine())!=null) {
                    Log.d(MainActivity.activityTag,"data read:" + lineBuffer);
                    String []data = lineBuffer.split(" ");
                    int numDataValues = data.length;
                    if(numDataValues == DrinksInformation.NUM_DATA_VALUES) {
                        //put into predictor and get the result
                        //calculate the difference of weight
                        //retrieve from database
                        previousLabel = predictedLabel;
                        for(int dim = 0;dim < DrinksInformation.NUM_FEATURE_VALUES;dim++) {
                            featureData[dim] = data[dim];
                        }
                        //predictedLabel = mClassifier.predictInstance(featureData);
                        predictedLabel = simpleClassifier.getNearestNeighborIndex(featureData);
                        //predictedLabel = 0; //simulate the predicted result
                        currentWeight = Float.valueOf(data[numDataValues - 1]);
                        if(previousLabel != predictedLabel) {
                            if(previousLabel >= 0){
                                mSource.createDrinkRecord(DrinksInformation.drinks_list[previousLabel], Calendar.getInstance(), totalDeltaWeight);
                            }
                            totalDeltaWeight = 0;
                        }
                        else {
                            float deltaWeight = previousWeight - currentWeight;
                            //float deltaWeight = random.nextInt(200) - 50;
                            if(deltaWeight >= 0) {
                                totalDeltaWeight += deltaWeight;
                            }
                            else {
                                Log.d(activityTag,"warning:previous weight < current weight");
                            }
                        }

                        changeCurrentFragment = true;
                        fragmentClass = FragmentClass.CurrentDrink;
                        Intent intent = new Intent(notifyAdapterIntentFilterTag);
                        getApplicationContext().sendBroadcast(intent);
                        break;
                    }
                    else {
                        Log.d(activityTag,"data read failed");
                    }
                }
                mBufferedReader.close();

            } catch (Exception e) {
                //Toast.makeText(BluetoothActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

                try {
                    mBufferedReader.close();
                }
                catch(IOException e2) {

                }
                Log.d(MainActivity.activityTag,e.getLocalizedMessage());
                Toast.makeText(MainActivity.this,"Exception during connection with bottle:" + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();

            }
        }
    };

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_dummy, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
}
