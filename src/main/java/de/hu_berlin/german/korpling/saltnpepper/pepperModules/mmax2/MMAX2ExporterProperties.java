package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperty;
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
