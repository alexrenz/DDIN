package de.uni_mannheim.desq.examples;

import de.uni_mannheim.desq.dictionary.Dictionary;
import de.uni_mannheim.desq.io.DelPatternWriter;
import de.uni_mannheim.desq.io.DelSequenceReader;
import de.uni_mannheim.desq.io.SequenceReader;
import de.uni_mannheim.desq.mining.DesqDfs;
import de.uni_mannheim.desq.mining.DesqMinerContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class CompareDesqDfs {

	// Input parameters 
	//String patternExpression = "([.^ . .]|[. .^ .]|[. . .^])";
	//String patternExpression = "(.^){0,3}";
	String patternExpression = "(.^ JJ@ NN@)";
	int sigma = 1000;
	
	
	// IO
	String inputFile = "data-local/nyt-1991-data.del";
	String dictFile = "data-local/nyt-1991-dict.avro.gz";
	String outputFile = "tmp/output";

	public void run(boolean pruneIrrelevantInputs, boolean useTwoPass) throws IOException {
		Dictionary dict = Dictionary.loadFrom(dictFile);
		SequenceReader dataReader = new DelSequenceReader(new FileInputStream(inputFile), true);
		dataReader.setDictionary(dict);
		DesqMinerContext ctx = new DesqMinerContext();
		ctx.dict = dict;
		DelPatternWriter patternWriter = new DelPatternWriter(
		        new FileOutputStream(outputFile+"-DesqDfs-"+pruneIrrelevantInputs+"-"+useTwoPass),
				DelPatternWriter.TYPE.SID);
		patternWriter.setDictionary(dict);
		ctx.patternWriter = patternWriter;
		ctx.conf = DesqDfs.createConf(patternExpression, sigma);
		ctx.conf.setProperty("desq.mining.prune.irrelevant.inputs", pruneIrrelevantInputs);
		ctx.conf.setProperty("desq.mining.use.two.pass", useTwoPass);

		ExampleUtils.runMiner(dataReader, ctx);
		System.out.println();
		patternWriter.close();
	}
	
	public static void main(String[] args) throws Exception {
		CompareDesqDfs cdd = new CompareDesqDfs();

		cdd.run(false, false);
		cdd.run(true, false);
		cdd.run(true, true);
	}
	
	
}
