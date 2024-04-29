package org.lineageos.settings.battery;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.preference.PreferenceManager;
import android.util.Log;
import org.lineageos.settings.utils.FileUtils;

public class CustomTileService extends TileService {

    private static final String MIN_GPU = "/sys/class/kgsl/kgsl-3d0/min_clock_mhz";
    private static final String MAX_GPU = "/sys/class/kgsl/kgsl-3d0/max_clock_mhz";

    private static final int P_MIN_GPU = 100;
    private static final int PB_GPU = 439;
    private static final int BAL_MAX_GPU = 765;
    private static final int BEAST_GPU = 912;

    private static final String OPTION_BALANCE_KEY = "option_balance_enable";
    private static final String OPTION_BEASTMODE_KEY = "option_beastmode_enable";
    private static final String OPTION_POWERSAVE_KEY = "option_powersave_enable";

    private static final String NODE_MIN_POLICY0 = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"; 
    private static final String NODE_MIN_POLICY4 = "/sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq";
    private static final String NODE_MIN_POLICY7 = "/sys/devices/system/cpu/cpu7/cpufreq/scaling_min_freq";
   
    private static final String NODE_POLICY0 = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    private static final String NODE_POLICY4 = "/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq";
    private static final String NODE_POLICY7 = "/sys/devices/system/cpu/cpu7/cpufreq/scaling_max_freq";

    private static final int MIN_CPU0 = 300000;
    private static final int MIN_CPU4 = 633600;
    private static final int MIN_CPU7 = 787200;

    private static final int FREQUENCY_MAX_BEASTMODE_POLICY0 = 2016000;
    private static final int FREQUENCY_MAX_BEASTMODE_POLICY4 = 2745600;
    private static final int FREQUENCY_MAX_BEASTMODE_POLICY7 = 3187200;

    private static final int FREQUENCY_BALANCE = 1996800; // Balance mode frequency for both Policy 4 and 7
    private static final int FREQUENCY_POWERSAVE_POLICY4 = 1171200; // PowerSave mode frequency for Policy 4
    private static final int FREQUENCY_POWERSAVE_POLICY7 = 1286400; // PowerSave mode frequency for Policy 7
    private static final int FREQUENCY_BEASTMODE_POLICY4 = 2745600; // BeastMode mode frequency for Policy 4
    private static final int FREQUENCY_BEASTMODE_POLICY7 = 3187200; // BeastMode mode frequency for Policy 7

    private static final String TAG = "CustomTileService";

    @Override
    public void onStartListening() {
        super.onStartListening();
        // No arguments are passed to onStartListening()
        updateUI();
    }

    @Override
    public void onClick() {
        super.onClick();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int selectedOption = getSelectedOption(sharedPrefs);

        selectedOption = (selectedOption + 1) % 3; // Cycle through options

        updatePreferencesAndFrequency(selectedOption);

        // Update UI (implement your UI update logic here)
        // You can use Tile APIs like getTileList() or getStatusBarIcon()
    }

    private int getSelectedOption(SharedPreferences sharedPrefs) {
        if (sharedPrefs.getBoolean(OPTION_BALANCE_KEY, false)) {
            return 0;
        } else if (sharedPrefs.getBoolean(OPTION_BEASTMODE_KEY, false)) {
            return 1;
        } else if (sharedPrefs.getBoolean(OPTION_POWERSAVE_KEY, false)) {
            return 2;
        } else {
            // Default to Balance if none are selected
            return 0;
        }
    }

