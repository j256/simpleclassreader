package com.j256.simpleclassreader;

/**
 * Type of entries in the cp-info table.
 */
public enum ConstantPoolType {
	UTF8(1),
	INTEGER(3),
	FLOAT(4),
	LONG(5),
	DOUBLE(6),
	CLASS(7),
	STRING(8),
	FIELD_REF(9),
	METHOD_REF(10),
	INTERFACE_REF(11),
	NAME_AND_TYPE(12),
	METHOD_HANDLE(15),
	METHOD_TYPE(16),
	INVOKE_DYNAMIC(18),
	MODULE(19),
	PACKAGE(20),
	// end
	;

	private static final ConstantPoolType[] types;
	private final int code;

	static {
		int max = 0;
		for (ConstantPoolType type : values()) {
			if (type.code > max) {
				max = type.code;
			}
		}
		types = new ConstantPoolType[max + 1];
		for (ConstantPoolType type : values()) {
			types[type.code] = type;
		}
	}

	private ConstantPoolType(int code) {
		this.code = code;
	}

	/**
	 * Return the cp-info type associated with code or null if none.
	 */
	public static ConstantPoolType fromCode(int code) {
		if (code < 0 || code >= types.length) {
			return null;
		} else {
			return types[code];
		}
	}
}
