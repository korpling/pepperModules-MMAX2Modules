package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions;

import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;



public class SaltExtendedMMAX2WrapperException extends MMAX2WrapperException {

	public SaltExtendedMMAX2WrapperException(String message) {
		super("SaltExtendedMMAX2Wrapper",message);
	}
}
