package org.key_project.sed.core.model.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.key_project.sed.core.model.ISEDDebugNode;
import org.key_project.sed.core.model.ISEDDebugTarget;
import org.key_project.sed.core.model.ISEDThread;
import org.key_project.sed.core.model.memory.ISEDMemoryDebugNode;
import org.key_project.sed.core.model.memory.ISEDMemoryStackFrameCompatibleDebugNode;
import org.key_project.sed.core.model.memory.SEDMemoryBranchCondition;
import org.key_project.sed.core.model.memory.SEDMemoryBranchNode;
import org.key_project.sed.core.model.memory.SEDMemoryDebugTarget;
import org.key_project.sed.core.model.memory.SEDMemoryExceptionalTermination;
import org.key_project.sed.core.model.memory.SEDMemoryMethodCall;
import org.key_project.sed.core.model.memory.SEDMemoryMethodReturn;
import org.key_project.sed.core.model.memory.SEDMemoryStatement;
import org.key_project.sed.core.model.memory.SEDMemoryTermination;
import org.key_project.sed.core.model.memory.SEDMemoryThread;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * Instances of this class are used to read proprietary XML file
 * created via {@link SEDXMLWriter}. The result is a {@link List} of
 * contained {@link ISEDDebugTarget}s. The created instances are memory
 * instances and contains only the saved values. An execution at runtime, like
 * termination, step over, etc. is not possible.
 * </p>
 * <p>
 * The main use case of the serialization is to persistent an actual
 * {@link ISEDDebugTarget} as oracle file which is later used in test cases
 * to compare a given {@link ISEDDebugTarget} with the loaded instance
 * of the oracle file.
 * </p>
 * @author Martin Hentschel
 * @see SEDXMLWriter
 */
public class SEDXMLReader {
   /**
    * Parses the given XML content.
    * @param xml The XML content to parse.
    * @return The contained {@link ISEDDebugTarget}s in the given XML content.
    * @throws ParserConfigurationException Occurred Exception.
    * @throws SAXException Occurred Exception.
    * @throws IOException Occurred Exception.
    */
   public List<ISEDDebugTarget> read(String xml) throws ParserConfigurationException, SAXException, IOException {
      return read(new ByteArrayInputStream(xml.getBytes()));
   }
   
