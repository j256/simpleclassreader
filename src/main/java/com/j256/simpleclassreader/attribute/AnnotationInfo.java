package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;
import com.j256.simpleclassreader.DataDescriptor;

/**
 * Information about an annotation on a class, method, field, or method parameter.
 * 
 * @author graywatson
 */
public class AnnotationInfo {

	private final String type;
	private final AnnotationNameValue[] values;

	public AnnotationInfo(String type, AnnotationNameValue[] values) {
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
			parseErrors.add(ClassReaderError.ANNOTATION_TYPE_INDEX_INVALID);
			return null;
		}
		DataDescriptor type = DataDescriptor.fromString(typeStr);
		if (type == null) {
			parseErrors.add(ClassReaderError.ANNOTATION_TYPE_INDEX_INVALID);
			return null;
		}
		if (type.getReferenceClassName() != null) {
			typeStr = type.getReferenceClassName();
		}
		int numValuePairs = dis.readUnsignedShort();

		List<AnnotationNameValue> values = new ArrayList<>();
		for (int i = 0; i < numValuePairs; i++) {
			AnnotationNameValue value = AnnotationNameValue.read(dis, constantPool, parseErrors, true);
			if (value == null) {
				// error already added
				return null;
			}
			values.add(value);
		}

		return new AnnotationInfo(typeStr, values.toArray(new AnnotationNameValue[values.size()]));
	}

	public String getType() {
		return type;
	}

	public AnnotationNameValue[] getValues() {
		return values;
	}

	@Override
	public String toString() {
		return type + ": " + Arrays.toString(values);
	}
}
