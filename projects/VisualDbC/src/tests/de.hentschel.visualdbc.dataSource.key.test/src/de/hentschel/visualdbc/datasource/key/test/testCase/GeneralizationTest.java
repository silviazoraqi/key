/*******************************************************************************
 * Copyright (c) 2011 Martin Hentschel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Hentschel - initial API and implementation
 *******************************************************************************/

package de.hentschel.visualdbc.datasource.key.test.testCase;

import junit.framework.TestCase;

import org.junit.Test;

import de.hentschel.visualdbc.datasource.key.model.KeyConnection;
import de.hentschel.visualdbc.datasource.key.test.util.TestKeyUtil;
import de.hentschel.visualdbc.datasource.model.DSPackageManagement;

/**
 * Tests the handling of generalization (extends and implements) in a {@link KeyConnection}.
 * @author Martin Hentschel
 */
public class GeneralizationTest extends TestCase {
   /**
    * Tests attributes.
    */
   @Test
   public void testExtendsAndImplements() {
      TestKeyUtil.testKeyConnection("GeneralizationTest_testExtendsAndImplements",
                                    "data/generalizationTest",
                                    null,
                                    DSPackageManagement.FLAT_LIST,
                                    TestKeyUtil.createExpectedGeneralizationTestModel());
   }
}