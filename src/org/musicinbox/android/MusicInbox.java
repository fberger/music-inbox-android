package org.musicinbox.android;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;


public class MusicInbox extends Activity {
	
	private static final String TAG = "MusicInbox";
	
	private static final URI postUri = URI.create("http://192.168.1.101:8000/api/json/");
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(TAG, "All the titles");
        String []  star = {"*"};
        Map<String, Set<String>> albumsByArtists = new HashMap<String, Set<String>>(); 
        Uri allAudioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor ca = managedQuery(allAudioUri, star, null, null, null);
        for (ca.moveToFirst(); !ca.isAfterLast(); ca.moveToNext()){
        	String artist = ca.getString(ca.getColumnIndex("artist")); 
        	String album = ca.getString(ca.getColumnIndex("album"));
        	if (artist != null && album != null) {
        		Set<String> albums = albumsByArtists.get(artist);
				if (albums == null) {
					albums = new HashSet<String>();
					albumsByArtists.put(artist, albums);
				}
				albums.add(album);
        	}
        }
        ca.close();
        
        JSONObject json = toJson(albumsByArtists);
        
        // post json
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(postUri);
        try {
			post.setEntity(new StringEntity(json.toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		try {
			httpClient.execute(post);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
    }

	private JSONObject toJson(Map<String, Set<String>> albumsByArtists) {
		JSONObject json = new JSONObject();
		for (Entry<String, Set<String>> entry : albumsByArtists.entrySet()) {
			String artist = entry.getKey();
			for (String album : entry.getValue()) {
				try {
					json.accumulate(artist, album);
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return json;
	}
}