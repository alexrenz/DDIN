package de.uni_mannheim.desq.io;

import it.unimi.dsi.fastutil.ints.IntList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by rgemulla on 18.07.2016.
 */
// TODO: also support reading del files with item names (sids) like in DelPatternWriter
public class DelPatternReader extends PatternReader {
    private final BufferedReader reader;
    private final boolean usesFids;

    public DelPatternReader(InputStream in, boolean usesFids) {
        this.reader = new BufferedReader(new InputStreamReader(in));
        this.usesFids = usesFids;
    }

    @Override
    public long read(IntList items) throws IOException {
        String line = reader.readLine();
        if (line == null) return -1;
        return parseLine(line, items);
    }

    public static long parseLine(String line, IntList items) {
        items.clear();
        String[] tokens = line.split("\t");
        long frequency = Long.parseLong(tokens[0]);
        for (int i=1; i<tokens.length; i++) {
            if (tokens[i].isEmpty()) continue;
            items.add(Integer.parseInt(tokens[i]));
        }
        return frequency;
    }

    @Override
    public boolean usesFids() {
        return usesFids;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
