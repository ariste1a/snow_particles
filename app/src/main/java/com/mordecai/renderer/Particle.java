package com.mordecai.renderer;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Mordecai on 1/23/2016.
 */
public class Particle {
    public float[] velocity;
    public float[] acceleration;
    public float[] position;
    public float timeLived;
    public boolean isDead;
    public float lifeTime;
    public float floor;

    public Particle(float[] position, float[] velocity, float[] acceleration, float decay, float lifeTime)
    {
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.timeLived = decay;
        this.lifeTime = lifeTime;
        this.isDead = false;
        this.floor = -1f;
    }

    public void updateParticle(float time)
    {
        if(timeLived <= lifeTime)
        {
            if(this.position[1] > floor)
            {
                this.position[0] += velocity[0];
                this.position[1] += velocity[1];
                this.position[2] += velocity[2];
                velocity[0] += acceleration[0] * time;
                velocity[1] += acceleration[1] * time;
            }
            timeLived += time;
        }
        else
            this.isDead = true;
    }
}
