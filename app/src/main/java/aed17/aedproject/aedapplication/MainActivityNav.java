package aed17.aedproject.aedapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.vision.barcode.Barcode;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.neurosky.AlgoSdk.NskAlgoDataType;
import com.neurosky.AlgoSdk.NskAlgoSdk;
import com.neurosky.AlgoSdk.NskAlgoSignalQuality;
import com.neurosky.AlgoSdk.NskAlgoState;
import com.neurosky.AlgoSdk.NskAlgoType;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivityNav extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // GLOBAL
    private static String meditation = "Unknown";
    private static String attention = "Unknown";

    // GRAPH VARIABLE
    LineGraphSeries series;
    LineGraphSeries series2;
    Viewport viewport;

    // ALGORITHM VARIABLES
    DatabaseHelper aedDb;
    GraphView graphView;
    double lastX = 0.1;

    // LOCATION VARIABLES
    private GoogleMap mMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Button btnCheckLocation;
    static double Latitude;
    static double Longitude;
    int counter = 0;


// NEUROSKY
// VARIABLES
    private NskAlgoSdk nskAlgoSdk;
    final String TAG = "MainActivityTag";

    // gui variables
    private TextView tvSignalQuality,tvSdkState,tvMeditation,tvAttention,tvBlink;

    // internal variables
    private boolean bInited = false;
    private boolean bRunning = false;
    private NskAlgoType currentSelectedAlgo;

    // COMM SDK handles
    private TgStreamReader tgStreamReader;
    private BluetoothAdapter mBluetoothAdapter;

    // canned data variables
    private short raw_data[] = {0};
    private int raw_data_index= 0;
    private float output_data[];
    private int output_data_count = 0;
    private int raw_data_sec_len = 85;
