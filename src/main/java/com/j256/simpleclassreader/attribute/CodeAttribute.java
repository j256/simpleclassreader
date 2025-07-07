package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.AttributeInfo;
import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;
import com.j256.simpleclassreader.Utils;

/**
 * Code attribute that stores the compiled code.
 * 
 * @author graywatson
 */
public class CodeAttribute {

	private final int maxStack;
	private final int maxLocals;
	private final byte[] code;
	private final ExceptionTable[] exceptions;
	private final AttributeInfo[] attributes;

	public CodeAttribute(int maxStack, int maxLocals, byte[] code, ExceptionTable[] exceptions,
			AttributeInfo[] attributes) {
		this.maxStack = maxStack;
		this.maxLocals = maxLocals;
		this.code = code;
		this.exceptions = exceptions;
		this.attributes = attributes;
	}

	public static CodeAttribute read(DataInputStream dis, ConstantPool constantPool, List<ClassReaderError> parseErrors)
			throws IOException {

		// u2 attribute_name_index; (already read)
		// u4 attribute_length; (already read)
		// u2 max_stack;
		// u2 max_locals;
		// u4 code_length;
		// u1 code[code_length];
		// u2 exception_table_length;
		// {
		// u2 start_pc;
		// u2 end_pc;
		// u2 handler_pc;
		// u2 catch_type;
		// } exception_table[exception_table_length];
		// u2 attributes_count;
		// attribute_info attributes[attributes_count];

		int maxStack = dis.readUnsignedShort();
		int maxLocals = dis.readUnsignedShort();
		int codeLength = dis.readInt();
		byte[] code = Utils.readLength(dis, codeLength);
		int exceptionTableLength = dis.readUnsignedShort();
		ExceptionTable[] exceptions = new ExceptionTable[exceptionTableLength];
		for (int i = 0; i < exceptions.length; i++) {
			exceptions[i] = ExceptionTable.read(dis, constantPool, parseErrors);
		}
		int attributeCount = dis.readUnsignedShort();
		List<AttributeInfo> attributes = new ArrayList<>();
		for (int i = 0; i < attributeCount; i++) {
			AttributeInfo attribute = AttributeInfo.read(dis, constantPool, parseErrors);
			if (attribute == null) {
				// errors already added
			} else {
				attributes.add(attribute);
			}
		}

		return new CodeAttribute(maxStack, maxLocals, code, exceptions,
				attributes.toArray(new AttributeInfo[attributes.size()]));
	}

	public int getMaxStack() {
		return maxStack;
	}

	public int getMaxLocals() {
		return maxLocals;
	}

	public byte[] getCode() {
		return code;
	}

	public ExceptionTable[] getExceptions() {
		return exceptions;
	}

	public AttributeInfo[] getAttributes() {
		return attributes;
	}

	public static class ExceptionTable {
		private final int startPc;
		private final int endPc;
		private final int handlerPc;
		private final int catchType;

		private ExceptionTable(int startPc, int endPc, int handlerPc, int catchType) {
			this.startPc = startPc;
			this.endPc = endPc;
			this.handlerPc = handlerPc;
			this.catchType = catchType;
		}

		public static ExceptionTable read(DataInputStream dis, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {

			// u2 start_pc;
			// u2 end_pc;
			// u2 handler_pc;
			// u2 catch_type;

			int startPc = dis.readUnsignedShort();
			int endPc = dis.readUnsignedShort();
			int handlerPc = dis.readUnsignedShort();
			int catchType = dis.readUnsignedShort();

			return new ExceptionTable(startPc, endPc, handlerPc, catchType);
		}

		public int getStartPc() {
			return startPc;
		}

		public int getEndPc() {
			return endPc;
		}

		public int getHandlerPc() {
			return handlerPc;
		}

		public int getCatchType() {
			return catchType;
		}
	}
}
