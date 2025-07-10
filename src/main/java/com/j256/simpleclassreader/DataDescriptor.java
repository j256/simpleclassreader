package com.j256.simpleclassreader;

/**
 * Data type of field, method parameter, or method return value.
 * 
 * @author graywatson
 */
public class DataDescriptor {

	private final int arrayDepth;
	private final ComponentType componentType;
	private final String referenceClassName;

	public DataDescriptor(int arrayDepth, ComponentType componentType, String referenceClassName) {
		this.arrayDepth = arrayDepth;
		this.componentType = componentType;
		this.referenceClassName = referenceClassName;
	}

	/**
	 * Convert from string data type representation returning null if invalid.
	 */
	public static DataDescriptor fromString(String str) {
		return fromString(str, null);
	}

	/**
	 * Return a data descriptor which is converted from the string representation.
	 */
	public static DataDescriptor fromString(String str, MutableIndex mutableIndex) {
		int start;
		if (mutableIndex == null) {
			start = 0;
		} else {
			start = mutableIndex.value;
		}
		int index = start;
		for (; index < str.length(); index++) {
			if (str.charAt(index) != '[') {
				break;
			}
		}
		if (index >= str.length()) {
			return null;
		}
		int arrayCount = index - start;
		ComponentType componentType = ComponentType.fromChar(str.charAt(index));
		if (componentType == null) {
			return null;
		}
		index++;

		String className = null;
		if (componentType == ComponentType.REFERENCE) {
			int end = str.indexOf(';', index);
			if (end < 0) {
				// NOTE: should end just be str.length here?
				return null;
			}
			className = Utils.classPathToPackage(str.substring(index, end));
			index = end + 1;
		}

		if (mutableIndex != null) {
			// we may be processing the method string which has multiple of these for parameters and return type
			mutableIndex.setValue(index);
		}
		return new DataDescriptor(arrayCount, componentType, className);
	}

	/**
	 * Return the depth of the arrays that this type is, or 0 if not an array type.
	 */
	public int getArrayDepth() {
		return arrayDepth;
	}

	/**
	 * Return true if this type is an array type (1 or more dimensions) otherwise false.
	 */
	public boolean isArray() {
		return (arrayDepth != 0);
	}

	/**
	 * Get the component-type of this data which can be either a primitive or an object type.
	 */
	public ComponentType getComponentType() {
		return componentType;
	}

	/**
	 * Return if this type is a primitive type.
	 */
	public boolean isPrimitive() {
		return componentType.isPrimitive();
	}

	/**
	 * Return if this type is an object reference type.
	 */
	public boolean isReference() {
		return (componentType == ComponentType.REFERENCE);
	}

	/**
	 * Return the primitive data-class of the data-type or null if it is an object reference. If the type is a reference
	 * then you should use {@link #getDataClassName()} to get the type of the reference since we don't want to load the
	 * class here.
	 */
	public Class<?> getDataClass() {
		return componentType.dataClass;
	}

	/**
	 * Return the primitive data-class name or the name of reference class if type is an object reference type.
	 */
	public String getDataClassName() {
		if (componentType == ComponentType.REFERENCE) {
			return referenceClassName;
		} else {
			return componentType.dataClass.getName();
		}
	}

	/**
	 * Name of the class if the component-type is {@link ComponentType#REFERENCE} or null if other component-type.
	 */
	public String getReferenceClassName() {
		return referenceClassName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (arrayDepth > 0) {
			sb.append("array of ");
		}
		sb.append(getDataClassName());
		return sb.toString();
	}

	/**
	 * Component type of the data type which can be a primitive type, REFERENCE, or VOID.
	 */
	public static enum ComponentType {
		BYTE('B', true, Byte.TYPE),
		CHAR('C', true, Character.TYPE),
		DOUBLE('D', true, Double.TYPE),
		FLOAT('F', true, Float.TYPE),
		INT('I', true, Integer.TYPE),
		LONG('J', true, Long.TYPE),
		/* NOTE: the data-type is null because we can't instantiate the class here and data-class-name should be used */
		REFERENCE('L', false, null),
		SHORT('S', true, Short.TYPE),
		BOOLEAN('Z', true, Boolean.TYPE),
		VOID('V', true, Void.TYPE),
		// end
		;

		private static final ComponentType types[];

		private final char ch;
		private final boolean primitive;
		private final Class<?> dataClass;

		static {
			int max = 0;
			for (ComponentType type : values()) {
				if (type.ch > max) {
					max = type.ch;
				}
			}
			types = new ComponentType[max + 1];
			for (ComponentType type : values()) {
				types[type.ch] = type;
			}
		}

		private ComponentType(char ch, boolean primitive, Class<?> dataClass) {
			this.ch = ch;
			this.primitive = primitive;
			this.dataClass = dataClass;
		}

		public boolean isPrimitive() {
			return primitive;
		}

		public static ComponentType fromChar(char ch) {
			if (ch >= types.length) {
				return null;
			} else {
				return types[ch];
			}
		}
	}

	/**
	 * Mutable index integer so we can track our parsing of the method descriptor.
	 */
	static class MutableIndex {
		private int value;

		public MutableIndex(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public void increment(int adjust) {
			this.value += adjust;
		}
	}
}
