package de.uni_mannheim.desq.driver;

import com.google.common.base.Stopwatch;
import de.uni_mannheim.desq.dictionary.Dictionary;
import de.uni_mannheim.desq.io.CountPatternWriter;
import de.uni_mannheim.desq.io.DelSequenceReader;
import de.uni_mannheim.desq.io.SequenceReader;
import de.uni_mannheim.desq.mining.DesqCount;
import de.uni_mannheim.desq.mining.DesqMiner;
import de.uni_mannheim.desq.mining.DesqMinerContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author kbeedkar {kbeedkar@uni-mannheim.de}.
 */
public class DesqCountDriver extends Driver {

    private static final Logger logger = Logger.getLogger(DesqCountDriver.class.getSimpleName());

    public DesqCountDriver(String inputFile, String dictFile, String outDir, String patternExpression, int sigma) {
        super(inputFile, dictFile, outDir, patternExpression, sigma);
    }

    public void runDesqCount(boolean useFlist, boolean pruneIrrelevantInputs, boolean useTwoPass) throws IOException {

        // Set IO paths
        Dictionary dict = Dictionary.loadFrom(dictFile);
        dict.freeze();
        //Dictionary dict = DictionaryIO.loadFromDel(new FileInputStream(dictFile), true);

        SequenceReader dataReader = new DelSequenceReader(new FileInputStream(inputFile), true);
        dataReader.setDictionary(dict);

        String method = toLetter(useFlist) + toLetter(pruneIrrelevantInputs) + toLetter(useTwoPass) + "-" + sanitize(patternExpression) + sigma;
        File outFile = new File(outDir + "/" + "DesqCount-" + method);
        File parentFile = outFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        outFile.delete();


        DesqMinerContext ctx = new DesqMinerContext();
        ctx.dict = dict;
        //DelPatternWriter patternWriter = new DelPatternWriter(new FileOutputStream(outFile), DelPatternWriter.TYPE.SID);
        //ctx.patternWriter = patternWriter;
        CountPatternWriter result = new CountPatternWriter();
        ctx.patternWriter = result;

        ctx.conf = DesqCount.createConf(patternExpression, sigma);
        ctx.conf.setProperty("desq.mining.use.flist", useFlist);
        ctx.conf.setProperty("desq.mining.prune.irrelevant.inputs", pruneIrrelevantInputs);
        ctx.conf.setProperty("desq.mining.use.two.pass", useTwoPass);

        Stopwatch constructionTime = new Stopwatch().start();
        DesqMiner miner = DesqMiner.create(ctx);
        constructionTime.stop();

        Stopwatch readInputTime = new Stopwatch().start();
        miner.addInputSequences(dataReader);
        readInputTime.stop();

        Stopwatch miningTime = new Stopwatch().start();
        miner.mine();
        miningTime.stop();

        //patternWriter.close();
        result.close();


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(method + ", ");
        stringBuilder.append(constructionTime.elapsed(TimeUnit.SECONDS) + ", ");
        stringBuilder.append(readInputTime.elapsed(TimeUnit.SECONDS) + ", ");
        stringBuilder.append(miningTime.elapsed(TimeUnit.SECONDS) + ", ");
        stringBuilder.append(result.getCount() + ", ");
        stringBuilder.append(result.getTotalFrequency());

        logger.info(stringBuilder.toString());

    }

    public static void main(String[] args) throws IOException {
        // get command line options
        if (args.length != 5) {
            System.out.println("usage: inputFile dictFile outDir \"patternExpression\" sigma");
            System.exit(-1);
        }
        String inputFile = args[0];
        String dictFile = args[1];
        String outDir = args[2];
        String patternExpression = args[3];
        int sigma = Integer.parseInt(args[4]);

        DesqCountDriver dcd = new DesqCountDriver(inputFile, dictFile, outDir, patternExpression, sigma);

        logger.setLevel(Level.INFO);
        logger.info(patternExpression + "-" + sigma + "\n");

        //dcd.runDesqCount(false, false, false); //naive
        dcd.runDesqCount(true, false, false); //desq-count
        dcd.runDesqCount(true, true, false); //desq-count with pruning
        dcd.runDesqCount(true, true, true); //desq-count two-pass
    }
}
