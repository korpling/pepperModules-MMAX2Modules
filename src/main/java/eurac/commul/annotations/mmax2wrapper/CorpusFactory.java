package eurac.commul.annotations.mmax2wrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eurac.commul.annotations.mmax2wrapper.DocumentFactory.Document;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;

/**
 * @author Lionel Nicolas
 *
 */
public class CorpusFactory{
	
	private DocumentBuilder documentBuilder; // The Xml parser to use for parsing Xml files
	
	/**
	 * Simple constructor => no Xml parsing required
	 */
	public CorpusFactory(){}
	
	/**
	 * Full constructor => Xml parsing required
	 * @param documentBuilder The Xml parser 
	 */
	public CorpusFactory(DocumentBuilder documentBuilder){
		this.documentBuilder = documentBuilder;
	}
	
	/**
	 * Parse a whole Mmax2 corpus and builds accordingly a corresponding object 
	 * @param path The path of the Mmax2 corpus to parse
	 * @return a Corpus object
	 * @throws SAXException
	 * @throws IOException
	 * @throws MMAX2WrapperException
	 */
	
    public Corpus getCorpus(String path) throws SAXException, IOException, MMAX2WrapperException{
		if(this.documentBuilder == null)
			throw new MMAX2WrapperException("To use function 'getCorpus' a DocumentBuilder needs to be provided at instantiation");
		
		Node baseDataNode = null;
		Node markablesNode = null;
		Node schemesNode = null;
		Node stylesNode = null;
		Node customizationsNode = null;
		Node annotationsNode = null;	
		Node userSwitches = null;		
		{
			NodeList nodes = documentBuilder.parse(new File(path +File.separator+Mmax2Infos.COMMON_PATH_FILE)).getDocumentElement().getChildNodes();
			for(int i = 0; i < nodes.getLength(); i ++){
				Node node = nodes.item(i);
				String nodeName = node.getNodeName();
				if(nodeName != null){
					if(nodeName.equals(Mmax2Infos.COMMON_PATH_BASEDATA_PATH_NODE_NAME)){
						baseDataNode = node;
					}else if(nodeName.equals(Mmax2Infos.COMMON_PATH_MARKABLE_PATH_NODE_NAME)){
						markablesNode = node;
					}else if(nodeName.equals(Mmax2Infos.COMMON_PATH_SCHEME_PATH_NODE_NAME)){
						schemesNode = node;
					}else if(nodeName.equals(Mmax2Infos.COMMON_PATH_STYLE_PATH_NODE_NAME)){
						stylesNode = node;
					}else if(nodeName.equals(Mmax2Infos.COMMON_PATH_CUSTOMIZATION_PATH_NODE_NAME)){
						customizationsNode = node;
					}else if(nodeName.equals(Mmax2Infos.COMMON_PATH_ANNOTATIONS_NODE_NAME)){
						annotationsNode = node;
					}else if(nodeName.equals(Mmax2Infos.COMMON_PATH_USER_SWITCHES_NODE_NAME)){
						userSwitches = node;
					}
					
				}
			}
		}
		
		if(baseDataNode == null){
			throw new MMAX2WrapperException("Common path file  '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
					+"' is corrupted: no basedata node '"+Mmax2Infos.COMMON_PATH_BASEDATA_PATH_NODE_NAME+"' has been defined");
		}
		if(markablesNode ==  null){
			throw new MMAX2WrapperException("Common path file  '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
					+"' is corrupted: no markables path node '"+Mmax2Infos.COMMON_PATH_MARKABLE_PATH_NODE_NAME+"' has been defined");
		}
		if(schemesNode == null){
			throw new MMAX2WrapperException("Common path file  '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
					+"' is corrupted: no schemes path node '"+Mmax2Infos.COMMON_PATH_SCHEME_PATH_NODE_NAME+"' has been defined");
		}
		if(stylesNode ==  null){
			throw new MMAX2WrapperException("Common path file  '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
					+"' is corrupted: no style path node '"+Mmax2Infos.COMMON_PATH_STYLE_PATH_NODE_NAME+"' has been defined");
		}
		if(customizationsNode ==  null){
			throw new MMAX2WrapperException("Common path file  '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
					+"' is corrupted: no customizations path node '"+Mmax2Infos.COMMON_PATH_CUSTOMIZATION_PATH_NODE_NAME+"' has been defined");
		}
		if(annotationsNode ==  null){
			throw new MMAX2WrapperException("Common path file  '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
					+"' is corrupted: no annotations node '"+Mmax2Infos.COMMON_PATH_ANNOTATIONS_NODE_NAME+"' has been defined");
		}
		
		
		String baseDatePath = path;
		if(!baseDataNode.getTextContent().equals("")){
			baseDatePath = baseDatePath + File.separator + baseDataNode.getTextContent();
		}
		
		String markablesPath = path;
		if(!markablesNode.getTextContent().equals("")){
			markablesPath = markablesPath + File.separator + markablesNode.getTextContent();
		}
		
		String schemesPath = path;
		if(!schemesNode.getTextContent().equals("")){
			schemesPath = schemesPath + File.separator + schemesNode.getTextContent();
		}
		
		String stylesPath = path;
		if(!stylesNode.getTextContent().equals("")){
			stylesPath = stylesPath + File.separator + stylesNode.getTextContent();
		}
		
		String customizationsPath = path;
		if(!customizationsNode.getTextContent().equals("")){
			customizationsPath = customizationsPath + File.separator + customizationsNode.getTextContent();
		}
		
		Corpus corpus = newCorpus(new File(path),new File(baseDatePath),new File(markablesPath),new File(schemesPath),new File(stylesPath),new File(customizationsPath));
		
		if(userSwitches != null){
			NodeList nodes = userSwitches.getChildNodes();
			for(int i = 0; i < nodes.getLength(); i ++){
				Node node = nodes.item(i);
				String nodeName = node.getNodeName();
				if(nodeName != null){
					if(nodeName.equals(Mmax2Infos.COMMON_PATH_USER_SWITCH_NODE_NAME)){
						NamedNodeMap attributes = node.getAttributes();
						
						Node userSwitchNameAttribute = attributes.getNamedItem(Mmax2Infos.COMMON_PATH_USER_SWITCH_NAME_ATTR_NAME);
						if(userSwitchNameAttribute == null){
							throw new MMAX2WrapperException("Common path file '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
									+"' is corrupted, user switch'"+node+"' has no name attribute '"
									+Mmax2Infos.COMMON_PATH_USER_SWITCH_NAME_ATTR_NAME+"'");
						}
						String userSwitchName = userSwitchNameAttribute.getNodeValue();
						
						Node userSwitchDefaultAttribute = attributes.getNamedItem(Mmax2Infos.COMMON_PATH_USER_SWITCH_ACTIVE_ATTR_NAME);
						if(userSwitchDefaultAttribute == null){
							throw new MMAX2WrapperException("Common path file '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
									+"' is corrupted, level '"+node+"' has no attribute '"
									+Mmax2Infos.COMMON_PATH_USER_SWITCH_ACTIVE_ATTR_NAME+"'");
						}
						Boolean active = new Boolean((userSwitchDefaultAttribute.getNodeValue().equals("on"))? true:false);
						corpus.addUserSwitch(userSwitchName,active);
					}
				}
			}
		}
		
		SchemeFactory schemeFactory = new SchemeFactory(corpus, this.documentBuilder);
		ArrayList<Node> levelsNodes = new ArrayList<Node>();
		{
			NodeList nodes = annotationsNode.getChildNodes();
			for(int i = 0; i < nodes.getLength(); i ++){
				Node node = nodes.item(i);
				String nodeName = node.getNodeName();
				if(nodeName != null){
					if(nodeName.equals(Mmax2Infos.COMMON_PATH_LEVEL_NODE_NAME)){
						levelsNodes.add(node);
					}
				}
			}
		}
		if(levelsNodes.size() == 0){
			throw new MMAX2WrapperException("Common path file  '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
					+"' is corrupted: no levels in node '"+Mmax2Infos.COMMON_PATH_ANNOTATIONS_NODE_NAME+"' has been defined");
		}
		
		File[] schemesFiles = new  File(schemesPath).listFiles();
		Hashtable<String, Boolean> schemesFileHash = new Hashtable<String, Boolean>();
		for(int i = 0; i < schemesFiles.length; i++){
			schemesFileHash.put(schemesFiles[i].getName(), false);
		}
		
		for(Node levelNode: levelsNodes){
			NamedNodeMap attributes = levelNode.getAttributes();
			
			Node schemeNameAttribute = attributes.getNamedItem(Mmax2Infos.COMMON_PATH_LEVEL_SCHEME_NAME_ATTR_NAME);
			if(schemeNameAttribute == null){
				throw new MMAX2WrapperException("Common path file '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
						+"' is corrupted, level'"+levelNode+"' has no scheme name attribute '"
						+Mmax2Infos.COMMON_PATH_LEVEL_SCHEME_NAME_ATTR_NAME+"'");
			}
			String schemeName = schemeNameAttribute.getNodeValue();
			
			Node schemeAttribute = attributes.getNamedItem(Mmax2Infos.COMMON_PATH_LEVEL_SCHEME_ATTR_NAME);
			if(schemeAttribute == null){
				throw new MMAX2WrapperException("Common path file '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
						+"' is corrupted, level '"+schemeName+"' has no attribute '"
						+Mmax2Infos.COMMON_PATH_LEVEL_SCHEME_ATTR_NAME+"'");
			}
			String schemePath = schemeAttribute.getNodeValue();
			if(!schemesFileHash.containsKey(schemePath)){
				throw new MMAX2WrapperException("Common path file '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
						+"' is corrupted, level '"+schemeName+"' designates a scheme file '"
						+schemePath+"' that has not been found in '"+schemesPath+"'");
			}
			schemesFileHash.put(schemePath,true);
			
			String schemePattern = levelNode.getTextContent();
			if((schemePattern == null) || (schemePattern.equals(""))){
				throw new MMAX2WrapperException("Common path file '"+Mmax2Infos.COMMON_PATH_FILE+"' in '"+path
						+"' is corrupted, level '"+schemeName+"' has no file patern defined");
			}
		
			corpus.addScheme(schemeFactory.getScheme(schemeName,schemePath,schemePattern));
		}
		return corpus;
	}
	
