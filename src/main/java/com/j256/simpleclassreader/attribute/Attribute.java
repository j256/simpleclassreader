package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;

/**
 * Class which stores attribute info.
 * 
 * @author graywatson
 */
public enum Attribute {
	CONSTANT_VALUE("ConstantValue") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) ConstantValueAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	CIDE("Code") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) CodeAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	Exceptions("Exceptions") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) Exceptions.read(dis, length, constantPool, parseErrors);
			return result;
		}
	}
	// end
	;

	private static final Map<String, Attribute> nameMap = new HashMap<>();

	private final String name;

	static {
		for (Attribute attribute : values()) {
			nameMap.put(attribute.name, attribute);
		}
	}

	private Attribute(String name) {
		this.name = name;
	}

	/**
	 * Return the attribute
	 */
	public static Attribute fromString(String name) {
		return nameMap.get(name);
	}

	/**
	 * Read in an attribute and return a type that extends BaseAttribute.
	 */
	public abstract <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException;
}
