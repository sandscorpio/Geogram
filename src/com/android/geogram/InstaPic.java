package com.android.geogram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class InstaPic extends Activity {
	//final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	
	String TAG = "InstaPic";
	WebView webview;
	Button btnDownload;
	DropboxClient _drop;
	Button _btnMoreFromUser;
	String _id;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.instapic);
		 
		 webview = (WebView) findViewById(R.id.webview);
		 btnDownload = (Button) findViewById(R.id.btnDownload);
		 btnDownload.setOnClickListener(_btnDownloadListener);
		 _btnMoreFromUser = (Button) findViewById(R.id.btnMoreFromUser);
		 _btnMoreFromUser.setOnClickListener(_btnMoreClicked);
		 
		 
		 Intent intent = getIntent();
		 String uri = intent.getStringExtra("uri");
		 _id = intent.getStringExtra("id");
		 String[] IDs = _id.split(":");
		 _id = IDs[0];

		 String html = String.format("<img src=\"%s\"><p>\"#%s Best Photo of The Week\"</p>", uri, IDs[1]);
		 webview.setWebViewClient(new WebViewClient() {
			   public void onProgressChanged(WebView view, int progress) {
			     // Activities and WebViews measure progress with different scales.
			     // The progress meter will automatically disappear when we reach 100%
			     InstaPic.this.setProgress(progress * 1000);
			   }
			 });
		 webview.loadData(html, "text/html", null);		 
		 
	}
	
	protected void onResume() {
	    super.onResume();
	    
	    if (_drop != null) {
	    	_drop.onResume();
	    }
	    
	}
	
	public OnClickListener _btnDownloadListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (_drop == null) {
				//login
				_drop = new DropboxClient();
				_drop.init();
				_drop.login();
				return;
			}
			
			//upload
			
		}		
	};
	
	public OnClickListener _btnMoreClicked = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			 getUserPics(_id);
		}		
	};
	
	public void getUserPics(String id) {
		String uri  = String.format("https://api.instagram.com/v1/users/%s/media/recent/?count=5&access_token=%s", id, GeoInstagram.TOKEN);
		Log.d(TAG, uri);
		new RequestTask().execute(uri);
	}
	
	public void downloaded() {
		Toast.makeText(this, "Dropped it!", Toast.LENGTH_SHORT).show();
	}
	
	class RequestTask extends AsyncTask<String, String, String>{
		
	    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        
	        try {
	            response = httpclient.execute(new HttpGet(uri[0]));
	            StatusLine statusLine = response.getStatusLine();
	            
	            if (statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
	        return responseString;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        
	        ArrayList<InstaData> outstring = new ArrayList<InstaData>(10);
	        
	        try {
				JSONObject response = new JSONObject(result);
				JSONArray data = response.getJSONArray("data");
				for (int i=0; i<data.length(); i++) {
					JSONObject o = data.getJSONObject(i);
					InstaData s = JsonParser.getImageLinkArray(o);
					outstring.add(s);
				}							
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        InstaData [] arr = new InstaData[outstring.size()];
	        for (int i=0; i<arr.length; i++) {
	        	arr[i] = outstring.get(i);
	        }
	        
	        
	        Arrays.sort(arr, new Comparator<InstaData>() {
	            public int compare(InstaData a, InstaData b) {
	            	int c1 = Integer.valueOf(a.count);
	            	int c2 = Integer.valueOf(b.count);
	            	
	            	if (c1 < c2) 
	            		return 1;
	            	else if (c1 > c2)
	            		return -1;
	            	else 
	            		return 0;
	            }
	        });	
	        
	        for (InstaData a : outstring) {
	        		Log.d(TAG, a.url);
	        }
	        
	        String html = "";
	        for (int i=0; i<arr.length; i++) {
	        	html += String.format("<img src=\"%s\">", arr[i].url);
	        	
	        	long time = Long.valueOf(arr[i].time);
	        	java.util.Date time2 =new java.util.Date(time*1000);
	        	
	        	html += String.format("%s | %s<p>", arr[i].username, time2);
	        }
	        
	        Log.d(TAG, result);
	        webview.loadData(html, "text/html", null);
	    }
	}

	private class DropboxClient {
		final static private String APP_KEY = "DROPBOX-KEY";
		final static private String APP_SECRET = "DROPBOX-SECRET";
	
		// In the class declaration section:
		private DropboxAPI<AndroidAuthSession> mDBApi;
		private InstaPic _activity;
		
		public DropboxClient() {		
		}
		
		public void init() {
			// And later in some initialization function:
			AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
			AndroidAuthSession session = new AndroidAuthSession(appKeys,  AccessType.APP_FOLDER);
			mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		}
		
		public void login() {
			// MyActivity below should be your activity class name
			AccessTokenPair access = getStoredKeys();
			if (access != null) {			
				mDBApi.getSession().setAccessTokenPair(access);
			}
			else {
				//mDBApi.getSession().startAuthentication(MyActivity.this);
			}
		}
		
		public void onResume() { 
		    if (mDBApi.getSession().authenticationSuccessful()) {
		        try {
		            // MANDATORY call to complete auth.
		            // Sets the access token on the session
		            mDBApi.getSession().finishAuthentication();

		            AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

		            // Provide your own storeKeys to persist the access token pair
		            // A typical way to store tokens is using SharedPreferences
		            //storeKeys(tokens.key, tokens.secret);
		            SharedPreferences prefs = _activity.getPreferences(Context.MODE_PRIVATE);
		            Editor editor = prefs.edit();
		            editor.putString("DROPBOX_KEY", tokens.key);
		            editor.putString("DROPBOX_SECRET", tokens.secret);
		            editor.commit();
		        } catch (IllegalStateException e) {
		            Log.i("DbAuthLog", "Error authenticating", e);
		        }
		    }
		    // ...
		}
		
		public AccessTokenPair getStoredKeys() {
			SharedPreferences prefs = _activity.getPreferences(Context.MODE_PRIVATE);
			String key = prefs.getString("DROPBOX_KEY", "");
			String secret = prefs.getString("DROPBOX_SECRET", "");
		
			if (key.equals("") || secret.equals("")) return null;
			return new AccessTokenPair(key, secret);
		}
		
		public void upload(String uri) {
			InputStream stream = null;
			try {
				URL oracle = new URL(uri);
				stream = oracle.openStream();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if (stream == null) {
				return;
			}
			
			// Uploading content.
			String fileContents = "Hello World!";
			ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
			try {
			   Entry newEntry = mDBApi.putFile("/testing.txt", stream, fileContents.length(), null, null);
			   Log.i("DbExampleLog", "The uploaded file's rev is: " + newEntry.rev);
			} catch (DropboxUnlinkedException e) {
			   // User has unlinked, ask them to link again here.
			   Log.e("DbExampleLog", "User has unlinked.");
			} catch (DropboxException e) {
			   Log.e("DbExampleLog", "Something went wrong while uploading.");
			}
			
			_activity.downloaded();
		}
		
	}

	
}
