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
import java.lang.reflect.Method;

import org.junit.Test;

import com.j256.simpleclassreader.attribute.AnnotationInfo;

public class MethodInfoTest {

	@Test
	public void testStuff() throws IOException {
		String path = classToPath(TestClass.class);
		try (InputStream fis = new FileInputStream(path);) {
			ClassInfo info = ClassReader.readClass(fis);
			assertNotNull(info);
			assertEquals(TestClass.class.getName(), info.getClassName());
			assertEquals(Object.class.getName(), info.getSuperClassName());
			Constructor<?>[] reflectionConstructors = TestClass.class.getDeclaredConstructors();
			MethodInfo[] constructors = info.getConstructors();
			assertEquals(reflectionConstructors.length, constructors.length);
			// reflection shows the constructor as the type name
			Method[] reflectionMethods = TestClass.class.getDeclaredMethods();
			MethodInfo[] methods = info.getMethods();
			// NOTE: there might be artificial methods when running with coverage data
			for (int i = 0; i < methods.length; i++) {
				Method reflectionMethod = null;
				MethodInfo methodInfo = methods[i];
				for (Method method : reflectionMethods) {
					if (methodInfo.getName().equals(method.getName())) {
						reflectionMethod = method;
					}
				}
				assertNotNull(reflectionMethod);
				assertEquals(reflectionMethod.getName(), methodInfo.getName());
				assertEquals(reflectionMethod.getReturnType().getName(),
						methodInfo.getReturnDescriptor().getDataClassName());
				assertEquals(reflectionMethod.isBridge(), methodInfo.isBridge());
				assertEquals(reflectionMethod.isVarArgs(), methodInfo.isVarargs());
				assertEquals(reflectionMethod.isSynthetic(), methodInfo.isSynthetic());
				if ("changeBar".equals(methodInfo.getName())) {
					assertTrue(methodInfo.isDeprecated());
					AnnotationInfo[] annotations = methodInfo.getRuntimeAnnotations();
					assertNotNull(annotations);
					assertEquals(1, annotations.length);
					assertEquals(Float.TYPE, methodInfo.getReturnDescriptor().getDataClass());
					DataDescriptor[] paramDescriptors = methodInfo.getParameterDataDescriptors();
					assertNotNull(paramDescriptors);
					assertEquals(2, paramDescriptors.length);
					assertEquals(AccessFlag.PUBLIC.getBit(), methodInfo.getAccessFlagsValue());
					assertArrayEquals(new AccessFlag[] { AccessFlag.PUBLIC }, methodInfo.getAccessFlags());
					assertTrue(methodInfo.isPublic());
					assertFalse(methodInfo.isPrivate());
					assertFalse(methodInfo.isProtected());
					assertFalse(methodInfo.isStatic());
					assertFalse(methodInfo.isFinal());
					assertFalse(methodInfo.isSynchronized());
					assertFalse(methodInfo.isBridge());
					assertFalse(methodInfo.isVarargs());
					assertFalse(methodInfo.isNative());
					assertFalse(methodInfo.isAbstract());
					assertFalse(methodInfo.isStrict());
					assertFalse(methodInfo.isSynthetic());
					assertFalse(methodInfo.isConstructor());
					assertEquals("(Ljava/lang/String;F)F", methodInfo.getMethodDescriptor().toString());
					assertArrayEquals(new String[] { "java.io.IOException" }, methodInfo.getExceptions());
					assertEquals("method " + methodInfo.getName(), methodInfo.toString());
				} else {
					assertFalse(methodInfo.isDeprecated());
				}
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
}
