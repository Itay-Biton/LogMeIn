package com.example.logmein;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    // UI Components
    private SeekBar slider;
    private Button continueButton;
    private ImageView circleVolume, circleWifi, circleProximity, circleSlider, circlePower, circleSettings, circleNotification;

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private BatteryManager batteryManager;
    private AudioManager audioManager;
    private UsageStatsManager usageStatsManager;

    private boolean isProximityNear = false;
    private BroadcastReceiver batteryReceiver;
    private BroadcastReceiver wifiReceiver;
    private BroadcastReceiver notificationReceiver;

    private boolean checkedNotificationAccess = false;
    private boolean checkedUsageStats = false;
    private boolean checkedLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        slider = findViewById(R.id.slider);
        continueButton = findViewById(R.id.continueButton);

        // Initialize circles for checks
        circleVolume = findViewById(R.id.circleVolume);
        circleWifi = findViewById(R.id.circleWifi);
        circleProximity = findViewById(R.id.circleProximity);
        circleSlider = findViewById(R.id.circleSlider);
        circlePower = findViewById(R.id.circlePower);
        circleSettings = findViewById(R.id.circleSettings);
        circleNotification = findViewById(R.id.circleNotification);

        // Initialize the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get the Proximity Sensor
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        // Register the Sensor Event Listener for proximity sensor
        if (proximitySensor != null)
            sensorManager.registerListener(sensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_UI);

        // Initialize the UsageStatsManager for app usage tracking
        usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

        // Set the slider listener
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                validateConditions();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Get the BatteryManager system service
        batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);

        // AudioManager for volume
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Initially disable the continue button
        continueButton.setEnabled(false);

        continueButton.setOnClickListener(v -> onContinueButtonClicked());
        // Register BroadcastReceivers for battery and wifi
        registerBatteryReceiver();
        registerWifiReceiver();
        registerNotificationsReceiver();
        timerHandler.post(timerRunnable);

        checkNotificationAccess();
        checkUsageStatsPermission();
        checkLocationPermission();
    }

    public void checkNotificationAccess() {
        if (!checkedNotificationAccess) {
            ComponentName cn = new ComponentName(this, NotificationListener.class);
            String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
            if (flat == null || !flat.contains(cn.flattenToString())) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            }
            checkedNotificationAccess = true;
        }
    }

    public void checkUsageStatsPermission() {
        if (!checkedUsageStats) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
            if (mode != AppOpsManager.MODE_ALLOWED) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
            checkedUsageStats = true;
        }
    }

    public void checkLocationPermission() {
        if (!checkedLocation) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
            checkedLocation = true;
        }
    }

    // Timer Handler to check settings, and volume every second
    private final Handler timerHandler = new Handler();
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            validateConditions();
            timerHandler.postDelayed(this, 1000);
        }
    };

    // Register receiver to listen for changes in power connections
    private void registerBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                validateConditions();
            }
        };
        // Register a single IntentFilter to listen for power connection and disconnection
        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        batteryFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(batteryReceiver, batteryFilter);
    }

    // Register receiver to listen for Wi-Fi connection changes
    private void registerWifiReceiver() {
        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                validateConditions();
            }
        };
        IntentFilter wifiFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiReceiver, wifiFilter);
    }

    // Register receiver to listen for Wi-Fi connection changes
    private void registerNotificationsReceiver() {
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                validateConditions();
            }
        };
        registerReceiver(notificationReceiver, new IntentFilter(NotificationListener.NOTIFICATION_POSTED), Context.RECEIVER_NOT_EXPORTED);
    }

    // 1. Power check: Check if the device is connected to power (charging)
    private boolean checkPower() {
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL; // Charging or full battery
    }

    // 2. Slider condition: Slider needs to be at least at the battery level
    private boolean checkSliderCondition() {
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return slider.getProgress() >= batteryLevel;
    }

    // 3. Check volume level: Volume must be at least 80% of the maximum volume
    private boolean checkVolumeLevel() {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currentVolume >= (maxVolume * 0.8);
    }

    // 4. WIFI check: Wifi should be connected to "itay's iPhone"
    private boolean checkWifiConnection() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String currentSSID = wifiInfo.getSSID();
            if (currentSSID != null && currentSSID.startsWith("\"") && currentSSID.endsWith("\""))
                currentSSID = currentSSID.substring(1, currentSSID.length() - 1);

            return "Ronen WiFi".equals(currentSSID);
        }
        return false;
    }

    // 5. Background check: Setting should be opened in the last 60 seconds
    private boolean checkBackgroundApps() {
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 60 * 1000;  // Check the last 60 seconds

        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, currentTime);
        if (stats != null) {
            for (UsageStats usageStats : stats) {
                if (usageStats.getPackageName().equals("com.android.settings") &&
                        usageStats.getLastTimeUsed() > startTime &&
                            usageStats.getTotalTimeInForeground() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    // 6. Notification check
    private boolean checkNotification() {
        if (NotificationListener.lastPackageName != null) {
            return NotificationListener.lastNotificationText.equalsIgnoreCase("Password");
        }
        return false;
    }

    // 7. Proximity check: The proximity sensor should detect something close
    private boolean checkProximitySensor() {
        return isProximityNear; // Return the current proximity state tracked by the listener
    }

    // Check if all conditions are met
    private void validateConditions() {
        boolean conditionsMet = true;

        conditionsMet &= checkPower(); // Check if device connected to power
        conditionsMet &= checkSliderCondition(); // Check if slider is above battery level
        conditionsMet &= checkVolumeLevel(); // Check if volume is >= 80% of max volume
        conditionsMet &= checkWifiConnection(); // Check if Wi-Fi is connected to the target network
        conditionsMet &= checkBackgroundApps(); // Check if settings app was opened in the last 60 seconds
        conditionsMet &= checkNotification(); // Check if you received a notification contains the word "password"
        conditionsMet &= checkProximitySensor(); // Check if proximity sensor detects near object

        // Update circles based on conditions
        updateCircle(circlePower, checkPower());
        updateCircle(circleSlider, checkSliderCondition());
        updateCircle(circleVolume, checkVolumeLevel());
        updateCircle(circleWifi, checkWifiConnection());
        updateCircle(circleSettings, checkBackgroundApps());
        updateCircle(circleNotification, checkNotification());
        updateCircle(circleProximity, checkProximitySensor());

        // Enable or disable the "Continue" button based on the conditions
        continueButton.setEnabled(conditionsMet);
    }

    // Update the circle color based on condition
    private void updateCircle(ImageView circle, boolean conditionMet) {
        if (conditionMet)
            circle.setImageResource(R.drawable.green_circle);
        else
            circle.setImageResource(R.drawable.red_circle);
    }

    // Sensor Event Listener for proximity sensor
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                float distance = event.values[0];
                isProximityNear = distance < proximitySensor.getMaximumRange(); // Update proximity state
                validateConditions(); // Re-check all conditions
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    // Handle the Continue button click
    public void onContinueButtonClicked() {
        Toast.makeText(this, "Conditions met!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
        unregisterReceiver(batteryReceiver);
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(notificationReceiver);
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (proximitySensor != null)
            sensorManager.registerListener(sensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        registerBatteryReceiver();
        registerWifiReceiver();
        registerNotificationsReceiver();
        timerHandler.post(timerRunnable);
    }
}
