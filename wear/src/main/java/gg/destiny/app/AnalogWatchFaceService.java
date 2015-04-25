package gg.destiny.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hayk on 4/11/2015.
 */
public class AnalogWatchFaceService extends CanvasWatchFaceService implements
        MessageApi.MessageListener,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "AnalogWatchFaceService";
    public static final String DATA_UPDATE_PATH = "/set_live";

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    Engine mEngine;
    GoogleApiClient mGoogleApiClient;

    AnalogWatchFaceService thisService;

    @Override
    public void onCreate(){
        super.onCreate();
        thisService = this;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addOnConnectionFailedListener(this)
        .addConnectionCallbacks(this)
        // Request access only to the Wearable API
        .addApi(Wearable.API)
        .build();
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onUnbind(Intent intent){
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        mEngine = new Engine();
        return mEngine;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        boolean i = false;
        Log.d(TAG, "Got Message!");
        if (messageEvent.getPath().equals(DATA_UPDATE_PATH)) {
            byte[] raw_bytes = messageEvent.getData();
            String raw_data = new String(raw_bytes);
            Log.d(TAG, String.valueOf(raw_bytes.length)+" Bytes of Raw Data: "+raw_data);
            handleNotification(raw_data);
        }
    }



    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(DATA_UPDATE_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    handleNotification(dataMap.getString("raw_notification_data"));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    public void handleNotification(String raw_notification_data){
        try{
            JSONObject status = new JSONObject(raw_notification_data);
            mEngine.isLive = status.getBoolean("is_live");
            Log.d("Got Live State!", mEngine.isLive+" == isLive?");
            if (!status.has("alert")){
                // vibrate in case there's no visual alert
                final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = { 0, 800 };
                v.vibrate( pattern, -1 );
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed: " + result);
    }

    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine {
        static final int MSG_UPDATE_TIME = 0;

        /* a time object */
        Time mTime;

        boolean mMute;

        /* graphic objects */
        Paint mHourPaint;
        Paint mMinutePaint;
        Paint mSecondPaint;
        Paint mTickPaint;

        /* handler to update the time once a second in interactive mode */
        final Handler mUpdateTimeHandler = new Handler() {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, "updating time");
                        }
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler
                                    .sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        /* receiver to update the time zone */
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        boolean mBurnInProtection;

        Bitmap mBackgroundBitmap;
        Bitmap liveBackgroundBitmap;
        Bitmap offlineBackgroundBitmap;
        Bitmap mBackgroundScaledBitmap;

        Bitmap minuteArm;
        Bitmap minuteArmScaled;

        Bitmap hourArm;
        Bitmap hourArmScaled;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            /* initialize your watch face */

            /* configure the system UI */
            setWatchFaceStyle(new WatchFaceStyle.Builder(AnalogWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle
                            .BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            /* load the background image */
            Resources resources = AnalogWatchFaceService.this.getResources();


            Drawable liveBackgroundDrawable = resources.getDrawable(R.drawable.le_ruse);
            liveBackgroundBitmap = ((BitmapDrawable) liveBackgroundDrawable).getBitmap();

            Drawable offlineBackgroundDrawable = resources.getDrawable(R.drawable.da_feels);
            offlineBackgroundBitmap = ((BitmapDrawable) offlineBackgroundDrawable).getBitmap();

            mBackgroundBitmap = offlineBackgroundBitmap;

            Drawable minuteArmDrawable = resources.getDrawable(R.drawable.long_arm);
            minuteArm = ((BitmapDrawable)minuteArmDrawable).getBitmap();

            Drawable hourArmDrawable = resources.getDrawable(R.drawable.short_arm);
            hourArm = ((BitmapDrawable)hourArmDrawable).getBitmap();

            /* create graphic styles */
            mHourPaint = new Paint();
            mHourPaint.setARGB(255, 200, 200, 200);
            mHourPaint.setStrokeWidth(5.0f);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);

            mMinutePaint = new Paint();
            mMinutePaint.setARGB(255, 200, 200, 200);
            mMinutePaint.setStrokeWidth(3.f);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);
            mMinutePaint.setShadowLayer(4.f, 0, 0, Color.BLACK);

            mSecondPaint = new Paint();
            mSecondPaint.setARGB(255, 255, 0, 0);
            mSecondPaint.setStrokeWidth(2.f);
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setStrokeCap(Paint.Cap.ROUND);

            mTickPaint = new Paint();
            mTickPaint.setARGB(100, 255, 255, 255);
            mTickPaint.setStrokeWidth(2.f);
            mTickPaint.setAntiAlias(true);

            /* allocate an object to hold the time */
            mTime = new Time();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            /* get device features (burn-in, low-bit ambient) */
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION,
                    false);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onPropertiesChanged: low-bit ambient = " + mLowBitAmbient);
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            /* the time changed */
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onTimeTick: ambient = " + isInAmbientMode());
            }
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            /* the wearable switched between modes */
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);
            }
            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mHourPaint.setAntiAlias(antiAlias);
                mMinutePaint.setAntiAlias(antiAlias);
                mSecondPaint.setAntiAlias(antiAlias);
                mTickPaint.setAntiAlias(antiAlias);
            }
            invalidate();
            updateTimer();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);
            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        boolean isLive = false;
        boolean lastLive = false;

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            /* draw your watch face */
            // Update the time
            mTime.setToNow();

            int width = bounds.width();
            int height = bounds.height();

            // if live changed. simulate with ambient mode.
            // TODO: implement true live checking via push notifications from parse

            if(lastLive != isLive){
                mBackgroundScaledBitmap = null;
                lastLive = isLive;
            }

            // Draw the background, scaled to fit.
            if (mBackgroundScaledBitmap == null
                    || mBackgroundScaledBitmap.getWidth() != width
                    || mBackgroundScaledBitmap.getHeight() != height) {
                // choose the right picture based on live/offline
                if(mBackgroundScaledBitmap == null){
                    mBackgroundBitmap = isLive ? liveBackgroundBitmap : offlineBackgroundBitmap;
                }

                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                        width, height, true /* filter */);
