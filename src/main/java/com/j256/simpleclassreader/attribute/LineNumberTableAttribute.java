package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;

/**
 * Table of line numbers associated with the [@link {@link CodeAttribute}}.
 * 
 * @author graywatson
 */
public class LineNumberTableAttribute {

	private final LineNumberLocation[] lineNumberLocations;

	private LineNumberTableAttribute(LineNumberLocation[] lineNumberInfos) {
		this.lineNumberLocations = lineNumberInfos;
	}

	public static LineNumberTableAttribute read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {

		// u2 attribute_name_index; (already read)
		// u4 attribute_length; (already read)
		// u2 line_number_table_length;
		// { } line_number_table[line_number_table_length];

		int number = dis.readUnsignedShort();
		LineNumberLocation[] lineNumberLocations = new LineNumberLocation[number];
		for (int i = 0; i < number; i++) {
			lineNumberLocations[i] = LineNumberLocation.read(dis, constantPool, parseErrors);
		}

		return new LineNumberTableAttribute(lineNumberLocations);
	}

	public LineNumberLocation[] getLineNumberLocations() {
		return lineNumberLocations;
	}

	@Override
	public String toString() {
		return Arrays.toString(lineNumberLocations);
	}

	/**
	 * Program counter to line-number.
	 */
	public static class LineNumberLocation {

		private final int startPc;
		private final int lineNumber;

		private LineNumberLocation(int startPc, int lineNumber) {
			this.startPc = startPc;
			this.lineNumber = lineNumber;
		}

		public static LineNumberLocation read(DataInputStream dis, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {

			// u2 start_pc;
			// u2 line_number;

			int startPc = dis.readUnsignedShort();
			int lineNumber = dis.readUnsignedShort();

			return new LineNumberLocation(startPc, lineNumber);
		}

		public int getStartPc() {
			return startPc;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		@Override
		public String toString() {
			return "LineNumberLocation [startPc=" + startPc + ", lineNumber=" + lineNumber + "]";
		}
	}
}
