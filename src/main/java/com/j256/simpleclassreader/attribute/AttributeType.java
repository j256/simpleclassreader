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
public enum AttributeType {
	CONSTANT_VALUE("ConstantValue") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) ConstantValueAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	CODE("Code") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) CodeAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	DEPRECATED("Deprecated") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) {
			// there is no additional information in this attribute so no need to have a type
			return null;
		}
	},
	EXCEPTIONS("Exceptions") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) ExceptionsAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	INNER_CLASSES("InnerClasses") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) InnerClassesAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	LINE_UMBER_TABLE("LineNumberTable") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) LineNumberTableAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	LOCAL_VARIABLE_TABLE("LocalVariableTable") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) LocalVariableTableAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	RUNTIME_VISIBLE_ANNOTATIONS("RuntimeVisibleAnnotations") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) RuntimeVisibleAnnotationsAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	SOURCE_FILE("SourceFile") {
		@Override
		public <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) SourceFileAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	// end
	;

	private static final Map<String, AttributeType> nameMap = new HashMap<>();

	private final String name;

	static {
		for (AttributeType attribute : values()) {
			nameMap.put(attribute.name, attribute);
		}
	}

	private AttributeType(String name) {
		this.name = name;
	}

	/**
	 * Return the attribute
	 */
	public static AttributeType fromString(String name) {
		return nameMap.get(name);
	}

	/**
	 * Read in an attribute and return a type that extends BaseAttribute.
	 */
	public abstract <T> T read(DataInputStream dis, int length, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException;
}
