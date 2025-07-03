package com.j256.simpleclassreader;

import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.DataDescriptor.MutableIndex;

/**
 * Descriptor of a method signature.
 * 
 * @author graywatson
 */
public class MethodDescriptor {

	private final DataDescriptor[] parameterDataDescriptors;
	private final DataDescriptor returnDescriptor;

	public MethodDescriptor(DataDescriptor[] parameterDataDescriptors, DataDescriptor returnDescriptor) {
		this.parameterDataDescriptors = parameterDataDescriptors;
		this.returnDescriptor = returnDescriptor;
	}

	public DataDescriptor[] getParameterDataDescriptors() {
		return parameterDataDescriptors;
	}

	public DataDescriptor getReturnDescriptor() {
		return returnDescriptor;
	}

	/**
	 * Convert from string data type representation returning null if invalid.
	 */
	public static MethodDescriptor fromString(String str) {

		if (str.length() < 3) {
			// must at least be ()V
			return null;
		}
		int index = 0;
		if (str.charAt(index) != '(') {
			return null;
		}
		MutableIndex mutableIndex = new MutableIndex(1);

		List<DataDescriptor> parameterDataDescriptors = new ArrayList<>();
		while (index < str.length() && str.charAt(mutableIndex.getValue()) != ')') {
			parameterDataDescriptors.add(DataDescriptor.fromString(str, mutableIndex));
		}
		// skip over the ')'
		mutableIndex.increment(1);

		DataDescriptor returnDescriptor = DataDescriptor.fromString(str, mutableIndex);
		return new MethodDescriptor(
				parameterDataDescriptors.toArray(new DataDescriptor[parameterDataDescriptors.size()]),
				returnDescriptor);
	}
}
