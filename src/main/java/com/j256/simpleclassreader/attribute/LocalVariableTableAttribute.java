package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;
import com.j256.simpleclassreader.DataDescriptor;

/**
 * Table of line numbers for debugging purposes.
 * 
 * @author graywatson
 */
public class LocalVariableTableAttribute {

	private final LocalVariable[] localVariables;

	private LocalVariableTableAttribute(LocalVariable[] localVariables) {
		this.localVariables = localVariables;
	}

	public static LocalVariableTableAttribute read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {

		// u2 attribute_name_index;
		// u4 attribute_length;
		// u2 local_variable_table_length;
		// { } local_variable_table[local_variable_table_length];

		int number = dis.readUnsignedShort();
		LocalVariable[] localVariables = new LocalVariable[number];
		for (int i = 0; i < number; i++) {
			localVariables[i] = LocalVariable.read(dis, constantPool, parseErrors);
		}

		return new LocalVariableTableAttribute(localVariables);
	}

	public LocalVariable[] getLocalVariables() {
		return localVariables;
	}

	@Override
	public String toString() {
		return Arrays.toString(localVariables);
	}

	/**
	 * Local variable information.
	 */
	public static class LocalVariable {

		private final int startPc;
		private final int length;
		private final String name;
		private final DataDescriptor descriptor;
		private final int index;

		public LocalVariable(int startPc, int length, String name, DataDescriptor descriptor, int index) {
			this.startPc = startPc;
			this.length = length;
			this.name = name;
			this.descriptor = descriptor;
			this.index = index;
		}

		public static LocalVariable read(DataInputStream dis, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {

			// u2 start_pc;
			// u2 length;
			// u2 name_index;
			// u2 descriptor_index;
			// u2 index;

			int startPc = dis.readUnsignedShort();
			int length = dis.readUnsignedShort();
			int index = dis.readUnsignedShort();
			String name = constantPool.findName(index);
			index = dis.readUnsignedShort();
			DataDescriptor descriptor = DataDescriptor.fromString(constantPool.findName(index));
			// index of the local variable with long/double taking 2 slots
			index = dis.readUnsignedShort();

			return new LocalVariable(startPc, length, name, descriptor, index);
		}

		public int getStartPc() {
			return startPc;
		}

		public int getLength() {
			return length;
		}

		public String getName() {
			return name;
		}

		public DataDescriptor getDescriptor() {
			return descriptor;
		}

		/**
		 * Index of the local variable with long/double types taking 2 elements.
		 */
		public int getIndex() {
			return index;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
