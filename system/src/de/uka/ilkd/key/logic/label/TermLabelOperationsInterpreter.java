// This file is part of KeY - Integrated Deductive Software Design 
//
// Copyright (C) 2001-2011 Universitaet Karlsruhe (TH), Germany 
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
// Copyright (C) 2011-2013 Karlsruhe Institute of Technology, Germany 
//                         Technical University Darmstadt, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General 
// Public License. See LICENSE.TXT for details.
//

package de.uka.ilkd.key.logic.label;

import java.util.HashSet;
import java.util.LinkedHashSet;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.logic.ITermLabel;
import de.uka.ilkd.key.logic.Term;

public class TermLabelOperationsInterpreter {

   public static ImmutableArray<ITermLabel> intersection(
           ImmutableArray<ITermLabel> left, ImmutableArray<ITermLabel> right) {
       final HashSet<ITermLabel> set = new LinkedHashSet<ITermLabel>();
       for (ITermLabel r : right) {
           if (left.contains(r)) {
              set.add(r);
           }
       }
       return new ImmutableArray<ITermLabel>(set.toArray(new ITermLabel[set.size()]));
   }
   
    public static ImmutableArray<ITermLabel> union(
            ImmutableArray<ITermLabel> left, ImmutableArray<ITermLabel> right) {
        final HashSet<ITermLabel> set = new LinkedHashSet<ITermLabel>();
        for (ITermLabel l : left) { 
            set.add(l);
        }
        for (ITermLabel l : right) { 
            set.add(l);
        }
        return new ImmutableArray<ITermLabel>(set.toArray(new ITermLabel[set.size()]));
    }

    public static ImmutableArray<ITermLabel> sub(
            ImmutableArray<ITermLabel> left, ImmutableArray<ITermLabel> right) {
        final HashSet<ITermLabel> set = new LinkedHashSet<ITermLabel>();
        for (ITermLabel l : left) { 
            set.add(l);
        }
        for (ITermLabel l : right) { 
            set.remove(l);
        }
        return new ImmutableArray<ITermLabel>(set.toArray(new ITermLabel[set.size()]));
    }

    /**
     * resolves term redundancy (used by Sequent to avoid duplicate formulas).
     * In case of t2 being unlabeled a list with t1 as single element is 
     * returned. Otherwise if t1 is unlabeled (and t2 labeled), then a list with t2 as single element 
     * is returned. If both terms are labeled a list of terms is returned which is not redundant. 
     * 
     * The terms t1 and t2 may only differ in their attached labels. Otherwise they have to be
     * structural identical 
     * 
     * This method should be used in case to implement more complex redundancy checks.
     * @param t1 the Term t1 which is structural equivalent to t2 (except maybe labels)
     * @param t2 the Term t2
     * @return a list which represents a redundancy free result of merging labels in t1 and t2
     */
    public static ImmutableList<Term> resolveRedundancy(Term t1, Term t2) {
        ImmutableList<Term> result = ImmutableSLList.<Term>nil();
        if (!t2.hasLabels()) {
            return result.prepend(t1);
        } else if (!t1.hasLabels()) {
            return result.prepend(t2);
        }
        
        for (int i = 0; i<t1.arity(); i++) {
            if (!t1.sub(i).equals(t2.sub(i))) {
                
            }
        }
        
        return null;
    }
    
}