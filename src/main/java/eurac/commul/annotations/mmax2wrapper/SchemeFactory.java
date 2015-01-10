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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eurac.commul.annotations.mmax2wrapper.CorpusFactory.Corpus;

/**
 * 
 * @author Lionel Nicolas
 *
 */
public class SchemeFactory{
	
	private Corpus corpus; // The corpus to which the Scheme belongs
	private DocumentBuilder documentBuilder; // The Xml parser to use for parsing Xml files
	
	/**
	 * Simple constructor => no Xml parsing required
	 * @param corpus The corpus to which the Scheme belongs
	 */
	public SchemeFactory(Corpus corpus){
		this.corpus = corpus;
	}
	
	/**
	 * Full constructor => Xml parsing required
	 * @param documentBuilder The Xml parser 
	 * @param corpus The corpus to which the Scheme belongs
	 */
	public SchemeFactory(Corpus corpus, DocumentBuilder documentBuilder){
		this.corpus = corpus;
		this.documentBuilder = documentBuilder;
	}
	
	
	/**
	 * Parse a Scheme Xml file of an Mmax2 corpus and creates the object accordingly
	 * @param schemeName the Name to use for the Scheme
	 * @param schemeFilePattern The pattern of the files name to use for the Scheme 
	 * @return
	 * @throws MMAX2WrapperException
	 * @throws SAXException
	 * @throws IOException
	 */
	Scheme getScheme(String schemeName,String schemeFilePath, String schemeFilePattern) throws SAXException, IOException, MMAX2WrapperException{
		if(this.documentBuilder == null)
			throw new MMAX2WrapperException("To use function 'getScheme' a DocumentBuilder needs to be provided at instantiation");
		
		File schemeFile = new File(this.corpus.getSchemesPath().getAbsolutePath()+File.separator+schemeFilePath);
		NodeList nodes = documentBuilder.parse(schemeFile).getDocumentElement().getChildNodes();
				
		Scheme scheme = new Scheme(this,schemeName,schemeFilePattern);	
		for(int i = 0; i < nodes.getLength(); i ++){
			Node node = nodes.item(i);
			String nodeName = node.getNodeName();
			if((nodeName != null) && (nodeName.equals(Mmax2Infos.SCHEME_ATTR_NAME))){
				scheme.addMarkableAttributeFactory(this.getMarkableAttributeFactory(scheme,node));
			}
		}
		
		return scheme;	
	}
	
