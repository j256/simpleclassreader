package com.j256.simpleclassreader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

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
				FieldInfo fieldInfo = fields[i];
				Field reflectionField = null;
				for (Field field : reflectionFields) {
					if (fieldInfo.getName().equals(field.getName())) {
						reflectionField = field;
					}
				}
				assertNotNull(reflectionField);
				assertEquals(reflectionField.getName(), fieldInfo.getName());
				assertEquals(reflectionField.getType().getName(), fieldInfo.getDataDescriptor().getDataClassName());
				assertEquals(reflectionField.isSynthetic(), fieldInfo.isSynthetic());
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
			InnerClassInfo[] innerClasses = info.getInnerClasses();
			assertNotNull(innerClasses);
			assertEquals(1, innerClasses.length);
			for (int i = 0; i < innerClasses.length; i++) {
				System.out.println("interface " + i + ": " + innerClasses[i]);
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
}
