package com.j256.simpleclassreader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class that reads in class bytes and returns a {@link ClassInfo}.
 */
public class ClassReader {

	/**
	 * Read in a {@link ClassInfo} from a byte array.
	 * 
	 * @param classBytes
	 *            Array of bytes that contains the class.
	 * @throws EOFException
	 *             If the end of the buffer was reached prematurely.
	 * @throws IOException
	 *             General input error..
	 */
	public static ClassInfo readClass(byte[] classBytes) throws EOFException, IOException {
		return readClass(new ByteArrayInputStream(classBytes));
	}

	/**
	 * Read in a {@link ClassInfo} from a byte array starting at offset of length bytes.
	 * 
	 * @param classBytes
	 *            Array of bytes that contains the class.
	 * @param offset
	 *            Start of the bytes of the class.
	 * @param length
	 *            Length of the class bytes to read.
	 * @throws EOFException
	 *             If the end of the buffer was reached prematurely.
	 * @throws IOException
	 *             General input error..
	 */
	public static ClassInfo readClass(byte[] classBytes, int offset, int length) throws EOFException, IOException {
		return readClass(new ByteArrayInputStream(classBytes, offset, length));
	}

	/**
	 * Read in a {@link ClassInfo} using the input-stream which will _not_ be closed.
	 * 
	 * @throws EOFException
	 *             If the end of the buffer was reached prematurely.
	 * @throws IOException
	 *             General input error..
	 */
	public static ClassInfo readClass(InputStream inputStream) throws EOFException, IOException {
		try (DataInputStream dis = new DataInputStream(inputStream);) {
			return ClassInfo.read(dis);
		}
	}
}