	// Creates a MarkableAttributeFactory out of the Xml entry 
	private MarkableAttributeFactory getMarkableAttributeFactory(Scheme scheme, Node xmlNode) throws MMAX2WrapperException{
		NamedNodeMap attributes = xmlNode.getAttributes();
	
		MarkableAttributeFactory result;
		
		Node idAttributeNode = attributes.getNamedItem(Mmax2Infos.SCHEME_ID_ATTR_NAME);
		if(idAttributeNode == null){
			throw new MMAX2WrapperException("Attribute '"+xmlNode.toString()+"' has no '"+Mmax2Infos.SCHEME_ID_ATTR_NAME+"' attribute defined");
		}
		//String idAttribute = idAttributeNode.getNodeValue();
		
		Node nameAttributeNode = attributes.getNamedItem(Mmax2Infos.SCHEME_NAME_ATTR_NAME);
		if(nameAttributeNode == null){
			throw new MMAX2WrapperException("Attribute '"+xmlNode.toString()+"' has no '"+Mmax2Infos.SCHEME_NAME_ATTR_NAME+"' attribute defined");
		}
		String nameAttribute = nameAttributeNode.getNodeValue().toLowerCase();
		
		Node typeAttributeNode = attributes.getNamedItem(Mmax2Infos.SCHEME_TYPE_ATTR_NAME);
		if(typeAttributeNode == null){
			throw new MMAX2WrapperException("Attribute '"+xmlNode.toString()+"' has no '"+Mmax2Infos.SCHEME_TYPE_ATTR_NAME+"' attribute defined");
		}
		String typeAttribute = typeAttributeNode.getNodeValue();
		
		if((typeAttribute.equals(Mmax2Infos.SCHEME_NOMINAL_LIST_ATTR_TYPE)) || (typeAttribute.equals(Mmax2Infos.SCHEME_NOMINAL_BUTTON_ATTR_TYPE))){
			NodeList nodes = xmlNode.getChildNodes();
			ArrayList<String> valuesAccepted = new ArrayList<String>();
			for(int i = 0; i < nodes.getLength(); i ++){
				Node node = nodes.item(i);
				String nodeName = node.getNodeName();
				if((nodeName != null) && (nodeName.equals(Mmax2Infos.SCHEME_NOMINAL_ATTR_VALUE_NAME))){
					NamedNodeMap valueAttributes = node.getAttributes();
					
					Node valueAttributeNode = valueAttributes.getNamedItem(Mmax2Infos.SCHEME_NOMINAL_ATTR_VALUE_NAME_NAME);
					if(valueAttributeNode == null){
						throw new MMAX2WrapperException("Value '"+node.toString()+"' of attribute '"+nameAttribute+"' has no '"+Mmax2Infos.SCHEME_NOMINAL_ATTR_VALUE_NAME_NAME+"' attribute defined");
					}
					valuesAccepted.add(valueAttributeNode.getNodeValue());
				}
			}
			if(valuesAccepted.size() == 0){
				throw new MMAX2WrapperException("Nominal attribute '"+nameAttribute+"' has no values defined");
			}
			result = new MarkableNominalAttributeFactory(scheme,nameAttribute, valuesAccepted);
	
		}else if(typeAttribute.equals(Mmax2Infos.SCHEME_FREETEXT_ATTR_TYPE)){
			result = new MarkableFreetextAttributeFactory(scheme,nameAttribute);
		
		}else if(typeAttribute.equals(Mmax2Infos.SCHEME_POINTER_ATTR_TYPE)){
			Node targetDomainAttributeNode = attributes.getNamedItem(Mmax2Infos.SCHEME_TARGET_DOMAIN_ATTR_NAME);
			String targetDomainAttribute = null;
			if(targetDomainAttributeNode != null){
				targetDomainAttribute = targetDomainAttributeNode.getNodeValue();
			}
			result = new MarkablePointerAttributeFactory(scheme,nameAttribute, targetDomainAttribute);			
		
		}else if(typeAttribute.equals(Mmax2Infos.SCHEME_SET_ATTR_TYPE)){
			result = new MarkableSetAttributeFactory(scheme,nameAttribute);
		
		}else{
			throw new MMAX2WrapperException("Attribute '"+nameAttribute+"' has an unknown type '"+typeAttribute+"'");
		}
		
		return result;		
	}
	
	/**
	 * Returns a new Scheme
	 * @param attributeName The name of the Scheme
	 * @return A new Scheme
	 */
	public Scheme newScheme(String schemeName){
		return this.newScheme(schemeName,"$_"+schemeName+"_level.xml");
	}
	
	/**
	 * Returns a new Scheme
	 * @param attributeName The name of the Scheme
	 * @param schemeFilePattern The pattern of the files name for this Scheme 
	 * @return A new Scheme
	 */
	public Scheme newScheme(String schemeName, String schemeFilePattern){
		return new Scheme(this,schemeName,schemeFilePattern);
	}
	
	/**
	 * Returns a new MarkablePointerAttributeFactory
	 * @param attributeName The name of the MarkablePointerAttributeFactory
	 * @return A new MarkablePointerAttributeFactory
	 */
	public MarkablePointerAttributeFactory newMarkablePointerAttributeFactory(Scheme scheme, String attributeName, String targetSchemeName){
		return new MarkablePointerAttributeFactory(scheme, attributeName,targetSchemeName);
	}
	
	/**
	 * Returns a new MarkableNominalAttributeFactory
	 * @param attributeName The name of the MarkableNominalAttributeFactory
	 * @return A new MarkableNominalAttributeFactory
	 */
	public MarkableNominalAttributeFactory newMarkableNominalAttributeFactory(Scheme scheme,String attributeName){
		return new MarkableNominalAttributeFactory(scheme,attributeName);
	}
	
