package de.uni_mannheim.desq.fst;

import de.uni_mannheim.desq.dictionary.BasicDictionary;
import de.uni_mannheim.desq.dictionary.Dictionary;
import de.uni_mannheim.desq.util.CollectionUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.*;

/**
 * Created by rgemulla on 02.12.2016.
 */
public final class EagerDfaState extends DfaState {

    public EagerDfaState(Dfa dfa, BitSet fstStates) {
        super(dfa, fstStates);
        reachableDfaStates.add(null); // position 0 is default transition
    }

    public DfaState consume(int itemFid) {
        return reachableDfaStates.get(indexByFid.getInt(itemFid));
    }

    // -- Eager DFA construction --------------------------------------------------------------------------------------

    @Override
    void construct() {
        dfa.initial = this;
        dfa.states.clear();
        dfa.stateByTransitions.clear();
        Fst fst = dfa.fst;
        BasicDictionary dict = dfa.dict;
        boolean processFinalCompleteStates = dfa.processFinalCompleteStates;

        // GLOBAL DATA STRUCTURES
        // unprocessed dfa states
        Stack<BitSet> unprocessedStates = new Stack<>();

        // map from transition label (e.g., "(.^)") to items that fire (used as cache)
        Map<String, IntList> firedItemsByLabel = new HashMap<>();

        // DATA STRUCTURES FOR CURRENTLY PROCESSED DFA STATE
        // fst states reachable by all items
        // used to represent all outgoing FST transitions where Transition#firesAll is true
        BitSet defaultTransition = new BitSet(dfa.fst.numStates());

        // for all items, which of the outgoing transitions fire (excluding fires-all transitions)
        BitSet activeFids = new BitSet(dict.lastFid() + 1); // indexed by item
        BitSet[] firedTransitionsByFid = new BitSet[dict.lastFid() + 1]; // indexed by item
        for (int i = 0; i < firedTransitionsByFid.length; i++) {
            firedTransitionsByFid[i] = new BitSet();
        }

        // MAIN LOOP: while there is an unprocessed state, compute all its transitions
        // starting with the initial state (i.e., this state)
        dfa.states.put(fstStates, this);
        unprocessedStates.push(fstStates);
        while (!unprocessedStates.isEmpty()) {
            // get next state to process
            BitSet fromStates = unprocessedStates.pop();
            EagerDfaState fromDfaState = (EagerDfaState) dfa.states.get(fromStates);

            // System.out.println("Processing " + fromStates.toString());

            // if the state is final complete and the option to not process those state is set, we do compute
            // the outgoing transitions of this state
            if (!processFinalCompleteStates && fromDfaState.isFinalComplete()) {
                continue;
            }

            // collect all transitions with distinct labels and compute target FST states for each
            fromDfaState.collectTransitions(defaultTransition, (short)0, firedItemsByLabel);

            // now set the default transition in case there were transitions that fire on all items
            if (!defaultTransition.isEmpty()) {
                fromDfaState.reachableDfaStates.set(0, getDfaState(defaultTransition, unprocessedStates));
            }

            // if there are no other transitions, we are done with this DFA state
            if (fromDfaState.transitionLabels == null)
                continue; // no non-default transitions

            // index the transitions to the DFA state
            String key = fromDfaState.transitionLabels != null ? String.join(" ", fromDfaState.transitionLabels) : "null";
            if (!dfa.stateByTransitions.containsKey(key)) {
                // we haven't seen this combination of transitions -> compute everything from scratch
                fromDfaState.indexTransitions(activeFids, firedTransitionsByFid, firedItemsByLabel,
                        defaultTransition, unprocessedStates);

                // cache the just created DFA state to reuse indexByFid later on if possible
                dfa.stateByTransitions.put(key, fromDfaState);
            } else {
                // reuse transition index from a previously processed state with the same outgoing FST transisions
                EagerDfaState similarState = (EagerDfaState)dfa.stateByTransitions.get(key);
                fromDfaState.indexTransitions(similarState, defaultTransition, unprocessedStates);
            }
        }
    }