    /**
     * Returns all ids of the document in the files of the corpus
     * @param path
     * @return All ids of the document in the files of the corpus
     */
	public ArrayList<String> getDocumentIds(String path){
		ArrayList<String> files = new ArrayList<String>();
		File[] documentsFiles = new File(path).listFiles();
		for(int i = 0; i < documentsFiles.length; i++){
			File currentFile = documentsFiles[i];
			if(!currentFile.isFile()){
				continue;
			}
			String filePath = currentFile.getName();
			if(filePath.endsWith(".mmax")){
				files.add(filePath.replaceAll(".mmax$",""));
			}
		}
		
		return files;
	}
	
	/**
	 * Creates a new Empty corpus 
	 * @param corpusPath The path where to store the corpus if outputting it 
	 * @return
	 */
	public Corpus newEmptyCorpus(File corpusPath){
		String baseDataPath = corpusPath.getAbsolutePath() + File.separator + Mmax2Infos.DOCUMENT_BASEDATA_FOLDER;
		String markablesPath = corpusPath.getAbsolutePath() + File.separator + Mmax2Infos.MARKABLES_FOLDER;
		String schemesPath = corpusPath.getAbsolutePath() + File.separator + Mmax2Infos.SCHEMES_FOLDER;
		String stylesPath = corpusPath.getAbsolutePath() + File.separator + Mmax2Infos.STYLES_FOLDER;
		String customizationsPath = corpusPath.getAbsolutePath() + File.separator + Mmax2Infos.CUSTOMIZATIONS_FOLDER;
		return newCorpus(corpusPath, new File(baseDataPath),new File(markablesPath),new File(schemesPath),new File(stylesPath),new File(customizationsPath));
	}
	
