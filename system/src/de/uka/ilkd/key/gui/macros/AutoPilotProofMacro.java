package de.uka.ilkd.key.gui.macros;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.gui.KeYMediator;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.SequentFormula;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.ObserverFunction;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.UpdateApplication;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.rule.OneStepSimplifier;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.RuleSet;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.strategy.LongRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCostCollector;
import de.uka.ilkd.key.strategy.Strategy;
import de.uka.ilkd.key.strategy.TopRuleAppCost;

public class AutoPilotProofMacro extends StrategyProofMacro {

    private static final String[] ADMITTED_RULES = {
        "orRight", "impRight", "notRight", "close",
        "andRight"
    };

    private static final Set<String> ADMITTED_RULES_SET = asSet(ADMITTED_RULES);

    private static final Name NON_HUMAN_INTERACTION_RULESET = new Name("to_be_done");


    @Override
    public String getName() {
        return "Auto pilot";
    }

    @Override
    public String getDescription() {
        return "tbd";
    }


    /*
     * convert a string array to a set of strings
     */
    protected static Set<String> asSet(String[] strings) {
        return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(strings)));
    }

    /*
     * find a modality term in a node
     */
    private static boolean hasModality(Node node) {
        Sequent sequent = node.sequent();
        for (SequentFormula sequentFormula : sequent) {
            if(hasModality(sequentFormula.formula())) {
                return true;
            }
        }

        return false;
    }

    /*
     * recursively descent into the term to detect a modality.
     */
    private static boolean hasModality(Term term) {
        if(term.op() instanceof Modality) {
            return true;
        }

        for (Term sub : term.subs()) {
            if(hasModality(sub)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isNonHumanInteractionTagged(Rule rule, Services services) {
        if (rule instanceof Taclet) {
            Taclet taclet = (Taclet) rule;
            ImmutableList<RuleSet> ruleSets = taclet.getRuleSets();
            RuleSet interactionRuleSet = (RuleSet)services.getNamespaces().ruleSets().
                    lookup(NON_HUMAN_INTERACTION_RULESET);
            return ruleSets.contains(interactionRuleSet);
        }
        return false;
    }

    private static class AutoPilotStrategy implements Strategy {

        private static final Name NAME = new Name("Autopilot filter strategy");
        private final KeYMediator mediator;
        private final PosInOccurrence posInOcc;
        private final Strategy delegate;

        public AutoPilotStrategy(KeYMediator mediator, PosInOccurrence posInOcc) {
            this.mediator = mediator;
            this.posInOcc = posInOcc;
            this.delegate = mediator.getInteractiveProver().getProof().getActiveStrategy();
        }

        @Override
        public Name name() {
            return NAME;
        }

        @Override
        public boolean isApprovedApp(RuleApp app, PosInOccurrence pio, Goal goal) {
            // TODO it this sensible?
            return true;
        }

        @Override
        public RuleAppCost computeCost(RuleApp app, PosInOccurrence pio, Goal goal) {

            Rule rule = app.rule();
            if(isNonHumanInteractionTagged(rule, goal.proof().getServices())) {
                return TopRuleAppCost.INSTANCE;
            }

            if(hasModality(goal.node())) {
                return delegate.computeCost(app, pio, goal);
            }

            String name = rule.name().toString();
            if(ADMITTED_RULES_SET.contains(name)) {
                return LongRuleAppCost.ZERO_COST;
            }

            if(name.startsWith("Class_invariant_axiom_for")) {
                return LongRuleAppCost.ZERO_COST;
            }

            // apply OSS to <inv>() calls.
            if(rule == OneStepSimplifier.INSTANCE) {
                Term target = pio.subTerm();
                if(target.op() instanceof UpdateApplication) {
                    Operator updatedOp = target.sub(1).op();
                    if(updatedOp instanceof ObserverFunction) {
                        return LongRuleAppCost.ZERO_COST;
                    }
                }
            }

            return TopRuleAppCost.INSTANCE;
        }

        @Override
        public void instantiateApp(RuleApp app, PosInOccurrence pio, Goal goal,
                RuleAppCostCollector collector) {
            delegate.instantiateApp(app, pio, goal, collector);
        }

    }

    @Override
    protected Strategy createStrategy(KeYMediator mediator, PosInOccurrence posInOcc) {
        return new AutoPilotStrategy(mediator, posInOcc);
    }
}