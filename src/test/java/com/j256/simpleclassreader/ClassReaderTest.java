package com.j256.simpleclassreader;

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

public class ClassReaderTest {

	@Test
	public void testStuff() throws IOException {
		String path = classToPath(TestClass.class);
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo classInfo = ClassReader.readClass(fis);
			assertNotNull(classInfo);
			assertEquals(TestClass.class.getName(), classInfo.getClassName());
			assertEquals(Object.class.getName(), classInfo.getSuperClassName());
			Field[] reflectionFields = TestClass.class.getDeclaredFields();
			FieldInfo[] fields = classInfo.getFields();
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
				assertEquals(reflectionField.getType().getName(), fields[i].getDataType().getDataClassName());
				assertEquals(reflectionField.isSynthetic(), fields[i].isSynthetic());
			}
			Constructor<?>[] reflectionConstructors = TestClass.class.getDeclaredConstructors();
			MethodInfo[] constructors = classInfo.getConstructors();
			assertEquals(reflectionConstructors.length, constructors.length);
			// reflection shows the constructor as the type name
			Method[] reflectionMethods = TestClass.class.getDeclaredMethods();
			MethodInfo[] methods = classInfo.getMethods();
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
			}
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
			assertTrue(info.getAccessFlags() != 0);
			assertFalse(info.isAbstract());
			assertFalse(info.isSynthetic());
			assertFalse(info.isAnnotation());
			assertFalse(info.isFinal());
			assertFalse(info.isInterface());
			assertFalse(info.isEnum());
			assertFalse(info.isModule());
			// XXX: why?
			assertTrue(info.isSuper());
			String[] interfaces = info.getInterfaces();
			assertEquals(1, interfaces.length);
			assertEquals(Runnable.class.getName(), interfaces[0]);
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
		}
	}

	@Test
	public void testInterface() throws IOException {
		String path = classToPath(TestInterface.class);
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo info = ClassReader.readClass(fis);
			int major = info.getMajorVersion();
			assertTrue(major > 50);
			assertEquals(0, info.getMinorVersion());
			assertEquals(JdkVersion.fromMajor(major).getJdkString() + ".0", info.getJdkVersionString());
			assertTrue(info.getAccessFlags() != 0);
			assertTrue(info.isAbstract());
			assertFalse(info.isSynthetic());
			assertFalse(info.isAnnotation());
			assertFalse(info.isFinal());
			assertTrue(info.isInterface());
			assertFalse(info.isEnum());
			assertFalse(info.isModule());
			assertFalse(info.isSuper());
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
	}

	@SuppressWarnings("unused")
	private static interface TestInterface extends Runnable {
		public float changeBar(String message, float newBar);
	}
}
