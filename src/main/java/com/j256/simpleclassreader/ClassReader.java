package com.j256.simpleclassreader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple class that reads the first couple of bytes from a class to get the name and the JDK version.
 * 
 * Based on careful reading of: https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.4.2
 */
public class ClassReader {

	private static final int CLASS_MAGIC = 0xCAFEBABE;
	private static final String UNKNOWN_VERSION = "unknown";

	private int minorVersion;
	private int majorVersion;
	private JdkVersion jdkVersion;
	private ConstantPoolType[] cpInfoTypes;
	private int[] indexes;
	private String[] names;
	private int accessFlags;
	private String className;
	private String superClassName;
	private String[] interfaces;
	private FieldInfo[] fields;
	private MethodInfo[] methods;
	private AttributeInfo[] attributes;
	private boolean valid;
	private List<ClassReaderError> errors = new ArrayList<>();

	public ClassReader(byte[] classBytes) {
		this(new ByteArrayInputStream(classBytes));
	}

	public ClassReader(byte[] classBytes, int offset, int length) {
		this(new ByteArrayInputStream(classBytes, offset, length));
	}

	public ClassReader(InputStream inputStream) {
		try (DataInputStream dis = new DataInputStream(inputStream);) {
			readClass(dis);
		} catch (EOFException eofe) {
			errors.add(ClassReaderError.PREMATURE_EOF);
			valid = false;
		} catch (IOException ioe) {
			errors.add(ClassReaderError.INPUT_PROBLEMS);
			valid = false;
		}
	}

