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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import javax.inject.Inject;

import org.andromda.cartridges.java.SimpleJavaTransformer;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.crowdcode.kissmda.core.Context;
import de.crowdcode.kissmda.core.StandardContext;
import de.crowdcode.kissmda.core.TransformerException;

/**
 * Test Guice Java Module.
 * 
 * @author Lofi Dewanto
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(JukitoRunner.class)
public class SimpleJavaModuleTest {

	private static final Logger logger = Logger
			.getLogger(SimpleJavaModuleTest.class.getName());

	@Inject
	private SimpleJavaTransformer simpleJavaTransformer;

	private Context context;

	@Before
	public void setUp() throws Exception {
		context = new StandardContext();
	}

	@Test
	public void testConfigure() {
		try {
			String thisPath = this.getClass().getProtectionDomain()
					.getCodeSource().getLocation().getPath();
			logger.info("Path: " + thisPath);
			context.setSourceModel(thisPath + "model/emf/test-uml.uml");
			context.setTargetModel("target/generated-sources/java-module");
			simpleJavaTransformer.transform(context);
		} catch (TransformerException e) {
			assertFalse(true);
		}
		assertTrue(true);
	}

}
