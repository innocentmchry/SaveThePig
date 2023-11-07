package com.example.savethepig;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GameOver extends AppCompatActivity {

    //few object references of textview, sharedpreff and image view
    TextView tvPoints;
    TextView tvHighest;
    SharedPreferences sharedPreferences;
    ImageView ivNewHighest;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //deleted persistent state
        super.onCreate(savedInstanceState);
        // set content view and pass the layout res file for game over
        setContentView(R.layout.game_over);
        // get the handles
        tvPoints = findViewById(R.id.tvPoints);
        tvHighest = findViewById(R.id.tvHighest);
        ivNewHighest = findViewById(R.id.ivNewHighest);
        // get the points from intent
        int points = getIntent().getExtras().getInt("points");
        tvPoints.setText("" + points);
        // instantiate shared pref object, mypref is file name and 0 for private mode
        sharedPreferences = getSharedPreferences("my_pref", 0);
        // create a shared pref key called highest with default value of 0
        // also store this key value in another int variable also named highest
        int highest = sharedPreferences.getInt("highest", 0);
        if (points > highest){
            ivNewHighest.setVisibility(View.VISIBLE);
            highest = points;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("highest", highest);
            editor.commit();
        }
        tvHighest.setText(""+highest);
    }

    public void restart(View view){
        Intent intent = new Intent(GameOver.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void exit(View view){
        finish();
    }
}
