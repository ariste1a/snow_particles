package com.mordecai.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import android.util.Log;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Mordecai on 10/17/2015.
 * http://opengles-book-samples.googlecode.com/svn/!svn/bc/49/trunk/Android/Ch13_ParticleSystem/src/com/openglesbook/particlesystem/ParticleSystemRenderer.java
 */
public class Renderer implements GLSurfaceView.Renderer {
    /**
     * Used for debug logs.
     */
    private static final String TAG = "LessonFiveRenderer";

    private final Context mActivityContext;

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D viewport.
     */
    private float[] mProjectionMatrix = new float[16];

    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private float[] mMVPMatrix = new float[16];

    /**
     * This will be used to pass in the transformation matrix.
     */
    private int mMVPMatrixHandle;

    /**
     * This will be used to pass in model position information.
     */
    private int mPositionHandle;

    /**
     * This will be used to pass in model color information.
     */
    private int mColorHandle;

    /**
     * How many bytes per float.
     */
    private final int mBytesPerFloat = 4;

    /**
     * Size of the position data in elements.
     */
    private final int mPositionDataSize = 3;

    /**
     * Size of the color data in elements.
     */
    private final int mColorDataSize = 4;

    /**
     * This is a handle to our cube shading program.
     */
    private int mProgramHandle;

    // Attribute locations
    private int mLifetimeLoc;
    private int mStartPositionLoc;
    private int mEndPositionLoc;

    // Uniform location
    private int mTimeLoc;
    private int mColorLoc;
    private int mVelocityLoc;
    private int mSamplerLoc;
    private int mCenterPositionLoc;

    // Texture handle
    private int mTextureId;

    // Update time
    private float mTime;
    private long mLastTime;

    // Additional member variables
    private int mWidth;
    private int mHeight;
    private FloatBuffer mParticles;

    private final int NUM_PARTICLES = 5000;
    private final int PARTICLE_SIZE = 7;

    private final float[] mParticleData = new float[NUM_PARTICLES * PARTICLE_SIZE];
    private SensorManager senSensorManager;
    private Sensor sensor;
    private ParticleSystem snow;

    /**
     * Initialize the model data.
     */
    public Renderer(final Context activityContext) {
        mActivityContext = activityContext;

    }

