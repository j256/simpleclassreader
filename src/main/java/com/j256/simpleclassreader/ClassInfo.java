package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class information structure.
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
	private final List<ClassReaderError> errors;

	private ClassInfo(int minorVersion, int majorVersion, JdkVersion jdkVersion, int accessFlags, String className,
			String superClassName, String[] interfaces, FieldInfo[] fields, MethodInfo[] constructors,
			MethodInfo[] methods, AttributeInfo[] attributes, List<ClassReaderError> errors) {
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
		this.errors = errors;
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
	public List<ClassReaderError> getErrors() {
		return errors;
	}

	/**
	 * Read in a class bytes and return the class info or null on error.
	 */
	public static ClassInfo read(DataInputStream dis) throws IOException {

		List<ClassReaderError> errors = new ArrayList<>();
		int magic = dis.readInt();
		if (magic != CLASS_MAGIC) {
			errors.add(ClassReaderError.MAGIC_INVALID);
			return null;
		}
		int minorVersion = dis.readUnsignedShort();
		int majorVersion = dis.readUnsignedShort();
		JdkVersion jdkVersion = JdkVersion.fromMajor(majorVersion);

		ConstantPool constantPool = ConstantPool.read(dis);
		if (constantPool == null) {
			errors.add(ClassReaderError.UNKNOWN_CONSTANT_POOL_INFO);
			return null;
		}

		int accessFlags = dis.readUnsignedShort();
		// this class-name
		int index = dis.readUnsignedShort();
		String className = constantPool.findClassName(index);
		if (className == null) {
			errors.add(ClassReaderError.INVALID_CLASS_NAME_INDEX);
			return null;
		}
		className = className.replace('/', '.');
		// super class-name
		index = dis.readUnsignedShort();
		String superClassName = constantPool.findClassName(index);
		if (superClassName == null) {
			errors.add(ClassReaderError.INVALID_CLASS_NAME_INDEX);
			return null;
		}
		superClassName = superClassName.replace('/', '.');

		String[] interfaces = readInterfaces(dis, constantPool, errors);
		FieldInfo[] fields = readFields(dis, constantPool, errors);
		MethodInfo[] allMethods = readMethods(dis, constantPool, errors);
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
		AttributeInfo[] attributes = readAttributes(dis, constantPool, errors);

		return new ClassInfo(minorVersion, majorVersion, jdkVersion, accessFlags, className, superClassName, interfaces,
				fields, constructors, methods, attributes, errors);
	}

	private static String[] readInterfaces(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> errors) throws IOException {
		int num = dis.readUnsignedShort();
		String[] names = new String[num];
		for (int i = 0; i < names.length; i++) {
			int index = dis.readUnsignedShort();
			names[i] = constantPool.findClassName(index);
		}
		return names;
	}

	private static FieldInfo[] readFields(DataInputStream dis, ConstantPool constantPool, List<ClassReaderError> errors)
			throws IOException {
		int num = dis.readUnsignedShort();
		FieldInfo[] fields = new FieldInfo[num];
		for (int i = 0; i < fields.length; i++) {
			fields[i] = FieldInfo.read(dis, constantPool, errors);
		}
		return fields;
	}

	private static MethodInfo[] readMethods(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> errors) throws IOException {
		int num = dis.readUnsignedShort();
		MethodInfo[] methods = new MethodInfo[num];
		for (int i = 0; i < methods.length; i++) {
			methods[i] = MethodInfo.read(dis, constantPool, errors);
		}
		return methods;
	}

	private static AttributeInfo[] readAttributes(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> errors) throws IOException {
		int num = dis.readUnsignedShort();
		AttributeInfo[] attributes = new AttributeInfo[num];
		for (int i = 0; i < attributes.length; i++) {
			attributes[i] = AttributeInfo.read(dis, constantPool, errors);
		}
		return attributes;
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
}
