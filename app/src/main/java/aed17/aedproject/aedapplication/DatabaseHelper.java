package aed17.aedproject.aedapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "aed.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableProfile(db);
        createTableSettings(db);
    }

    private void createTableProfile(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();

        // Create table_profile
        sb.append("CREATE TABLE table_profile ");
        sb.append("(ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append("LNAME TEXT, ");
        sb.append("FNAME TEXT, ");
        sb.append("MNAME TEXT, ");
        sb.append("BDATE TEXT, ");
        sb.append("AGE TEXT, ");
        sb.append("ADDRESS TEXT, ");
        sb.append("CONTACTNO TEXT, ");
        sb.append("EMAIL TEXT, ");
        sb.append("RELATION TEXT, ");
        sb.append("IMAGE BLOB, ");
        sb.append("ISUSER BOOLEAN)");
        db.execSQL(sb.toString());
    }

    private void createTableSettings(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();

        // Create table_settings
        sb.append("CREATE TABLE table_settings ");
        sb.append("(ID TEXT, "); // 1 is Meditation, 2 is Attention
        sb.append("MEDVALUE TEXT, ");
        sb.append("ATTVALUE TEXT, ");
        sb.append("SMSENABLED TEXT)");
        db.execSQL(sb.toString());
        db.execSQL("INSERT INTO table_settings (ID,MEDVALUE, ATTVALUE, SMSENABLED) SELECT 1, 20, 20, 1 WHERE NOT EXISTS (SELECT 1 FROM table_settings WHERE ID = 1)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS table_profile");
        onCreate(db);
    }

    public boolean updateProfile(String lname, String fname, String mname, String bdate, String age, String address, String email, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("table_profile","ISUSER" + "='"+1+"'",null);
        ContentValues contentValues = new ContentValues();
        contentValues.put("LNAME",lname);
        contentValues.put("FNAME",fname);
        contentValues.put("MNAME",mname);
        contentValues.put("BDATE",bdate);
        contentValues.put("AGE",age);
        contentValues.put("ADDRESS",address);
        contentValues.put("EMAIL",email);
        contentValues.put("IMAGE",image);
        contentValues.put("ISUSER",true);
        long result = db.insert("table_profile",null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public void updateSettings(String meditation, String attention, String enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE table_settings SET MEDVALUE="+meditation+", ATTVALUE="+attention+", SMSENABLED="+enabled+" WHERE ID=1");
    }

    public boolean updateContact(String lname, String fname, String mname, String bdate, String age, String address, String email, String relationship, String contactno, String oldcontactno, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        //db.delete("table_profile","ISUSER=0 AND CONTACTNO='"+contactno+"'",null);
        ContentValues contentValues = new ContentValues();
        contentValues.put("LNAME",lname);
        contentValues.put("FNAME",fname);
        contentValues.put("MNAME",mname);
        contentValues.put("BDATE",bdate);
        contentValues.put("AGE",age);
        contentValues.put("ADDRESS",address);
        contentValues.put("EMAIL",email);
        contentValues.put("RELATION",relationship);
        contentValues.put("CONTACTNO",contactno);
        contentValues.put("IMAGE",image);
        contentValues.put("ISUSER",false);
        long result = db.update("table_profile",contentValues, "ISUSER=0 AND CONTACTNO='"+oldcontactno+"'",null);
        if (result == -1)
            return false;
        else
            return true;
    }

    public boolean addEmergencyContact(String lname, String fname, String mname, String bdate, String age, String address, String email, String contactno, String relationship, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("LNAME",lname);
        contentValues.put("FNAME",fname);
        contentValues.put("MNAME",mname);
        contentValues.put("BDATE",bdate);
        contentValues.put("AGE",age);
        contentValues.put("ADDRESS",address);
        contentValues.put("EMAIL",email);
        contentValues.put("RELATION",relationship);
        contentValues.put("CONTACTNO",contactno);
        contentValues.put("IMAGE",image);
        contentValues.put("ISUSER",false);
        long result = db.insert("table_profile",null,contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getUserProfile() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT LNAME, FNAME, MNAME, BDATE, AGE, ADDRESS, EMAIL, IMAGE FROM table_profile WHERE ISUSER=1",null);
        return result;
    }

    public Cursor getSettingsDefaultValue() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT MEDVALUE,ATTVALUE,SMSENABLED FROM table_settings WHERE ID=1",null);
        return result;
    }

    public Cursor getEditContact(String contactno) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT LNAME, FNAME, MNAME, BDATE, AGE, ADDRESS, EMAIL, RELATION, CONTACTNO, IMAGE FROM table_profile WHERE ISUSER=0 AND CONTACTNO='"+contactno+"'",null);
        return result;
    }

    public Cursor getAllContact() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT CONTACTNO FROM table_profile WHERE ISUSER=0",null);
        return result;
    }

    public Cursor getEmergencyContacts() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT CONTACTNO, FNAME || ' ' || MNAME || ' ' || LNAME AS FULLNAME, RELATION, IMAGE FROM table_profile WHERE ISUSER=0",null);
        return result;
    }

    public void deleteUserFromDataBase(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("table_profile","FNAME || ' ' || MNAME || ' ' || LNAME" + "='"+name+"'",null);
    }
}