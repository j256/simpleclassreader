package com.j256.simpleclassreader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.simpleclassreader.DataDescriptor.ComponentType;
import com.j256.simpleclassreader.DataDescriptor.MutableIndex;

public class DataDescriptorTest {

	@Test
	public void testStuff() {
		DataDescriptor desc = DataDescriptor.fromString("B");
		assertEquals(Byte.TYPE, desc.getDataClass());
		assertEquals(Byte.TYPE.getName(), desc.getDataClassName());
		assertEquals(0, desc.getArrayDepth());
		assertEquals(ComponentType.BYTE, desc.getComponentType());
		assertNull(desc.getReferenceClassName());
		assertFalse(desc.isArray());
		assertTrue(desc.isPrimitive());
		assertFalse(desc.isReference());

		desc = DataDescriptor.fromString(" I", new MutableIndex(1));
		assertEquals(Integer.TYPE, desc.getDataClass());
		assertEquals(Integer.TYPE.getName(), desc.getDataClassName());
		assertEquals(0, desc.getArrayDepth());
		assertEquals(ComponentType.INT, desc.getComponentType());
		assertNull(desc.getReferenceClassName());
		assertFalse(desc.isArray());
		assertTrue(desc.isPrimitive());
		assertFalse(desc.isReference());

		desc = DataDescriptor.fromString(" [[S", new MutableIndex(1));
		assertEquals(Short.TYPE, desc.getDataClass());
		assertEquals(Short.TYPE.getName(), desc.getDataClassName());
		assertEquals(2, desc.getArrayDepth());
		assertEquals(ComponentType.SHORT, desc.getComponentType());
		assertNull(desc.getReferenceClassName());
		assertTrue(desc.isArray());
		assertTrue(desc.isPrimitive());
		assertFalse(desc.isReference());
		assertEquals("array of short", desc.toString());

		String className = getClass().getName();
		desc = DataDescriptor.fromString("L" + className + ";");
		assertNull(desc.getDataClass());
		assertEquals(className, desc.getDataClassName());
		assertEquals(0, desc.getArrayDepth());
		assertEquals(ComponentType.REFERENCE, desc.getComponentType());
		assertEquals(className, desc.getReferenceClassName());
		assertFalse(desc.isArray());
		assertFalse(desc.isPrimitive());
		assertTrue(desc.isReference());
	}

	@Test
	public void testInvalid() {
		assertNull(DataDescriptor.fromString("["));
		assertNull(DataDescriptor.fromString("X"));
		assertNull(DataDescriptor.fromString("z"));
		// NO ;
		assertNull(DataDescriptor.fromString("Ljava.lang.String"));
	}
}
