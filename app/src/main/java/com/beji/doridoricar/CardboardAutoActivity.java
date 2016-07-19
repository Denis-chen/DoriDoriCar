package com.beji.doridoricar;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Surface;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;


public class CardboardAutoActivity extends CardboardActivity implements CardboardView.StereoRenderer, SurfaceTexture.OnFrameAvailableListener, OnPreparedListener,
        OnVideoSizeChangedListener, OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private static final String TAG = "CardboardActivity";
    //gl magic key
    private static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    //vertex position
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f, 1.0f, 0, 0.f, 1.f,
            1.0f, 1.0f, 0, 1.f, 1.f,
    };
    //shader
    private final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +

                    "}\n";
    private final String mFragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";
    //view,mediaplayer
    private CardboardOverlayView overlayView;
    private MediaPlayer mMediaPlayer = null;
    private String path = null;
    private Vibrator vibrator;
    private FloatBuffer mTriangleVertices;
    //matrix
    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];
    //hadler, texture
    private int mProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;
    //surface
    private SurfaceTexture mSurface;
    private boolean updateSurface = false;
    //video size
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;

    //auto
    private boolean status = true;
    private SensorManager manager1;
    private SensorManager manager2;

    private Sensor gyro;
    private Sensor accl;

    private datathread2 buttonthread;
    private int autoport;
    private String autoip;
    private float lastUpdate;
    ///sensor listener
    SensorEventListener MySensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;
            long actualTime = event.timestamp;

            if (status) { //카메라
                if (sensor.getType() == Sensor.TYPE_GYROSCOPE) { //카메라 x
                    int camleri = Math.round(event.values[0] * 10);
                    int input = -1;
                    //카메라x축
                    if (-10 < camleri && camleri < 10) {
                    } else if (camleri < -30) input = 180;
                    else if (camleri < -20) input = 150;
                    else if (camleri <= -10) input = 120;
                    else if (camleri > 30) input = 0;
                    else if (camleri > 20) input = 30;
                    else if (camleri >= 10) input = 60;

                    if (input != -1) {
                        buttonthread = new datathread2(autoip, autoport, "x" + input);
                        buttonthread.execute();
                    }
                }
                if (actualTime - lastUpdate > 800000000) {
                    if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        int camupdown = (int) event.values[2];
                        //카메라 y축
                        camupdown = (((-1 * camupdown) + 9) * 180) / 17;
                        if (camupdown >= 180) camupdown = 180;
                        if (camupdown <= 0) camupdown = 0;
                        buttonthread = new datathread2(autoip, autoport, "y" + camupdown);
                        buttonthread.execute();
                    }
                    lastUpdate = actualTime;
                }

            } else { //바퀴
                if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    int camleri = Math.round(event.values[0] * 10);
                    String mode = null;
                    if (camleri <= -10) mode = "r";
                    else if (camleri >= 10) mode = "l";

                    if (mode != null) {
                        buttonthread = new datathread2(autoip, autoport, mode);
                        buttonthread.execute();
                    }
                }


            }//if
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }


    }; //end sensorlistener
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //vitamio library 확인
        if (!LibsChecker.checkVitamioLibs(this))
            return;

        //context를 가진 mediaplayer 생성
        mMediaPlayer = new MediaPlayer(this);

        //cardboard view에 renderer 등록
        setContentView(R.layout.common_ui);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        //정점들의 위치를 저장
        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

        //매트릭스 생성
        Matrix.setIdentityM(mSTMatrix, 0);

        //overlap되어서 글자나 이미지를 띄우는 뷰 생성 & 3D 글자 toast
        overlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        overlayView.show3DToast("Wait few minutes...streaming loading:)");
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //부모 intent로 부터 server ip를 받아서 video path로 사용
        //"rtsp://220.67.128.179:8554/pi"  or "rtsp://192.168.42.1:8554/pi"
        Intent pre = getIntent();
        if (!pre.getExtras().isEmpty()) {
            autoip = pre.getExtras().getString("autoip");
            autoport = pre.getExtras().getInt("autoport");
        }
        path = "rtsp://" + autoip + ":8554/pi";

        //sensor
        manager1 = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        manager2 = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        gyro = manager1.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accl = manager2.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        manager1.registerListener(MySensorListener, accl, SensorManager.SENSOR_DELAY_NORMAL);
        manager2.registerListener(MySensorListener, gyro, SensorManager.SENSOR_DELAY_NORMAL);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    } //end oncreate

    @Override
    public void onBackPressed() {

        manager1.unregisterListener(MySensorListener);
        manager2.unregisterListener(MySensorListener);

        if (buttonthread != null)
            buttonthread.cancel(true);
        finish();

        super.onBackPressed();
    }

    //자석 이벤트
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");
        //진동으로 알림
        vibrator.vibrate(50);

        // 바퀴모드/카메라모드 전환
        status = !status;
        if (status) {
            buttonthread = new datathread2(autoip, autoport, "s");
            buttonthread.execute();
            buttonthread = new datathread2(autoip, autoport, "d");
            buttonthread.execute();
            overlayView.show3DToast("Camera controll mode");
        } else {
            buttonthread = new datathread2(autoip, autoport, "f");
            buttonthread.execute();
            overlayView.show3DToast("Rc car controll mode");
        }

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    //surface 생성시 texture를 만들고, texture를 가진 surface에 mediaplayer를 재생한다.
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");

        // shader와 handler set
        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");

        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        if (mSurface != null) {
            mSurface.release();
        }

        // 만들어진 textureID를 가지는 surfaceTexture를 MediaPlayer에 넘긴다.
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);
        Surface surface = new Surface(mSurface);

        synchronized (this) {
            updateSurface = false;
        }
        try {
            //path 설정과 listener 등록
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //mediaplayer 재생
        mMediaPlayer.start();
    }

    //mediaplayer의 크기를 뷰 크기에 맞춘다.
    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = getCardboardView().getWidth();
        int viewHeight = getCardboardView().getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // width가 좁으면 height 제한.
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            //  height가 좁으면 width 제한.
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight + " view=" + viewWidth + "x" + viewHeight
                + " newView=" + newWidth + "x" + newHeight + " off=" + xoff + "," + yoff);

        android.graphics.Matrix txform = new android.graphics.Matrix();

        // CardboardView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        //CardboardView().setTransform(txform);
    }

    //비디오 사이즈가 변경될때, mediaplayer의 크기를 뷰 크기에 맞춘다.
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.v(TAG, "onVideoSizeChanged called");

        if (width == 0 || height == 0) {
            Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback(mp);
        }
    }

    //play준비가 되면, 뷰크기에 맞춰서 video를 재생한다.
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared called");
        mIsVideoReadyToBePlayed = true;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback(mp);
        }// TODO Auto-generated method stub

    }

    //뷰크기에 맞춰서 video 재생
    private void startVideoPlayback(MediaPlayer mp) {
        Log.v(TAG, "startVideoPlayback");
        adjustAspectRatio(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
        mp.start();
    }

    //각각의 눈을 위한 프레임 그리고, 자동으로 뷰를 왜곡한다.
    @Override
    public void onDrawEye(Eye eye) {

        mSurface.updateTexImage();
        mSurface.getTransformMatrix(mSTMatrix);
        updateSurface = false;

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glFinish();
    }

    //shader를 로드한다.
    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    //shader로 정점들과 fragment에 표면을 채운다.
    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    //error 확인
    private void checkGlError(String op) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    //뷰가 렌더링 될때마다 호출, 머리 위치를 알 수 있다.
    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    //frame이 끝났을 때 행동
    public void onFinishFrame(Viewport viewport) {
    }

    //이용가능한 새로운 데이터가 있으면, sufraceTexture를 부른다.
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        updateSurface = true;
    }

    public void onCompletion(MediaPlayer mp) {
        mp.start();
    }

    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // TODO Auto-generated method stub

    }

    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown ");
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CardboardAuto Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.beji.doridoricar/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CardboardAuto Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.beji.doridoricar/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}