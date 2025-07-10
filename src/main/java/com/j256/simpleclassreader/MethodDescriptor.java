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

	static final DataDescriptor[] EMPTY_PARAMS = new DataDescriptor[0];

	private final String descriptorStr;
	private final DataDescriptor[] parameterDescriptors;
	private final DataDescriptor returnDescriptor;

	public MethodDescriptor(String descriptorStr, DataDescriptor[] parameterDescriptors,
			DataDescriptor returnDescriptor) {
		this.descriptorStr = descriptorStr;
		this.parameterDescriptors = parameterDescriptors;
		this.returnDescriptor = returnDescriptor;
	}

	/**
	 * Return the raw descriptor string.
	 */
	public String getDescriptorStr() {
		return descriptorStr;
	}

	/**
	 * Returns the parsed parameter descriptors.
	 */
	public DataDescriptor[] getParameterDescriptors() {
		return parameterDescriptors;
	}

	/**
	 * Returns the parsed return descriptor.
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

		// descriptor must start with '(' and be at least be ()V
		if (descriptorStr.charAt(0) != '(' || descriptorStr.length() < 3) {
			return null;
		}
		// we need to track the index because we are parsing multiple data descriptors from the descriptor string
		MutableIndex mutableIndex = new MutableIndex(1);

		List<DataDescriptor> parameterDescriptors = null;
		while (descriptorStr.charAt(mutableIndex.getValue()) != ')') {
			if (parameterDescriptors == null) {
				parameterDescriptors = new ArrayList<>();
			}
			parameterDescriptors.add(DataDescriptor.fromString(descriptorStr, mutableIndex));
		}
		// skip over the ')'
		mutableIndex.increment(1);

		DataDescriptor[] params = EMPTY_PARAMS;
		if (parameterDescriptors != null) {
			params = parameterDescriptors.toArray(new DataDescriptor[parameterDescriptors.size()]);
		}

		DataDescriptor returnDescriptor = DataDescriptor.fromString(descriptorStr, mutableIndex);
		return new MethodDescriptor(descriptorStr, params, returnDescriptor);
	}
}
