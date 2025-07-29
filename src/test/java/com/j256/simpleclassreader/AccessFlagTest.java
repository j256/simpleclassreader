package com.j256.simpleclassreader;

import static org.junit.Assert.*;

import org.junit.Test;

public class AccessFlagTest {

	@Test
	public void testStuff() {
		assertEquals("[PUBLIC,PRIVATE,PROTECTED,STATIC]", AccessFlag.flagsToString(0xF, false, false, false));
		// all on
		assertEquals(
				"[PUBLIC,PRIVATE,PROTECTED,STATIC,FINAL,SYNCHRONIZED,SUPER,BRIDGE,VOLATILE,TRANSIENT,VARARGS,NATIVE,INTERFACE,ABSTRACT,STRICT,SYNTHETIC,ANNOTATION,ENUM,MODULE]",
				AccessFlag.flagsToString(0xFFFF, false, false, false));
		// restricted
		assertEquals(
				"[PUBLIC,PRIVATE,PROTECTED,STATIC,FINAL,SUPER,NATIVE,INTERFACE,ABSTRACT,STRICT,SYNTHETIC,ANNOTATION,ENUM,MODULE]",
				AccessFlag.flagsToString(0xFFFF, true, false, false));
		assertEquals(
				"[PUBLIC,PRIVATE,PROTECTED,STATIC,FINAL,VOLATILE,TRANSIENT,NATIVE,INTERFACE,ABSTRACT,STRICT,SYNTHETIC,ANNOTATION,ENUM,MODULE]",
				AccessFlag.flagsToString(0xFFFF, false, true, false));
		assertEquals(
				"[PUBLIC,PRIVATE,PROTECTED,STATIC,FINAL,SYNCHRONIZED,BRIDGE,VARARGS,NATIVE,INTERFACE,ABSTRACT,STRICT,SYNTHETIC,ANNOTATION,ENUM,MODULE]",
				AccessFlag.flagsToString(0xFFFF, false, false, true));
	}
}
