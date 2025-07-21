package com.j256.simpleclassreader;

public class TestUtils {

	public static String classToPath(Class<?> clazz) {
		return "target/test-classes/" + clazz.getName().replace('.', '/') + ".class";
	}
}
