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
