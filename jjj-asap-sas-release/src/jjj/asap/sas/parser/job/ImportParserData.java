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

package jjj.asap.sas.parser.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.FileIterator;
import jjj.asap.sas.util.IOUtils;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.StringUtils;
import jjj.asap.sas.weka.Dataset;
import jjj.asap.sas.weka.DatasetBuilder;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Reads the raw text parser output and creates ARFF files from it.
 */
public class ImportParserData extends Job {

	private static Pattern ID_PATTERN = Pattern.compile("[0-9]+\\.txt\\.sp");
	private static Pattern TAGS_PATTERN = Pattern.compile(".+/.+");
	private static Pattern TREE_PATTERN = Pattern.compile("^\\(ROOT.*");
	private static Pattern DEPENDS_PATTERN = Pattern.compile("[^\\(]+\\(.+,.+\\)");
	private static Pattern TOKENS_PATTERN = Pattern.compile("^.+/.+/.+$");

	/**
	 * @param args[0] - the parent directory of the data, likely either "t" or "u".
	 */
	public static void main(String[] args) {
		Job job = new ImportParserData();
		job.start();
	}

	private Set<String> standardPosTags;

	/**
	 * Do actual work
	 */
	@Override
	protected void run() throws Exception {

		this.standardPosTags = new HashSet<String>();
		Iterator<String> it = new FileIterator("data/pos.tags");
		while(it.hasNext()) {
			standardPosTags.add(it.next());
		}

		process("t");
		process("u");

	}