	/**
	 * Returns a new MarkableFreetextAttributeFactory
	 * @param attributeName The name of the MarkableFreetextAttributeFactory
	 * @return A new MarkableFreetextAttributeFactory
	 */
	public MarkableFreetextAttributeFactory newMarkableFreetextAttributeFactory(Scheme scheme,String attributeName){
		return new MarkableFreetextAttributeFactory(scheme,attributeName);
	}
	
	/**
	 * Returns a new MarkableSetAttributeFactory
	 * @param attributeName The name of the MarkableSetAttributeFactory
	 * @return A new MarkableSetAttributeFactory
	 */
	public MarkableSetAttributeFactory newMarkableSetAttributeFactory(Scheme scheme,String attributeName) {
		return new MarkableSetAttributeFactory(scheme,attributeName);
	}
	
	/**
	 * Returns the corpus to which this Scheme belongs 
	 * @return The corpus to which this Scheme belongs 
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * This class models a Mmax2 Scheme
	 * @author Lionel Nicolas
	 *
	 */
	public class Scheme{
		private SchemeFactory factory; // the factory that has produced the Scheme
		private String schemeName; // the Scheme Name
		private String schemeFilePattern; // the pattern of the files name for this Scheme 
		private Hashtable <String,MarkableAttributeFactory> attributesFactories; // the attribute factories (one for each different attribute)
		
		private Scheme(SchemeFactory factory, String schemeName, String schemeFilePattern){
			this.factory = factory;
			this.schemeName = schemeName;
			this.schemeFilePattern = schemeFilePattern;
			this.attributesFactories = new Hashtable<String, MarkableAttributeFactory>();
		}
		
		/**
		 * Returns the factory with which this Scheme has been created 
		 * @return The factory with which this Scheme has been created 
		 */
		public SchemeFactory getFactory() {
			return factory;
		}
		
		/**
		 * Returns the name of the Scheme 
		 * @return The name of the Scheme 
		 */
		public String getName(){
			return this.schemeName;
		}
		
		/**
		 * Returns the pattern of the file names  of the Scheme 
		 * @return The pattern of the file names  of the Scheme 
		 */
		public String getSchemeFilePattern() {
			return schemeFilePattern;
		}
		

		/** 
		 * Returns all the AttributeFactories of the Scheme
		 * @return All the AttributeFactories of the Scheme
		 */
		public ArrayList<MarkableAttributeFactory> getAttributesFactories() {
			return  new ArrayList<MarkableAttributeFactory>(attributesFactories.values());
		}
		
		/**
		 * Returns an AttributeFactory of the Scheme
		 * @param factoryName The name of the Factory
		 * @return An AttributeFactory of the Scheme
		 */
		public MarkableAttributeFactory getAttributeFactory(String factoryName){
			return this.attributesFactories.get(factoryName);
		}
	
		/**
		 * Adds a MarkableAttributeFactory
		 * @param factory The MarkableAttributeFactory to add
		 */
		public void addMarkableAttributeFactory(MarkableAttributeFactory factory){
			this.attributesFactories.put(factory.getAttributeName(),factory);
		}
		
		/**
		 * Removes a MarkableAttributeFactory
		 * @param factory The MarkableAttributeFactory to remove
		 */
		public void removeMarkableAttributesFactory(MarkableAttributeFactory factory){
			this.attributesFactories.remove(factory.getAttributeName());
		}
		
		
	}

	/**
	 * 
	 * @author Lionel Nicolas
	 *
	 */
	public abstract class MarkableAttributeFactory{
		private Scheme scheme; // the scheme it belongs to
		private String attributeName; // the name of the Attributes produced by the factory
		private String attributeType;
		
		protected MarkableAttributeFactory(Scheme scheme, String attributeName, String attributeType){
			this.scheme = scheme;
			this.attributeName = attributeName;
			this.attributeType = attributeType;
		}
		
