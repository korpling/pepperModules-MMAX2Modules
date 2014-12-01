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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eurac.commul.annotations.mmax2wrapper.CorpusFactory.Corpus;
import eurac.commul.annotations.mmax2wrapper.DocumentFactory.Document.BaseDataUnit;
import eurac.commul.annotations.mmax2wrapper.MarkableFactory.Markable;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;

/**
 * 
 * @author Lionel Nicolas
 *
 */
public class DocumentFactory {
	
	private Corpus corpus; // The corpus to which the documents produced by the new factory should belong
	private Hashtable<String, MarkableFactory> markablesFactories; // The MarkableFactories for producing the Markables of the document
	protected DocumentBuilder documentBuilder; // The Xml parser to use for parsing Xml files
	 
	/**
	 * Simple constructor => no Xml parsing needed 
	 * @param corpus The corpus to which the documents produced by the new factory should belong
	 */
	public DocumentFactory(Corpus corpus){
		this.corpus = corpus;
		this.markablesFactories = new Hashtable<String, MarkableFactory>();
	}
	
	/**
	 * Full constructor => Xml parsing required 
	 * @param corpus The corpus to which the documents produced by the new factory should belong
	 * @param documentBuilder The Xml parser
	 */
	public DocumentFactory(Corpus corpus, DocumentBuilder documentBuilder){
		this.corpus = corpus;
		this.markablesFactories = new Hashtable<String, MarkableFactory>();
		this.documentBuilder = documentBuilder;
	}
	
	/**
	 * Builds the Document object from the files in the corpus. 
	 * @param documentId
	 * @return
	 * @throws MMAX2WrapperException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document getNewDocument(String documentId) throws MMAX2WrapperException, SAXException, IOException{
		if(this.documentBuilder == null)
			throw new MMAX2WrapperException("To use function 'getNewDocument' a DocumentBuilder needs to be provided at instantiation");
		
		ArrayList<Scheme> schemes = this.corpus.getSchemes();
		for(Scheme scheme: schemes){	
			this.addMarkableFactory(new MarkableFactory(scheme,this.documentBuilder));
		}
		return new Document(documentId,this,this.getBaseDataUnits(documentId),this.getMarkables(documentId));
	}
	

	protected ArrayList<BaseDataUnit> getBaseDataUnits(String documentId) throws MMAX2WrapperException, SAXException, IOException  {
		if(this.documentBuilder == null)
			throw new MMAX2WrapperException("To use function 'getBaseDataUnits' a DocumentBuilder needs to be provided at instantiation");
		
		Corpus corpus = this.getCorpus();
		File corpusPath = corpus.getCorpusPath();
		File documentBaseDataPath = corpus.getBaseDataPath();
		
		File documentFilePath = new File(corpusPath.toString()+ File.separator + documentId + Mmax2Infos.DOCUMENT_FILE_ENDING);
		NodeList nodesDocument = documentBuilder.parse(documentFilePath).getDocumentElement().getChildNodes();
		
		String wordsFilePath = "";
		for(int j = 0; j < nodesDocument.getLength(); j ++){
			Node node = nodesDocument.item(j);
			String nodeName = node.getNodeName();
			if((nodeName != null) && (nodeName.equals(Mmax2Infos.DOCUMENT_WORDS_NODE_NAME))){
					wordsFilePath = node.getTextContent();
					break;
			}
		}
		
		if(wordsFilePath.equals("")){
			throw new MMAX2WrapperException("Document '"+documentId+"' ("+documentFilePath.getAbsolutePath()+") is corrupted: no words file in node '"
					+Mmax2Infos.DOCUMENT_WORDS_NODE_NAME+"' has been defined");
		}
		
		NodeList nodesWords = documentBuilder.parse(documentBaseDataPath+ File.separator + wordsFilePath).getDocumentElement().getChildNodes();
		Document fakeDocument = new Document("",this);
		
		ArrayList<BaseDataUnit> tokens = new ArrayList<BaseDataUnit>();
		for(int j = 0; j < nodesWords.getLength(); j ++){
			Node node = nodesWords.item(j);
			
			String nodeName = node.getNodeName();
			if((nodeName != null) && (nodeName.equals(Mmax2Infos.WORDS_WORD_NODE_NAME))){
				String token = node.getTextContent();
				
				Node idAttributeNode = node.getAttributes().getNamedItem(Mmax2Infos.WORD_ID_ATTR_NAME);
				if(idAttributeNode == null){
					throw new MMAX2WrapperException("BaseDataUnit '"+node.toString()+"' has no '"+Mmax2Infos.WORD_ID_ATTR_NAME+"' attribute defined");
				}
				String idAttribute = idAttributeNode.getNodeValue();
				
				tokens.add(fakeDocument.new BaseDataUnit(idAttribute,token));		
			}
		}
		
		if(tokens.size() == 0){
			throw new MMAX2WrapperException("BaseData of Document '"+documentId+"' in '"+documentBaseDataPath.getAbsolutePath()+"' has no tokens...");
		}
		
		return tokens;
	}
	
	
	protected ArrayList<Markable> getMarkables(String documentId) throws SAXException, IOException, MMAX2WrapperException{			
		Corpus corpus = getCorpus();
		
		ArrayList<Scheme> schemes = corpus.getSchemes();
		ArrayList<Markable> markables = new ArrayList<Markable>();
		
		for(Scheme scheme: schemes){
			MarkableFactory markableFactory = this.getMarkableFactory(scheme);	
			markables.addAll(markableFactory.getMarkables(documentId));
		}
		return markables;
	}
	
	/**
	 * Returns the factory that allows to create markable belonging to a scheme
	 * @param scheme The scheme to which the markables belong
	 * @return A markableFactory
	 */
	public MarkableFactory getMarkableFactory(Scheme scheme){
		return this.markablesFactories.get(scheme.getName());
	}
	
