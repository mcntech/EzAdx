var session = null;
//var NAMESPACE = "org.dashif.dashjs";
var NAMESPACE = "urn:x-cast:org.dashif.dashjs";
//var url = "http://www.digitalprimates.net/dash/streams/gpac/mp4-main-multi-mpd-AV-NBS.mpd";
// var contentType = 'application/x-mpegURL';


var adUrl = "none";
var adContentType = "application/x-ezadx";
var adInterval = 60;
var mainstreamUrl = "none";
var mainstreamIsLive = false;
var mainstreamContetType = "application/x-mpegURL"; 
    
function CasterController($scope) {
    $scope.availableAdZones = [
       {
           name: "Disable",
           url: "none",
           isLive: false,
           contetType: "application/x-ezadx"
       },
        {
            name: "Ad Zone-1",
            url: "http://www.onyxvideo.com/revive/www/delivery/fc.php?script=bannerTypeHtml:vastInlineBannerTypeHtml:vastInlineHtml&zones=pre-roll:0.0-0%3D1&nz=4&source=&r=R0.8214839450083673&block=0&format=vast&charset=UTF-8",
            isLive: false,
            contetType: "application/x-ezadx"
        },

        {
            name: "Ad Zone-2",
            url: "http://www.onyxvideo.com/revive/www/delivery/fc.php?script=bannerTypeHtml:vastInlineBannerTypeHtml:vastInlineHtml&zones=pre-roll:0.0-0%3D2&nz=4&source=&r=R0.8214839450083673&block=0&format=vast&charset=UTF-8",
            isLive: false,
            contetType: "application/x-ezadx"
        },
        // Debug Streams
    ];

    $scope.availableAdIntervals = [
       {
           name: "1 Minute",
           interval: 1,
       },
       {
           name: "5 Minute",
           interval: 5,
       },
       {
           name: "10 Minutes",
           interval: 10,
       },
       {
           name: "20 Minutes",
           interval: 20,
       },
       {
           name: "30 Minutes",
           interval: 30,
       },
   ];

    $scope.availableMainStreams = [
       {
           name: "Disable",
           url: "none",
           isLive: false,
           contetType: "application/x-mpegURL"
       },
       {
           name: "Faulty Src-1",
           url: "https://s3.amazonaws.com/onyx_tv20/email_feeds/email_feeds_20111229_0010.m3u8",
           isLive: false,
           contetType: "application/x-mpegURL"
       },
    
       {
           name: "Main Src-2",
           url: "https://s3.amazonaws.com/onyx_tv20/programs/program_20111228_0003.m3u8",
           isLive: false,
           contetType: "application/x-mpegURL"
       },
       {
           name: "Main Src-3",
           url: "https://s3.amazonaws.com/onyx_tv20/programs/program_20120120_0004.m3u8",
           isLive: false,
           contetType: "application/x-mpegURL"
       },
       
       {
           name: "Custom Live",
           url: "Enter HTTP Live Stream Url",
           isLive: true,
           contetType: "application/x-mpegURL"
       },
       {
           name: "Custom VOD",
           url: "Enter HTTP Live Stream Url",
           isLive: false,
           contetType: "application/x-mpegURL"
       }
       
       
       
       // Debug Streams
   ];

    // -----------------------------------
    // Properties
    // -----------------------------------

    var STATE_LOADING = "loading",
        STATE_READY = "ready",
        STATE_CASTING = "casting",
        self = this,
        initialized = false;

    $scope.castApiReady = false;
    $scope.hasError = false;
    $scope.playing = false;
    $scope.muted = false;
    $scope.volume = 1;
    $scope.duration = 0;
    $scope.currentTime = 0;
    $scope.selectedAdZone =  $scope.availableAdZones[0];
    $scope.selectedAdInterval = $scope.availableAdIntervals[0];
    $scope.selectedMainStream = $scope.availableMainStreams[0];
    // -----------------------------------
    // Getters / Setters
    // -----------------------------------

    $scope.setAdZone = function (item) {
        $scope.selectedAdZone = item;
    }

    $scope.setAdInterval = function (item) {
        $scope.selectedAdInterval = item;
    }

    $scope.setMainStream = function (item) {
        $scope.selectedMainStream = item;
    }
    
    $scope.doCast = function () {
        $scope.state = STATE_CASTING;
        // TODO: Bind to timeout
        //Caster.doLaunch();
        launchApp($scope.selectedAdZone, $scope.selectedAdInterval, $scope.selectedMainStream);
    }
}


