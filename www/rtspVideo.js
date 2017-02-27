'use strict';

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