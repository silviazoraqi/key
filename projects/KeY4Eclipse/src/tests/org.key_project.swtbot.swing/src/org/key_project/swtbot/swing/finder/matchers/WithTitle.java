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

package org.key_project.swtbot.swing.finder.matchers;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher;
import org.eclipse.swtbot.swt.finder.matchers.WithText;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * <p>
 * Matches {@link Component} if the getTitle() method of the {@link Component} matches the specified text.
 * </p>
 * <p>
 * The class structure (attributes, methods, visibilities, ...) is oriented
 * on the implementation of {@link WithText}.
 * </p>
 * @author Martin Hentschel
 */
public class WithTitle<T> extends AbstractMatcher<T> {
   /**
    * The title
    */
   protected String title;

   /**
    * A flag to denote if this should ignore case.
    */
   protected boolean ignoreCase  = false;
   
   /**
    * Constructs this matcher with the given title.
    * @param title The title to match on the {@link Component}
    */
   WithTitle(String title) {
      this(title, false);
   }

   /**
    * Constructs this matcher with the given title.
    * @param title The title to match on the {@link Component}
    * @param ignoreCase Determines if this should ignore case during the comparison.
    */
   WithTitle(String title, boolean ignoreCase) {
      title = title.replaceAll("\\r\\n", "\n");
      title = title.replaceAll("\\r", "\n");
      this.title = title;
      this.ignoreCase = ignoreCase;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void describeTo(Description description) {
      description.appendText("with title '").appendText(title).appendText("'");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected boolean doMatch(Object obj) {
      try {
         boolean result = false;
         if (ignoreCase)
            result = getTitle(obj).equalsIgnoreCase(title);
         else
            result = getTitle(obj).equals(title);
         return result;
      } catch (Exception e) {
         // do nothing
      }
      return false;
   }
   
   /**
    * Gets the title of the object using the getTitle method. If the object doesn't contain a get title method an
    * exception is thrown.
    * @param obj any object to get the title from.
    * @return the return value of obj#getTitle()
    * @throws NoSuchMethodException if the method "getTitle" does not exist on the object.
    * @throws IllegalAccessException if the java access control does not allow invocation.
    * @throws InvocationTargetException if the method "getTitle" throws an exception.
    * @see Method#invoke(Object, Object[])
    */   
   static String getTitle(Object obj) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      return ((String) SWTUtils.invokeMethod(obj, "getTitle")).replaceAll(Text.DELIMITER, "\n");
   }
   
   /**
    * Matches a widget that has the specified exact title.
    * @param title the label.
    * @return a matcher.
    */
   @Factory
   public static <T> Matcher<T> withTitle(String title) {
      return new WithTitle<T>(title);
   }

   /**
    * Matches a widget that has the specified title, ignoring case considerations.
    * @param title the label.
    * @return a matcher.
    */
   @Factory
   public static <T> Matcher<T> withTitleIgnoringCase(String title) {
      return new WithTitle<T>(title, true);
   }
}