	/**
	 * Adds a Factory to the document
	 * @param factory The factory to add
	 */
	public void addMarkableFactory(MarkableFactory factory){
		this.markablesFactories.put(factory.getScheme().getName(), factory);
	}
	
	/**
	 * Returns the corpus to which the documents produced by the new factory belong
	 * @return The corpus to which the documents produced by the new factory belong
	 */
	public Corpus getCorpus() {
		return this.corpus;
	}
	
	/**
	 * Creates an empty document
	 * @param documentId The id of the document
	 * @return An empty document
	 */
	public Document newDocument(String documentId){
		return new Document(documentId,this);
	}
	
	/**
	 * Creates a non-empty document
	 * @param documentId The id of the document
	 * @param baseDataUnits The baseDataUnits of the document
	 * @param markables The markables of the document
	 * @return a non-empty document
	 */
	
	public Document newDocument(String documentId,ArrayList<BaseDataUnit> baseDataUnits, ArrayList<Markable> markables){
		return new Document(documentId,this,baseDataUnits,markables);
	}
	
	
	/**
	 * This class models a Mmax2 document
	 * @author Lionel Nicolas
	 *
	 */
	public class Document{
		protected String documentId;
		protected DocumentFactory factory;
		protected ArrayList<BaseDataUnit> baseDataUnits;
		protected Hashtable<String,ArrayList<Markable>> markables;

		protected Document(String documentId, DocumentFactory factory){			
			this.documentId = documentId;
			this.factory = factory;
			this.baseDataUnits = new ArrayList<BaseDataUnit>();
			this.markables = new Hashtable<String, ArrayList<Markable>>();
		}
		
		protected Document(String documentId,DocumentFactory factory,ArrayList<BaseDataUnit> baseDataUnits, ArrayList<Markable> markables){
			this(documentId,factory);
			this.baseDataUnits.addAll(baseDataUnits);
			for(Markable markable: markables){
				addMarkable(markable);		
			}
			
		}
	
		/**
		 * Returns the id of the document
		 * @return The id of the document
		 */
		public String getDocumentId() {
			return this.documentId;
		}
		
		/**
		 * Returns the factory that has created this document
		 * @return The factory that has created this document
		 */
		public DocumentFactory getFactory() {
			return factory;
		}
		
		/**
		 * Returns all BaseDataUnits of the document 
		 * @return All BaseDataUnits of the document
		 */
		public ArrayList<BaseDataUnit> getBaseDataUnits() {
			return this.baseDataUnits;
		}
		
		/**
		 * Adds a BaseDataUnit to the document
		 * @param baseDataUnit The BaseDataUnit to add
		 */
		public void addBaseDataUnit(BaseDataUnit baseDataUnit){
			this.baseDataUnits.add(baseDataUnit);
		}
		
		/**
		 * Returns all markables of the document
		 * @return All markables of the document
		 */
		public ArrayList<Markable> getAllMarkables(){
			ArrayList<Markable> todo = new ArrayList<Markable>();
			for(String levelName: this.markables.keySet()){
				todo.addAll(this.markables.get(levelName));
			}
			return todo;
		}
		
		/**
		 *
		 * @param levelName The name of the annotation level
		 * @return all markables of the document belonging to a certain annotation scheme
		 */
		public ArrayList<Markable> getMarkablesOfLevel(String levelName){
			if(this.markables.containsKey(levelName)){
				return this.markables.get(levelName);
			}else{
				return new ArrayList<Markable>();
			}
		}
		
		/**
		 * Adds a markable to the document
		 * @param markable The markable to add
		 */
		public void addMarkable(Markable markable){
			String markableType = markable.getFactory().getScheme().getName();
			
			if(!markables.containsKey(markableType))
				markables.put(markableType,new ArrayList<Markable>());
			
			markables.get(markableType).add(markable);
		}
		
		/**
		 * Removes a markable from the document
		 * @param markable The markable to remove
		 */
		public void removeMarkable(Markable markable){
			markables.get(markable.getFactory().getScheme().getName()).remove(markable);
		}
		
		/**
		 * Produces a new BaseDataUnit
		 * @param unitId The mmax2 id of the unit 
		 * @param unitText
		 * @return The text contained by the unit 
		 */
		
		public BaseDataUnit newBaseDataUnit(String unitId, String unitText){
			return new BaseDataUnit(unitId,unitText);
		}
		
		/**
		 * This class models a basedata unit
		 * @author Lionel Nicolas
		 *
		 */
		public class BaseDataUnit{
			private String unitId; // the mmax2 id of the unit 
			private String unitText; // the text of the unit
			
			private BaseDataUnit(String unitId, String unitText) {
				this.unitId = unitId;
				this.unitText = unitText;
			}
			
			/**
			 * Returns the id of the unit 
			 * @return The id of the unit
			 */
			public String getId() {
				return unitId;
			}
			
			/**
			 * Returns the text of the unit
			 * @return The text of the unit
			 */
			public String getText() {
				return unitText;
			}
		}
		
	}
}
