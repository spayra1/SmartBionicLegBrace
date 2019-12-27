package com.by2.control.companion.connect.app;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import com.by2.control.companion.connect.R;
import com.by2.control.companion.connect.ble.BleManager;

import java.nio.ByteBuffer;

public class ThirdPadActivity extends UartInterfaceActivity  {
    // Log
    private final static String TAG = ThirdPadActivity.class.getSimpleName();

    // Constants
    private final static float kMinAspectRatio = 1.5f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pad3);

        mBleManager = BleManager.getInstance(this);

       final GlobalWalkMode globalWalkMode = (GlobalWalkMode) getApplication();

        // UI
        Button decreaseSpeed = (Button) findViewById(R.id.decreaseSpeed);
        decreaseSpeed.setOnTouchListener(mPadButtonTouchListener);
        Button increaseSpeed = (Button) findViewById(R.id.increaseSpeed);
        increaseSpeed.setOnTouchListener(mPadButtonTouchListener);

        RadioGroup walkChoices = (RadioGroup) findViewById(R.id.radioGroup1);
            RadioButton timerButton = (RadioButton) findViewById(R.id.radioButton1);
            RadioButton oldPotButton = (RadioButton) findViewById(R.id.radioButton2);
            RadioButton newPotButton = (RadioButton) findViewById(R.id.radioButton3);
            RadioButton autoSpeedButton = (RadioButton) findViewById(R.id.radioButton4);
            RadioButton antiShockButton = (RadioButton) findViewById(R.id.radioButton5);

        // set the bubble to whatever is set in the global variable, so that screen is preserved across closes.
        int walkChoice = globalWalkMode.getWalkChoice();
        if (walkChoice == 6) {walkChoices.check(timerButton.getId());}
        else if (walkChoice == 7) {walkChoices.check(oldPotButton.getId());}
        else if (walkChoice == 8) {walkChoices.check(newPotButton.getId());}
        else if (walkChoice == 9) {walkChoices.check(autoSpeedButton.getId());}
        else if (walkChoice == 10) {walkChoices.check(antiShockButton.getId());}

        walkChoices.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
               public void onCheckedChanged(RadioGroup group, int checkedId) {
                   switch (checkedId) {
                       case R.id.radioButton1: // if the selection is changed to the timer
                           globalWalkMode.setWalkChoice(6);
                           break;
                       case R.id.radioButton2: // if the selection is changed to the old potentiometer
                           globalWalkMode.setWalkChoice(7);
                           break;
                       case R.id.radioButton3: // if the selection is changed to the new potentiometer
                           globalWalkMode.setWalkChoice(8);
                           break;
                       case R.id.radioButton4: // if the selection is changed to auto speed adjustment
                           globalWalkMode.setWalkChoice(9);
                           break;
                       case R.id.radioButton5: // if the selection is changed to shock absorption
                           globalWalkMode.setWalkChoice(10);
                           break;
                   }
               }
           }
        );

        // Start services
        onServicesDiscovered();
    }

    private void adjustAspectRatio() {
        ViewGroup rootLayout = (ViewGroup) findViewById(R.id.rootLayout);
        int mainWidth = rootLayout.getWidth();

        if (mainWidth > 0) {
            View topSpacerView = findViewById(R.id.topSpacerView);
            View bottomSpacerView = findViewById(R.id.bottomSpacerView);
            int mainHeight = rootLayout.getHeight() - topSpacerView.getLayoutParams().height - bottomSpacerView.getLayoutParams().height;
            if (mainHeight > 0) {
                // Add black bars if aspect ratio is below min
                float aspectRatio = mainWidth / (float) mainHeight;
                if (aspectRatio < kMinAspectRatio) {
                    final int spacerHeight = Math.round(mainHeight * (kMinAspectRatio - aspectRatio));
                    topSpacerView.getLayoutParams().height = spacerHeight / 2;
                    bottomSpacerView.getLayoutParams().height = spacerHeight / 2;
                }
            }
        }
    }

    View.OnTouchListener mPadButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int tag = new Integer((String) view.getTag());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.setPressed(true);
                sendTouchEvent(tag, true);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                view.setPressed(false);
                // sendTouchEvent(tag, false);
                return true;
            }
            return false;
        }
    };

    private void sendTouchEvent(int tag, boolean pressed) {
        String data = "!B" + "0" + tag + (pressed ? "1" : "0");
        ByteBuffer buffer = ByteBuffer.allocate(data.length()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.put(data.getBytes());
        sendDataWithCRC(buffer.array());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pad, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public void onConfigurationChanged (Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        adjustAspectRatio();
    }
    */

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Set full screen mode
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            adjustAspectRatio();
        }

    }

    public void onClickExit(View view) {
        finish();
    }

    /*
    @Override
    public void onConnected() {

    }

    @Override
    public void onConnecting() {

    }
*/
    @Override
    public void onDisconnected() {
        super.onDisconnected();
        Log.d(TAG, "Disconnected. Back to previous activity");
        setResult(-1);      // Unexpected Disconnect
        finish();
    }
/*
    @Override
    public void onServicesDiscovered() {
        mUartService = mBleManager.getGattService(UUID_SERVICE);
    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {
    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }

    */
}
