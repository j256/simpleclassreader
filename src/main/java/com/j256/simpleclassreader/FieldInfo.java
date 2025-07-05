package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.attribute.Attribute;

/**
 * Information about a field that the class has.
 * 
 * @author graywatson
 */
public class FieldInfo {

	private final int accessFlags;
	private final String name;
	private final DataDescriptor dataType;
	private final AttributeInfo[] attributeInfos;
	private final Object constantValue;

	public FieldInfo(int accessFlags, String name, DataDescriptor dataType, AttributeInfo[] attributeInfos,
			Object constantValue) {
		this.accessFlags = accessFlags;
		this.name = name;
		this.dataType = dataType;
		this.attributeInfos = attributeInfos;
		this.constantValue = constantValue;
	}

	/**
	 * Read in an attribute.
	 */
	public static FieldInfo read(DataInputStream dis, ConstantPool constantPool, List<ClassReaderError> errors)
			throws IOException {

		// u2 access_flags;
		// u2 name_index;
		// u2 descriptor_index;
		// u2 attributes_count;
		// attribute_info attributes[attributes_count];

		int accessFlags = dis.readUnsignedShort();
		int index = dis.readUnsignedShort();
		String name = constantPool.findName(index);
		if (name == null) {
			errors.add(ClassReaderError.INVALID_FIELD_NAME_INDEX);
			return null;
		}
		index = dis.readUnsignedShort();
		String typeStr = constantPool.findName(index);
		if (typeStr == null) {
			errors.add(ClassReaderError.INVALID_FIELD_DESCRIPTOR_INDEX);
			return null;
		}
		DataDescriptor dataType = null;
		if (typeStr != null) {
			dataType = DataDescriptor.fromString(typeStr);
		}
		int attributeCount = dis.readUnsignedShort();
		Object constantValue = null;
		List<AttributeInfo> attributeInfos = new ArrayList<>();
		for (int i = 0; i < attributeCount; i++) {
			AttributeInfo attributeInfo = AttributeInfo.read(dis, constantPool, errors);
			if (attributeInfo == null) {
				// error already added
			} else {
				attributeInfos.add(attributeInfo);
				if (attributeInfo.getAttribute() == Attribute.CONSTANT_VALUE) {
					constantValue = attributeInfo.getValue();
				}
			}
		}

		return new FieldInfo(accessFlags, name, dataType,
				attributeInfos.toArray(new AttributeInfo[attributeInfos.size()]), constantValue);
	}

	/**
	 * Return name of the field.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns access-flags value.
	 */
	public int getAccessFlags() {
		return accessFlags;
	}

	/**
	 * Returns true if declared public; may be accessed from outside its package
	 */
	public boolean isPublic() {
		return FieldAccessInfo.PUBLIC.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared private; accessible only within defining class and other classes belonging to same nest
	 */
	public boolean isPrivate() {
		return FieldAccessInfo.PRIVATE.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared protected; may be accessed within subclasses.
	 */
	public boolean isProtected() {
		return FieldAccessInfo.PROTECTED.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared static.
	 */
	public boolean isStatic() {
		return FieldAccessInfo.STATIC.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared final; never directly assigned to after object construction (JLS §17.5).
	 */
	public boolean isFinal() {
		return FieldAccessInfo.FINAL.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared volatile; cannot be cached.
	 */
	public boolean isVolatile() {
		return FieldAccessInfo.VOLATILE.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared transient; not written or read by a persistent object manager.
	 */
	public boolean isTransient() {
		return FieldAccessInfo.TRANSIENT.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared synthetic; not present in the source code.
	 */
	public boolean isSynthetic() {
		return FieldAccessInfo.SYNTHETIC.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared as an element of an enum.
	 */
	public boolean isEnum() {
		return FieldAccessInfo.ENUM.isEnabled(accessFlags);
	}

	/**
	 * Returns the data-type of the field or null if it couldn't be parsed.
	 */
	public DataDescriptor getDataType() {
		return dataType;
	}

	/**
	 * Returns the attribute-info entries associated with the field.
	 */
	public AttributeInfo[] getAttributeInfos() {
		return attributeInfos;
	}

	public Object getConstantValue() {
		return constantValue;
	}

	@Override
	public String toString() {
		return "field " + name;
	}

	/**
	 * Access information associated with a field from the access-flags.
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
