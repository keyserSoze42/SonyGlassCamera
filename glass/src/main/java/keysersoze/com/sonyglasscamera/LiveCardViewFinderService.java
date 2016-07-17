package keysersoze.com.sonyglasscamera;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.keysersoze.sonyandroidlib.CameraConnectionController;
import com.keysersoze.sonyandroidlib.IsSupportedUtil;

import sony.sdk.cameraremote.ServerDevice;
import sony.sdk.cameraremote.SimpleRemoteApi;
import sony.sdk.cameraremote.SimpleSsdpClient;

/**
 * A {@link Service} that publishes a {@link LiveCard} in the timeline.
 */
public class LiveCardViewFinderService extends Service implements SurfaceHolder.Callback{

    private static final String LIVE_CARD_TAG = "LiveCardService";

    private LiveCard mLiveCard;

    private CameraConnectionController cameraConnectionController;
    private final String TAG = "LiveCardViewFinder";
    private static SimpleRemoteApi mRemoteApi;
    private SimpleSsdpClient ssdpClient;

    private ConnectionStatusRenderer connectionStatusrenderer;

    SimpleSsdpClient.SearchResultHandler searchResultHandler = new SimpleSsdpClient.SearchResultHandler() {
        @Override
        public void onDeviceFound(ServerDevice serverDevice) {
            mRemoteApi = SimpleRemoteApi.getInstance();
            mRemoteApi.init(serverDevice);
            cameraConnectionController.onDeviceFound(serverDevice);
        }

        @Override
        public void onFinished() {
            CameraConnectionController.openConnection();
        }

        @Override
        public void onErrorFinished() {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {

            final Context context = this.getBaseContext();
            connectionStatusrenderer = new ConnectionStatusRenderer(this);

            //final ViewFinderRenderer renderer = new ViewFinderRenderer(this);
            mLiveCard = new LiveCard(context, LIVE_CARD_TAG);
            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(connectionStatusrenderer);
            ssdpClient = new SimpleSsdpClient();
            CameraConnectionController.CameraConnectionHandler cameraConnectionHandler = new CameraConnectionController.CameraConnectionHandler() {
                @Override
                public void onCameraConnected() {
                    if (IsSupportedUtil.isCameraApiAvailable("startLiveview", cameraConnectionController.getApiSet())) {
                        Log.d(TAG, "openConnection(): LiveviewSurface.start()");
                        String liveViewUrl = cameraConnectionController.startLiveview();
                        connectionStatusrenderer.setConnected(true, liveViewUrl);
                    }
                }

                @Override
                public void onCameraReady() {

                }
            };
            ssdpClient.search(searchResultHandler);
            cameraConnectionController = new CameraConnectionController(getBaseContext(), cameraConnectionHandler);


            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.attach(this);
            mLiveCard.publish(PublishMode.REVEAL);

        } else {
            mLiveCard.navigate();
        }

        return START_STICKY;
    }

/*    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_CAMERA) {
            try {
                mRemoteApi.actTakePicture();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }*/


    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }

        if (connectionStatusrenderer != null) {
            connectionStatusrenderer.setConnected(false, null);
            connectionStatusrenderer = null;
            cameraConnectionController.stopLiveview();
            cameraConnectionController = null;
        }

        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
