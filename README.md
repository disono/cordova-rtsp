# Cordova RTSP Player
Cordova RTSP Video Player (Simple)

# Installation
Latest stable version from npm:
```sh
$ cordova plugin add cordova-rtsp
```
Bleeding edge version from Github:
```sh
$ cordova plugin add https://github.com/disono/cordova-rtsp
```

# Using the plugin
Call play with video URL (RTSP) as argument. The video player will close after the video is completed playing.
```sh
rtspVideo.play('rtsp://your-ip/file.mp4', [successCallback], [failureCallback]);
```
Stop and close the video player
```sh
rtspVideo.stop();
```

# Example
```sh
rtspVideo.play('rtsp://your-ip/file.mp4', function () {
    console.log('Done Playing.');
}, function (e) {
    console.error('Error: ' + e);
});
```

# License
Cordova RTSP Video Player (Simple) is licensed under the Apache License (ASL) license. For more information, see the LICENSE file in this repository.