package com.j256.simpleclassreader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
		String path = "target/test-classes/" + TestClass.class.getName().replace('.', '/') + ".class";
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo info = ClassReader.readClass(fis);
			assertNotNull(info);
			assertEquals(TestClass.class.getName(), info.getClassName());
			assertEquals(Object.class.getName(), info.getSuperClassName());
			Field[] reflectionFields = TestClass.class.getDeclaredFields();
			FieldInfo[] fields = info.getFields();
			assertEquals(reflectionFields.length, fields.length);
			for (int i = 0; i < fields.length; i++) {
				assertEquals(reflectionFields[i].getName(), fields[i].getName());
				assertEquals(reflectionFields[i].getType().getName(), fields[i].getDataType().getDataClassName());
				assertEquals(reflectionFields[i].isSynthetic(), fields[i].isSynthetic());
			}
			Constructor<?>[] reflectionConstructors = TestClass.class.getDeclaredConstructors();
			MethodInfo[] constructors = info.getConstructors();
			assertEquals(reflectionConstructors.length, constructors.length);
			// reflection shows the constructor as the type name
			Method[] reflectionMethods = TestClass.class.getDeclaredMethods();
			MethodInfo[] methods = info.getMethods();
			assertEquals(reflectionMethods.length, methods.length);
			for (int i = 0; i < methods.length; i++) {
				assertEquals(reflectionMethods[i].getName(), methods[i].getName());
				assertEquals(reflectionMethods[i].getReturnType().getName(),
						methods[i].getReturnDescriptor().getDataClassName());
				assertEquals(reflectionMethods[i].isBridge(), methods[i].isBridge());
				assertEquals(reflectionMethods[i].isVarArgs(), methods[i].isVarargs());
				assertEquals(reflectionMethods[i].isSynthetic(), methods[i].isSynthetic());
			}
		}
	}

	@SuppressWarnings("unused")
	private static class TestClass implements Runnable {

		public int foo;
		private float bar;
		protected String zip;

		public TestClass(float bar) {
			this.bar = bar;
		}

		@Deprecated
		public float changeBar(String message, float newBar) {
			bar = newBar;
			return bar;
		}

		@Override
		public void run() {
			// do thread stuff
		}
	}
}
