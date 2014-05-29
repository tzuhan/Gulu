package com.example.android.effectivenavigation;
import android.util.Log;

import java.net.*;
import java.io.*;

/**
 * Created by kantei on 2014/05/28.
 */

public class WizardSocket {
    private int[] ports = {12345,6789,6890,7899,9000};
    private ServerSocket serverSocket;
    private Socket communicateSocket = null;

    private final static String debugTag = WizardSocket.class.getName();

    public WizardSocket() {

    }

    public void waitForConnect() {
        for(int port : ports) {
            try {
                serverSocket = new ServerSocket(port);
                serverSocket.setSoTimeout(0); //means waiting until client connecting
                communicateSocket = serverSocket.accept();

            }
            catch(IOException e) {
                Log.d(debugTag,e.getLocalizedMessage());
            }

        }
    }

    public void sendDrinkData() {

    }

    public void confirm() {

    }



}
