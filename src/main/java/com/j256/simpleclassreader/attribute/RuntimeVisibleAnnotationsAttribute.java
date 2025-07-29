package com.j256.simpleclassreader.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.ClassReaderError;
import com.j256.simpleclassreader.ConstantPool;

/**
 * Runtime visible annotations associated with class, field, and methods.
 * 
 * @author graywatson
 */
public class RuntimeVisibleAnnotationsAttribute {

	private final AnnotationInfo[] annotations;

	private RuntimeVisibleAnnotationsAttribute(AnnotationInfo[] annotations) {
		this.annotations = annotations;
	}

	public static RuntimeVisibleAnnotationsAttribute read(DataInputStream dis, ConstantPool constantPool,
			List<ClassReaderError> parseErrors) throws IOException {

		// u2 attribute_name_index; (already read)
		// u4 attribute_length; (already read)
		// u2 num_annotations;
		// annotation annotations[num_annotations];
		//
		// element_value {
		// u1 tag;
		// union {
		// u2 const_value_index;
		//
		// { u2 type_name_index;
		// u2 const_name_index;
		// } enum_const_value;
		//
		// u2 class_info_index;
		//
		// annotation annotation_value;
		//
		// { u2 num_values;
		// element_value values[num_values];
		// } array_value;
		// } value;
		// }

		int annotationCount = dis.readUnsignedShort();
		List<AnnotationInfo> annotationInfos = new ArrayList<>();
		for (int i = 0; i < annotationCount; i++) {
			AnnotationInfo annotationInfo = AnnotationInfo.read(dis, constantPool, parseErrors);
			if (annotationInfo == null) {
				// error added already
				return null;
			}
			annotationInfos.add(annotationInfo);
		}

		return new RuntimeVisibleAnnotationsAttribute(
				annotationInfos.toArray(new AnnotationInfo[annotationInfos.size()]));
	}

	public AnnotationInfo[] getAnnotations() {
		return annotations;
	}
}