    /** Returns the DFA state for the given set of FST states. If this DFA state has not been created, creates it,
     * adds it to the DFA, and adds <code>fstStates</code> to <code>unprocessedStates</code>. */
    private EagerDfaState getDfaState(BitSet fstStates, List<BitSet> unprocessedStates) {
        EagerDfaState dfaState = (EagerDfaState) dfa.states.get(fstStates);
        if (dfaState == null) {
            fstStates = CollectionUtils.copyOf(fstStates); // store our own copy
            dfaState = new EagerDfaState(dfa, fstStates);
            dfa.states.put(fstStates, dfaState);
            unprocessedStates.add(fstStates);
        }
        return dfaState;
    }

    /** Index transitions from scratch. Computes {@link #reachableDfaStates}, {@link #indexByFid},
     *  and {@link #indexByFiredTransitions}. */
    private void indexTransitions(BitSet activeFids, BitSet[] firedTransitionsByFid, Map<String, IntList> firedItemsByLabel,
                                  BitSet defaultTransition, List<BitSet> unprocessedStates) {
        // we first compute which transitions fire per item
        activeFids.clear();
        for (int t = 0; t < transitionLabels.length; t++) { // iterate over transitions
            String transitionLabel = transitionLabels[t];
            IntList firedItems = firedItemsByLabel.get(transitionLabel); // computed in collectTransitions
            for (int i = 0; i < firedItems.size(); i++) {
                int fid = firedItems.get(i);
                if (!activeFids.get(fid)) {
                    // activate and initialize fid if not yet seen
                    activeFids.set(fid);
                    firedTransitionsByFid[fid].clear();
                    firedTransitionsByFid[fid].set(t);
                }

                // add the states we can reach with this fid
                firedTransitionsByFid[fid].set(t);
            }
        }

        // now iterate over the items and add transitions to the DFA
        for (int fid = activeFids.nextSetBit(0);
             fid >= 0;
             fid = activeFids.nextSetBit(fid + 1)) {

            // get the position of the corresponding next state in EagerDfaState#reachableDfaStates
            BitSet firedTransitions = firedTransitionsByFid[fid];
            int index = indexByFiredTransitions.getInt(firedTransitions);
            if (index == 0) { // not present
                // compute subsequent state
                BitSet toStates = new BitSet();
                toStates.or(defaultTransition); // always fires
                for (int t = firedTransitions.nextSetBit(0);
                     t >= 0;
                     t = firedTransitions.nextSetBit(t + 1)) {
                    toStates.or(toStatesByLabel[t]);
                }

                // get the corresponding DFA state
                EagerDfaState toDfaState = getDfaState(toStates, unprocessedStates);

                // add the state as a successor state to the DFA
                reachableDfaStates.add(toDfaState);
                index = reachableDfaStates.size() - 1;
                firedTransitionsByIndex.add(CollectionUtils.copyOf(firedTransitions));
                indexByFiredTransitions.put(firedTransitionsByIndex.get(index), index);
            }

            // add the transition
            indexByFid.set(fid, index);
        }
    }

    /** Index transitions by reusing data structures from another DFA state with the same distinct outgoing
     * transition labels. Computes {@link #reachableDfaStates} and reuses {@link #indexByFid}
     * and {@link #indexByFiredTransitions}. */
    private void indexTransitions(EagerDfaState similarState, BitSet defaultTransition, List<BitSet> unprocessedStates) {
        indexByFid = similarState.indexByFid;
        indexByFiredTransitions = similarState.indexByFiredTransitions;
        firedTransitionsByIndex = similarState.firedTransitionsByIndex;
        reachableDfaStates.addAll(Collections.nCopies( similarState.reachableDfaStates.size() - 1, null)); // resize to correct size

        // iterate over active combinations of fired transitions and set the corresponding toStates
        for (Object2IntMap.Entry<BitSet> entry : indexByFiredTransitions.object2IntEntrySet()) {
            BitSet firedTransitions = entry.getKey();
            int index = entry.getIntValue();

            // compute subsequent state for this combination of fired transitions
            BitSet toStates = new BitSet();
            toStates.or(defaultTransition); // always fires
            for (int t = firedTransitions.nextSetBit(0);
                 t >= 0;
                 t = firedTransitions.nextSetBit(t + 1)) {
                toStates.or(toStatesByLabel[t]);
            }

            // get the corresponding FST state
            EagerDfaState toDfaState = getDfaState(toStates, unprocessedStates);

            // and put it to the corresponding positon
            reachableDfaStates.set(index, toDfaState);
        }
    }
}
