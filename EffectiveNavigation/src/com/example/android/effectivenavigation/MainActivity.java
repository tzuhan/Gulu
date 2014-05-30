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
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.UUID;

import bluetoothmodule.ArduinoBluetooth;
import bluetoothmodule.BluetoothConst;
import database.DrinkRecordDataSource;
import mlmodule.My1NN;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    //debugging tag
    public static final String activityTag = "MainActivity";
    //action tag
    public static final String startDiscoveringIntentFilterTag = MainActivity.class.getName() + ".startDiscovering";
    public static final String notifyAdapterIntentFilterTag = MainActivity.class.getName() + ".notifyAdapter";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 一定要是這組
    private static BluetoothAdapter mBluetoothAdapter = null; // 用來搜尋、管理藍芽裝置
    private static BluetoothSocket mBluetoothSocket = null; // 用來連結藍芽裝置、以及傳送指令
    public final int numOfTabs = 3;
    public Fragment[] tabFragments = new Fragment[numOfTabs];
    private CurrentDrinkFragment drinkFragment = null;
    public boolean[] changeCurrentFragmentFlag = new boolean[numOfTabs];
    public boolean[] refreshCurrentFragmentFlag = new boolean[numOfTabs];
    public final int firstTabIndex = 0;
    public final int secondTabIndex = 1;
    public final int thirdTabIndex = 2;
    private final String mBluetoothThreadName = "BluetoothThread";
    private final String mSocketThreadName = "WizardServant";
    public boolean timeToDetectDrink = false;
    private BufferedReader mBufferedReader;
    private HandlerThread mBTThread;
    private Handler mBTThreadHandler;
    private HandlerThread mWifiThread;
    private Handler mWifiThreadHandler;
    private HandlerThread mTempThread;
    private Handler mTempThreadHandler;
    private ArduinoBluetooth mArduinoBluetooth;
    private FragmentManager mFragmentManager;
    //private IncrementalClassifier mClassifier;
    private int predictedLabel;
    private int previousLabel;
    public boolean automaticMode = false;
    private Runnable communicateWithPhone = new Runnable() {
        private WizardSocket wizardSocket = new WizardSocket();
        private MainActivity mainActivity = MainActivity.this;
        private final int numOfDrinks = DrinksInformation.drinks_list.length;

        private boolean sendMessage(String message) {
            if (!wizardSocket.sendOtherMessage(message)) {
                return false;
            } else {
                Log.d(WizardSocket.debugTag, "send message succeed");
                return true;
            }
        }

        @Override
        public void run() {
            while (true) {
                Log.d(WizardSocket.debugTag, "call waitForConnect");
                wizardSocket.waitForConnect();
                if (!sendMessage("Hi!Wizard~")) {
                    break;
                }
                while (true) {
                    String message = wizardSocket.readData();
                    if (message == null) {
                        break;
                    } else {
                        Log.d(WizardSocket.debugTag, "get " + message);
                        char firstChar = message.charAt(0);
                        if (firstChar == 'd' || firstChar == 'v') { //try to send drink label
                            if (!mainActivity.timeToDetectDrink) {
                                if (!sendMessage("not yet pressed button")) {
                                    break;
                                }
                            }
                            else {
                                String numberPart = message.substring(1);
                                try {
                                    if(firstChar == 'd') {
                                        int label = Integer.parseInt(numberPart);
                                        if (label >= 0 && label < numOfDrinks) {
                                            mainActivity.drinkFragment.mToShowLabel = label;
                                            mainActivity.drinkFragment.mTotalVolume = 0;
                                            mainActivity.drinkFragment.mIsLoading = false;
                                            refreshCurrentFragment(firstTabIndex);
                                            //maybe refresh here
                                            if (!sendMessage("OK,updated")) {
                                                break;
                                            }
                                        } else {
                                            if (!sendMessage("Not in range")) {
                                                break;
                                            }
                                        }
                                    }
                                    else {
                                        int volume = Integer.parseInt(numberPart);
                                        if(volume > 0) {
                                            mainActivity.drinkFragment.mTotalVolume = volume;
                                            mainActivity.drinkFragment.mIsLoading = false;
                                            refreshCurrentFragment(firstTabIndex);
                                            //maybe refresh here
                                            if (!sendMessage("OK,updated")) {
                                                break;
                                            }
                                        } else {
                                            if (!sendMessage("Not in range")) {
                                                break;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.d(WizardSocket.debugTag, e.getLocalizedMessage());
                                    if (!sendMessage("format error?")) {
                                        break;
                                    }
                                }

                            }
                        } else if (message.compareTo("bt") == 0) { //get bt data from cup
                            if (!sendMessage("D:" + String.valueOf(mainActivity.predictedLabel) + ',' + ((int) (mainActivity.totalDeltaHeight * UltrasonicInfo.bottomArea)))) {
                                break;
                            }
                        } else if (message.compareTo("a") == 0) { //automatic
                            MainActivity.this.automaticMode = true;
                            if (!sendMessage("auto")) {
                                break;
                            }
                        } else if (message.compareTo("na") == 0) { //non-automatic
                            MainActivity.this.automaticMode = false;
                            if (!sendMessage("non-auto")) {
                                break;
                            }
                        } else {
                            //send message
                            if (!sendMessage("Not cmd")) {
                                break;
                            }
                        }
                    }

                }
            }
        }
    };
    private float totalDeltaHeight;
    private float previousHeight;
    private float currentHeight;

    public void callTempThreadForHelp(Runnable r) {
        mTempThreadHandler.removeCallbacks(r);
        mTempThreadHandler.post(r);
    }

    public Runnable toEnableBTAndStartToDiscover = new Runnable() {
        @Override
        public void run() {
            MainActivity.this.enableBTandStartToDiscover();
        }
    };

    private Runnable communicateWithBluetooth = new Runnable() {
        @Override
        public void run() {
            try {

                // 連結到該裝置
                mBluetoothSocket = mArduinoBluetooth.device.createRfcommSocketToServiceRecord(MY_UUID);
                mBluetoothSocket.connect();

                Log.d(MainActivity.activityTag, "connection successes");

                mBufferedReader = new BufferedReader(new InputStreamReader(mBluetoothSocket.getInputStream()));

                String lineBuffer = null;
                String[] featureData = new String[DrinksInformation.NUM_FEATURE_VALUES];

                while ((lineBuffer = mBufferedReader.readLine()) != null) {
                    //Log.d(MainActivity.activityTag,"data read:" + lineBuffer);
                    String[] data = lineBuffer.split(" ");
                    int numDataValues = data.length;
                    if (numDataValues == DrinksInformation.NUM_DATA_VALUES) {
                        //put into predictor and get the result
                        //calculate the difference of weight
                        //retrieve from database

                        for (int dim = 0; dim < DrinksInformation.NUM_FEATURE_VALUES; dim++) {
                            featureData[dim] = data[dim];
                        }
                        predictedLabel = mClassifier.predictInstance(featureData);
                        currentHeight = Float.valueOf(data[numDataValues - 1]);

                        if (((int) currentHeight) == -1) { //empty
                            if (previousHeight > 0 && previousLabel >= 0) {
                                totalDeltaHeight += (UltrasonicInfo.emptyHeight - previousHeight);
                                mSource.insertNewDrinkRecord(
                                        DrinksInformation.drinks_list[previousLabel],
                                        Calendar.getInstance(),
                                        totalDeltaHeight * UltrasonicInfo.bottomArea);
                            }
                            totalDeltaHeight = 0;
                            previousHeight = -1;
                        } else {
                            if (((int) previousHeight) == -1) {
                                previousHeight = currentHeight;
                            } else {
                                float deltaHeight = currentHeight - previousHeight;
                                if (deltaHeight >= 0) {
                                    totalDeltaHeight += deltaHeight;
                                    previousHeight = currentHeight;
                                } else {
                                    Log.d(activityTag, "noise:previous weight < current weight");
                                }
                            }
                        }

                        previousLabel = predictedLabel;

                        //notify controller to switch to result fragment
                        if (automaticMode) {
                            drinkFragment.mTotalVolume = totalDeltaHeight * UltrasonicInfo.bottomArea;
                            drinkFragment.mToShowLabel = predictedLabel;
                            drinkFragment.mIsLoading = false;
                            refreshCurrentFragment(firstTabIndex);
                            //changeCurrentFragment(firstTabIndex, CurrentDrinkFragment.newInstance(MainActivity.this, predictedLabel, totalDeltaHeight * UltrasonicInfo.bottomArea));
                        } else {
                            drinkFragment.mTotalVolume = totalDeltaHeight * UltrasonicInfo.bottomArea;
                            drinkFragment.mIsLoading = false;
                            refreshCurrentFragment(firstTabIndex);
                        }
                    } else {
                        Log.d(activityTag, "data re-read");
                    }
                }

            } catch (Exception e) {
                //Toast.makeText(BluetoothActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                Log.d(MainActivity.activityTag, e.getLocalizedMessage());
            }

            try {
                mBufferedReader.close();
            } catch (IOException e2) {

            }
        }
    };
    public BroadcastReceiver EventHandler = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
        @Override
        public void onReceive(Context context, Intent intent) {
            // 當收尋到裝置時
            String actionStr = intent.getAction();
            if (actionStr.equals(notifyAdapterIntentFilterTag)) {
                mAppSectionsPagerAdapter.notifyDataSetChanged();
            } else if (actionStr.equals(BluetoothDevice.ACTION_FOUND)) {
                // 取得藍芽裝置這個物件
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(activityTag, "find device:" + device.getAddress() + ",name:" + device.getName());
                // 判斷那個裝置是不是你要連結的裝置，根據藍芽裝置address判斷
                if (device.getAddress().equals(ArduinoBluetooth.address)) {
                    Log.d(activityTag, "find wanted devices");
                    //due to wanted device found
                    mBluetoothAdapter.cancelDiscovery();
                    mArduinoBluetooth.device = device;
                    //due to a IO operation, we need to do it asynchronously.That is,in another thread.
                    mBTThreadHandler.removeCallbacks(communicateWithBluetooth);
                    mBTThreadHandler.post(communicateWithBluetooth);
                }
            }
            else if (actionStr.equals(startDiscoveringIntentFilterTag)) {
                //to show start discovery fragment
                //changeCurrentFragment(firstTabIndex, BTConfigFragment.newInstance(BTConfigFragment.btDataLoading));
                mBluetoothAdapter.startDiscovery();
                Log.d(BluetoothConst.appTag, "bluetooth start discovering");
            } else {
                Log.d(activityTag, "unknown action in EventHandler");
            }
        }
    };
    private IntentFilter EventFilter;
    private My1NN mClassifier;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    //this activity implements ActionBar.TabListener
    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    private ViewPager mViewPager;
    //DB
    private DrinkRecordDataSource mSource;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        predictedLabel = -1;
        previousLabel = -1;

        currentHeight = -1;
        previousHeight = -1;
        totalDeltaHeight = 0;

        //mClassifier = IncrementalClassifier.getInstance(DrinksInformation.NUM_FEATURE_VALUES, DataConst.attNames, DrinksInformation.drinks_list);
        //mClassifier.loadModel();
        mClassifier = new My1NN();

        //initialize adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // 如果裝置不支援藍芽
            Log.d(MainActivity.activityTag, "Device doesn't support bluetooth");
            finish();
            return;
        }

        mArduinoBluetooth = new ArduinoBluetooth();

        //initialize default fragment
/*
        if (mBluetoothAdapter.isEnabled()) {
            tabFragments[firstTabIndex] = BTConfigFragment.newInstance(BTConfigFragment.btWaitForConnect);
        } else {
            tabFragments[firstTabIndex] = BTConfigFragment.newInstance(BTConfigFragment.btNotEnabled);
        }
*/
        drinkFragment = CurrentDrinkFragment.newInstance(this, -1, 0);
        tabFragments[firstTabIndex] = drinkFragment;
        tabFragments[secondTabIndex] = DayDrinkFragment.newInstance();
        tabFragments[thirdTabIndex] = GoalFeaturesListFragment.newInstance(this);

        for (int i = 0; i < numOfTabs; i++) {
            changeCurrentFragmentFlag[i] = false;
            refreshCurrentFragmentFlag[i] = false;
        }

        //initialize thread and start it
        //this thread responsible for IO event with bluetooth
        mBTThread = new HandlerThread(mBluetoothThreadName);
        mBTThread.start();
        mBTThreadHandler = new Handler(mBTThread.getLooper());

        mWifiThread = new HandlerThread(mSocketThreadName);
        mWifiThread.start();
        mWifiThreadHandler = new Handler(mWifiThread.getLooper());

        mTempThread = new HandlerThread("temp thread");
        mTempThread.start();
        mTempThreadHandler = new Handler(mTempThread.getLooper());

        /*
        *
        * tab bar
        * responsible for managing the tabs
        * tab clicked events ...
        *
        */

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        /*
        * ViewPager
        * responsible for making fragments swipable and sync the position of fragment with tabs' positions
        *
        * */

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mFragmentManager = getSupportFragmentManager();
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(mFragmentManager, this);

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
                            .setTabListener(this)
            );
        }

        //Select which event to listen in this Activity

        EventFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        EventFilter.addAction(startDiscoveringIntentFilterTag);
        EventFilter.addAction(notifyAdapterIntentFilterTag);

        //EventFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        //onResume will be run after on create so we register eventHandler there

        //Database initialization
        mSource = new DrinkRecordDataSource(this);
        try {
            mSource.openDB();
        } catch (Exception e) {
            Log.d(activityTag, Log.getStackTraceString(e));
        }

        //keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

    public void refreshCurrentFragment(int tabIndex) {
        refreshCurrentFragmentFlag[tabIndex] = true;
        Intent intent = new Intent(notifyAdapterIntentFilterTag);
        sendBroadcast(intent);
        return;
    }

    public void changeCurrentFragment(int tabIndex, Fragment intendedFragment) {
        changeCurrentFragmentFlag[tabIndex] = true;
        tabFragments[tabIndex] = intendedFragment;
        Intent intent = new Intent(notifyAdapterIntentFilterTag);
        sendBroadcast(intent);
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void enableBTandStartToDiscover() {
        if (mBluetoothAdapter != null) {
            if(!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            while (!mBluetoothAdapter.isEnabled());
            Intent intent = new Intent(startDiscoveringIntentFilterTag);
            sendBroadcast(intent);
        }
    }

    public void startServerSocket() {
        mWifiThreadHandler.removeCallbacks(communicateWithPhone);
        mWifiThreadHandler.post(communicateWithPhone);
    }

    public void disableBT() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
            drinkFragment.mToShowLabel = -1;
            refreshCurrentFragment(firstTabIndex);
            //changeCurrentFragment(firstTabIndex, BTConfigFragment.newInstance(BTConfigFragment.btNotEnabled));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.open_bluetooth) {
            enableBTandStartToDiscover();
        } else if (id == R.id.close_bluetooth) {
            disableBT();
        } else if (id == R.id.start_socket) {
            startServerSocket();
        } else {
            Log.d(BluetoothConst.appTag, "unknown menu items in BluetoothActivity");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(EventHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(EventHandler, EventFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBTThread != null) {
            mBTThread.quit();
        }
        if (mWifiThread != null) {
            mWifiThread.quit();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    //suitable for fixed number of fragments

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
            Log.d(MainActivity.activityTag, "instantiate position:" + position);
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);

            if (mainActivity.changeCurrentFragmentFlag[position]) { //this would renew an fragment
                mainActivity.changeCurrentFragmentFlag[position] = false;
                removeFragment((Fragment) object);
            } else if (mainActivity.refreshCurrentFragmentFlag[position]) { //this only recall onCreateView
                mainActivity.refreshCurrentFragmentFlag[position] = false;
            }

            Log.d(MainActivity.activityTag, "destroy position:" + position);
        }

        //if we want to trigger getItem,we need to call removeFragment in destroyItem
        private void removeFragment(Fragment fragment) {
            android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }

        @Override
        public Fragment getItem(int i) {
            Log.d(MainActivity.activityTag, "getItem for position:" + i);
            switch (i) {
                case 0:
                case 1:
                case 2:
                case 3:
                    return mainActivity.tabFragments[i];

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
        //if return POSITION_NONE then destroyItem would be called
        @Override
        public int getItemPosition(Object object) {
            Log.d(MainActivity.activityTag, "getItemPosition called");
            for (int i = 0; i < mainActivity.numOfTabs; i++) {
                if (mainActivity.changeCurrentFragmentFlag[i] || mainActivity.refreshCurrentFragmentFlag[i]) {
                    return POSITION_NONE;
                }
            }
            return POSITION_UNCHANGED;
        }

        @Override
        public int getCount() {
            return mainActivity.numOfTabs;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Current";
                case 1:
                    return "History";
                case 2:
                    return "Goal";
                case 3:
                    return "WizardTest";
                default:
                    return "";
            }
        }
    }

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
