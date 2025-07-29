package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.Utils;

/**
 * Attribute that we don't have specific code for so we just read it as a bunch of bytes.
 * 
 * @author graywatson
 */
public class UnknownAttribute {

	private final String name;
	private final byte[] value;

	private UnknownAttribute(String name, byte[] value) {
		this.name = name;
		this.value = value;
	}

	public static UnknownAttribute read(DataInputStream dis, String name, int length,
			List<ClassReaderError> parseErrors) throws IOException {

		// u2 attribute_name_index; (already read)
		// u4 attribute_length; (already read)
		// u1 code[attribute_length];

		byte[] value = Utils.readBytes(dis, length);
		return new UnknownAttribute(name, value);
	}

	/**
	 * Name of the attribute.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Value of the attribute as a bag of bytes.
	 */
	public byte[] getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "attribute '" + name + "' with " + value.length + " bytes";
	}
}
