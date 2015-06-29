/*
x * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.andromda.cartridges.java;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Property;

import de.crowdcode.kissmda.core.uml.PackageHelper;

/**
 * Generate enumeration from UML class with <<Enumeration>> stereotype.
 * 
 * @author Walter Mourão
 * @version 1.0.0
 * @since 1.0.0
 */
public class EnumClassGenerator {

	private static final Logger logger = Logger.getLogger(EnumClassGenerator.class.getName());

	@Inject
	private PackageHelper packageHelper;
	
	@Inject
	private AndromdaHelper andromdaHelper;
	
	@Inject
	private NameMasker nameMasker;
	
	private String sourceDirectoryPackageName;
	private String javaType;
	
	/**
	 * Generate the Enum from the <<Enumeration>> class.
	 * 
	 * @param Class
	 *            clazz the UML class
	 * @return the complete class with its content as a String
	 */
	public String generateEnumClass(Classifier clazz) {

	    StringWriter outString= new StringWriter();
		PrintWriter writer=new PrintWriter(outString);

		javaType = clazz.getAttributes().iterator().next().getType().getName();
		
		generatePackage(clazz, writer);
		generateEnum(clazz, writer);
		
		writer.flush();

		String fileContent = outString.toString();
		logger.log(Level.INFO, "Compilation unit: \n\n" + fileContent);
		return fileContent;
	}

	protected static final String FROM_STRING_COMMENT = "Retrieves an instance of %s from <code>its name</code>.";
	
    /**
     * Generate the fromString method.
     * 
     * @param Class
     *              clazz the UML class
     * @param PrintWriter
     *              the writer used to write the code
     */
	protected void generateFromStringMethod(Classifier clazz, PrintWriter writer) {
        String className = clazz.getName();
        generateSimpleComment(writer, String.format(FROM_STRING_COMMENT, className));
        writer.format("public static %s fromString(%s value) { return %s.valueOf(value); }",className,javaType,className);
        writer.println();
	}

	protected static final String VALUE_COMMENT = "Returns an enumeration literal String <code>value</code>.";

    /**
     * Generate the value method.
     * 
     * @param Class
     *              clazz the UML class
     * @param PrintWriter
     *              the writer used to write the code
     */
	protected void generateValueMethod(Classifier clazz, PrintWriter writer) {
        generateSimpleComment(writer, String.format(VALUE_COMMENT, clazz.getName()));
        writer.format("public %s value() { return this.enumValue; }",javaType);
        writer.println();
    }
	
	protected static final String PRIVATE_CONSTRUCTOR_COMMENT = "The constructor with enumeration literal value allowing super classes to access it.";
	
    /**
     * Generate the private constructor.
     * 
     * @param Class
     *              clazz the UML class
     * @param PrintWriter
     *              the writer used to write the code
     */
    protected void generateConstructor(Classifier clazz, PrintWriter writer) {
        writer.format("private final %s enumValue;",javaType);
        writer.println();
        generateSimpleComment(writer, PRIVATE_CONSTRUCTOR_COMMENT);
        writer.format("private %s(%s value) { this.enumValue = value; }",clazz.getName(),javaType);
        writer.println();
    }

    private static String SERIAL_VERSION_UID_COMMENT = "The serial version UID of this class. Needed for serialization.";
	
    /**
     * Generate the serialVersionUID.
     * 
     * @param Class
     *              clazz the UML class
     * @param PrintWriter
     *              the writer used to write the code
     */
    public void generateSerialVersionUID(Classifier clazz, PrintWriter writer) {
        logger.log(Level.FINE, "Class: " + clazz.getName() + " - serialVersionUID");

        generateSimpleComment(writer, SERIAL_VERSION_UID_COMMENT);

        //TODO generate a number based in class attributes, name, etc.
        writer.println("private static final long serialVersionUID = 1L;");
    }

	/**
	 * Generate the Enum class.
	 * 
     * @param Class
     *              clazz the UML class
     * @param PrintWriter
     *              the writer used to write the code
     */
	public void generateEnum(Classifier clazz, PrintWriter writer) {
	    writer.println("public enum "+getClassName(clazz)+" {");
	    
        generateConstants(clazz, writer);
        generateSerialVersionUID(clazz, writer);
        generateConstructor(clazz, writer);
        generateFromStringMethod(clazz, writer);
        generateValueMethod(clazz, writer);
        
        writer.println("}");
	}

	protected void generateSimpleComment(PrintWriter writer, String...lines){
        writer.println("/**");
        for(String line: lines){
            writer.print(" * ");
            writer.println(line);
        }
        writer.println(" */");
	}
	
    /**
	 * Generate the Java package from UML package.
	 * 
	 * @param clazz
	 *            the UML class
	 * @param ast
	 *            the JDT Java AST
	 * @param cu
	 *            the generated Java compilation unit
	 */
	public void generatePackage(Classifier clazz, PrintWriter writer) {
	    generateSimpleComment(writer, PackageComment.CONTENT_1.getValue(), PackageComment.CONTENT_2.getValue());
	    writer.println("package "+getFullPackageName(clazz)+";");
	}

    protected String resolveConstantValue(Property enumLiteral) {
        String typeName = enumLiteral.getType().getName(); //TODO should use the qualifiedName and map to the desired type
        String value = enumLiteral.getDefaultValue() == null? nameMasker.mask(enumLiteral.getName(),NameMasker.UPPERUNDERSCORE): enumLiteral.getDefaultValue().toString();
        if("Integer".equals(typeName) || "Boolean".equals(typeName)){
            return value;
        } else if("Long".equals(typeName)){
            return value+"L";
        } else {
            return "\""+value+"\"";
        }
    }
	
	/**
	 * Generate Enumeration constants.
	 * 
	 * @param clazz
	 *            the UML class
	 * @param ast
	 *            the JDT Java AST
	 * @param ed
	 *            Enumeration declaration for Java JDT
	 */
	public void generateConstants(Classifier clazz, PrintWriter writer) {
		// Get all properties for this enumeration class
		Class enumerationClass = (Class) clazz;
		EList<Property> attributes = enumerationClass.getAttributes();
		int count=0;
		for (Property enumLiteral : attributes) {
		    count++;
		    String comments=andromdaHelper.concatComments(enumLiteral.getOwnedComments());
		    if(StringUtils.isNotEmpty(comments)){
		        generateSimpleComment(writer, comments.split("\n"));
		    }
            String literalName = nameMasker.mask(enumLiteral.getName(),NameMasker.UPPERUNDERSCORE);
		    writer.format("%s(%s)",literalName,resolveConstantValue(enumLiteral));
		    if(count < attributes.size()){
		        writer.println(",");
		    } else {
                writer.println(";");
		    }
		}
	}

	private String getClassName(Classifier clazz) {
		String className = clazz.getName();
		return className;
	}

	private String getFullPackageName(Classifier clazz) {
		String fullPackageName = packageHelper.getFullPackageName(clazz,
				sourceDirectoryPackageName);
		return fullPackageName;
	}
}
