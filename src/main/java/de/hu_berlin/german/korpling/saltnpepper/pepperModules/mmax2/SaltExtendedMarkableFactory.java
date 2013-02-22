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

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.SaltExtendedMMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.MarkableFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableAttributeFactory.MarkableAttribute;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;


public class SaltExtendedMarkableFactory extends MarkableFactory{
	
	public SaltExtendedMarkableFactory(Scheme scheme){
		super(scheme);
	}
	
	public SaltExtendedMarkableFactory(Scheme scheme,DocumentBuilder documentBuilder){
		super(scheme,documentBuilder);
	}
	
	public ArrayList<SaltExtendedMarkable> getSaltExtendedMarkables(String documentId, File corpusPath) throws IOException, SAXException, ParserConfigurationException, MMAX2WrapperException {
		if(this.documentBuilder == null)
			throw new SaltExtendedMMAX2WrapperException("To use function 'getCorpus' a DocumentBuilder needs to be provided at instantiation");
		
		Hashtable<String,Markable> correspondance = new Hashtable<String,Markable>();	
		for(Markable markable : super.getMarkables(documentId,corpusPath)){
			correspondance.put(markable.getId(), markable);			
		}
		
		File saltInfoFile = new File(corpusPath.toString()+ File.separator + SaltExtendedMmax2Infos.SALT_INFO_FOLDER + File.separator+ documentId + SaltExtendedMmax2Infos.SALT_INFO_FILE_ENDING);	
		
		ArrayList<SaltExtendedMarkable> results = new ArrayList<SaltExtendedMarkable>();
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
					
				Node sLayerAttributeNode = attributes.getNamedItem(SaltExtendedMmax2Infos.SALT_INFO_SLAYER_ATTR_NAME);
				if(sLayerAttributeNode == null){
					throw new SaltExtendedMMAX2WrapperException("Salt information '"+xmlNode.toString()+" in File '"+saltInfoFile+"' has no '"+SaltExtendedMmax2Infos.SALT_INFO_SLAYER_ATTR_NAME+"' attribute defined");
				}
				String sLayerAttributeId = sLayerAttributeNode.getNodeValue();
				
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
				
				if(correspondance.containsKey(idAttribute)){
					Markable markable = correspondance.get(idAttribute);
					correspondance.remove(idAttribute);
					results.add(new SaltExtendedMarkable(this,idAttribute, markable.getSpan(), markable.getAttributes(), sLayerAttributeId, sTypeAttribute,sNameAttribute, sIdAttribute));
				}
			}
		}
		for(Markable markable: correspondance.values()){
			results.add(new SaltExtendedMarkable(this,markable.getId(), markable.getSpan(), markable.getAttributes()));
		}

		return results;	
	}
	
	public SaltExtendedMarkable newMarkable(String id, String span, ArrayList<MarkableAttribute> attributes, String sLayerId, String sType, String sName, String sId){
		return new SaltExtendedMarkable(this, id, span, attributes, sLayerId, sType, sName, sId);
	}
	
	public class SaltExtendedMarkable extends Markable {
		private boolean hasSaltInformation;
		private String sId;
		private String sLayerId;
		private String sType;
		private String sName;
		
		protected SaltExtendedMarkable(SaltExtendedMarkableFactory factory, String id, String span, ArrayList<MarkableAttribute> attributes){
			super(factory,id,span,attributes);
			this.hasSaltInformation = false;
		}
		
		protected SaltExtendedMarkable(SaltExtendedMarkableFactory factory, String id, String span, ArrayList<MarkableAttribute> attributes, String sLayerId, String sType, String sName, String sId){
			super(factory,id,span,attributes);
			this.hasSaltInformation = true;
			this.sLayerId = sLayerId;
			this.sType = sType;
			this.sName = sName;
			this.sId = sId;
		}
		
		public String getSName() {
			return this.sName;
		}
		
		public String getSLayerId(){
			return this.sLayerId;
		}
		
		public String getSType() {
			return sType;
		}
		
		public String getSId() {
			return sId;
		}
		
		public boolean hasSaltInformation() {
			return this.hasSaltInformation;
		}
	}
}
