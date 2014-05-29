package com.example.android.effectivenavigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by kantei on 2014/05/28.
 */
public class TestWizardOfOz extends Fragment {

    private String messageToShow = null;

    public static TestWizardOfOz newInstance(String message) {
        TestWizardOfOz fragment = new TestWizardOfOz();
        fragment.messageToShow = message;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_testwoo,container,false);
        TextView testText = (TextView)rootView.findViewById(R.id.testText);
        testText.setText(messageToShow);
        return rootView;

    }
}
