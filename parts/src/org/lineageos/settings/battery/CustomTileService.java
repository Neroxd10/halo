package org.lineageos.settings.battery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.preference.PreferenceManager;
import android.util.Log;
import org.lineageos.settings.utils.FileUtils;
    // LEGION ASSISTANT PORT TO AOSP BUILD BY NERO //
public class CustomTileService extends TileService {
    // GOVERNOR CPU //
    private static final String Governor1 = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    private static final String Governor4 = "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor";
    private static final String Governor7 = "/sys/devices/system/cpu/cpu7/cpufreq/scaling_governor";
    // MIN & MAX GPU //
    private static final String MIN_GPU = "/sys/class/kgsl/kgsl-3d0/min_clock_mhz";
    private static final String MAX_GPU = "/sys/class/kgsl/kgsl-3d0/max_clock_mhz";
    // GPU POWER LEVEL //
    private static final String MAX_PWR = "/sys/class/kgsl/kgsl-3d0/max_pwrlevel";
    private static final String MIN_PWR = "/sys/class/kgsl/kgsl-3d0/min_pwrlevel";
    // MIN CPU //
    private static final String MIN_POLICY0 = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"; 
    private static final String MIN_POLICY4 = "/sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq";
    private static final String MIN_POLICY7 = "/sys/devices/system/cpu/cpu7/cpufreq/scaling_min_freq";
    // MAX CPU // 
    private static final String MAX_POLICY0 = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    private static final String MAX_POLICY4 = "/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq";
    private static final String MAX_POLICY7 = "/sys/devices/system/cpu/cpu7/cpufreq/scaling_max_freq";
    // MODE //
    private static final String OPTION_POWERSAVE_KEY = "option_powersave_enable";
    private static final String OPTION_DEFAULT_KEY = "option_default_enable";
    private static final String OPTION_BALANCE_KEY = "option_balance_enable";
    private static final String OPTION_BEASTMODE_KEY = "option_beastmode_enable";
    private static final String OPTION_BEASTMAX_KEY = "option_beastmax_enable";

