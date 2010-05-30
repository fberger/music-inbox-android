package org.musicinbox.android.rss;

import java.util.Date;

import org.musicinbox.android.Utils;

import android.net.Uri;

public class Item {
	
	private final String title;
	private final String guid;
	private final Date pubDate;
	private final Uri link;

	public Item(String title, String guid, Date pubDate, Uri link) {
		this.title = title;
		this.guid = guid;
		this.pubDate = pubDate;
		this.link = link;
	}

	public String getTitle() {
		return title;
	}
	
	public Uri getLink() {
		return link;
	}
	
	public Date getPubDate() {
		return pubDate;
	}
	
	public String getGuid() {
		return guid;
	}

	@Override
	public String toString() {
		return Utils.toString(this);
	}
}
