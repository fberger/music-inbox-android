package org.musicinbox.android;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class Utils {

	public static void close(HttpResponse response) {
		if (response != null) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException e) {
				}
			}
		}
	}
	
}
