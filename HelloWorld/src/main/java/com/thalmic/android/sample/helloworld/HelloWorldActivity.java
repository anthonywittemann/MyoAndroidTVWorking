/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import java.util.ArrayList;

public class HelloWorldActivity extends Activity {

    private TextView mLockStateView;
    private TextView mTextView;
    private TextView mSimonMessageTV;
    private TextView numMovesTV;
    private ImageView gestureIV;
    private ImageView rainbowTV;
    private ArrayList<Integer> previousGestures = new ArrayList<Integer>();

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        private final int M_FIST = 1;
        private final int M_FINGERS_SPREAD = 2;
        private final int M_WAVE_OUT = 3;
        private final int M_WAVE_INT = 4;
        private int currentGesture = M_FIST; //0 for no gesture


        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            mTextView.setTextColor(Color.CYAN);
            Toast.makeText(getApplicationContext(), "Connected!!!",Toast.LENGTH_SHORT).show();
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            mTextView.setTextColor(Color.RED);
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mTextView.setText(myo.getArm() == Arm.LEFT ? R.string.arm_left : R.string.arm_right);
            Toast.makeText(getApplicationContext(), "Arm Sync",Toast.LENGTH_SHORT).show();
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mTextView.setText(R.string.hello_world);
            Toast.makeText(getApplicationContext(), "Arm Un-Sync",Toast.LENGTH_SHORT).show();
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.unlocked);
            Toast.makeText(getApplicationContext(), "Unlocked",Toast.LENGTH_SHORT).show();
            mSimonMessageTV.setText(R.string.s_fist);
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.locked);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }

            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
            mTextView.setRotation(roll);
            mTextView.setRotationX(pitch);
            mTextView.setRotationY(yaw);
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            //Toast.makeText(getApplicationContext(), "Posing",Toast.LENGTH_SHORT).show();
            switch (pose) {
                case UNKNOWN:
                    mTextView.setText(getString(R.string.waiting));
                    break;
                case REST:
                case DOUBLE_TAP:
                    //int restTextId = R.string.hello_world;
                    int restTextId = R.string.waiting;
                    switch (myo.getArm()) {
                        case LEFT:
                            //restTextId = R.string.arm_left; took out so that simon says works
                            break;
                        case RIGHT:
                            //restTextId = R.string.arm_right; took out so that simon says works
                            break;
                    }
                    mTextView.setText(getString(restTextId));
                    break;
                case FIST:
                    mTextView.setText(getString(R.string.pose_fist));
                    //confirm that first was made
                    if(currentGesture == M_FIST){
                        generateNextDirection();
                    }
                    break;
                case WAVE_IN:
                    mTextView.setText(getString(R.string.pose_wavein));
                    //confirm that you waved in
                    if(currentGesture == M_WAVE_INT){
                        generateNextDirection();
                    }
                    break;
                case WAVE_OUT:
                    mTextView.setText(getString(R.string.pose_waveout));
                    //confirm that you waved out
                    if(currentGesture == M_WAVE_OUT){
                        generateNextDirection();
                    }
                    break;
                case FINGERS_SPREAD:
                    mTextView.setText(getString(R.string.pose_fingersspread));
                    //confirm that fingers were spread
                    if(currentGesture == M_FINGERS_SPREAD){
                        generateNextDirection();
                    }
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.HOLD);
            }
        }

        //create next Simon says
        private void generateNextDirection(){
            //keeps track of previous gestures in arrayList
            previousGestures.add(currentGesture);
            numMovesTV.setText("Moves Completed: " + Integer.toString(previousGestures.size()));
            if(previousGestures.size() % 10 == 0 && previousGestures.size() != 0){
                rainbowTV.setVisibility(View.VISIBLE);
            }
            else{
                rainbowTV.setVisibility(View.INVISIBLE);
            }

            int nextDir = (int) (Math.random() * 4) + 1; //random int from 1 - 4
            currentGesture = nextDir;
            //Toast.makeText(getApplicationContext(), Integer.toString(nextDir), Toast.LENGTH_SHORT).show();
            // change Simon says message
            switch (currentGesture){
                case M_FINGERS_SPREAD:
                    mSimonMessageTV.setText(getString(R.string.s_fingers_spread));
                    gestureIV.setImageResource(R.drawable.solid_blue_spread_fingers2x);
                    break;
                case M_FIST:
                    mSimonMessageTV.setText(getString(R.string.s_fist));
                    gestureIV.setImageResource(R.drawable.solid_blue_fist2x);
                    break;
                case M_WAVE_INT:
                    mSimonMessageTV.setText(getString(R.string.s_wave_in));
                    gestureIV.setImageResource(R.drawable.solid_blue_wave_left2x);
                    break;
                case M_WAVE_OUT:
                    mSimonMessageTV.setText(getString(R.string.s_wave_out));
                    gestureIV.setImageResource(R.drawable.solid_blue_wave_right2x);
                    break;
            }
        }

        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {

            if (accel.x() > 1.0) {

                //Toast.makeText(getApplicationContext(), "Accel in X", Toast.LENGTH_SHORT).show();
                //TODO add "Don't hit the screen!!"
            }

            //TODO make a splat


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        mLockStateView = (TextView) findViewById(R.id.lock_state);
        mTextView = (TextView) findViewById(R.id.text);
        mSimonMessageTV = (TextView) findViewById(R.id.simon_directions);
        gestureIV = (ImageView) findViewById(R.id.imageView);
        numMovesTV = (TextView) findViewById(R.id.moves_completed);
        rainbowTV = (ImageView) findViewById(R.id.rainbow_reward);
        //TODO create imageview for rainbow

        //Toast.makeText(getApplicationContext(), "On Create", Toast.LENGTH_SHORT).show();

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }
}
