package com.example.logmein;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private SeekBar slider;
    private EditText passwordInput;
    private Button continueButton;
    private ImageView circleScreenTime, circleVolume, circleWifi, circleProximity, circleLandscape, circleSlider, circlePassword, circlePower, circleSettings;

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private BatteryManager batteryManager;
    private long screenOnTimeStart;
    private AudioManager audioManager;
    private UsageStatsManager usageStatsManager;

    private boolean isProximityNear = false; // Track proximity state
    private BroadcastReceiver batteryReceiver;
    private BroadcastReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        slider = findViewById(R.id.slider);
        passwordInput = findViewById(R.id.passwordInput);
        continueButton = findViewById(R.id.continueButton);

        // Initialize circles for checks
        circleScreenTime = findViewById(R.id.circleScreenTime);
        circleVolume = findViewById(R.id.circleVolume);
        circleWifi = findViewById(R.id.circleWifi);
        circleProximity = findViewById(R.id.circleProximity);
        circleLandscape = findViewById(R.id.circleLandscape);
        circleSlider = findViewById(R.id.circleSlider);
        circlePassword = findViewById(R.id.circlePassword);
        circlePower = findViewById(R.id.circlePower);
        circleSettings = findViewById(R.id.circleSettings);

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

        // Track screen on time
        screenOnTimeStart = SystemClock.elapsedRealtime();

        // Initially disable the continue button
        continueButton.setEnabled(false);

        continueButton.setOnClickListener(v -> onContinueButtonClicked());
        // Register BroadcastReceivers for battery and wifi
        registerBatteryReceiver();
        registerWifiReceiver();
        timerHandler.post(timerRunnable);
    }

    // Timer Handler to check screen time, settings, and volume every second
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
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

    // 1. Check if the screen has been on for a minimum amount of time
    private boolean checkScreenOnTime() {
        long elapsedOnTime = SystemClock.elapsedRealtime() - screenOnTimeStart;
        return elapsedOnTime >= 10000; // Must be 10 seconds or more
    }

    // 2. Power check: Check if the device is connected to power (charging)
    private boolean checkPower() {
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL; // Charging or full battery
    }

    // 3. Slider condition: Slider needs to be at least at the battery level
    private boolean checkSliderCondition() {
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return slider.getProgress() >= batteryLevel;
    }

    // 4. Check volume level: Volume must be at least 80% of the maximum volume
    private boolean checkVolumeLevel() {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currentVolume >= (maxVolume * 0.8);
    }

    // 5. Password check: The password should be correct
    private boolean checkPassword() {
        String enteredPassword = passwordInput.getText().toString();
        return "hello".equals(enteredPassword);
    }

    // 6. WIFI check: Wifi should be connected to "itay's iPhone"
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

    // 7. Background check: Setting should be opened in the last 30 seconds
    private boolean checkBackgroundApps() {
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 30 * 1000;  // Check the last 30 seconds

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

    // 8. Landscape mode: Phone needs to be sideways
    private boolean checkLandscapeMode() {
        return this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    // 9. Proximity check: The proximity sensor should detect something close
    private boolean checkProximitySensor() {
        return isProximityNear; // Return the current proximity state tracked by the listener
    }

    // Check if all conditions are met
    private void validateConditions() {
        boolean conditionsMet = true;

        conditionsMet &= checkScreenOnTime(); // Check if screen has been on for 10+ seconds
        conditionsMet &= checkPower(); // Check if device connected to power
        conditionsMet &= checkSliderCondition(); // Check if slider is above battery level
        conditionsMet &= checkVolumeLevel(); // Check if volume is >= 80% of max volume
        conditionsMet &= checkPassword(); // Check if password is correct
        conditionsMet &= checkWifiConnection(); // Check if Wi-Fi is connected to the target network
        conditionsMet &= checkBackgroundApps(); // Check if settings app was opened in the last 30 seconds
        conditionsMet &= checkLandscapeMode(); // Check if in landscape mode
        conditionsMet &= checkProximitySensor(); // Check if proximity sensor detects near object

        // Update circles based on conditions
        updateCircle(circleScreenTime, checkScreenOnTime());
        updateCircle(circleVolume, checkVolumeLevel());
        updateCircle(circleWifi, checkWifiConnection());
        updateCircle(circleProximity, checkProximitySensor());
        updateCircle(circleLandscape, checkLandscapeMode());
        updateCircle(circleSlider, checkSliderCondition());
        updateCircle(circlePassword, checkPassword());
        updateCircle(circlePower, checkPower());
        updateCircle(circleSettings, checkBackgroundApps());

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
        Toast.makeText(this, "Conditions met! Proceeding...", Toast.LENGTH_SHORT).show();
        // Add code to handle what happens after pressing continue
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
        unregisterReceiver(batteryReceiver);
        unregisterReceiver(wifiReceiver);
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proximitySensor != null)
            sensorManager.registerListener(sensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        timerHandler.post(timerRunnable);
    }
}
