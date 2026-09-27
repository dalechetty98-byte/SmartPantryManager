package za.co.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS = "smart_pantry_settings";
    private static final String EXPIRY_ALERTS = "expiry_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch alerts = findViewById(R.id.switchExpiryAlerts);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        alerts.setChecked(prefs.getBoolean(EXPIRY_ALERTS, true));

        alerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(EXPIRY_ALERTS, isChecked).apply());
    }
}
