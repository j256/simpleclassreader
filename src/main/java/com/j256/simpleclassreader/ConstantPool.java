package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple class that reads the first couple of bytes from a class to get the name and the JDK version.
 * 
 * Based on: https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html
 */
public class ConstantPool {

	private final ConstantPoolType[] types;
	private final int[] indexes;
	private final String[] names;
	private final Object[] values;

	private ConstantPool(ConstantPoolType[] types, int[] indexes, String[] names, Object[] values) {
		this.types = types;
		this.indexes = indexes;
		this.names = names;
		this.values = values;
	}

	/**
	 * Read in the constant-pool information.
	 */
	public static ConstantPool read(DataInputStream dis) throws IOException {

		int numCpEntries = dis.readUnsignedShort();
		ConstantPoolType[] types = new ConstantPoolType[numCpEntries];
		int[] indexes = new int[numCpEntries];
		String[] names = new String[numCpEntries];
		Object[] values = new Object[numCpEntries];

		// NOTE: this starts at 1 because all of the indexes are 1 based (facepalm)
		for (int poolCount = 1; poolCount < numCpEntries; poolCount++) {
			int tag = dis.readUnsignedByte();
			ConstantPoolType constantPool = ConstantPoolType.fromCode(tag);
			if (constantPool == null) {
				return null;
			}
			types[poolCount] = constantPool;

			switch (constantPool) {
				case UTF8:
					names[poolCount] = readUtf8(dis);
					break;
				case INTEGER:
					values[poolCount] = readInteger(dis);
					break;
				case FLOAT:
					values[poolCount] = readFloat(dis);
					break;
				case LONG:
					values[poolCount] = readLong(dis);
					/*
					 * From the Java docs: All 8-byte constants take up two entries in the constant_pool table of the
					 * class file. If a CONSTANT_Long_info or CONSTANT_Double_info structure is the entry at index n in
					 * the constant_pool table, then the next usable entry in the table is located at index n+2. The
					 * constant_pool index n+1 must be valid but is considered unusable.
					 * 
					 * !! In retrospect, making 8-byte constants take two constant pool entries was a poor choice. !!
					 */
					poolCount++;
					break;
				case DOUBLE:
					values[poolCount] = readDouble(dis);
					// see CP_INFO_LONG comment above
					poolCount++;
					break;
				case CLASS:
				case STRING:
				case METHOD_TYPE:
				case MODULE:
				case PACKAGE:
					indexes[poolCount] = dis.readUnsignedShort();
					break;
				case FIELD_REF:
				case METHOD_REF:
				case INTERFACE_REF:
					values[poolCount] = readRef(dis);
					break;
				case NAME_AND_TYPE:
					values[poolCount] = readNameAndType(dis);
					break;
				case METHOD_HANDLE:
					values[poolCount] = readMethodHandle(dis);
					break;
				case INVOKE_DYNAMIC:
					values[poolCount] = readInvokeDynamic(dis);
					break;
				default:
					return null;
			}
		}

		return new ConstantPool(types, indexes, names, values);
	}

	/**
	 * Find a UTF8 name in the cp-info blocks
	 */
	public String findName(int index) {
		if (index >= names.length) {
			return null;
		}
		if (names[index] == null) {
			return null;
		}
		if (types[index] == ConstantPoolType.UTF8) {
			return names[index];
		} else {
			return null;
		}
	}

	public String findClassName(int index) {
		if (index >= indexes.length) {
			return null;
		}
		// the class name points to a class-type which points to a UTF8
		if (index >= indexes.length) {
			return null;
		}
		if (types[index] != ConstantPoolType.CLASS) {
			return null;
		}
		index = indexes[index];
		return findName(index);
	}

	public Object findValue(int index) {
		if (index >= values.length) {
			return null;
		}
		return values[index];
	}

	private static String readUtf8(DataInputStream dis) throws IOException {
		int nameLength = dis.readUnsignedShort();
		if (nameLength < 0 && nameLength > 256) {
			System.err.println("Invalid utf8 name-length: " + nameLength);
			return null;
		}
		byte[] nameBytes = IoUtils.readLength(dis, nameLength);
		return new String(nameBytes, StandardCharsets.UTF_8);
	}

	private static DoubleInt readRef(DataInputStream dis) throws IOException {
		int classIndex = dis.readUnsignedShort();
		int nameAndTypeIndex = dis.readUnsignedShort();
		return new DoubleInt(classIndex, nameAndTypeIndex);
	}

