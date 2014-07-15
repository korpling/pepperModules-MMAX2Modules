package eurac.commul.annotations.mmax2wrapper;

/**
 * 
 * @author Lionel Nicolas
 *
 */
public class MMAX2WrapperException extends Exception {

	private static final long serialVersionUID = 3698072205535580116L;

	/** 
	 * @param message The message of the Exception
	 */
    public MMAX2WrapperException(String message) {
		this("MMAX2Wrapper",message);
	}
	
    protected MMAX2WrapperException(String entete, String message) {
		super(entete + ":" + message);
	}
}
