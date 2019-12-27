package com.by2.control.companion.connect.app;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.by2.control.companion.connect.R;
import com.by2.control.companion.connect.ble.BleManager;
import com.google.android.gms.vision.text.Text;

import java.nio.ByteBuffer;

public class SecondPadActivity extends UartInterfaceActivity  {
    // Log
    private final static String TAG = SecondPadActivity.class.getSimpleName();

    // Constants
    private final static float kMinAspectRatio = 1.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pad2);

        mBleManager = BleManager.getInstance(this);

        // UI
        Button OneButton = (Button) findViewById(R.id.OneButton);
        OneButton.setOnTouchListener(mPadButtonTouchListener);
        Button TwoButton = (Button) findViewById(R.id.TwoButton);
        TwoButton.setOnTouchListener(mPadButtonTouchListener);
        Button ThreeButton = (Button) findViewById(R.id.ThreeButton);
        ThreeButton.setOnTouchListener(mPadButtonTouchListener);
        Button FourButton = (Button) findViewById(R.id.FourButton);
        FourButton.setOnTouchListener(mPadButtonTouchListener);
        Button FiveButton = (Button) findViewById(R.id.FiveButton);
        FiveButton.setOnTouchListener(mPadButtonTouchListener);
        Button SixButton = (Button) findViewById(R.id.SixButton);
        SixButton.setOnTouchListener(mPadButtonTouchListener);
        Button SevenButton = (Button) findViewById(R.id.SevenButton);
        SevenButton.setOnTouchListener(mPadButtonTouchListener);
        Button EightButton = (Button) findViewById(R.id.EightButton);
        EightButton.setOnTouchListener(mPadButtonTouchListener);
        Button NineButton = (Button) findViewById(R.id.NineButton);
        NineButton.setOnTouchListener(mPadButtonTouchListener);
        Button ClearButton = (Button) findViewById(R.id.ClearButton);
        ClearButton.setOnTouchListener(mPadButtonTouchListener);
        Button EnterButton = (Button) findViewById(R.id.EnterButton);
        EnterButton.setOnTouchListener(mPadButtonTouchListener);

        // Start services
        onServicesDiscovered();
    }

// this does not work if I took out the spacers
/*
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
*/

    int keypadInput = 0;

    public void setNumberDisplay(int keypadInput) {
        TextView numDisplay = (TextView) findViewById(R.id.numDisplay);
        numDisplay.setText(""+keypadInput);
    }

    View.OnTouchListener mPadButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int tag = new Integer((String) view.getTag());
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (tag < 11) {
                        if (keypadInput == 0) {
                            keypadInput = keypadInput + tag;
                        }
                        else {
                            keypadInput = keypadInput*10 + tag;
                        }
                        // keypadInput = keypadInput + tag;
                        setNumberDisplay(keypadInput);
                    }
                    else if (tag == 11) {
                        keypadInput = 0;
                        setNumberDisplay(keypadInput);
                    }
                    else if (tag == 12) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            view.setPressed(true);
                            // sendTouchEvent(tag, true);
                            sendTouchEvent(keypadInput, true);
                            return true;
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            view.setPressed(false);
                            // sendTouchEvent(keypadInput, false);
                            return true;
                        }
                        return false;
                    }
                    return false;
                }
            return false;
        }
    };

    private void sendTouchEvent(int keypadInput, boolean pressed) {
        if (keypadInput < 10) {
            String data = "!B" + "0" + keypadInput;
            ByteBuffer buffer = ByteBuffer.allocate(data.length()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            buffer.put(data.getBytes());
            sendDataWithCRC(buffer.array());
        }
        else {
            String data = "!B" + keypadInput;
            ByteBuffer buffer = ByteBuffer.allocate(data.length()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            buffer.put(data.getBytes());
            sendDataWithCRC(buffer.array());
        }
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
            //adjustAspectRatio();
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
