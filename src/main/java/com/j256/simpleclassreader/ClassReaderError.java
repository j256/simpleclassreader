package com.j256.simpleclassreader;

/**
 * Enumerated types of errors generated by reading in a class.
 * 
 * @author graywatson
 */
public class ClassReaderError {

	private final ClassReaderErrorType type;
	private final String details;

	public ClassReaderError(ClassReaderErrorType type, String details) {
		this.type = type;
		this.details = details;
	}

	public ClassReaderError(ClassReaderErrorType type, Object details) {
		this.type = type;
		if (details == null) {
			this.details = null;
		} else {
			this.details = String.valueOf(details);
		}
	}

	public ClassReaderErrorType getType() {
		return type;
	}

	public String getDetails() {
		return details;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		if (details != null) {
			sb.append(": ").append(details);
		}
		return sb.toString();
	}
}
