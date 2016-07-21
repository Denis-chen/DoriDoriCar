/*
 * Copyright (C) 2013 yixia.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beji.doridoricar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;

import io.vov.vitamio.LibsChecker;

public class ManualActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = "MediaPlayerDemo";
    private SeekBar seekbar;
    private int angleX = 90, angleY = 90;
    private int V;
    private SurfaceView mPreview;
    private SurfaceHolder holder;
    //private String path = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
    private String path;
    private int manualport;
    private String manualip;
    private Button cbtnup;
    private Button cbtndown;
    private Button cbtnleft;
    private Button cbtnright;
    private Button btnup;
    private Button btndown;
    private Button btnleft;
    private Button btnright;
    private Button stop;
    private long lastUpdate = 0;
    private OnTouchListener onBtnTouchListener = new OnTouchListener() {
        datathread buttonthread = null;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (v.getId()) {
                case R.id.cbtnup:
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            buttonthread = new datathread(manualip, manualport, "y+" + angleY);
                            buttonthread.execute();
                            break;
                        case MotionEvent.ACTION_UP:
                            angleY = buttonthread.getAngleY();
                            buttonthread.cancel(true);
                            break;
                        default:
                            break;
                    }
                    break;

                case R.id.cbtndown:
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            buttonthread = new datathread(manualip, manualport, "y-" + angleY);
                            buttonthread.execute();
                            break;
                        case MotionEvent.ACTION_UP:
                            angleY = buttonthread.getAngleY();
                            buttonthread.cancel(true);
                            break;
                        default:
                            break;
                    }
                    break;

                case R.id.cbtnleft:
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            buttonthread = new datathread(manualip, manualport, "x-" + angleX);
                            buttonthread.execute();
                            break;
                        case MotionEvent.ACTION_UP:
                            angleX = buttonthread.getAngleX();
                            buttonthread.cancel(true);
                            break;
                        default:
                            break;
                    }
                    break;

                case R.id.cbtnright:
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            buttonthread = new datathread(manualip, manualport, "x+" + angleX);
                            buttonthread.execute();
                            break;
                        case MotionEvent.ACTION_UP:
                            angleX = buttonthread.getAngleX();
                            buttonthread.cancel(true);
                            break;
                        default:
                            break;
                    }
                    break;

                case R.id.stop:
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            buttonthread = new datathread(manualip, manualport, "s");
                            buttonthread.execute();
                            break;
                        case MotionEvent.ACTION_UP:
                            buttonthread.cancel(true);
                            break;

                        default:
                            break;
                    }
                    break;

                case R.id.upbtn:
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            buttonthread = new datathread(manualip, manualport, "f");
                            buttonthread.execute();
                            break;
                        case MotionEvent.ACTION_UP:
                            buttonthread.cancel(true);
                            break;
                        default:
                            break;
                    }
                    break;

                case R.id.downbtn:
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            buttonthread = new datathread(manualip, manualport, "b");
                            buttonthread.execute();
                            break;
                        case MotionEvent.ACTION_UP:
                            buttonthread.cancel(true);
                            break;
                        default:
                            break;
                    }
                    break;

                case R.id.leftbtn:
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            buttonthread = new datathread(manualip, manualport, "l");
                            buttonthread.execute();
                            break;
                        case MotionEvent.ACTION_UP:
                            buttonthread.cancel(true);
                            break;
                        default:
                            break;
                    }
                    break;

                case R.id.rightbtn:
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            buttonthread = new datathread(manualip, manualport, "r");
                            buttonthread.execute();
                            break;
                        case MotionEvent.ACTION_UP:
                            buttonthread.cancel(true);
                            break;
                        default:
                            break;
                    }
                    break;
            }


            return false;
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        setContentView(R.layout.manual);

        Intent pre = getIntent();
        mPreview = (SurfaceView) findViewById(R.id.surface1);
        holder = mPreview.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBA_8888);

        if (!pre.getExtras().isEmpty()) {
            manualip = pre.getExtras().getString("manualip");
            manualport = pre.getExtras().getInt("manualport");
        }
        path = "rtsp://" + manualip + ":8554/pi";

        cbtnup = (Button) findViewById(R.id.cbtnup);
        cbtnup.setOnTouchListener(onBtnTouchListener);
        cbtndown = (Button) findViewById(R.id.cbtndown);
        cbtndown.setOnTouchListener(onBtnTouchListener);
        cbtnleft = (Button) findViewById(R.id.cbtnleft);
        cbtnleft.setOnTouchListener(onBtnTouchListener);
        cbtnright = (Button) findViewById(R.id.cbtnright);
        cbtnright.setOnTouchListener(onBtnTouchListener);

        btnup = (Button) findViewById(R.id.upbtn);
        btnup.setOnTouchListener(onBtnTouchListener);
        btndown = (Button) findViewById(R.id.downbtn);
        btndown.setOnTouchListener(onBtnTouchListener);
        btnleft = (Button) findViewById(R.id.leftbtn);
        btnleft.setOnTouchListener(onBtnTouchListener);
        btnright = (Button) findViewById(R.id.rightbtn);
        btnright.setOnTouchListener(onBtnTouchListener);
        stop = (Button) findViewById(R.id.stop);
        stop.setOnTouchListener(onBtnTouchListener);

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax(40);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                V = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // Toast.makeText(ManualActivity.this, V, Toast.LENGTH_LONG).show();
                datathread2 buttonthread = new datathread2(manualip, manualport, "p" + V);
                buttonthread.execute();
            }
        });
    }

    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        Log.d(TAG, "surfaceChanged called");

    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed called");
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called");
        customVideo cv = new customVideo(holder, this, path);
        cv.playVideo();
    }


}
