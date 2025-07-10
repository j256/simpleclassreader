package com.j256.simpleclassreader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ClassReaderErrorTest {

	@Test
	public void testStuff() {
		ClassReaderErrorType type = ClassReaderErrorType.ATTRIBUTE_NAME_INDEX_INVALID;
		assertNotNull(type.getMessage());
		String msg = "very very bad";
		ClassReaderError error = new ClassReaderError(type, msg);
		assertSame(type, error.getType());
		assertSame(msg, error.getDetails());
		assertEquals(type + ": " + msg, error.toString());

		int foo = 1234;
		type = ClassReaderErrorType.CLASS_NAME_INDEX_INVALID;
		error = new ClassReaderError(type, foo);
		assertSame(type, error.getType());
		assertEquals(Integer.toString(foo), error.getDetails());
		assertEquals(type + ": " + foo, error.toString());

		error = new ClassReaderError(type, (Object) null);
		assertSame(type, error.getType());
		assertNull(error.getDetails());
		assertEquals(type + "", error.toString());
	}
}
