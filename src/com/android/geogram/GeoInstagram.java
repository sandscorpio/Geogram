package com.android.geogram;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import android.os.AsyncTask;
import android.util.Log;

public class GeoInstagram {
	private static final String TAG = "Geogram";
	public static final String TOKEN = "INSTAGRAM-TOKEN";
	private GeogramActivity _g;
	
	public GeoInstagram(GeogramActivity g) {
		_g = g;
	}

	public void getPics(double latitude, double longitude, long timestart) {
		String uri  = String.format("https://api.instagram.com/v1/media/search?lat=%f&lng=%f&min_timestamp=%d&access_token=%s&count=%d", latitude, longitude, timestart, TOKEN, 150);
		//String uri  = String.format("https://api.instagram.com/v1/media/search?lat=%f&lng=%f&min_timestamp=%d&access_token=%s", latitude, longitude, timestart, TOKEN);
		Log.d(TAG, uri);
		new RequestTask(this).execute(uri);
	}	

	public void getPics(double latitude, double longitude) {
		//String uri  = String.format("https://api.instagram.com/v1/media/search?lat=%f&lng=%f&min_timestamp=%d&access_token=%s&count=%d", latitude, longitude, timestart, TOKEN, 100);
		String uri  = String.format("https://api.instagram.com/v1/media/search?lat=%f&lng=%f&access_token=%s", latitude, longitude, TOKEN);
		Log.d(TAG, uri);
		new RequestTask(this).execute(uri);
	}	
	
	public void receivedPics(InstaData[] data) {
		_g.initOverlays(data);
	}
	
	class RequestTask extends AsyncTask<String, String, String>{
		GeoInstagram _g;
		
		RequestTask(GeoInstagram g) {
			_g = g;
		}
		
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
	        
	        ArrayList<InstaData> outstring = new ArrayList<InstaData>(100);
	        
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
	        
	        Log.d(TAG, result);
	        
	        receivedPics(arr);
	    }
	}
}


