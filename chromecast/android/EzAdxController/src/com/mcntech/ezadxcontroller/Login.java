package com.mcntech.ezadxcontroller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Login extends Activity {
	String userName;
	//String userPassword;
	Button btnLogin;
	EditText txtUsr;
	EditText txtPwd;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnLogin=(Button)this.findViewById(R.id.btnLogin);
		txtUsr = (EditText) findViewById(R.id.txtUname);
        //txtPwd = (EditText) findViewById(R.id.txtPwd);
        
        btnLogin.setOnClickListener(new OnClickListener() {
   
	   @Override
	   public void onClick(View v) {
		    // TODO: Run web service
		    userName = txtUsr.getText().toString();
		    //userPassword = txtPwd.getText().toString();
		    String url = "";
			try {
				url = "http://www.ezadx.com/usradmin/affiliate.php?user=" + URLEncoder.encode(userName, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			new DownloadWebpageTask().execute(url);
		   }
        });       
	}
    
	private String downloadUrl(String myurl) throws IOException {
	    InputStream is = null;
	    // Only display the first 500 characters of the retrieved
	    // web page content.
	    int len = 500;
	        
	    try {
	        URL url = new URL(myurl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        // Starts the query
	        conn.connect();
	        int response = conn.getResponseCode();
	        is = conn.getInputStream();

	        // Convert the InputStream into a string
	        String contentAsString = readIt(is, len);
	        return contentAsString;
	        
	    // Makes sure that the InputStream is closed after the app is
	    // finished using it.
	    } finally {
	        if (is != null) {
	            is.close();
	        }
	    }
	}	
	// Reads an InputStream and converts it to a String.
	public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
	    Reader reader = null;
	    reader = new InputStreamReader(stream, "UTF-8");        
	    char[] buffer = new char[len];
	    reader.read(buffer);
	    return new String(buffer);
	}
	
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            try {
                return downloadUrl(url[0]);
            } catch (IOException e) {
            	String error = "Failed";
                return error;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	
        	if(result.equals("Failed")) {
    		    Toast.makeText(Login.this, "Server Connection Failed",Toast.LENGTH_LONG).show();
        		return;
        	}
			Intent mainactivity = new Intent(getApplicationContext(), MainActivity.class);
			Bundle b = new Bundle();
			b.putString("streams", result);
			mainactivity.putExtras(b);

			// Close all views before launching Dashboard
			mainactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainactivity);
			 
			// Close Login Screen
			finish();
       }
    }
} 