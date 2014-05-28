package com.example.android.effectivenavigation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by kantei on 2014/01/02.
 */
public class BTConfigFragment extends Fragment {

    public static final String stringDataTag = "stringData";
    public static final String btNotAvailable = "Bluetooth isn't available";
    public static final String btNotEnabled = "Bluetooth isn't enabled";
    public static final String btWaitForConnect = "Bluetooth Device isn't connected";
    public static final String btDataLoading = "Bluetooth data is being loaded";

    private static final String fragmentTag = BTConfigFragment.class.getName();

    private String messageToShow = null;

    public static BTConfigFragment newInstance (String message) {
        BTConfigFragment fragment = new BTConfigFragment();

        Bundle args = new Bundle();
        args.putString(stringDataTag,message);
        fragment.setArguments(args);

        //Log.d(fragmentTag,"New Fragment For WaitForBTData");
        return fragment;
    }

    public BTConfigFragment() {
        Log.d(fragmentTag,"New Fragment For WaitForBTData");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        messageToShow = args.getString(stringDataTag);
        //Log.d(fragmentTag,"onCreate");
    }

    public void setMessageToShow(String message) {
        if(message != null) {
            messageToShow = message;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(fragmentTag,"onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d(fragmentTag,"onPause");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wait_for_data,container,false);
        TextView messageText = ((TextView) rootView.findViewById(R.id.waitForDataMessage));
        messageText.setText(messageToShow);
        if(messageToShow.equals(btDataLoading)) {
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.btDataLoadingProgressBar);
            progressBar.setVisibility(View.VISIBLE);

        }
        else if(messageToShow.equals(btWaitForConnect)) {
            ((ProgressBar) rootView.findViewById(R.id.btDataLoadingProgressBar)).setVisibility(View.INVISIBLE); //let textView align the margin bottom
            final Button connectButton = ((Button) rootView.findViewById(R.id.btDeviceConnect));
            connectButton.setVisibility(View.VISIBLE);
            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    connectButton.setClickable(false);
                    Intent startDiscoveringBTDevices = new Intent(MainActivity.startDiscoveringIntentFilterTag);
                    getActivity().getApplicationContext().sendBroadcast(startDiscoveringBTDevices);
                    connectButton.setClickable(true);
                }
            });
        }
        else {
            ((ProgressBar) rootView.findViewById(R.id.btDataLoadingProgressBar)).setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            messageText.setLayoutParams(layoutParams);
        }
        //Log.d(fragmentTag,"create view");
        return rootView;
    }
}