function sessionListener(e) {
    session = e;
    console.log('New session');
    if (session.media.length != 0) {
            console.log('Found ' + session.media.length + ' sessions.');
    }
}

function receiverListener(e) {
    if( e === 'available' ) {
            console.log("Chromecast was found on the network.");
    }
    else {
            console.log("There are no Chromecasts available.");
    }
}

function onInitSuccess() {
    console.log("Initialization succeeded");
}


function onInitError() {
    console.log("Initialization failed");
}

function launchApp(adZone, adInterval, mainStream) {
    console.log("Launching the Chromecast App...");
    
   console.log("Launching the Chromecast App...");

    adUrl = adZone.url;
    adContentType = adZone.contetType;
    
    adInterval = 60 * adInterval.interval;
    
    mainstreamUrl = mainStream.url;
    mainstreamIsLive = mainStream.isLive;
    mainstreamContetType = mainStream.contetType;

    chrome.cast.requestSession(onRequestSessionSuccess, onLaunchError);
}

function onRequestSessionSuccess(e) {
    console.log("Successfully created session: " + e.sessionId);
    session = e;
    //loadMdp(url, true);
    //loadMedia();
    loadMediaUsingSendMsg();
}


function loadMedia() {
    if (!session) {
            console.log("No session.");
            return;
    }
 
    var mediaInfo = new chrome.cast.media.MediaInfo(adUrl, adContentType);
    mediaInfo.customData = {adInterval : adInterval, mainstreamUrl : mainstreamUrl, mainstreamIsLive : mainstreamIsLive};    
    var request = new chrome.cast.media.LoadRequest(mediaInfo);
    request.autoplay = true;
    session.loadMedia(request, onLoadSuccess, onLoadError);
}

function loadMediaUsingSendMsg() {
    if (!session) {
            console.log("No session.");
            return;
    }
 
    sendMessage("load", {
            "adUrl" : adUrl, 
            "adInterval" : adInterval, 
            "adContentType" : adContentType, 
            "mainstreamUrl" : mainstreamUrl, 
            "mainstreamContetType" : mainstreamContetType, 
            "mainstreamIsLive" : mainstreamIsLive}, onMessageSent);
}


//==================================
function sendMessage (command, attrs, callback) {
    if (!session) {
        console.log("No session.");
        return;
}
    var msg = $.extend({ command: command }, attrs);
    session.sendMessage(NAMESPACE, msg, callback);
}

// This is for future use
function onMessageSent (e) {
    console.log("Message sent.  Result...");
    console.log(e);
}

// This is for future use
function loadMdp(url, live) {
    if (!session) {
        console.log("No session.");
        return;
    }
    
    console.log("Send load media...");
    sendMessage("load", {
        manifest: url,
        isLive: live
    }, onMessageSent);
}

//This is for future use
function playMedia() {
    sendMessage("play", {}, onMessageSent);
}

//=======================================

function onLoadSuccess() {
    console.log('Successfully loaded image.');
}


function onLoadError() {
    console.log('Failed to load image.');
}

function onLaunchError() {
    console.log("Error connecting to the Chromecast.");
}


function stopApp() {
    session.stop(onStopAppSuccess, onStopAppError);
}

function onStopAppSuccess() {
    console.log('Successfully stopped app.');
}


function onStopAppError() {
    console.log('Error stopping app.');
}

$( document ).ready(function(){
       var loadCastInterval = setInterval(function(){
               if (chrome.cast.isAvailable) {
                       console.log('Cast has loaded.');
                       clearInterval(loadCastInterval);
                       initializeCastApi();
               } else {
                       console.log('Cast is not available');
               }
       }, 1000);
});

function initializeCastApi() {
    var applicationID = 'A886EAB8'; //chrome.cast.media.DEFAULT_MEDIA_RECEIVER_APP_ID; // 
    //var applicationID = chrome.cast.media.DEFAULT_MEDIA_RECEIVER_APP_ID;
    var sessionRequest = new chrome.cast.SessionRequest(applicationID);
    var apiConfig = new chrome.cast.ApiConfig(sessionRequest,
            sessionListener,
            receiverListener);
    chrome.cast.initialize(apiConfig, onInitSuccess, onInitError);
};