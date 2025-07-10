package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ClassReaderErrorType;
import com.j256.simpleclassreader.ConstantPool;
import com.j256.simpleclassreader.Utils;

/**
 * Exceptions that are thrown by a method attribute.
 * 
 * @author graywatson
 */
public class ExceptionsAttribute {

	private final String[] exceptions;

	private ExceptionsAttribute(String[] exceptions) {
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
				parseErrors.add(new ClassReaderError(ClassReaderErrorType.EXCEPTION_NAME_INDEX_INVALID, index));
			} else {
				// another class that has / instead of . in the path
				name = Utils.classPathToPackage(name);
				exceptions.add(name);
			}
		}

		return new ExceptionsAttribute(exceptions.toArray(new String[exceptions.size()]));
	}

	public String[] getExceptions() {
		return exceptions;
	}

	@Override
	public String toString() {
		return Arrays.toString(exceptions);
	}
}