    private static final String TAG = "CustomTileService";

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateUI();
    }

    @Override
    public void onClick() {
        super.onClick();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int selectedOption = getSelectedOption(sharedPrefs);

        selectedOption = (selectedOption + 1) % 5;

        updatePreferencesAndFrequency(selectedOption);
        updateUI();
    }

    private int getSelectedOption(SharedPreferences sharedPrefs) {
        if (sharedPrefs.getBoolean(OPTION_BALANCE_KEY, false)) {
            return 0;
        } else if (sharedPrefs.getBoolean(OPTION_BEASTMODE_KEY, false)) {
            return 1;
        } else if (sharedPrefs.getBoolean(OPTION_BEASTMAX_KEY, false)) {
            return 2;
        } else if (sharedPrefs.getBoolean(OPTION_POWERSAVE_KEY, false)) {
            return 3;
        } else if (sharedPrefs.getBoolean(OPTION_DEFAULT_KEY, false)) {
            return 4;
        } else {
            return 0;
        }
    }

    private void updatePreferencesAndFrequency(int selectedOption) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        switch (selectedOption) {
            case 0:
                // BALANCE MODE ACTIVATED //
                editor.putBoolean(OPTION_BALANCE_KEY, true);
                editor.putBoolean(OPTION_BEASTMODE_KEY, false);
                editor.putBoolean(OPTION_BEASTMAX_KEY, false);
                editor.putBoolean(OPTION_POWERSAVE_KEY, false);
                editor.putBoolean(OPTION_DEFAULT_KEY, false);
                updateMAXCPU(2016000, 1996800, 1996800);
                updateMINCPU(300000, 633600, 787200);
                Gpu(364, 765, 4, 14);
                Governor("walt", "walt", "walt");
                break;
            case 1:
                // BEAST MODE ACTIVATED //
                editor.putBoolean(OPTION_BALANCE_KEY, false);
                editor.putBoolean(OPTION_BEASTMODE_KEY, true);
                editor.putBoolean(OPTION_BEASTMAX_KEY, false);
                editor.putBoolean(OPTION_POWERSAVE_KEY, false);
                editor.putBoolean(OPTION_DEFAULT_KEY, false);
                updateMAXCPU(2016000,2745600, 3187200);
                updateMINCPU(300000, 633600, 787200);
                Gpu(100, 912, 0, 14);
                Governor("walt", "walt", "walt");
                break;
            case 2:
                // BEAST MAX ACTIVATED //
                editor.putBoolean(OPTION_BALANCE_KEY, false);
                editor.putBoolean(OPTION_BEASTMODE_KEY, false);
                editor.putBoolean(OPTION_BEASTMAX_KEY, true);
                editor.putBoolean(OPTION_POWERSAVE_KEY, false);
                editor.putBoolean(OPTION_DEFAULT_KEY, false);
                updateMAXCPU(2016000, 2745600, 3187200);
                updateMINCPU(2016000, 2745600, 3187200);
                Gpu(912, 912, 0, 0);
                Governor("performance", "performance", "performance");
                break;
            case 3:
                // POWERSAVE MODE ACTIVATED //
                editor.putBoolean(OPTION_BALANCE_KEY, false);
                editor.putBoolean(OPTION_BEASTMODE_KEY, false);
                editor.putBoolean(OPTION_BEASTMAX_KEY, false);
                editor.putBoolean(OPTION_POWERSAVE_KEY, true);
                editor.putBoolean(OPTION_DEFAULT_KEY, false);
                updateMAXCPU(1228800,1113600, 787200);
                updateMINCPU(300000, 633600, 787200);
                Gpu(100, 220, 13, 14);
                Governor("schedutil", "schedutil", "powersave");
                break;
            case 4:
                // DEFAULT MODE ACTIVATED //
                editor.putBoolean(OPTION_BALANCE_KEY, false);
                editor.putBoolean(OPTION_BEASTMODE_KEY, false);
                editor.putBoolean(OPTION_BEASTMAX_KEY, false);
                editor.putBoolean(OPTION_POWERSAVE_KEY, false);
                editor.putBoolean(OPTION_DEFAULT_KEY, true);
                updateMAXCPU(2016000, 1209600, 1036800);
                updateMINCPU(300000, 633600, 787200);
                Gpu(100,324, 11, 14);
                Governor("schedutil", "schedutil", "schedutil");
                break;
        }
        editor.apply(); // Apply changes to SharedPreferences
    }

    private void updateMAXCPU(int maxSmall, int maxBig, int maxPrime) {
        try {
            FileUtils.writeLine(MAX_POLICY0, String.valueOf(maxSmall));
            FileUtils.writeLine(MAX_POLICY4, String.valueOf(maxBig));
            FileUtils.writeLine(MAX_POLICY7, String.valueOf(maxPrime));
            Log.d(TAG, "CPU maximum frequency updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to update CPU frequency: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateMINCPU(int minSmall, int minBig, int minPrime) {
        try {
            FileUtils.writeLine(MIN_POLICY0, String.valueOf(minSmall));
            FileUtils.writeLine(MIN_POLICY4, String.valueOf(minBig));
            FileUtils.writeLine(MIN_POLICY7, String.valueOf(minPrime));
            Log.d(TAG, "CPU minimum frequency updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to update CPU frequency: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Gpu(int minGPU, int maxGPU, int maxpwrlevel, int minpwrlevel) {
        try {
            FileUtils.writeLine(MIN_GPU, String.valueOf(minGPU));
            FileUtils.writeLine(MAX_GPU, String.valueOf(maxGPU));
            FileUtils.writeLine(MAX_PWR, String.valueOf(maxpwrlevel));
            FileUtils.writeLine(MIN_PWR, String.valueOf(minpwrlevel));
            Log.d(TAG, "GPU frequency updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to update GPU frequency: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Governor(String CpuGovernor1, String CpuGovernor4, String CpuGovernor7) {
        try {
            FileUtils.writeLine(Governor1, CpuGovernor1);
            FileUtils.writeLine(Governor4, CpuGovernor4);
            FileUtils.writeLine(Governor7, CpuGovernor7);
            Log.d(TAG, "CPU Governor updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to update CPU Governor: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Update UI logic goes here

    private void updateUI() {
        Tile tile = getQsTile();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isBalanceEnabled = sharedPrefs.getBoolean(OPTION_BALANCE_KEY, false);
        boolean isBeastModeEnabled = sharedPrefs.getBoolean(OPTION_BEASTMODE_KEY, false);
        boolean isBeastMaxEnabled = sharedPrefs.getBoolean(OPTION_BEASTMAX_KEY, false);
        boolean isPowerSaveEnabled = sharedPrefs.getBoolean(OPTION_POWERSAVE_KEY, false);
        boolean isDefaultEnabled = sharedPrefs.getBoolean(OPTION_DEFAULT_KEY, false);

    if (isBalanceEnabled) {
        tile.setLabel("Balance");
        tile.setState(Tile.STATE_ACTIVE);
    } else if (isBeastModeEnabled) {
        tile.setLabel("Beast Mode");
        tile.setState(Tile.STATE_ACTIVE);
    } else if (isBeastMaxEnabled) {
        tile.setLabel("Beast Max");
        tile.setState(Tile.STATE_ACTIVE);
    } else if (isPowerSaveEnabled) {
        tile.setLabel("Power Save");
        tile.setState(Tile.STATE_ACTIVE);
    } else if (isDefaultEnabled) {
        tile.setLabel("Default");
        tile.setState(Tile.STATE_ACTIVE);
    } else {
        tile.setLabel("Balance");
        tile.setState(Tile.STATE_ACTIVE);
    }
    tile.updateTile(); // Update the tile to reflect changes
}
}
