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
import java.util.Arrays;
import java.util.UUID;

import bluetoothmodule.BluetoothConst;
import mlmodule.DataConst;
import mlmodule.IncrementalClassifier;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    private static BluetoothAdapter mBluetoothAdapter = null; // 用來搜尋、管理藍芽裝置
    private static BluetoothSocket mBluetoothSocket = null; // 用來連結藍芽裝置、以及傳送指令
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 一定要是這組
    private BufferedReader mBufferedReader;
    private final String mBluetoothThreadName = "readBluetoothDataThread";
    private HandlerThread mBluetoothThread;
    private Handler mBluetoothHandler;
    private IncrementalClassifier mClassifier;

    private static class ArduinoBluetooth { //dont forget to fill it out
        static final String address = "20:12:05:27:03:20";
        static final String name = "Finger";
        static final String password = "1234";

        public BluetoothDevice device;

        public ArduinoBluetooth() {
            device = null;
        }
    }

    private ArduinoBluetooth mArduinoBluetooth;
    private FragmentManager mFragmentManager;
    private String bluetoothMessage = null;
    private boolean bluetoothStateChange;
    private int predictedLabel;
    private int previousLabel;
    private float totalDeltaWeight;
    private float previousWeight;
    private float currentWeight;
    private boolean toVisualizeCurrentDrink;
    private enum FragmentClass {
        DefaultFragment,
        CurrentDrink,
        BTConfig,
        DayDrink,
        WeekList,
    }
    private FragmentClass fragmentClass;
    private IntentFilter EventHandle;

    public static final String activityTag = "MainActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;

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
        toVisualizeCurrentDrink = false;
        fragmentClass = FragmentClass.DefaultFragment;
        mClassifier = IncrementalClassifier.getInstance(DrinksInformation.NUM_FEATURE_VALUES, DataConst.attNames, DrinksInformation.drinks_list);
        mClassifier.loadModel();

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
        //onResume will be run after on create so we register eventHandler there

    }

    private Handler getUIHandler() {
        return new Handler();
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
                if(mainActivity.toVisualizeCurrentDrink) {
                    mainActivity.toVisualizeCurrentDrink = false;
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
                        return CurrentDrinkFragment.newInstance(mainActivity.predictedLabel,mainActivity.currentWeight,mainActivity.totalDeltaWeight);
                    }
                    else {
                        if(mBluetoothAdapter != null) {
                            if(mBluetoothAdapter.isEnabled()){
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

                /*
                case 1:
                    //the drinking data today
                    break;
                */
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

        @Override
        public int getItemPosition(Object object) {

            if(mainActivity.bluetoothStateChange && object instanceof BTConfigFragment) {
                mainActivity.bluetoothStateChange = false;
                return POSITION_NONE;
            }
            else if(mainActivity.toVisualizeCurrentDrink && object instanceof BTConfigFragment){
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

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(EventHandler);
        if(mBluetoothHandler != null) {
            mBluetoothHandler.removeCallbacks(connectWithBluetoothAndRead);
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
                bluetoothStateChange = true;
                bluetoothMessage = BTConfigFragment.btWaitForConnect;
                mAppSectionsPagerAdapter.notifyDataSetChanged();
            }

        }
        else if(id == R.id.close_bluetooth) {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
                bluetoothStateChange = true;
                bluetoothMessage = BTConfigFragment.btNotEnabled;
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
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                // 取得藍芽裝置這個物件
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(activityTag, "find device:" + device.getAddress());
                // 判斷那個裝置是不是你要連結的裝置，根據藍芽裝置address判斷
                if (device.getAddress().equals(ArduinoBluetooth.address)){
                    Log.d(activityTag,"find wanted devices");
                    mBluetoothAdapter.cancelDiscovery();
                    mArduinoBluetooth.device = device;
                    //due to a IO operation, we need to do it asynchronously.That is,in another thread.
                    mBluetoothHandler.post(connectWithBluetoothAndRead);

                }
            }
            else if(intent.getAction().equals(startDiscoveringIntentFilterTag)) {
                bluetoothStateChange = true;
                bluetoothMessage = BTConfigFragment.btDataLoading;
                mAppSectionsPagerAdapter.notifyDataSetChanged();
                mBluetoothAdapter.startDiscovery();
                Log.d(BluetoothConst.appTag, "start discovering");
            }
            else if(intent.getAction().equals(notifyAdapterIntentFilterTag)) {
                mAppSectionsPagerAdapter.notifyDataSetChanged();
            }

        }
    };

    private Runnable connectWithBluetoothAndRead = new Runnable() {
        @Override
        public void run() {
            try {
                //avoid when exception happen we can't re-connect
                bluetoothStateChange = true;
                bluetoothMessage = BTConfigFragment.btWaitForConnect;

                // 連結到該裝置
                mBluetoothSocket = mArduinoBluetooth.device.createRfcommSocketToServiceRecord(MY_UUID);
                //mBluetoothSocket = mArduinoBluetooth.device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                mBluetoothSocket.connect();

                Log.d(MainActivity.activityTag,"connection successes");

                mBufferedReader = new BufferedReader(new InputStreamReader(mBluetoothSocket.getInputStream()));

                String lineBuffer = null;

                while((lineBuffer = mBufferedReader.readLine())!=null) {
                    Log.d(MainActivity.activityTag,"data read:" + lineBuffer);
                    String []data = lineBuffer.split(" ");
                    int numDataValues = data.length;
                    if(numDataValues == DrinksInformation.NUM_DATA_VALUES) {
                        //put into predictor and get the result
                        //calculate the difference of weight
                        //retrieve from database
                        predictedLabel = mClassifier.predictInstance(Arrays.copyOfRange(data,0,DrinksInformation.NUM_FEATURE_VALUES));
                        previousLabel = predictedLabel;
                        //predictedLabel = 0; //simulate the predicted result
                        currentWeight = Float.valueOf(data[data.length - 1]);
                        if(previousLabel != predictedLabel) {
                            totalDeltaWeight = 0;
                        }
                        else {
                            float deltaWeight = previousWeight - currentWeight;
                            if(deltaWeight >= 0) {
                                totalDeltaWeight += deltaWeight;
                            }
                            else {
                                Log.d(activityTag,"warning:previous weight < current weight");
                            }
                        }

                        toVisualizeCurrentDrink = true;
                        fragmentClass = FragmentClass.CurrentDrink;
                        Intent intent = new Intent(notifyAdapterIntentFilterTag);
                        getApplicationContext().sendBroadcast(intent);


                    /*
                    //Log.d(BluetoothConst.appTag,"device output:" + line);
                    //Toast.makeText(BluetoothActivity.this,line,Toast.LENGTH_SHORT).show();
                    */
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
