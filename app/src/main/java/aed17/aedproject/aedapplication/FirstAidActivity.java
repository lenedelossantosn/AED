package aed17.aedproject.aedapplication;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by sharlene on 4/12/2017.
 */

public class FirstAidActivity extends AppCompatActivity {
    FirstAidAdapterActivity swipeadapter;
    ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstaid_activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_aed_app2);
        actionBar.setTitle("   First Aid");

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        swipeadapter = new FirstAidAdapterActivity(this);
        viewPager.setAdapter(swipeadapter);

    }


    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.first_aid_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.activity_first_aid_exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}