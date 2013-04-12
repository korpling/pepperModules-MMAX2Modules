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
import eurac.commul.annotations.mmax2wrapper.MarkableFactory.Markable;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;

public class SaltExtendedDocumentFactory extends DocumentFactory {
	private SaltExtendedCorpus corpus;
	
	
	public SaltExtendedDocumentFactory(SaltExtendedCorpus corpus) throws ParserConfigurationException {
		super(corpus);
		this.corpus = corpus;
	}
	
	public SaltExtendedDocumentFactory(SaltExtendedCorpus corpus, DocumentBuilder documentBuilder) {
		super(corpus,documentBuilder);
		this.corpus = corpus;
	}
	
	public SaltExtendedDocument getNewDocument(String documentId) throws MMAX2WrapperException, SAXException, IOException, ParserConfigurationException{
		return new SaltExtendedDocument(documentId,this,this.getBaseDataUnits(documentId, this.corpus.getCorpusPath(), this.corpus.getBaseDataPath()),this.getSaltExtendedMarkables(documentId));
	}
	
	protected ArrayList<SaltExtendedMarkable> getSaltExtendedMarkables(String documentId) throws SAXException, IOException, ParserConfigurationException, MMAX2WrapperException{
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
				
				Node sNameAttributeNode = attributes.getNamedItem(SaltExtendedMmax2Infos.SALT_INFO_SNAME_ATTR_NAME);
				if(sNameAttributeNode == null){
					throw new SaltExtendedMMAX2WrapperException("Salt information '"+xmlNode.toString()+" in File '"+saltInfoFile+"' has no '"+SaltExtendedMmax2Infos.SALT_INFO_SNAME_ATTR_NAME+"' attribute defined");
				}
				String sNameAttribute = sNameAttributeNode.getNodeValue();
				
				Node sidAttributeNode = attributes.getNamedItem(SaltExtendedMmax2Infos.SALT_INFO_SID_ATTR_NAME);
				if(sidAttributeNode == null){
					throw new SaltExtendedMMAX2WrapperException("Salt information '"+xmlNode.toString()+" in File '"+saltInfoFile+"' has no '"+SaltExtendedMmax2Infos.SALT_INFO_SID_ATTR_NAME+"' attribute defined");
				}
				String sIdAttribute = sidAttributeNode.getNodeValue();
				
				Hashtable<String,String> saltInfoMarkable = new Hashtable<String, String>();
				saltInfoMarkable.put("SType", sTypeAttribute);
				saltInfoMarkable.put("SName", sNameAttribute);
				saltInfoMarkable.put("SId", sIdAttribute);
				saltInfos.put(idAttribute, saltInfoMarkable);
			}
		}
		
		ArrayList<Scheme> schemes = this.corpus.getSchemes();
		for(Scheme scheme: schemes){				
			SaltExtendedMarkableFactory markableFactory = this.getMarkableFactory(scheme);
			markables.addAll(markableFactory.getSaltExtendedMarkables(documentId,this.corpus.getMarkablesPath(),saltInfos));
		}
		return markables;
	}

	public SaltExtendedDocument newDocument(String documentId){
		return new SaltExtendedDocument(documentId,this);
	}

	public SaltExtendedMarkableFactory getMarkableFactory(Scheme scheme) {
		return (SaltExtendedMarkableFactory) super.getMarkableFactory(scheme);
	}
	
	protected SaltExtendedMarkableFactory createNewMarkableFactory(Scheme scheme){
		return new SaltExtendedMarkableFactory(scheme,this.documentBuilder);		
	}	
	
	public void addMarkableFactory(SaltExtendedMarkableFactory factory){
		super.addMarkableFactory(factory);
	}
	
	public Corpus getCorpus() {
		return corpus;
	}
	
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
		
		public ArrayList<SaltExtendedMarkable> getAllSaltExtendedMarkables(){
			ArrayList<SaltExtendedMarkable> results = new ArrayList<SaltExtendedMarkable>();
			for(Markable markable:  super.getAllMarkables())
				results.add((SaltExtendedMarkable) markable);
		
			return results;
		}
		
		public SaltExtendedDocumentFactory getFactory(){
			return (SaltExtendedDocumentFactory) this.factory;
		}
		
		public void addMarkable(SaltExtendedMarkable markable){
			super.addMarkable(markable);
		}
		
		public void removeMarkable(SaltExtendedMarkable markable){
			super.removeMarkable(markable);
		}
		
		public ArrayList<SaltExtendedMarkable> getSaltExtendedMarkablesOfLevel(String levelName){
			ArrayList<SaltExtendedMarkable> results = new ArrayList<SaltExtendedMarkable>();
			for(Markable markable: this.markables.get(levelName))
				results.add((SaltExtendedMarkable) markable);
			
			
			return results;
		}
		
		public SaltExtendedCorpus getCorpus(){
			return corpus;
		}
	}

}
