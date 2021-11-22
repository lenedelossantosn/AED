package aed17.aedproject.aedapplication;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


/**
 * Created by sharlene on 4/13/2017.
 */

public class EpilepsyInfoActivity extends AppCompatActivity  {

    EpilepsyInfoAdapterActivity epiadapter;
    ViewPager epiviewer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.epilepsy_info_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_aed_app2);
        actionBar.setTitle("   About");

        epiviewer = (ViewPager) findViewById(R.id.view_pager_info);
        epiadapter = new EpilepsyInfoAdapterActivity(this);
        epiviewer.setAdapter(epiadapter);

    }


    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.epilepsy_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.activity_epilepsy_info_exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
