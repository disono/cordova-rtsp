package com.webmons.disono.videostream;

import android.app.Activity;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: http://www.webmons.com
 * <p>
 * Created at: 2/27/2017
 */

public class RTSPStream extends CordovaPlugin {
    private final static int REQUEST_CODE = 10000;

    private CallbackContext callbackContext;
    private Activity activity;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // application context
        activity = cordova.getActivity();
        this.callbackContext = callbackContext;

        String url;
        PluginResult pluginResult;

        switch (action) {
            case "play":
                url = args.getString(0);
                _initializePlayer(url, null, 1);

                // Don't return any result now
                pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);

                return true;
            case "playWithMarquee":
                url = args.getString(0);
                String marqueeTextContent = args.getString(1);
                int marqueeTextSize = args.getInt(2);
                _initializePlayer(url, marqueeTextContent, marqueeTextSize);

                // Don't return any result now
                pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);

                return true;
            case "pause":
                _filters("pause");

                return true;
            case "resume":
                _filters("resume");

                return true;
            case "stop":
                _filters("stop");

                return true;
        }

        return false;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        _filters("pause");
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        _filters("resume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    private void _filters(String methodName) {
        Intent intent = new Intent();
        intent.setAction("com.webmons.disono.videostream.method");
        intent.putExtra("method", methodName);
        activity.sendBroadcast(intent);
    }

    /**
     * Initialize and play the video
     */
    private void _initializePlayer(final String uri, String marqueeText, int marqueeTextSize) {
        Intent i = new Intent(activity, RTSPActivity.class);
        i.putExtra("uri", uri);
        i.putExtra("marqueeText", marqueeText);
        i.putExtra("marqueeTextSize", marqueeTextSize);
        activity.startActivityForResult(i, REQUEST_CODE);
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
