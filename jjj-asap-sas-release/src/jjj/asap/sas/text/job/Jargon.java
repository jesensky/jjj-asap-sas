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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import jjj.asap.sas.util.FileIterator;
import jjj.asap.sas.util.IOUtils;
import jjj.asap.sas.util.Job;

/**
 * Creates a 
 */
public class Jargon extends Job {

	public static void main(String[] args) throws Exception {
		Job job = new Jargon();
		job.start();
	}

	// will hold all the words
	private Set<String> words = new TreeSet<String>();

	@Override
	protected void run() throws Exception {

		// check file
		String outFile = "jaspell/dict/jargon.txt";
		if(IOUtils.exists(outFile)) {
			Job.log("NOTE",outFile + " already exists - nothing to do.");
			return;
		}

		// for each essay set
		for(int k=1;k<=10;k++) {
			Iterator<String> it = new FileIterator("data\\Data Set #"+k+"--ReadMeFirst.txt");
			while(it.hasNext()) {
				String text = it.next();
				if(text.length() > 0) {
					String[] tokens = text.split("[^a-zA-Z']+");
					for(String token : tokens) {
						if(token != null && token.length() > 0 && !token.startsWith("'")) {
							words.add(token.toLowerCase());
						}
					}
				}
			}
		}

		// now load integers
		for(int i=0;i<=99999;i++) {
			words.add(String.valueOf(i));
		}
		
		// misc.
		words.add("atp");
		words.add("adp");
		words.add("anticodons");
		words.add("grna");
		words.add("exocytosis");
		words.add("endocytosis");
		words.add("prophase");
		words.add("metaphase");
		words.add("anaphase");
		words.add("telophase");
		words.add("interphase");
		words.add("endoplasmic");
		words.add("towards");
		words.add("mitochondria");
		words.add("golgi");
		words.add("macinnes");
		words.add("mrna");
		words.add("grna");
		words.add("rrna");
		words.add("rna");
		words.add("trna");	
		words.add("anticodon");
		words.add("codon");
		words.add("codons");
				
		// write out 
		
		PrintWriter output = new PrintWriter(outFile);
		for(String word : words) {
			output.println(word);
		}
		output.flush();
		output.close();
	}

}
