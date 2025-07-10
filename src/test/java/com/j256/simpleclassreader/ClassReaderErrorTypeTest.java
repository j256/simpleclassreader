package com.j256.simpleclassreader;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ClassReaderErrorTypeTest {

	@Test
	public void testCoverage() {
		for (ClassReaderErrorType type : ClassReaderErrorType.values()) {
			assertNotNull(type.getMessage());
		}
	}
}
