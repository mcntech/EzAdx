function create_ezadx(vjs_player) {
    var player = vjs_player;
    
    var adserverUrl = "http://www.onyxvideo.com/revive/www/delivery/fc.php?script=bannerTypeHtml:vastInlineBannerTypeHtml:vastInlineHtml&zones=pre-roll:0.0-0%3D1&nz=4&source=&r=R0.8214839450083673&block=0&format=vast&charset=UTF-8";
    var STRM_TYPE_AD = "ad",
      STRM_TYPE_MAIN = "main",
      STRM_TYPE_NONE = "none"
    CONTENT_TYPE_EZADX = "application/x-ezadx",
      crntStrmType = STRM_TYPE_NONE,
      enableMainStrm = false,
      enableAdStrm = true,
      playposMainStrm = 0,
      adwaitStart = -1;

    var mainstreamIsLive = false;
 

    // TODO: Remove
    //cast cast.receiver.logger.setLevelValue(cast.receiver.LoggerLevel.DEBUG);
    //cast cast.player.api.setLoggerLevel(cast.player.api.LoggerLevel.DEBUG);

    //var mediaElement = document.getElementById('vid');

    var onTimerUpdate = function () {
        console.log("Time: " + player.currentTime());
        if (crntStrmType == STRM_TYPE_MAIN && enableAdStrm && enableMainStrm && player.currentTime() > 1) {
            if (adwaitStart == -1)
                adwaitStart = player.currentTime();
            if (player.currentTime() - adwaitStart >= adInterval) {
                // Stop stream
                console.log("Insert Ad: ");
                playposMainStrm = player.currentTime();
                adwaitStart = -1; // Reset
                //cast window.mediaManager.resetMediaElement();
                // todo
                // cast window.mediaManager.onEnded();
                // todo
            }
        }
    },

    
    // Create the media manager. This will handle all media messages by default.
    //cast window.mediaManager = new cast.receiver.MediaManager(mediaElement);
    // TODO
    onEnded = function () {
        if (crntStrmType == STRM_TYPE_MAIN) {
            // Main Stream complete or stopped
            if (enableAdStrm) {
                console.log("Switching to ad");
                crntStrmType = STRM_TYPE_AD;
                getNextMediaUrl(adserverUrl, onNextUrl);
            } else {
                console.log("Looping video:" + mainstreamUrl);
                PlayNextMainStream();
            }
            if (mainstreamIsLive !== true && player.currentTime() >= player.duration() - 0.25) {
                playposMainStrm = 0;
            }
        } else if (crntStrmType == STRM_TYPE_AD) {
            // Ad complete. If main stream is set, play it else play next ad

            if (enableMainStrm) {
                console.log("Restarting Main");
                crntStrmType = STRM_TYPE_MAIN;
                ContinueMainStream();
            } else {
                console.log("Starting next Ad");
                getNextMediaUrl(adserverUrl, onNextUrl);
            }
        } else {
            console.log("End of Stream");
            //cast window.defaultOnEnded();  // call the default behavior  
        }
    },

    //cast window.defaultOnEnded = window.mediaManager.onEnded;  // grab the default
    // todo:
    // cast window.mediaManager.onEnded = window.onEnded;

    PlayNextMainStream = function () {
        if (mainstreamContentType === CONTENT_TYPE_EZADX) {
            getNextMediaUrl(mainstreamUrl, onNextUrl);
        } else {
            mainstreamMediaUrl = mainstreamUrl;
            onNextUrl(mainstreamMediaUrl);
        }
    },

    ContinueMainStream = function () {
        onNextUrl(mainstreamMediaUrl);
    },

    onNextUrl = function (nextUrl) {

        console.log('Starting media application');
        var url = nextUrl;
        console.log('url :' + url);
        crntUrl = url;
        
        if (crntStrmType == STRM_TYPE_AD) {
            adMediaUrl = url;
        } else {
            mainstreamMediaUrl = url;
        }

        var ext = url.substring(url.lastIndexOf('.'), url.length);
        var initStart = 0; // TODO Get from adserver Query
        var autoplay = true;

        //player.autoplay(true);  // Make sure autoplay get's set

        if (crntStrmType == STRM_TYPE_MAIN) {
            if (mainstreamIsLive)
                initStart = Infinity;
            else
                initStart = playposMainStrm;
        }
        if (url.lastIndexOf('.m3u8') >= 0) {
            // HTTP Live Streaming
            player.src({ type: 'video/mp4', src: url});
        } else if (url.lastIndexOf('.mpd') >= 0) {
            // MPEG-DASH
            // cast protocol = cast.player.api.CreateDashStreamingProtocol(host);
            // todo
        }
        console.log("we have protocol " + ext);
    },

    // Remember the default value for the Receiver onLoad, so this sample can Play
    // non-adaptive media as well.  
    // cast window.defaultOnLoad = mediaManager.onLoad;
    // todo:
    
    //console.log('Application is ready, starting system');
    //cast window.castReceiverManager = cast.receiver.CastReceiverManager.getInstance();

    onError = function(){
        console.log("!!!!!!!!!!!!!!!! Player Error !!!!!!!!!!!!");
        onEnded();
    },
    
    onEzadxMessage = function (e) {
        var message = JSON.parse(e.data),
          channel = e.target;

        console.log('Event [' + e.senderId + ']: ' + e.data);
        console.debug('Message ', JSON.stringify(message));

        switch (message.command) {
            case "load":
                adserverUrl = message["adUrl"];
                console.log('adserverUrl :' + adserverUrl);
                mainstreamUrl = message["mainstreamUrl"];
                mainstreamContentType = message["mainstreamContetType"];
                adContentType = message["adContentType"];
                adInterval = message["adInterval"];
                mainstreamIsLive = message["mainstreamIsLive"];

                if (adserverUrl && adserverUrl.length && adserverUrl != "none")
                    enableAdStrm = true;
                else
                    enableAdStrm = false;

                if (mainstreamUrl && mainstreamUrl.length && mainstreamUrl != "none")
                    enableMainStrm = true;
                else
                    enableMainStrm = false;

                if (enableMainStrm) {
                    crntStrmType = STRM_TYPE_MAIN;
                    PlayNextMainStream();
                } else if (enableAdStrm) {
                    crntStrmType = STRM_TYPE_AD;
                    getNextMediaUrl(adserverUrl, onNextUrl);
                }
                break;
        }
    };

    // create a CastMessageBus to handle messages for a custom namespace
    //cast window.messageBus = window.castReceiverManager.getCastMessageBus(NAMESPACE);

    //cast window.messageBus.addEventListener("message", window.onEzadxMessage.bind(this));
    //window.messageBus.onMessage = function(event) {
    //    console.log('Message [' + event.senderId + ']: ' + event.data);
    //    window.messageBus.send(event.senderId, event.data);
    //}

    //cast castReceiverManager.start();
    /*

    */
    return {
        init : function() {
            player.on('timeupdate', onTimerUpdate);
            player.on('error', onError)
            crntStrmType = STRM_TYPE_AD;
            player.on("ended", onEnded);
            //player.requestFullscreen();
            player.on("play", function () {
                this.requestFullScreen();
            });
            //player.controls(false);
            player.autoplay(true);
        },        
        start : function() {
            player.on('timeupdate', onTimerUpdate);
            crntStrmType = STRM_TYPE_AD;
            getNextMediaUrl(adserverUrl, onNextUrl);
            //player.src({ type: 'video/mp4', src: "https://s3.amazonaws.com/ezadx/ubuntu/regularad_Ad1/regularad_Ad1.m3u8"});
            //player.play();
        }
    };
};
