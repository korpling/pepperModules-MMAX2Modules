package eurac.commul.annotations.mmax2wrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableAttributeFactory.MarkableAttribute;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;

/**
 * 
 * @author Lionel Nicolas
 *
 */
public class MarkableFactory{
		
	protected Scheme scheme; // The scheme that the Markable produced by this factory should respect
	protected DocumentBuilder documentBuilder; // The Xml parser to use for parsing Xml files
	
	/**
	 * Simple constructor => no Xml parsing required
	 * @param scheme The scheme that the Markable produced by this factory should respect
	 */
	public MarkableFactory(Scheme scheme){
		this.scheme = scheme;
	}
	
	/**
	 * Full constructor => Xml parsing required 
	 * @param scheme The scheme that the Markable produced by this factory should respect
	 * @param documentBuilder The Xml parser to use for parsing Xml files
	 */
	public MarkableFactory(Scheme scheme, DocumentBuilder documentBuilder){
		this.scheme = scheme;
		this.documentBuilder = documentBuilder;
	}
	
	/**
	 * Parse all Markables Xml files of an Mmax2 document and creates all Markables object accordingly
	 * @param documentId The id of the document
	 * @return
	 * @throws MMAX2WrapperException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected ArrayList<Markable> getMarkables(String documentId) throws MMAX2WrapperException, SAXException, IOException{
		if(this.documentBuilder == null)
			throw new MMAX2WrapperException("To use function 'getMarkables' a DocumentBuilder needs to be provided at instantiation");
		
		File markablesPath = this.scheme.getFactory().getCorpus().getMarkablesPath();
		ArrayList<Markable> markables = new ArrayList<Markable>();
		
		String markableFilePath =  markablesPath.toString() + File.separator + scheme.getSchemeFilePattern().replaceAll(Pattern.quote("$"), documentId);
		File markableFile = new File(markableFilePath);
		if(!markableFile.exists()){
			return markables;
		}
			
		NodeList nodes = documentBuilder.parse(markableFile).getDocumentElement().getChildNodes();
		for(int i = 0; i < nodes.getLength(); i ++){
			Node node = nodes.item(i);
			String nodeName = node.getNodeName();
			if((nodeName != null) && (nodeName.equals(Mmax2Infos.MARKABLE_NODE_NAME))){
				Markable markable = this.getMarkable(node);
				markables.add(markable);
			}
		}
		
		return markables;		
	}

	// creates a Markable Object from an Xml entry in a Markable file
	private Markable getMarkable(Node xmlNode) throws MMAX2WrapperException{
		NamedNodeMap attributes = xmlNode.getAttributes();
		
		Node idAttributeNode = attributes.getNamedItem(Mmax2Infos.MARKABLE_ID_ATTR_NAME);
		if(idAttributeNode == null){
			throw new MMAX2WrapperException("Markable '"+xmlNode.toString()+"' has no '"+ Mmax2Infos.MARKABLE_ID_ATTR_NAME +"' attribute defined");
		}
		String idAttribute = idAttributeNode.getNodeValue().replaceAll("markable_", "");
		attributes.removeNamedItem(Mmax2Infos.MARKABLE_ID_ATTR_NAME);
		
		Node spanAttributeNode = attributes.getNamedItem(Mmax2Infos.MARKABLE_SPAN_ATTR_NAME);
		if(spanAttributeNode == null){
			throw new MMAX2WrapperException("Markable '"+xmlNode.toString()+"' has no '"+ Mmax2Infos.MARKABLE_SPAN_ATTR_NAME +"' attribute defined");
		}
		String spanAttribute = spanAttributeNode.getNodeValue();
		attributes.removeNamedItem(Mmax2Infos.MARKABLE_SPAN_ATTR_NAME);
		
		// Some samples provided with mmax2 have no "mmax2_level" attributes in their markables files => the attribute is this optionnal
		// when you come to think about it is logic since it is redundant from the common path.
		//Node levelAttributeNode = attributes.getNamedItem(Mmax2Infos.MARKABLE_LEVEL_ATTR_NAME);
		//if(levelAttributeNode == null){
		//	throw new MMAX2WrapperException("Markable '"+xmlNode.toString()+"' has no '"+ Mmax2Infos.MARKABLE_LEVEL_ATTR_NAME +"' attribute defined");
		//}
		//String levelAttribute = levelAttributeNode.getNodeValue();
		//attributes.removeNamedItem(Mmax2Infos.MARKABLE_LEVEL_ATTR_NAME);
			
		//if(!this.scheme.getName().toLowerCase().equals(levelAttribute.toLowerCase())){
		//	throw new MMAX2WrapperException("Markable '"+xmlNode.toString()+"' has a level attribute '"+ levelAttribute	+"' that does not match scheme name '"+this.scheme.getName()+"'");
		//}
		
		Markable markable = newMarkable(idAttribute, spanAttribute);
		for(int i = 0; i < attributes.getLength(); i++){
			Node attributeNode = attributes.item(i);
			if(!attributeNode.getNodeName().equals(Mmax2Infos.MARKABLE_LEVEL_ATTR_NAME)){// This attribute is optionnal
				MarkableAttributeFactory markableAttributeFactory = this.scheme.getAttributeFactory(attributeNode.getNodeName().toLowerCase());
				if(markableAttributeFactory == null){
					throw new MMAX2WrapperException("Attribute '"+attributeNode.getNodeName()+"' is not defined in scheme '"+scheme.getName()+"'");
				}
				markable.addAttribute(markableAttributeFactory.newAttribute(attributeNode.getNodeValue()));
			}
		}
		return markable;		
	}
	
	private Markable newMarkable(String id, String span){
		return new Markable(this,id,span);
	}
	
	/**
	 * Returns the Scheme that the Markable produced by this factory should respect
	 * @return The Scheme that the Markable produced by this factory should respect
	 */
	public Scheme getScheme(){
		return this.scheme;
	}
		
