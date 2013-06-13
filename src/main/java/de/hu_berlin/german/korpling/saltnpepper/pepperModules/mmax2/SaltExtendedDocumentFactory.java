package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkable;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.SaltExtendedMMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.CorpusFactory.Corpus;
import eurac.commul.annotations.mmax2wrapper.DocumentFactory;
import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.MarkableFactory;
import eurac.commul.annotations.mmax2wrapper.MarkableFactory.Markable;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;

/**
 * @author Lionel Nicolas
 */
public class SaltExtendedDocumentFactory extends DocumentFactory {
	
	private SaltExtendedCorpus corpus;// The corpus to which the documents produced by the factory shall belong
	
	/**
	 * Simple constructor => no Xml file parsing needed
	 * @param corpus The corpus to which the documents produced by the factory shall belong
	 */
	public SaltExtendedDocumentFactory(SaltExtendedCorpus corpus){
		super(corpus);
		this.corpus = corpus;
	}
	
	/**
	 * Full constructor => Xml file parsing required
	 * @param corpus The corpus to which the documents produced by the factory shall belong
	 * @param documentBuilder the Xml parser to use
	 */
	public SaltExtendedDocumentFactory(SaltExtendedCorpus corpus, DocumentBuilder documentBuilder) {
		super(corpus,documentBuilder);
		this.corpus = corpus;
	}
	
	/**
	 * Parses the files of an Mmax2 document enhanced with Salt information and builds an object accordingly
	 * @param documentId The id of the document to parse
	 * @return An object representing an Mmmax2 document enhanced with SAlt information
	 */
	public SaltExtendedDocument getNewDocument(String documentId) throws MMAX2WrapperException, SAXException, IOException{
		if(this.documentBuilder == null)
			throw new MMAX2WrapperException("To use function 'getNewDocument' a DocumentBuilder needs to be provided at instantiation");
		
		ArrayList<Scheme> schemes = this.corpus.getSchemes();
		for(Scheme scheme: schemes){	
			this.addMarkableFactory(new SaltExtendedMarkableFactory(scheme,this.documentBuilder));
		}
		return new SaltExtendedDocument(documentId,this,this.getBaseDataUnits(documentId),this.getSaltExtendedMarkables(documentId));
	}
	
	protected ArrayList<SaltExtendedMarkable> getSaltExtendedMarkables(String documentId) throws SAXException, IOException, MMAX2WrapperException {
		if(this.documentBuilder == null)
			throw new MMAX2WrapperException("To use function 'getSaltExtendedMarkables' a DocumentBuilder needs to be provided at instantiation");
		
		ArrayList<SaltExtendedMarkable> markables = new ArrayList<SaltExtendedMarkable>();
		
		File saltInfoFile = new File(this.corpus.getSaltInfoPath() + File.separator + documentId + SaltExtendedMmax2Infos.SALT_INFO_FILE_ENDING);	
		Hashtable<String, Hashtable <String,String>> saltInfos = new Hashtable<String, Hashtable<String,String>>();
		if(saltInfoFile.exists()){
			NodeList nodes = this.documentBuilder.parse(saltInfoFile).getDocumentElement().getChildNodes();
			for(int i = 0; i < nodes.getLength(); i ++){	
				Node xmlNode = nodes.item(i);
				String nodeName = xmlNode.getNodeName();
				
				if((nodeName == null) || (!nodeName.equals(SaltExtendedMmax2Infos.SALT_INFO_NODE_NAME))){
					continue;
				}
				NamedNodeMap attributes = xmlNode.getAttributes();
				Hashtable<String,String> saltInfoMarkable = new Hashtable<String, String>();
				
				Node idAttributeNode = attributes.getNamedItem(SaltExtendedMmax2Infos.SALT_INFO_ID_ATTR_NAME);
				if(idAttributeNode == null){
					throw new SaltExtendedMMAX2WrapperException("Salt information '"+xmlNode.toString()+" in File '"+saltInfoFile+"' has no '"+SaltExtendedMmax2Infos.SALT_INFO_ID_ATTR_NAME+"' attribute defined");
				}
				String idAttribute = idAttributeNode.getNodeValue();
					
				Node sTypeAttributeNode = attributes.getNamedItem(SaltExtendedMmax2Infos.SALT_INFO_STYPE_ATTR_NAME);
				if(sTypeAttributeNode == null){
					throw new SaltExtendedMMAX2WrapperException("Salt information '"+xmlNode.toString()+" in File '"+saltInfoFile+"' has no '"+SaltExtendedMmax2Infos.SALT_INFO_STYPE_ATTR_NAME+"' attribute defined");
				}
				String sTypeAttribute = sTypeAttributeNode.getNodeValue();
				saltInfoMarkable.put("SType", sTypeAttribute);
				
				if(sTypeAttribute.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER)){
					Node containedIdAttributeNode = attributes.getNamedItem(SaltExtendedMmax2Infos.SALT_INFO_CONTAINED_ID_ATTR_NAME);
					if(containedIdAttributeNode == null){
						throw new SaltExtendedMMAX2WrapperException("Salt information '"+xmlNode.toString()+" on a SContained in File '"+saltInfoFile+"' has no '"+SaltExtendedMmax2Infos.SALT_INFO_CONTAINED_ID_ATTR_NAME+"' attribute defined");
					}
					saltInfoMarkable.put("ContainedId", containedIdAttributeNode.getNodeValue());
				}
				
				Node sNameAttributeNode = attributes.getNamedItem(SaltExtendedMmax2Infos.SALT_INFO_SNAME_ATTR_NAME);
				if(sNameAttributeNode == null){
					throw new SaltExtendedMMAX2WrapperException("Salt information '"+xmlNode.toString()+" in File '"+saltInfoFile+"' has no '"+SaltExtendedMmax2Infos.SALT_INFO_SNAME_ATTR_NAME+"' attribute defined");
				}
				String sNameAttribute = sNameAttributeNode.getNodeValue();
				saltInfoMarkable.put("SName", sNameAttribute);
				
				Node sidAttributeNode = attributes.getNamedItem(SaltExtendedMmax2Infos.SALT_INFO_SID_ATTR_NAME);
				if(sidAttributeNode == null){
					throw new SaltExtendedMMAX2WrapperException("Salt information '"+xmlNode.toString()+" in File '"+saltInfoFile+"' has no '"+SaltExtendedMmax2Infos.SALT_INFO_SID_ATTR_NAME+"' attribute defined");
				}
				String sIdAttribute = sidAttributeNode.getNodeValue();
				saltInfoMarkable.put("SId", sIdAttribute);
				
				saltInfos.put(idAttribute, saltInfoMarkable);
			}
		}
		
