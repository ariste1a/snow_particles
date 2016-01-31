package com.mordecai.renderer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.mordecai.renderer.Renderer;

public class MainActivity extends Activity {

    /** Hold a reference to our GLSurfaceView */
    private GLSurfaceView mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // Turn off the window's title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mGLSurfaceView = new GLSurfaceView(this);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2)
        {
            // Request an OpenGL ES 2.0 compatible context.
            mGLSurfaceView.setEGLContextClientVersion(2);

            // Set the renderer to our demo renderer, defined below.
            mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            mGLSurfaceView.setBackgroundResource(R.drawable.my_background);
            mGLSurfaceView.setZOrderOnTop(true);
            mGLSurfaceView.setRenderer(new Renderer(this));
        }
        else
        {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            Log.e("ParticleSystem", "OpenGL ES 2.0 not supported on device.  Exiting...");
            return;
        }

        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume()
    {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        mGLSurfaceView.onPause();
    }

}
