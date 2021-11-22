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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class UserProfileActivity extends AppCompatActivity {
    private static int SELECT_IMAGE;
    DatabaseHelper aedDb;
    EditText lname,fname,mname,bdate,age,address,email;
    Button btnSave;
    ImageButton imageButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_user_profile);

        aedDb = new DatabaseHelper(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_aed_app2);
        actionBar.setTitle("   Profile");

        lname = (EditText) findViewById(R.id.lname);
        fname = (EditText) findViewById(R.id.fname);
        mname = (EditText) findViewById(R.id.mname);
        bdate = (EditText) findViewById(R.id.bdate);
        age = (EditText) findViewById(R.id.age);
        address = (EditText) findViewById(R.id.address);
        email = (EditText) findViewById(R.id.email);
        btnSave = (Button) findViewById(R.id.btnSaveProfile);
        imageButton = (ImageButton) findViewById(R.id.imageButton);

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

                DatePickerDialog mDatePicker=new DatePickerDialog(UserProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
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

        updateProfile();
        getUserProfile();
        chooseImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.back_save_button, menu);
        return true;
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
            boolean isInserted = aedDb.updateProfile(lname.getText().toString(),
                    fname.getText().toString(),
                    mname.getText().toString(),
                    bdate.getText().toString(),
                    age.getText().toString(),
                    address.getText().toString(),
                    email.getText().toString(),
                    imageData);

            if (isInserted == true)
            {
                Toast.makeText(UserProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(UserProfileActivity.this, "Profile Not Updated!", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.activity_contact_exit2) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

//@Override
//    public void onBackPressed() {
//        startActivity(new Intent(UserProfileActivity.this,SettingsActivity.class));
//        finish();
//    }

    private void chooseImage() {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(UserProfileActivity.this, "", Toast.LENGTH_SHORT).show();
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

    public void updateProfile() {

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
                        boolean isInserted = aedDb.updateProfile(lname.getText().toString(),
                                            fname.getText().toString(),
                                            mname.getText().toString(),
                                            bdate.getText().toString(),
                                            age.getText().toString(),
                                            address.getText().toString(),
                                            email.getText().toString(),
                                            imageData);

                        if (isInserted == true)
                        {
                            Toast.makeText(UserProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(UserProfileActivity.this, "Profile Not Updated!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    public void getUserProfile() {

        Cursor result = aedDb.getUserProfile();
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
                // Convert imageData back to Original Image
                byte [] imageData = result.getBlob(7);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
                Bitmap originalImage = BitmapFactory.decodeStream(inputStream);
                imageButton.setImageBitmap(originalImage);

            }
        }
    }






}
