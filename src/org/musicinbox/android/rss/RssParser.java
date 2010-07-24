package org.musicinbox.android.rss;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.SAXException;

import android.net.Uri;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;
import android.util.Xml.Encoding;

public class RssParser {

	public static Channel parse(InputStream in, Encoding encoding) throws IOException, SAXException {
		final SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
		RootElement rss = new RootElement("rss");
		final ChannelBuilder channelBuilder = new ChannelBuilder();
		final ItemBuilder itemBuilder = new ItemBuilder();
		
		Element channel = rss.getChild("channel");
		channel.getChild("title").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				channelBuilder.setTitle(body);
			}
		});
		channel.getChild("link").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				channelBuilder.setLink(Uri.parse(body));
			}
		});
		
		Element item = channel.getChild("item");
		item.getChild("title").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				itemBuilder.setTitle(body.trim());
			}
		});
		item.getChild("guid").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				itemBuilder.setGuid(body);
			}
		});
		item.getChild("link").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				Uri link = Uri.parse(body);
				itemBuilder.setLink(link);
			}
		});
		item.getChild("pubDate").setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				try {
					Date pubDate = dateFormatter.parse(body.trim());
					itemBuilder.setPubDate(pubDate);
				} catch (ParseException e) {
					// todo handle
				}
			}
		});
		
		item.setEndElementListener(new EndElementListener() {
			public void end() {
				channelBuilder.addItem(itemBuilder.build());
			}
		});
		
		Xml.parse(in, encoding, rss.getContentHandler());
		return channelBuilder.build();
	}
	
}
