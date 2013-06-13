package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions;

import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;

/**
 * @author Lionel Nicolas
 */
public class SaltExtendedMMAX2WrapperException extends MMAX2WrapperException {
	
	private static final long serialVersionUID = 7771432905508129491L;

	/** 
	 * @param message The message of the Exception
	 */
	public SaltExtendedMMAX2WrapperException(String message) {
		super("SaltExtendedMMAX2Wrapper:",message);
	}
}
