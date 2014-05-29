package com.example.android.effectivenavigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by kantei on 2014/05/28.
 */
public class TestWizardOfOz extends Fragment {

    public static TestWizardOfOz newInstance() {
        return new TestWizardOfOz();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_testwoo,container,false);

        return rootView;

    }
}