   /**
    * Parses the given XML content defined by the {@link InputStream}.
    * @param in The {@link InputStream} with the XML content to parse.
    * @return The contained {@link ISEDDebugTarget}s in the given XML content.
    * @throws ParserConfigurationException Occurred Exception.
    * @throws SAXException Occurred Exception.
    * @throws IOException Occurred Exception.
    */
   public List<ISEDDebugTarget> read(InputStream in) throws ParserConfigurationException, SAXException, IOException {
      if (in != null) {
         try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            SEDSAXHandler handler = new SEDSAXHandler();
            saxParser.parse(in, handler);
            return handler.getResult();
         }
         finally {
            in.close();
         }
      }
      else {
         return null;
      }
   }
   
   /**
    * SAX implementation of {@link DefaultHandler} used to parse XML content
    * created via {@link SEDXMLWriter}.
    * @author Martin Hentschel
    */
   private class SEDSAXHandler extends DefaultHandler {
      /**
       * The found {@link ISEDDebugTarget}s.
       */
      private List<ISEDDebugTarget> result = new LinkedList<ISEDDebugTarget>();
      
      /**
       * The current {@link SEDMemoryDebugTarget}.
       */
      private SEDMemoryDebugTarget target;
      
      /**
       * The current {@link SEDMemoryThread}.
       */
      private SEDMemoryThread thread;
      
      /**
       * The parent hierarchy filled by {@link #startElement(String, String, String, Attributes)}
       * and emptied by {@link #endElement(String, String, String)}.
       */
      private Deque<ISEDMemoryDebugNode> parentStack = new LinkedList<ISEDMemoryDebugNode>();

      /**
       * {@inheritDoc}
       */
      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         ISEDMemoryDebugNode parent = parentStack.peekFirst();
         Object obj = createElement(target, parent != null ? parent : thread, thread, uri, localName, qName, attributes);
         if (obj instanceof SEDMemoryDebugTarget) {
            target = (SEDMemoryDebugTarget)obj;
            result.add(target);
         }
         else if (obj instanceof SEDMemoryThread) {
            thread = (SEDMemoryThread)obj;
            if (target != null) {
               target.addSymbolicThread(thread);
            }
            else {
               throw new SAXException("Model is in inconsistent state.");
            }
         }
         else if (obj instanceof ISEDMemoryDebugNode) {
            ISEDMemoryDebugNode child = (ISEDMemoryDebugNode)obj; 
            parentStack.addFirst(child);
            if (parent != null) {
               parent.addChild(child);
            }
            else if (thread != null) {
               thread.addChild(child);
            }
            else {
               throw new SAXException("Model is in inconsistent state.");
            }
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
         if (!parentStack.isEmpty()) {
            parentStack.removeFirst();
         }
         else if (thread != null) {
            thread = null;
         }
         else if (target != null) {
            target = null;
         }
         else if (SEDXMLWriter.TAG_LAUNCH.equals(qName)) {
            // Nothing to do, but still valid.
         }
         else {
            throw new SAXException("Model is in inconsistent state.");
         }
      }

      /**
       * Returns the found {@link ISEDDebugTarget}s.
       * @return The found {@link ISEDDebugTarget}s.
       */
      public List<ISEDDebugTarget> getResult() {
         return result;
      }
   }
   
   /**
    * Creates an {@link Object} for the element defined by the given tag.
    * @param target The parent {@link ISEDDebugTarget} or {@code null} if not available.
    * @param parent The parent {@link ISEDDebugNode} or {@code null} if not available.
    * @param thread The parent {@link ISEDThread} or {@code null} if not available.
    * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @return The created {@link Object}.
    * @throws SAXException Occurred Exception.
    */
   protected Object createElement(ISEDDebugTarget target, ISEDDebugNode parent, ISEDThread thread, String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (SEDXMLWriter.TAG_LAUNCH.equals(qName)) {
         return null; // Nothing to do
      }
      else if (SEDXMLWriter.TAG_DEBUG_TARGET.equals(qName)) {
         return createDebugTarget(uri, localName, qName, attributes);
      }
      else if (SEDXMLWriter.TAG_BRANCH_CONDITION.equals(qName)) {
         return createBranchCondition(target, parent, uri, localName, qName, attributes);
      }
      else if (SEDXMLWriter.TAG_BRANCH_NODE.equals(qName)) {
         return createBranchNode(target, parent, thread, uri, localName, qName, attributes);
      }
      else if (SEDXMLWriter.TAG_EXCEPTIONAL_TERMINATION.equals(qName)) {
         return createExceptionalTermination(target, parent, uri, localName, qName, attributes);
      }
      else if (SEDXMLWriter.TAG_METHOD_CALL.equals(qName)) {
         return createMethodCall(target, parent, thread, uri, localName, qName, attributes);
      }
      else if (SEDXMLWriter.TAG_METHOD_RETURN.equals(qName)) {
         return createMethodReturn(target, parent, thread, uri, localName, qName, attributes);
      }
      else if (SEDXMLWriter.TAG_STATEMENT.equals(qName)) {
         return createStatement(target, parent, thread, uri, localName, qName, attributes);
      }
      else if (SEDXMLWriter.TAG_TERMINATION.equals(qName)) {
         return createTermination(target, parent, uri, localName, qName, attributes);
      }
      else if (SEDXMLWriter.TAG_THREAD.equals(qName)) {
         return createThread(target, uri, localName, qName, attributes);
      }
      else {
         throw new SAXException("Unknown tag \"" + qName + "\".");
      }
   }
   
   /**
    * Creates a {@link SEDMemoryBranchCondition} instance for the content in the given tag.
    * @param target The parent {@link ISEDDebugTarget} or {@code null} if not available.
    * @param parent The parent {@link ISEDDebugNode} or {@code null} if not available.
    * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @return The created {@link SEDMemoryBranchCondition}.
    */
   protected SEDMemoryBranchCondition createBranchCondition(ISEDDebugTarget target, ISEDDebugNode parent, String uri, String localName, String qName, Attributes attributes) {
      SEDMemoryBranchCondition termination = new SEDMemoryBranchCondition(target, parent);
      fillDebugNode(termination, attributes);
      return termination;
   }
   
   /**
    * Creates a {@link SEDMemoryDebugTarget} instance for the content in the given tag.
    * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @return The created {@link SEDMemoryDebugTarget}.
    */
   protected SEDMemoryDebugTarget createDebugTarget(String uri, String localName, String qName, Attributes attributes) {
      SEDMemoryDebugTarget target = new SEDMemoryDebugTarget(null);
      target.setName(getName(attributes));
      target.setModelIdentifier(getModelIdentifier(attributes));
      return target;
   }

   /**
    * Creates a {@link SEDMemoryBranchNode} instance for the content in the given tag.
    * @param target The parent {@link ISEDDebugTarget} or {@code null} if not available.
    * @param parent The parent {@link ISEDDebugNode} or {@code null} if not available.
    * @param thread The parent {@link ISEDThread} or {@code null} if not available.
    * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @return The created {@link SEDMemoryBranchNode}.
    * @throws SAXException Occurred Exception.
    */   
   protected SEDMemoryBranchNode createBranchNode(ISEDDebugTarget target, ISEDDebugNode parent, ISEDThread thread, String uri, String localName, String qName, Attributes attributes) throws SAXException {
      SEDMemoryBranchNode methodReturn = new SEDMemoryBranchNode(target, parent, thread);
      fillDebugNode(methodReturn, attributes);
      fillStackFrame(methodReturn, attributes);
      return methodReturn;
   }
   
   /**
    * Creates a {@link SEDMemoryExceptionalTermination} instance for the content in the given tag.
    * @param target The parent {@link ISEDDebugTarget} or {@code null} if not available.
    * @param parent The parent {@link ISEDDebugNode} or {@code null} if not available.
    * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @return The created {@link SEDMemoryExceptionalTermination}.
    */   
   protected SEDMemoryExceptionalTermination createExceptionalTermination(ISEDDebugTarget target, ISEDDebugNode parent, String uri, String localName, String qName, Attributes attributes) {
      SEDMemoryExceptionalTermination termination = new SEDMemoryExceptionalTermination(target, parent);
      fillDebugNode(termination, attributes);
      return termination;
   }
   
   /**
    * Creates a {@link SEDMemoryMethodCall} instance for the content in the given tag.
    * @param target The parent {@link ISEDDebugTarget} or {@code null} if not available.
    * @param parent The parent {@link ISEDDebugNode} or {@code null} if not available.
    * @param thread The parent {@link ISEDThread} or {@code null} if not available.
    * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @return The created {@link SEDMemoryMethodCall}.
    * @throws SAXException Occurred Exception.
    */   
   protected SEDMemoryMethodCall createMethodCall(ISEDDebugTarget target, ISEDDebugNode parent, ISEDThread thread, String uri, String localName, String qName, Attributes attributes) throws SAXException {
      SEDMemoryMethodCall methodReturn = new SEDMemoryMethodCall(target, parent, thread);
      fillDebugNode(methodReturn, attributes);
      fillStackFrame(methodReturn, attributes);
      return methodReturn;
   }
   
   /**
    * Creates a {@link SEDMemoryMethodReturn} instance for the content in the given tag.
    * @param target The parent {@link ISEDDebugTarget} or {@code null} if not available.
    * @param parent The parent {@link ISEDDebugNode} or {@code null} if not available.
    * @param thread The parent {@link ISEDThread} or {@code null} if not available.
    * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @return The created {@link SEDMemoryMethodReturn}.
    * @throws SAXException Occurred Exception.
    */   
   protected SEDMemoryMethodReturn createMethodReturn(ISEDDebugTarget target, ISEDDebugNode parent, ISEDThread thread, String uri, String localName, String qName, Attributes attributes) throws SAXException {
      SEDMemoryMethodReturn methodReturn = new SEDMemoryMethodReturn(target, parent, thread);
      fillDebugNode(methodReturn, attributes);
      fillStackFrame(methodReturn, attributes);
      return methodReturn;
   }
   
   /**
    * Creates a {@link SEDMemoryStatement} instance for the content in the given tag.
    * @param target The parent {@link ISEDDebugTarget} or {@code null} if not available.
    * @param parent The parent {@link ISEDDebugNode} or {@code null} if not available.
    * @param thread The parent {@link ISEDThread} or {@code null} if not available.
    * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @return The created {@link SEDMemoryStatement}.
    * @throws SAXException Occurred Exception.
    */   
   protected SEDMemoryStatement createStatement(ISEDDebugTarget target, ISEDDebugNode parent, ISEDThread thread, String uri, String localName, String qName, Attributes attributes) throws SAXException {
      SEDMemoryStatement statement = new SEDMemoryStatement(target, parent, thread);
      fillDebugNode(statement, attributes);
      fillStackFrame(statement, attributes);
      return statement;
   }
   
   /**
    * Creates a {@link SEDMemoryTermination} instance for the content in the given tag.
    * @param target The parent {@link ISEDDebugTarget} or {@code null} if not available.
    * @param parent The parent {@link ISEDDebugNode} or {@code null} if not available.
    * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @return The created {@link SEDMemoryTermination}.
    */   
   protected SEDMemoryTermination createTermination(ISEDDebugTarget target, ISEDDebugNode parent, String uri, String localName, String qName, Attributes attributes) {
      SEDMemoryTermination termination = new SEDMemoryTermination(target, parent);
      fillDebugNode(termination, attributes);
      return termination;
   }
   
   /**
    * Creates a {@link SEDMemoryThread} instance for the content in the given tag.
    * @param target The parent {@link ISEDDebugTarget} or {@code null} if not available.
    * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @return The created {@link SEDMemoryThread}.
    */   
   protected SEDMemoryThread createThread(ISEDDebugTarget target, String uri, String localName, String qName, Attributes attributes) {
      SEDMemoryThread thread = new SEDMemoryThread(target);
      fillDebugNode(thread, attributes);
      return thread;
   }
   
   /**
    * Fills the attributes of the given {@link ISEDMemoryDebugNode}.
    * @param node The {@link ISEDMemoryDebugNode} to fill.
    * @param attributes The {@link Attributes} which provides the content.
    */
   protected void fillDebugNode(ISEDMemoryDebugNode node, Attributes attributes) {
      node.setName(getName(attributes));
   }

   /**
    * Fills the attributes of the given {@link ISEDMemoryStackFrameCompatibleDebugNode}.
    * @param node The {@link ISEDMemoryStackFrameCompatibleDebugNode} to fill.
    * @param attributes The {@link Attributes} which provides the content.
    * @throws SAXException Occurred Exception.
    */
   protected void fillStackFrame(ISEDMemoryStackFrameCompatibleDebugNode node, Attributes attributes) throws SAXException {
      node.setLineNumber(getLineNumber(attributes));
      node.setCharStart(getCharStart(attributes));
      node.setCharEnd(getCharEnd(attributes));
   }
   
   /**
    * Returns the name value.
    * @param attributes The {@link Attributes} which provides the content.
    * @return The value.
    */
   protected String getName(Attributes attributes) {
      return attributes.getValue(SEDXMLWriter.ATTRIBUTE_NAME);
   }
   
   /**
    * Returns the model identifier value.
    * @param attributes The {@link Attributes} which provides the content.
    * @return The value.
    */
   protected String getModelIdentifier(Attributes attributes) {
      return attributes.getValue(SEDXMLWriter.ATTRIBUTE_MODEL_IDENTIFIER);
   }
   
   /**
    * Returns the line number value.
    * @param attributes The {@link Attributes} which provides the content.
    * @return The value.
    * @throws SAXException Occurred Exception.
    */
   protected int getLineNumber(Attributes attributes) throws SAXException {
      try {
         return Integer.parseInt(attributes.getValue(SEDXMLWriter.ATTRIBUTE_LINE_NUMBER));
      }
      catch (NumberFormatException e) {
         throw new SAXException(e);
      }
   }
   
   /**
    * Returns the char start value.
    * @param attributes The {@link Attributes} which provides the content.
    * @return The value.
    * @throws SAXException Occurred Exception.
    */
   protected int getCharStart(Attributes attributes) throws SAXException {
      try {
         return Integer.parseInt(attributes.getValue(SEDXMLWriter.ATTRIBUTE_CHAR_START));
      }
      catch (NumberFormatException e) {
         throw new SAXException(e);
      }
   }
   
   /**
    * Returns the char end value.
    * @param attributes The {@link Attributes} which provides the content.
    * @return The value.
    * @throws SAXException Occurred Exception.
    */
   protected int getCharEnd(Attributes attributes) throws SAXException {
      try {
         return Integer.parseInt(attributes.getValue(SEDXMLWriter.ATTRIBUTE_CHAR_END));
      }
      catch (NumberFormatException e) {
         throw new SAXException(e);
      }
   }
}