	private Corpus newCorpus(File corpusPath, File baseDataPath,File markablesPath,File schemesPath, File stylesPath,File customizationsPath){
		return new Corpus(corpusPath, baseDataPath,markablesPath,schemesPath,stylesPath,customizationsPath);
	}
	
	/**
	 * This class models a Mmax2 corpus
	 * @author Lionel Nicolas
	 *
	 */
	public class Corpus {
		private File corpusPath; // The root path of the corpus
		private File baseDataPath; // The basedata folder in the root path of the corpus
		private File markablesPath; // The markables folder in the root path of the corpus
		private File schemesPath; // The schemes folder in the root path of the corpus
		private File customizationsPath; // The customizations folder in the root path of the corpus
		private File stylesPath; // The styles folder in the root path of the corpus
		private Hashtable<String,Boolean> userSwitches; // The userswitches of the corpus
		private Hashtable<String,Document> documents; // The Document objects representing the documents of the corpus
		protected Hashtable<String,Scheme> schemes; // The Scheme objects representing the schemes of the corpus
		
		
		protected Corpus(File corpusPath, File baseDataPath,File markablesPath,File schemesPath, File stylesPath,File customizationsPath){
			this.corpusPath = corpusPath;
			this.baseDataPath = baseDataPath;
			this.markablesPath = markablesPath;
			this.schemesPath = schemesPath;
			this.stylesPath = stylesPath;
			this.customizationsPath = customizationsPath;
			this.userSwitches = new Hashtable<String, Boolean>();
			this.documents = new Hashtable<String,Document>();
			this.schemes = new Hashtable<String, Scheme>();
		}
		
