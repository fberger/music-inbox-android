package org.musicinbox.android;

import static org.musicinbox.android.MusicInbox.TAG;

import java.io.IOException;
import java.io.InputStream;

import org.musicinbox.android.rss.Channel;
import org.musicinbox.android.rss.RssParser;
import org.xml.sax.SAXException;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

public class RssActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss);

		testRss();
		
		Intent intent = getIntent();
		if (intent != null) {
			Uri uri = intent.getData();
			Log.d(TAG, "rss uri" + uri.toString());
			// add new rss feed, hopefully we don't have one yet
		} else {
			// load existing rss feed
		}
	}

	public void testRss() {
		InputStream in = getResources().openRawResource(R.raw.rss);
		try {
			Channel channel = RssParser.parse(in, Xml.Encoding.UTF_8);
			Log.d(TAG, channel.getTitle());
			Log.d(TAG, channel.getLink().toString());
		} catch (IOException e) {
			Log.d(TAG, e.toString());
		} catch (SAXException e) {
			Log.d(TAG, e.toString());
		}
	}
}
