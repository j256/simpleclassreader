package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;

/**
 * Source-file that generated the class.
 * 
 * @author graywatson
 */
public class SourceFileAttribute {

	private final String sourceFile;

	public SourceFileAttribute(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public static SourceFileAttribute read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {

		// u2 attribute_name_index; already read
		// u4 attribute_length; already read
		// u2 sourcefile_index;

		int index = dis.readUnsignedShort();
		String sourceFile = constantPool.findName(index);

		return new SourceFileAttribute(sourceFile);
	}

	public String getSourceFile() {
		return sourceFile;
	}
	
	@Override
	public String toString() {
		return sourceFile;
	}
}
