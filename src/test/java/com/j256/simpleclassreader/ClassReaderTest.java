package com.j256.simpleclassreader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

import com.j256.simpleclassreader.attribute.AnnotationInfo;
import com.j256.simpleclassreader.attribute.AnnotationNameValue;
import com.j256.simpleclassreader.attribute.InnerClassesAttribute.InnerClassInfo;

public class ClassReaderTest {

	@Test
	public void testStuff() throws IOException {
		String path = classToPath(TestClass.class);
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo info = ClassReader.readClass(fis);
			assertNotNull(info);
			assertEquals(TestClass.class.getName(), info.getClassName());
			assertEquals(Object.class.getName(), info.getSuperClassName());
			Field[] reflectionFields = TestClass.class.getDeclaredFields();
			FieldInfo[] fields = info.getFields();
			// NOTE: there might be artificial fields when running with coverage data
			for (int i = 0; i < fields.length; i++) {
				Field reflectionField = null;
				for (Field field : reflectionFields) {
					if (fields[i].getName().equals(field.getName())) {
						reflectionField = field;
					}
				}
				assertNotNull(reflectionField);
				assertEquals(reflectionField.getName(), fields[i].getName());
				assertEquals(reflectionField.getType().getName(), fields[i].getDataDescriptor().getDataClassName());
				assertEquals(reflectionField.isSynthetic(), fields[i].isSynthetic());
			}
			Constructor<?>[] reflectionConstructors = TestClass.class.getDeclaredConstructors();
			MethodInfo[] constructors = info.getConstructors();
			assertEquals(reflectionConstructors.length, constructors.length);
			// reflection shows the constructor as the type name
			Method[] reflectionMethods = TestClass.class.getDeclaredMethods();
			MethodInfo[] methods = info.getMethods();
			// NOTE: there might be artificial methods when running with coverage data
			for (int i = 0; i < methods.length; i++) {
				Method reflectionMethod = null;
				for (Method method : reflectionMethods) {
					if (methods[i].getName().equals(method.getName())) {
						reflectionMethod = method;
					}
				}
				assertNotNull(reflectionMethod);
				assertEquals(reflectionMethod.getName(), methods[i].getName());
				assertEquals(reflectionMethod.getReturnType().getName(),
						methods[i].getReturnDescriptor().getDataClassName());
				assertEquals(reflectionMethod.isBridge(), methods[i].isBridge());
				assertEquals(reflectionMethod.isVarArgs(), methods[i].isVarargs());
				assertEquals(reflectionMethod.isSynthetic(), methods[i].isSynthetic());
				if ("changeBar".equals(methods[i].getName())) {
					assertTrue(methods[i].isDeprecated());
				} else {
					assertFalse(methods[i].isDeprecated());
				}
			}
			System.err.println("parse errors: " + info.getParseErrors());
		}
	}

	@Test
	public void testCoverage() throws IOException {
		String path = classToPath(TestClass.class);
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo info = ClassReader.readClass(fis);
			int major = info.getMajorVersion();
			assertTrue(major > 50);
			assertEquals(0, info.getMinorVersion());
			assertEquals(JdkVersion.fromMajor(major).getJdkString() + ".0", info.getJdkVersionString());
			assertArrayEquals(new AccessFlag[] { AccessFlag.SUPER }, info.getAccessFlags());
			assertFalse(info.isAbstract());
			assertFalse(info.isSynthetic());
			assertFalse(info.isAnnotation());
			assertFalse(info.isFinal());
			assertFalse(info.isInterface());
			assertFalse(info.isEnum());
			assertFalse(info.isModule());
			// XXX: why?
			assertTrue(info.isSuper());
			InnerClassInfo[] innerClasses = info.getInnerClasses();
			assertNotNull(innerClasses);
			assertEquals(2, innerClasses.length);
			for (int i = 0; i < innerClasses.length; i++) {
				System.out.println(i + ": " + innerClasses[i]);
			}
			String[] interfaces = info.getInterfaces();
			assertEquals(1, interfaces.length);
			assertEquals(Runnable.class.getName(), interfaces[0]);
			System.err.println("parse errors: " + info.getParseErrors());
		}
	}

	@Test
	public void testExceptions() throws IOException {
		String path = classToPath(TestClass.class);
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo info = ClassReader.readClass(fis);
			MethodInfo[] methods = info.getMethods();
			boolean found = false;
			for (MethodInfo method : methods) {
				if ("changeBar".equals(method.getName())) {
					String[] exceptions = method.getExceptions();
					assertNotNull(exceptions);
					assertEquals(1, exceptions.length);
					assertEquals("java.io.IOException", exceptions[0]);
					found = true;
				}
			}
			assertTrue(found);
			System.err.println("parse errors: " + info.getParseErrors());
		}
	}

	@Test
	public void testInterface() throws IOException {
		String path = classToPath(TestInterface.class);
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo info = ClassReader.readClass(fis);
			assertArrayEquals(new AccessFlag[] { AccessFlag.INTERFACE, AccessFlag.ABSTRACT }, info.getAccessFlags());
			assertTrue(info.isAbstract());
			assertFalse(info.isSynthetic());
			assertFalse(info.isAnnotation());
			assertFalse(info.isFinal());
			assertTrue(info.isInterface());
			assertFalse(info.isEnum());
			assertFalse(info.isModule());
			assertFalse(info.isSuper());
			InnerClassInfo[] innerClasses = info.getInnerClasses();
			assertNotNull(innerClasses);
			assertEquals(1, innerClasses.length);
			for (int i = 0; i < innerClasses.length; i++) {
				System.out.println("interface " + i + ": " + innerClasses[i]);
			}
			System.err.println("parse errors: " + info.getParseErrors());
		}
	}

	@Test
	public void testAnnotations() throws IOException {
		String path = classToPath(AnnotationTest.class);
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo info = ClassReader.readClass(fis);
			AnnotationInfo[] annotations = info.getRuntimeAnnotations();
			assertNotNull(annotations);
			assertEquals(1, annotations.length);
			AnnotationNameValue[] values = annotations[0].getValues();
			assertNotNull(values);
			assertEquals(12, values.length);
			assertEquals((byte) 123, values[0].getConstValue());
			assertEquals('h', values[1].getConstValue());
			assertEquals((short) 31241, values[2].getConstValue());
			assertEquals(1021341, values[3].getConstValue());
			assertEquals(3213123123123L, values[4].getConstValue());
			assertEquals(1.23F, values[5].getConstValue());
			assertEquals(21348.2323D, values[6].getConstValue());
			assertEquals("hello", values[7].getConstValue());
			assertEquals(true, values[8].getConstValue());
			assertEquals(MyNum.class.getName(), values[9].getEnumValue().getType());
			assertEquals(MyNum.BAR.name(), values[9].getEnumValue().getConstant());
			assertEquals(String.class.getName(), values[10].getClassValue());
			AnnotationNameValue[] arrayValues = values[11].getArrayValues();
			assertNotNull(arrayValues);
			// should match [ 1, 2, 3 ]
			assertEquals(3, arrayValues.length);
			assertEquals(2, arrayValues[0].getConstValue());
			assertEquals(4, arrayValues[1].getConstIntValue());
			assertEquals(8, arrayValues[2].getConstIntValue());
			InnerClassInfo[] innerClasses = info.getInnerClasses();
			assertNotNull(innerClasses);
			assertEquals(3, innerClasses.length);
			for (int i = 0; i < innerClasses.length; i++) {
				System.out.println(i + ": " + innerClasses[i]);
			}
			System.err.println("parse errors: " + info.getParseErrors());
		}
	}

	private String classToPath(Class<?> clazz) {
		return "target/test-classes/" + clazz.getName().replace('.', '/') + ".class";
	}

	@SuppressWarnings("unused")
	private static class TestClass implements Runnable {

		public static int foo;
		private float bar;
		protected String zip;

		public TestClass(float bar) {
			this.bar = bar;
		}

		@Deprecated
		public float changeBar(String message, float newBar) throws IOException {
			bar = newBar;
			return bar;
		}

		@Override
		public void run() {
			// do thread stuff
		}

		public static class Inner {
			// empty
		}
	}

	private static interface TestInterface extends Runnable {
		public float changeBar(String message, float newBar);
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
