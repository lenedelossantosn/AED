package aed17.aedproject.aedapplication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayInputStream;

public class SettingsActivity extends AppCompatActivity {
    DatabaseHelper aedDb;
    EditText etDefMeditation,etDefAttention;
    CheckBox cbEnabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity_main);
        aedDb = new DatabaseHelper(this);
        etDefMeditation = (EditText) findViewById(R.id.etDefMeditation);
        etDefAttention = (EditText) findViewById(R.id.etDefAttention);
        cbEnabled = (CheckBox) findViewById(R.id.cbEnabled);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_aed_app2);
        actionBar.setTitle("   Alert Setting");

        getDefaultSettings();
    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.back_save_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.activity_contact_exit) {
            String enabled = cbEnabled.isChecked() ? "1" : "0";
            aedDb.updateSettings(etDefMeditation.getText().toString(),etDefAttention.getText().toString(), enabled);
            Toast.makeText(SettingsActivity.this,"Settings Updated!", Toast.LENGTH_LONG).show();
        }
        else if (id == R.id.activity_contact_exit2) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    public void getDefaultSettings() {

        Cursor result = aedDb.getSettingsDefaultValue();
        if (result.getCount() != 0)
        {
            while (result.moveToNext()) {
                etDefMeditation.setText(result.getString(0));
                etDefAttention.setText(result.getString(1));
                String enabled = result.getString(2);
                if (enabled.equals("1")) {
                    cbEnabled.setChecked(true);
                }
                else {
                    cbEnabled.setChecked(false);
                }
            }
        }
    }
}
