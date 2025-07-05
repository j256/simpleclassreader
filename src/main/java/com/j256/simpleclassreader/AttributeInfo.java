package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import com.j256.simpleclassreader.attribute.Attribute;

/**
 * Information about a an attribute for the class, field, or method.
 * 
 * @author graywatson
 */
public class AttributeInfo {

	private final String name;
	private final Attribute attribute;
	private final Object value;

	public AttributeInfo(String name, Attribute attribute, Object value) {
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
		Attribute attribute = Attribute.fromString(name);
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

	public Attribute getAttribute() {
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
