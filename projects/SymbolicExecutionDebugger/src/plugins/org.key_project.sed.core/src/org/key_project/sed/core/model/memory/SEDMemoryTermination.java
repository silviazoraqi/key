package org.key_project.sed.core.model.memory;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.key_project.sed.core.model.ISEDDebugNode;
import org.key_project.sed.core.model.ISEDDebugTarget;
import org.key_project.sed.core.model.ISEDTermination;
import org.key_project.sed.core.model.impl.AbstractSEDTermination;

/**
 * Implementation of {@link ISEDTermination} that stores all
 * information in the memory.
 * @author Martin Hentschel
 */
public class SEDMemoryTermination extends AbstractSEDTermination implements ISEDMemoryDebugNode {
   /**
    * The contained child nodes.
    */
   private List<ISEDDebugNode> children = new LinkedList<ISEDDebugNode>();
   
   /**
    * Constructor.
    * @param target The {@link ISEDDebugTarget} in that this termination is contained.
    * @param parent The parent in that this node is contained as child.
    */
   public SEDMemoryTermination(ISEDDebugTarget target, 
                               ISEDDebugNode parent) {
      super(target, parent);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ISEDDebugNode[] getChildren() throws DebugException {
      return children.toArray(new ISEDDebugNode[children.size()]);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void addChild(ISEDDebugNode child) {
      if (child != null) {
         children.add(child);
      }
   }
   
   /**
    * <p>
    * {@inheritDoc}
    * </p>
    * <p>
    * Changed visibility to public.
    * </p>
    */
   @Override
   public void setName(String name) {
      super.setName(name);
   }
}