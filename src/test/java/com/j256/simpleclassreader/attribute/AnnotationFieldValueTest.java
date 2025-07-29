package com.j256.simpleclassreader.attribute;

import static org.junit.Assert.*;

import org.junit.Test;

import com.j256.simpleclassreader.attribute.AnnotationFieldValue.AnnotationValueType;

public class AnnotationFieldValueTest {

	@Test
	public void testType() {
		assertNull(AnnotationValueType.fromChar(-1));
		assertNull(AnnotationValueType.fromChar(12345));
		AnnotationValueType tag = AnnotationValueType.STRING;
		assertEquals(tag, AnnotationValueType.fromChar(tag.getTagChar()));
	}
}
