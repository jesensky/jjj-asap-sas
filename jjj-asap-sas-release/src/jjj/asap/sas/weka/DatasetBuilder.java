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

package jjj.asap.sas.weka;

import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instances;

/**
 * Builds an empty dataset
 */
public class DatasetBuilder {

	private List<Attribute> attributes;
	
	public DatasetBuilder() {
		this.attributes = new ArrayList<Attribute>();
	}
	
	public void addVariable(final String name) {
		this.attributes.add(new Attribute(name));
	}
	
	public void addStringVariable(final String name) {
		this.attributes.add(new Attribute(name,(List)null));
	}

	public void addNominalVariable(final String name,List<String> values) {
		this.attributes.add(new Attribute(name,values));
	}
	
	public Instances getDataset(final String relName) {
		return new Instances(relName,(ArrayList)this.attributes,0);
	}
	
}
