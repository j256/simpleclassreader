package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import com.j256.simpleclassreader.attribute.AttributeType;

/**
 * Information about a an attribute of a class, field, or method.
 * 
 * @author graywatson
 */
public class AttributeInfo {

	public static final AttributeInfo[] EMPTY_ARRAY = new AttributeInfo[0];

	private final String name;
	private final AttributeType type;
	private final Object value;

	public AttributeInfo(String name, AttributeType type, Object value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	/**
	 * Read in an attribute.
	 */
	public static AttributeInfo read(DataInputStream dis, ConstantPool constantPool, List<ClassReaderError> parseErrors)
			throws IOException {

		// u2 attribute_name_index;
		// u4 attribute_length;
		// u1 info[attribute_length]; // read by the per-attribute class

		int index = dis.readUnsignedShort();
		String name = constantPool.findName(index);
		if (name == null) {
			parseErrors.add(new ClassReaderError(ClassReaderErrorType.ATTRIBUTE_NAME_INDEX_INVALID, index));
			// try and continue
		}
		int length = dis.readInt();
		AttributeType type = AttributeType.fromString(name);
		if (type == null) {
			parseErrors.add(new ClassReaderError(ClassReaderErrorType.ATTRIBUTE_NAME_UNKNOWN, name));
			// skip over the rest of the attribute
			dis.skip(length);
			return null;
		}
		Object value = type.read(dis, length, constantPool, parseErrors);
		return new AttributeInfo(name, type, value);
	}

	public String getName() {
		return name;
	}

	public AttributeType getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "attribute " + name;
	}
}