// VARIABLES
//METHODS

    // UTILITY METHODS
    public void showToast(final String msg, final int timeStyle) {
        MainActivityNav.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }

    private void checkIfBluetoothIsActive() {
        try {
            // (1) Make sure that the device supports Bluetooth and Bluetooth is on
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                //Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
                //finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.getMessage());
        }
    }


    // MAIN METHODS
    private void PrimaryCONNECTION() {
        // check selected algos
        int algoTypes = 0;// = NskAlgoType.NSK_ALGO_TYPE_CR.value;
        currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_INVALID;
        tvAttention.setText("--");
        tvMeditation.setText("--");
        tvSdkState.setText("");
        tvSignalQuality.setText("");
        algoTypes += NskAlgoType.NSK_ALGO_TYPE_MED.value;
        algoTypes += NskAlgoType.NSK_ALGO_TYPE_ATT.value;
        algoTypes += NskAlgoType.NSK_ALGO_TYPE_BLINK.value;

        if (algoTypes == 0) {
            //showDialog("Please select at least one algorithm");
        } else {
            if (bInited) {
                nskAlgoSdk.NskAlgoUninit();
                bInited = false;
            }
            int ret = nskAlgoSdk.NskAlgoInit(algoTypes, getFilesDir().getAbsolutePath());
            if (ret == 0) {
                bInited = true;
            }

            Log.d(TAG, "NSK_ALGO_Init() " + ret);
            String sdkVersion = "SDK ver.: " + nskAlgoSdk.NskAlgoSdkVersion();

            if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_ATT.value) != 0) {
                sdkVersion += "\nATT ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_ATT.value);
            }
            if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_MED.value) != 0) {
                sdkVersion += "\nMED ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_MED.value);
            }
            if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_BLINK.value) != 0) {
                sdkVersion += "\nBlink ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_BLINK.value);
            }
            if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_BP.value) != 0) {
                sdkVersion += "\nEEG Bandpower ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_BP.value);
            }
            //showToast(sdkVersion, Toast.LENGTH_LONG);
        }
    }

    private void SecondaryConnection() {
        // SECONDARY
        output_data_count = 0;
        output_data = null;

        raw_data = new short[512];
        raw_data_index = 0;

        // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
        tgStreamReader = new TgStreamReader(mBluetoothAdapter,callback);

        if(tgStreamReader != null && tgStreamReader.isBTConnected()){

            // Prepare for connecting
            tgStreamReader.stop();
            tgStreamReader.close();
        }

        // (4) Demo of  using connect() and start() to replace connectAndStart(),
        // please call start() when the state is changed to STATE_CONNECTED
        tgStreamReader.connect();

        if (bRunning == false) {
            nskAlgoSdk.NskAlgoStart(false);
        } else {
            nskAlgoSdk.NskAlgoPause();
        }
    }

    // PRIMARY NSKALGO
    private void PrimaryNSKALGO() {
        nskAlgoSdk.setOnSignalQualityListener(new NskAlgoSdk.OnSignalQualityListener() {
            @Override
            public void onSignalQuality(int level) {
                //Log.d(TAG, "NskAlgoSignalQualityListener: level: " + level);
                final int fLevel = level;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        String sqStr = NskAlgoSignalQuality.values()[fLevel].toString();
                        tvSignalQuality.setText(sqStr);
                    }
                });
            }
        });

        nskAlgoSdk.setOnStateChangeListener(new NskAlgoSdk.OnStateChangeListener() {
            @Override
            public void onStateChange(int state, int reason) {
                String stateStr = "";
                String reasonStr = "";
                for (NskAlgoState s : NskAlgoState.values()) {
                    if (s.value == state) {
                        stateStr = s.toString();
                    }
                }
                for (NskAlgoState r : NskAlgoState.values()) {
                    if (r.value == reason) {
                        reasonStr = r.toString();
                    }
                }
                Log.d(TAG, "NskAlgoSdkStateChangeListener: state: " + stateStr + ", reason: " + reasonStr);
                final String finalStateStr = stateStr + " | " + reasonStr;
                final int finalState = state;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        tvSdkState.setText(finalStateStr);

                        if (finalState == NskAlgoState.NSK_ALGO_STATE_RUNNING.value || finalState == NskAlgoState.NSK_ALGO_STATE_COLLECTING_BASELINE_DATA.value) {
                            bRunning = true;
                            //startButton.setText("Pause");
                            //startButton.setEnabled(true);
                            //stopButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_STOP.value) {
                            bRunning = false;
                            raw_data = null;
                            raw_data_index = 0;
                            //startButton.setText("Start");
                            //startButton.setEnabled(true);
                            //stopButton.setEnabled(false);

                            //headsetButton.setEnabled(true);
                            //cannedButton.setEnabled(true);

                            if (tgStreamReader != null && tgStreamReader.isBTConnected()) {

                                // Prepare for connecting
                                tgStreamReader.stop();
                                tgStreamReader.close();
                            }

                            output_data_count = 0;
                            output_data = null;

                            System.gc();
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_PAUSE.value) {
                            bRunning = false;
                            //startButton.setText("Start");
                            //startButton.setEnabled(true);
                            //stopButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_ANALYSING_BULK_DATA.value) {
                            bRunning = true;
                            //startButton.setText("Start");
                            //startButton.setEnabled(false);
                            //stopButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_INITED.value || finalState == NskAlgoState.NSK_ALGO_STATE_UNINTIED.value) {
                            bRunning = false;
                            //startButton.setText("Start");
                            //startButton.setEnabled(true);
                            //stopButton.setEnabled(false);
                        }
                    }
                });
            }
        });

        nskAlgoSdk.setOnSignalQualityListener(new NskAlgoSdk.OnSignalQualityListener() {
            @Override
            public void onSignalQuality(final int level) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        String sqStr = NskAlgoSignalQuality.values()[level].toString();
                        tvSignalQuality.setText(sqStr);
                    }
                });
            }
        });

        nskAlgoSdk.setOnBPAlgoIndexListener(new NskAlgoSdk.OnBPAlgoIndexListener() {
            @Override
            public void onBPAlgoIndex(float delta, float theta, float alpha, float beta, float gamma) {
                Log.d(TAG, "NskAlgoBPAlgoIndexListener: BP: D[" + delta + " dB] T[" + theta + " dB] A[" + alpha + " dB] B[" + beta + " dB] G[" + gamma + "]");

                final float fDelta = delta, fTheta = theta, fAlpha = alpha, fBeta = beta, fGamma = gamma;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        //AddValueToPlot(bp_deltaSeries, fDelta);
                        //AddValueToPlot(bp_thetaSeries, fTheta);
                        //AddValueToPlot(bp_alphaSeries, fAlpha);
                        //AddValueToPlot(bp_betaSeries, fBeta);
                        //AddValueToPlot(bp_gammaSeries, fGamma);
                    }
                });
            }
        });

        nskAlgoSdk.setOnAttAlgoIndexListener(new NskAlgoSdk.OnAttAlgoIndexListener() {
            @Override
            public void onAttAlgoIndex(final int value) {
                Log.d(TAG, "NskAlgoAttAlgoIndexListener: Attention:" + value);
                String attStr = "[" + value + "]";
                final String finalAttStr = attStr;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        if (tvSignalQuality.getText().toString() == "GOOD") {
                             // GRAPH TEST
                            try {
                                series2.appendData(new com.jjoe64.graphview.series.DataPoint(lastX,value),true,100);
                                graphView.addSeries(series2);
                                lastX += 0.1;
                            }
                            catch (Exception ex) {
                                //series.resetData(new com.jjoe64.graphview.series.DataPoint[] {});
                            }
                        }
                        if (tvSignalQuality.getText().toString() == "GOOD" && value < getAttention()) {
                            attention = finalAttStr;
                            sendSMSNow(Latitude + "," + Longitude);
                        }
                        tvAttention.setText(finalAttStr);
                    }
                });
            }
        });

        nskAlgoSdk.setOnMedAlgoIndexListener(new NskAlgoSdk.OnMedAlgoIndexListener() {
            @Override
            public void onMedAlgoIndex(final int value) {
                Log.d(TAG, "NskAlgoMedAlgoIndexListener: Meditation:" + value);
                String medStr = "[" + value + "]";
                final String finalMedStr = medStr;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        if (tvSignalQuality.getText().toString() == "GOOD") {
                            // GRAPH TEST
                            try {
                                series.appendData(new com.jjoe64.graphview.series.DataPoint(lastX,value),true,100);
                                graphView.addSeries(series);
                                lastX += 0.1;
                            }
                            catch (Exception ex) {
                                //series.resetData(new com.jjoe64.graphview.series.DataPoint[] {});
                            }
                        }
                        if (tvSignalQuality.getText().toString() == "GOOD" && value < getMeditation()) {
                            meditation = finalMedStr;
                            sendSMSNow(Latitude + "," + Longitude);
                        }
                        tvMeditation.setText(finalMedStr);
                    }
                });
            }
        });

        nskAlgoSdk.setOnEyeBlinkDetectionListener(new NskAlgoSdk.OnEyeBlinkDetectionListener() {
            @Override
            public void onEyeBlinkDetect(int strength) {
                Log.d(TAG, "NskAlgoEyeBlinkDetectionListener: Eye blink detected: " + strength);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvBlink.setText("1");
                        //blinkImage.setImageResource(R.mipmap.led_on);
                        Timer timer = new Timer();

                        timer.schedule(new TimerTask() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //blinkImage.setImageResource(R.mipmap.led_off);
                                        tvBlink.setText("0");
                                    }
                                });
                            }
                        }, 100);
                    }
                });
            }
        });
    }
