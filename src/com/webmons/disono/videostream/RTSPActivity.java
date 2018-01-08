package com.webmons.disono.videostream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.cordova.CordovaActivity;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: http://www.webmons.com
 * <p>
 * Created at: 2/27/2017
 */

public class RTSPActivity extends CordovaActivity {
    private static final String TAG = "RTSPStream";

    private FullScreenVideoView videoView;
    BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String method = intent.getStringExtra("method");

                if (method != null) {
                    switch (method) {
                        case "pause":
                            _pause();
                            break;
                        case "resume":
                            _resume();
                            break;
                        case "stop":
                            _stop();
                            break;
                    }
                }
            }
        }
    };
    private ProgressBar spinner;
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.cordovaInterface.getActivity();

        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            _initializePlayer(bundle.getString("uri"), bundle.getString("marqueeText"),
                    bundle.getInt("marqueeTextSize"));
        }

        _broadcastRCV();
    }

    private void _broadcastRCV() {
        IntentFilter filter = new IntentFilter("com.webmons.disono.videostream.method");
        activity.registerReceiver(br, filter);
    }

    /**
     * Initialize and play the video
     */
    private void _initializePlayer(final String uri, String marqueeText, int marqueeTextSize) {
        try {
            _hideSystemUi();

            activity.runOnUiThread(() -> {
                Handler h = new Handler();
                h.post(() -> {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                    // set spinner
                    spinner = new ProgressBar(activity, null, android.R.attr.progressBarStyleLarge);
                    spinner.setVisibility(View.VISIBLE);

                    // video container layout
                    final RelativeLayout main = new RelativeLayout(activity);
                    RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout
                            .LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
                    relativeLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    main.setLayoutParams(relativeLayoutParams);

                    // moving text (Marquee)
                    TextView textView = new TextView(activity);
                    DisplayMetrics metrics;
                    metrics = getApplicationContext().getResources().getDisplayMetrics();
                    float textSize = 0;
                    if (marqueeText != null) {
                        textView.setLayoutParams(new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                        textView.setText(marqueeText);
                        textView.setFocusable(true);
                        textView.setFocusableInTouchMode(true);
                        textView.setSingleLine(true);
                        textView.setHorizontallyScrolling(true);
                        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                        textView.setLines(1);
                        textView.setMarqueeRepeatLimit(-1);
                        textView.setSelected(true);
                        textSize = textView.getTextSize() / metrics.density;
                        textView.setTextSize(textSize + marqueeTextSize);
                    }

                    // container for our progress bar
                    final LinearLayout progressBarContainer = new LinearLayout(activity);
                    progressBarContainer.setLayoutParams(new LinearLayout
                            .LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
                    progressBarContainer.setGravity(Gravity.CENTER);
                    progressBarContainer.setOrientation(LinearLayout.VERTICAL);

                    // initialize video view
                    videoView = new FullScreenVideoView(activity);
                    videoView.setVideoPath(uri);
                    videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                    videoView.requestFocus();
                    videoView.setZOrderOnTop(true);
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int textSizeMinus = (marqueeText != null) ? (marqueeTextSize + 28) : 0;
                    videoView.setLayoutParams(new FrameLayout.LayoutParams(WindowManager
                            .LayoutParams.MATCH_PARENT, displaymetrics.heightPixels - textSizeMinus));

                    // media controllers
                    final MediaController mediaController = new MediaController(activity);
                    mediaController.setMediaPlayer(videoView);
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);
                    videoView.setOnPreparedListener(mp -> {
                        Log.i(TAG, "Duration = " + videoView.getDuration());
                        spinner.setVisibility(View.GONE);
                        mediaController.show(2000);
                    });
                    videoView.setOnCompletionListener(mediaPlayer -> _sendResults("donePlaying", null));

                    // add surface holder
                    final SurfaceHolder mHolder = videoView.getHolder();
                    mHolder.setKeepScreenOn(true);
                    mHolder.addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder holder) {
                            _play();
                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder holder) {

                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                        }
                    });

                    // add the video and spinner progressbar to view
                    progressBarContainer.addView(spinner);
                    main.addView(progressBarContainer);
                    main.addView(videoView);
                    if (marqueeText != null) {
                        main.addView(textView);
                    }
                    activity.setContentView(main);
                });
            });
        } catch (Exception e) {
            spinner.setVisibility(View.GONE);
            _sendResults("error", e.getMessage());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        _pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        _resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Play video
     */
    public void _play() {
        if (videoView != null) {
            videoView.start();
        }
    }

    /**
     * Pause video
     */
    public void _pause() {
        if (videoView != null) {
            videoView.pause();
        }
    }

    /**
     * Resume video
     */
    public void _resume() {
        if (videoView != null) {
            videoView.resume();
        }
    }

    /**
     * Stop video
     */
    public void _stop() {
        if (videoView != null) {
            videoView.stopPlayback();
            unregisterReceiver(br);
            finish();
        }
    }

    /**
     * Send response type
     *
     * @param type
     * @param message
     */
    private void _sendResults(String type, String message) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("type", type);
        resultIntent.putExtra("message", message);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Hides StatusBar and NavigationBar
     */
    private void _hideSystemUi() {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);

        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