		/**
		 * Returns the name of the Attributes produced by the factory
		 * @return The name of the Attributes produced by the factory
		 */
		public String getAttributeName(){
			return this.attributeName;
		}
		
		/**
		 * Returns the type of the Attributes produced by the factory ("MMAX2_POINTER" or "MMAX2_NOMINAL" or "MMAX2_FREETEXT" or "MMAX2_SET")
		 * @return The type of the Attributes produced by the factory
		 */
		public String getAttributeType() {
			return attributeType;
		}
		
		/**
		 * Returns the Scheme of the factory
		 * @return The Scheme of the factory
		 */
		public Scheme getScheme() {
			return scheme;
		}
		
		/** 
		 * Tells if the value can be accepted
		 * @param value The value to test
		 * @return a boolean value that correspond to the answer of the test 
		 */
		public boolean isValueAccepted(String value){
			return true;
		}
		
		/** 
		 * Creates a new MarkableAttribute
		 * @param value
		 * @return A new MarkableAttribute
		 * @throws MMAX2WrapperException
		 */
		public abstract MarkableAttribute newAttribute(String value) throws MMAX2WrapperException;
		
		/**
		 * Clones a MarkableAttributeFactory.
		 */
		public abstract MarkableAttributeFactory clone();
	
		
		/**
		 *  This class models an Attribute of a Markable
		 * @author Lionel Nicolas
		 *
		 */
		public abstract class MarkableAttribute{
			String value; // the value of the MarkableAttribute
			MarkableAttributeFactory factory; // the factory that has created it
			
			protected MarkableAttribute(MarkableAttributeFactory factory, String value) throws MMAX2WrapperException{
				this.factory = factory;
				this.setValue(value);
			}
		
			/**
			 * Returns the name of the attribute
			 * @return The name of the attribute
			 */
			public String getName(){
				return this.factory.getAttributeName();
			}
			
			/**
			 * Returns the value of the attribute
			 * @return The value of the attribute
			 */
			public String getValue(){
				return this.value;
			}
			/**
			 * Sets the value of the attribute
			 * @param value The value to set
			 * @throws MMAX2WrapperException
			 */
			public void setValue(String value) throws MMAX2WrapperException {
				if(this.factory.isValueAccepted(value)){
					this.value = value;
				}else{
					throw new MMAX2WrapperException("Value '"+value+"' cannot be applied to attribute '"+this.factory.attributeName+"'...");
				}
			}
			
			/**
			 * Returns the MarkableAttributeFactory of the attribute
			 * @return The MarkableAttributeFactory of the attribute
			 */
			public MarkableAttributeFactory getFactory() {
				return factory;
			}
		}
	}

	
	public class MarkablePointerAttributeFactory extends MarkableAttributeFactory{
		public static final String pointerType = "MMAX2_POINTER";
		
		private String targetSchemeName; 
		
		public MarkablePointerAttributeFactory(Scheme scheme, String attributeName, String targetSchemeName){
			super(scheme,attributeName, pointerType);
			this.targetSchemeName = targetSchemeName;
		}
		
		public String getTargetSchemeName() {
			return this.targetSchemeName;
		}
		
		public void setTargetSchemeName(String targetSchemeName) {
			this.targetSchemeName = targetSchemeName;
		}
		
		public MarkablePointerAttribute newAttribute(String value) throws MMAX2WrapperException{
			ArrayList<String> valuesStr = new ArrayList<String>();
			String[] values = value.split(";");
			
			for(int i = 0; i< values.length; i++){
				String value_i = values[i];
				if(value_i.contains(":")){
					String[] elems = value_i.split(":");
					value_i = elems[1];
				}
				value_i = value_i.replaceAll(Mmax2Infos.SCHEME_POINTER_ID_PREFIX,"");
				valuesStr.add(value_i);
			}
			String finalStr = StringUtils.join(valuesStr,";");
			return new MarkablePointerAttribute(this,finalStr);
		}
		
