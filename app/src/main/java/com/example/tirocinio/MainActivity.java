package com.example.tirocinio;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;

import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.widget.TextView;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.os.Process;
import android.os.SystemClock;


public class MainActivity extends AppCompatActivity  {

    Handler handler = new Handler();
    final Runnable r= new Runnable(){
        public void run(){
            batteryMonitoring();
            memoryMonitoring();
            networkMonitoring();
            cpuMonitoring();
            handler.postDelayed(r, 10000);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int UPDATE_INTERVAL = 30;

        handler.post(r);
        /*
        batteryMonitoring();
        memoryMonitoring();
        networkMonitoring();
        cpuMonitoring();

        while(true){
            try{
                batteryMonitoring();
                memoryMonitoring();
                networkMonitoring();
                cpuMonitoring();
                Thread.sleep(UPDATE_INTERVAL*1000);
            } catch(InterruptedException e){ }
        }
        */


    }

    public void batteryMonitoring(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter); //Register a BroadcastReceiver to be run in the main activity thread

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float)scale;

        TextView battery_view = findViewById(R.id.battery);
        battery_view.setText( "Available battery: " + Float.toString(batteryPct) );
    }

    public void memoryMonitoring(){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long availableMemory = memoryInfo.availMem; //memoria RAM
        long totalMmemory = memoryInfo.totalMem;

        TextView memory_view = findViewById(R.id.memory);
        memory_view.setText("Available memory: " + Long.toString(availableMemory));
        memory_view.append("\nTotal memory: " + Long.toString(totalMmemory));
    }

    public void networkMonitoring(){
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        Network currentNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);

        TextView bandwidth_view = findViewById(R.id.net_bandwidth);
        bandwidth_view.setText("Downstream bandwidth for this network in Kbps: "+caps.getLinkDownstreamBandwidthKbps());
        bandwidth_view.append("\nUpstream bandwidth for this network in Kbps: "+caps.getLinkUpstreamBandwidthKbps());

        TextView signal_strength_view = findViewById(R.id.net_signal_strength);
        signal_strength_view.setText("Signal strength: "+ caps.getSignalStrength() ); //unità di misura?->consulta documentazione (Dal risultato suppongo siano dBm)

        TextView capabilities_view = findViewById(R.id.net_capabilities);
        capabilities_view.setText("The network is able to reach internet: "+caps.hasCapability(NET_CAPABILITY_INTERNET));
        capabilities_view.append("\nThe network is not congested: "+caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED));
        capabilities_view.append("\nThe network is not suspended: "+caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED));
    }

    public void cpuMonitoring(){

        TextView cpu_view = findViewById(R.id.cpu);
        cpu_view.setText("Primary CPU architecture: "+ Build.SUPPORTED_ABIS[0]);
        cpu_view.append("\nNumber of CPU cores: "+Runtime.getRuntime().availableProcessors());

        int NUM_CORES = Runtime.getRuntime().availableProcessors();

        long[] cpuUsageDiff = new long[NUM_CORES];
        long[] systemUsageDiff = new long[NUM_CORES];

        long[] cpuUsageDelta = new long[NUM_CORES];
        long[] systemUsageDelta = new long[NUM_CORES];

        long[] prevCpuTicks = new long[NUM_CORES];
        long[] prevSystemTicks = new long[NUM_CORES];


        for (int i = 0; i < NUM_CORES ; i++) {
            cpuUsageDelta[i] = Process.getElapsedCpuTime() - prevCpuTicks[i];
            systemUsageDelta[i] = SystemClock.elapsedRealtime() - prevSystemTicks[i];

            cpuUsageDiff[i] += cpuUsageDelta[i];
            systemUsageDiff[i] += systemUsageDelta[i];

            prevCpuTicks[i] = Process.getElapsedCpuTime();
            prevSystemTicks[i] = SystemClock.elapsedRealtime();

            //if (i != 0) {
                float cpuUsage = (float) cpuUsageDiff[i] / systemUsageDiff[i];
                cpu_view.append("\nCPU usage of CORE " + i + ": " + (cpuUsage*100) );
            //}
        }

    }


}

