package com.webmons.disono.videostream;

import android.app.Activity;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Author: Archie, Disono (disono.apd@gmail.com / webmonsph@gmail.com)
 * Website: http://www.webmons.com
 *
 * Created at: 2/27/2017
 */

public class RTSPStream extends CordovaPlugin {
    private final static int REQUEST_CODE = 10000;
    private RTSPActivity rtspView;

    private CallbackContext callbackContext;
    private Activity activity;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // application context
        activity = cordova.getActivity();
        this.callbackContext = callbackContext;

        if (action.equals("play")) {
            String url = args.getString(0);
            _initializePlayer(url);

            // Don't return any result now
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);

            return true;
        } else if (action.equals("pause")) {
            _pause();

            return true;
        } else if (action.equals("resume")) {
            _resume();

            return true;
        } else if (action.equals("stop")) {
            _stop();

            return true;
        }

        return false;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        _pause();
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        _resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _stop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent == null || requestCode != REQUEST_CODE) {
            return;
        }

        String responseType = intent.getStringExtra("type");
        String responseMsg = intent.getStringExtra("message");

        if (responseType.equals("error")) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, responseMsg);

            // release status callback in JS side
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
        }

        if (responseType.equals("donePlaying")) {
            _releaseActivity();
        }
    }

    /**
     * Initialize and play the video
     */
    private void _initializePlayer(final String uri) {
        rtspView = new RTSPActivity();

        Intent i = new Intent(activity, RTSPActivity.class);
        i.putExtra("uri", uri);
        activity.startActivityForResult(i, REQUEST_CODE);
    }

    /**
     * Pause video
     */
    private void _pause() {
        if (rtspView != null) {
            rtspView._pause();
        }
    }

    /**
     * Resume video
     */
    private void _resume() {
        if (rtspView != null) {
            rtspView._resume();
        }
    }

    /**
     * Stop video
     */
    private void _stop() {
        if (rtspView != null) {
            rtspView._stop();
            _releaseActivity();
        }
    }

    /**
     * Release activity
     */
    private void _releaseActivity() {
        // at last call sendPluginResult
        PluginResult result = new PluginResult(PluginResult.Status.OK);
        // release status callback in JS side
        result.setKeepCallback(false);
        callbackContext.sendPluginResult(result);
    }
}
