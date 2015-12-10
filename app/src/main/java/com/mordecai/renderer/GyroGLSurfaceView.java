package com.mordecai.renderer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by rigby on 12/3/2015.
 */
public class GyroGLSurfaceView extends GLSurfaceView{
    private Renderer mRenderer;

    public GyroGLSurfaceView(Context context)
    {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event != null)
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                if (mRenderer != null)
                {
                    // Ensure we call switchMode() on the OpenGL thread.
                    // queueEvent() is a method of GLSurfaceView that will do this for us.
                    queueEvent(new Runnable()
                    {
                        @Override
                        public void run()
                        {

                        }
                    });

                    return true;
                }
            }
        }

        return super.onTouchEvent(event);
    }

    // Hides superclass method.
    public void setRenderer(Renderer renderer)
    {
        mRenderer = renderer;
        super.setRenderer(renderer);
    }
}
