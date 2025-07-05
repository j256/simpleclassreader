Simple Java Class Reader
========================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.simpleclassreader/simpleclassreader/badge.svg?style=flat-square)](https://mvnrepository.com/artifact/com.j256.simpleclassreader/simpleclassreader/latest)
[![javadoc](https://javadoc.io/badge2/com.j256.simpleclassreader/simpleclassreader/javadoc.svg)](https://javadoc.io/doc/com.j256.simpleclassreader/simpleclassreader)
[![ChangeLog](https://img.shields.io/github/v/release/j256/simpleclassreader?label=changelog&display_name=release)](https://github.com/j256/simpleclassreader/blob/master/src/main/javadoc/doc-files/changelog.txt)
[![CodeCov](https://img.shields.io/codecov/c/github/j256/simpleclassreader.svg)](https://codecov.io/github/j256/simpleclassreader/)
[![CircleCI](https://circleci.com/gh/j256/simpleclassreader.svg?style=shield)](https://circleci.com/gh/j256/simpleclassreader)
[![GitHub License](https://img.shields.io/github/license/j256/simpleclassreader)](https://github.com/j256/simpleclassreader/blob/master/LICENSE.txt)

Little library that allows you to read class bytes and see all of the class metadata without loading the class which
might cause a large amount of class loading from imports.  There are a number of times you might want to do this if
you wanted to interrogate a class to make sure that it has certain annotations or qualities before incurring the
cost of loading it with the ClassLoader.

Enjoy.  Gray Watson

## Quick Example:

Read in a class from a file.

	String path = "target/classes/path/to/classfile.class";
	CsvProcessor<Account> csvProcessor = new CsvProcessor<Account>(Account.class);
	try (InputStream fis = new FileInputStream(path);) {
		ClassInfo info = ClassReader.readClass(fis);
		System.out.println("Class name is " + info.getName());
		System.out.println("Fields: " + Arrays.toString(info.getFields());
		System.out.println("Constructors: " + Arrays.toString(info.getConstructors());
		System.out.println("Methods: " + Arrays.toString(info.getMethods());
	}

# Maven Configuration

Maven packages are published via [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.simplecsv/simplecsv/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.j256.simplecsv/simplecsv/)

``` xml
<dependency>
	<groupId>com.j256.simpleclassreader</groupId>
	<artifactId>simpleclassreader</artifactId>
	<version>0.1</version>
</dependency>
```

# ChangeLog Release Notes

See the [ChangeLog.txt file](src/main/javadoc/doc-files/changelog.txt).
