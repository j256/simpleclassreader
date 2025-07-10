package com.j256.simpleclassreader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class MethodDescriptorTest {

	@Test
	public void testStuff() {
		String str = "(Ljava/lang/String;F)[F";
		MethodDescriptor desc = MethodDescriptor.fromString(str);
		assertSame(str, desc.getDescriptorStr());
		assertSame(str, desc.toString());
		DataDescriptor[] paramDescs = desc.getParameterDescriptors();
		assertEquals(2, paramDescs.length);
		assertEquals(0, paramDescs[0].getArrayDepth());
		assertNull(paramDescs[0].getDataClass());
		assertEquals(String.class.getName(), paramDescs[0].getDataClassName());
		assertEquals(0, paramDescs[1].getArrayDepth());
		assertEquals(Float.TYPE, paramDescs[1].getDataClass());
		assertEquals(Float.TYPE.getName(), paramDescs[1].getDataClassName());
		DataDescriptor retunDesc = desc.getReturnDescriptor();
		assertEquals(1, retunDesc.getArrayDepth());
		assertEquals(Float.TYPE, retunDesc.getDataClass());
		assertEquals(Float.TYPE.getName(), retunDesc.getDataClassName());
	}

	@Test
	public void testNoParams() {
		MethodDescriptor desc = MethodDescriptor.fromString("()V");
		assertEquals(0, desc.getParameterDescriptors().length);
		assertEquals(Void.TYPE, desc.getReturnDescriptor().getDataClass());
	}

	@Test
	public void testInvalid() {
		assertNull(MethodDescriptor.fromString("F"));
		assertNull(MethodDescriptor.fromString("("));
	}
}
