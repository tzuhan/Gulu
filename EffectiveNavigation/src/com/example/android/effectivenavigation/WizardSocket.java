package com.example.android.effectivenavigation;
import android.util.Log;

import java.net.*;
import java.io.*;

/**
 * Created by kantei on 2014/05/28.
 */

public class WizardSocket {
    private int[] ports = {49152,50000,54321,54213,49999};
    private ServerSocket serverSocket;
    private Socket communicateSocket = null;
    private BufferedReader in;
    private BufferedWriter out;
    //private byte[] buffer = new byte[1024];

    public final static String debugTag = WizardSocket.class.getName();

    public WizardSocket() {

    }

    public void waitForConnect() {
        while(true) {
            for (int port : ports) {
                try {
                    Log.d(debugTag,"start to listen on port:" + port);
                    serverSocket = new ServerSocket(port);
                    //serverSocket.setSoTimeout(0); //means waiting until client connecting
                    communicateSocket = serverSocket.accept();
                    communicateSocket.setSoTimeout(0);

                    serverSocket.close();
                    in = new BufferedReader(new InputStreamReader(communicateSocket.getInputStream()));
                    out = new BufferedWriter(new OutputStreamWriter(communicateSocket.getOutputStream()));
                    Log.d(debugTag,"get client from port:" + port + ",address:" + communicateSocket.getInetAddress());
                    return;
                }
                catch (Exception e) {
                    Log.d(debugTag, e.getLocalizedMessage());
                }
            }
        }
    }

    public boolean sendDrinkType(int drinkIndex) {

        try {
            out.write("Data:" + drinkIndex);
            out.flush();
            return true;
        } catch (Exception e) {
            Log.d(debugTag, e.getLocalizedMessage());
            try{
                communicateSocket.close();
            }
            catch(Exception e2) {
                Log.d(debugTag,e2.getLocalizedMessage());
            }
            return false;
        }

    }

    public boolean sendOtherMessage(String message) {

        try {
            out.write(message);
            out.flush();
            return true;
        } catch (Exception e) {
            Log.d(debugTag, e.getLocalizedMessage());
            try{
                communicateSocket.close();
            }
            catch(Exception e2) {
                Log.d(debugTag,e2.getLocalizedMessage());
            }
            return false;
        }

    }

    public String readData() {

        try {
            return in.readLine();
        } catch (Exception e) {
            Log.d(debugTag, e.getLocalizedMessage());
            try{
                communicateSocket.close();
            }
            catch(Exception e2) {
                Log.d(debugTag,e2.getLocalizedMessage());
            }
            return null;
        }

    }


}
