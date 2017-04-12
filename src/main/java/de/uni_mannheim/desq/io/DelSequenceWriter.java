package de.uni_mannheim.desq.io;

import it.unimi.dsi.fastutil.ints.IntList;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public class DelSequenceWriter extends SequenceWriter {
	private final PrintWriter writer;
	private final boolean convertToIds;

	public DelSequenceWriter(PrintWriter writer, boolean convertToIds) {
		this.writer = writer;
		this.convertToIds = convertToIds;
	}

	public DelSequenceWriter(Writer writer, boolean convertToIds) {
		this(new PrintWriter(writer, true), convertToIds);
	}	
	
	public  DelSequenceWriter(OutputStream out, boolean convertToIds) {
		this(new PrintWriter(out, true), convertToIds);
	}

	@Override
	public void write(IntList itemFids) {
		String sep="";
		for (int i=0; i<itemFids.size(); i++) {
			int fid = itemFids.getInt(i);
			writer.write(sep);
			sep = "\t";
			if (convertToIds) {
				writer.print( dict.gidOf(fid));
			} else {
				writer.print( fid );
			}
		}
		writer.println();
	}

	public void close() {
		writer.close();
	}
}
