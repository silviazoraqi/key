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
package de.uka.ilkd.key.strategy.termfeature;

import de.uka.ilkd.key.ldt.HeapLDT;
import de.uka.ilkd.key.logic.Term;


public final class BaseHeapTermFeature extends BinaryTermFeature {

    private final HeapLDT heapLDT;

    private BaseHeapTermFeature(HeapLDT heapLDT) {
        this.heapLDT = heapLDT;
    }

    public static TermFeature create(HeapLDT heapLDT) {
        return new BaseHeapTermFeature(heapLDT);
    }
    
    @Override
    protected boolean filter (Term t) {
        return  t.op() == heapLDT.getHeap();
    }

}