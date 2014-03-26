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
	public static final String PROP_MATCHING_CONDITIONS="MMAX2Exporter.matchingConditionsFilePath";
	/** name of property to ???**/ //TODO what does this property do 
	public static final String PROP_POINTERS_MATCHING_CONDITIONS="MMAX2Exporter.pointersMatchingConditionsFilePath";
	
	public MMAX2ExporterProperties()
	{
		this.addProperty(new PepperModuleProperty<String>(PROP_MATCHING_CONDITIONS, String.class, "???", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_POINTERS_MATCHING_CONDITIONS, String.class, "???",false));
	}
	
	public String getMatchingConditionsFilePath(){
		String retVal= null;
		retVal= getProperty(PROP_MATCHING_CONDITIONS).toString();
		return(retVal);
	}
	
	public String getPointersMatchingConditionsFilePath(){
		String retVal= null;
		retVal= getProperty(PROP_POINTERS_MATCHING_CONDITIONS).toString();
		return(retVal);
	}
}