//METHODS
// NEUROSKY METHODS


    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);

        nskAlgoSdk = new NskAlgoSdk();
        aedDb = new DatabaseHelper(this);



        // GRAPH
        graphView = (GraphView) findViewById(R.id.linearGraph);
        series = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();
        series.setColor(Color.RED);
        series2.setColor(Color.BLUE);
//        series.setTitle("Meditation");
//        series2.setTitle("Attention");

//        graphView.getLegendRenderer().setVisible(true);
//        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        viewport = graphView.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(100);
        viewport.setScrollable(true);
        viewport.setScalable(true);
        //viewport.setScalableY(true);

        tvSignalQuality = (TextView)findViewById(R.id.tvSignalQuality);
        tvSdkState = (TextView)findViewById(R.id.tvSdkState);
        tvMeditation = (TextView)findViewById(R.id.tvMeditation);
        tvAttention = (TextView)findViewById(R.id.tvAttention);
        tvBlink = (TextView) findViewById(R.id.tvBlink);


        checkIfBluetoothIsActive();
        PrimaryNSKALGO();
        PrimaryCONNECTION();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_location);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                // GPS
//                if (gps.canGetLocation()) {
//                    double latitude = gps.getLatitude();
//                    double longitude = gps.getLongitude();
//                    Toast.makeText(getApplicationContext(),"Your Location is -\nLat: "+latitude+"\nLong: "+longitude,Toast.LENGTH_SHORT).show();
//                } else {
//                    gps.showSettingsAlert();
//                }
                startActivity(new Intent(MainActivityNav.this, MapsActivity.class));
            }
        });



        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // GOOGLE MAP
        buildGoogleApiClient();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

    }

    // NEUROSKY SPECIAL METHODS
    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    // Do something when connecting
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    // Do something when connected
                    tgStreamReader.start();
                    showToast("Connected", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_WORKING:
                    // Do something when working

                    //(9) demo of recording raw data , stop() will call stopRecordRawData,
                    //or you can add a button to control it.
                    //You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData
                    //tgStreamReader.startRecordRawData();

                    /*MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Button startButton = (Button) findViewById(R.id.button);
                            startButton.setEnabled(true);
                        }

                    });*/

                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    // Do something when getting data timeout

                    //(9) demo of recording raw data, exception handling
                    //tgStreamReader.stopRecordRawData();

                    showToast("Get data time out!", Toast.LENGTH_SHORT);

                    if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
                        tgStreamReader.stop();
                        tgStreamReader.close();
                    }

                    break;
                case ConnectionStates.STATE_STOPPED:
                    // Do something when stopped
                    // We have to call tgStreamReader.stop() and tgStreamReader.close() much more than
                    // tgStreamReader.connectAndstart(), because we have to prepare for that.

                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    // Do something when disconnected
                    break;
                case ConnectionStates.STATE_ERROR:
                    // Do something when you get error message
                    break;
                case ConnectionStates.STATE_FAILED:
                    // Do something when you get failed message
                    // It always happens when open the BluetoothSocket error or timeout
                    // Maybe the device is not working normal.
                    // Maybe you have to try again
                    break;
            }
        }

        @Override
        public void onRecordFail(int flag) {
            // You can handle the record error message here
            Log.e(TAG,"onRecordFail: " +flag);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // You can handle the bad packets here.
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // You can handle the received data here
            // You can feed the raw data to algo sdk here if necessary.
            //Log.i(TAG,"onDataReceived");
            switch (datatype) {
                case MindDataType.CODE_ATTENTION:
                    short attValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ATT.value, attValue, 1);
                    break;
                case MindDataType.CODE_MEDITATION:
                    short medValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_MED.value, medValue, 1);
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    short pqValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_PQ.value, pqValue, 1);
                    break;
                case MindDataType.CODE_RAW:
                    raw_data[raw_data_index++] = (short)data;
                    if (raw_data_index == 512) {
                        nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_EEG.value, raw_data, raw_data_index);
                        raw_data_index = 0;
                    }
                    break;
                default:
                    break;
            }
        }

    };
    // NEUROSKY SPECIAL METHODS

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_exit_apps) {
            finish();
            System Exit;
        } else if (id == R.id.activity_first_aid) {
            startActivity(new Intent(this, FirstAidActivity.class));

        } else if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_message) {
            sendSMSNow(Latitude + "," + Longitude);
        } else if (id == R.id.nav_contact) {
            //Toast.makeText(this, "Add Contact", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, EmergencyContactsActivity.class));

        } else if (id == R.id.nav_userinfo) {
            //Toast.makeText(this, "User Account", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, UserProfileActivity.class));

        } else if (id == R.id.nav_device) {
            //Toast.makeText(this, "MindWave Connectivity", Toast.LENGTH_SHORT).show();
            //DeviceActivity device = new DeviceActivity();
            //FragmentManager fragmentManager = getSupportFragmentManager();

            SecondaryConnection();


        } else if (id == R.id.nav_settings) {
            //Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SettingsActivity.class));

        } else if (id == R.id.nav_epilepsy_info) {
            //Toast.makeText(this, "About Epilepsy Info", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, EpilepsyInfoActivity.class));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        counter++;
        Latitude = location.getLatitude();
        Longitude = location.getLongitude();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        //mMap.setMyLocationEnabled(true);
                        buildGoogleApiClient();
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    // SMS FUNCTIONALITY
    private void sendSMS(String phoneNo, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
        }
        catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    private void sendSMSNow(String coordinates) {

        Cursor result2 = aedDb.getSettingsDefaultValue();
        String enabled="";
        if (result2.getCount() != 0)
        {
            while (result2.moveToNext()) {
                enabled = result2.getString(2);
            }
        }
        if (enabled.equals("1")) {
            final Cursor result = aedDb.getAllContact();
            if (result.getCount() != 0)
            {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                List<Address> addresses  = null;
                try {
                    addresses = geocoder.getFromLocation(Latitude,Longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String city = addresses.get(0).getLocality();
                String province = addresses.get(0).getSubAdminArea();
                //String state = addresses.get(0).getAdminArea();
                //String zip = addresses.get(0).getPostalCode();
                //String country = addresses.get(0).getCountryName();
                while (result.moveToNext()) {
                    //sendSMS(result.getString(0),"test");

                    String time = new SimpleDateFormat().format(Calendar.getInstance().getTime());
                    //String dateTime = DateFormat.getDateTimeInstance().format("yyyy-MM-dd hh:mm:ss", new Date());
                    sendSMS(result.getString(0),"AED\nDateTime:"+time+"" +
                            "\nCoordinate:http://maps.google.com?q="+coordinates+
                            "\nLocation:" + city + ", " + province +
                            "\nMeditation:" + meditation +
                            "\nAttention:" + attention);
                /*Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                },2000);*/
                }
                Toast.makeText(getApplicationContext(), "SMS/s Sent",
                        Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "SMS Disabled",
                    Toast.LENGTH_LONG).show();
        }

    }

    public int getMeditation() {
        int meditation=0;
        Cursor result = aedDb.getSettingsDefaultValue();
        if (result.getCount() != 0)
        {
            while (result.moveToNext()) {
                meditation = Integer.parseInt(result.getString(0));
            }
        }
        return meditation;
    }

    public int getAttention() {
        int attention=0;
        Cursor result = aedDb.getSettingsDefaultValue();
        if (result.getCount() != 0)
        {
            while (result.moveToNext()) {
                attention = Integer.parseInt(result.getString(1));
            }
        }
        return attention;
    }
}