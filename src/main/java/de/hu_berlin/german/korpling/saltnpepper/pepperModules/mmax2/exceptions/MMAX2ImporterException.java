package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;

/**
 * @author Lionel Nicolas
 */
public class MMAX2ImporterException extends PepperModuleException {

	private static final long serialVersionUID = 553407151506655375L;
	
	/** 
	 * @param message The message of the Exception
	 */
    public MMAX2ImporterException(String message){ 
    	super("MMAX2Importer:" + message); 
    }
}
