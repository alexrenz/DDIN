package de.uni_mannheim.desq.examples;

import de.uni_mannheim.desq.mining.OutputLabel;
import de.uni_mannheim.desq.mining.OutputNFA;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Created by alex on 10.02.17.
 */
public class MergeTesting {
    public static void main(String[] args) {
        IntArrayList a = new IntArrayList();
        a.add(1);
        IntArrayList b = new IntArrayList();
        b.add(2);
        IntArrayList c = new IntArrayList();
        c.add(3);
        IntArrayList d = new IntArrayList();
        d.add(4);
        IntArrayList e = new IntArrayList();
        e.add(5);
        IntArrayList f = new IntArrayList();
        f.add(6);
        IntArrayList g = new IntArrayList();
        g.add(7);
        IntArrayList h = new IntArrayList();
        h.add(8);

        OutputLabel la = new OutputLabel(null, 1, a);
        OutputLabel lb = new OutputLabel(null, 1, b);
        OutputLabel lc = new OutputLabel(null, 1, c);
        OutputLabel ld = new OutputLabel(null, 1, d);
        OutputLabel le = new OutputLabel(null, 1, e);
        OutputLabel lf = new OutputLabel(null, 1, f);
        OutputLabel lg = new OutputLabel(null, 1, g);
        OutputLabel lh = new OutputLabel(null, 1, h);

        OutputNFA nfa = new OutputNFA(50, null);


        OutputLabel[] path1 = {la, ld, le, lh};
        OutputLabel[] path2 = {la, ld, lf, lg, lh};
        OutputLabel[] path3 = {lb, lc, ld, le, lh};
        OutputLabel[] path4 = {lb, lc, ld, lf, lg, lh};

        nfa.addPathAndReturnNextPivot(path1);
        nfa.addPathAndReturnNextPivot(path2);
        nfa.addPathAndReturnNextPivot(path3);
        nfa.addPathAndReturnNextPivot(path4);

        nfa.exportGraphViz("nfa-tree.pdf");
        nfa.mergeSuffixes();
        nfa.exportGraphViz("nfa-suffixes-merged.pdf");
    }
}
