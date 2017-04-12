package de.uni_mannheim.desq.mining.spark

import java.util

import de.uni_mannheim.desq.util.DesqProperties

/** Helper methods to creates configurations for (all) miners. Used for testing. */
object MinerConfigurations {
    def desqCount(sigma: Long, patternExpression: String, useFlist: Boolean,
                  pruneIrrelevantInputs: Boolean, useLazyDfa: Boolean, useTwoPass: Boolean): (String, DesqProperties) = {
        val conf = DesqCount.createConf(patternExpression, sigma)
        conf.setProperty("desq.mining.use.flist", useFlist)
        conf.setProperty("desq.mining.prune.irrelevant.inputs", pruneIrrelevantInputs)
        conf.setProperty("desq.mining.use.lazy.dfa", useLazyDfa)
        conf.setProperty("desq.mining.use.two.pass", useTwoPass)
        val minerName = "DesqCount-" + toLetter(useFlist) + toLetter(pruneIrrelevantInputs) +
                toLetter(useTwoPass) + toLetter(useTwoPass)
        (minerName, conf)
    }
    
    def desqDfs(sigma: Long, patternExpression: String, useFlist: Boolean, iterative: Boolean,
                  pruneIrrelevantInputs: Boolean, useTwoPass: Boolean): (String, DesqProperties) = {
        val conf = DDIN.createConf(patternExpression, sigma)
        conf.setProperty("desq.mining.use.flist", useFlist)
        conf.setProperty("desq.mining.iterative", iterative)
        conf.setProperty("desq.mining.prune.irrelevant.inputs", pruneIrrelevantInputs)
        conf.setProperty("desq.mining.use.two.pass", useTwoPass)
        val minerName = "DesqCount-" + toLetter(useFlist) + toLetter(iterative) + toLetter(pruneIrrelevantInputs) +
                toLetter(useTwoPass)
        (minerName, conf)
    }

    def desqCount(sigma: Long, gamma: Int, lambda: Int, generalize: Boolean, useFlist: Boolean,
                  pruneIrrelevantInputs: Boolean, useLazyDfa: Boolean, useTwoPass: Boolean): (String, DesqProperties) = {
        val  patternExpression = DesqMiner.patternExpressionFor(gamma, lambda, generalize)
        desqCount(sigma, patternExpression, useFlist, pruneIrrelevantInputs, useLazyDfa, useTwoPass)
    }

    def all(sigma: Long, gamma: Int, lambda: Int, generalize: Boolean): util.List[(String, DesqProperties)] = {
        val allMiners = new util.ArrayList[(String, DesqProperties)]()
        val patternExpression = DesqMiner.patternExpressionFor(gamma, lambda, generalize)
        allMiners.addAll( all(sigma, patternExpression) )
        allMiners
    }

    def all(sigma: Long, patternExpression: String): util.List[(String, DesqProperties)] = {
        val allMiners = new util.ArrayList[(String, DesqProperties)]()
        allMiners.add(desqCount(sigma, patternExpression, useFlist = false, pruneIrrelevantInputs = false, useLazyDfa = false, useTwoPass = false))
        allMiners.add(desqCount(sigma, patternExpression, useFlist = false, pruneIrrelevantInputs = true, useLazyDfa = false, useTwoPass = false))
        allMiners.add(desqCount(sigma, patternExpression, useFlist = false, pruneIrrelevantInputs = true, useLazyDfa = true, useTwoPass = false))
        allMiners.add(desqCount(sigma, patternExpression, useFlist = false, pruneIrrelevantInputs = true, useLazyDfa = false, useTwoPass = true))
        allMiners.add(desqCount(sigma, patternExpression, useFlist = false, pruneIrrelevantInputs = true, useLazyDfa = true, useTwoPass = true))

        allMiners.add(desqCount(sigma, patternExpression, useFlist = true, pruneIrrelevantInputs = false, useLazyDfa = false, useTwoPass = false))
        allMiners.add(desqCount(sigma, patternExpression, useFlist = true, pruneIrrelevantInputs = true, useLazyDfa = false, useTwoPass = false))
        allMiners.add(desqCount(sigma, patternExpression, useFlist = true, pruneIrrelevantInputs = true, useLazyDfa = true, useTwoPass = false))
        allMiners.add(desqCount(sigma, patternExpression, useFlist = true, pruneIrrelevantInputs = true, useLazyDfa = false, useTwoPass = true))
        allMiners.add(desqCount(sigma, patternExpression, useFlist = true, pruneIrrelevantInputs = true, useLazyDfa = true, useTwoPass = true))
        allMiners
    }

    def toLetter(b: Boolean): String = {
        if (b) "t" else "f"
    }
}

