package org.musicinbox.android.rss;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

public class ChannelBuilder {

	private String title;
	private Uri link;
	private List<Item> items = new ArrayList<Item>();

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setLink(Uri link) {
		this.link = link;
	}
	
	public void addItem(Item item) {
		this.items.add(item);
	}
	
	public Channel build() {
		return new Channel(title, link, items);
	}
	
}