//
                int mx = Math.round(((float)width)*((float)minuteArm.getWidth())/((float)mBackgroundBitmap.getWidth()));
                int my = Math.round(((float)height)*((float)minuteArm.getHeight())/((float)mBackgroundBitmap.getHeight()));

                minuteArmScaled = Bitmap.createScaledBitmap(minuteArm, mx, my, true);

                int hx = Math.round(((float)width)*((float)hourArm.getWidth())/((float)mBackgroundBitmap.getWidth()));
                int hy = Math.round(((float)height)*((float)hourArm.getHeight())/((float)mBackgroundBitmap.getHeight()));

                hourArmScaled = Bitmap.createScaledBitmap(hourArm, hx, hy, true);
            }
            canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);

            // Find the center. Ignore the window insets so that, on round watches
            // with a "chin", the watch face is centered on the entire screen, not
            // just the usable portion.
            float centerX = width / 2f;
            float centerY = height / 2f;


            // Draw the ticks.
//            This code comes from the SDK samples, but not the documentation
//            try it out later

//            float innerTickRadius = centerX - 10;
//            float outerTickRadius = centerX;
//            for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
//                float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
//                float innerX = (float) Math.sin(tickRot) * innerTickRadius;
//                float innerY = (float) -Math.cos(tickRot) * innerTickRadius;
//                float outerX = (float) Math.sin(tickRot) * outerTickRadius;
//                float outerY = (float) -Math.cos(tickRot) * outerTickRadius;
//                canvas.drawLine(centerX + innerX, centerY + innerY,
//                        centerX + outerX, centerY + outerY, mTickPaint);
//            }



            // Compute rotations and lengths for the clock hands.
            int minutes = mTime.minute;
            float minRot = minutes / 30f * (float) Math.PI;
            float hrRot = ((mTime.hour + (minutes / 60f)) / 6f ) * (float) Math.PI;

            float secLength = centerX - 20;
//            float minLength = centerX - 40;
//            float hrLength = centerX - 80;

            // Only draw the second hand in interactive mode.
            if (!isInAmbientMode()) {
                float secRot = mTime.second / 30f * (float) Math.PI;
//                float secX = (float) Math.sin(secRot) * secLength;
//                float secY = (float) -Math.cos(secRot) * secLength;

                Matrix secondMatrix = new Matrix();
//                Log.d("Raw Second Arm Rotation", String.valueOf(secRot));
                float csr  = ((float) Math.toDegrees((double)secRot)-90.f);
//                Log.d("Comp. Second. Rotation", String.valueOf(csr));
                secondMatrix.postRotate(csr, 0f, (0.5f*(float)minuteArmScaled.getHeight()));
                secondMatrix.postTranslate(centerX, centerY-(0.5f*(float)minuteArmScaled.getHeight()));
                canvas.drawBitmap(minuteArmScaled, secondMatrix, null);

//                canvas.drawLine(centerX, centerY, centerX + secX, centerY +  secY, mSecondPaint);
            }

            // Draw the minute and hour hands.
//            float minX = (float) Math.sin(minRot) * minLength;
//            float minY = (float) -Math.cos(minRot) * minLength;
//            canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, mMinutePaint);

            // minute hand
            Matrix minuteMatrix = new Matrix();
//            Log.d("Raw Minute Arm Rotation", String.valueOf(minRot));
            float cmr  = ((float) Math.toDegrees((double)minRot)-90.f);
//            Log.d("Comp. Mint. Rotation", String.valueOf(cmr));
            minuteMatrix.postRotate(cmr, 0f, (0.5f*(float)minuteArmScaled.getHeight()));
            minuteMatrix.postTranslate(centerX, centerY-(0.5f*(float)minuteArmScaled.getHeight()));
            canvas.drawBitmap(minuteArmScaled, minuteMatrix, null);

            // hour hand
            Matrix hourMatrix = new Matrix();
//            Log.d("Raw Hour Arm Rotation", String.valueOf(hrRot));
            float chr = ((float) Math.toDegrees((double)hrRot)-90.f);
//            Log.d("Comp. Hour Rotation", String.valueOf(chr));
            hourMatrix.postRotate(chr, 0f, (0.5f*(float)hourArmScaled.getHeight()));
            hourMatrix.postTranslate(centerX, centerY-(0.5f*(float)hourArmScaled.getHeight()));
            canvas.drawBitmap(hourArmScaled, hourMatrix, null);

            Log.d("DUBS", "I drew them at "+mTime.toString());

//            float hrX = (float) Math.sin(hrRot) * hrLength;
//            float hrY = (float) -Math.cos(hrRot) * hrLength;
//            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mHourPaint);
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onVisibilityChanged: " + visible);
            }

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible and
            // whether we're in ambient mode), so we may need to start or stop the timer
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            AnalogWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            AnalogWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void updateTimer() {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "updateTimer");
            }
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }


//    helpers. move to another class when necessary

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
