package eu.menzerath.bpchat;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Diese Activity ist für die Einstellungen zuständig.
 * Das System kümmert sich um das Abspeichern / Laden der Einstellungen, wobei hier bestimmte Aktionen beim Druck auf eine Schaltfläche festgelegt wurden.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSettings();

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupSettings() {
        addPreferencesFromResource(R.xml.prefs);

        Preference aboutAppPref = getPreferenceScreen().findPreference("aboutApp");
        Preference aboutDevPref = getPreferenceScreen().findPreference("aboutDev");

        aboutAppPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO: Bei Veröffentlichung im PlayStore anpassen
                // Öffne die Seite dieser App in der PlayStore-App
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                return false;
            }
        });

        aboutDevPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Öffne http://menzerath.eu im Browser
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://menzerath.eu"));
                startActivity(i);
                return false;
            }
        });

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            aboutAppPref.setSummary(getString(R.string.about_version) + " " + pInfo.versionName);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}