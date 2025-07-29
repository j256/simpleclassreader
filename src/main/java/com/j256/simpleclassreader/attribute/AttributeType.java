package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;

/**
 * Type of the annotation which maps from the name to its type for reading.
 * 
 * @author graywatson
 */
public enum AttributeType {
	/** constant-value attribute read in by {@link ConstantValueAttribute} */
	CONSTANT_VALUE("ConstantValue") {
		@Override
		public <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) ConstantValueAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	/** code attribute read in by {@link CodeAttribute} */
	CODE("Code") {
		@Override
		public <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) CodeAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	/** deprecated attribute which has no additional bytes to read */
	DEPRECATED("Deprecated") {
		@Override
		public <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) {
			// there is no additional information in this attribute so no need to have a type
			return null;
		}
	},
	/** exceptions attribute read in by {@link ExceptionsAttribute} */
	EXCEPTIONS("Exceptions") {
		@Override
		public <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) ExceptionsAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	/** inner-classes attribute read in by {@link InnerClassesAttribute} */
	INNER_CLASSES("InnerClasses") {
		@Override
		public <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) InnerClassesAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	/** line-number-table attribute read in by {@link LineNumberTableAttribute} */
	LINE_NUMBER_TABLE("LineNumberTable") {
		@Override
		public <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) LineNumberTableAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	/** local-variable-table attribute read in by {@link LocalVariableTableAttribute} */
	LOCAL_VARIABLE_TABLE("LocalVariableTable") {
		@Override
		public <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) LocalVariableTableAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	/** runtime-visible-annotations attribute read in by {@link RuntimeVisibleAnnotationsAttribute} */
	RUNTIME_VISIBLE_ANNOTATIONS("RuntimeVisibleAnnotations") {
		@Override
		public <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) RuntimeVisibleAnnotationsAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	/** source-file attribute read in by {@link SourceFileAttribute} */
	SOURCE_FILE("SourceFile") {
		@Override
		public <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) SourceFileAttribute.read(dis, constantPool, parseErrors);
			return result;
		}
	},
	/** unknown attribute read in as a bag of bytes by {@link UnknownAttribute} */
	UNKNOWN("Unknown") {
		@Override
		public <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			@SuppressWarnings("unchecked")
			T result = (T) UnknownAttribute.read(dis, name, length, parseErrors);
			return result;
		}
	},
	// end
	;

	private static final Map<String, AttributeType> attributeNameMap = new HashMap<>();

	private final String name;

	static {
		for (AttributeType attribute : values()) {
			attributeNameMap.put(attribute.name, attribute);
		}
	}

	private AttributeType(String name) {
		this.name = name;
	}

	/**
	 * Return the attribute-type from the name or {@link #UNKNOWN} if the type name is unknown.
	 */
	public static AttributeType fromString(String name) {
		return attributeNameMap.getOrDefault(name, UNKNOWN);
	}

	/**
	 * Read in an attribute and return a type that extends BaseAttribute.
	 */
	public abstract <T> T read(DataInputStream dis, String name, int length, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException;
}