    private void updatePreferencesAndFrequency(int selectedOption) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        switch (selectedOption) {
            case 0:
                editor.putBoolean(OPTION_BALANCE_KEY, true);
                editor.putBoolean(OPTION_BEASTMODE_KEY, false);
                editor.putBoolean(OPTION_POWERSAVE_KEY, false);
                updateFrequency(FREQUENCY_BALANCE, FREQUENCY_BALANCE);
                MinCPU(MIN_CPU0, MIN_CPU4, MIN_CPU7);
                Gpu(PB_GPU, BAL_MAX_GPU);
                break;
            case 1:
                editor.putBoolean(OPTION_BALANCE_KEY, false);
                editor.putBoolean(OPTION_BEASTMODE_KEY, true);
                editor.putBoolean(OPTION_POWERSAVE_KEY, false);
                updateFrequency(FREQUENCY_BEASTMODE_POLICY4, FREQUENCY_BEASTMODE_POLICY7);
                updateFrequency1(FREQUENCY_MAX_BEASTMODE_POLICY0, FREQUENCY_MAX_BEASTMODE_POLICY4, FREQUENCY_MAX_BEASTMODE_POLICY7);
                Gpu(BEAST_GPU, BEAST_GPU);
                break;
            case 2:
                editor.putBoolean(OPTION_BALANCE_KEY, false);
                editor.putBoolean(OPTION_BEASTMODE_KEY, false);
                editor.putBoolean(OPTION_POWERSAVE_KEY, true);
                updateFrequency(FREQUENCY_POWERSAVE_POLICY4, FREQUENCY_POWERSAVE_POLICY7);
                MinCPU(MIN_CPU0, MIN_CPU4, MIN_CPU7);
                Gpu(P_MIN_GPU, PB_GPU);
                break;
        }
        editor.apply(); // Apply changes to SharedPreferences
    }

    private void updateFrequency(int freqPolicy4, int freqPolicy7) {
        try {
            FileUtils.writeLine(NODE_POLICY4, String.valueOf(freqPolicy4));
            FileUtils.writeLine(NODE_POLICY7, String.valueOf(freqPolicy7));
            Log.d(TAG, "CPU maximum frequency updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to update CPU frequency: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateFrequency1(int minPolicy0, int minPolicy4, int minPolicy7) {
        try {
            FileUtils.writeLine(NODE_MIN_POLICY0, String.valueOf(minPolicy0));
            FileUtils.writeLine(NODE_MIN_POLICY4, String.valueOf(minPolicy4));
            FileUtils.writeLine(NODE_MIN_POLICY7, String.valueOf(minPolicy7));
            Log.d(TAG, "BEAST MODE ACTIVATED");
        } catch (Exception e) {
            Log.e(TAG, "Failed to BEAST MODE: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void MinCPU(int cpu01, int cpu04, int cpu07) {
        try {
            FileUtils.writeLine(NODE_MIN_POLICY0, String.valueOf(cpu01));
            FileUtils.writeLine(NODE_MIN_POLICY4, String.valueOf(cpu04));
            FileUtils.writeLine(NODE_MIN_POLICY7, String.valueOf(cpu07));
            Log.d(TAG, "CPU minimum frequency updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to update CPU frequency: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Gpu(int minGPU, int maxGPU) {
        try {
            FileUtils.writeLine(MIN_GPU, String.valueOf(minGPU));
            FileUtils.writeLine(MAX_GPU, String.valueOf(maxGPU));
            Log.d(TAG, "CPU minimum frequency updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to update CPU frequency: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update UI logic goes here
    // Update UI logic goes here
// Update UI logic goes here
private void updateUI() {
    Tile tile = getQsTile();
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    boolean isBalanceEnabled = sharedPrefs.getBoolean(OPTION_BALANCE_KEY, false);
    boolean isBeastModeEnabled = sharedPrefs.getBoolean(OPTION_BEASTMODE_KEY, false);
    boolean isPowerSaveEnabled = sharedPrefs.getBoolean(OPTION_POWERSAVE_KEY, false);

    if (isBalanceEnabled) {
        tile.setLabel("Balance");
    } else if (isBeastModeEnabled) {
        tile.setLabel("Beast Mode");
    } else if (isPowerSaveEnabled) {
        tile.setLabel("Power Save");
    } else {
        // Default to Balance if none are selected
        tile.setLabel("Balance");
    }
    tile.updateTile(); // Update the tile to reflect changes
}
}
