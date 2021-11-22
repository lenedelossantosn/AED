package aed17.aedproject.aedapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class AddEmergencyContactActivity extends AppCompatActivity {
    DatabaseHelper aedDb;
    EditText lname,fname,mname,bdate,age,address,email,contactno;
    Spinner relationship;
    Button btnSave;
    ImageButton imageButton;
    private static int SELECT_IMAGE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_emergency_contacts);

        aedDb = new DatabaseHelper(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_aed_app2);
        actionBar.setTitle("   Add New Contact");

        lname = (EditText) findViewById(R.id.elname);
        fname = (EditText) findViewById(R.id.efname);
        mname = (EditText) findViewById(R.id.emname);
        bdate = (EditText) findViewById(R.id.ebdate);
        age = (EditText) findViewById(R.id.eage);
        address = (EditText) findViewById(R.id.eaddress);
        email = (EditText) findViewById(R.id.eemail);
        contactno = (EditText) findViewById(R.id.econtactno);
        relationship = (Spinner) findViewById(R.id.erelationship);
        btnSave = (Button) findViewById(R.id.btneSaveProfile);
        imageButton = (ImageButton) findViewById(R.id.eimageButton);
        addEmergencyContact();
        chooseImage();

        contactno.setInputType(InputType.TYPE_CLASS_NUMBER);

        // CALENDAR PICKER
        bdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                Calendar mcurrentDate=Calendar.getInstance();
                final int mYear=mcurrentDate.get(Calendar.YEAR);
                final int mMonth=mcurrentDate.get(Calendar.MONTH);
                final int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(AddEmergencyContactActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        bdate.setText(selectedmonth+"/"+selectedday+"/"+selectedyear);
                        int currentAge = mYear - selectedyear;
                        age.setText(String.valueOf(currentAge));
                    }
                },mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Birth Date");
                mDatePicker.show();
            }
        });

        // SPINNER
        Spinner dynamicSpinner = (Spinner) findViewById(R.id.erelationship);
        String[] items = new String[] { "Relationship", "Father", "Mother", "Sister", "Brother", "Friend" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);



        dynamicSpinner.setAdapter(adapter);
        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.back_save_button2, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.activity_contact_exit) {
            //SAVE
            byte [] imageData = null;
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ((BitmapDrawable)imageButton.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG,50,outputStream);
                imageData = outputStream.toByteArray();
                outputStream.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            boolean isInserted = aedDb.addEmergencyContact(lname.getText().toString(),
                    fname.getText().toString(),
                    mname.getText().toString(),
                    bdate.getText().toString(),
                    age.getText().toString(),
                    address.getText().toString(),
                    email.getText().toString(),
                    contactno.getText().toString(),
                    relationship.getSelectedItem().toString(),
                    imageData);
            if (isInserted == true)
            {
                Toast.makeText(AddEmergencyContactActivity.this, "New Contact Added!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddEmergencyContactActivity.this,EmergencyContactsActivity.class));
                finish();
            }
            else
            {
                Toast.makeText(AddEmergencyContactActivity.this, "Error in Adding New Contact!", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.activity_contact_exit2) {
            startActivity(new Intent(this, EmergencyContactsActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, EmergencyContactsActivity.class));
        finish();
    }

    private void chooseImage() {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(AddEmergencyContactActivity.this, "", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE)
        {
            if (resultCode == UserProfileActivity.RESULT_OK)
            {
                if (data != null)
                {
                    try
                    {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        imageButton.setImageBitmap(Bitmap.createBitmap(bitmap,0,0,bitmap .getWidth(),bitmap .getHeight(), matrix, true));
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED)
            {
                //Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addEmergencyContact() {
        btnSave.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        byte [] imageData = null;
                        try {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            ((BitmapDrawable)imageButton.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG,50,outputStream);
                            imageData = outputStream.toByteArray();
                            outputStream.close();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        boolean isInserted = aedDb.addEmergencyContact(lname.getText().toString(),
                                fname.getText().toString(),
                                mname.getText().toString(),
                                bdate.getText().toString(),
                                age.getText().toString(),
                                address.getText().toString(),
                                email.getText().toString(),
                                contactno.getText().toString(),
                                relationship.getSelectedItem().toString(),
                                imageData);
                        if (isInserted == true)
                        {
                            Toast.makeText(AddEmergencyContactActivity.this, "New Contact Added!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddEmergencyContactActivity.this,EmergencyContactsActivity.class));
                            finish();
                        }
                        else
                        {
                            Toast.makeText(AddEmergencyContactActivity.this, "Error in Adding New Contact!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
}