	/**
	 * Loads the parser data and processes it one essay at a time
	 * @param parent the parent directory of datasets and parser output
	 */
	private void process(final String parent) {

		// check if output exists
		boolean any = false;
		for(int k=1;k<=10;k++) {
			if(!IOUtils.exists("work/datasets/"+parent+"/"+k+"-extra-stats.arff")) any=true;
			if(!IOUtils.exists("work/datasets/"+parent+"/"+k+"-pos-tags.arff")) any=true;
			if(!IOUtils.exists("work/datasets/"+parent+"/"+k+"-parse-tree.arff")) any=true;
			if(!IOUtils.exists("work/datasets/"+parent+"/"+k+"-depends0.arff")) any=true;
			if(!IOUtils.exists("work/datasets/"+parent+"/"+k+"-depends1.arff")) any=true;
			if(!IOUtils.exists("work/datasets/"+parent+"/"+k+"-depends2.arff")) any=true;
			if(!IOUtils.exists("work/datasets/"+parent+"/"+k+"-depends3.arff")) any=true;
			if(!IOUtils.exists("work/datasets/"+parent+"/"+k+"-depends4.arff")) any=true;
			if(!IOUtils.exists("work/datasets/"+parent+"/"+k+"-depends5.arff")) any=true;
			if(!IOUtils.exists("work/datasets/"+parent+"/"+k+"-depends6.arff")) any=true;
		}
		if(!any) {
			Job.log("NOTE", "work/datasets/" + parent + "/ has all required datasets - nothing to do");
			return;
		}

		// these maps will hold the data
		final Map<Double,List<String>> tags = new HashMap<Double,List<String>>();
		final Map<Double,List<String>> parseTrees = new HashMap<Double,List<String>>();
		final Map<Double,List<String>> depends = new HashMap<Double,List<String>>();

		List<String> tag = new ArrayList<String>();
		List<String> parseTree = new ArrayList<String>();
		List<String> depend = new ArrayList<String>();

		double id = -1;

		Iterator<String> it = new FileIterator("parser/"+parent+"/parser.data");
		while(it.hasNext()) {

			String line = it.next();

			// skip blank or really short lines
			if(line.length() == 1 || line.length() == 2) {
				Job.log("WARNING", "Short line: " + line);
			}
			if(line.length() < 3) continue;

			// parse
			boolean isId = ID_PATTERN.matcher(line).matches();
			boolean isTag = TAGS_PATTERN.matcher(line).matches();
			boolean isTree = TREE_PATTERN.matcher(line).matches();
			boolean isDepend = DEPENDS_PATTERN.matcher(line).matches();

			int matchCount = getMatchCount(isId, isTag, isTree, isDepend);

			if(matchCount != 1) {
				Job.log("WARNING","Line does not match exactly 1 pattern. Expected 1 but matched " 
						+ matchCount + " matches (" + isId + "," + isTag + "," + isTree + "," + isDepend + ") Line was: " + line);
				continue;
			}

			// handle cases

			if(isId) { // id

				if(id != -1) {

					if(tag.size() != parseTree.size()) {
						Job.log("WARNING","tags and parse tree counts differ for "+id + " tags=" + tag.size() + " trees=" + parseTree.size());
					}

					if(depend.size() < 1) {
						Job.log("WARNING","No depends. found for "+id);
						depend.add("NONE/NONE/NONE");
					}

					tags.put(id, tag);
					parseTrees.put(id,parseTree);
					depends.put(id,depend);
					tag = new ArrayList<String>();
					parseTree = new ArrayList<String>();
					depend = new ArrayList<String>();
				}

				int pos = line.indexOf('.');
				id = Double.valueOf(line.substring(0,pos));

			} else if(isTag) { // POS tags

				String[] allTagged = line.split(" ");
				if(allTagged == null || allTagged.length == 0) {
					Job.log("WARNING", "Expecting POS tags but found nothing: "+line);
					continue;
				}

				StringBuilder justTheTags = new StringBuilder();
				for(String tagged : allTagged) {

					int pos = tagged.indexOf('/');
					if(pos == -1) {
						Job.log("WARNING","Expecting word slash tag but found: " + tagged);
						continue;
					}
					if(tagged.length() < pos+2) {
						Job.log("WARNING","Expecting word slash tag but found: " + tagged);
						continue;
					}

					justTheTags.append(tagged.substring(pos+1));
					justTheTags.append(" ");
				}

				if(justTheTags.length() < 1) {
					Job.log("WARNING","This line looked like tags but had none: " + line);
					continue;
				}
				tag.add(justTheTags.toString());

			} else if(isTree) { // parse tree

				String[] nodes = line.split(" ");
				if(nodes == null || nodes.length == 0) {
					Job.log("WARNING","This line looked like a parse tree but had no nodes: "+line);
					continue;
				}

				StringBuilder justTheParseTags = new StringBuilder();
				for(String node : nodes) {
					if(node == null || node.length() < 2) {
						Job.log("WARNING","Cannot handle this strange node: " + node);
						continue;
					}
					if(node.startsWith("(") && node.endsWith(")")) {
						Job.log("WARNING","Cannot handle this strange node: " + node);
						continue;
					}

					if(node.startsWith("(")) {
						String parseTag = node.substring(1);
						if(!this.standardPosTags.contains(parseTag)) {
							justTheParseTags.append(parseTag);
							justTheParseTags.append(" ");
						}
					}

				} // end node in nodes

				if(justTheParseTags.length() < 1) {
					Job.log("WARNING","This line looked like a parse tree but had no nodes: " + line);
					continue;
				}
				parseTree.add(justTheParseTags.toString());

			} else if(isDepend) {

				// format is:
				// NAME(WORD1-99, WORD2-99)
				// and we want it to be
				// NAME/WORD1/WORD2

				// first some checking
				if(line.indexOf('(') == -1 || line.indexOf(')') == -1 || line.indexOf(',') == -1) {
					Job.log("WARNING","invalid depends format (1): " + line);
					continue;
				}

				if(line.indexOf('(') != line.lastIndexOf('(')) {
					Job.log("WARNING","invalid depends format (2): " + line);
					continue;
				}


				//if(line.indexOf(',') != line.lastIndexOf(',')) {
				//	Job.log("WARNING","invalid depends format (3): " + line);
				//	continue;
				//}

				if(line.indexOf(')') != line.lastIndexOf(')')) {
					Job.log("WARNING","invalid depends format (4): " + line);
					continue;
				}

				// break up
				String foo = "";
				String t = line; foo += ("*" + t);
				t = t.replaceAll("-[0-9]+'*, ", "/"); foo+=("**" + t);
				t = t.replaceAll("-[0-9]+'*\\)$",""); foo+=("***" + t);
				t = t.replaceAll("\\(","/"); foo+=("****" + t);
				if(!TOKENS_PATTERN.matcher(t).matches()) {
					Job.log("WARNING","tokenized depends looks wrong: " + t);
					System.out.println(foo);
				}

				depend.add(t);

			} else {
				throw new RuntimeException("should not ever reach here");
			}

		} // end while it has next

		// process last insert

		if(tag.size() != parseTree.size()) {
			Job.log("WARNING","tags and parse tree counts differ for "+id);
		}

		if(depend.size() < 1) {
			Job.log("WARNING","No depends. found for "+id);
		}

		tags.put(id, tag);
		parseTrees.put(id,parseTree);
		depends.put(id,depend);

		// now process for each essay set
		for(int k=1;k<=10;k++) {
			process(parent,k,tags,parseTrees,depends);
		}
	}

