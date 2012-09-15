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

package jjj.asap.sas.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import weka.core.Instances;

/**
 * Helper class for working with buckets
 */
public class Bucket {

	/**
	 * Adds an arbitrary item to a bucket
	 */
	public static void add(final String bucketType, final String bucketName,
			final String itemName, final String itemContents) 
	{
		final String name = "buckets/"+bucketType+"/"+bucketName+"/"+itemName;
		try {
			PrintWriter writer = new PrintWriter(name);
			writer.println(itemContents);
			writer.flush();
			writer.close();

		} catch(IOException e) {
			throw new RuntimeException(name,e);
		}		
	}

	/**
	 * Adds a dataset to a bucket. The relName is the item name.
	 */
	public static void add(final Instances dataset,final String bucketName) {

		try {

			PrintWriter writer = new PrintWriter(
					"buckets/datasets/"+bucketName+"/"+dataset.relationName());
			writer.println(dataset.numAttributes());
			writer.flush();
			writer.close();

		} catch(IOException e) {
			throw new RuntimeException(dataset.relationName(),e);
		}
	}

	/**
	 * Returns the name of all items in a bucket
	 */
	public static List<String> getBucketItems(final String bucketType, final String bucketName) {

		List<String> items = new ArrayList<String>();

		final File bucket = new File("buckets/" + bucketType + "/" + bucketName);
		File[] files = bucket.listFiles();
		if(files != null) {
			for(File file : files) {
				if(!file.isDirectory()) {
					items.add(file.getName());
				}
			}
		}

		return items;
	}

	/**
	 * @return true if this looks like a valid bucket
	 */
	public static boolean isBucket(final String bucketType, final String bucketName) {
		final File bucket = new File("buckets/" + bucketType + "/" + bucketName);
		return bucket.exists() && bucket.isDirectory();
	}


}