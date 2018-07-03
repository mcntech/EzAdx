package com.mcntech.ezadxcontroller;

import java.io.IOException; 
import java.util.ArrayList; 
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
import android.content.Intent; 
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable; 
import android.os.Bundle; 
import android.speech.RecognizerIntent; 
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat; 
import android.support.v7.app.ActionBar; 
import android.support.v7.app.ActionBarActivity; 
import android.support.v7.app.MediaRouteActionProvider; 
import android.support.v7.media.MediaRouteSelector; 
import android.support.v7.media.MediaRouter; 
import android.support.v7.media.MediaRouter.RouteInfo; 
import android.util.Log; 
import android.view.Menu; 
import android.view.MenuItem; 
import android.view.View; 
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast; 
 
import com.google.android.gms.cast.ApplicationMetadata; 
import com.google.android.gms.cast.Cast; 
import com.google.android.gms.cast.Cast.ApplicationConnectionResult; 
import com.google.android.gms.cast.Cast.MessageReceivedCallback; 
import com.google.android.gms.cast.CastDevice; 
import com.google.android.gms.cast.CastMediaControlIntent; 
import com.google.android.gms.common.ConnectionResult; 
import com.google.android.gms.common.api.GoogleApiClient; 
import com.google.android.gms.common.api.ResultCallback; 
import com.google.android.gms.common.api.Status; 
import com.mcntech.ezadxcontroller.EditCustomUrl;
import com.mcntech.ezadxcontroller.EditCustomUrl.EditCustomUrlDialogListener;

public class MainActivity extends  ActionBarActivity implements AdapterView.OnItemSelectedListener, EditCustomUrlDialogListener {
	private static final String TAG = MainActivity.class.getSimpleName(); 
	private static final int REQUEST_CODE = 1; 
 	private MediaRouter mMediaRouter; 
 	private MediaRouteSelector mMediaRouteSelector; 
 	private MediaRouter.Callback mMediaRouterCallback; 
 	private CastDevice mSelectedDevice; 
 	private GoogleApiClient mApiClient; 
 	private Cast.Listener mCastListener; 
 	private ConnectionCallbacks mConnectionCallbacks; 
 	private ConnectionFailedListener mConnectionFailedListener; 
 	private HelloWorldChannel mHelloWorldChannel; 
 	private boolean mApplicationStarted; 
 	private boolean mWaitingForReconnect; 
 	private String mSessionId; 
 
 	private String  adUrl = "http://www.onyxvideo.com/revive/www/delivery/fc.php?script=bannerTypeHtml:vastInlineBannerTypeHtml:vastInlineHtml&zones=pre-roll:0.0-0%3D1&nz=4&source=&r=R0.8214839450083673&block=0&format=vast&charset=UTF-8";
 	private String  adContentType = "application/x-ezadx";
 	private Integer adInterval = 60;

 	private String  crntMainCustomStream = "none"; 	
 	private Boolean crntMainCustomStreamIsLive = false; 	
 	private Stream  crntMainStream;
 	
 	private Boolean mainstreamIsLive = false;
 	private String  mainstreamContetType = "application/x-mpegURL"; 
 	private Spinner spinner_adurl;
 	private Spinner spinner_adinterval;
 	private Spinner spinner_mainurl;
 	private ArrayAdapter<Stream> streams_ad;
 	private ArrayAdapter<Stream> streams_main; 	
 	
 	
 	private ArrayAdapter<AdInterval> list_adintervals;
 	//"http://www.onyxvideo.com/revive/www/delivery/fc.php?script=bannerTypeHtml:vastInlineBannerTypeHtml:vastInlineHtml&zones=pre-roll:0.0-0%3D1&nz=4&source=&r=R0.8214839450083673&block=0&format=vast&charset=UTF-8"
 	private static final String AdServerUrl = "http://www.onyxvideo.com/revive/www/delivery/fc.php";
 	private static final String AdQueryScript = "bannerTypeHtml:vastInlineBannerTypeHtml:vastInlineHtml"; 	
 	private static final String AdQueryZonePrefix = "pre-roll:0.0-0%3D";
 	private static final String AdQuerySource = "";
 	private static final Integer AdQueryBlock = 0;
 	private static final String AdQueryFormat = "vast";
 	private static final String AdQueryCharset = "UTF-8";
 	
 	
 	private static Integer mStackLevel = 0;
 	public static final int MY_REQUEST_CODE = 1;
	Button btnConfigure;
	
