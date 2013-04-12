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
	
	public ArrayList<SaltExtendedMarkable> getSaltExtendedMarkables(String documentId, File markablesPath, Hashtable<String,Hashtable<String,String>> saltInfos) throws IOException, SAXException, ParserConfigurationException, MMAX2WrapperException {
		ArrayList<SaltExtendedMarkable> results = new ArrayList<SaltExtendedMarkable>();
		for(Markable markable : super.getMarkables(documentId,markablesPath)){
			if(saltInfos.containsKey(markable.getId())){
				Hashtable<String,String> saltInfoMarkable = saltInfos.get(markable.getId());
				results.add(new SaltExtendedMarkable(this,markable.getId(), markable.getSpan(), markable.getAttributes(), saltInfoMarkable.get("SType"),saltInfoMarkable.get("SName"), saltInfoMarkable.get("SId")));	
			}else{
				results.add(new SaltExtendedMarkable(this,markable.getId(), markable.getSpan(), markable.getAttributes()));
			}
		}
		return results;	
	}
	
	public SaltExtendedMarkable newMarkable(String id, String span, ArrayList<MarkableAttribute> attributes, String sType, String sName, String sId){
		return new SaltExtendedMarkable(this, id, span, attributes, sType, sName, sId);
	}
	
	public class SaltExtendedMarkable extends Markable {
		private boolean hasSaltInformation;
		private String sId;
		private String sType;
		private String sName;
		
		protected SaltExtendedMarkable(SaltExtendedMarkableFactory factory, String id, String span, ArrayList<MarkableAttribute> attributes){
			super(factory,id,span,attributes);
			this.hasSaltInformation = false;
		}
		
		protected SaltExtendedMarkable(SaltExtendedMarkableFactory factory, String id, String span, ArrayList<MarkableAttribute> attributes, String sType, String sName, String sId){
			super(factory,id,span,attributes);
			this.hasSaltInformation = true;
			this.sType = sType;
			this.sName = sName;
			this.sId = sId;
		}
		
		public String getSName() {
			return this.sName;
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
