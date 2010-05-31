package org.musicinbox.android.rss;

import java.io.Serializable;
import java.util.List;

import org.musicinbox.android.Utils;

import android.net.Uri;

/**
 * Represents an rss 2.0 channel.
 */
public class Channel {

	private final String title;
	private final Uri link;
	private final List<Item> items;

	public Channel(String title, Uri link, List<Item> items) {
		this.title = title;
		this.link = link;
		this.items = items;
	}

	public String getTitle() {
		return title;
	}
	
	public Uri getLink() {
		return link;
	}
	
	public List<Item> getItems() {
		return items;
	}
	
	@Override
	public String toString() {
		return Utils.toString(this);
	}
}
