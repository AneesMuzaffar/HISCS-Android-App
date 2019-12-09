package com.alphamstudios.hiscs;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.alphamstudios.hiscs.ui.main.PriorityFragment;
import com.alphamstudios.hiscs.ui.main.ScheduleFragment;
import com.alphamstudios.hiscs.ui.main.SectionsPagerAdapter;
import com.alphamstudios.hiscs.ui.main.StatusFragment;
import com.alphamstudios.hiscs.ui.main.SwitchFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import android.os.Handler;

public class MainActivity extends AppCompatActivity implements HISCSInterface {

    String address = null;

    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    InputStream mmInputStream;

    HISCSData data;

    public int[] priority = {0, 1, 2, 3};
    public String[] scheduleStrings = {"", "", "", ""};

    public double maxAllowedCurrent = 4.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(PairDevice.EXTRA_ADDRESS); // address of the bluetooth device

        if (address == null) {
            // Start Pairing Activity
            startActivity(new Intent(this, PairDevice.class));
            finish();
        }

        data = new HISCSData();

        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Home & Inverter Supply Control System", Snackbar.LENGTH_LONG).show();
            }
        });

        if (!isBtConnected && address != null)
            new ConnectBT().execute();
    }

    // HISCSInterface Implementation
    public void setPriority(String p)
    {
        msg("P" + p);
        sendString("P" + p);
    }

    public void setSwitch(String s)
    {
        msg("S" + s);
        sendString("S" + s);
    }

    public void setStatus(String s)
    {
        msg("R" + s);
        sendString("R" + s);
    }

    public void recalculate()
    {
        msg("X");
        sendString("X");
    }

    public void addSchedule(String s)
    {
        msg("T" + s);
        sendString("T" + s);
    }

    public void refreshData()
    {
        notifyFragments(data);
    }

    public void cancelAllSchedules()
    {
        msg("Z");
        sendString("Z");
    }
    // Interface Implementation


    private static char[] loadIndex = {'A', 'B', 'C', 'D'};
    private void newDataReceived(String s)
    {
        msg(s);

        if (s.charAt(0) != 'A') {
            msg("Invalid data received (1): " + s);
            return;
        }

        String[] parts = s.split(";");
        if (parts.length < 5) {
            msg("Invalid data received (2): " + s);
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (parts[i].charAt(0) != loadIndex[i]) return;

            data.state[i] = parts[i].charAt(1)  == '1';
            data.loadCurrents[i] = 0;

            try {
                data.loadCurrents[i] = Double.parseDouble(parts[i].substring(2));
            } catch (Exception e) {
                Toast.makeText(this, "Error parsing current (" + parts[i].charAt(0) +
                        " : " + parts[i].substring(2) + "): " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        for (int i = 0; i < 4; i++) {
            data.scheduleState[i] = parts[4].charAt(i) == '1';
        }

        notifyFragments(data);
    }

    void notifyFragments(HISCSData data)
    {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f: fragments) {
            if (f instanceof StatusFragment) {
                ((StatusFragment) f).setHISCSData(data);
            }

            if (f instanceof SwitchFragment) {
                ((SwitchFragment) f).setHISCSData(data);
            }

            if (f instanceof PriorityFragment) {
                ((PriorityFragment) f).setHISCSData(data);
            }

            if (f instanceof ScheduleFragment) {
                ((ScheduleFragment) f).setHISCSData(data);
            }
        }
    }

    private void Disconnect()
    {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Error");
            }
        }
        startActivity(new Intent(this, PairDevice.class));
        finish();
    }

    private void sendString(String str)
    {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write(str.getBytes());
            } catch (IOException e) {
                msg("Error sending string: " + str);
            }
        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            newDataReceived(data);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice device = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (final IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
                try {
                    mmInputStream = btSocket.getInputStream();
                    beginListenForData();

                    recalculate();
                } catch (Exception e) {
                    msg(e.getMessage());
                }
            }
            progress.dismiss();
        }
    }
}