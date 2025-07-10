package com.j256.simpleclassreader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.junit.Test;

import com.j256.simpleclassreader.attribute.AttributeType;
import com.j256.simpleclassreader.attribute.RuntimeVisibleAnnotationsAttribute;

public class FieldInfoTest {
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
				if ("zip".equals(fieldInfo.getName())) {
					assertArrayEquals(new AccessFlag[] { AccessFlag.PROTECTED }, fieldInfo.getAccessFlags());
					assertEquals(AccessFlag.PROTECTED.getBit(), fieldInfo.getAccessFlagsValue());
					assertFalse(fieldInfo.isPublic());
					assertFalse(fieldInfo.isPrivate());
					assertTrue(fieldInfo.isProtected());
					assertFalse(fieldInfo.isStatic());
					assertFalse(fieldInfo.isFinal());
					assertFalse(fieldInfo.isVolatile());
					assertFalse(fieldInfo.isTransient());
					assertFalse(fieldInfo.isSynthetic());
					assertFalse(fieldInfo.isEnum());
					AttributeInfo[] attributeInfos = fieldInfo.getAttributeInfos();
					assertNotNull(attributeInfos);
					assertEquals(2, attributeInfos.length);
					assertEquals(AttributeType.DEPRECATED, attributeInfos[0].getType());
					assertEquals(AttributeType.RUNTIME_VISIBLE_ANNOTATIONS, attributeInfos[1].getType());
					assertEquals("",
							((RuntimeVisibleAnnotationsAttribute) attributeInfos[1].getValue()).getAnnotations());
					assertNull(fieldInfo.getConstantValue());
					assertEquals(1, fieldInfo.getRuntimeAnnotations().length);
					assertTrue(fieldInfo.isDeprecated());
					assertEquals("field " + fieldInfo.getName(), fieldInfo.toString());
				}
			}
			System.err.println("parse errors: " + info.getParseErrors());
		}
	}

	private String classToPath(Class<?> clazz) {
		return "target/test-classes/" + clazz.getName().replace('.', '/') + ".class";
	}

	@SuppressWarnings("unused")
	private static class TestClass {

		public static int foo;
		private float bar;
		@Deprecated
		protected String zip;

		public TestClass(float bar) {
			this.bar = bar;
		}
	}
}