	private void process(final String parent, int essaySet,
			Map<Double,List<String>> tags,
			Map<Double,List<String>> parseTrees,
			Map<Double,List<String>> depends) {


		// check if output exists
		boolean any = false;
		
		if(!IOUtils.exists("work/datasets/"+parent+"/"+essaySet+"-extra-stats.arff")) any=true;
		if(!IOUtils.exists("work/datasets/"+parent+"/"+essaySet+"-pos-tags.arff")) any=true;
		if(!IOUtils.exists("work/datasets/"+parent+"/"+essaySet+"-parse-tree.arff")) any=true;
		if(!IOUtils.exists("work/datasets/"+parent+"/"+essaySet+"-depends0.arff")) any=true;
		if(!IOUtils.exists("work/datasets/"+parent+"/"+essaySet+"-depends1.arff")) any=true;
		if(!IOUtils.exists("work/datasets/"+parent+"/"+essaySet+"-depends2.arff")) any=true;
		if(!IOUtils.exists("work/datasets/"+parent+"/"+essaySet+"-depends3.arff")) any=true;
		if(!IOUtils.exists("work/datasets/"+parent+"/"+essaySet+"-depends4.arff")) any=true;
		if(!IOUtils.exists("work/datasets/"+parent+"/"+essaySet+"-depends5.arff")) any=true;
		if(!IOUtils.exists("work/datasets/"+parent+"/"+essaySet+"-depends6.arff")) any=true;

		if(!any) {
			Job.log("NOTE", "work/datasets/" + parent + "/" + essaySet + "-*.arff returns all required datasets - nothing to do");
			return;
		}

		// Load an existing dataset to use as a template.
		Instances dataset = Dataset.load("work/datasets/"+parent+"/"+essaySet+"-spell-checked.arff");

		// create the output datasets here. except for the extra statistics, 
		// the format is the same as 'dataset'.

		Instances tagsData = new Instances(dataset,0);
		tagsData.setRelationName(essaySet+"-pos-tags.arff");
		Instances treeData = new Instances(dataset,0);
		treeData.setRelationName(essaySet+"-parse-tree.arff");

		Instances dependsData[] = new Instances[7];
		for(int j=0;j<7;j++) {
			dependsData[j] = new Instances(dataset,0);
			dependsData[j].setRelationName(essaySet + "-depends"+j+".arff");
		}

		// extra stats
		DatasetBuilder builder = new DatasetBuilder();
		builder.addVariable("id");
		if(Contest.isMultiChoice(essaySet)) {
			builder.addNominalVariable("color", Contest.COLORS);
		}
		builder.addVariable("x_sent");
		builder.addVariable("x_para");
		builder.addVariable("x_length");
		builder.addVariable("x_words");
		builder.addVariable("x_unique_words");
		builder.addNominalVariable("score",Contest.getRubrics(essaySet));

		Instances extraStats = builder.getDataset(essaySet+"-extra-stats.arff");

		// now add rows for each instance

		for(int i=0;i<dataset.numInstances();i++) {

			// common variables
			Instance ob = dataset.instance(i);
			double id = ob.value(0);
			String y = ob.isMissing(dataset.numAttributes()-1) ? null : ob.stringValue(dataset.numAttributes()-1);
			String color = Contest.isMultiChoice(essaySet) ? ob.stringValue(dataset.attribute("color")) : null;
			String str = ob.stringValue(dataset.attribute("text"));

			//
			// Extra stats
			//

			int nSent = tags.containsKey(id) ? tags.get(id).size() : 0;
			int nPara = 0;
			for(int a=0;a<str.length();a++) {
				if(str.charAt(a) == '^') nPara++;
			}
			int nLength = str.length();
			int nWords = 0;
			int nUniqueWords = 0;
			String[] words = str.toLowerCase().split(" ");
			nWords = words.length;
			Set<String> u = new HashSet<String>();
			for(String w : words) {
				u.add(w);
			}
			nUniqueWords = u.size();

			extraStats.add(new DenseInstance(extraStats.numAttributes()));
			Instance extra = extraStats.lastInstance();
			extra.setValue(0,id);
			if(Contest.isMultiChoice(essaySet)) {
				extra.setValue(1,color);
			}

			extra.setValue(extraStats.attribute("x_sent"),nSent);
			extra.setValue(extraStats.attribute("x_para"),nPara);
			extra.setValue(extraStats.attribute("x_length"),nLength);
			extra.setValue(extraStats.attribute("x_words"),nWords);
			extra.setValue(extraStats.attribute("x_unique_words"),nUniqueWords);

			if(y==null)
				extra.setValue(extraStats.numAttributes()-1, Utils.missingValue());
			else
				extra.setValue(extraStats.numAttributes()-1, y);

			//
			// POS tags
			//

			String tagsText = "";
			List<String> tagsList = tags.get(id);
			if(tagsList == null || tagsList.isEmpty()) {
				Job.log("WARNING", "no tags for " + id);
				tagsText = "x";
			} else {
				for(String tagsItem : tagsList) {
					tagsText += tagsItem;
				}
			}

			tagsData.add(new DenseInstance(ob.numAttributes()));
			Instance tagsOb = tagsData.lastInstance();
			tagsOb.setValue(0,id);
			if(Contest.isMultiChoice(essaySet)) {
				tagsOb.setValue(1,color);
				tagsOb.setValue(2,tagsText.trim());
				if(y==null) {
					tagsOb.setValue(3,Utils.missingValue());
				} else {
					tagsOb.setValue(3,y);
				}
			} else {
				tagsOb.setValue(1,tagsText.trim());
				if(y==null) {
					tagsOb.setValue(2,Utils.missingValue());
				} else {
					tagsOb.setValue(2,y);
				}
			}

			//
			// Parse Tree
			//

			String treeText = "";
			List<String> treeList = parseTrees.get(id);
			if(treeList == null || treeList.isEmpty()) {
				Job.log("WARNING", "no parse tree for " + id);
				treeText = "x";
			} else {
				for(String treeItem : treeList) {
					treeText += treeItem;
				}
			}

			treeData.add(new DenseInstance(ob.numAttributes()));
			Instance treeOb = treeData.lastInstance();
			treeOb.setValue(0,id);
			if(Contest.isMultiChoice(essaySet)) {
				treeOb.setValue(1,color);
				treeOb.setValue(2,treeText.trim());
				if(y==null) {
					treeOb.setValue(3,Utils.missingValue());
				} else {
					treeOb.setValue(3,y);
				}
			} else {
				treeOb.setValue(1,treeText.trim());
				if(y==null) {
					treeOb.setValue(2,Utils.missingValue());
				} else {
					treeOb.setValue(2,y);
				}
			}

			//
			// Depends data
			//

			for(int j=0;j<7;j++) {

				String text = "";
				List<String> list = depends.get(id);
				if(list == null || list.isEmpty()) {
					Job.log("WARNING", "no depends for " + id);
					text = "x";
				} else {
					for(String item : list) {
						String[] term = StringUtils.safeSplit(item, "/", 3);
						switch(j) {
						case 0:
							text += item;
							break;
						case 1:
							text += term[1]+"/"+term[2];
							break;
						case 2:
							text += term[0]+"/"+term[2];
							break;
						case 3:
							text += term[0]+"/"+term[1];
							break;
						case 4:
							text += term[0];
							break;
						case 5:
							text += term[1];
							break;
						case 6:
							text += term[2];
							break;
						}
						text+=" ";
					}
				}

				dependsData[j].add(new DenseInstance(ob.numAttributes()));
				Instance dependsOb = dependsData[j].lastInstance();
				dependsOb.setValue(0,id);
				if(Contest.isMultiChoice(essaySet)) {
					dependsOb.setValue(1,color);
					dependsOb.setValue(2,text.trim());
					if(y==null) {
						dependsOb.setValue(3,Utils.missingValue());
					} else {
						dependsOb.setValue(3,y);
					}
				} else {
					dependsOb.setValue(1,text.trim());
					if(y==null) {
						dependsOb.setValue(2,Utils.missingValue());
					} else {
						dependsOb.setValue(2,y);
					}				
				}

			} // j
		} // dataset

		// Now save the new datasets

		Dataset.save("work/datasets/"+parent+"/"+tagsData.relationName(),tagsData);
		Dataset.save("work/datasets/"+parent+"/"+treeData.relationName(),treeData);
		for(int j=0;j<7;j++) {
			Dataset.save("work/datasets/"+parent+"/"+dependsData[j].relationName(),dependsData[j]);
		}
		Dataset.save("work/datasets/"+parent+"/"+extraStats.relationName(),extraStats);		

	} // method

	private int getMatchCount(boolean a, boolean b, boolean c, boolean d) {
		int count = 0;
		if(a) count++;
		if(b) count++;
		if(c) count++;
		if(d) count++;
		return count;
	}


}
