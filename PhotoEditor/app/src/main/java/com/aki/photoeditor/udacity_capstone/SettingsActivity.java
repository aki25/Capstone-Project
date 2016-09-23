package com.aki.photoeditor.udacity_capstone;

import android.os.Bundle;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class SettingsActivity extends AppCompatPreferenceActivity  {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);
        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onOptionsItemSelected(item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
