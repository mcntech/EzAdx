window.onload = function () {
    var NAMESPACE = "urn:x-cast:org.dashif.dashjs";
    var STRM_TYPE_AD = "ad",
      STRM_TYPE_MAIN = "main",
      STRM_TYPE_NONE = "none"
    CONTENT_TYPE_EZADX = "application/x-ezadx",
      crntStrmType = STRM_TYPE_NONE,
      enableMainStrm = false,
      enableAdStrm = false,
      playposMainStrm = 0,
      adwaitStart = -1;

    window.mainstreamIsLive = false;
    // If you set ?Debug=true in the URL, such as a different App ID in the
    // developer console, include debugging information.
    if (window.location.href.indexOf('Debug=true') != -1) {
        cast.receiver.logger.setLevelValue(cast.receiver.LoggerLevel.DEBUG);
        cast.player.api.setLoggerLevel(cast.player.api.LoggerLevel.DEBUG);
    }

    // TODO: Remove
    cast.receiver.logger.setLevelValue(cast.receiver.LoggerLevel.DEBUG);
    cast.player.api.setLoggerLevel(cast.player.api.LoggerLevel.DEBUG);

    var mediaElement = document.getElementById('vid');

    onTimerUpdate = function () {
        console.log("Time: " + mediaElement.currentTime);
        if (crntStrmType == STRM_TYPE_MAIN && enableAdStrm && enableMainStrm && mediaElement.currentTime > 1) {
            if (adwaitStart == -1)
                adwaitStart = mediaElement.currentTime;
            if (mediaElement.currentTime - adwaitStart >= window.adInterval) {
                // Stop stream
                console.log("Insert Ad: ");
                playposMainStrm = mediaElement.currentTime;
                adwaitStart = -1; // Reset
                window.mediaManager.resetMediaElement();
                window.mediaManager.onEnded();
            }
        }
    }

    mediaElement.addEventListener('timeupdate', onTimerUpdate, false);
    // Create the media manager. This will handle all media messages by default.
    window.mediaManager = new cast.receiver.MediaManager(mediaElement);

    window.onEnded = function () {
        if (crntStrmType == STRM_TYPE_MAIN) {
            // Main Stream complete or stopped
            if (enableAdStrm) {
                console.log("Switching to ad");
                crntStrmType = STRM_TYPE_AD;
                getNextMediaUrl(window.adserverUrl, window.onNextUrl);
            } else {
                console.log("Looping video:" + window.mainstreamUrl);
                PlayNextMainStream();
            }
            if (window.mainstreamIsLive !== true && mediaElement.currentTime >= mediaElement.duration - 0.25) {
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
                getNextMediaUrl(window.adserverUrl, window.onNextUrl);
            }
        } else {
            console.log("End of Stream");
            window.defaultOnEnded();  // call the default behavior  
        }
    }

    window.defaultOnEnded = window.mediaManager.onEnded;  // grab the default
    window.mediaManager.onEnded = window.onEnded;

    window.PlayNextMainStream = function () {
        if (window.mainstreamContentType === CONTENT_TYPE_EZADX) {
            getNextMediaUrl(window.mainstreamUrl, window.onNextUrl);
        } else {
            window.mainstreamMediaUrl = window.mainstreamUrl;
            onNextUrl(window.mainstreamMediaUrl);
        }
    }

    window.ContinueMainStream = function () {
        onNextUrl(window.mainstreamMediaUrl);
    }

    window.onNextUrl = function (nextUrl) {

        window.mediaManager.onEnded = window.onEnded;

        if (window.player !== null) {
            player.unload();    // Must unload before starting again.
            window.player = null;
        }

        console.log('Starting media application');
        var url = nextUrl;
        console.log('url :' + url);
        window.crntUrl = url;
        
        if (crntStrmType == STRM_TYPE_AD) {
            window.adMediaUrl = url;
        } else {
            window.mainstreamMediaUrl = url;
        }

        window.host = new cast.player.api.Host({ 'mediaElement': mediaElement, 'url': url });

        host.onError = function (errorCode) {
            console.log("Fatal Error - " + errorCode + " Continuing with the next ad!!");
            window.mediaManager.onEnded();
        };

        var ext = url.substring(url.lastIndexOf('.'), url.length);
        var initStart = 0; // TODO Get from adserver Query
        var autoplay = true;
        var protocol = null;
        mediaElement.autoplay = autoplay;  // Make sure autoplay get's set

        if (crntStrmType == STRM_TYPE_MAIN) {
            if (window.mainstreamIsLive)
                initStart = Infinity;
            else
                initStart = playposMainStrm;
        }
        if (url.lastIndexOf('.m3u8') >= 0) {
            // HTTP Live Streaming
            protocol = cast.player.api.CreateHlsStreamingProtocol(host);
        } else if (url.lastIndexOf('.mpd') >= 0) {
            // MPEG-DASH
            protocol = cast.player.api.CreateDashStreamingProtocol(host);
        } else if (url.indexOf('.ism') >= 0) {
            // Smooth Streaming
            protocol = cast.player.api.CreateSmoothStreamingProtocol(host);
        }
        console.log("we have protocol " + ext);
        if (protocol !== null) {
            console.log("Starting Media Player Library");
            window.player = new cast.player.api.Player(host);
            window.player.load(protocol, initStart);
        }
        else {
            window.defaultOnLoad(event);    // do the default process
        }
    }

    // Remember the default value for the Receiver onLoad, so this sample can Play
    // non-adaptive media as well.  
    window.defaultOnLoad = mediaManager.onLoad;

    window.player = null;
    console.log('Application is ready, starting system');
    window.castReceiverManager = cast.receiver.CastReceiverManager.getInstance();


    window.onEzadxMessage = function (e) {
        var message = JSON.parse(e.data),
          channel = e.target;

        console.log('Event [' + e.senderId + ']: ' + e.data);
        console.debug('Message ', JSON.stringify(message));

        switch (message.command) {
            case "load":
                window.adserverUrl = message["adUrl"];
                console.log('adserverUrl :' + window.adserverUrl);
                window.mainstreamUrl = message["mainstreamUrl"];
                window.mainstreamContentType = message["mainstreamContetType"];
                window.adContentType = message["adContentType"];
                window.adInterval = message["adInterval"];
                window.mainstreamIsLive = message["mainstreamIsLive"];

                if (window.adserverUrl && window.adserverUrl.length && window.adserverUrl != "none")
                    enableAdStrm = true;
                else
                    enableAdStrm = false;

                if (window.mainstreamUrl && window.window.mainstreamUrl.length && window.mainstreamUrl != "none")
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
    }

    // create a CastMessageBus to handle messages for a custom namespace
    window.messageBus = window.castReceiverManager.getCastMessageBus(NAMESPACE);

    window.messageBus.addEventListener("message", window.onEzadxMessage.bind(this));
    //window.messageBus.onMessage = function(event) {
    //    console.log('Message [' + event.senderId + ']: ' + event.data);
    //    window.messageBus.send(event.senderId, event.data);
    //}

    castReceiverManager.start();
};
