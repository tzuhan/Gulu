package bluetoothmodule;

import android.bluetooth.BluetoothDevice;

/**
 * Created by kantei on 2014/01/06.
 */
public class ArduinoBluetooth { //dont forget to fill it out
    //static final String address = "20:12:05:27:03:20"; //baud rate:57600
    public static final String address = "98:D3:31:20:0C:77"; //another bluetooth //baud rate:9600
    //static final String name = "Finger";
    //static final String password = "1234";

    public BluetoothDevice device;

    public ArduinoBluetooth() {
        device = null;
    }
}