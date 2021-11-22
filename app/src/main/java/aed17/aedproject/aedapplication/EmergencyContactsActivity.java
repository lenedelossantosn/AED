package aed17.aedproject.aedapplication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class EmergencyContactsActivity extends AppCompatActivity {
    DatabaseHelper aedDb;
    ArrayList<String> list = new ArrayList<String>();
    ListView lv;
    int ctr;
    public static String [] relationship = new String[99];
    public static String [] contactno = new String[99];
    public static Bitmap [] bitmap = new Bitmap[99];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_emergency_contacts);
        aedDb = new DatabaseHelper(this);
        ctr = 0;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_aed_app2);
        actionBar.setTitle("   Contacts");

        // Generate list
        getEmergencyContact();
        populateListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_contact, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.activity_contact_exit) {
            startActivity(new Intent(this, AddEmergencyContactActivity.class));
            finish();
        }
        else if (id == R.id.activity_contact_exit2) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void populateListView()
    {
        // Instantiate Custom Adapter
        MyCustomAdapter adapter = new MyCustomAdapter(this.contactno,this.relationship,list, this, this.bitmap);

        // Handle Listview and Assign Adapter
        ListView lView = (ListView)findViewById((R.id.my_listview));
        lView.setAdapter(adapter);
    }

    public void getEmergencyContact() {
        // Display Current Profile to EditText Fields
        Cursor result = aedDb.getEmergencyContacts();
        if (result.getCount() != 0)
        {
            while (result.moveToNext()) {
                list.add(result.getString(1));
                this.contactno[ctr] = result.getString(0);
                this.relationship[ctr] = result.getString(2);
                // Convert imageData back to Original Image
                byte [] imageData = result.getBlob(3);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
                Bitmap originalImage = BitmapFactory.decodeStream(inputStream);
                this.bitmap[ctr] = originalImage;
                ctr++;
            }
        }
    }

}
