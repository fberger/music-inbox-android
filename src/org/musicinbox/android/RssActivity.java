package org.musicinbox.android;

import static org.musicinbox.android.MusicInbox.TAG;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.musicinbox.android.rss.Channel;
import org.musicinbox.android.rss.Item;
import org.musicinbox.android.rss.RssParser;
import org.xml.sax.SAXException;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RssActivity extends ListActivity {
	
	private static final int PROGRESS_DIALOG = 0;
	/**
	 * Dialog completely managed by {@link GetRssChannelTask}.
	 */
	private ProgressDialog progressDialog;
	
	public static final int SCAN_ARTISTS_REQUEST = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss);

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		if (preferences.getString("rssUri", null) == null) {
        	startActivityForResult(new Intent(getApplicationContext(), MusicInbox.class),
        			SCAN_ARTISTS_REQUEST);
        } else {
        	Intent intent = getIntent();
        	handleIntent(intent);
		}
	}
	
	private void handleIntent(Intent intent) {
		Uri uri = intent.getData();
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    	if (uri != null) {
    		Log.d(TAG, "rss uri" + uri.toString());
    		preferences.edit().putString("rssUri", uri.toString()).commit();
    		new GetRssChannelTask().execute(uri);
    	} else {
    		// load existing rss feed
    		String rssUri = preferences.getString("rssUri", null);
    		if (rssUri != null) {
    			new GetRssChannelTask().execute(Uri.parse(rssUri));
    		}
    	}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == SCAN_ARTISTS_REQUEST && resultCode == MusicInbox.SCAN_ARTISTS_RESULT) {
			handleIntent(intent);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public ArrayAdapter<Item> getListAdapter() {
		return (ArrayAdapter<Item>) super.getListAdapter();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Fetching new releases");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			return progressDialog;
		default:
			throw new IllegalArgumentException("uknown dialog");
		}
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Item item = getListAdapter().getItem(position);
		Uri link = item.getLink();
		if (link != null) {
			Intent intent = new Intent(Intent.ACTION_VIEW, link);
			startActivity(intent);
		}
	}
	
	private class GetRssChannelTask extends AsyncTask<Uri, Integer, Channel> implements ProgressMonitor {
		
		@Override
		protected void onPreExecute() {
			showDialog(PROGRESS_DIALOG);
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			progressDialog.setProgress(values[0]);
		}
		
		@Override
		protected void onPostExecute(Channel channel) {
			progressDialog.dismiss();
			progressDialog = null;
			if (channel == null) {
				// error handling
				return;
			} 
			Log.d(TAG, channel.toString());
			TextView titleView = (TextView) findViewById(R.id.rss_title);
			titleView.setText(channel.getTitle());
			setListAdapter(new RssItemAdapter(RssActivity.this, channel.getItems()));
		}

		@Override
		protected Channel doInBackground(Uri... uris) {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet get = new HttpGet(uris[0].toString());
			HttpResponse response = null;
			publishProgress(500);
			try {
				response = httpClient.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					publishProgress(1000);
					HttpEntity entity = response.getEntity();
					if (entity != null) { 
						Channel channel = RssParser.parse(new ProgressInputStream(entity.getContent(), this, 6200), Xml.Encoding.UTF_8);
						return channel;
					}
				} else {
					Log.d(TAG, "status line: " + response.getStatusLine());
				}
			} catch (IOException e) {
				Log.d(TAG, e.toString());
			} catch (SAXException e) {
				Log.d(TAG, e.toString());
			} finally {
				Utils.close(response);
			}
			return null;
		}

		public void updateProgress(int progress) {
			Log.d(TAG, "progress " + progress);
			publishProgress(1000 + 90 * progress);
		}
	}
	
	private class RssItemAdapter extends ArrayAdapter<Item> {

		private final LayoutInflater layoutInflater;
		
		private final SimpleDateFormat pubDateFormat = new SimpleDateFormat("MM/dd/yyyy");

		public RssItemAdapter(Context context,	List<Item> items) {
			super(context, 0, items);
			layoutInflater = LayoutInflater.from(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView != null ? convertView : layoutInflater.inflate(R.layout.rss_item, null);
			Item item = getItem(position);
			TextView titleView = (TextView) view.findViewById(R.id.rss_item_title);
			titleView.setText(item.getTitle());
			TextView pubDateView = (TextView) view.findViewById(R.id.rss_item_pub_date);
			pubDateView.setText(pubDateFormat.format(item.getPubDate()));
			return view;
		}
		
	}
	
}