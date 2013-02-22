package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkable;
import eurac.commul.annotations.mmax2wrapper.CorpusFactory.Corpus;
import eurac.commul.annotations.mmax2wrapper.DocumentFactory;
import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.MarkableFactory.Markable;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;

public class SaltExtendedDocumentFactory extends DocumentFactory {
	
	public SaltExtendedDocumentFactory(Corpus corpus) throws ParserConfigurationException {
		super(corpus);
	}
	
	public SaltExtendedDocumentFactory(Corpus corpus, DocumentBuilder documentBuilder) {
		super(corpus,documentBuilder);
	}
	
	public SaltExtendedDocument getNewDocument(String documentId, File corpusPath) throws MMAX2WrapperException, SAXException, IOException, ParserConfigurationException{
		return new SaltExtendedDocument(documentId,this,this.getBaseDataUnits(documentId, corpusPath),this.getSaltExtendedMarkables(documentId, corpusPath));
	}
	
	protected ArrayList<SaltExtendedMarkable> getSaltExtendedMarkables(String documentId, File corpusPath) throws SAXException, IOException, ParserConfigurationException, MMAX2WrapperException{
		ArrayList<SaltExtendedMarkable> markables = new ArrayList<SaltExtendedMarkable>();
		
		ArrayList<Scheme> schemes = this.corpus.getSchemes();
		for(Scheme scheme: schemes){				
			SaltExtendedMarkableFactory markableFactory = this.getMarkableFactory(scheme);
			markables.addAll(markableFactory.getSaltExtendedMarkables(documentId,corpusPath));
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
	}

}
