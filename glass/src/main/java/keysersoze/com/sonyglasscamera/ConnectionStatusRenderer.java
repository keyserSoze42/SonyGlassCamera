package keysersoze.com.sonyglasscamera;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.glass.timeline.DirectRenderingCallback;
import com.keysersoze.sonyandroidlib.ViewFinderLayout;

import keysersoze.com.sonyglasscamera.layouts.ConnectionStatusLayout;

/**
 * Created by aaron on 7/10/16.
 */

public class ConnectionStatusRenderer implements DirectRenderingCallback{

    private final ConnectionStatusLayout connectionStatusView;
    private final ViewFinderLayout viewFinderView;
    private boolean isConnected = false;
    private boolean isLiveViewStreaming = false;
    private String streamUrl = null;
    private final Context context;

    private String TAG = "ConnectionStatusRenderer";

    public ConnectionStatusRenderer(Context context) {
        this.context = context;
        connectionStatusView = new ConnectionStatusLayout(context);
        viewFinderView = new ViewFinderLayout(context);
        viewFinderView.setStreamErrorListener(new ViewFinderLayout.StreamErrorListener() {
            @Override
            public void onError(ViewFinderLayout.StreamErrorListener.StreamErrorReason streamErrorReason) {
                viewFinderView.stop();
            }
        });

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.w(TAG, "surfaceDestroyed");
        connectionStatusView.surfaceChanged(holder, format, width, height);
        viewFinderView.surfaceChanged(holder, format, width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.w(TAG, "surfaceDestroyed");
        connectionStatusView.surfaceCreated(holder);
        viewFinderView.surfaceCreated(holder);

        updateRenderingState();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.w(TAG, "surfaceDestroyed");
        connectionStatusView.surfaceDestroyed(holder);
        viewFinderView.surfaceDestroyed(holder);

        //updateRenderingState();
    }

    @Override
    public void renderingPaused(SurfaceHolder holder, boolean paused) {
        if(!isConnected) {
            connectionStatusView.renderingPaused(holder, paused);
        }
        updateRenderingState();
    }

    protected void setStreamUrl(String streamUrl){
        this.streamUrl = streamUrl;
    }

    /**
     * Starts or stops rendering according to the {@link LiveCard}'s state.
     */
    private void updateRenderingState(){
        Log.d(TAG, "ConnectionRenderer: updateRenderingState.start()");

        if(!isConnected) {
            connectionStatusView.updateRenderingState();
        }else {
            connectionStatusView.quit();
            if(streamUrl != null && !viewFinderView.isStarted()){
                Log.d(TAG, "ConnectionRenderer: starting stream");
                viewFinderView.setStreamUrl(streamUrl);
                viewFinderView.updateRenderingState();
            }
        }
    }

    protected void setConnected(boolean newConnectStatus, String streamUrl){
        if(newConnectStatus != isConnected && newConnectStatus == true) {
            setStreamUrl(streamUrl);
            isConnected = newConnectStatus;
            updateRenderingState();
        }else {
            isConnected = newConnectStatus;
            connectionStatusView.quit();
            viewFinderView.stop();
        }
    }

/*    protected void setLiveViewFinderView(ViewFinderFrameLayout simpleStreamSurfaceView) {
        viewFinderView = simpleStreamSurfaceView;
    }

    protected void setLiveViewStreaming(boolean newLiveViewStatus){
        isLiveViewStreaming = newLiveViewStatus;
    }*/
}
