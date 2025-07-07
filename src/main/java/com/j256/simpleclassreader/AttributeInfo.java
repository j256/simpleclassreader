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

	private final String name;
	private final AttributeType attribute;
	private final Object value;

	public AttributeInfo(String name, AttributeType attribute, Object value) {
		this.name = name;
		this.attribute = attribute;
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
			parseErrors.add(ClassReaderError.INVALID_ATTRIBUTE_NAME_INDEX);
			// try and continue
		}
		int length = dis.readInt();
		AttributeType attribute = AttributeType.fromString(name);
		if (attribute == null) {
			parseErrors.add(ClassReaderError.UNKNOWN_ATTRIBUTE_NAME);
			// skip over the rest of the attribute
			dis.skip(length);
			return null;
		}
		Object value = attribute.read(dis, length, constantPool, parseErrors);
		return new AttributeInfo(name, attribute, value);
	}

	public String getName() {
		return name;
	}

	public AttributeType getAttribute() {
		return attribute;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "attribute " + name;
	}
}
