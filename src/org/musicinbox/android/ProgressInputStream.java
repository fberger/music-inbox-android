package org.musicinbox.android;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class ProgressInputStream extends FilterInputStream {

	private long read = 0;
	
	private int previous = 0;

	private final long toRead;

	private final ProgressMonitor progressMonitor;

	public ProgressInputStream(InputStream in, ProgressMonitor progressMonitor,
			long toRead) {
		super(in);
		this.progressMonitor = progressMonitor;
		this.toRead = toRead;
		if (toRead <= 0) {
			throw new IllegalArgumentException();
		}
	}

	private long updateProgress(long read) {
		this.read += read;
		Log.d("MusicInbox", "previous " + previous);
		int current = (int)(100 * this.read / toRead);
		Log.d("MusicInbox", "current " + current);
		if (current > previous) {
			previous = current;
			progressMonitor.updateProgress(current);
		}
		return read;
	}
	
	@Override
	public int read() throws IOException {
		int read = super.read();
		if (read != -1) {
			updateProgress(1);
		}
		return read;
	}
	
	@Override
	public long skip(long count) throws IOException {
		return updateProgress(super.skip(count));
	}
	
	@Override
	public int read(byte[] buffer, int offset, int count) throws IOException {
		return (int)updateProgress(super.read(buffer, offset, count));
	}

}