    protected String getVertexShader() {
        return RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.snow_vertex_shader);
    }

    protected String getFragmentShader() {
        return RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.snow_fragment_shader);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        
        //Set the background clear color to black.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

         //No culling of back faces
        GLES20.glDisable(GLES20.GL_CULL_FACE);

         //No depth testing
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

         //Enable blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        final String vertexShader = getVertexShader();
        final String fragmentShader = getFragmentShader();

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        snow = new ParticleSystem(NUM_PARTICLES);

        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, new String[]{"a_Position", "a_Color"});

        // Get the attribute locations
        mLifetimeLoc = GLES20.glGetAttribLocation(mProgramHandle, "a_lifetime");
        mStartPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "a_startPosition");
        mEndPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "a_endPosition");

        // Get the uniform locations
        mTimeLoc = GLES20.glGetUniformLocation(mProgramHandle, "u_time");
        mColorLoc = GLES20.glGetUniformLocation(mProgramHandle, "u_color");
        mSamplerLoc = GLES20.glGetUniformLocation(mProgramHandle, "s_texture");
        mVelocityLoc = GLES20.glGetUniformLocation(mProgramHandle, "u_velocity");
        mCenterPositionLoc = GLES20.glGetUniformLocation(mProgramHandle, "u_centerPosition");
        SensorManager senSensorManager = (SensorManager) mActivityContext.getSystemService(Context.SENSOR_SERVICE);
        sensor = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(listener, senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        // Fill in particle data array
        Random generator = new Random();

        for (int i = 0; i < NUM_PARTICLES; i++) {
            // Lifetime of particle
            mParticleData[i * 7 + 0] = generator.nextFloat();

            // End position of particle
            mParticleData[i * 7 + 1] = snow.particles[i].position[0];//generator.nextFloat() * 2.0f - 1.0f;
            mParticleData[i * 7 + 2] = snow.particles[i].position[1];//generator.nextFloat() * 2.0f - 1.0f;
            mParticleData[i * 7 + 3] = snow.particles[i].position[2];//generator.nextFloat() * 2.0f - 1.0f;

            // Start position of particle
            mParticleData[i * 7 + 4] = generator.nextFloat() * 0.25f - 0.125f;
            mParticleData[i * 7 + 5] = generator.nextFloat() * 0.25f - 0.125f;
            mParticleData[i * 7 + 6] = generator.nextFloat() * 0.25f - 0.125f;
        }

        mParticles = ByteBuffer.allocateDirect(mParticleData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mParticles.put(mParticleData).position(0);

        // Initialize time to cause reset on first update
        mTime = 1.0f;

        mTextureId = loadTexture(mActivityContext.getResources().openRawResource(R.raw.snowflake));
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;
        mWidth = width;
        mHeight = height;
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

        mWidth = width;
        mHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        update();
        // Set the viewport
        GLES20.glViewport(0, 0, mWidth, mHeight);

        // Clear the color buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Use the program object
        GLES20.glUseProgram(mProgramHandle);

        // Load the vertex attributes
        mParticles.position(0);
        GLES20.glVertexAttribPointer(mLifetimeLoc, 1, GLES20.GL_FLOAT,
                false, PARTICLE_SIZE * 4,
                mParticles);

        mParticles.position(1);
        GLES20.glVertexAttribPointer(mEndPositionLoc, 3, GLES20.GL_FLOAT,
                false, PARTICLE_SIZE * 4,
                mParticles);

        mParticles.position(4);
        GLES20.glVertexAttribPointer(mStartPositionLoc, 3, GLES20.GL_FLOAT,
                false, PARTICLE_SIZE * 4,
                mParticles);


        GLES20.glEnableVertexAttribArray(mLifetimeLoc);
        GLES20.glEnableVertexAttribArray(mEndPositionLoc);
        GLES20.glEnableVertexAttribArray(mStartPositionLoc);

        // Blend particles
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);

        // Bind the texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        mParticles = ByteBuffer.allocateDirect(mParticleData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mParticles.put(mParticleData).position(0);

        // Set the sampler texture unit to 0
        GLES20.glUniform1i(mSamplerLoc, 0);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, NUM_PARTICLES);

    }

    private void update() {
        if (mLastTime == 0)
            mLastTime = SystemClock.uptimeMillis();
        long curTime = SystemClock.uptimeMillis();
        long elapsedTime = curTime - mLastTime;
        float deltaTime = elapsedTime / 1000.0f;
        mLastTime = curTime;

        mTime += deltaTime;

        if (mTime >= 1.0f) {
            Random generator = new Random();
            float[] centerPos = new float[3];
            float[] color = new float[4];

            mTime = 0.0f;

            // Pick a new start location and color
//            centerPos[0] = generator.nextFloat() * 1.0f - 0.5f;
//            centerPos[1] = generator.nextFloat() * 1.0f - 0.5f;
//            centerPos[2] = generator.nextFloat() * 1.0f - 0.5f;

            //GLES20.glUniform3f ( mCenterPositionLoc, centerPos[0], centerPos[1], centerPos[2]);

            // Random color
            color[0] = generator.nextFloat() * 0.5f + 0.5f;
            color[1] = generator.nextFloat() * 0.5f + 0.5f;
            color[2] = generator.nextFloat() * 0.5f + 0.5f;
            color[3] = 0.5f;

            GLES20.glUniform4f(mColorLoc, color[0], color[1], color[2], color[3]);
        }

        snow.update(deltaTime);
        for (int i = 0; i < NUM_PARTICLES; i++) {
            mParticleData[i * 7 + 0] = snow.particles[i].timeLived;
            mParticleData[i * 7 + 1] = snow.particles[i].position[0];
            mParticleData[i * 7 + 2] = snow.particles[i].position[1];
            mParticleData[i * 7 + 3] = snow.particles[i].position[2];
        }

        // Load uniform time variable
        GLES20.glUniform1f(mTimeLoc, mTime);
    }

    ///
    //  Load texture from resource
    //
    private int loadTexture(InputStream is) {
        int[] textureId = new int[1];
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeStream(is);
        byte[] buffer = new byte[bitmap.getWidth() * bitmap.getHeight() * 3];

        for (int y = 0; y < bitmap.getHeight(); y++)
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int pixel = bitmap.getPixel(x, y);
                buffer[(y * bitmap.getWidth() + x) * 3 + 0] = (byte) ((pixel >> 16) & 0xFF);
                buffer[(y * bitmap.getWidth() + x) * 3 + 1] = (byte) ((pixel >> 8) & 0xFF);
                buffer[(y * bitmap.getWidth() + x) * 3 + 2] = (byte) ((pixel >> 0) & 0xFF);
            }

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bitmap.getWidth() * bitmap.getHeight() * 3);
        byteBuffer.put(buffer).position(0);

        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, bitmap.getWidth(), bitmap.getHeight(), 0,
                GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, byteBuffer);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        return textureId[0];
    }

    private SensorEventListener listener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent e) {
            if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                double netForce = e.values[0] * e.values[0];

                netForce += e.values[1] * e.values[1];
                netForce += e.values[2] * e.values[2];

                snow.tilt = e.values;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // unused
        }
    };
}

