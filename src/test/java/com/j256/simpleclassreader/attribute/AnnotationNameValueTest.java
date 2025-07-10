package com.j256.simpleclassreader.attribute;

import static org.junit.Assert.*;

import org.junit.Test;

import com.j256.simpleclassreader.attribute.AnnotationNameValue.AnnotationValueTag;

public class AnnotationNameValueTest {

	@Test
	public void testTag() {
		assertNull(AnnotationValueTag.fromChar(-1));
		assertNull(AnnotationValueTag.fromChar(12345));
	}
}
