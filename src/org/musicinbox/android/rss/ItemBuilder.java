package org.musicinbox.android.rss;

import java.util.Date;

import android.net.Uri;

public class ItemBuilder {
	
	private String title;
	private String guid;
	private Date pubDate;
	private Uri link;

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}
	
	public void setLink(Uri link) {
		this.link = link;
	}
	
	public Item build() {
		Item item = new Item(title, guid, pubDate, link);
		title = null;
		guid = null;
		pubDate = null;
		link = null;
		return item;
	}
}
