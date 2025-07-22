package com.j256.simpleclassreader.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.j256.simpleclassreader.AttributeInfo;
import com.j256.simpleclassreader.ClassInfo;
import com.j256.simpleclassreader.ClassReader;
import com.j256.simpleclassreader.MethodInfo;
import com.j256.simpleclassreader.TestUtils;
import com.j256.simpleclassreader.attribute.LineNumberTableAttribute.LineNumberLocation;

public class LineNumberTableAttributeTest {

	@Test
	public void testStuff() throws IOException {
		int lineNumber = new LineNumberTest().findLineNumber();

		String path = TestUtils.classToPath(LineNumberTest.class);
		ClassInfo classInfo = ClassReader.readClass(new File(path));
		boolean found = false;
		for (MethodInfo method : classInfo.getMethods()) {
			if (!"findLineNumber".equals(method.getName())) {
				continue;
			}
			assertNotNull(method.getCode());
			for (AttributeInfo attribute : method.getAttributes()) {
				if (attribute.getType() != AttributeType.CODE) {
					continue;
				}
				found = true;
				LineNumberLocation[] locations = ((CodeAttribute) attribute.getValue()).getLineNumberLocations();
				assertNotNull(locations);
				assertEquals(2, locations.length);
				assertEquals(lineNumber, locations[0].getLineNumber());
			}
		}
		System.err.println("parse errors: " + classInfo.getParseErrors());
		assertTrue(found);
	}

	private static class LineNumberTest {
		public int findLineNumber() {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			return stackTrace[1].getLineNumber();
		}
	}
}
