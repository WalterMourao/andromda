/*
 * Licensed to the Apache Software Foundation (ASF) under one
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Stereotype;

import de.crowdcode.kissmda.core.Context;
import de.crowdcode.kissmda.core.Transformer;
import de.crowdcode.kissmda.core.TransformerException;
import de.crowdcode.kissmda.core.file.JavaFileWriter;
import de.crowdcode.kissmda.core.uml.PackageHelper;

/**
 * Simple Java Transformer. This generates Interfaces for all the classes from
 * the given UML model.
 * 
 * <p>
 * Most important helper classes from kissmda-core which are used in this
 * Transformer: PackageHelper, JavaFileWriter.
 * </p>
 * 
 * @author Lofi Dewanto
 * @version 1.0.0
 * @since 1.0.0
 */
public class JavaTransformer implements Transformer {

	private static final Logger logger = Logger
			.getLogger(JavaTransformer.class.getName());

	private static final String STEREOTYPE_ENUMERATION = "Enumeration";

	private static final String STEREOTYPE_SOURCEDIRECTORY = "SourceDirectory";

	private String sourceDirectoryPackageName;

	@Inject
	private PackageHelper packageHelper;

	@Inject
	private JavaFileWriter javaFileWriter;

	@Inject
	private EnumClassGenerator enumClassGenerator;

    @Inject
    private JavaCodeFormatter javaCodeFormatter;

    private Context context;

	/**
	 * Start the transformation and generation.
	 * 
	 * @param context
	 *            context object from Maven plugin
	 * @return void nothing
	 * @exception throw
	 *                TransformerException if something wrong happens
	 */
	@Override
	public void transform(Context context) throws TransformerException {
		this.context = context;
		try {
			// Get the root package
			org.eclipse.uml2.uml.Package outPackage = getRootPackage(context);
			sourceDirectoryPackageName = "";

			// Check the stereotype of the root package
			checkStereotypeRootPackage(outPackage);

			// Get all elements with defined stereotypes and enums
			EList<Element> elements = outPackage.allOwnedElements();
			for (Element element : elements) {
				// Stereotypes
				EList<Stereotype> stereotypes = element.getAppliedStereotypes();
				for (Stereotype stereotype : stereotypes) {
					if (stereotype.getName().equals(STEREOTYPE_SOURCEDIRECTORY)) {
						// From this SourceDirectory we can work...
						org.eclipse.uml2.uml.Package packagez = (org.eclipse.uml2.uml.Package) element;
						sourceDirectoryPackageName = packagez.getName();
						logger.log(Level.FINE, "SourceDirectory package name: "
								+ sourceDirectoryPackageName);
					}
					if (stereotype.getName().equals(STEREOTYPE_ENUMERATION)) {
						generateEnumerationClass(element);
					}
				}

				/***
				// Enums
				if (element.eClass().getName().equals(TYPE_ENUM)) {
					generateEnum(element);
				}
				***/
			}
		} catch (URISyntaxException e) {
			throw new TransformerException(e);
		} catch (IOException e) {
			throw new TransformerException(e);
		}
	}

	private void generateEnumerationClass(Element element) throws IOException {
		Class clazz = (Class) element;
		logger.log(Level.FINE, "Enumeration class: " + clazz.getName());
		// Generate the enumeration for this class
		CompilationUnit compilationUnit = enumClassGenerator.generateEnum(clazz,
				sourceDirectoryPackageName);
		generateClassFile(clazz, compilationUnit);
	}

	private void checkStereotypeRootPackage(
			org.eclipse.uml2.uml.Package outPackage) {
		EList<Stereotype> rootStereotypes = outPackage.getAppliedStereotypes();
		for (Stereotype stereotype : rootStereotypes) {
			if (stereotype.getName().equals(STEREOTYPE_SOURCEDIRECTORY)) {
				// From this SourceDirectory we can work...
				org.eclipse.uml2.uml.Package packagez = outPackage;
				sourceDirectoryPackageName = packagez.getName();
				logger.log(Level.FINE, "SourceDirectory package name: "
						+ sourceDirectoryPackageName);
			}
		}
	}

	private org.eclipse.uml2.uml.Package getRootPackage(Context context)
			throws URISyntaxException {
		org.eclipse.uml2.uml.Package outPackage = packageHelper
				.getRootPackage(context);
		return outPackage;
	}

	/**
	 * Create the output file on the directory. We also format the code before
	 * we save it.
	 * 
	 * @param clazz
	 *            UML2 class of Eclipse
	 * @param compilationUnit
	 *            compilation unit from JDT
	 * @throws IOException
	 *             input or output error on file system
	 */
	private void generateClassFile(Classifier clazz,
			CompilationUnit compilationUnit) throws IOException {

		String fullPackageName = packageHelper.getFullPackageName(clazz,
				sourceDirectoryPackageName);

        // Format before we generate the class file
        String formattedCode = javaCodeFormatter.format(compilationUnit
                .toString());
        javaFileWriter.createJavaFile(context, fullPackageName,
                clazz.getName(), formattedCode);
	}
}