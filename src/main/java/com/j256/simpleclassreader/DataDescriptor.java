package com.j256.simpleclassreader;

/**
 * Type of field, method parameter, or method return value.
 * 
 * @author graywatson
 */
public class DataDescriptor {

	private final int arrayDepth;
	private final BaseType baseType;
	private final String referenceClassName;

	public DataDescriptor(int arrayDepth, BaseType baseType, String referenceClassName) {
		this.arrayDepth = arrayDepth;
		this.baseType = baseType;
		this.referenceClassName = referenceClassName;
	}

	/**
	 * Convert from string data type representation returning null if invalid.
	 */
	public static DataDescriptor fromString(String str) {
		return fromString(str, null);
	}

	public static DataDescriptor fromString(String str, MutableIndex mutableIndex) {
		int index;
		if (mutableIndex == null) {
			index = 0;
		} else {
			index = mutableIndex.value;
		}
		for (; index < str.length(); index++) {
			if (str.charAt(index) != '[') {
				break;
			}
		}
		if (index >= str.length()) {
			return null;
		}
		int arrayCount = index;
		BaseType baseType = BaseType.fromChar(str.charAt(index));
		if (baseType == null) {
			return null;
		}
		index++;

		String className = null;
		if (baseType == BaseType.REFERENCE) {
			int end = str.indexOf(';', index);
			className = str.substring(index, end);
			className = className.replace('/', '.');
			index = end + 1;
		}

		if (mutableIndex != null) {
			mutableIndex.setValue(index);
		}
		return new DataDescriptor(arrayCount, baseType, className);
	}

	/**
	 * Return the number of arrays that this type is or 0 if none.
	 */
	public int getArrayDepth() {
		return arrayDepth;
	}

	/**
	 * Return true if this type is an array type (of 1 or more dimensions) otherwise false.
	 */
	public boolean isArray() {
		return (arrayDepth != 0);
	}

	/**
	 * Get the base-type of this data which can be either a primitive or an object type.
	 */
	public BaseType getBaseType() {
		return baseType;
	}

	/**
	 * Return if this type is a primitive type.
	 */
	public boolean isPrimitive() {
		return baseType.isPrimitive();
	}

	/**
	 * Return if this type is an object type.
	 */
	public boolean isObject() {
		return (baseType == BaseType.REFERENCE);
	}

	/**
	 * Return the primitive data-class of the data-type or Object.class if it is an object reference.
	 */
	public Class<?> getDataClass() {
		return baseType.dataClass;
	}

	/**
	 * Return the primitive data-class name or the name of reference class if type is a REFERENCE.
	 */
	public String getDataClassName() {
		if (baseType == BaseType.REFERENCE) {
			return referenceClassName;
		} else {
			return baseType.dataClass.getName();
		}
	}

	/**
	 * Classname if the base-type is BaseType.REFERENCE.
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
	 * Base type of the data type.
	 */
	public static enum BaseType {
		BYTE('B', true, Byte.TYPE),
		CHAR('C', true, Character.TYPE),
		DOUBLE('D', true, Double.TYPE),
		FLOAT('F', true, Float.TYPE),
		INT('I', true, Integer.TYPE),
		LONG('J', true, Long.TYPE),
		REFERENCE('L', true, Object.class),
		SHORT('S', true, Short.TYPE),
		BOOLEAN('Z', true, Boolean.TYPE),
		VOID('V', true, Void.TYPE),
		// end
		;

		private static final BaseType types[];

		private final char ch;
		private final boolean primitive;
		private final Class<?> dataClass;

		static {
			int max = 0;
			for (BaseType type : values()) {
				if (type.ch > max) {
					max = type.ch;
				}
			}
			types = new BaseType[max + 1];
			for (BaseType type : values()) {
				types[type.ch] = type;
			}
		}

		private BaseType(char ch, boolean primitive, Class<?> dataClass) {
			this.ch = ch;
			this.primitive = primitive;
			this.dataClass = dataClass;
		}

		public boolean isPrimitive() {
			return primitive;
		}

		public static BaseType fromChar(char ch) {
			if (ch >= types.length) {
				return null;
			} else {
				return types[ch];
			}
		}
	}

	/**
	 * Little mutable int so we can track our parsing of the method descriptor.
	 */
	public static class MutableIndex {
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
