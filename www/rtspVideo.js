var exec = require('cordova/exec');
var rtspVideo = {};

rtspVideo.play = function(uri, success, failure) {
    // fire
    exec(
        success,
        failure,
        'RTSPStream',
        'play',
        [uri]
    );
};

rtspVideo.playWithMarquee = function(uri, marqueeText, marqueeTextSize, success, failure) {
    marqueeText = (marqueeText) ? marqueeText : null;
    marqueeTextSize = (marqueeTextSize) ? marqueeTextSize : 1;

    // fire
    exec(
        success,
        failure,
        'RTSPStream',
        'playWithMarquee',
        [uri, marqueeText, marqueeTextSize]
    );
};

rtspVideo.pause = function(success, failure) {
    // fire
    exec(
        success,
        failure,
        'RTSPStream',
        'pause',
        []
    );
};

rtspVideo.resume = function(success, failure) {
    // fire
    exec(
        success,
        failure,
        'RTSPStream',
        'resume',
        []
    );
};

rtspVideo.stop = function(success, failure) {
    // fire
    exec(
        success,
        failure,
        'RTSPStream',
        'stop',
        []
    );
};

module.exports = rtspVideo;