package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.SaltExtendedMMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.CorpusFactory;
import eurac.commul.annotations.mmax2wrapper.DocumentFactory.Document;
import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.Mmax2Infos;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;

/**
 * 
 * @author Lionel Nicolas
 *
 */
public class SaltExtendedCorpusFactory extends CorpusFactory{
	
	/**
	 * Simple constructor => no Xml parsing required
	 */
	public SaltExtendedCorpusFactory(){
		super();
	}
	
	/**
	 * Full constructor => Xml parsing required
	 * @param documentBuilder The Xml parser 
	 */
	public SaltExtendedCorpusFactory(DocumentBuilder documentBuilder){
		super(documentBuilder);
	}
	
	/**
	 *  Parse a the files of a Mmmax2 corpus enhanced with Salt information and builds an object accordingly
	 *  @param path The path of the corpus
	 *  @return a SaltExtendedCorpus representing the corpus parsed 
	 */
	public SaltExtendedCorpus getCorpus(String path) throws SAXException, IOException, MMAX2WrapperException{
		if (path== null)
			throw new SaltExtendedMMAX2WrapperException("Path to corpus was not found.");
		Corpus baseCorpus = super.getCorpus(path);
		String saltInfoPath = SaltExtendedMmax2Infos.SALT_INFO_FOLDER;

		SaltExtendedCorpus corpus = newCorpus(baseCorpus.getCorpusPath(),
											  baseCorpus.getBaseDataPath(), 
											  baseCorpus.getMarkablesPath(),
											  baseCorpus.getSchemesPath(), 
											  baseCorpus.getStylesPath(),
											  baseCorpus.getCustomizationsPath(),
											  saltInfoPath);
		for(Scheme scheme: baseCorpus.getSchemes()){
			corpus.addScheme(scheme);
		}
		return corpus;
	}
	
	/**
	 * Creates a new Empty corpus with all default paths
	 * @param corpusPath The root path of the corpus
	 * @return A new empty corpus
	 */
	public SaltExtendedCorpus newEmptyCorpus(String corpusPath){
		String baseDataPath = Mmax2Infos.DOCUMENT_BASEDATA_FOLDER;
		String markablesPath = Mmax2Infos.MARKABLES_FOLDER;
		String schemesPath = Mmax2Infos.SCHEMES_FOLDER;
		String stylesPath = Mmax2Infos.STYLES_FOLDER;
		String customizationsPath = Mmax2Infos.CUSTOMIZATIONS_FOLDER;
		String saltInfoPath = SaltExtendedMmax2Infos.SALT_INFO_FOLDER;
		return newCorpus(corpusPath, baseDataPath,markablesPath,schemesPath,stylesPath,customizationsPath,saltInfoPath);
	}
	
	/**
	 * Creates a new Empty corpus with all corresponding paths
	 * @param corpusPath
	 * @param baseDataPath
	 * @param markablesPath
	 * @param schemesPath
	 * @param stylesPath
	 * @param customizationsPath
	 * @param saltInfoPath
	 * @return A new empty corpus
	 */
	public SaltExtendedCorpus newCorpus(String corpusPath, String baseDataPath,String markablesPath,String schemesPath, String stylesPath, String customizationsPath, String saltInfoPath){
		return new SaltExtendedCorpus(corpusPath, baseDataPath,markablesPath,schemesPath,stylesPath,customizationsPath,saltInfoPath);
	}
	
	/**
	 * This class represents a Mmax2 corpus enhanced with Salt information
	 * @author Lionel Nicolas
	 *
	 */
	public class SaltExtendedCorpus extends Corpus{
		private String saltInfoPath; // the path of the folder containing the salt informations
		
		protected SaltExtendedCorpus(String corpusPath, String baseDataPath, String markablesPath, String schemesPath, String stylesPath, String customizationsPath,String saltInfoPath) {		
			super(corpusPath,baseDataPath, markablesPath,schemesPath,stylesPath,customizationsPath);
			this.saltInfoPath = saltInfoPath;
		}

		/**
		 * Returns all documents in the corpus
		 * @return
		 */
		public ArrayList<SaltExtendedDocument> getSaltExtendedDocuments(){
			ArrayList<Document> documents = super.getDocuments();
			ArrayList<SaltExtendedDocument> results = new ArrayList<SaltExtendedDocument> ();
			for(Document document : documents)
				results.add((SaltExtendedDocument) document);
			
			return results;
		}		
		
		/**
		 * Returns a document of the corpus
		 * @param documentId The id of the document to return
		 * @return A document of the corpus
		 */
		public SaltExtendedDocument getDocument(String documentId){
			return (SaltExtendedDocument) super.getDocument(documentId);
		}
		
		/**
		 * Adds a document to the corpus
		 * @param document The SAltExtendedDocument to add
		 */
		public synchronized void addDocument(SaltExtendedDocument document){
			super.addDocument(document);
		}
		
		
		/**
		 * Returns the path to the folder containing the Salt informations 
		 * @return
		 */
		public String getSaltInfoPath() {
			return saltInfoPath;
		}
	}
}