	public int getMajorVersion() {
		return majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * Return the string version of the JDK based on the major and minor numbers.
	 */
	public String getJdkVersion() {
		if (jdkVersion == null) {
			return UNKNOWN_VERSION;
		} else {
			return jdkVersion.makeJdk(minorVersion);
		}
	}

	/**
	 * Get the acccess-flags for the class.
	 */
	public int getAccessFlags() {
		return accessFlags;
	}

	public boolean isFinal() {
		return ClassAccessInfo.FINAL.isEnabled(accessFlags);
	}

	public boolean isSuper() {
		return ClassAccessInfo.SUPER.isEnabled(accessFlags);
	}

	public boolean isInterface() {
		return ClassAccessInfo.INTERFACE.isEnabled(accessFlags);
	}

	public boolean isAbstract() {
		return ClassAccessInfo.ABSTRACT.isEnabled(accessFlags);
	}

	public boolean isSynthetic() {
		return ClassAccessInfo.SYNTHETIC.isEnabled(accessFlags);
	}

	public boolean isAnnotation() {
		return ClassAccessInfo.ANNOTATION.isEnabled(accessFlags);
	}

	public boolean isEnum() {
		return ClassAccessInfo.ENUM.isEnabled(accessFlags);
	}

	public boolean isModule() {
		return ClassAccessInfo.MODULE.isEnabled(accessFlags);
	}

	public String getClassName() {
		return className;
	}

	/**
	 * Return the interfaces that the class extends or a blank array if none.
	 */
	public String[] getInterfaces() {
		return interfaces;
	}

	public FieldInfo[] getFields() {
		return fields;
	}

	public MethodInfo[] getMethods() {
		return methods;
	}

	public AttributeInfo[] getAttributes() {
		return attributes;
	}

	/**
	 * Returns true if the class was able to full parse.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Find a UTF8 name in the cp-info blocks
	 */
	String findName(int nameIndex, ClassReaderError error) {
		if (nameIndex >= indexes.length) {
			errors.add(error);
			return null;
		}
		nameIndex = indexes[nameIndex];
		if (nameIndex >= names.length) {
			errors.add(error);
			return null;
		}
		if (names[nameIndex] == null) {
			errors.add(error);
			return null;
		}
		return names[nameIndex];
	}

	private String findClassName(int nameIndex) {
		if (nameIndex >= indexes.length) {
			errors.add(ClassReaderError.INVALID_CLASS_NAME_INDEX);
			return null;
		}
		return findName(indexes[nameIndex], ClassReaderError.INVALID_CLASS_NAME_INDEX);
	}

	/**
	 * Read in a class bytes and return the class info or null on error.
	 */
	private void readClass(DataInputStream dis) throws IOException {

		int magic = dis.readInt();
		if (magic != CLASS_MAGIC) {
			errors.add(ClassReaderError.MAGIC_INVALID);
			return;
		}
		minorVersion = dis.readUnsignedShort();
		majorVersion = dis.readUnsignedShort();
		jdkVersion = JdkVersion.fromMajor(majorVersion);
		if (!readCpInfo(dis)) {
			return;
		}

		accessFlags = dis.readUnsignedShort();
		// this class-name
		int index = dis.readUnsignedShort();
		className = findClassName(index);
		if (className == null) {
			return;
		}
		// super class-name
		index = dis.readUnsignedShort();
		superClassName = findClassName(index);
		if (superClassName == null) {
			return;
		}

		interfaces = readInterfaces(dis);
		fields = readFields(dis);
		methods = readMethods(dis);
		attributes = readAttributes(dis);

		valid = true;
	}

	private String[] readInterfaces(DataInputStream dis) throws IOException {
		int num = dis.readUnsignedShort();
		String[] names = new String[num];
		for (int i = 0; i < names.length; i++) {
			int index = dis.readUnsignedShort();
			names[i] = findClassName(index);
		}
		return names;
	}

	private FieldInfo[] readFields(DataInputStream dis) throws IOException {
		int num = dis.readUnsignedShort();
		FieldInfo[] fields = new FieldInfo[num];
		for (int i = 0; i < names.length; i++) {
			fields[i] = FieldInfo.read(this, dis);
		}
		return fields;
	}

	private MethodInfo[] readMethods(DataInputStream dis) throws IOException {
		int num = dis.readUnsignedShort();
		MethodInfo[] methods = new MethodInfo[num];
		for (int i = 0; i < names.length; i++) {
			methods[i] = MethodInfo.read(this, dis);
		}
		return null;
	}

	private AttributeInfo[] readAttributes(DataInputStream dis) throws IOException {
		int num = dis.readUnsignedShort();
		AttributeInfo[] attributes = new AttributeInfo[num];
		for (int i = 0; i < names.length; i++) {
			attributes[i] = AttributeInfo.read(this, dis);
		}
		return attributes;
	}

	private boolean readCpInfo(DataInputStream dis) throws IOException {
		int numCpEntries = dis.readUnsignedShort();
		cpInfoTypes = new ConstantPoolType[numCpEntries];
		names = new String[numCpEntries];
		indexes = new int[numCpEntries];

		// NOTE: this starts at 1 because all of the indexes are 1 based (facepalm)
		for (int poolCount = 1; poolCount < numCpEntries; poolCount++) {
			int tag = dis.readUnsignedByte();
			ConstantPoolType constantPool = ConstantPoolType.fromCode(tag);
			if (constantPool == null) {
				errors.add(ClassReaderError.UNKNOWN_CP_INFO);
				return false;
			}
			cpInfoTypes[poolCount] = constantPool;

			switch (constantPool) {
				case UTF8:
					names[poolCount] = readUtf8(dis);
					break;
				case INTEGER:
					readInteger(dis);
					break;
				case FLOAT:
					readFloat(dis);
					break;
				case LONG:
					readLong(dis);
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
					readDouble(dis);
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
					readRef(dis);
					break;
				case NAME_AND_TYPE:
					readNameAndType(dis);
					break;
				case METHOD_HANDLE:
					readMethodHandle(dis);
					break;
				case INVOKE_DYNAMIC:
					readInvokeDynamic(dis);
					break;
				default:
					errors.add(ClassReaderError.UNKNOWN_CP_INFO);
					return false;
			}
		}
		return true;
	}

	private String readUtf8(DataInputStream dis) throws IOException {
		int nameLength = dis.readUnsignedShort();
		if (nameLength < 0 && nameLength > 256) {
			System.err.println("Invalid utf8 name-length: " + nameLength);
			return null;
		}
		byte[] nameBytes = new byte[nameLength];
		dis.readFully(nameBytes);
		return new String(nameBytes, StandardCharsets.UTF_8);
	}

	@SuppressWarnings("unused")
	private void readRef(DataInputStream dis) throws IOException {
		int classIndex = dis.readUnsignedShort();
		int nameAndTypeIndex = dis.readUnsignedShort();
	}

	private int readInteger(DataInputStream dis) throws IOException {
		return dis.readInt();
	}

	private float readFloat(DataInputStream dis) throws IOException {
		return dis.readFloat();
	}

	private long readLong(DataInputStream dis) throws IOException {
		return dis.readLong();
	}

	private double readDouble(DataInputStream dis) throws IOException {
		return dis.readDouble();
	}

	@SuppressWarnings("unused")
	private void readNameAndType(DataInputStream dis) throws IOException {
		int nameIndex = dis.readUnsignedShort();
		int descriptorIndex = dis.readUnsignedShort();
	}

	@SuppressWarnings("unused")
	private void readMethodHandle(DataInputStream dis) throws IOException {
		int refKind = dis.read();
		int referenceIndex = dis.readUnsignedShort();
	}

	@SuppressWarnings("unused")
	private void readInvokeDynamic(DataInputStream dis) throws IOException {
		int bootstrapMethodAttrIndex = dis.readUnsignedShort();
		int nameAndTypeIndex = dis.readUnsignedShort();
	}

	/**
	 * Access information about classes, fields, and methods.
	 * 
	 * @author graywatson
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
}
