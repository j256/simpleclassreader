package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ClassReaderErrorType;
import com.j256.simpleclassreader.ConstantPool;
import com.j256.simpleclassreader.DataDescriptor;

/**
 * The name and values of the annotation, listed in the specs as element_value_pairs.
 * 
 * @author graywatson
 */
public class AnnotationFieldValue {

	private final String fieldName;
	private final AnnotationValueType type;
	private final Object constValue;
	private final EnumAnnotationValue enumValue;
	private final String classValue;
	private final AnnotationInfo subAnnotationValue;
	private final AnnotationFieldValue[] arrayValues;

	private AnnotationFieldValue(String fieldName, AnnotationValueType type, Object constValue,
			EnumAnnotationValue enumValue, String classValue, AnnotationInfo subAnnotationValue,
			AnnotationFieldValue[] arrayValues) {
		this.fieldName = fieldName;
		this.type = type;
		this.constValue = constValue;
		this.enumValue = enumValue;
		this.classValue = classValue;
		this.subAnnotationValue = subAnnotationValue;
		this.arrayValues = arrayValues;
	}

	/**
	 * Read in an element-value-pair into a AnnotationValue class.
	 */
	public static AnnotationFieldValue read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors, boolean readName) throws IOException {

		// { u2 element_name_index;
		// element_value value;
		// } element_value_pairs[num_element_value_pairs];

		String fieldName = null;
		if (readName) {
			int index = dis.readUnsignedShort();
			fieldName = constantPool.findName(index);
			if (fieldName == null) {
				parseErrors.add(new ClassReaderError(ClassReaderErrorType.ANNOTATION_FIELD_NAME_INDEX_INVALID, index));
			}
		}

		// reading element_value
		// u1 tag
		int typeChar = dis.read();
		AnnotationValueType type = AnnotationValueType.fromChar(typeChar);
		if (type == null) {
			parseErrors.add(new ClassReaderError(ClassReaderErrorType.ANNOTATION_VALUE_TAG_INVALID, typeChar));
			return null;
		}

		Object constValue = null;
		EnumAnnotationValue enumValue = null;
		String classValue = null;
		AnnotationInfo subAnnotationValue = null;
		AnnotationFieldValue[] arrayValues = null;

		switch (type) {
			case BYTE:
				// u2 const_value_index;
				// read as an integer
				int valueIndex = dis.readUnsignedShort();
				constValue = constantPool.findValue(valueIndex);
				constValue = (constValue == null ? null : (byte) (int) constValue);
				break;
			/** short constant value type */
			case CHARACTER:
				// u2 const_value_index;
				// read as an integer
				valueIndex = dis.readUnsignedShort();
				constValue = constantPool.findValue(valueIndex);
				constValue = (constValue == null ? null : (char) (int) constValue);
				break;
			/** short constant value type */
			case SHORT:
				// u2 const_value_index;
				// read as an integer
				valueIndex = dis.readUnsignedShort();
				constValue = constantPool.findValue(valueIndex);
				constValue = (constValue == null ? null : (short) (int) constValue);
				break;
			/** int constant value type */
			case INTEGER:
				/** long constant value type */
			case LONG:
				/** float constant value type */
			case FLOAT:
				/** double constant value type */
			case DOUBLE:
				// u2 const_value_index;
				// these don't change their form from how they were read in
				valueIndex = dis.readUnsignedShort();
				constValue = constantPool.findValue(valueIndex);
				break;
			/** boolean constant value type */
			case BOOLEAN:
				// u2 const_value_index;
				// read as an integer
				valueIndex = dis.readUnsignedShort();
				constValue = constantPool.findValue(valueIndex);
				// need to convert into a boolean
				constValue = ((constValue == null || (int) constValue == 0) ? false : true);
				break;
			/** String constant value type */
			case STRING:
				// u2 const_value_index;
				int nameIndex = dis.readUnsignedShort();
				constValue = constantPool.findName(nameIndex);
				break;
			case ENUM:
				// u2 type_name_index;
				// u2 const_name_index;

				int typeIndex = dis.readUnsignedShort();
				String enumType = constantPool.findName(typeIndex);
				if (enumType == null) {
					parseErrors.add(
							new ClassReaderError(ClassReaderErrorType.ANNOTATION_ENUM_NAME_INDEX_INVALID, typeIndex));
					return null;
				}
				DataDescriptor descriptor = DataDescriptor.fromString(enumType);
				if (descriptor != null) {
					enumType = descriptor.getReferenceClassName();
				}
				nameIndex = dis.readUnsignedShort();
				String enumConstant = constantPool.findName(nameIndex);
				if (enumConstant == null) {
					parseErrors.add(
							new ClassReaderError(ClassReaderErrorType.ANNOTATION_ENUM_CONST_INDEX_INVALID, nameIndex));
					return null;
				}
				enumValue = new EnumAnnotationValue(enumType, enumConstant);
				break;
			case CLASS:
				// XXX: not sure what this is
				// u2 class_info_index;
				int classInfoIndex = dis.readUnsignedShort();
				classValue = constantPool.findName(classInfoIndex);
				descriptor = DataDescriptor.fromString(classValue);
				if (descriptor != null) {
					classValue = descriptor.getReferenceClassName();
				}
				break;
			case SUB_ANNOTATION:
				// annotation annotation_value;
				subAnnotationValue = AnnotationInfo.read(dis, constantPool, parseErrors);
				if (subAnnotationValue == null) {
					// error already added
					return null;
				}
				break;
			case ARRAY:
				// u2 num_values;
				// element_value values[num_values];
				int numValues = dis.readUnsignedShort();
				List<AnnotationFieldValue> arrayValueList = new ArrayList<>();
				for (int i = 0; i < numValues; i++) {
					AnnotationFieldValue element = AnnotationFieldValue.read(dis, constantPool, parseErrors, false);
					if (element == null) {
						// error already added
						return null;
					} else {
						arrayValueList.add(element);
					}
				}
				arrayValues =
						arrayValueList.toArray(arrayValueList.toArray(new AnnotationFieldValue[arrayValueList.size()]));
				break;
			default:
				parseErrors.add(new ClassReaderError(ClassReaderErrorType.ANNOTATION_VALUE_TAG_INVALID, type));
				return null;
		}

		return new AnnotationFieldValue(fieldName, type, constValue, enumValue, classValue, subAnnotationValue,
				arrayValues);
	}

	/**
	 * Return the name of the annotation field. Called "type-name" in the specification.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Return the type of the annotation field called "tag" in the specification.
	 */
	public AnnotationValueType getType() {
		return type;
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueType#BYTE}, {@link AnnotationValueType#CHARACTER},
	 * {@link AnnotationValueType#SHORT}, {@link AnnotationValueType#INTEGER}, {@link AnnotationValueType#LONG},
	 * {@link AnnotationValueType#FLOAT}, {@link AnnotationValueType#DOUBLE}, {@link AnnotationValueType#BOOLEAN}, and
	 * {@link AnnotationValueType#STRING}.
	 */
	public Object getConstValue() {
		return constValue;
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueType#BYTE}.
	 */
	public Byte getConstByteValue() {
		if (type == AnnotationValueType.BYTE) {
			return (Byte) constValue;
		} else {
			throw new IllegalArgumentException("annotation name/value has " + type + " tag, not byte");
		}
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueType#CHARACTER}.
	 */
	public Character getConstCharacterValue() {
		if (type == AnnotationValueType.CHARACTER) {
			return (Character) constValue;
		} else {
			throw new IllegalArgumentException("annotation name/value has " + type + " tag, not characterr");
		}
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueType#SHORT}.
	 */
	public Short getConstShortValue() {
		if (type == AnnotationValueType.SHORT) {
			return (Short) constValue;
		} else {
			throw new IllegalArgumentException("annotation name/value has " + type + " tag, not short");
		}
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueType#INTEGER}.
	 */
	public Integer getConstIntegerValue() {
		if (type == AnnotationValueType.INTEGER) {
			return (Integer) constValue;
		} else {
			throw new IllegalArgumentException("annotation name/value has " + type + " tag, not integer");
		}
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueType#LONG}.
	 */
	public Long getConstLongValue() {
		if (type == AnnotationValueType.LONG) {
			return (Long) constValue;
		} else {
			throw new IllegalArgumentException("annotation name/value has " + type + " tag, not long");
		}
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueType#FLOAT}.
	 */
	public Float getConstFloatValue() {
		if (type == AnnotationValueType.FLOAT) {
			return (Float) constValue;
		} else {
			throw new IllegalArgumentException("annotation name/value has " + type + " tag, not float");
		}
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueType#DOUBLE}.
	 */
	public Double getConstDoubleValue() {
		if (type == AnnotationValueType.DOUBLE) {
			return (Double) constValue;
		} else {
			throw new IllegalArgumentException("annotation name/value has " + type + " tag, not double");
		}
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueType#BOOLEAN}.
	 */
	public Boolean getConstBooleanValue() {
		if (type == AnnotationValueType.BOOLEAN) {
			return (Boolean) constValue;
		} else {
			throw new IllegalArgumentException("annotation name/value has " + type + " tag, not boolean");
		}
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueType#STRING}.
	 */
	public String getConstStringValue() {
		if (type == AnnotationValueType.STRING) {
			return (String) constValue;
		} else {
			throw new IllegalArgumentException("annotation name/value has " + type + " tag, not string");
		}
	}

	/**
	 * Return the enum value if the tag is {@link AnnotationValueType#ENUM}.
	 */
	public EnumAnnotationValue getEnumValue() {
		return enumValue;
	}

	/**
	 * Return the class value if the tag is {@link AnnotationValueType#CLASS}.
	 */
	public String getClassValue() {
		return classValue;
	}

	/**
	 * Return the sub-annotation value if the tag is {@link AnnotationValueType#SUB_ANNOTATION}.
	 */
	public AnnotationInfo getSubAnnotationValue() {
		return subAnnotationValue;
	}

	/**
	 * // * Return the array of annotations value if the tag is {@link AnnotationValueType#ARRAY}.
	 */
	public AnnotationFieldValue[] getArrayValues() {
		return arrayValues;
	}

	@Override
	public String toString() {
		return type + " value";
	}

	/**
	 * Value union of the annotation from the tag.
	 */
	public static enum AnnotationValueType {
		/** byte constant value type converted from int */
		BYTE('B'),
		/** char constant value type converted from int */
		CHARACTER('C'),
		/** short constant value type converted from int */
		SHORT('S'),
		/** int constant value type */
		INTEGER('I'),
		/** long constant value type */
		LONG('J'),
		/** float constant value type */
		FLOAT('F'),
		/** double constant value type */
		DOUBLE('D'),
		/** bool constant value type converted from int */
		BOOLEAN('Z'),
		/** String constant value type */
		STRING('s'),
		/** enum value as a {@link EnumAnnotationValue} */
		ENUM('e'),
		/** class information */
		CLASS('c'),
		/** recursive {@link AnnotationInfo} value */
		SUB_ANNOTATION('@'),
		/** array of recursive {@link AnnotationInfo} values */
		ARRAY('['),
		// end
		;

		private final static AnnotationValueType[] tags;

		private final char tagChar;

		static {
			int max = 0;
			for (AnnotationValueType tag : values()) {
				if (tag.tagChar > max) {
					max = tag.tagChar;
				}
			}
			tags = new AnnotationValueType[max + 1];
			for (AnnotationValueType tag : values()) {
				tags[tag.tagChar] = tag;
			}
		}

		private AnnotationValueType(char tagChar) {
			this.tagChar = tagChar;
		}

		/**
		 * Return the value tag associated with the ch argument or null if none.
		 */
		public static AnnotationValueType fromChar(int ch) {
			if (ch < 0 || ch >= tags.length) {
				return null;
			} else {
				return tags[ch];
			}
		}

		/**
		 * Return the character that it is represented with in the class file.
		 */
		public char getTagChar() {
			return tagChar;
		}
	}

	/**
	 * Enum annotation value.
	 */
	public static class EnumAnnotationValue {

		private final String enumType;
		private final String enumConstant;

		public EnumAnnotationValue(String enumType, String enumConstant) {
			this.enumType = enumType;
			this.enumConstant = enumConstant;
		}

		public String getType() {
			return enumType;
		}

		public String getConstant() {
			return enumConstant;
		}

		@Override
		public String toString() {
			return enumType + ":" + enumConstant;
		}
	}
}
