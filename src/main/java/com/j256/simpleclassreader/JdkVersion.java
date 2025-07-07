package com.j256.simpleclassreader;

/**
 * Version of the JDK based on the major and minor numbers from the class file.
 * 
 * @author graywatson
 */
public enum JdkVersion {
	JDK_1_1(45, "1.1"),
	JDK_1_2(46, "1.2"),
	JDK_1_3(47, "1.3"),
	JDK_1_4(48, "1.4"),
	JDK_5(49, "5"),
	JDK_6(50, "6"),
	JDK_7(51, "7"),
	JDK_8(52, "8"),
	JDK_9(53, "9"),
	JDK_10(54, "10"),
	JDK_11(55, "11"),
	JDK_12(56, "12"),
	JDK_13(57, "13"),
	JDK_14(58, "14"),
	JDK_15(59, "15"),
	JDK_16(60, "16"),
	JDK_17(61, "17"),
	JDK_18(62, "18"),
	JDK_19(63, "19"),
	JDK_20(64, "20"),
	JDK_21(65, "21"),
	JDK_22(66, "22"),
	JDK_23(67, "23"),
	JDK_24(68, "24"),
	// end
	;

	private static final JdkVersion[] versions;

	private final int majorVersion;
	private final String jdk;

	static {
		int max = 0;
		for (JdkVersion version : values()) {
			if (version.majorVersion > max) {
				max = version.majorVersion;
			}
		}
		versions = new JdkVersion[max + 1];
		for (JdkVersion version : values()) {
			versions[version.majorVersion] = version;
		}
	}

	private JdkVersion(int majorVersion, String jdk) {
		this.majorVersion = majorVersion;
		this.jdk = jdk;
	}

	/**
	 * Lookup the version by it's major number. Returns null if unknown.
	 */
	public static JdkVersion fromMajor(int majorVersion) {
		if (majorVersion < 0 || majorVersion >= versions.length) {
			return null;
		} else {
			return versions[majorVersion];
		}
	}

	public int getMajorVersion() {
		return majorVersion;
	}

	public String getJdkString() {
		return jdk;
	}

	public String makeJdkString(int minorVersion) {
		if (jdk.indexOf('.') >= 0) {
			return jdk;
		} else {
			return jdk + "." + minorVersion;
		}
	}
}
