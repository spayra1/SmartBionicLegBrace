package com.by2.control.companion.connect.app;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.by2.control.companion.connect.R;
import com.by2.control.companion.connect.ble.BleManager;

import java.nio.ByteBuffer;
import java.util.List;

public class VCActivity extends UartInterfaceActivity  {
    // Log
    private final static String TAG = VCActivity.class.getSimpleName();

    // Constants
    private final static float kMinAspectRatio = 1.5f;





// inserted this area to try voice contrl
    private static final int SPEECH_REQUEST_CODE = 0;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText
            sendVoiceEvent(spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendVoiceEvent(String voiceInput) {
        final GlobalWalkMode globalWalkMode = (GlobalWalkMode) getApplication();
        String voiceProcessed = voiceInput.toLowerCase();
        if (voiceProcessed.equals("brace stop")) {
            String data = "!B" + "0" + "2" + "1";
            ByteBuffer buffer = ByteBuffer.allocate(data.length()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            buffer.put(data.getBytes());
            sendDataWithCRC(buffer.array());
            Toast.makeText(getApplicationContext(), "brace stopped", Toast.LENGTH_SHORT).show();
        }
        else if (voiceProcessed.equals("brace walk")) {
            int walkChoice = globalWalkMode.getWalkChoice();
            if (walkChoice < 10) {
                String data = "!B" + "0" + walkChoice + "1";
                ByteBuffer buffer = ByteBuffer.allocate(data.length()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                buffer.put(data.getBytes());
                sendDataWithCRC(buffer.array());
                Toast.makeText(getApplicationContext(), "walking started", Toast.LENGTH_SHORT).show();
            } else if (walkChoice < 20) {
                String data = "!B" + walkChoice + "1";
                ByteBuffer buffer = ByteBuffer.allocate(data.length()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                buffer.put(data.getBytes());
                sendDataWithCRC(buffer.array());
                Toast.makeText(getApplicationContext(), "walking started", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "invalid command", Toast.LENGTH_SHORT).show();
        }
    }
// end inserted voice control area, 2017-02-04,5:48PM






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vc);

        mBleManager = BleManager.getInstance(this);

        // UI
        Button StopButton = (Button) findViewById(R.id.StopButton);
        StopButton.setOnTouchListener(mPadButtonTouchListener);
        Button WalkButton = (Button) findViewById(R.id.WalkButton);
        WalkButton.setOnTouchListener(mPadButtonTouchListener);

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
            if (tag == 1) {displaySpeechRecognizer(); return true;}
            else if (tag == 2) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    view.setPressed(true);
                    sendTouchEvent(tag, true);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.setPressed(false);
                    return true;
                }
                return false;
            }
            else {return false;}
        }
    };

    private void sendTouchEvent(int tag, boolean pressed) {
        final GlobalWalkMode globalWalkMode = (GlobalWalkMode) getApplication();
        if (tag != 40) {
            String data = "!B" + "0" + tag + (pressed ? "1" : "0");
            ByteBuffer buffer = ByteBuffer.allocate(data.length()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            buffer.put(data.getBytes());
            sendDataWithCRC(buffer.array());
        }
        else {
            int walkChoice = globalWalkMode.getWalkChoice();
            if (walkChoice < 10) {
                String data = "!B" + "0" + walkChoice + (pressed ? "1" : "0");
                ByteBuffer buffer = ByteBuffer.allocate(data.length()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                buffer.put(data.getBytes());
                sendDataWithCRC(buffer.array());
            } else if (walkChoice < 20) {
                String data = "!B" + walkChoice + (pressed ? "1" : "0");
                ByteBuffer buffer = ByteBuffer.allocate(data.length()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                buffer.put(data.getBytes());
                sendDataWithCRC(buffer.array());
            }
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

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        Log.d(TAG, "Disconnected. Back to previous activity");
        setResult(-1);      // Unexpected Disconnect
        finish();
    }

}