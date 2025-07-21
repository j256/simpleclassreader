package com.j256.simpleclassreader;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Some central utility methods.
 * 
 * @author graywatson
 */
public class Utils {

	private static final int BUFFER_SIZE = 16384;

	/**
	 * Return a byte[] with the length bytes from the input-stream.
	 */
	public static byte[] readBytes(DataInputStream dis, int length) throws IOException {
		// shortcut if the length is small, no reason to pay for the reallocs
		if (length <= BUFFER_SIZE) {
			byte[] bytes = new byte[length];
			dis.readFully(bytes);
			return bytes;
		}

		/*
		 * We jump through these hoops because we are worried that length is invalid and we want to hit the EOF before
		 * we do the new byte[length] and possibly allocate a huge buffer unnecessarily. Yes, this will cause us to
		 * realloc a bunch if the byte[] is large.
		 */
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[BUFFER_SIZE];
		while (length > 0) {
			int max = buffer.length;
			if (max > length) {
				max = length;
			}
			dis.readFully(buffer, 0, max);
			baos.write(buffer, 0, max);
			length -= max;
		}
		return baos.toByteArray();
	}

	/**
	 * Convert from a class path with '/' to a package with '.'.
	 */
	public static String classPathToPackage(String path) {
		if (path == null || path.isEmpty()) {
			return path;
		} else {
			return path.replace('/', '.');
		}
	}
}