		/**
		 * Returns the documents of the corpus 
		 * @return The documents of the corpus 
		 */
		public ArrayList<Document> getDocuments() {
			return new ArrayList<Document> (documents.values());
		}
		
		/**
		 * Returns the schemes of the corpus 
		 * @return The schemes of the corpus 
		 */
		public ArrayList<Scheme> getSchemes() {
			return new ArrayList<Scheme>(this.schemes.values());
		}
		
		/**
		 * Returns the userSwitches of the corpus 
		 * @return The userSwitches of the corpus 
		 */
		public Hashtable<String, Boolean> getuserSwitches() {
			return this.userSwitches;
		}
		
		/**
		 * Returns a specific document of the corpus
		 * @param documentId The id of the document to return
		 * @return A specific document of the corpus
		 */
		public Document getDocument(String documentId){
			return this.documents.get(documentId);
		}
		
		/**
		 * Returns a specific scheme of the corpus
		 * @param schemeId The id of the scheme to return
		 * @return A specific scheme of the corpus
		 */
		public Scheme getScheme(String schemeId){
			return this.schemes.get(schemeId);
		}
		
		/**
		 * Returns a the status of a specific userSwitch of the corpus
		 * @param userSwitchName The name of the userSwitch 
		 * @return The status of a specific userSwitch of the corpus
		 */
		public Boolean getUserSwitchStatus(String userSwitchName){
			return this.userSwitches.get(userSwitchName);
		}
		
		/**
		 * Returns the root path of the corpus
		 * @return The root path of the corpus
		 */
		public File getCorpusPath() {
			return corpusPath;
		}
		
		/**
		 * Returns the basedata folder path of the corpus
		 * @return The basedata folder path of the corpus
		 */	
		public File getBaseDataPath() {
			return baseDataPath;
		}
		
		/**
		 * Returns the markables folder path of the corpus
		 * @return The markables folder path of the corpus
		 */	
		public File getMarkablesPath() {
			return markablesPath;
		}
		
		/**
		 * Returns the schemes folder path of the corpus
		 * @return The schemes folder path of the corpus
		 */	
		public File getSchemesPath() {
			return schemesPath;
		}
		
		/**
		 * Returns the customizations folder path of the corpus
		 * @return The customizations folder path of the corpus
		 */	
		public File getCustomizationsPath() {
			return customizationsPath;
		}
		
		/**
		 * Returns the styles folder path of the corpus
		 * @return The styles folder path of the corpus
		 */	
		public File getStylesPath() {
			return stylesPath;
		}
		
		/**
		 * Add a Scheme
		 * @param scheme The Scheme to add
		 */
		public synchronized void addScheme(Scheme scheme){
			this.schemes.put(scheme.getName(), scheme);
		}
		
		/**
		 * Add an userSwitch
		 * @param userSwitchName The name of the userSwitch to add
		 * @param active The status of the userSwitch to add
		 */
		public synchronized void addUserSwitch(String userSwitchName, boolean active){
			this.userSwitches.put(userSwitchName, active);
		}
		
		/** 
		 * Add a document
		 * @param document The Document to add
		 */
		
		public synchronized void addDocument(Document document){
			this.documents.put(document.getDocumentId(), document);
		}
		
		
		/**
		 * Remove a Scheme
		 * @param scheme The Scheme to remove
		 */
		public synchronized void removeScheme(Scheme scheme){
			this.schemes.remove(scheme.getName());
		}
		
		/**
		 * Remove an userSwitch
		 * @param userSwitchName The name of the userSwitch to remove
		 * @param active The status of the userSwitch to add
		 */
		public synchronized void removeUserSwitch(String userSwitchName){
			this.userSwitches.remove(userSwitchName);
		}
		
		/** 
		 * Remove a document
		 * @param document The Document to remove
		 */
		
		public synchronized void removeDocument(Document document){
			this.documents.remove(document.getDocumentId());
		}
		
	}
}
