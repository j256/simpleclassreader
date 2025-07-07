package com.j256.simpleclassreader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class JdkVersionTest {

	@Test
	public void testStuff() {
		int minor = 123;
		assertFalse(JdkVersion.JDK_1_1.makeJdkString(minor).contains(Integer.toString(minor)));
		assertNull(JdkVersion.fromMajor(-1));
		assertNull(JdkVersion.fromMajor(10000000));
		int major = 50;
		assertEquals(major, JdkVersion.fromMajor(major).getMajorVersion());
	}
}
