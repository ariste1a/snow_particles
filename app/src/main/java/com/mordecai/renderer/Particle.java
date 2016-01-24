package com.mordecai.renderer;

/**
 * Created by Mordecai on 1/23/2016.
 */
public class Particle {
    public float[] velocity;
    public float acceleration;
    public float[] position;
    public float decay;
    public boolean isDead;
    public float lifeTime;

    public Particle(float[] position, float[] velocity, float acceleration, float decay, float lifeTime)
    {
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.decay = decay;
        this.lifeTime = lifeTime;
        this.isDead = false;
    }

    public void updateParticle(float time)
    {
        if(lifeTime <= time && !isDead)
        {
            this.position[0] += velocity[0];
            this.position[1] += velocity[1];
            this.position[2] += velocity[2];
            velocity[1] += acceleration;
        }
        else
            this.isDead = true;
    }
}
