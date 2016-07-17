package keysersoze.com.sonyglasscamera.layouts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by aaron on 7/10/16.
 */

public class ConnectionStatusLayout extends FrameLayout{

    private static final String TAG = ConnectionStatusLayout.class.getSimpleName();
    /**
     * The duration, in millisconds, of one frame.
     */
    private static final long FRAME_TIME_MILLIS = 40;

    /**
     * "Hello world" text size.
     */
    private static final float TEXT_SIZE = 70f;

    /**
     * Alpha variation per frame.
     */
    private static final int ALPHA_INCREMENT = 5;

    /**
     * Max alpha value.
     */
    private static final int MAX_ALPHA = 256;

    private Paint mPaint;
    private String mText;

    private int mCenterX;
    private int mCenterY;

    private SurfaceHolder mHolder;
    private boolean mRenderingPaused;

    private Thread mRenderThread;

    public ConnectionStatusLayout(Context context) {
        super(context);
        init();
    }

    public ConnectionStatusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConnectionStatusLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(TEXT_SIZE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        mPaint.setAlpha(0);
        mText = "Connecting";
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCenterX = width / 2;
        mCenterY = height / 2;
        Log.w(TAG, "surfaceChanged: X: " + Integer.toString(mCenterX) + " Y: " + Integer.toString(mCenterY));
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        mRenderingPaused = false;
        Log.w(TAG, "surfaceCreated");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        quit();
        mRenderThread = null;
        Log.w(TAG, "surfaceDestroyed");
    }


    public void renderingPaused(SurfaceHolder holder, boolean paused) {
        mRenderingPaused = paused;
        updateRenderingState();
    }

    /**
     * Starts or stops rendering according to the {@link LiveCard}'s state.
     */
    public void updateRenderingState(){

        mShouldRun = true;
        mRenderThread = getDrawThread();
        mRenderThread.start();
/*        boolean shouldRender = (mHolder != null) && !mRenderingPaused;
        boolean isRendering = (mRenderThread != null);

        if (shouldRender != isRendering) {
            if (shouldRender) {
                mShouldRun = true;
                mRenderThread = getDrawThread();
                mRenderThread.start();
            } else {
                quit();
                mRenderThread = null;
            }
        }*/
    }

    private boolean mShouldRun;

    /**
     * Returns true if the rendering thread should continue to run.
     *
     * @return true if the rendering thread should continue to run
     */
    private synchronized boolean shouldRun() {
        return mShouldRun;
    }

    /**
     * Requests that the rendering thread exit at the next opportunity.
     */
    public synchronized void quit() {
        mShouldRun = false;
    }

    public Thread getDrawThread(){
        Thread newDrawThread = null;
        if(mRenderThread == null) {
            newDrawThread = new Thread() {
                @Override
                public void run() {
                    while (shouldRun()) {
                        long frameStart = SystemClock.elapsedRealtime();
                        draw();
                        long frameLength = SystemClock.elapsedRealtime() - frameStart;

                        long sleepTime = FRAME_TIME_MILLIS - frameLength;
                        if (sleepTime > 0) {
                            SystemClock.sleep(sleepTime);
                        }
                    }
                }
            };
        }
        return newDrawThread;
    }

    /**
     * Draws the view in the SurfaceHolder's canvas.
     */
    private void draw() {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
            // Clear the canvas.
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            // Update the text alpha and draw the text on the canvas.
            mPaint.setAlpha((mPaint.getAlpha() + ALPHA_INCREMENT) % MAX_ALPHA);
            canvas.drawText(mText, mCenterX, mCenterY, mPaint);

            // Unlock the canvas and post the updates.
            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * Redraws the {@link View} in the background.
     */
    /*private class RenderThread extends Thread {
        private boolean mShouldRun;

        *//**
         * Initializes the background rendering thread.
         *//*
        public RenderThread() {
            mShouldRun = true;
        }

        *//**
         * Returns true if the rendering thread should continue to run.
         *
         * @return true if the rendering thread should continue to run
         *//*
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        *//**
         * Requests that the rendering thread exit at the next opportunity.
         *//*
        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run() {
            while (shouldRun()) {
                long frameStart = SystemClock.elapsedRealtime();
                draw();
                long frameLength = SystemClock.elapsedRealtime() - frameStart;

                long sleepTime = FRAME_TIME_MILLIS - frameLength;
                if (sleepTime > 0) {
                    SystemClock.sleep(sleepTime);
                }
            }
        }
    }*/
}
