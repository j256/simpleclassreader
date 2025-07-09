package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.attribute.AnnotationInfo;
import com.j256.simpleclassreader.attribute.AttributeType;
import com.j256.simpleclassreader.attribute.RuntimeVisibleAnnotationsAttribute;

/**
 * Information about a field of a class including name, access details, data type, annotations, and attributes.
 * 
 * @author graywatson
 */
public class FieldInfo {

	private final String name;
	private final int accessFlags;
	private final DataDescriptor dataDescriptor;
	private final AttributeInfo[] attributeInfos;
	private final Object constantValue;
	private final AnnotationInfo[] runtimeAnnotations;

	public FieldInfo(String name, int accessFlags, DataDescriptor dataDescriptor, AttributeInfo[] attributeInfos,
			Object constantValue, AnnotationInfo[] runtimeAnnotations) {
		this.name = name;
		this.accessFlags = accessFlags;
		this.dataDescriptor = dataDescriptor;
		this.attributeInfos = attributeInfos;
		this.constantValue = constantValue;
		this.runtimeAnnotations = runtimeAnnotations;
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
			errors.add(new ClassReaderError(ClassReaderErrorType.FIELD_NAME_INDEX_INVALID, index));
			return null;
		}
		index = dis.readUnsignedShort();
		String typeStr = constantPool.findName(index);
		if (typeStr == null) {
			errors.add(new ClassReaderError(ClassReaderErrorType.FIELD_DATA_DESCRIPTOR_INDEX_INVALID, index));
			return null;
		}
		DataDescriptor dataDescriptor = null;
		if (typeStr != null) {
			dataDescriptor = DataDescriptor.fromString(typeStr);
			if (dataDescriptor == null) {
				errors.add(new ClassReaderError(ClassReaderErrorType.FIELD_DATA_DESCRIPTOR_INVALID, typeStr));
				return null;
			}
		}
		int attributeCount = dis.readUnsignedShort();
		Object constantValue = null;
		AnnotationInfo[] runtimeAnnotations = null;
		List<AttributeInfo> attributeInfos = null;
		for (int i = 0; i < attributeCount; i++) {
			AttributeInfo attributeInfo = AttributeInfo.read(dis, constantPool, errors);
			if (attributeInfo == null) {
				// try to read other known attributes
				continue;
			}
			if (attributeInfo.getType() == AttributeType.CONSTANT_VALUE) {
				constantValue = attributeInfo.getValue();
			}
			if (attributeInfo.getType() == AttributeType.RUNTIME_VISIBLE_ANNOTATIONS) {
				runtimeAnnotations = ((RuntimeVisibleAnnotationsAttribute) attributeInfo.getValue()).getAnnotations();
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
		return new FieldInfo(name, accessFlags, dataDescriptor, attributes, constantValue, runtimeAnnotations);
	}

	/**
	 * Return name of the field.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the raw access-flags value. The {@link #isPublic()} and other methods use this access-flags data.
	 */
	public int getAccessFlagsValue() {
		return accessFlags;
	}

	/**
	 * Get the access-flags as an array of enums.
	 */
	public AccessFlag[] getAccessFlags() {
		return AccessFlag.extractFlags(accessFlags, true, false);
	}

	/**
	 * Returns true if declared public; may be accessed from outside its package
	 */
	public boolean isPublic() {
		return AccessFlag.PUBLIC.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared private; accessible only within defining class and other classes belonging to same nest
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
	 * Returns true if declared static.
	 */
	public boolean isStatic() {
		return AccessFlag.STATIC.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared final; never directly assigned to after object construction (JLS §17.5).
	 */
	public boolean isFinal() {
		return AccessFlag.FINAL.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared volatile; cannot be cached.
	 */
	public boolean isVolatile() {
		return AccessFlag.VOLATILE.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared transient; not written or read by a persistent object manager.
	 */
	public boolean isTransient() {
		return AccessFlag.TRANSIENT.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared synthetic; not present in the source code.
	 */
	public boolean isSynthetic() {
		return AccessFlag.SYNTHETIC.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared as an element of an enum.
	 */
	public boolean isEnum() {
		return AccessFlag.ENUM.isEnabled(accessFlags);
	}

	/**
	 * Returns the data-type of the field or null if it couldn't be parsed.
	 */
	public DataDescriptor getDataDescriptor() {
		return dataDescriptor;
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

	public AnnotationInfo[] getRuntimeAnnotations() {
		return runtimeAnnotations;
	}

	@Override
	public String toString() {
		return "field " + name;
	}
}
