package com.example.savethepig;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class Spike {
    //bitmap array for holding all spike images
    Bitmap[] spike = new Bitmap[3];
    int spikeFrame =0;
    int spikeX, spikeY, spikeVelocity;
    Random random;

    //constructor for spike class
    public Spike(Context context){
        spike[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.rocket0);
        spike[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.rocket1);
        spike[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.rocket2);
        random = new Random();
        // it will reset spikex spikey spikevelocity
        resetPosition();
    }

    //GETTER METHODS TO GET SPIKE, spike width and spike height
    public Bitmap getSpike(int spikeFrame){
        return spike[spikeFrame];
    }

    public int getSpikeWidth(){
        return spike[0].getWidth();
    }

    public int getSpikeHeight(){
        return spike[0].getHeight();
    }

    public void resetPosition(){
        // initialize with random values as shown
        spikeX = random.nextInt(GameView.dWidth - getSpikeWidth());
        spikeY = -200 + random.nextInt(600) * -1;
        spikeVelocity = 35 + random.nextInt(16);
    }
}
