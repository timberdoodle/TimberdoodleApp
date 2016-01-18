package de.tudarmstadt.timberdoodle.ui.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import de.tudarmstadt.adtn.preferences.IAdtnPreferences;
import de.tudarmstadt.timberdoodle.IService;
import de.tudarmstadt.timberdoodle.LocationPrivacy;
import de.tudarmstadt.timberdoodle.R;

/**
 * Activity which represents the settings menu
 */
public class SettingsActivity extends TimberdoodleActivity {

    private interface IntegerSettingListener {
        int onGetSetting();

        void onSetSetting(int value);
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onTimberdoodleServiceReady(final IService service) {
        super.onTimberdoodleServiceReady(service);

        // Add Switch and AlertDialog
        final Switch privacySwitch = (Switch) findViewById(R.id.privacy_switch);
        privacySwitch.setChecked(LocationPrivacy.isEnabled(this));
        privacySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new AlertDialog.Builder(SettingsActivity.this)
                            .setMessage("This will set your phone into airplane-mode and enable Wifi afterwards. " +
                                    "This will suspend most of the device's signal transmitting functions. " +
                                    "You can\'t recieve any calls or messages while in airplane-mode. " +
                                    "Do you want to proceed?")
                            .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final ProgressDialog barProgressDialog;
                                    barProgressDialog = new ProgressDialog(SettingsActivity.this);
                                    barProgressDialog.setTitle("Please wait ...");
                                    barProgressDialog.setMessage("Setting up privacy mode ...");
                                    barProgressDialog.setIndeterminate(true);
                                    barProgressDialog.setCancelable(false);
                                    barProgressDialog.show();

                                    LocationPrivacy.enable(SettingsActivity.this, new Runnable() {
                                        @Override
                                        public void run() {
                                            barProgressDialog.dismiss();
                                            // Start networking on privacy mode
                                            service.getAdtnService().startNetworking();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    privacySwitch.toggle();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    privacySwitch.toggle();
                                }
                            })
                            .show();
                } else {
                    // Stop networking if privacy mode is disabled
                    service.getAdtnService().stopNetworking();
                    LocationPrivacy.disable(SettingsActivity.this);
                }
            }
        });

        // Set up expert mode setting number pickers

        final IAdtnPreferences netPrefs = service.getAdtnService().getPreferences();

        setUpNumberPicker(1, 9999999, R.id.interval_textView, R.id.edit_interval_button,
                R.string.settingsDialogTexts_0, new IntegerSettingListener() {
                    @Override
                    public int onGetSetting() {
                        return netPrefs.getSendingPoolSendInterval();
                    }

                    @Override
                    public void onSetSetting(int value) {
                        netPrefs.edit();
                        netPrefs.setSendingPoolSendInterval(value);
                        netPrefs.commit();
                    }
                });

        setUpNumberPicker(1, 9999999, R.id.factor_textView, R.id.edit_factor_button,
                R.string.settingsDialogTexts_1, new IntegerSettingListener() {
                    @Override
                    public int onGetSetting() {
                        return netPrefs.getSendingPoolRefillThreshold();
                    }

                    @Override
                    public void onSetSetting(int value) {
                        netPrefs.edit();
                        netPrefs.setSendingPoolRefillThreshold(value);
                        netPrefs.commit();
                    }
                });

        setUpNumberPicker(1, 9999999, R.id.batch_textView, R.id.edit_batch_button,
                R.string.settingsDialogTexts_2, new IntegerSettingListener() {
                    @Override
                    public int onGetSetting() {
                        return netPrefs.getSendingPoolBatchSize();
                    }

                    @Override
                    public void onSetSetting(int value) {
                        netPrefs.edit();
                        netPrefs.setSendingPoolBatchSize(value);
                        netPrefs.commit();
                    }
                });

        final ToggleButton toggleButton = (ToggleButton) findViewById(R.id.help_icons_toggle_button);
        toggleButton.setChecked(getService().getAdtnService().getPreferences().getShowHelpButtons());

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IAdtnPreferences preferences = getService().getAdtnService().getPreferences();
                boolean showHelpButtons = !preferences.getShowHelpButtons();
                toggleButton.setChecked(showHelpButtons);
                preferences.edit();
                preferences.setShowHelpButtons(showHelpButtons);
                preferences.commit();
            }
        });

        // Set up check box to enable/disable ad-hoc auto join
        ToggleButton autoJoinAdhoc = (ToggleButton) findViewById(R.id.auto_join_adhoc_network);
        autoJoinAdhoc.setChecked(netPrefs.getAutoJoinAdHocNetwork());
        autoJoinAdhoc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                netPrefs.edit();
                netPrefs.setAutoJoinAdHocNetwork(isChecked);
                netPrefs.commit();
            }
        });
    }

    private void setUpNumberPicker(int min, int max, @IdRes int textViewId, @IdRes int imageButtonId,
                                   @StringRes final int settingName,
                                   final IntegerSettingListener settingListener) {
        // Set up NumberPicker
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(min);
        numberPicker.setMaxValue(max);

        // Set up TextView
        final TextView textView = (TextView) findViewById(textViewId);
        textView.setText(Integer.toString(settingListener.onGetSetting()));

        findViewById(imageButtonId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(settingName)
                        .setView(numberPicker)
                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                textView.setText(Integer.toString(numberPicker.getValue()));
                                settingListener.onSetSetting(numberPicker.getValue());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        });
    }
}
