package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.AttributeInfo;
import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ClassReaderErrorType;
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
	private final ExceptionHandler[] exceptions;
	private final AttributeInfo[] attributes;

	private CodeAttribute(int maxStack, int maxLocals, byte[] code, ExceptionHandler[] exceptions,
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
		byte[] code = Utils.readBytes(dis, codeLength);
		int exceptionTableLength = dis.readUnsignedShort();
		ExceptionHandler[] exceptions = new ExceptionHandler[exceptionTableLength];
		for (int i = 0; i < exceptions.length; i++) {
			exceptions[i] = ExceptionHandler.read(dis, constantPool, parseErrors);
		}
		int attributeCount = dis.readUnsignedShort();
		List<AttributeInfo> attributeInfos = new ArrayList<>();
		for (int i = 0; i < attributeCount; i++) {
			AttributeInfo attributeInfo = AttributeInfo.read(dis, constantPool, parseErrors);
			if (attributeInfo == null) {
				// try to read other known attributes
				continue;
			}
			if (attributeInfos == null) {
				attributeInfos = new ArrayList<>();
			}
			attributeInfos.add(attributeInfo);
		}

		AttributeInfo[] attributes = AttributeInfo.EMPTY_ARRAY;
		if (attributeInfos != null) {
			attributes = attributeInfos.toArray(new AttributeInfo[attributeInfos.size()]);
		}
		return new CodeAttribute(maxStack, maxLocals, code, exceptions, attributes);
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

	/**
	 * The exceptions that are handled by the code.
	 */
	public ExceptionHandler[] getExceptionHandlers() {
		return exceptions;
	}

	public AttributeInfo[] getAttributes() {
		return attributes;
	}

	/**
	 * Exception that is handled by a portion of the code.
	 */
	public static class ExceptionHandler {
		private final int startPc;
		private final int endPc;
		private final int handlerPc;
		private final String catchType;

		private ExceptionHandler(int startPc, int endPc, int handlerPc, String catchType) {
			this.startPc = startPc;
			this.endPc = endPc;
			this.handlerPc = handlerPc;
			this.catchType = catchType;
		}

		public static ExceptionHandler read(DataInputStream dis, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {

			// u2 start_pc;
			// u2 end_pc;
			// u2 handler_pc;
			// u2 catch_type;

			int startPc = dis.readUnsignedShort();
			int endPc = dis.readUnsignedShort();
			int handlerPc = dis.readUnsignedShort();
			int index = dis.readUnsignedShort();
			String catchType = constantPool.findClassName(index);
			if (catchType == null) {
				parseErrors.add(new ClassReaderError(ClassReaderErrorType.CODE_CATCH_TYPE_INDEX_INVALID, index));
			} else {
				catchType = Utils.classPathToPackage(catchType);
			}

			return new ExceptionHandler(startPc, endPc, handlerPc, catchType);
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

		/**
		 * The type of exception that is caught by the code portion.
		 */
		public String getCatchType() {
			return catchType;
		}
	}
}
