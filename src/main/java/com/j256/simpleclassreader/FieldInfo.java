package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Information about a field that the class has.
 * 
 * @author graywatson
 */
public class FieldInfo {

	private final int accessFlags;
	private final String name;
	private final DataDescriptor dataType;
	private final AttributeInfo[] attributes;

	public FieldInfo(int accessFlags, String name, DataDescriptor dataType, AttributeInfo[] attributes) {
		this.accessFlags = accessFlags;
		this.name = name;
		this.dataType = dataType;
		this.attributes = attributes;
	}

	public String getName() {
		return name;
	}

	public boolean isPublic() {
		return FieldAccessInfo.PUBLIC.isEnabled(accessFlags);
	}

	public boolean isPrivate() {
		return FieldAccessInfo.PRIVATE.isEnabled(accessFlags);
	}

	public boolean isProtected() {
		return FieldAccessInfo.PROTECTED.isEnabled(accessFlags);
	}

	public boolean isStatic() {
		return FieldAccessInfo.STATIC.isEnabled(accessFlags);
	}

	public boolean isFinal() {
		return FieldAccessInfo.FINAL.isEnabled(accessFlags);
	}

	public boolean isVolatile() {
		return FieldAccessInfo.VOLATILE.isEnabled(accessFlags);
	}

	public boolean isTransient() {
		return FieldAccessInfo.TRANSIENT.isEnabled(accessFlags);
	}

	public boolean isSynthetic() {
		return FieldAccessInfo.SYNTHETIC.isEnabled(accessFlags);
	}

	public boolean isEnum() {
		return FieldAccessInfo.ENUM.isEnabled(accessFlags);
	}

	/**
	 * Get the data-type of the field or null if it couldn't be parsed.
	 */
	public DataDescriptor getDataType() {
		return dataType;
	}

	public AttributeInfo[] getAttributes() {
		return attributes;
	}

	/**
	 * Read in an attribute.
	 */
	public static FieldInfo read(ClassReader reader, DataInputStream dis) throws IOException {
		// u2 access_flags;
		// u2 name_index;
		// u2 descriptor_index;
		// u2 attributes_count;
		// attribute_info attributes[attributes_count];

		int accessFlags = dis.readUnsignedShort();
		int index = dis.readUnsignedShort();
		String name = reader.findName(index, ClassReaderError.INVALID_FIELD_NAME_INDEX);
		index = dis.readUnsignedShort();
		String typeStr = reader.findName(index, ClassReaderError.INVALID_FIELD_DESCRIPTOR_INDEX);
		DataDescriptor dataType = null;
		if (typeStr != null) {
			dataType = DataDescriptor.fromString(typeStr);
		}
		int attributeCount = dis.readUnsignedShort();
		AttributeInfo[] attributes = new AttributeInfo[attributeCount];
		for (int i = 0; i < attributeCount; i++) {
			attributes[i] = AttributeInfo.read(reader, dis);
		}

		return new FieldInfo(accessFlags, name, dataType, attributes);
	}

	/**
	 * Access information associated with a field.
	 */
	private static enum FieldAccessInfo {
		/** Declared public; may be accessed from outside its package */
		PUBLIC(0x0001),
		/** Declared private; accessible only within defining class and other classes belonging to same nest */
		PRIVATE(0x0002),
		/** Declared protected; may be accessed within subclasses. */
		PROTECTED(0x0004),
		/** Declared static. */
		STATIC(0x0008),
		/** Declared final; never directly assigned to after object construction (JLS §17.5). */
		FINAL(0x0010),
		/** Declared volatile; cannot be cached. */
		VOLATILE(0x0040),
		/** Declared transient; not written or read by a persistent object manager. */
		TRANSIENT(0x0080),
		/** Declared synthetic; not present in the source code. */
		SYNTHETIC(0x1000),
		/** Declared as an element of an enum. */
		ENUM(0x4000),
		// end
		;

		private final int bit;

		private FieldAccessInfo(int bit) {
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
