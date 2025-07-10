package com.j256.simpleclassreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.j256.simpleclassreader.attribute.AnnotationInfo;
import com.j256.simpleclassreader.attribute.AttributeType;
import com.j256.simpleclassreader.attribute.ExceptionsAttribute;
import com.j256.simpleclassreader.attribute.RuntimeVisibleAnnotationsAttribute;

/**
 * Information about a method of a class.
 * 
 * @author graywatson
 */
public class MethodInfo {

	/** name of the constructor methods in the method list (hopefully true) */
	private static final String CONSTRUCTOR_METHOD_NAME = "<init>";

	private final String name;
	private final int accessFlags;
	private final MethodDescriptor methodDescriptor;
	private final AttributeInfo[] attributes;
	private final String[] exceptions;
	private final AnnotationInfo[] runtimeAnnotations;
	private final boolean deprecated;
	private final boolean constructor;

	public MethodInfo(String name, int accessFlags, MethodDescriptor methodDescriptor, AttributeInfo[] attributes,
			String[] exceptions, AnnotationInfo[] runtimeAnnotations, boolean deprecated) {
		this.name = name;
		this.accessFlags = accessFlags;
		this.methodDescriptor = methodDescriptor;
		this.attributes = attributes;
		this.exceptions = exceptions;
		this.runtimeAnnotations = runtimeAnnotations;
		this.deprecated = deprecated;
		// we see if the method name is the constructor constant
		this.constructor = CONSTRUCTOR_METHOD_NAME.equals(name);
	}

	/**
	 * Read in a field information entry.
	 */
	public static MethodInfo read(DataInputStream dis, ConstantPool constantPool, List<ClassReaderError> errors)
			throws IOException {

		// u2 access_flags;
		// u2 name_index;
		// u2 descriptor_index;
		// u2 attributes_count;
		// attribute_info attributes[attributes_count];

		int accessFlags = dis.readUnsignedShort();
		int index = dis.readUnsignedShort();
		String name = constantPool.findName(index);
		if (name == null) {
			errors.add(new ClassReaderError(ClassReaderErrorType.METHOD_NAME_INDEX_INVALID, index));
			return null;
		}
		index = dis.readUnsignedShort();
		String descriptorStr = constantPool.findName(index);
		if (descriptorStr == null) {
			errors.add(new ClassReaderError(ClassReaderErrorType.METHOD_DESCRIPTOR_INDEX_INVALID, index));
			return null;
		}
		MethodDescriptor methodDescriptor = null;
		if (descriptorStr != null) {
			methodDescriptor = MethodDescriptor.fromString(descriptorStr);
		}
		int attributeCount = dis.readUnsignedShort();
		List<AttributeInfo> attributeInfos = null;
		String[] exceptions = null;
		AnnotationInfo[] runtimeAnnotations = null;
		boolean deprecated = false;
		for (int i = 0; i < attributeCount; i++) {
			AttributeInfo attributeInfo = AttributeInfo.read(dis, constantPool, errors);
			if (attributeInfo == null) {
				continue;
			}
			if (attributeInfo.getType() == AttributeType.EXCEPTIONS) {
				exceptions = ((ExceptionsAttribute) attributeInfo.getValue()).getExceptions();
			}
			if (attributeInfo.getType() == AttributeType.RUNTIME_VISIBLE_ANNOTATIONS) {
				runtimeAnnotations = ((RuntimeVisibleAnnotationsAttribute) attributeInfo.getValue()).getAnnotations();
			}
			if (attributeInfo.getType() == AttributeType.DEPRECATED) {
				deprecated = true;
			}
			if (attributeInfos == null) {
				attributeInfos = new ArrayList<>();
			}
			attributeInfos.add(attributeInfo);
		}

		AttributeInfo[] attributes = AttributeInfo.EMPTY_ARRAY;
		if (attributeInfos != null) {
			attributes = attributeInfos.toArray(new AttributeInfo[attributeInfos.size()]);
		}
		return new MethodInfo(name, accessFlags, methodDescriptor, attributes, exceptions, runtimeAnnotations,
				deprecated);
	}

	/**
	 * Returns the name of the method. Constructors will have the name of "<init>"
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the access-flags for the method. The {@link #isPublic()} and other methods use this access-flags data.
	 */
	public int getAccessFlagsValue() {
		return accessFlags;
	}

	/**
	 * Get the access-flags as an array of enums.
	 */
	public AccessFlag[] getAccessFlags() {
		return AccessFlag.extractFlags(accessFlags, false, false, true);
	}

	/**
	 * Returns true if declared public; may be accessed from outside its package
	 */
	public boolean isPublic() {
		return AccessFlag.PUBLIC.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared private; accessible only within defining class and other classes belonging to same nest
	 */
	public boolean isPrivate() {
		return AccessFlag.PRIVATE.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared protected; may be accessed within subclasses.
	 */
	public boolean isProtected() {
		return AccessFlag.PROTECTED.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared static to the class.
	 */
	public boolean isStatic() {
		return AccessFlag.STATIC.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared final; must not be overridden
	 */
	public boolean isFinal() {
		return AccessFlag.FINAL.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared synchronized; invocation is wrapped by a monitor use.
	 */
	public boolean isSynchronized() {
		return AccessFlag.SYNCHRONIZED.isEnabled(accessFlags);
	}

	/**
	 * Returns true if this is a bridge method, generated by the compiler.
	 */
	public boolean isBridge() {
		return AccessFlag.BRIDGE.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared with variable number of arguments.
	 */
	public boolean isVarargs() {
		return AccessFlag.VARARGS.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared native; implemented in a language other than the Java programming language.
	 */
	public boolean isNative() {
		return AccessFlag.NATIVE.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared abstract; no implementation is provided.
	 */
	public boolean isAbstract() {
		return AccessFlag.ABSTRACT.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared strictfp; floating-point mode is FP-strict.
	 */
	public boolean isStrict() {
		return AccessFlag.STRICT.isEnabled(accessFlags);
	}

	/**
	 * Returns true if declared synthetic; not present in the source code.
	 */
	public boolean isSynthetic() {
		return AccessFlag.SYNTHETIC.isEnabled(accessFlags);
	}

	/**
	 * Returns true if this method has the constructor name of "<init>"
	 */
	public boolean isConstructor() {
		return constructor;
	}

	/**
	 * Returns the details about the parameters and return type.
	 */
	public MethodDescriptor getMethodDescriptor() {
		return methodDescriptor;
	}

	/**
	 * Returns the attributes of the method.
	 */
	public AttributeInfo[] getAttributes() {
		return attributes;
	}

	/**
	 * Returns the parameter descriptors from the method descriptor.
	 */
	public DataDescriptor[] getParameterDataDescriptors() {
		if (methodDescriptor == null) {
			return null;
		} else {
			return methodDescriptor.getParameterDescriptors();
		}
	}

	/**
	 * Returns the return descriptor from the method descriptor.
	 */
	public DataDescriptor getReturnDescriptor() {
		if (methodDescriptor == null) {
			return null;
		} else {
			return methodDescriptor.getReturnDescriptor();
		}
	}

	/**
	 * Exceptions extracted from the attributes.
	 */
	public String[] getExceptions() {
		return exceptions;
	}

	/**
	 * Return any runtime annotations on this method.
	 */
	public AnnotationInfo[] getRuntimeAnnotations() {
		return runtimeAnnotations;
	}

	/**
	 * Is the method marked with the Deprecated attribute.
	 */
	public boolean isDeprecated() {
		return deprecated;
	}

	@Override
	public String toString() {
		return "method " + name;
	}
}
