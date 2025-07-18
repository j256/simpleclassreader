package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Internal class "constant pool" which stores the strings and other data items used by the various other parts of the
 * class metadata. The metadata is often an index into the constant-pool.
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
	 * Find a UTF8 name in the cp-info entries
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

	/**
	 * Find a class in the cp-info entries which is an index to another name.
	 */
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

	/**
	 * Find an object value (like int, short, etc.) in the cp-info entries.
	 */
	public Object findValue(int index) {
		if (index >= values.length) {
			return null;
		} else {
			return values[index];
		}
	}

	private static String readUtf8(DataInputStream dis) throws IOException {
		int nameLength = dis.readUnsignedShort();
		if (nameLength < 0 && nameLength > 256) {
			System.err.println("Invalid utf8 name-length: " + nameLength);
			return null;
		} else {
			byte[] nameBytes = Utils.readLength(dis, nameLength);
			return new String(nameBytes, StandardCharsets.UTF_8);
		}
	}

	private static TwoIntegerEntry readRef(DataInputStream dis) throws IOException {
		int classIndex = dis.readUnsignedShort();
		int nameAndTypeIndex = dis.readUnsignedShort();
		return new TwoIntegerEntry(classIndex, nameAndTypeIndex);
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

	private static TwoIntegerEntry readNameAndType(DataInputStream dis) throws IOException {
		int nameIndex = dis.readUnsignedShort();
		int descriptorIndex = dis.readUnsignedShort();
		return new TwoIntegerEntry(nameIndex, descriptorIndex);
	}

	private static TwoIntegerEntry readMethodHandle(DataInputStream dis) throws IOException {
		int refKind = dis.read();
		int referenceIndex = dis.readUnsignedShort();
		return new TwoIntegerEntry(refKind, referenceIndex);
	}

	private static TwoIntegerEntry readInvokeDynamic(DataInputStream dis) throws IOException {
		int bootstrapMethodAttrIndex = dis.readUnsignedShort();
		int nameAndTypeIndex = dis.readUnsignedShort();
		return new TwoIntegerEntry(bootstrapMethodAttrIndex, nameAndTypeIndex);
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
	public static class TwoIntegerEntry {

		private final int first;
		private final int second;

		public TwoIntegerEntry(int first, int second) {
			this.first = first;
			this.second = second;
		}

		public int getFirst() {
			return first;
		}

		public int getSecond() {
			return second;
		}
	}
}
