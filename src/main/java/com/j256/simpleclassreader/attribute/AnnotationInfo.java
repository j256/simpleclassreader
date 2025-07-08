package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;
import com.j256.simpleclassreader.DataDescriptor;
import com.j256.simpleclassreader.Utils;

/**
 * Information about an annotation on a class, method, field, or method parameter.
 * 
 * @author graywatson
 */
public class AnnotationInfo {

	private DataDescriptor type;
	private AnnotationValue[] values;

	public AnnotationInfo(DataDescriptor type, AnnotationValue[] values) {
		this.type = type;
		this.values = values;
	}

	/**
	 * Read in an annotation info.
	 */
	public static AnnotationInfo read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {

		// u2 type_index;
		// u2 num_element_value_pairs;
		// { u2 element_name_index;
		// element_value value;
		// } element_value_pairs[num_element_value_pairs];

		int typeIndex = dis.readUnsignedShort();
		String typeStr = constantPool.findName(typeIndex);
		if (typeStr == null) {
			parseErrors.add(ClassReaderError.INVALID_ANNOTATION_TYPE_INDEX);
			return null;
		}
		DataDescriptor type = DataDescriptor.fromString(typeStr);
		if (type == null) {
			parseErrors.add(ClassReaderError.INVALID_ANNOTATION_TYPE_INDEX);
			return null;
		}
		int numValuePairs = dis.readUnsignedShort();

		List<AnnotationValue> values = new ArrayList<>();
		for (int i = 0; i < numValuePairs; i++) {
			AnnotationValue value = AnnotationValue.read(dis, constantPool, parseErrors);
			if (value == null) {
				// error already added
				return null;
			}
			values.add(value);
		}

		return new AnnotationInfo(type, values.toArray(new AnnotationValue[values.size()]));
	}

	public DataDescriptor getType() {
		return type;
	}

	public AnnotationValue[] getValues() {
		return values;
	}

	/**
	 * The values of the annotation, listed in the specs as element_value_pairs.
	 */
	public static class AnnotationValue {

		private final char tag;
		private final Object constValue;
		private final EnumAnnotationValue enumValue;
		private final String classInfo;
		private final AnnotationInfo annotation;
		private final AnnotationValue[] arrayValues;

		public AnnotationValue(char tag, Object constValue, EnumAnnotationValue enumValue, String classInfo,
				AnnotationInfo annotation, AnnotationValue[] arrayValues) {
			this.tag = tag;
			this.constValue = constValue;
			this.enumValue = enumValue;
			this.classInfo = classInfo;
			this.annotation = annotation;
			this.arrayValues = arrayValues;
		}

		/**
		 * Read in an element-value-pair into a AnnotationValue class.
		 */
		public static AnnotationValue read(DataInputStream dis, ConstantPool constantPool,
				List<ClassReaderError> parseErrors) throws IOException {
			// { u2 element_name_index;
			// element_value value;
			// } element_value_pairs[num_element_value_pairs];

			int nameIndex = dis.readUnsignedShort();
			String name = constantPool.findName(nameIndex);
			if (name == null) {
				parseErrors.add(ClassReaderError.EXCEPTION_NAME_INDEX);
			} else {
				// another class that has / instead of . in the path
				name = Utils.classPathToPackage(name);
			}

			// u1 tag
			int tag = dis.read();

			Object constValue = null;
			EnumAnnotationValue enumValue = null;
			String classInfo = null;
			AnnotationInfo annotation = null;
			AnnotationValue[] arrayValues = null;

			switch (tag) {
				// constant values: Byte, Character, Double, Float, Integer, J for Long, Short, and Z for boolean
				// u2 const_value_index;
				case 'B':
					int valueIndex = dis.readUnsignedShort();
					constValue = constantPool.findValue(valueIndex);
					constValue = (constValue == null ? null : (byte) (int) constValue);
					break;
				case 'C':
					valueIndex = dis.readUnsignedShort();
					constValue = constantPool.findValue(valueIndex);
					constValue = (constValue == null ? null : (char) (int) constValue);
					break;
				case 'S':
					valueIndex = dis.readUnsignedShort();
					constValue = constantPool.findValue(valueIndex);
					constValue = (constValue == null ? null : (short) (int) constValue);
					break;
				case 'D':
				case 'F':
				case 'I':
				case 'J':
					valueIndex = dis.readUnsignedShort();
					constValue = constantPool.findValue(valueIndex);
					break;
				case 'Z':
					valueIndex = dis.readUnsignedShort();
					constValue = constantPool.findValue(valueIndex);
					// need to convert into a boolean
					constValue = ((constValue == null || (int) constValue == 0) ? false : true);
					break;
				// string
				// u2 const_value_index;
				case 's':
					nameIndex = dis.readUnsignedShort();
					constValue = constantPool.findName(nameIndex);
					break;
				case 'e':
					// u2 type_name_index;
					// u2 const_name_index;

					int typeIndex = dis.readUnsignedShort();
					String enumName = constantPool.findName(typeIndex);
					if (enumName == null) {
						parseErrors.add(ClassReaderError.INVALID_ANNOTATION_ENUM_NAME_INDEX);
						return null;
					}
					nameIndex = dis.readUnsignedShort();
					String constName = constantPool.findName(nameIndex);
					if (constName == null) {
						parseErrors.add(ClassReaderError.INVALID_ANNOTATION_ENUM_CONST_INDEX);
						return null;
					}
					enumValue = new EnumAnnotationValue(enumName, constName);
					break;
				case 'c':
					// XXX: not sure what this is
					// u2 class_info_index;
					int classInfoIndex = dis.readUnsignedShort();
					classInfo = constantPool.findName(classInfoIndex);
					break;
				case '@':
					// recursive annotation
					// annotation annotation_value;
					annotation = AnnotationInfo.read(dis, constantPool, parseErrors);
					if (annotation == null) {
						// error already added
						return null;
					}
					break;
				case '[':
					// array of recursive values
					// u2 num_values;
					// element_value values[num_values];
					int numValues = dis.readUnsignedShort();
					List<AnnotationValue> arrayValueList = new ArrayList<>();
					for (int i = 0; i < numValues; i++) {
						AnnotationValue element = AnnotationValue.read(dis, constantPool, parseErrors);
						if (element == null) {
							// error already added
							return null;
						}
					}
					arrayValues =
							arrayValueList.toArray(arrayValueList.toArray(new AnnotationValue[arrayValueList.size()]));
					break;
				default:
					parseErrors.add(ClassReaderError.INVALID_ANNOTATION_VALUE_TAG);
					return null;
			}

			return new AnnotationValue((char) tag, constValue, enumValue, classInfo, annotation, arrayValues);
		}

		public char getTag() {
			return tag;
		}

		public Object getConstValue() {
			return constValue;
		}

		public EnumAnnotationValue getEnumValue() {
			return enumValue;
		}

		public String getClassInfo() {
			return classInfo;
		}

		public AnnotationInfo getAnnotation() {
			return annotation;
		}

		public AnnotationValue[] getArrayValues() {
			return arrayValues;
		}

		@Override
		public String toString() {
			return tag + " value";
		}
	}

	/**
	 * Enum annotation value.
	 */
	public static class EnumAnnotationValue {

		private final String enumName;
		private final String constName;

		public EnumAnnotationValue(String enumName, String constName) {
			this.enumName = enumName;
			this.constName = constName;
		}

		public String getEnumName() {
			return enumName;
		}

		public String getConstName() {
			return constName;
		}
	}
}
