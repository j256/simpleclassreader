package com.j256.simpleclassreader;

import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.DataDescriptor.MutableIndex;

/**
 * Descriptor of a method which includes the parameter and return descriptors..
 * 
 * @author graywatson
 */
public class MethodDescriptor {

	private final String descriptorStr;
	private final DataDescriptor[] parameterDataDescriptors;
	private final DataDescriptor returnDescriptor;

	public MethodDescriptor(String descriptorStr, DataDescriptor[] parameterDataDescriptors,
			DataDescriptor returnDescriptor) {
		this.descriptorStr = descriptorStr;
		this.parameterDataDescriptors = parameterDataDescriptors;
		this.returnDescriptor = returnDescriptor;
	}

	/**
	 * Return the raw descriptor string.
	 */
	public String getDescriptorStr() {
		return descriptorStr;
	}

	/**
	 * Returns the parameter descriptors.
	 */
	public DataDescriptor[] getParameterDataDescriptors() {
		return parameterDataDescriptors;
	}

	/**
	 * Returns the return descriptor.
	 */
	public DataDescriptor getReturnDescriptor() {
		return returnDescriptor;
	}

	@Override
	public String toString() {
		return descriptorStr;
	}

	/**
	 * Convert from string data type representation returning null if invalid.
	 */
	public static MethodDescriptor fromString(String descriptorStr) {

		if (descriptorStr.length() < 3) {
			// must at least be ()V
			return null;
		}
		int index = 0;
		if (descriptorStr.charAt(index) != '(') {
			return null;
		}
		MutableIndex mutableIndex = new MutableIndex(1);

		List<DataDescriptor> parameterDataDescriptors = new ArrayList<>();
		while (index < descriptorStr.length() && descriptorStr.charAt(mutableIndex.getValue()) != ')') {
			parameterDataDescriptors.add(DataDescriptor.fromString(descriptorStr, mutableIndex));
		}
		// skip over the ')'
		mutableIndex.increment(1);

		DataDescriptor returnDescriptor = DataDescriptor.fromString(descriptorStr, mutableIndex);
		DataDescriptor[] params = parameterDataDescriptors.toArray(new DataDescriptor[parameterDataDescriptors.size()]);
		return new MethodDescriptor(descriptorStr, params, returnDescriptor);
	}
}
