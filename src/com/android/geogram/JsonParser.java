package com.android.geogram;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonParser {
	
	public static InstaData getImageLinkArray(JSONObject Jsonstring) throws JSONException {
		
		Log.i(JsonParser.class.getName(), "We're inside JSONParser");
		
		double latitude, longitude;
		JSONObject images;
		String url;
		JSONObject location, likes, user;
		int count, id;

		// Required properties
		location = Jsonstring.getJSONObject("location");
		latitude = (location.getDouble("latitude"));	
		longitude = (location.getDouble("longitude"));
		
		likes = Jsonstring.getJSONObject("likes");
		count = (likes.getInt("count"));
		
		images = Jsonstring.getJSONObject("images");
		images = images.getJSONObject("low_resolution");
		url = images.getString("url");
		
		user = Jsonstring.getJSONObject("user");
		id = (user.getInt("id"));
			
		InstaData data = new InstaData();
		
		data.latitude = latitude;
		data.longitude = longitude;
		data.count = count;
		data.url = url;
		data.id = id;
		data.username = user.getString("username");
		data.time = Jsonstring.getString("created_time");
		
		return data;		
	}				
	
}
