package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;

/**
 * @author Lionel Nicolas
 */
public class MMAX2ExporterException extends PepperModuleException {

	private static final long serialVersionUID = 6942425892766934367L;
	
	/** 
	 * @param message The message of the Exception
	 */
	public MMAX2ExporterException(String message){ 
    	super("MMAX2Exporter:" + message); 
    }
}
