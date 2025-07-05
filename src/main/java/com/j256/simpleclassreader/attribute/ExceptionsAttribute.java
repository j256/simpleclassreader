package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;

/**
 * ConstantValue attribute
 * 
 * @author graywatson
 */
public class ExceptionsAttribute {

	private final String[] exceptions;

	public ExceptionsAttribute(String[] exceptions) {
		this.exceptions = exceptions;
	}

	public static ExceptionsAttribute read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {

		// u2 attribute_name_index; (already read)
		// u4 attribute_length; (already read)
		// u2 number_of_exceptions;
		// u2 exception_index_table[number_of_exceptions];

		int exceptionCount = dis.readUnsignedShort();
		List<String> exceptions = new ArrayList<>();
		for (int i = 0; i < exceptionCount; i++) {
			int index = dis.readUnsignedShort();
			String name = constantPool.findClassName(index);
			if (name == null) {
				parseErrors.add(ClassReaderError.EXCEPTION_NAME_INDEX);
			} else {
				exceptions.add(name);
			}
		}

		return new ExceptionsAttribute(exceptions.toArray(new String[exceptions.size()]));
	}

	public String[] getExceptions() {
		return exceptions;
	}
}
