package com.example.savethepig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {

    Bitmap background, ground, rabbit;

    // two rect object references for background and ground
    Rect rectBackground, rectGround;
    Context context;
    Handler handler;
    final long UPDATE_MILLIS = 30;
    Runnable runnable;

    // for showing points and health
    Paint textPaint = new Paint();
    Paint textPaint2 = new Paint();
    Paint textPaint3 = new Paint();
    Paint textPaint4 = new Paint();
    Paint healthPaint = new Paint();

    float TEXT_SIZE = 100;
    int points = 0;
    int life = 3;
    static int dWidth, dHeight;
    Random random;
    float rabbitX, rabbitY;

    //for reposition the rabbit during a touch
    float oldX;
    float oldRabbitX;
    ArrayList<Spike> spikes;
    ArrayList<Explosion> explosions;

    private int level = 1;
    private int spikeCount = 2;

    private String maxEmotionIntervalText = "";

    public int consecutiveHappyDetections = 0;
    public int consecutiveAngryDetections = 0;
    public int consecutiveSadDetections = 0;
    public int maxConsecutiveDetections = 3;

    private Handler levelChangeHandler = new Handler();
    private static final long LEVEL_CHANGE_DELAY = 1000; // 1 second (adjust as needed)
    private boolean canIncreaseLevel = true;
    private boolean canDecreaseLevel = true;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        rabbit = BitmapFactory.decodeResource(getResources(), R.drawable.pig);
        //instantiate a display object
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //will return device height and device weight
        dWidth = size.x;
        dHeight = size.y;
        rectBackground = new Rect(0, 0, dWidth, dHeight);
        rectGround = new Rect(0, dHeight - ground.getHeight(), dWidth, dHeight);
        //this class schedules the runnable to be executed after some delay
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // this will basically call the onDraw method for us and the view?? will be redrawn
                invalidate();
            }
        };
        textPaint.setColor(Color.rgb(255, 165, 0));
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.typographica));

        textPaint2.setColor(Color.rgb(36, 110, 185));
        textPaint2.setTextSize(TEXT_SIZE);
        textPaint2.setTextAlign(Paint.Align.LEFT);
        textPaint2.setTypeface(ResourcesCompat.getFont(context, R.font.typographica));

        textPaint3.setColor(Color.rgb(1, 50, 32));
        textPaint3.setTextSize(TEXT_SIZE);
        textPaint3.setTextAlign(Paint.Align.LEFT);
        textPaint3.setTypeface(ResourcesCompat.getFont(context, R.font.typographica));

        textPaint4.setColor(Color.rgb(170, 51, 106));
        textPaint4.setTextSize(TEXT_SIZE);
        textPaint4.setTextAlign(Paint.Align.LEFT);
        textPaint4.setTypeface(ResourcesCompat.getFont(context, R.font.typographica));

        healthPaint.setColor(Color.GREEN);
        random = new Random();
        // that the rabbit can be drawn horizontally center and on top of ground
        rabbitX = dWidth / 2 - rabbit.getWidth() / 2;
        rabbitY = dHeight - ground.getHeight() - rabbit.getHeight();
        spikes = new ArrayList<>();
        explosions = new ArrayList<>();
        for (int i=0; i<spikeCount; i++){
            Spike spike = new Spike(context);
            spikes.add(spike);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw the background ground and rabbit bitmaps
        canvas.drawBitmap(background, null, rectBackground, null);
        canvas.drawBitmap(ground, null, rectGround, null);
        canvas.drawBitmap(rabbit, rabbitX, rabbitY, null);
        //
        for (int i=0; i<spikes.size(); i++){//minus 1 bola tha,
            // draw the spike element with current spike frame
            canvas.drawBitmap(spikes.get(i).getSpike(spikes.get(i).spikeFrame), spikes.get(i).spikeX, spikes.get(i).spikeY, null);
            spikes.get(i).spikeFrame++;
            if (spikes.get(i).spikeFrame > 2){
                spikes.get(i).spikeFrame = 0;
            }
            // incr spikey with spike velocity for top down movement
            spikes.get(i).spikeY += spikes.get(i).spikeVelocity;
            // check if bottop edge of the spike touch the top edge of the ground
            if (spikes.get(i).spikeY + spikes.get(i).getSpikeHeight() >= dHeight - ground.getHeight()){
                points += 10;
                // create an explosion object
                Explosion explosion = new Explosion(context);
                explosion.explosionX = spikes.get(i).spikeX;
                explosion.explosionY = spikes.get(i).spikeY;
                //add this explosion object to arraylist
                explosions.add(explosion);
                spikes.get(i).resetPosition();
            }
        }

        //
        for (int i=0; i<spikes.size(); i++){
            // check if spike right edge touch the rabbit left edge
            // && check if spike left edge touch the rabbit right edge
            // && check if spike bottom edge touch the top edge of the rabbit
            // && check if spike bottom edge is less than bottom of the rabbit
            if (spikes.get(i).spikeX + spikes.get(i).getSpikeWidth() >= rabbitX
            && spikes.get(i).spikeX <= rabbitX + rabbit.getWidth()
            && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() >= rabbitY
            && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() <= rabbitY + rabbit.getHeight()){
                life--;
                spikes.get(i).resetPosition();
                // when life is 0 create intent object put points into the intent object
                // launch game over activity and finish the current activity
                if (life == 0){
                    Intent intent = new Intent(context, GameOver.class);
                    intent.putExtra("points", points);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }
        }


        for(int i=0; i<explosions.size(); i++){
            // draw the explosions bitmap
            canvas.drawBitmap(explosions.get(i).getExplosion(explosions.get(i).explosionFrame), explosions.get(i).explosionX,
                    explosions.get(i).explosionY, null);
            // increment explosion frame for every element of explosions arraylist
            explosions.get(i).explosionFrame++;
            // once explosions become greater than 3 remove the explosion object from arraylist
            if (explosions.get(i).explosionFrame > 3){
                explosions.remove(i);
            }
        }

        // check the life
        if(life == 2){
            healthPaint.setColor(Color.YELLOW);
        } else if(life == 1){
            healthPaint.setColor(Color.RED);
        }

        //draw the health bar
        canvas.drawRect(dWidth-200, 30, dWidth-200+60*life, 80, healthPaint);
        // draw the points
//        canvas.drawText("" + points, 20, TEXT_SIZE, textPaint);
//        // my part
//        //textPaint.setColor(Color.BLUE);
        String levelText = "Level   " + level;
        canvas.drawText(levelText, 20, TEXT_SIZE, textPaint2);
        canvas.drawText("Life   "+life, 400, TEXT_SIZE, textPaint4);
        canvas.drawText(""+points, 20, 2*TEXT_SIZE, textPaint);
        canvas.drawText(maxEmotionIntervalText, 20, 3*TEXT_SIZE, textPaint3);

//
//        if (points == 1600 && level < 7) {
//            increaseLevel(6);
//        }
//        else if (points == 1200 && level < 7) {
//            increaseLevel(5);
//        }
//        else if (points == 800 && level < 7) {
//            increaseLevel(4);
//        }
//        else if (points == 500 && level < 7) {
//            increaseLevel(3);
//        }
//        else if (points == 300 && level < 7) {
//            increaseLevel(2);
//        } else if (points == 100 && level < 7) {
//            increaseLevel(1);
//        }

        if (consecutiveHappyDetections >= maxConsecutiveDetections && level < 5 && canIncreaseLevel) {

            canIncreaseLevel = false; // Prevent further calls until the delay is over
            levelChangeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    increaseLevel();
                    canIncreaseLevel = true; // Allow the method to be called again after the delay
                }
            }, LEVEL_CHANGE_DELAY);
        }

        // Decrease level if there are consecutive angry detections and the current level is greater than 1
        if ((consecutiveAngryDetections >= maxConsecutiveDetections || consecutiveSadDetections >= maxConsecutiveDetections) && level > 1 && canDecreaseLevel) {

            canDecreaseLevel = false; // Prevent further calls until the delay is over
            levelChangeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    decreaseLevel();
                    canDecreaseLevel = true; // Allow the method to be called again after the delay
                }
            }, LEVEL_CHANGE_DELAY);
        }



        // call postdelayed in handler object which will call the run method in runnable after 30 ms
        // in run method we call invalidate method which will call the on draw method for us and the view will be redrawn
        // it will again call the run method in runnable after 30ms whihch will again call invalidate
        // and this way we created an infinite game loop
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // for storing x y coordinates for touch point
        float touchX = event.getX();
        float touchY = event.getY();
        // we will consider only when touch is on or below the rabbit
        if (touchY >= rabbitY){
            // determine what kind of action is being performed by calling getAction method on motion event object and store in an int variable
            int action = event.getAction();
            // when you touch the scrrenn action_down fires up
            if(action == MotionEvent.ACTION_DOWN){
                // store x cordinate of x in oldx and store rabbitx in old rabbit x
                oldX = event.getX();
                oldRabbitX = rabbitX;
            }
            // check if the event is action_move
            // this event is rapidly fired up as long as the player is moving his her finger
            // here we move the rabbit as the same amount as the difference between the old touch and the new touch
            // but we wont let the rabbit go off the screen
            if( action == MotionEvent.ACTION_MOVE){
                float shift = oldX - touchX;
                float newRabbitX = oldRabbitX - shift;
                if (newRabbitX <= 0){
                    rabbitX = 0;
                }
                else if(newRabbitX >= dWidth - rabbit.getWidth())
                    rabbitX = dWidth - rabbit.getWidth();
                else rabbitX = newRabbitX;
            }
        }
        return true;
    }

//    public void increaseLevel(int lvl) {
//        if(lvl == level){
//            Toast.makeText(context, "Level is increased", Toast.LENGTH_SHORT).show();
//            level++;
//            // Update the number of spikes based on the level
//            Spike spike = new Spike(context);
//            spikes.add(spike);
//        }
//    }

    private void increaseLevel(){
            Toast.makeText(context, "Level is increased", Toast.LENGTH_SHORT).show();
            level++;
            // Update the number of spikes based on the level
            Spike spike = new Spike(context);
            spikes.add(spike);
        //onDrawCount1 = 0;
    }

    private void decreaseLevel(){
            Toast.makeText(context, "Level is decreased", Toast.LENGTH_SHORT).show();
            level--;
            // Update the number of spikes based on the level
            spikes.remove(1);
        //onDrawCount1 = 0;
    }

    public void updateMaxEmotionIntervalText(String emotion) {
        maxEmotionIntervalText = emotion;
    }

}

