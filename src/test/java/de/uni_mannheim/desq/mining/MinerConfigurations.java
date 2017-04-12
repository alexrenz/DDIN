package de.uni_mannheim.desq.mining;

import de.uni_mannheim.desq.util.DesqProperties;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/** Helper methods to creates configurations for (all) miners. Used for testing. */
public class MinerConfigurations {
    public static Pair<String, DesqProperties> prefixGrowth(long sigma, int gamma, int lambda, boolean generalize) {
        String minerName = "PrefixGrowth";
        DesqProperties conf = PrefixGrowthMiner.createConf(sigma, gamma, lambda, generalize);
        return Pair.of(minerName, conf);
    }

    public static Pair<String, DesqProperties> cSpade(long sigma, int gamma, int lambda, boolean generalize) {
        String minerName = "CSpade";
        DesqProperties conf = CSpadeMiner.createConf(sigma, gamma, lambda, generalize);
        return Pair.of(minerName, conf);
    }

    public static Pair<String, DesqProperties> desqCount(long sigma, String patternExpression, boolean useFlist,
                                                        boolean pruneIrrelevantInputs, boolean useLazyDfa, boolean useTwoPass) {
        DesqProperties conf = DesqCount.createConf(patternExpression, sigma);
        conf.setProperty("desq.mining.use.flist", useFlist);
        conf.setProperty("desq.mining.prune.irrelevant.inputs", pruneIrrelevantInputs);
        conf.setProperty("desq.mining.use.lazy.dfa", useLazyDfa);
        conf.setProperty("desq.mining.use.two.pass", useTwoPass);
        String minerName = "DesqCount-" + toLetter(useFlist) + toLetter(pruneIrrelevantInputs)
                + toLetter(useLazyDfa) + toLetter(useTwoPass);
        return Pair.of(minerName, conf);
    }

    public static Pair<String, DesqProperties> desqCount(long sigma, int gamma, int lambda, boolean generalize, boolean useFlist,
                                          boolean pruneIrrelevantInputs, boolean useLazyDfa, boolean useTwoPass) {
        String patternExpression = DesqMiner.patternExpressionFor(gamma, lambda, generalize);
        return desqCount(sigma, patternExpression, useFlist, pruneIrrelevantInputs, useLazyDfa, useTwoPass);
    }

    public static Pair<String, DesqProperties> desqDfs(long sigma, String patternExpression,
                                                      boolean pruneIrrelevantInputs, boolean useLazyDfa, boolean useTwoPass) {
        DesqProperties conf = DesqDfs.createConf(patternExpression, sigma);
        conf.setProperty("desq.mining.prune.irrelevant.inputs", pruneIrrelevantInputs);
        conf.setProperty("desq.mining.use.lazy.dfa", useLazyDfa);
        conf.setProperty("desq.mining.use.two.pass", useTwoPass);
        String minerName = "DesqDfs-" + toLetter(pruneIrrelevantInputs) + toLetter(useLazyDfa) + toLetter(useTwoPass);
        return Pair.of(minerName, conf);
    }

    public static Pair<String, DesqProperties> desqDfs(long sigma, int gamma, int lambda, boolean generalize,
                                          boolean pruneIrrelevantInputs, boolean useLazyDfa, boolean useTwoPass) {
        String patternExpression = DesqMiner.patternExpressionFor(gamma, lambda, generalize);
        return desqDfs(sigma, patternExpression, pruneIrrelevantInputs, useLazyDfa, useTwoPass);
    }

    public static List<Pair<String, DesqProperties>> all(long sigma, int gamma, int lambda, boolean generalize) {
        List<Pair<String, DesqProperties>> allMiners = new ArrayList<>();
        allMiners.add(prefixGrowth(sigma, gamma, lambda, generalize));
        allMiners.add(cSpade(sigma, gamma, lambda, generalize));
        String patternExpression = DesqMiner.patternExpressionFor(gamma, lambda, generalize);
        allMiners.addAll(all(sigma, patternExpression));
        return allMiners;
    }

    public static List<Pair<String, DesqProperties>> all(long sigma, String patternExpression) {
        List<Pair<String, DesqProperties>> allMiners = new ArrayList<>();
        allMiners.add(desqCount(sigma, patternExpression, false, false, false, false));
        allMiners.add(desqCount(sigma, patternExpression, false, true, false, false));
        allMiners.add(desqCount(sigma, patternExpression, false, true, true, false));
        allMiners.add(desqCount(sigma, patternExpression, false, true, false, true));
        allMiners.add(desqCount(sigma, patternExpression, false, true, true, true));
        allMiners.add(desqCount(sigma, patternExpression, true, false, false, false));
        allMiners.add(desqCount(sigma, patternExpression, true, true, false, false));
        allMiners.add(desqCount(sigma, patternExpression, true, true, true, false));
        allMiners.add(desqCount(sigma, patternExpression, true, true, false, true));
        allMiners.add(desqCount(sigma, patternExpression, true, true, true, true));


        allMiners.add(desqDfs(sigma, patternExpression, false, false, false));
        allMiners.add(desqDfs(sigma, patternExpression, true, false, false));
        allMiners.add(desqDfs(sigma, patternExpression, true, true, false));
        allMiners.add(desqDfs(sigma, patternExpression, true, false, true));
        allMiners.add(desqDfs(sigma, patternExpression, true, true, true));
        return allMiners;
    }

    private static String toLetter(boolean b) {
        return b ? "t" : "f";
    }
}
