/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;

/**
 * Properties to customize the mapping from Salt to Mmax2 data.
 * @author Florian Zipser
 *
 */
public class MMAX2ExporterProperties extends PepperModuleProperties {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3789432537912976767L;
	/** name of property to ???**/ //TODO what does this property do
	public static final String PROP_MAPPINGS_SANNOTATIONS_FP="MMAX2Exporter.sannotationMappingsFilePath";
	/** name of property to ???**/ //TODO what does this property do 
	public static final String PROP_MAPPINGS_SRELATIONS_FP="MMAX2Exporter.srelationMappingsFilePath";
	
	public MMAX2ExporterProperties()
	{
		this.addProperty(new PepperModuleProperty<String>(PROP_MAPPINGS_SANNOTATIONS_FP, String.class, "???", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_MAPPINGS_SRELATIONS_FP, String.class, "???",false));
	}
	
	public String getSAnnotationMappingsFilePath(){
		String retVal= null;
		retVal= (String)getProperty(PROP_MAPPINGS_SANNOTATIONS_FP).getValue();
		return(retVal);
	}
	
	public String getSRelationMappingsFilePath(){
		String retVal= null;
		retVal= (String)getProperty(PROP_MAPPINGS_SRELATIONS_FP).getValue();
		return(retVal);
	}
}
