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
import android.app.Activity;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import bluetoothmodule.BluetoothConst;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    private static BluetoothAdapter mBluetoothAdapter = null; // 用來搜尋、管理藍芽裝置
    private static BluetoothSocket mBluetoothSocket = null; // 用來連結藍芽裝置、以及傳送指令
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 一定要是這組
    private BufferedReader mBufferedReader;
    private final String mBluetoothThreadName = "bluetoothThread";
    private HandlerThread mBluetoothThread;
    private Handler mBluetoothHandler;

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
    private final int currentDrinkFragmentIndex = 0;
    private String bluetoothMessage = null;
    private boolean bluetoothStateChange;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothMessage = null;
        bluetoothStateChange = false;

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
        IntentFilter startDiscoveringBTDevices = new IntentFilter(startDiscoveringIntentFilterTag);
        registerReceiver(btEventHandler,startDiscoveringBTDevices);

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
            if(fragment instanceof WaitForBTDataFragment) {
                ((WaitForBTDataFragment)fragment).setMessageToShow(mainActivity.bluetoothMessage);
            }
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            Log.d(MainActivity.activityTag, "destroy position:" + position);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    Log.d(MainActivity.activityTag,"getItem new Fragment WaitForBTDataFragment");
                    if(mBluetoothAdapter != null) {
                        if(mBluetoothAdapter.isEnabled()){
                            return WaitForBTDataFragment.newInstance(WaitForBTDataFragment.btWaitForConnect);
                        }
                        else {
                            return WaitForBTDataFragment.newInstance(WaitForBTDataFragment.btNotEnabled);
                        }
                    }
                    else {
                        return WaitForBTDataFragment.newInstance(WaitForBTDataFragment.btNotAvailable);
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

            if(object instanceof WaitForBTDataFragment && mainActivity.bluetoothStateChange) {
                Log.d(MainActivity.activityTag,"getItemPosition change Fragment WaitForBTDataFragment");
                mainActivity.bluetoothStateChange = false;
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

        unregisterReceiver(btEventHandler);
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
                bluetoothStateChange = true;
                bluetoothMessage = WaitForBTDataFragment.btWaitForConnect;
                mAppSectionsPagerAdapter.notifyDataSetChanged();
                /*
                android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
                Fragment originalFragment = mFragmentManager.findFragmentByTag("android:switcher" + R.id.MainActivityPager + "");
                ft.detach();
                */
            }

        }
        else if(id == R.id.close_bluetooth) {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
                bluetoothStateChange = true;
                bluetoothMessage = WaitForBTDataFragment.btNotEnabled;
                mAppSectionsPagerAdapter.notifyDataSetChanged();
            }
        }
        else {
            Log.d(BluetoothConst.appTag, "unknown menu items in BluetoothActivity");
        }

        return super.onOptionsItemSelected(item);
    }


    public BroadcastReceiver btEventHandler = new BroadcastReceiver() {
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
            else if(intent.getAction().equals(startDiscoveringIntentFilterTag)) {
                IntentFilter discoverBTDevices = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                context.registerReceiver(btEventHandler, discoverBTDevices);
                mBluetoothAdapter.startDiscovery();
                Log.d(BluetoothConst.appTag, "start discovering");
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
                Toast.makeText(MainActivity.this,"IOException during connection with bottle:" + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * A fragment that launches other parts of the demo application.
     */
    /*
    public static class LaunchpadSectionFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_launchpad, container, false);

            // Demonstration of a collection-browsing activity.
            rootView.findViewById(R.id.demo_collection_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), CollectionDemoActivity.class);
                            startActivity(intent);
                        }
                    });

            // Demonstration of navigating to external activities.
            rootView.findViewById(R.id.demo_external_activity)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Create an intent that asks the user to pick a photo, but using
                            // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET, ensures that relaunching
                            // the application from the device home screen does not return
                            // to the external activity.
                            Intent externalActivityIntent = new Intent(Intent.ACTION_PICK);
                            externalActivityIntent.setType("image/*");
                            externalActivityIntent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            startActivity(externalActivityIntent);
                        }
                    });

            return rootView;
        }
    }
    */
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
