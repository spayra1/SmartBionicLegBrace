package com.by2.control.companion.connect.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.by2.control.companion.connect.R;
import com.by2.control.companion.connect.app.settings.ConnectedSettingsActivity;
import com.by2.control.companion.connect.ble.BleManager;
import com.by2.control.companion.connect.ui.utils.ExpandableHeightExpandableListView;
import com.by2.control.companion.connect.ui.utils.ExpandableHeightListView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.nio.ByteBuffer;

public class ControllerActivity extends UartInterfaceActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // Config
    private final static boolean kKeepUpdatingParentValuesInChildActivities = true;

    // Log
    private final static String TAG = ControllerActivity.class.getSimpleName();

    // Activity request codes (used for onActivityResult)
    private static final int kActivityRequestCode_ConnectedSettingsActivity = 0;

    // Constants
    private final static String kPreferences = "ControllerActivity_prefs";
    private final static String kPreferences_uartToolTip = "uarttooltip";


    // UI
    private ExpandableHeightExpandableListView mControllerListView;

    private ViewGroup mUartTooltipViewGroup;

    // Data
    private Handler sendDataHandler = new Handler();
    private GoogleApiClient mGoogleApiClient;

    private float[] mRotation = new float[9];
    private float[] mOrientation = new float[3];
    private float[] mQuaternion = new float[4];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        //Log.d(TAG, "onCreate");

        mBleManager = BleManager.getInstance(this);

        ExpandableHeightListView interfaceListView = (ExpandableHeightListView) findViewById(R.id.interfaceListView);
        ArrayAdapter<String> interfaceListAdapter = new ArrayAdapter<>(this, R.layout.layout_controller_interface_title, R.id.titleTextView, getResources().getStringArray(R.array.controller_interface_items));
        assert interfaceListView != null;
        interfaceListView.setAdapter(interfaceListAdapter);
        interfaceListView.setExpanded(true);
        interfaceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Intent intent = new Intent(ControllerActivity.this, PadActivity.class);
                    startActivityForResult(intent, 0);
                } else if (position == 1) {
                    Intent intent = new Intent(ControllerActivity.this, SecondPadActivity.class);
                    startActivityForResult(intent, 0);
                } else if (position == 2) {
                    Intent intent = new Intent(ControllerActivity.this, ThirdPadActivity.class);
                    startActivityForResult(intent, 0);
                } else if (position == 3) {
                    Intent intent = new Intent(ControllerActivity.this, FourthPadActivity.class);
                    startActivityForResult(intent, 0);
                } else if (position == 4) {
                    Intent intent = new Intent(ControllerActivity.this, VCActivity.class);
                    startActivityForResult(intent, 0);
                }
            }
        });

        mUartTooltipViewGroup = (ViewGroup) findViewById(R.id.uartTooltipViewGroup);
        SharedPreferences preferences = getSharedPreferences(kPreferences, Context.MODE_PRIVATE);
        final boolean showUartTooltip = preferences.getBoolean(kPreferences_uartToolTip, true);
        mUartTooltipViewGroup.setVisibility(showUartTooltip ? View.VISIBLE : View.GONE);


        // Google Play Services (used for location updates)
        buildGoogleApiClient();

        // Start services
        onServicesDiscovered();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        if (!kKeepUpdatingParentValuesInChildActivities) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            startHelp();
            return true;
        } else if (id == R.id.action_connected_settings) {
            startConnectedSettings();
            return true;
        } else if (id == R.id.action_refreshcache) {
            if (mBleManager != null) {
                mBleManager.refreshDeviceCache();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void startConnectedSettings() {
        // Launch connected settings activity
        Intent intent = new Intent(this, ConnectedSettingsActivity.class);
        startActivityForResult(intent, kActivityRequestCode_ConnectedSettingsActivity);
    }

    private void startHelp() {
        // Launch app help activity
        Intent intent = new Intent(this, CommonHelpActivity.class);
        intent.putExtra("title", getString(R.string.controller_help_title));
        intent.putExtra("help", "controller_help.html");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode < 0) {       // Unexpected disconnect
                setResult(resultCode);
                finish();
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void onClickCloseTooltip(View view) {
        SharedPreferences settings = getSharedPreferences(kPreferences, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(kPreferences_uartToolTip, false);
        editor.apply();

        mUartTooltipViewGroup.setVisibility(View.GONE);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}

/*
Taken out of the xml file:

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="@string/controller_sectiontitle_stream"
                android:textColor="@drawable/default_textcolor" />

            <com.by2.control.companion.connect.ui.utils.ExpandableHeightExpandableListView
                android:id="@+id/controllerListView"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="10dp"
                android:background="#ffffff"
                android:childDivider="#00000000"
                android:groupIndicator="@null" />

            In LinearLayout:
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="0dp"
                android:text="@string/controller_sectiontitle_interface"
                android:textColor="@drawable/default_textcolor" />

            In expandableheightlistview:
            android:layout_marginTop="10dp"
 */