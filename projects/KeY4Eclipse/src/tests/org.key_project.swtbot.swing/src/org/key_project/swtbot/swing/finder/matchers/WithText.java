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
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * <p>
 * Matches {@link Component} if the getText() method of the {@link Component} matches the specified text.
 * </p>
 * <p>
 * The class structure (attributes, methods, visibilities, ...) is oriented
 * on the implementation of {@link org.eclipse.swtbot.swt.finder.matchers.WithText}.
 * </p>
 * @author Martin Hentschel
 */
public class WithText<T> extends AbstractMatcher<T> {
   /**
    * The text
    */
   protected String text;

   /**
    * A flag to denote if this should ignore case.
    */
   protected boolean ignoreCase  = false;
   
   /**
    * Constructs this matcher with the given text.
    * @param text The text to match on the {@link Component}
    */
   WithText(String text) {
      this(text, false);
   }

   /**
    * Constructs this matcher with the given text.
    * @param text The text to match on the {@link Component}
    * @param ignoreCase Determines if this should ignore case during the comparison.
    */
   WithText(String text, boolean ignoreCase) {
      text = text.replaceAll("\\r\\n", "\n");
      text = text.replaceAll("\\r", "\n");
      this.text = text;
      this.ignoreCase = ignoreCase;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void describeTo(Description description) {
      description.appendText("with text '").appendText(text).appendText("'");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected boolean doMatch(Object obj) {
      try {
         boolean result = false;
         if (ignoreCase)
            result = getText(obj).equalsIgnoreCase(text);
         else
            result = getText(obj).equals(text);
         return result;
      } catch (Exception e) {
         // do nothing
      }
      return false;
   }
   
   /**
    * Gets the text of the object using the getText method. If the object doesn't contain a get text method an
    * exception is thrown.
    * @param obj any object to get the text from.
    * @return the return value of obj#getText()
    * @throws NoSuchMethodException if the method "getText" does not exist on the object.
    * @throws IllegalAccessException if the java access control does not allow invocation.
    * @throws InvocationTargetException if the method "getText" throws an exception.
    * @see Method#invoke(Object, Object[])
    */   
   static String getText(Object obj) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      return ((String) SWTUtils.invokeMethod(obj, "getText")).replaceAll(Text.DELIMITER, "\n");
   }
   
   /**
    * Matches a widget that has the specified exact text.
    * @param text the label.
    * @return a matcher.
    */
   @Factory
   public static <T> Matcher<T> withText(String text) {
      return new WithText<T>(text);
   }

   /**
    * Matches a widget that has the specified text, ignoring case considerations.
    * @param text the label.
    * @return a matcher.
    */
   @Factory
   public static <T> Matcher<T> withTextIgnoringCase(String text) {
      return new WithText<T>(text, true);
   }
}