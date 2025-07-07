package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The metadata and other information about a class.
 * 
 * Based on: https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html
 */
public class ClassInfo {

	private static final int CLASS_MAGIC = 0xCAFEBABE;
	private static final String UNKNOWN_VERSION = "unknown";

	private final int minorVersion;
	private final int majorVersion;
	private final JdkVersion jdkVersion;
	private final int accessFlags;
	private final String className;
	private final String superClassName;
	private final String[] interfaces;
	private final FieldInfo[] fields;
	private final MethodInfo[] constructors;
	private final MethodInfo[] methods;
	private final AttributeInfo[] attributes;
	private final List<ClassReaderError> parseErrors;

	private ClassInfo(int minorVersion, int majorVersion, JdkVersion jdkVersion, int accessFlags, String className,
			String superClassName, String[] interfaces, FieldInfo[] fields, MethodInfo[] constructors,
			MethodInfo[] methods, AttributeInfo[] attributes, List<ClassReaderError> parseErrors) {
		this.minorVersion = minorVersion;
		this.majorVersion = majorVersion;
		this.jdkVersion = jdkVersion;
		this.accessFlags = accessFlags;
		this.className = className;
		this.superClassName = superClassName;
		this.interfaces = interfaces;
		this.fields = fields;
		this.constructors = constructors;
		this.methods = methods;
		this.attributes = attributes;
		this.parseErrors = parseErrors;
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
	public String getJdkVersionString() {
		if (jdkVersion == null) {
			return UNKNOWN_VERSION;
		} else {
			return jdkVersion.makeJdkString(minorVersion);
		}
	}

	/**
	 * Get the acccess-flags for the class.
	 */
	public int getAccessFlags() {
		return accessFlags;
	}

	/**
	 * Declared final; no subclasses allowed.
	 */
	public boolean isFinal() {
		return ClassAccessInfo.FINAL.isEnabled(accessFlags);
	}

	/**
	 * Treat superclass methods specially when invoked by the invoke-special instruction.
	 */
	public boolean isSuper() {
		return ClassAccessInfo.SUPER.isEnabled(accessFlags);
	}

	/**
	 * Is an interface, not a class.
	 */
	public boolean isInterface() {
		return ClassAccessInfo.INTERFACE.isEnabled(accessFlags);
	}

	/**
	 * Declared abstract; must not be instantiated.
	 */
	public boolean isAbstract() {
		return ClassAccessInfo.ABSTRACT.isEnabled(accessFlags);
	}

	/**
	 * Declared synthetic; not present in the source code.
	 */
	public boolean isSynthetic() {
		return ClassAccessInfo.SYNTHETIC.isEnabled(accessFlags);
	}

	/**
	 * Declared as an annotation type.
	 */
	public boolean isAnnotation() {
		return ClassAccessInfo.ANNOTATION.isEnabled(accessFlags);
	}

	/**
	 * Declared as an enum type.
	 */
	public boolean isEnum() {
		return ClassAccessInfo.ENUM.isEnabled(accessFlags);
	}

	/**
	 * Is a module, not a class or interface.
	 */
	public boolean isModule() {
		return ClassAccessInfo.MODULE.isEnabled(accessFlags);
	}

	/**
	 * Name of the class.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Name of the super class which may be java.lang.Object.
	 */
	public String getSuperClassName() {
		return superClassName;
	}

	/**
	 * Return the interfaces that the class extends or a blank array if none.
	 */
	public String[] getInterfaces() {
		return interfaces;
	}

	/**
	 * Return fields from the class.
	 */
	public FieldInfo[] getFields() {
		return fields;
	}

	/**
	 * Return constructor methods from the class.
	 */
	public MethodInfo[] getConstructors() {
		return constructors;
	}

	/**
	 * Return methods from the class.
	 */
	public MethodInfo[] getMethods() {
		return methods;
	}

	/**
	 * Return attributes.
	 */
	public AttributeInfo[] getAttributes() {
		return attributes;
	}

	/**
	 * Return errors from the parse..
	 */
	public List<ClassReaderError> getParseErrors() {
		return parseErrors;
	}

	/**
	 * Read in a class bytes and return the class info or null on error.
	 */
	public static ClassInfo read(DataInputStream dis) throws IOException {

		List<ClassReaderError> parseErrors = new ArrayList<>();
		int magic = dis.readInt();
		if (magic != CLASS_MAGIC) {
			parseErrors.add(ClassReaderError.MAGIC_INVALID);
			return null;
		}
		int minorVersion = dis.readUnsignedShort();
		int majorVersion = dis.readUnsignedShort();
		JdkVersion jdkVersion = JdkVersion.fromMajor(majorVersion);
		if (jdkVersion == null) {
			parseErrors.add(ClassReaderError.UNKNOWN_MAJOR_VERSION);
			// try to continue
		}

		ConstantPool constantPool = ConstantPool.read(dis);
		if (constantPool == null) {
			parseErrors.add(ClassReaderError.INVALID_CONSTANT_POOL_INFO);
			return null;
		}

		int accessFlags = dis.readUnsignedShort();
		// this class-name
		String className = readClassName(dis, constantPool, parseErrors);
		// super class-name
		String superClassName = readClassName(dis, constantPool, parseErrors);

		String[] interfaces = readInterfaces(dis, constantPool, parseErrors);
		FieldInfo[] fields = readFields(dis, constantPool, parseErrors);
		MethodInfo[] allMethods = readMethods(dis, constantPool, parseErrors);
		List<MethodInfo> constructorList = new ArrayList<>();
		List<MethodInfo> methodList = new ArrayList<>();
		for (MethodInfo method : allMethods) {
			if (method.isConstructor()) {
				constructorList.add(method);
			} else {
				methodList.add(method);
			}
		}
		MethodInfo[] constructors = constructorList.toArray(new MethodInfo[constructorList.size()]);
		MethodInfo[] methods = methodList.toArray(new MethodInfo[methodList.size()]);
		AttributeInfo[] attributes = readAttributes(dis, constantPool, parseErrors);

		return new ClassInfo(minorVersion, majorVersion, jdkVersion, accessFlags, className, superClassName, interfaces,
				fields, constructors, methods, attributes, parseErrors);
	}

	private static String readClassName(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {
		int index = dis.readUnsignedShort();
		String name = constantPool.findClassName(index);
		if (name == null) {
			parseErrors.add(ClassReaderError.INVALID_CLASS_NAME_INDEX);
			return null;
		}
		name = Utils.classPathToPackage(name);
		return name;
	}

	private static String[] readInterfaces(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {
		int num = dis.readUnsignedShort();
		List<String> names = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			int index = dis.readUnsignedShort();
			String name = constantPool.findClassName(index);
			if (name == null) {
				parseErrors.add(ClassReaderError.INVALID_INTERFACE_NAME_INDEX);
				// try to continue
			} else {
				name = Utils.classPathToPackage(name);
				names.add(name);
			}
		}
		return names.toArray(new String[names.size()]);
	}

	private static FieldInfo[] readFields(DataInputStream dis, ConstantPool constantPool, List<ClassReaderError> errors)
			throws IOException {
		int num = dis.readUnsignedShort();
		List<FieldInfo> fields = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			FieldInfo field = FieldInfo.read(dis, constantPool, errors);
			if (field == null) {
				// try to continue
			} else {
				fields.add(field);
			}
		}
		return fields.toArray(new FieldInfo[fields.size()]);
	}

	private static MethodInfo[] readMethods(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> errors) throws IOException {
		int num = dis.readUnsignedShort();
		List<MethodInfo> methods = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			MethodInfo method = MethodInfo.read(dis, constantPool, errors);
			if (method == null) {
				// try to continue
			} else {
				methods.add(method);
			}
		}
		return methods.toArray(new MethodInfo[methods.size()]);
	}

	private static AttributeInfo[] readAttributes(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> errors) throws IOException {
		int num = dis.readUnsignedShort();
		List<AttributeInfo> attributes = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			AttributeInfo attribute = AttributeInfo.read(dis, constantPool, errors);
			if (attribute == null) {
				// try to continue
			} else {
				attributes.add(attribute);
			}
		}
		return attributes.toArray(new AttributeInfo[attributes.size()]);
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
		 * Return true if the access-flags have this access-info bit set.
		 */
		public boolean isEnabled(int accessFlags) {
			return ((accessFlags & bit) != 0);
		}
	}
}
