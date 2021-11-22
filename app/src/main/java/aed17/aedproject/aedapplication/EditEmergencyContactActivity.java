package aed17.aedproject.aedapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class EditEmergencyContactActivity extends AppCompatActivity {
    DatabaseHelper aedDb;
    EditText lname,fname,mname,bdate,age,address,email,contactno;
    Spinner relationship;
    Button btnSave;
    String contactnoparam;
    ImageButton imageButton;
    private static int SELECT_IMAGE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_edit_emergency_contacts);

        aedDb = new DatabaseHelper(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_aed_app2);
        actionBar.setTitle("   Update Contact");

        Intent intent = getIntent();
        contactnoparam = intent.getStringExtra("global_ContactNo");

        lname = (EditText) findViewById(R.id.e_lname);
        fname = (EditText) findViewById(R.id.e_fname);
        mname = (EditText) findViewById(R.id.e_mname);
        bdate = (EditText) findViewById(R.id.e_bdate);
        age = (EditText) findViewById(R.id.e_age);
        address = (EditText) findViewById(R.id.e_address);
        email = (EditText) findViewById(R.id.e_email);
        relationship = (Spinner) findViewById(R.id.e_relationship);
        contactno = (EditText) findViewById(R.id.e_contactno);
        btnSave = (Button) findViewById(R.id.btne_SaveProfile);
        imageButton = (ImageButton) findViewById((R.id.e_imageButton));


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

                DatePickerDialog mDatePicker=new DatePickerDialog(EditEmergencyContactActivity.this, new DatePickerDialog.OnDateSetListener() {
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
        Spinner dynamicSpinner = (Spinner) findViewById(R.id.e_relationship);
        String[] items = new String[] { "Relationship", "Brother", "Mother", "Sister", "Brother", "Friend" };
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

        getEditContact();
        updateContact();
        chooseImage();
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
            // SAVE
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
            boolean isInserted = aedDb.updateContact(lname.getText().toString(),
                    fname.getText().toString(),
                    mname.getText().toString(),
                    bdate.getText().toString(),
                    age.getText().toString(),
                    address.getText().toString(),
                    email.getText().toString(),
                    relationship.getSelectedItem().toString(),
                    contactno.getText().toString(),
                    contactnoparam,
                    imageData);

            if (isInserted == true)
            {
                Toast.makeText(EditEmergencyContactActivity.this, "Contact Updated!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditEmergencyContactActivity.this,EmergencyContactsActivity.class));
                finish();
            }
            else
            {
                Toast.makeText(EditEmergencyContactActivity.this, "Contact Not Updated!", Toast.LENGTH_SHORT).show();
            }

        }
        else if (id == R.id.activity_contact_exit2) {
            // BACK
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

    private void getEditContact() {
        Cursor result = aedDb.getEditContact(this.contactnoparam);
        if (result.getCount() != 0)
        {
            while (result.moveToNext()) {
                lname.setText(result.getString(0));
                fname.setText(result.getString(1));
                mname.setText(result.getString(2));
                bdate.setText(result.getString(3));
                age.setText(result.getString(4));
                address.setText(result.getString(5));
                email.setText(result.getString(6));
                relationship.setSelection(((ArrayAdapter)relationship.getAdapter()).getPosition(result.getString(7)));
                contactno.setText(result.getString(8));
                // Convert imageData back to Original Image
                byte [] imageData = result.getBlob(9);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
                Bitmap originalImage = BitmapFactory.decodeStream(inputStream);
                imageButton.setImageBitmap(originalImage);
            }
        }
    }

    private void chooseImage() {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(EditEmergencyContactActivity.this, "", Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(this,"", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void updateContact() {
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
                        boolean isInserted = aedDb.updateContact(lname.getText().toString(),
                                fname.getText().toString(),
                                mname.getText().toString(),
                                bdate.getText().toString(),
                                age.getText().toString(),
                                address.getText().toString(),
                                email.getText().toString(),
                                relationship.getSelectedItem().toString(),
                                contactno.getText().toString(),
                                contactnoparam,
                                imageData);

                        if (isInserted == true)
                        {
                            Toast.makeText(EditEmergencyContactActivity.this, "Contact Updated!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EditEmergencyContactActivity.this,EmergencyContactsActivity.class));
                            finish();
                        }
                        else
                        {
                            Toast.makeText(EditEmergencyContactActivity.this, "Contact Not Updated!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

}
