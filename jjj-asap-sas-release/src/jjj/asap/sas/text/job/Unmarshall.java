/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or GITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) 2012 James Jesensky
 */

package jjj.asap.sas.text.job;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.FileIterator;
import jjj.asap.sas.util.IOUtils;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.StringUtils;

/**
 * This job basically splits the train and test files into 10 separate files: 1
 * for each essay set.
 * 
 * It also performs some basic data cleaning.
 */
public class Unmarshall extends Job {

	/**
	 * These training observations seem to have bad data. We will exclude
	 * them from all training.
	 */
	public static final List<Integer> REJECTS = Arrays.asList(
			14,
			646,
			1051,
			1194,
			1304,
			1452,
			3002,
			3070,
			3148,
			3196,
			3393,
			3403,
			3489,
			3709,
			3806,
			3850,
			4009);

	public static void main(String[] args) throws Exception {
		Job job = new Unmarshall();
		job.start();
	}

	@Override
	protected void run() throws Exception {

		unmarshallTrainingFile();
		unmarshallTestFile();
	}

	private void unmarshallTestFile() throws FileNotFoundException {

		// create a writer for each essay set
		final PrintWriter[] writers = new PrintWriter[10];
		for(int i=0;i<writers.length;i++) {
			final String filename = "work/text/u/"+(i+1)+"-raw.txt";
			if(IOUtils.exists(filename)) {
				writers[i] = null;
				Job.log("NOTE", filename + " already exists - nothing to do.");
			} else {
				writers[i] = new PrintWriter(filename);
			}
		}

		// input file
		final Iterator<String> it = new FileIterator("data/test.tsv");
		it.next(); // skip header
		while(it.hasNext()) {
			final String record = it.next();
			final String[] fields = StringUtils.safeSplit(record,"\t",3);
			// basic re-formatting
			final String text = reformat(fields[2]);
			// which essay set?
			final int essaySet = Integer.valueOf(fields[1]);
			// essay #10 is a special case since its text embeds the answer to a multi-choice question
			if(Contest.isMultiChoice(essaySet)) {
				final String[] answers = StringUtils.safeSplit(text, "::", 2);
				if(writers[essaySet-1] != null) {
					writers[essaySet-1].println(fields[0]+"\t"+fields[1]+"\t?\t?\t"
							+StringUtils.safeForNLP(answers[0])+"\t"+fix(StringUtils.safeForNLP(answers[1])));
				}
			} else {
				if(writers[essaySet-1] != null) { 
					writers[essaySet-1].println(fields[0]+"\t"+fields[1]+"\t?\t?\t?\t"+StringUtils.safeForNLP(text));
				}
			}
		}
		// close files
		for(int i=0;i<writers.length;i++) {
			if(writers[i] != null) {
				writers[i].flush();
				writers[i].close();
			}
		}

	}

	private void unmarshallTrainingFile() throws FileNotFoundException {

		// create a writer for each essay set
		final PrintWriter[] writers = new PrintWriter[10];
		for(int i=0;i<writers.length;i++) {
			final String filename = "work/text/t/"+(i+1)+"-raw.txt";
			if(IOUtils.exists(filename)) {
				writers[i] = null;
				Job.log("NOTE", filename + " already exists - nothing to do.");
			} else {
				writers[i] = new PrintWriter(filename);
			}
		}

		// input file
		final Iterator<String> it = new FileIterator("data/train.tsv");
		it.next(); // skip header
		while(it.hasNext()) {
			final String record = it.next();
			final String[] fields = StringUtils.safeSplit(record,"\t",5);
			// basic re-formatting
			final String text = reformat(fields[4]);
			// which essay set?
			final int essaySet = Integer.valueOf(fields[1]);
			if(writers[essaySet-1] == null) continue;
			// check for bad training examples.
			if(REJECTS.contains(Integer.valueOf(fields[0]))) {
				Job.log("NOTE", "Rejecting this record:\n"+record);
				continue;
			}
			// essay #10 is a special case since its text embeds the answer to a multi-choice question
			if(Contest.isMultiChoice(essaySet)) {
				final String[] answers = StringUtils.safeSplit(text, "::", 2);
				if(writers[essaySet-1] != null) {
					writers[essaySet-1].println(fields[0]+"\t"+fields[1]+"\t"+fields[2]+"\t"+fields[3]
							+"\t"+StringUtils.safeForNLP(answers[0])+"\t"+fix(StringUtils.safeForNLP(answers[1])));
				}
			} else {
				if(writers[essaySet-1] != null) { 
					writers[essaySet-1].println(fields[0]+"\t"+fields[1]+"\t"+fields[2]+"\t"+fields[3]
							+"\t?\t"+StringUtils.safeForNLP(text));
				}
			}
		}
		// close files
		for(int i=0;i<writers.length;i++) {
			if(writers[i] != null) {
				writers[i].flush();
				writers[i].close();
			}
		}
	}