	public class Stream {
		public String name;
		public String url;
		public Boolean isLive;
		public String contentType;
		
		protected Stream(String n, String u, Boolean isLiveIn, String contentTypeIn) {
			name = n;
			url = u;
			isLive = isLiveIn;
			contentType = contentTypeIn;
		}
		
		public String toString()
		{
			return name;
		}
	}

	public class AdInterval {
		public String name;
		public Integer interval;

		
		protected AdInterval(String n, Integer i) {
			name = n;
			interval = i;
		}
		
		public String toString()
		{
			return name;
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(
				android.R.color.transparent));

		RestoreAppParams();
		btnConfigure=(Button)this.findViewById(R.id.btnConfigure);	
		
		btnConfigure.setOnClickListener(new OnClickListener() {
		   @Override
		   public void onClick(View v) {
			   LaunchConfigureDialog();
		   }
		});
		spinner_adurl = (Spinner) findViewById(R.id.spinner_adurl);
		String webdata = getIntent().getExtras().getString("streams");
		populateAdStreamsFromWebData(webdata);
		spinner_adurl.setOnItemSelectedListener(this);
		
		spinner_adinterval = (Spinner) findViewById(R.id.spinner_adinterval);
		populateAdIntervals();
		spinner_adinterval.setOnItemSelectedListener(this);
		
		spinner_mainurl = (Spinner) findViewById(R.id.spinner_mainurl);
		populateMaintreamsFromWebData(webdata);

		spinner_mainurl.setOnItemSelectedListener(this);
		
		// Configure Cast device discovery
		mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		mMediaRouteSelector = new MediaRouteSelector.Builder()
				.addControlCategory(
						CastMediaControlIntent.categoryForCast(getResources()
								.getString(R.string.app_id))).build();
		mMediaRouterCallback = new MyMediaRouterCallback();
    }

