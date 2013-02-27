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


public class SaltExtendedCorpusFactory extends CorpusFactory{
	
	public SaltExtendedCorpusFactory() {
		super();
	}
	
	
	public SaltExtendedCorpusFactory(DocumentBuilder documentBuilder) {
		super(documentBuilder);
	}

	public SaltExtendedCorpus getCorpusBase(String path) throws SAXException, IOException, MMAX2WrapperException, ParserConfigurationException{
		Corpus baseCorpus = getCorpus(path);
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
	
	public SaltExtendedCorpus newEmptyCorpus(File corpusPath){
		String baseDataPath = corpusPath.toString() + File.separator + Mmax2Infos.DOCUMENT_BASEDATA_FOLDER;
		String markablesPath = corpusPath.toString() + File.separator + Mmax2Infos.MARKABLES_FOLDER;
		String schemesPath = corpusPath.toString() + File.separator + Mmax2Infos.SCHEMES_FOLDER;
		String stylesPath = corpusPath.getAbsolutePath() + File.separator + Mmax2Infos.STYLES_FOLDER;
		String customizationsPath = corpusPath.getAbsolutePath() + File.separator + Mmax2Infos.CUSTOMIZATIONS_FOLDER;
		String saltInfoPath = corpusPath.toString() + File.separator + SaltExtendedMmax2Infos.SALT_INFO_FOLDER;
		return newCorpus(corpusPath, new File(baseDataPath),new File(markablesPath),new File(schemesPath),new File(stylesPath),new File(customizationsPath), new File(saltInfoPath));
	}
	
	
	public SaltExtendedCorpus newCorpus(File corpusPath, File baseDataPath,File markablesPath,File schemesPath, File stylesPath, File customizationsPath, File saltInfoPath){
		return new SaltExtendedCorpus(corpusPath, baseDataPath,markablesPath,schemesPath,stylesPath,customizationsPath,saltInfoPath);
	}
	
	public class SaltExtendedCorpus extends Corpus{
		private File saltInfoPath;
		
		protected SaltExtendedCorpus(File corpusPath, File baseDataPath, File markablesPath, File schemesPath, File stylesPath, File customizationsPath,File saltInfoPath) {		
			super(corpusPath,baseDataPath, markablesPath,schemesPath,stylesPath,customizationsPath);
			this.saltInfoPath = saltInfoPath;
		}

		public ArrayList<SaltExtendedDocument> getSaltExtendedDocuments(){
			ArrayList<Document> documents = super.getDocuments();
			ArrayList<SaltExtendedDocument> results = new ArrayList<SaltExtendedDocument> ();
			for(Document document : documents)
				results.add((SaltExtendedDocument) document);
			
			return results;
		}		
		
		public SaltExtendedDocument getDocument(String documentId){
			return (SaltExtendedDocument) super.getDocument(documentId);
		}
		
		public synchronized void addDocument(SaltExtendedDocument document){
			super.addDocument(document);
		}
		
		public File getSaltInfoPath() {
			return saltInfoPath;
		}
	}
}