		public boolean isValueAccepted(String value){
			String[] values = value.split(";");
			
			for(int i = 0; i< values.length; i++){
				String value_i = values[i];
				if((this.targetSchemeName != null) && (value_i.contains(":"))){
					String[] elems = value_i.split(":");
					if(!elems[0].equals(targetSchemeName)){
						return false;
					}
				}
			}
			return true;
		}
		
		public MarkablePointerAttributeFactory clone() {
			return new MarkablePointerAttributeFactory(this.getScheme(),this.getAttributeName(), this.getTargetSchemeName());
		}
			
		protected class MarkablePointerAttribute extends MarkableAttribute{
			
			protected MarkablePointerAttribute(MarkablePointerAttributeFactory factory, String targetId) throws MMAX2WrapperException{
				super(factory,targetId);
			}
		}

		
	}
	
	public class MarkableNominalAttributeFactory extends MarkableAttributeFactory{
		public static final String nominalType = "MMAX2_NOMINAL";
		
		private Hashtable<String,String> valuesAccepted;
		
		public MarkableNominalAttributeFactory(Scheme scheme, String attributeName){
			super(scheme,attributeName,nominalType);
			this.valuesAccepted = new Hashtable<String,String>();
		}
		
		protected MarkableNominalAttributeFactory(Scheme scheme, String attributeName,ArrayList<String> valuesAcc){
			this(scheme,attributeName);
			for(String valueAccepted: valuesAcc){
				this.valuesAccepted.put(valueAccepted, "");
			}
		}
		
		public MarkableNominalAttribute newAttribute(String value) throws MMAX2WrapperException{
			return new MarkableNominalAttribute(this,value);
		}
		
		public ArrayList<String> getValuesAccepted() {
			return  new ArrayList<String>(valuesAccepted.keySet());
		}
		
		// Disconnected because several attributes can have a same name but different Ids.
		// However in markables, only the name is mentioned... Thus checking if the values does belongs to the set can lead to false alarms...
		//public boolean isValueAccepted(String value){
		//	return this.valuesAccepted.containsKey(value);			
		//}
		
		public void enableValue(String value){
			this.valuesAccepted.put(value,"");
		}
		
		public MarkableNominalAttributeFactory clone() {
			return new MarkableNominalAttributeFactory(this.getScheme(), this.getAttributeName());
		}
		
		public class MarkableNominalAttribute extends MarkableAttribute{
			private MarkableNominalAttribute(MarkableNominalAttributeFactory factory, String value) throws MMAX2WrapperException {
				super(factory, value);
			}
		}
	}
	
	public class MarkableFreetextAttributeFactory extends MarkableAttributeFactory{
		public static final String freetextType = "MMAX2_FREETEXT";
		
		public MarkableFreetextAttributeFactory(Scheme scheme, String attributeName) {
			super(scheme, attributeName,freetextType);
		}
	
		public MarkableFreetextAttribute newAttribute(String value) throws MMAX2WrapperException{
			return new MarkableFreetextAttribute(this,value);
		}
	
		public MarkableFreetextAttributeFactory clone() {
			return new MarkableFreetextAttributeFactory(this.getScheme(), this.getAttributeName());
		}
		
		protected class MarkableFreetextAttribute extends MarkableAttribute{
			private MarkableFreetextAttribute(MarkableFreetextAttributeFactory factory, String value) throws MMAX2WrapperException {
				super(factory, value);
			}
		}
	}
	
	public class MarkableSetAttributeFactory extends MarkableAttributeFactory{
		public static final String setType = "MMAX2_SET";
		
		public MarkableSetAttributeFactory(Scheme scheme, String attributeName) {
			super(scheme,attributeName,setType);
		}
	
		public MarkableSetAttribute newAttribute(String value) throws MMAX2WrapperException{
			return new MarkableSetAttribute(this,value);
		}
		
		public MarkableSetAttributeFactory clone() {
			return new MarkableSetAttributeFactory(this.getScheme(), this.getAttributeName());
		}
		
		protected class MarkableSetAttribute extends MarkableAttribute{
			private MarkableSetAttribute(MarkableSetAttributeFactory factory, String value) throws MMAX2WrapperException {
				super(factory, value);
			}
		}
	}
}