	private void populateAdStreamsFromWebData(String webdata) {
		spinner_adurl = (Spinner) this.findViewById(R.id.spinner_adurl);
		Stream[] items = null;
		try {
			JSONObject json = new JSONObject(webdata);
			List<Stream> list = new ArrayList<Stream>();
			Stream item = new Stream("Disable","none",false,"application/x-ezadx");
			list.add(item);
			if(json.has("adstreams")) {
				JSONArray adurls  = json.getJSONArray("adstreams");
				for(int i=0; i < adurls.length(); i++) {
					JSONObject url = adurls.getJSONObject(i);
					String zoneName = url.getString("zonename");
					if(zoneName.startsWith("Ad")) {
						item = new Stream(url.getString("zonename"),url.getString("zoneid"),false,"application/x-ezadx");
						list.add(item);
					}
				}
			}
			items = list.toArray(new Stream[ list.size() ]);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		streams_ad = new ArrayAdapter<Stream>(
				this,
				android.R.layout.simple_spinner_item,
				items
			);
		        
		spinner_adurl.setAdapter(streams_ad);
	}
	
	private void populateAdIntervals() {
		spinner_adinterval = (Spinner) this.findViewById(R.id.spinner_adinterval);
		
		AdInterval[] items = new AdInterval[] {	
			new AdInterval (
		           "Ad Interval: 1 Minute",
		           1
		       ),
 
			new AdInterval(
		           "Ad Interval: 5 Minutes",
		           5
			),
			new AdInterval(
		           "Ad Interval: 30 Minutes",
		           30
			)
		};
		
		list_adintervals = new ArrayAdapter<AdInterval>(
				this,
				android.R.layout.simple_spinner_item,
				items
			);
		        
		spinner_adinterval.setAdapter(list_adintervals);
	}

 
	private void populateMaintreamsFromWebData(String webdata) {
		spinner_mainurl = (Spinner) this.findViewById(R.id.spinner_mainurl);
		
		Stream[] items = null;
		try {
			JSONObject json = new JSONObject(webdata);
			List<Stream> list = new ArrayList<Stream>();
			Stream item = new Stream("Disable","none",false,"application/x-ezadx");
			list.add(item);
			if(json.has("adstreams")) {
				JSONArray adurls  = json.getJSONArray("adstreams");				
				for(int i=0; i < adurls.length(); i++) {
					JSONObject url = adurls.getJSONObject(i);
					String zoneName = url.getString("zonename");
					if(!zoneName.startsWith("Ad")) {
						item = new Stream(url.getString("zonename"),url.getString("zoneid"),false,"application/x-ezadx");
						list.add(item);
					}
				}
			}
			item = new Stream("Custom", "none", true, "application/x-mpegURL" );
			list.add(item);
			items = list.toArray(new Stream[ list.size() ]);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		streams_main = new ArrayAdapter<Stream>(
				this,
				android.R.layout.simple_spinner_item,
				items
			);
		spinner_mainurl.setAdapter(streams_main);
	}
	
	private String GetAdUrl(String ZoneId)
	{
	 	//"http://www.onyxvideo.com/revive/www/delivery/fc.php?script=bannerTypeHtml:vastInlineBannerTypeHtml:vastInlineHtml&zones=pre-roll:0.0-0%3D1&nz=4&source=&r=R0.8214839450083673&block=0&format=vast&charset=UTF-8"
		Double random = Math.random(); 
		String AdUrl;
		AdUrl = AdServerUrl;
		AdUrl = AdUrl.concat("?script=").concat(AdQueryScript); 	
		AdUrl = AdUrl.concat("&zones=").concat(AdQueryZonePrefix);
		AdUrl = AdUrl.concat(ZoneId);
		AdUrl = AdUrl.concat("&nz=1");
		AdUrl = AdUrl.concat("&source=").concat(AdQuerySource);		
		AdUrl = AdUrl.concat("&r=R").concat(random.toString());
		AdUrl = AdUrl.concat("&block=").concat(AdQueryBlock.toString());		
		AdUrl = AdUrl.concat("&format=").concat(AdQueryFormat);		
		AdUrl = AdUrl.concat("&charset=").concat(AdQueryCharset);		
		
		return AdUrl;
	}
	
	 
    @Override
    public void onFinishEditDialog(String inputText, Boolean isLive) {
		crntMainCustomStream = inputText;
		crntMainCustomStreamIsLive = isLive;
		SaveAppParams();
    }
    
    public void LaunchConfigureDialog() {
		mStackLevel++;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	    Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);
	   
		DialogFragment editCustomUrlFragment = EditCustomUrl.newInstance(mStackLevel, crntMainCustomStream, crntMainCustomStreamIsLive);
		editCustomUrlFragment.show(ft, "dialog");
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.spinner_adurl)
        {
        	Stream crntAdStream = streams_ad.getItem(pos);
        	if(crntAdStream.url.equals("none")) {
        		adUrl = crntAdStream.url;
        	} else {
        		adUrl = GetAdUrl(crntAdStream.url);
        	}
        }
        else if(spinner.getId() == R.id.spinner_adinterval)
        {
        	AdInterval crntItem = list_adintervals.getItem(pos);
        	adInterval = crntItem.interval * 60;
        }
        else if(spinner.getId() == R.id.spinner_mainurl)
        {
        	crntMainStream = streams_main.getItem(pos);
        	if (crntMainStream.name.equals("Custom")) {
       			crntMainStream.url = crntMainCustomStream;
       			crntMainStream.isLive = crntMainCustomStreamIsLive;
        	} else if (crntMainStream.name.equals("Disable")) {
        		crntMainStream.url = "none";
        	} else {
        		crntMainStream.url = GetAdUrl(crntMainStream.url);
        		mainstreamContetType = "application/x-ezadx";
        	}
        }
   }   
    
    @Override 
	public void onNothingSelected(AdapterView<?> adapterView){ 
     
	} 

    
    

    public void SaveAppParams() {
		 SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		 editor.putString("CustomUrl", crntMainCustomStream);
		 editor.putBoolean("CustomUrlIsLive", crntMainCustomStreamIsLive);
		 editor.apply();
    }
    