	private static int readInteger(DataInputStream dis) throws IOException {
		return dis.readInt();
	}

	private static float readFloat(DataInputStream dis) throws IOException {
		return dis.readFloat();
	}

	private static long readLong(DataInputStream dis) throws IOException {
		return dis.readLong();
	}

	private static double readDouble(DataInputStream dis) throws IOException {
		return dis.readDouble();
	}

	private static DoubleInt readNameAndType(DataInputStream dis) throws IOException {
		int nameIndex = dis.readUnsignedShort();
		int descriptorIndex = dis.readUnsignedShort();
		return new DoubleInt(nameIndex, descriptorIndex);
	}

	private static DoubleInt readMethodHandle(DataInputStream dis) throws IOException {
		int refKind = dis.read();
		int referenceIndex = dis.readUnsignedShort();
		return new DoubleInt(refKind, referenceIndex);
	}

	private static DoubleInt readInvokeDynamic(DataInputStream dis) throws IOException {
		int bootstrapMethodAttrIndex = dis.readUnsignedShort();
		int nameAndTypeIndex = dis.readUnsignedShort();
		return new DoubleInt(bootstrapMethodAttrIndex, nameAndTypeIndex);
	}

	/**
	 * Access information about the class from the access-flags.
	 */
	public static enum ClassAccessInfo {
		/** Declared final; no subclasses allowed. */
		FINAL(0x0010),
		/** Treat superclass methods specially when invoked by the invoke-special instruction. */
		SUPER(0x0020),
		/** Is an interface, not a class. */
		INTERFACE(0x0200),
		/** Declared abstract; must not be instantiated. */
		ABSTRACT(0x0400),
		/** Declared synthetic; not present in the source code. */
		SYNTHETIC(0x1000),
		/** Declared as an annotation type. */
		ANNOTATION(0x2000),
		/** Declared as an enum type. */
		ENUM(0x4000),
		/** Is a module, not a class or interface. */
		MODULE(0x8000),
		// end
		;

		private final int bit;

		private ClassAccessInfo(int bit) {
			this.bit = bit;
		}

		/**
		 * Get the access info array from the access flags.
		 */
		public static ClassAccessInfo[] fromAccessFlags(int accessFlags) {
			List<ClassAccessInfo> accessInfos = new ArrayList<>();
			for (ClassAccessInfo info : values()) {
				if ((info.bit & accessFlags) != 0) {
					accessInfos.add(info);
				}
			}
			return accessInfos.toArray(new ClassAccessInfo[accessInfos.size()]);
		}

		/**
		 * Return true if the access-flags have this access-info bit set.
		 */
		public boolean isEnabled(int accessFlags) {
			return ((accessFlags & bit) != 0);
		}
	}

	/**
	 * Type of entries in the cp-info table.
	 */
	public static enum ConstantPoolType {
		UTF8(1),
		INTEGER(3),
		FLOAT(4),
		LONG(5),
		DOUBLE(6),
		CLASS(7),
		STRING(8),
		FIELD_REF(9),
		METHOD_REF(10),
		INTERFACE_REF(11),
		NAME_AND_TYPE(12),
		METHOD_HANDLE(15),
		METHOD_TYPE(16),
		INVOKE_DYNAMIC(18),
		MODULE(19),
		PACKAGE(20),
		// end
		;

		private static final ConstantPoolType[] types;
		private final int code;

		static {
			int max = 0;
			for (ConstantPoolType type : values()) {
				if (type.code > max) {
					max = type.code;
				}
			}
			types = new ConstantPoolType[max + 1];
			for (ConstantPoolType type : values()) {
				types[type.code] = type;
			}
		}

		private ConstantPoolType(int code) {
			this.code = code;
		}

		/**
		 * Return the cp-info type associated with code or null if none.
		 */
		public static ConstantPoolType fromCode(int code) {
			if (code < 0 || code >= types.length) {
				return null;
			} else {
				return types[code];
			}
		}
	}

	/**
	 * Two integer entry in constant pool.
	 */
	public static class DoubleInt {

		private final int first;
		private final int second;

		public DoubleInt(int first, int second) {
			this.first = first;
			this.second = second;
		}

		public int getFirst() {
			return first;
		}

		public int getSecond() {
			return second;
		}

		@Override
		public String toString() {
			return first + ":" + second;
		}
	}
}
