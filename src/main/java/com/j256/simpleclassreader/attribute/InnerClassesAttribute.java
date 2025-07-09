package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.j256.simpleclassreader.AccessFlag;
import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;
import com.j256.simpleclassreader.Utils;

/**
 * Source-file that generated the class.
 * 
 * @author graywatson
 */
public class InnerClassesAttribute {

	private final InnerClassInfo[] innerClasses;

	public InnerClassesAttribute(InnerClassInfo[] innerClasses) {
		this.innerClasses = innerClasses;
	}

	public static InnerClassesAttribute read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {

		// u2 attribute_name_index; already read
		// u4 attribute_length; already read
		// u2 number_of_classes;
		// { } classes[number_of_classes];

		int numberClasses = dis.readUnsignedShort();
		InnerClassInfo[] innerClasses = new InnerClassInfo[numberClasses];
		for (int i = 0; i < numberClasses; i++) {
			innerClasses[i] = InnerClassInfo.read(dis, constantPool, parseErrors);
		}

		return new InnerClassesAttribute(innerClasses);
	}

	public InnerClassInfo[] getInnerClasses() {
		return innerClasses;
	}

	@Override
	public String toString() {
		return Arrays.toString(innerClasses);
	}

	/**
	 * Information associated with an inner class.
	 */
	public static class InnerClassInfo {

		private final String name;
		private final String outerName;
		private final String simpleName;
		private final int accessFlags;

		private InnerClassInfo(String name, String outerName, String simpleName, int accessFlags) {
			this.name = name;
			this.outerName = outerName;
			this.simpleName = simpleName;
			this.accessFlags = accessFlags;
		}

		public static InnerClassInfo read(DataInputStream dis, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {

			// u2 inner_class_info_index;
			// u2 outer_class_info_index;
			// u2 inner_name_index;
			// u2 inner_class_access_flags;

			int index = dis.readUnsignedShort();
			String name = Utils.classPathToPackage(constantPool.findClassName(index));
			index = dis.readUnsignedShort();
			String outerName = null;
			if (index != 0) {
				outerName = Utils.classPathToPackage(constantPool.findClassName(index));
			}
			index = dis.readUnsignedShort();
			String simpleName = null;
			if (index != 0) {
				simpleName = constantPool.findName(index);
			}
			int accessFlags = dis.readUnsignedShort();

			return new InnerClassInfo(name, outerName, simpleName, accessFlags);
		}

		/**
		 * Get the full name of the inner class.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Get the full name of the outer class.
		 */
		public String getOuterName() {
			return outerName;
		}

		/**
		 * Get the simple name of the inner class.
		 */
		public String getSimpleName() {
			return simpleName;
		}

		/**
		 * Get the access flags value.
		 */
		public int getAccessFlagsValue() {
			return accessFlags;
		}

		/**
		 * Get the access flags as an array of enums.
		 */
		public AccessFlag[] getAccessFlags() {
			return AccessFlag.extractFlags(accessFlags, true, false, false);
		}

		/**
		 * Returns true if declared public; may be accessed from outside its package
		 */
		public boolean isPublic() {
			return AccessFlag.PUBLIC.isEnabled(accessFlags);
		}

		/**
		 * Returns true if declared private; accessible only within defining class and other classes belonging to same
		 * nest
		 */
		public boolean isPrivate() {
			return AccessFlag.PRIVATE.isEnabled(accessFlags);
		}

		/**
		 * Returns true if declared protected; may be accessed within subclasses.
		 */
		public boolean isProtected() {
			return AccessFlag.PROTECTED.isEnabled(accessFlags);
		}

		/**
		 * Returns true if declared static to the class.
		 */
		public boolean isStatic() {
			return AccessFlag.STATIC.isEnabled(accessFlags);
		}

		/**
		 * Returns true if declared final; must not be overridden
		 */
		public boolean isFinal() {
			return AccessFlag.FINAL.isEnabled(accessFlags);
		}

		/**
		 * Returns true if it is an interface class.
		 */
		public boolean isInterface() {
			return AccessFlag.INTERFACE.isEnabled(accessFlags);
		}

		/**
		 * Returns true if declared abstract; no implementation is provided.
		 */
		public boolean isAbstract() {
			return AccessFlag.ABSTRACT.isEnabled(accessFlags);
		}

		/**
		 * Returns true if declared synthetic; not present in the source code.
		 */
		public boolean isSynthetic() {
			return AccessFlag.SYNTHETIC.isEnabled(accessFlags);
		}

		/**
		 * Declared as an annotation type.
		 */
		public boolean isAnnotation() {
			return AccessFlag.ANNOTATION.isEnabled(accessFlags);
		}

		/**
		 * Declared as an enum type.
		 */
		public boolean isEnum() {
			return AccessFlag.ENUM.isEnabled(accessFlags);
		}

		@Override
		public String toString() {
			return "InnerClassInfo [name=" + name + ", outerName=" + outerName + ", simpleName=" + simpleName
					+ ", accessFlags=" + Arrays.toString(getAccessFlags()) + "]";
		}
	}
}
