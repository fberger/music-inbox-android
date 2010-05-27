package org.musicinbox.android;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import static org.musicinbox.android.MusicInbox.TAG;

public class RssActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss);
		
		Intent intent = getIntent();
		if (intent != null) {
			Uri uri = intent.getData();
			Log.d(TAG, uri.toString());
			// add new rss feed, hopefully we don't have one yet
		} else {
			// load existing rss feed
		}
	}
	
}