	public class Markable{
		private String id; // the id of the Markable
		private String span; // the span of the Markable

		private MarkableFactory factory; // the factory that has produced the Markable	
		private Hashtable<String,MarkableAttribute> attributes; // the attributes of the Markable
		
		// basic constructor => no attributes specified by default
		protected Markable(MarkableFactory factory, String id, String span){
			this.id = id;
			this.span = span;
			this.factory = factory;
			this.attributes = new Hashtable<String,MarkableAttribute>();
		}
		
		// full constructor => attributes provided as parameters
		protected Markable(MarkableFactory factory, String id, String span, ArrayList<MarkableAttribute> attributes){
			this(factory,id,span);
			for(MarkableAttribute attribute : attributes){
				this.addAttribute(attribute);
			}
		}
		
		/**
		 * Returns the id of the Markable
		 * @return The id of the Markable
		 */
		public String getId() {
			return this.id;
		}
	
		/**
		 * Returns the span of the Markable
		 * @return The span of the Markable
		 */
		public String getSpan() {
			return this.span;
		}
		
		/**
		 * Add a span to the Markable
		 * @param newSpan The span to add
		 */
		public void addSpan(String newSpan){
			if(this.span.equals("")){
				this.span = newSpan;
			}else{
				this.span = this.span + ","+newSpan;
			}
		}
		
		/**
		 * Set the span of a Markable
		 * @param span 
		 */
		public void setSpan(String span) {
			this.span = span;		
		}
		
		/**
		 * Returns the MarkableFactory that has created the Markable 
		 * @return The MarkableFactory that has created the Markable 
		 */
		public MarkableFactory getFactory(){
			return this.factory;
		}
	
		/**
		 * Returns a specific MarkableAttribute of the Markable
		 * @param attributeName The name of the MarkableAttribute
		 * @return the MarkableAttribute requested
		 */
		public MarkableAttribute getAttribute(String attributeName) {
			return this.attributes.get(attributeName);
		}
		
		/**
		 * Returns all MarkableAttributes of the Markable
		 * @return the MarkableAttributes of the Markable
		 */
		public ArrayList<MarkableAttribute> getAttributes() {
			return new ArrayList<MarkableAttribute>(this.attributes.values());
		}		
		
		/** 
		 * Adds a MarkableAttribute to the Markable
		 * @param attribute The MarkableAttribute to add
		 */
		public void addAttribute(MarkableAttribute attribute){
			this.attributes.put(attribute.getName(),attribute);
		}
		
		/**
		 * Removes a MarkableAttribute from the Markable
		 * @param attribute The MarkableAttribute to remove 
		 */
		public void removeAttribute(MarkableAttribute attribute){
			this.attributes.remove(attribute.getName());
		}
		
		public String toString(){
			ArrayList<String> attributesStrs = new ArrayList<String>();
			for(MarkableAttribute attribute: this.attributes.values()){
				attributesStrs.add(attribute.getName()+"='"+attribute.getValue()+"'");
			}
			Collections.sort(attributesStrs);
			return "[Markable id='"+this.id+"' level='"+this.factory.scheme.getName()+"' span='"+this.span+"'"+StringUtils.join(attributesStrs," ")+"]"; 
		}
		
		
	}
}


	



