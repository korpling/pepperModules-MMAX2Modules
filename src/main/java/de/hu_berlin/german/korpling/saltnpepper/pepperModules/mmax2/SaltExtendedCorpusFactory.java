package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.URI;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import eurac.commul.annotations.mmax2wrapper.CorpusFactory;
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
		SaltExtendedCorpus corpus = newCorpus();
		getCorpus(corpus,path);
		return corpus;
	}
	
	public SaltExtendedCorpus newCorpus(){
		return new SaltExtendedCorpus();
	}
	
	public class SaltExtendedCorpus extends Corpus{
		
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
	}
}
