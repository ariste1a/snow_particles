package com.mordecai.renderer;

import java.util.Random;

/**
 * Created by Mordecai on 1/23/2016.
 */
public class ParticleSystem
{
    int numParticles;
    Particle[] particles;

    public ParticleSystem(int numParticles)
    {
        float[] position;
        float[] velocity;
        float acceleration = -0.8f;
        float decay = 0.5f;
        float lifeTime = 0.5f;
        Random rand = new Random();
        for(int i = 0; i < numParticles; i++)
        {
            position = new float[3];
            velocity  = new float[]{0,0,0};
            decay = rand.nextFloat()/1000.0f+0.003f;
            position[0] = rand.nextFloat()*10 - 10;
            position[1] = 10;
            position[2] = rand.nextFloat()*10 - 10;

            velocity[0] = 0;
            velocity[1] = 0;
            velocity[2] = 0; 
            particles[i] = new Particle(position, velocity, acceleration,  decay,  lifeTime);
        }
    }

    public void update(float time)
    {
        for(Particle particle: particles)
        {
            particle.updateParticle(time);
        }
    }
}
