package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.SaltExtendedMMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.MarkableFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableAttributeFactory.MarkableAttribute;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;

/**
 * @author Lionel Nicolas
 */
public class SaltExtendedMarkableFactory extends MarkableFactory{
	
	/**
	 * Simple constructor => no Xml parsing required
	 * @param scheme The scheme that the Markable produced by this factory should respect
	 */
	public SaltExtendedMarkableFactory(Scheme scheme){
		super(scheme);
	}
	
	/**
	 * Full constructor => Xml parsing required 
	 * @param scheme The scheme that the Markable produced by this factory should respect
	 * @param documentBuilder The Xml parser to use for parsing Xml files
	 */
	public SaltExtendedMarkableFactory(Scheme scheme, DocumentBuilder documentBuilder){
		super(scheme,documentBuilder);
	}
		 
	
	/** 
	 * Parses the markables of a document and builds accordingly all SaltExtendedMarkables objects
	 * @param documentId The id of the document
	 * @return The SaltExtendedMarkables objects of a document
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws MMAX2WrapperException 
	 */
	ArrayList<SaltExtendedMarkable> getSaltExtendedMarkables(String documentId) throws MMAX2WrapperException, SAXException, IOException {
		ArrayList<SaltExtendedMarkable> results = new ArrayList<SaltExtendedMarkable>();
		
		for(Markable markable : super.getMarkables(documentId)){
			results.add(new SaltExtendedMarkable(this,markable.getId(), markable.getSpan(), markable.getAttributes()));
		}		
		
		return results;	
	}
	