		ArrayList<Scheme> schemes = this.corpus.getSchemes();
		for(Scheme scheme: schemes){				
			SaltExtendedMarkableFactory markableFactory = this.getMarkableFactory(scheme);
			markables.addAll(markableFactory.getSaltExtendedMarkables(documentId,saltInfos));
		}
		return markables;
	}

	/**
	 * Creates a new empty document
	 * @return A new empty document
	 */
	public SaltExtendedDocument newDocument(String documentId){
		return new SaltExtendedDocument(documentId,this);
	}

	/**
	 * Returns the factory that allows to create markable belonging to a scheme
	 * @param scheme The scheme to which the markables belong
	 * @return A SaltExtendedMarkableFactory
	 */
	public SaltExtendedMarkableFactory getMarkableFactory(Scheme scheme) {
		return (SaltExtendedMarkableFactory) super.getMarkableFactory(scheme);
	}
	
	/**
	 * Adds a Factory to the document
	 * @param factory The factory to add
	 */
	public void addMarkableFactory(SaltExtendedMarkableFactory factory){
		super.addMarkableFactory(factory);
	}
	
	/**
	 * Returns the corpus to which the document belongs to
	 * @return The corpus to which the document belongs to
	 */
	public SaltExtendedCorpus getCorpus() {
		return this.corpus;
	}
	
	
	/**
	 * This class models an Mmax2 document enhanced with SAlt information
	 * @author Lionel Nicolas
	 *
	 */
	public class SaltExtendedDocument extends Document{
		
		protected SaltExtendedDocument(String documentId, SaltExtendedDocumentFactory factory){
			super(documentId,factory);	
		}
		
		protected SaltExtendedDocument(String documentId,SaltExtendedDocumentFactory factory,ArrayList<BaseDataUnit> baseDataUnits, ArrayList<SaltExtendedMarkable> markables){
			super(documentId,factory,baseDataUnits,new ArrayList<Markable>());
			for(SaltExtendedMarkable markable: markables){
				addMarkable(markable);		
			}
		}
		
		/**
		 * Returns all markables of the document
		 * @return All markables of the document
		 */
		public ArrayList<SaltExtendedMarkable> getAllSaltExtendedMarkables(){
			ArrayList<SaltExtendedMarkable> results = new ArrayList<SaltExtendedMarkable>();
			for(Markable markable:  super.getAllMarkables())
				results.add((SaltExtendedMarkable) markable);
		
			return results;
		}
		
		/**
		 * Returns the factory that has created this document
		 * @return The factory that has created this document
		 */
		public SaltExtendedDocumentFactory getFactory(){
			return (SaltExtendedDocumentFactory) this.factory;
		}
		
		/**
		 * Adds a markable to the document
		 * @param markable The markable to add
		 */
		public void addMarkable(SaltExtendedMarkable markable){
			super.addMarkable(markable);
		}
		
		/**
		 * Removes a markable from the document
		 * @param markable The markable to remove
		 */
		public void removeMarkable(SaltExtendedMarkable markable){
			super.removeMarkable(markable);
		}
		
		/**
		 *
		 * @param levelName The name of the annotation level
		 * @return all markables of the document belonging to a certain annotation scheme
		 */
		public ArrayList<SaltExtendedMarkable> getSaltExtendedMarkablesOfLevel(String levelName){
			ArrayList<SaltExtendedMarkable> results = new ArrayList<SaltExtendedMarkable>();
			for(Markable markable: this.markables.get(levelName))
				results.add((SaltExtendedMarkable) markable);
			
			
			return results;
		}
	}

}
