package com.example.savethepig;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    static {
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity", "Open Cv is loaded");
        }else {
            Log.d("MainActivity", "Opencv failed to load");
        }
    }
    private Button camera_button;


//    private static final int REQUEST_CODE_CAMERA_ACTIVITY = 1;
//
//    private final ActivityResultLauncher<Intent> cameraLauncher =
//            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//                    new ActivityResultCallback<ActivityResult>() {
//                        @Override
//                        public void onActivityResult(ActivityResult result) {
//                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                                float emotionValue = result.getData().getFloatExtra("EMOTION_VALUE", 0f);
//                                GameView.updateEmotionValue(emotionValue);
//                            }
//                        }
//                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //to keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        camera_button=findViewById(R.id.camera_button);
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }

    public void startGame(View view) {
        //create an instance of gameview class.
        //GameView gameView = new GameView(this);
        //setContentView(gameView);


        //Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
        //cameraLauncher.launch(cameraIntent);
    }
}