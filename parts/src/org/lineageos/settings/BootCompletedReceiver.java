package org.lineageos.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.util.Log;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.utils.FileUtils;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final boolean DEBUG = false;
    private static final String TAG = "LegionParts";
    private static final String BYPASSCHARGING_ENABLE_KEY = "bypasscharging_enable";
    private static final String BYPASSCHARGING_NODE = "/sys/class/qcom-battery/batt_charge_bypass_en";
    private static final String CHARGINGLIMIT_ENABLE_KEY = "charginglimit_enable";
    private static final String CHARGINGLIMIT_NODE = "/sys/class/qcom-battery/batt_charge_health_en";
    private static final String TURBOCHARGING_ENABLE_KEY = "turbocharging_enable";
    private static final String TURBOCHARGING_NODE = "/sys/class/qcom-battery/batt_charge_accelerate_en";
    private static final String NODE_POLICY4 = "/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq";
    private static final String NODE_POLICY7 = "/sys/devices/system/cpu/cpu7/cpufreq/scaling_max_freq";

    private static final String OPTION_BALANCE_KEY = "option_balance_enable";
    private static final String OPTION_BEASTMODE_KEY = "option_beastmode_enable";
    private static final String OPTION_POWERSAVE_KEY = "option_powersave_enable";

    private static final int FREQUENCY_BALANCE = 1996800; // Balance mode frequency for both Policy 4 and 7
    private static final int FREQUENCY_POWERSAVE_POLICY4 = 1171200; // PowerSave mode frequency for Policy 4
    private static final int FREQUENCY_POWERSAVE_POLICY7 = 1286400; // PowerSave mode frequency for Policy 7
    private static final int FREQUENCY_BEASTMODE_POLICY4 = 2745600; // BeastMode mode frequency for Policy 4
    private static final int FREQUENCY_BEASTMODE_POLICY7 = 3187200; // BeastMode mode frequency for Policy 7

    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (DEBUG) Log.d(TAG, "Received boot completed intent");

        // Apply balance, power save, and beast mode settings
        applyBalancePowerSaveBeastModeSettings(sharedPrefs);

        // Apply other settings like bypass charging, charging limit, and turbo charging
        applyOtherSettings(sharedPrefs);
    }

    private void applyBalancePowerSaveBeastModeSettings(SharedPreferences sharedPrefs) {
        int selectedOption = getSelectedOption(sharedPrefs);
        switch (selectedOption) {
            case 0: // Balance mode
                updateFrequency(FREQUENCY_BALANCE, FREQUENCY_BALANCE);
                break;
            case 1: // Beast Mode
                updateFrequency(FREQUENCY_BEASTMODE_POLICY4, FREQUENCY_BEASTMODE_POLICY7);
                break;
            case 2: // Power Save mode
                updateFrequency(FREQUENCY_POWERSAVE_POLICY4, FREQUENCY_POWERSAVE_POLICY7);
                break;
        }
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

    private void updateFrequency(int freqPolicy4, int freqPolicy7) {
        try {
            FileUtils.writeLine(NODE_POLICY4, String.valueOf(freqPolicy4));
            FileUtils.writeLine(NODE_POLICY7, String.valueOf(freqPolicy7));
            Log.d(TAG, "CPU frequency updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to update CPU frequency: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyOtherSettings(SharedPreferences sharedPrefs) {
        boolean ByPassChargingEnabled = sharedPrefs.getBoolean(BYPASSCHARGING_ENABLE_KEY, false);
        FileUtils.writeLine(BYPASSCHARGING_NODE, ByPassChargingEnabled ? "1" : "0");

        boolean ChargingLimitEnabled = sharedPrefs.getBoolean(CHARGINGLIMIT_ENABLE_KEY, false);
        FileUtils.writeLine(CHARGINGLIMIT_NODE, ChargingLimitEnabled ? "1" : "0");

        boolean TurboChargingEnabled = sharedPrefs.getBoolean(TURBOCHARGING_ENABLE_KEY, false);
        FileUtils.writeLine(TURBOCHARGING_NODE, TurboChargingEnabled ? "1" : "0");
    }
}
