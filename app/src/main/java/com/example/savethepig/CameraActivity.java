package com.example.savethepig;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.HashMap;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="MainActivity";

    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;

    private facialExpressionRecognition facialExpressionRecognition;
    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    private HashMap<String, Integer> emotionCountMap = new HashMap<>();
    private static final int FRAMES_INTERVAL = 15;
    private int frameCounter = 0;
    private String maxEmotionDuringInterval = "";
    private GameView gameView;
    //private GameView gameView;



    public CameraActivity(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_camera);

        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.frame_surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(1);

        try{
            //input size of model is 48
            int inputSize = 48;
            facialExpressionRecognition = new facialExpressionRecognition(getAssets(), CameraActivity.this, "model300.tflite", inputSize);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        // Initialize the GameView
        gameView = findViewById(R.id.game_view);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }

        // Enable CameraView listener
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.enableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray =new Mat(height,width,CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        frameCounter++;


        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();
        Core.flip(mRgba, mRgba, 0);
        mRgba=facialExpressionRecognition.recognizeImage(mRgba);
        //float curr_emotion_v = facialExpressionRecognition.curr_emotion_v;
        String curr_emotion_string = facialExpressionRecognition.curr_emotion_s;
        updateEmotionCount(curr_emotion_string);

        if (frameCounter == FRAMES_INTERVAL) {
            frameCounter = 0;
            maxEmotionDuringInterval = getMaxEmotion();

            if(gameView.consecutiveHappyDetections == 3 || gameView.consecutiveAngryDetections == 3 || gameView.consecutiveSadDetections ==3){
                gameView.consecutiveHappyDetections = 0;
                gameView.consecutiveAngryDetections = 0;
                gameView.consecutiveSadDetections = 0;
            }
            else if (maxEmotionDuringInterval.equals("Happy")) {
                gameView.consecutiveHappyDetections++;
                gameView.consecutiveAngryDetections = 0;
            } else if (maxEmotionDuringInterval.equals("Angry")) {
                gameView.consecutiveAngryDetections++;
                gameView.consecutiveHappyDetections = 0;
                gameView.consecutiveSadDetections = 0;
            }
            else if (maxEmotionDuringInterval.equals("Sad")) {
                gameView.consecutiveSadDetections++;
                gameView.consecutiveHappyDetections = 0;
                gameView.consecutiveAngryDetections = 0;
            }
            else {
                // If it's neither happy nor angry, reset both counters
                gameView.consecutiveHappyDetections = 0;
                gameView.consecutiveAngryDetections = 0;
                gameView.consecutiveSadDetections = 0;
            }
            //gameView.onDrawCount1 = 0;

        }

        // Run UI updates on the main (UI) thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //String maxEmotion = getMaxEmotion();
                //TextView txtEmotion = findViewById(R.id.textView);
                //txtEmotion.setText(curr_emotion_string);
                //txtEmotion.setText(maxEmotionDuringInterval);
                gameView.updateMaxEmotionIntervalText(maxEmotionDuringInterval);
            }
        });
        return mRgba;
    }



    // Call this method when you want to return the maxEmotionValue to MainActivity


    private void updateEmotionCount(String emotion) {
        // Increment emotion count in the HashMap
        emotionCountMap.put(emotion, emotionCountMap.getOrDefault(emotion, 0) + 1);
    }

    private String getMaxEmotion() {
        // Find the emotion with the maximum count in the HashMap
        int maxCount = 0;
        String maxEmotion = "";
        for (String emotion : emotionCountMap.keySet()) {
            int count = emotionCountMap.get(emotion);
            if (count > maxCount) {
                maxCount = count;
                maxEmotion = emotion;
            }
        }

        // Clear the HashMap for the next 10 frames interval
        emotionCountMap.clear();
        return maxEmotion;
    }


}