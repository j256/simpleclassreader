package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;

/**
 * ConstantValue attribute.
 * 
 * @author graywatson
 */
public class ConstantValueAttribute {

	private final Object value;

	private ConstantValueAttribute(Object value) {
		this.value = value;
	}

	public static ConstantValueAttribute read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {

		// u2 attribute_name_index; (already read)
		// u4 attribute_length; (already read)
		// u2 constantvalue_index;

		int index = dis.readUnsignedShort();
		Object value = constantPool.findValue(index);

		return new ConstantValueAttribute(value);
	}

	public Object getValue() {
		return value;
	}
}
