package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import eurac.commul.annotations.mmax2wrapper.CorpusFactory;
import eurac.commul.annotations.mmax2wrapper.Mmax2Infos;
import eurac.commul.annotations.mmax2wrapper.CorpusFactory.Corpus;
import eurac.commul.annotations.mmax2wrapper.DocumentFactory.Document;
import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;
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
		Corpus baseCorpus = super.getCorpus(path);
		String saltInfoPath = path + File.separator + SaltExtendedMmax2Infos.SALT_INFO_FOLDER;

		SaltExtendedCorpus corpus = newCorpus(baseCorpus.getCorpusPath(),
											  baseCorpus.getBaseDataPath(), 
											  baseCorpus.getMarkablesPath(),
											  baseCorpus.getSchemesPath(), 
											  baseCorpus.getStylesPath(),
											  baseCorpus.getCustomizationsPath(),
											  new File(saltInfoPath));
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
	public SaltExtendedCorpus newEmptyCorpus(File corpusPath){
		String baseDataPath = corpusPath.toString() + File.separator + Mmax2Infos.DOCUMENT_BASEDATA_FOLDER;
		String markablesPath = corpusPath.toString() + File.separator + Mmax2Infos.MARKABLES_FOLDER;
		String schemesPath = corpusPath.toString() + File.separator + Mmax2Infos.SCHEMES_FOLDER;
		String stylesPath = corpusPath.getAbsolutePath() + File.separator + Mmax2Infos.STYLES_FOLDER;
		String customizationsPath = corpusPath.getAbsolutePath() + File.separator + Mmax2Infos.CUSTOMIZATIONS_FOLDER;
		String saltInfoPath = corpusPath.toString() + File.separator + SaltExtendedMmax2Infos.SALT_INFO_FOLDER;
		return newCorpus(corpusPath, new File(baseDataPath),new File(markablesPath),new File(schemesPath),new File(stylesPath),new File(customizationsPath), new File(saltInfoPath));
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
	public SaltExtendedCorpus newCorpus(File corpusPath, File baseDataPath,File markablesPath,File schemesPath, File stylesPath, File customizationsPath, File saltInfoPath){
		return new SaltExtendedCorpus(corpusPath, baseDataPath,markablesPath,schemesPath,stylesPath,customizationsPath,saltInfoPath);
	}
	
	/**
	 * This class represents a Mmax2 corpus enhanced with Salt information
	 * @author Lionel Nicolas
	 *
	 */
	public class SaltExtendedCorpus extends Corpus{
		private File saltInfoPath; // the path of the folder containing the salt informations
		
		protected SaltExtendedCorpus(File corpusPath, File baseDataPath, File markablesPath, File schemesPath, File stylesPath, File customizationsPath,File saltInfoPath) {		
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
		public File getSaltInfoPath() {
			return saltInfoPath;
		}
	}
}