	/**
	 * Essay set #10 appears to have semi-random spaces
	 * inserted into it. Transcription errors? Attempt to 
	 * fix some of it here.
	 */
	private String fix(final String body) {
		
		String text = body;
		
		text = fix(text,"white");
		text = fix(text,"black");
		text = fix(text,"gray");
		text = fix(text,"light");
		text = fix(text,"dark");
		text = fix(text,"color");
		text = fix(text,"because");
		text = fix(text,"doghouse");
		text = fix(text,"hot");
		text = fix(text,"temperature");
		text = fix(text,"heat");
		text = fix(text,"average");
		text = fix(text,"inside");
		text = fix(text,"not");
		text = fix(text,"paint");
		text = fix(text,"warm");
		text = fix(text,"keep");
		text = fix(text,"cold");
		text = fix(text,"colors");
		text = fix(text,"cooler");
		text = fix(text,"cool");
		text = fix(text,"air");
		text = fix(text,"experiment");
		text = fix(text,"summer");
		text = fix(text,"absorb");
		text = fix(text,"energy");
		text = fix(text,"darker");
		text = fix(text,"affect");
		text = fix(text,"warmer");
		text = fix(text,"winter");
		text = fix(text,"sun");
		text = fix(text,"degress");
		text = fix(text,"absorbs");
		text = fix(text,"data");
		text = fix(text,"effect");
		
		return text;
	}
	
	private String fix(final String body, final String word) {
		
		String text = body;
		
		for(int i=1;i<word.length(); i++) {
			
			String car = word.substring(0, i);
			String cdr = word.substring(i);
			
			text = text.replaceAll(" "+car+" "+cdr+" ", " "+car+cdr+" ");
			text = text.replaceAll(" "+car+" "+cdr+",", " "+car+cdr+",");
			text = text.replaceAll(" "+car+" "+cdr+".", " "+car+cdr+".");
		}
		
		return text;
	}

	/**
	 * Applies common reformatting to the essay responses.
	 */
	private static String reformat(final String raw) {

		String text = raw;

		// When response is surrounded with " we find that
		// it contains embedded, escaped quotes.
		if(text.startsWith("\"") && text.endsWith("\"")) {
			text = text.replaceAll("\"$", "");
			text = text.replaceAll("^\"", "");
			text = text.replaceAll("\"+", "\"");
		}
		// replace '' with "
		text = text.replaceAll("''", "\"");
		// & is often used to mean 'and'
		text = text.replaceAll(" & ", " and ");
		// slash(/) probably means 'or'
		text = text.replaceAll("/", " or ");
		// replace + with and'
		text = text.replaceAll("\\+", " and ");
		// remove ^p and ^P
		text = text.replaceAll("\\^p", " ");
		text = text.replaceAll("\\^P", " ");
		text = text.replaceAll("P\\^", " ");
		// replace (DEG)
		text = text.replaceAll("\\(DEG\\)"," degrees ");

		// trim and return
		return text.trim();
	}

}
