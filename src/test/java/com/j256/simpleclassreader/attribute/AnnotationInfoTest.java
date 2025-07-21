package com.j256.simpleclassreader.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import com.j256.simpleclassreader.ClassInfo;
import com.j256.simpleclassreader.ClassReader;
import com.j256.simpleclassreader.TestUtils;
import com.j256.simpleclassreader.attribute.InnerClassesAttribute.InnerClassInfo;

public class AnnotationInfoTest {

	@Test
	public void testAnnotations() throws IOException {
		String path = TestUtils.classToPath(AnnotationTest.class);
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo info = ClassReader.readClass(fis);
			AnnotationInfo[] annotations = info.getRuntimeAnnotations();
			assertNotNull(annotations);
			assertEquals(1, annotations.length);
			AnnotationNameValue[] values = annotations[0].getValues();
			assertNotNull(values);
			assertEquals(12, values.length);
			assertEquals((byte) 123, (byte) values[0].getConstByteValue());
			assertEquals('h', (char) values[1].getConstCharacterValue());
			assertEquals((short) 31241, (short) values[2].getConstShortValue());
			assertEquals(1021341, (int) values[3].getConstIntegerValue());
			assertEquals(3213123123123L, (long) values[4].getConstLongValue());
			assertEquals(1.23F, (float) values[5].getConstFloatValue(), 0);
			assertEquals(21348.2323D, (double) values[6].getConstDoubleValue(), 0);
			assertEquals("hello", values[7].getConstStringValue());
			assertEquals(true, values[8].getConstBooleanValue());
			assertEquals(MyNum.class.getName(), values[9].getEnumValue().getType());
			assertEquals(MyNum.BAR.name(), values[9].getEnumValue().getConstant());
			assertEquals(String.class.getName(), values[10].getClassValue());
			AnnotationNameValue[] arrayValues = values[11].getArrayValues();
			assertNotNull(arrayValues);
			// should match [ 1, 2, 3 ]
			assertEquals(3, arrayValues.length);
			assertEquals(2, arrayValues[0].getConstValue());
			assertEquals(4, (int) arrayValues[1].getConstIntegerValue());
			assertEquals(8, (int) arrayValues[2].getConstIntegerValue());
			InnerClassInfo[] innerClasses = info.getInnerClasses();
			assertNotNull(innerClasses);
			assertEquals(3, innerClasses.length);
			for (int i = 0; i < innerClasses.length; i++) {
				System.out.println(i + ": " + innerClasses[i]);
			}
			System.err.println("parse errors: " + info.getParseErrors());
		}
	}

	@Test
	public void testWrongTypes() throws IOException {
		String path = TestUtils.classToPath(AnnotationTest.class);
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo info = ClassReader.readClass(fis);
			AnnotationInfo[] annotations = info.getRuntimeAnnotations();
			assertNotNull(annotations);
			assertEquals(1, annotations.length);
			AnnotationNameValue[] values = annotations[0].getValues();
			assertNotNull(values);
			assertEquals(12, values.length);
			try {
				// should be byte
				values[0].getConstCharacterValue();
				fail("should have thrown");
			} catch (IllegalArgumentException iae) {
				// expected
			}
			try {
				// should be character
				values[1].getConstByteValue();
				fail("should have thrown");
			} catch (IllegalArgumentException iae) {
				// expected
			}
			try {
				// should be short
				values[2].getConstIntegerValue();
				fail("should have thrown");
			} catch (IllegalArgumentException iae) {
				// expected
			}
			try {
				// should be integer
				values[3].getConstShortValue();
				fail("should have thrown");
			} catch (IllegalArgumentException iae) {
				// expected
			}
			try {
				// should be long
				values[4].getConstFloatValue();
				fail("should have thrown");
			} catch (IllegalArgumentException iae) {
				// expected
			}
			try {
				// should be float
				values[5].getConstLongValue();
				fail("should have thrown");
			} catch (IllegalArgumentException iae) {
				// expected
			}
			try {
				// should be double
				values[6].getConstBooleanValue();
				fail("should have thrown");
			} catch (IllegalArgumentException iae) {
				// expected
			}
			try {
				// should be boolean
				values[6].getConstStringValue();
				fail("should have thrown");
			} catch (IllegalArgumentException iae) {
				// expected
			}
			try {
				// should be string
				values[7].getConstDoubleValue();
				fail("should have thrown");
			} catch (IllegalArgumentException iae) {
				// expected
			}
			try {
				// boolean
				values[8].getConstIntegerValue();
				fail("should have thrown");
			} catch (IllegalArgumentException iae) {
				// expected
			}
			System.err.println("parse errors: " + info.getParseErrors());
		}
	}

	@Retention(value = RetentionPolicy.RUNTIME)
	private @interface MyAnnotation {
		public byte byteValue();

		public char charValue();

		public short shortValue();

		public int intValue();

		public long longValue();

		public float floatValue();

		public double doubleValue();

		public String stringValue();

		public boolean booleanValue();

		public MyNum enumValue();

		public Class<?> classValue();

		public int[] arrayValue();
	}

	@MyAnnotation(byteValue = 123, charValue = 'h', shortValue = 31241, intValue = 1021341, longValue = 3213123123123L,
			floatValue = 1.23F, doubleValue = 21348.2323D, stringValue = "hello", booleanValue = true,
			enumValue = MyNum.BAR, classValue = String.class, arrayValue = { 2, 4, 8 })
	private static class AnnotationTest {
		// for testing annotations only
	}

	private static enum MyNum {
		FOO,
		BAR,
		BAZ,
		// end
		;
	}
}