    public void RestoreAppParams() {
    	SharedPreferences prefs = getPreferences(MODE_PRIVATE); 
    	crntMainCustomStream = prefs.getString("CustomUrl", "none");
    	crntMainCustomStreamIsLive = prefs.getBoolean("CustomUrlIsLive", false);
    }    
    
	/**
	 * Android voice recognition
	 */
    /*
	private void startCasting() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				getString(R.string.message_to_cast));
		startActivityForResult(intent, REQUEST_CODE);
	}
     */
    
	/*
	 * Handle the voice recognition response
	 * 
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (matches.size() > 0) {
				Log.d(TAG, matches.get(0));
				sendMessage(matches.get(0));
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Start media router discovery
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
				MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
	}

	@Override
	protected void onPause() {
		if (isFinishing()) {
			// End media router discovery
			mMediaRouter.removeCallback(mMediaRouterCallback);
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		teardown();
		super.onDestroy();
	}
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat
				.getActionProvider(mediaRouteMenuItem);
		// Set the MediaRouteActionProvider selector for device discovery.
		mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
		return true;
	}

    
	/**
	 * Callback for MediaRouter events
	 */
	private class MyMediaRouterCallback extends MediaRouter.Callback {

		@Override
		public void onRouteSelected(MediaRouter router, RouteInfo info) {
			Log.d(TAG, "onRouteSelected");
			// Handle the user route selection.
			mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

			launchReceiver();
		}

		@Override
		public void onRouteUnselected(MediaRouter router, RouteInfo info) {
			Log.d(TAG, "onRouteUnselected: info=" + info);
			teardown();
			mSelectedDevice = null;
		}
	}
	/**
	 * Start the receiver app
	 */
	private void launchReceiver() {
		try {
			mCastListener = new Cast.Listener() {

				@Override
				public void onApplicationDisconnected(int errorCode) {
					Log.d(TAG, "application has stopped");
					teardown();
				}

			};
			// Connect to Google Play services
			mConnectionCallbacks = new ConnectionCallbacks();
			mConnectionFailedListener = new ConnectionFailedListener();
			Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
					.builder(mSelectedDevice, mCastListener);
			mApiClient = new GoogleApiClient.Builder(this)
					.addApi(Cast.API, apiOptionsBuilder.build())
					.addConnectionCallbacks(mConnectionCallbacks)
					.addOnConnectionFailedListener(mConnectionFailedListener)
					.build();

			mApiClient.connect();
		} catch (Exception e) {
			Log.e(TAG, "Failed launchReceiver", e);
		}
	}
	
	
	/**
	 * Google Play services callbacks
	 */
	private class ConnectionCallbacks implements
			GoogleApiClient.ConnectionCallbacks {
		@Override
		public void onConnected(Bundle connectionHint) {
			Log.d(TAG, "onConnected");

			if (mApiClient == null) {
				// We got disconnected while this runnable was pending
				// execution.
				return;
			}

			try {
				if (mWaitingForReconnect) {
					mWaitingForReconnect = false;

					// Check if the receiver app is still running
					if ((connectionHint != null)
							&& connectionHint
									.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
						Log.d(TAG, "App  is no longer running");
						teardown();
					} else {
						// Re-create the custom message channel
						try {
							Cast.CastApi.setMessageReceivedCallbacks(
									mApiClient,
									mHelloWorldChannel.getNamespace(),
									mHelloWorldChannel);
						} catch (IOException e) {
							Log.e(TAG, "Exception while creating channel", e);
						}
					}
				} else {
					// Launch the receiver app
					Cast.CastApi
							.launchApplication(mApiClient,
									getString(R.string.app_id), false)
							.setResultCallback(
									new ResultCallback<Cast.ApplicationConnectionResult>() {
										@Override
										public void onResult(
												ApplicationConnectionResult result) {
											Status status = result.getStatus();
											Log.d(TAG,
													"ApplicationConnectionResultCallback.onResult: statusCode"
															+ status.getStatusCode());
											if (status.isSuccess()) {
												ApplicationMetadata applicationMetadata = result
														.getApplicationMetadata();
												mSessionId = result
														.getSessionId();
												String applicationStatus = result
														.getApplicationStatus();
												boolean wasLaunched = result
														.getWasLaunched();
												Log.d(TAG,
														"application name: "
																+ applicationMetadata
																		.getName()
																+ ", status: "
																+ applicationStatus
																+ ", sessionId: "
																+ mSessionId
																+ ", wasLaunched: "
																+ wasLaunched);
												mApplicationStarted = true;

												// Create the custom message
												// channel
												mHelloWorldChannel = new HelloWorldChannel();
												try {
													Cast.CastApi
															.setMessageReceivedCallbacks(
																	mApiClient,
																	mHelloWorldChannel
																			.getNamespace(),
																	mHelloWorldChannel);
												} catch (IOException e) {
													Log.e(TAG,
															"Exception while creating channel",
															e);
												}

												// set the initial instructions
												// on the receiver
												//sendMessage(getString(R.string.instructions));
												
												JSONObject payload = new JSONObject();
									        	try {
									        		payload.put("command", "load");
									        		payload.put("adUrl", adUrl);
									        		payload.put("adInterval", adInterval); 
									        		payload.put("adContentType", adContentType); 
					        						payload.put("mainstreamUrl", crntMainStream.url); 
			        								payload.put("mainstreamContetType", mainstreamContetType); 
	        										payload.put("mainstreamIsLive", mainstreamIsLive);	
	        									}
									        	catch (JSONException error) {
									        		
									        	}
									            String jsonText = payload.toString();
												sendMessage(jsonText);
											} else {
												Log.e(TAG,
														"application could not launch");
												teardown();
											}
										}
									});
				}
			} catch (Exception e) {
				Log.e(TAG, "Failed to launch application", e);
			}
		}

		@Override
		public void onConnectionSuspended(int cause) {
			Log.d(TAG, "onConnectionSuspended");
			mWaitingForReconnect = true;
		}
	}
	
