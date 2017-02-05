package com.malcubierre.joystick.bluno;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BlunoActivity {

    RelativeLayout layout_joystick;
    TextView textView1, textView2, textView3, textView4, textView5;
    Toolbar toolbar;

    Joystick js;

    boolean[] keys = {false, false, false, false, false, false};
    byte [] lastMessage = {};

    private int mInterval = 500; // 0.5 seconds by default, can be changed later
    private Handler mHandler;

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        toolbar.setTitle(Calendar.getInstance().get(Calendar.MILLISECOND));
                        byte [] message = getSendMessage(keys, js);
                        if (!Arrays.equals(message, lastMessage)) {
                            serialSend(message);
                            lastMessage = message;
                        }
                    } finally {
                        mHandler.postDelayed(mStatusChecker, mInterval);
                    }

                }
            });
        }
    };

    byte [] getSendMessage(boolean[] keys, Joystick js) {

        List<Byte> message = new ArrayList();
        message.add((byte)0x55);
        message.add((byte)0xAA);
        message.add((byte)0x11);

        int pressedKeys = 0;
        for(int i = 0; i < keys.length; i++) {
            if (keys[i] == true) {
                pressedKeys++;
            }
        }
        message.add((byte) pressedKeys);

        int[] position = js.getPosition();
        if (position[0] == 80 && position[1] == 80) {
            message.add((byte)0x00);
        } else {
            message.add((byte)0x03);
        }


        for(int i = 0; i < keys.length; i++) {
            if (keys[i] == true) {
                message.add((byte)(i+1));
            }
        }

        message.add((byte)position[1]);
        message.add((byte)position[0]);
        message.add((byte)0x00);
        message.add((byte)0x00);

        byte checksum = 0;
        for (Byte b: message) {
            checksum = (byte) (checksum + b);
        }
        message.add(checksum);

        return toPrimitives(message);
    }

    private byte[] toPrimitives(List<Byte> oBytes)
    {
        byte[] bytes = new byte[oBytes.size()];

        for(int i = 0; i < oBytes.size(); i++) {
            bytes[i] = oBytes.get(i);
        }
        return bytes;
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onCreateProcess();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();


        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);
        textView5 = (TextView)findViewById(R.id.textView5);

        layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);

        js = new Joystick(getApplicationContext()
                , layout_joystick, R.drawable.image_button);
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);

        layout_joystick.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    int[] position = js.getPosition();
                    textView1.setText("X : " + String.valueOf(position[0]));
                    textView2.setText("Y : " + String.valueOf(position[1]));
                    textView3.setText("Angle : " + String.valueOf(js.getAngle()));
                    textView4.setText("Distance : " + String.valueOf(js.getDistance()));

                    int direction = js.get8Direction();
                    if(direction == Joystick.STICK_UP) {
                        textView5.setText("Direction : Up");
                    } else if(direction == Joystick.STICK_UPRIGHT) {
                        textView5.setText("Direction : Up Right");
                    } else if(direction == Joystick.STICK_RIGHT) {
                        textView5.setText("Direction : Right");
                    } else if(direction == Joystick.STICK_DOWNRIGHT) {
                        textView5.setText("Direction : Down Right");
                    } else if(direction == Joystick.STICK_DOWN) {
                        textView5.setText("Direction : Down");
                    } else if(direction == Joystick.STICK_DOWNLEFT) {
                        textView5.setText("Direction : Down Left");
                    } else if(direction == Joystick.STICK_LEFT) {
                        textView5.setText("Direction : Left");
                    } else if(direction == Joystick.STICK_UPLEFT) {
                        textView5.setText("Direction : Up Left");
                    } else if(direction == Joystick.STICK_NONE) {
                        textView5.setText("Direction : Center");
                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView1.setText("X :");
                    textView2.setText("Y :");
                    textView3.setText("Angle :");
                    textView4.setText("Distance :");
                    textView5.setText("Direction :");
                }
                return true;
            }
        });


        serialBegin(115200);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonScanOnClickProcess();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected void onResume(){
        super.onResume();
        System.out.println("BlUNOActivity onResume");
        onResumeProcess();														//onResume Process by BlunoActivity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoActivity
        super.onActivityResult(requestCode, resultCode, data);
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
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoActivity
    }

    protected void onStop() {
        super.onStop();
        onStopProcess();														//onStop Process by BlunoActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyProcess();														//onDestroy Process by BlunoActivity
        stopRepeatingTask();
    }

    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                toolbar.setTitle("Connected");
                startRepeatingTask();
                fab.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_close_clear_cancel));
                break;
            case isConnecting:
                toolbar.setTitle("Connecting");
                break;
            case isToScan:
                toolbar.setTitle("Scan");
                break;
            case isScanning:
                toolbar.setTitle("Scanning");
                break;
            case isDisconnecting:
                toolbar.setTitle("isDisconnecting");
                stopRepeatingTask();
                fab.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_input_add));
                break;
            default:
                break;
        }
    }

    @Override
    public void onSerialReceived(String theString) {
        //Once connection data received, this function will be called
        // TODO Do nothing, only sends
    }

}
