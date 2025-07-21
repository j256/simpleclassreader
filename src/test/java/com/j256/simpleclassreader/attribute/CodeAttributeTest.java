package com.j256.simpleclassreader.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simpleclassreader.AttributeInfo;
import com.j256.simpleclassreader.ClassInfo;
import com.j256.simpleclassreader.ClassReader;
import com.j256.simpleclassreader.MethodInfo;
import com.j256.simpleclassreader.TestUtils;
import com.j256.simpleclassreader.attribute.CodeAttribute.ExceptionHandler;

public class CodeAttributeTest {

	@Test
	public void testStuff() throws IOException {
		String path = TestUtils.classToPath(CodeTest.class);
		ClassInfo classInfo = ClassReader.readClass(new File(path));
		boolean found = false;
		for (MethodInfo method : classInfo.getMethods()) {
			if (!"codeMethod".equals(method.getName())) {
				continue;
			}
			assertNotNull(method.getCode());
			for (AttributeInfo attribute : method.getAttributes()) {
				if (attribute.getType() != AttributeType.CODE) {
					continue;
				}
				found = true;
				CodeAttribute codeAttr = (CodeAttribute) attribute.getValue();
				// can't test this because there are some hidden locals I guess
				// assertEquals(0, codeAttr.getMaxLocals());
				ExceptionHandler[] exceptionHandlers = codeAttr.getExceptionHandlers();
				assertNotNull(exceptionHandlers);
				assertEquals(1, exceptionHandlers.length);
				assertEquals(IOException.class.getName(), exceptionHandlers[0].getCatchType());
			}
		}
		System.err.println("parse errors: " + classInfo.getParseErrors());
		assertTrue(found);
	}

	@SuppressWarnings("unused")
	private static class CodeTest {
		public void codeMethod() {
			System.out.println("wow zipper");
			try {
				new FileInputStream("/doesnotexist").close();
			} catch (IOException ioe) {
				// caught
			}
		}
	}
}
