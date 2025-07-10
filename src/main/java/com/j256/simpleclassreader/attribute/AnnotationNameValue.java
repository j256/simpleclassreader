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
public class AnnotationNameValue {

	private final String typeName;
	private final AnnotationValueTag tag;
	private final Object constValue;
	private final EnumAnnotationValue enumValue;
	private final String classValue;
	private final AnnotationInfo subAnnotationValue;
	private final AnnotationNameValue[] arrayValues;

	private AnnotationNameValue(String typeName, AnnotationValueTag tag, Object constValue,
			EnumAnnotationValue enumValue, String classValue, AnnotationInfo subAnnotationValue,
			AnnotationNameValue[] arrayValues) {
		this.typeName = typeName;
		this.tag = tag;
		this.constValue = constValue;
		this.enumValue = enumValue;
		this.classValue = classValue;
		this.subAnnotationValue = subAnnotationValue;
		this.arrayValues = arrayValues;
	}

	/**
	 * Read in an element-value-pair into a AnnotationValue class.
	 */
	public static AnnotationNameValue read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors, boolean readName) throws IOException {
		// { u2 element_name_index;
		// element_value value;
		// } element_value_pairs[num_element_value_pairs];

		String typeName = null;
		if (readName) {
			int nameIndex = dis.readUnsignedShort();
			typeName = constantPool.findName(nameIndex);
			if (typeName == null) {
				parseErrors.add(new ClassReaderError(ClassReaderErrorType.ANNOTATION_NAME_INDEX_INVALID, nameIndex));
			}
		}

		// u1 tag
		int tagChar = dis.read();
		AnnotationValueTag tag = AnnotationValueTag.fromChar(tagChar);
		if (tag == null) {
			parseErrors.add(new ClassReaderError(ClassReaderErrorType.ANNOTATION_VALUE_TAG_INVALID, tagChar));
			return null;
		}

		Object constValue = null;
		EnumAnnotationValue enumValue = null;
		String classValue = null;
		AnnotationInfo subAnnotationValue = null;
		AnnotationNameValue[] arrayValues = null;

		switch (tag) {
			case BYTE:
				// u2 const_value_index;
				// read as an integer
				int valueIndex = dis.readUnsignedShort();
				constValue = constantPool.findValue(valueIndex);
				constValue = (constValue == null ? null : (byte) (int) constValue);
				break;
			/** short constant value type */
			case CHAR:
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
				List<AnnotationNameValue> arrayValueList = new ArrayList<>();
				for (int i = 0; i < numValues; i++) {
					AnnotationNameValue element = AnnotationNameValue.read(dis, constantPool, parseErrors, false);
					if (element == null) {
						// error already added
						return null;
					} else {
						arrayValueList.add(element);
					}
				}
				arrayValues =
						arrayValueList.toArray(arrayValueList.toArray(new AnnotationNameValue[arrayValueList.size()]));
				break;
			default:
				parseErrors.add(new ClassReaderError(ClassReaderErrorType.ANNOTATION_VALUE_TAG_INVALID, tag));
				return null;
		}

		return new AnnotationNameValue(typeName, tag, constValue, enumValue, classValue, subAnnotationValue,
				arrayValues);
	}

	public String getTypeName() {
		return typeName;
	}

	public AnnotationValueTag getTag() {
		return tag;
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueTag#BYTE}, {@link AnnotationValueTag#CHAR},
	 * {@link AnnotationValueTag#SHORT}, {@link AnnotationValueTag#INTEGER}, {@link AnnotationValueTag#LONG},
	 * {@link AnnotationValueTag#FLOAT}, {@link AnnotationValueTag#DOUBLE}, {@link AnnotationValueTag#BOOLEAN}, and
	 * {@link AnnotationValueTag#STRING}.
	 */
	public Object getConstValue() {
		return constValue;
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueTag#BYTE}.
	 */
	public Byte getConstByteValue() {
		return (constValue == null ? null : (byte) (int) constValue);
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueTag#CHAR}.
	 */
	public char getConstCharValue() {
		return (constValue == null ? null : (char) (int) constValue);
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueTag#SHORT}.
	 */
	public short getConstShortValue() {
		return (constValue == null ? null : (short) (int) constValue);
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueTag#INTEGER}.
	 */
	public int getConstIntValue() {
		return (Integer) constValue;
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueTag#LONG}.
	 */
	public Long getConstLongValue() {
		return (Long) constValue;
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueTag#FLOAT}.
	 */
	public Float getConstFloatValue() {
		return (Float) constValue;
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueTag#DOUBLE}.
	 */
	public Double getConstDoubleValue() {
		return (Double) constValue;
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueTag#BOOLEAN}.
	 */
	public boolean getConstBooleanValue() {
		return (constValue == null || (int) constValue == 0 ? false : true);
	}

	/**
	 * Return the constant value if the tag is {@link AnnotationValueTag#STRING}.
	 */
	public String getConstStringValue() {
		return (String) constValue;
	}

	/**
	 * Return the enum value if the tag is {@link AnnotationValueTag#ENUM}.
	 */
	public EnumAnnotationValue getEnumValue() {
		return enumValue;
	}

	/**
	 * Return the class value if the tag is {@link AnnotationValueTag#CLASS}.
	 */
	public String getClassValue() {
		return classValue;
	}

	/**
	 * Return the sub-annotation value if the tag is {@link AnnotationValueTag#SUB_ANNOTATION}.
	 */
	public AnnotationInfo getSubAnnotationValue() {
		return subAnnotationValue;
	}

	/**
	 * // * Return the array of annotations value if the tag is {@link AnnotationValueTag#ARRAY}.
	 */
	public AnnotationNameValue[] getArrayValues() {
		return arrayValues;
	}

	@Override
	public String toString() {
		return tag + " value";
	}

	/**
	 * Value union of the annotation from the tag.
	 */
	public static enum AnnotationValueTag {
		/** byte constant value type converted from int */
		BYTE('B'),
		/** char constant value type converted from int */
		CHAR('C'),
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

		private final static AnnotationValueTag[] tags;

		private final char tagChar;

		static {
			int max = 0;
			for (AnnotationValueTag tag : values()) {
				if (tag.tagChar > max) {
					max = tag.tagChar;
				}
			}
			tags = new AnnotationValueTag[max + 1];
			for (AnnotationValueTag tag : values()) {
				tags[tag.tagChar] = tag;
			}
		}

		private AnnotationValueTag(char tagChar) {
			this.tagChar = tagChar;
		}

		/**
		 * Return the value tag associated with the ch argument or null if none.
		 */
		public static AnnotationValueTag fromChar(int ch) {
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