	/**
	 * Tear down the connection to the receiver
	 */
	private void teardown() {
		Log.d(TAG, "teardown");
		if (mApiClient != null) {
			if (mApplicationStarted) {
				if (mApiClient.isConnected()  || mApiClient.isConnecting()) {
					try {
						Cast.CastApi.stopApplication(mApiClient, mSessionId);
						if (mHelloWorldChannel != null) {
							Cast.CastApi.removeMessageReceivedCallbacks(
									mApiClient,
									mHelloWorldChannel.getNamespace());
							mHelloWorldChannel = null;
						}
					} catch (IOException e) {
						Log.e(TAG, "Exception while removing channel", e);
					}
					mApiClient.disconnect();
				}
				mApplicationStarted = false;
			}
			mApiClient = null;
		}
		mSelectedDevice = null;
		mWaitingForReconnect = false;
		mSessionId = null;
	}
    
    private class ConnectionFailedListener implements 
 			GoogleApiClient.OnConnectionFailedListener { 
 		@Override 
 		public void onConnectionFailed(ConnectionResult result) { 
			Log.e(TAG, "onConnectionFailed "); 
 
     
 			teardown(); 
 		} 
 	} 
   
	/**
	 * Send a text message to the receiver
	 * 
	 * @param message
	 */
	private void sendMessage(String message) {
		if (mApiClient != null && mHelloWorldChannel != null) {
			try {
				Cast.CastApi.sendMessage(mApiClient,
						mHelloWorldChannel.getNamespace(), message)
						.setResultCallback(new ResultCallback<Status>() {
							@Override
							public void onResult(Status result) {
								if (!result.isSuccess()) {
									Log.e(TAG, "Sending message failed");
								}
							}
						});
			} catch (Exception e) {
				Log.e(TAG, "Exception while sending message", e);
			}
		} else {
			Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT)
					.show();
		}
	}
    
	/** 
 	 * Custom message channel 
 	 */ 
 	class HelloWorldChannel implements MessageReceivedCallback { 
 
 
 		/** 
 		 * @return custom namespace 
 		 */ 
 		public String getNamespace() { 
 			return getString(R.string.namespace); 
 		} 
 
 
 		/* 
 		 * Receive message from the receiver app 
 		 */ 
 		@Override 
 		public void onMessageReceived(CastDevice castDevice, String namespace, 
 				String message) { 
 			Log.d(TAG, "onMessageReceived: " + message); 
 		} 
 	} 
}
