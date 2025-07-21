package com.j256.simpleclassreader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
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
	 *             If the end of the buffer was reached prematurely. This probably indicates truncated or corrupted
	 *             class information.
	 * @throws IOException
	 *             General input problem..
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
	 *            Start of the bytes of the class in the buffer.
	 * @param length
	 *            Length of the class bytes to read from the buffer.
	 * @throws EOFException
	 *             If the end of the buffer was reached prematurely. This probably indicates truncated or corrupted
	 *             class information.
	 * @throws IOException
	 *             General input problem..
	 */
	public static ClassInfo readClass(byte[] classBytes, int offset, int length) throws EOFException, IOException {
		return readClass(new ByteArrayInputStream(classBytes, offset, length));
	}

	/**
	 * Read in a {@link ClassInfo} using the input-stream which will _not_ be closed.
	 * 
	 * @param inputStream
	 *            Input stream to read the class bytes from. The stream should be closed by the caller.
	 * @throws EOFException
	 *             If the end of the input was reached prematurely. This probably indicates truncated or corrupted class
	 *             information.
	 * @throws IOException
	 *             General input problem..
	 */
	public static ClassInfo readClass(InputStream inputStream) throws EOFException, IOException {
		DataInputStream dis = new DataInputStream(inputStream);
		return ClassInfo.read(dis);
		// NOTE: dis is not closed on purpose because that would close the underlying input-stream
	}

	/**
	 * Read in a {@link ClassInfo} from a class file.
	 * 
	 * @param file
	 *            Class file from disk.
	 * @throws EOFException
	 *             If the end of the input was reached prematurely. This probably indicates truncated or corrupted class
	 *             information.
	 * @throws IOException
	 *             General input problem..
	 */
	public static ClassInfo readClass(File file) throws EOFException, IOException {
		try (DataInputStream dis = new DataInputStream(new FileInputStream(file));) {
			return ClassInfo.read(dis);
		}
	}
}