	/** 
	 * Parses the markables of a document and builds accordingly all SaltExtendedMarkables objects
	 * @param documentId The id of the document
	 * @param saltInfos The Salt informations associated with the document
	 * @return The SaltExtendedMarkables objects of a document
	 * @throws SAXException
	 * @throws IOException
	 * @throws MMAX2WrapperException
	 */
	ArrayList<SaltExtendedMarkable> getSaltExtendedMarkables(String documentId, Hashtable<String,Hashtable<String,String>> saltInfos) throws SAXException, IOException, MMAX2WrapperException {
		ArrayList<SaltExtendedMarkable> results = new ArrayList<SaltExtendedMarkable>();
		
		for(Markable markable : super.getMarkables(documentId)){
			if(saltInfos.containsKey(markable.getId())){
				Hashtable<String,String> saltInfoMarkable = saltInfos.get(markable.getId());
				if(!saltInfoMarkable.get(SaltExtendedMmax2Infos.SALT_INFO_STYPE_ATTR_NAME).equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER)){			
					results.add(newMarkable(markable.getId(), markable.getSpan(), markable.getAttributes(), 
							saltInfoMarkable.get(SaltExtendedMmax2Infos.SALT_INFO_STYPE_ATTR_NAME),
							saltInfoMarkable.get(SaltExtendedMmax2Infos.SALT_INFO_SNAME_ATTR_NAME), 
							saltInfoMarkable.get(SaltExtendedMmax2Infos.SALT_INFO_SID_ATTR_NAME)));	
				}else{
					results.add(new SaltExtendedMarkableContainer(this, markable.getId(), markable.getSpan(), markable.getAttributes(), 
							saltInfoMarkable.get(SaltExtendedMmax2Infos.SALT_INFO_STYPE_ATTR_NAME),
							saltInfoMarkable.get(SaltExtendedMmax2Infos.SALT_INFO_SNAME_ATTR_NAME), 
							saltInfoMarkable.get(SaltExtendedMmax2Infos.SALT_INFO_SID_ATTR_NAME), 
							saltInfoMarkable.get(SaltExtendedMmax2Infos.SALT_INFO_CONTAINED_ID_ATTR_NAME),
							saltInfoMarkable.get(SaltExtendedMmax2Infos.SALT_INFO_CONTAINED_SCHEME_ATTR_NAME)));
				}
				saltInfos.remove(markable.getId());
			}else{
				throw new SaltExtendedMMAX2WrapperException("Data corruption: SaltExtendedMarkable "+markable+" has no related Salt Informations");
			}
		}
		return results;	
	}
	
	
	/**
	 * Creates a new SaltExtendedMarkable
	 * @param id The Mmax2 id 
	 * @param span The Mmax2 Span
	 * @param attributes The Mmax2 Attributes
	 * @param sType The Salt type
	 * @param sName The Salt name
	 * @param sId The Salt id
	 * @return A new SaltExtendedMarkable
	 */
	public SaltExtendedMarkable newMarkable(String id, String span, ArrayList<MarkableAttribute> attributes, String sType, String sName, String sId){
		return new SaltExtendedMarkable(this, id, span, attributes, sType, sName, sId);
	}
	
	/**
	 * Creates a new SaltExtendedMarkableContainer
	 * @param id The Mmax2 id 
	 * @param span The Mmax2 Span
	 * @param attributes The Mmax2 Attributes
	 * @param sType The Salt type
	 * @param sName The Salt name
	 * @param sId The Salt id
	 * @param Mmax Id of the markable for which the SContainer is an alias
	 * @return A new SaltExtendedMarkable that is in reality a SaltExtendedMarkableContainer
	 */
	public SaltExtendedMarkableContainer newMarkableContainer(String id, String span, ArrayList<MarkableAttribute> attributes, String sType, String sName, String sId, String containedId, String containedSchemeName){
		return new SaltExtendedMarkableContainer(this, id, span, attributes, sType, sName, sId, containedId, containedSchemeName);
	}
	
	/**
	 * This class models a Markable enhanced with Salt information
	 * @author Lionel Nicolas
	 *
	 */
	
	public class SaltExtendedMarkable extends Markable {
		private boolean hasSaltInformation; // A boolean to know if the SAltExtendedMArkable already has SAlt informations
		private String sId; // The Salt Id if any
		private String sType; // The Salt type if any
		private String sName; // The Salt name if any 
		
		protected SaltExtendedMarkable(SaltExtendedMarkableFactory factory, String id, String span, ArrayList<MarkableAttribute> attributes){
			super(factory,id,span,attributes);
			this.hasSaltInformation = false;
			this.sId = "";
			this.sType = "";
			this.sName = "";
		}
		
		protected SaltExtendedMarkable(SaltExtendedMarkableFactory factory, String id, String span, ArrayList<MarkableAttribute> attributes, String sType, String sName, String sId){
			super(factory,id,span,attributes);
			this.hasSaltInformation = true;
			this.sType = sType;
			this.sName = sName;
			this.sId = sId;
		}
		
		/**
		 * Returns the Salt name
		 * @return The Salt name
		 */
		public String getSName() {
			return this.sName;
		}
		
		/**
		 * Returns the Salt type
		 * @return The Salt type
		 */
		public String getSType() {
			return sType;
		}
		
		/**
		 * Returns the Salt Id
		 * @return The Salt Id
		 */
		public String getSId() {
			return sId;
		}
		
		/**
		 * Tells if the SaltExtendedMarkable is a proper one or a Markable mapped into a  SaltExtendedMarkable
		 * @return A boolean value that indicates if the SaltExtendedMarkable is a proper one.
		 */
		public boolean hasSaltInformation() {
			return this.hasSaltInformation;
		}
	}

	
	/**
	 * This class models the container markable that are used to map data at export.
	 * @author Lionel Nicolas
	 */
	public class SaltExtendedMarkableContainer extends SaltExtendedMarkable{
		private String containedId;
		private String containedSchemeName;
		
		protected SaltExtendedMarkableContainer(SaltExtendedMarkableFactory factory, String id, String span, ArrayList<MarkableAttribute> attributes, String sType, String sName, String sId, String containedId, String containedSchemeName){
			super(factory,id,span,attributes,sType,sName,sId);
			this.containedId = containedId;
			this.containedSchemeName = containedSchemeName;
		}
			
		
		/**
		 * Returns the Id of the markable that has been mapped in the container
		 * @return The Id of the markable that has been mapped in the container
		 */ 
		public String getContainedId(){
			return this.containedId;
		}
		
		/**
		 * Returns the name of the Scheme of the markable that has been mapped in the container
		 * @return the name of the Scheme of the markable that has been mapped in the container
		 */
		public String getContainedSchemeName(){
			return this.containedSchemeName;
		}
		
	}
}
