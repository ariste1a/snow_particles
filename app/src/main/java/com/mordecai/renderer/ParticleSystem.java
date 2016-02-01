package com.mordecai.renderer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Random;

/**
 * Created by Mordecai on 1/23/2016.
 */
public class ParticleSystem
{
    private int numParticles;
    Particle[] particles;
    private static final float LIFETIME = 3.0f;
    public float[] tilt;
    public ParticleSystem(int numParticles)
    {
        float[] position;
        float[] velocity;
        float[] acceleration = new float[]{0, 0, 0};
        float lifeTime = LIFETIME;
        Random rand = new Random();
        this.numParticles = numParticles;
        particles = new Particle[numParticles];
        Random generator = new Random();
        for(int i = 0; i < numParticles; i++)
        {
            position = new float[3];
            velocity  = new float[]{0,0,0};
            float decay = rand.nextFloat()/1000.0f+0.003f;
            position[0] = generator.nextFloat() * 2.0f - 1.0f;;//rand.nextFloat()*10 - 10;
            position[1] = -5;
            position[2] = generator.nextFloat() * 2.0f - 1.0f;;//rand.nextFloat()*10 - 10;

            velocity[0] = 0;
            velocity[1] = 0;
            velocity[2] = 0; 
            particles[i] = new Particle(position, velocity, acceleration,  decay,  lifeTime);
        }
    }

    public void update(float time)
    {
        float tiltX = -(tilt[0]/1000);
        float[] acceleration = new float[]{tiltX, -0.001f, 0};
        for(int i = 0; i < numParticles; i++)
        {
            float[] position;
            float[] velocity;
            Random rand = new Random();
            float lifeTime = rand.nextFloat()*10 + 2;
            particles[i].updateParticle(time);
            particles[i].velocity[0] = tiltX;
            if(particles[i].isDead)
            {
                position = new float[3];
                //float startingVelocity = Math.abs(rand.nextFloat() - 0.1f);
                velocity  = new float[]{0,-0.01f,0};
                float decay = rand.nextFloat()/1000.0f+0.003f;
                position[0] = rand.nextFloat()*2f - 1.0f;
                position[1] = rand.nextFloat()+0.5f;
                position[2] = rand.nextFloat()*10 - 10;

                particles[i]= new Particle(position, velocity, acceleration,  decay,  lifeTime);
            }
        }
    }
}
