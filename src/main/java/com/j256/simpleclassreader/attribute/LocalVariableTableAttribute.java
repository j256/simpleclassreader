package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;

/**
 * Table of line numbers for debugging purposes.
 * 
 * @author graywatson
 */
public class LocalVariableTableAttribute {

	private final LineNumberInfo[] lineNumberInfos;

	public LocalVariableTableAttribute(LineNumberInfo[] lineNumberInfos) {
		this.lineNumberInfos = lineNumberInfos;
	}

	public static LocalVariableTableAttribute read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {

		// u2 attribute_name_index;
		// u4 attribute_length;
		// u2 line_number_table_length;
		// { u2 start_pc;
		// u2 line_number;
		// } line_number_table[line_number_table_length];

		int number = dis.readUnsignedShort();
		LineNumberInfo[] lineNumberInfos = new LineNumberInfo[number];
		for (int i = 0; i < number; i++) {
			lineNumberInfos[i] = LineNumberInfo.read(dis, constantPool, parseErrors);
		}

		return new LocalVariableTableAttribute(lineNumberInfos);
	}

	public LineNumberInfo[] getLineNumberInfos() {
		return lineNumberInfos;
	}

	@Override
	public String toString() {
		return Arrays.toString(lineNumberInfos);
	}

	/**
	 * Program counter to line-number.
	 */
	public static class LineNumberInfo {

		private final int startPc;
		private final int lineNumber;

		private LineNumberInfo(int startPc, int lineNumber) {
			this.startPc = startPc;
			this.lineNumber = lineNumber;
		}

		public static LineNumberInfo read(DataInputStream dis, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {

			// u2 start_pc;
			// u2 line_number;

			int startPc = dis.readUnsignedShort();
			int lineNumber = dis.readUnsignedShort();

			return new LineNumberInfo(startPc, lineNumber);
		}

		public int getStartPc() {
			return startPc;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		@Override
		public String toString() {
			return "LineNumberInfo [startPc=" + startPc + ", lineNumber=" + lineNumber + "]";
		}
	}
}
