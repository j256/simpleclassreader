package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Information about a an attribute for the class, field, or method.
 * 
 * @author graywatson
 */
public class AttributeInfo {

	private String name;
	private byte[] info;

	// u2 attribute_name_index;
	// u4 attribute_length;
	// u1 info[attribute_length];

	public AttributeInfo(String name, byte[] info) {
		this.name = name;
		this.info = info;
	}

	/**
	 * Read in an attribute.
	 */
	public static AttributeInfo read(DataInputStream dis, ConstantPool constantPool, List<ClassReaderError> errors)
			throws IOException {
		int index = dis.readUnsignedShort();
		String name = constantPool.findName(index);
		if (name == null) {
			errors.add(ClassReaderError.INVALID_ATTRIBUTE_NAME_INDEX);
		}
		int length = dis.readInt();
		dis.skip(length);
		return new AttributeInfo(name, null);
	}

	public String getName() {
		return name;
	}

	public byte[] getInfo() {
		return info;
	}

	@Override
	public String toString() {
		return "attribute " + name;
	}
}
