package org.musicinbox.android;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;


public class MusicInbox extends Activity {
	
	private static final String TAG = "MusicInbox";
	
	private static final URI postUri = URI.create("http://192.168.0.1:8000/api/library/form/");
	
	private static final int NO_ARTIST_DATA_DIALOG = 0;
	
	private static final int POST_ERROR_DIALOG = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(TAG, "All the titles");
        new QueryAritistsTask().execute();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case NO_ARTIST_DATA_DIALOG:
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("No artist data found in your local collection");
    		builder.setPositiveButton("OK", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
    		return builder.create();
    	case POST_ERROR_DIALOG:
    		builder = new AlertDialog.Builder(this);
    		builder.setMessage("Error uploading artist data");
    		builder.setPositiveButton("OK", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
    		return builder.create();
    	default:
    		throw new IllegalArgumentException("unhandled id " + id);
    	}
    }

    private List<NameValuePair> toFormData(Map<String, Set<String>> albumsByArtists) {
    	List<NameValuePair> formData = new ArrayList<NameValuePair>(albumsByArtists.size() * 2);
    	for (Entry<String, Set<String>> entry : albumsByArtists.entrySet()) {
    		String artist = entry.getKey();
    		for (String album : entry.getValue()) {
    			formData.add(new BasicNameValuePair(artist, album));
    		}
    	}
    	return formData;
    }
    
    private class QueryAritistsTask extends AsyncTask<Void, Void, Map<String, Set<String>>> {

		@Override
		protected Map<String, Set<String>> doInBackground(Void... params) {
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
	        albumsByArtists.put("name", Collections.singleton("My Android Artists"));
	        return albumsByArtists;
		}
    	
		@Override
		protected void onPostExecute(Map<String, Set<String>> result) {
			Log.d(TAG, result.toString());
			if (result.isEmpty()) {
				showDialog(NO_ARTIST_DATA_DIALOG);
			} else {
				// show in ui maybe
				new PostArtistsDataTask().execute(result);
			}
		}
    }
    
    private class PostArtistsDataTask extends AsyncTask<Map<String, Set<String>>, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Map<String, Set<String>>... params) {
			DefaultHttpClient httpClient = new DefaultHttpClient();
	        HttpPost post = new HttpPost(postUri);
	        HttpResponse response = null;
	        HttpParams httpParams = post.getParams();
	        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
	        HttpConnectionParams.setSoTimeout(httpParams, 5000);
	        try {
	        	post.setEntity(new UrlEncodedFormEntity(toFormData(params[0]), "UTF-8"));
	        	Log.d(TAG, "posting data");
				response = httpClient.execute(post);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						String json = EntityUtils.toString(entity);
						Log.d(TAG, json);
						return new JSONObject(json);
					} else {
						Log.e(TAG, "no http entity");
					}
				}
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			} finally {
				Utils.close(response);
			}
			return null;
		}
    	
		
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result == null) {
				Log.d(TAG, "error posting");
				showDialog(POST_ERROR_DIALOG);
				return;
			}
			// post rss intent
			String rssUri = result.optString("rssUri", null);
			if (rssUri == null) {
				showDialog(POST_ERROR_DIALOG);
			} else {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse(rssUri), "application/rss+xml");
				startActivity(intent);
			}
		}
    }
}