package com.j256.simpleclassreader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void testReadLengthShort() throws IOException {
		testReadLength(102);
	}

	@Test
	public void testReadLengthLong() throws IOException {
		testReadLength(102400);
	}

	@Test
	public void testCoverage() {
		String result = Utils.classPathToPackage("hello/there");
		assertEquals("hello.there", result);
		String foo = "foo";
		assertEquals(foo, Utils.classPathToPackage(foo));
		assertNull(Utils.classPathToPackage(null));
		assertEquals("", Utils.classPathToPackage(""));
	}

	private void testReadLength(int length) throws IOException {
		byte[] input = new byte[length];
		new Random().nextBytes(input);
		byte[] output;
		try (InputStream stream = new ByteArrayInputStream(input); //
				DataInputStream dis = new DataInputStream(stream);) {
			output = Utils.readLength(dis, input.length - 1);
		}
		input = Arrays.copyOf(input, input.length - 1);
		assertArrayEquals(input, output);
	}
}
