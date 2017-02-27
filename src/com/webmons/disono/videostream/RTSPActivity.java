package com.webmons.disono.videostream;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import org.apache.cordova.CordovaActivity;

/**
 * Author: Archie, Disono (disono.apd@gmail.com / webmonsph@gmail.com)
 * Website: http://www.webmons.com
 *
 * Created at: 2/27/2017
 */

public class RTSPActivity extends CordovaActivity {
    private static final String TAG = "RTSPStream";

    private VideoView videoView;
    private ProgressBar spinner;

    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.cordovaInterface.getActivity();

        _initializePlayer(getIntent().getExtras().getString("uri"));
    }

    /**
     * Initialize and play the video
     */
    private void _initializePlayer(final String uri) {
        try {
            _hideSystemUi();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Handler h = new Handler();
                    h.post(new Runnable() {
                        public void run() {
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                            // set spinner
                            spinner = new ProgressBar(activity, null, android.R.attr.progressBarStyleLarge);
                            spinner.setVisibility(View.VISIBLE);

                            // video container layout
                            final RelativeLayout main = new RelativeLayout(activity);
                            RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                            relativeLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            main.setLayoutParams(relativeLayoutParams);

                            // container for our progress bar
                            final LinearLayout progressBarContainer = new LinearLayout(activity);
                            progressBarContainer.setLayoutParams(new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
                            progressBarContainer.setGravity(Gravity.CENTER);
                            progressBarContainer.setOrientation(LinearLayout.VERTICAL);

                            // initialize video view
                            videoView = new VideoView(activity);
                            videoView.setVideoPath(uri);
                            videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                            videoView.requestFocus();
                            videoView.setLayoutParams(new RelativeLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));

                            // media controllers
                            final MediaController mediaController = new MediaController(activity);
                            mediaController.setMediaPlayer(videoView);
                            mediaController.setAnchorView(videoView);
                            videoView.setMediaController(mediaController);
                            videoView.setZOrderOnTop(true);
                            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    Log.i(TAG, "Duration = " + videoView.getDuration());
                                    spinner.setVisibility(View.GONE);
                                    mediaController.show(2000);
                                }
                            });
                            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    _sendResults("donePlaying", null);
                                }
                            });

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
                                    _stop();
                                }

                                @Override
                                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                                }
                            });

                            // add the video and spinner progressbar to view
                            progressBarContainer.addView(spinner);
                            main.addView(videoView);
                            main.addView(progressBarContainer);
                            activity.setContentView(main);
                        }
                    });
                }
            });
        } catch (Exception e) {
            spinner.setVisibility(View.GONE);
            _sendResults("error", e.getMessage());
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
        _stop();
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

        if (Build.VERSION.SDK_INT < 19) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
