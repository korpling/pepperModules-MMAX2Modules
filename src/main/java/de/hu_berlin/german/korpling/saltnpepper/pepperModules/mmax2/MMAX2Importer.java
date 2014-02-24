package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * This importer reads a corpus in MMAX2 format and maps it to a SALT corpus. 
 * The mapping of each document is done in a separate thread. The code has been 
 * initially adapted from the PAULAImporter
 * 
 * @author Lionel Nicolas
 * @author Florian Zipser (just adaptations)
 *
 */
@Component(name="MMAX2ImporterComponent", factory="PepperImporterComponentFactory")
public class MMAX2Importer extends PepperImporterImpl implements PepperImporter
{
//	private static final Logger logger= LoggerFactory.getLogger(MMAX2Importer.class); 
	
	public MMAX2Importer()
	{
		super();
		
		//setting name of module
		setName("MMAX2Importer");
		//set list of formats supported by this module
		addSupportedFormat("mmax2", "1.0", null);
	}
	
	/**
	 * Stores relation between documents and their resource 
	 */
	private SCorpusGraph sCorpusGraph= null;
		
	private SaltExtendedCorpus corpus;

	private File corpusPath;

	/**
	 * This method is called by Pepper at the start of conversion process. 
	 * It shall create the structure the corpus to import. That means creating all necessary SCorpus, 
	 * SDocument and all Relation-objects between them. The path tp the corpus to import is given by
	 * this.getCorpusDefinition().getCorpusPath().
	 * @param an empty graph given by Pepper, which shall contains the corpus structure
	 */
	@Override
	public void importCorpusStructure(SCorpusGraph sCorpusGraph)
			throws PepperModuleException 
	{
		if (this.getCorpusDesc().getCorpusPath()== null)
			throw new PepperModuleException(this, "Cannot import corpus-structure, because no corpus-path is given.");
		
		SCorpus sCorpus = SaltCommonFactory.eINSTANCE.createSCorpus();
		this.sCorpusGraph= sCorpusGraph;
		
		URI corpusUri = this.getCorpusDesc().getCorpusPath();
		try {
			SaltExtendedCorpusFactory factory = new SaltExtendedCorpusFactory(DocumentBuilderFactory.newInstance().newDocumentBuilder());
			
			String currentPath = corpusUri.toFileString();
			
			this.corpusPath = new File(currentPath);
			this.corpus = factory.getCorpus(corpusUri.toFileString()) ;
			
			sCorpus.setSName(this.corpusPath.getName());
			SElementId sCorpusId= SaltCommonFactory.eINSTANCE.createSElementId();
			sCorpusId.setSId(this.corpusPath.getName());
			sCorpus.setSElementId(sCorpusId);
					
			this.sCorpusGraph.setSName(this.corpusPath.getName()+"_graph");
			sCorpus.setSCorpusGraph(this.sCorpusGraph);
			
			ArrayList<String> documentsIds = factory.getDocumentIds(corpusUri.path());
			if(documentsIds.size() == 0)
				throw new PepperModuleException(this, "No documents found for the corpus in '"+corpusUri.toFileString()+"'");
			
			for(String documentId: documentsIds){
			
				SElementId sDocumentId= SaltCommonFactory.eINSTANCE.createSElementId();
				sDocumentId.setSId(documentId);
				SDocument sDocument= SaltCommonFactory.eINSTANCE.createSDocument();
				sDocument.setSName(documentId);
				sDocument.setSElementId(sDocumentId);
				
				this.sCorpusGraph.addSDocument(sCorpus, sDocument);
				
				SDocumentGraph sDocumentGraph = SaltCommonFactory.eINSTANCE.createSDocumentGraph();
				sDocument.setSDocumentGraph(sDocumentGraph);
			}
		} catch (Exception exception) {
			throw new PepperModuleException(this, "Cannot import corpus structure '"+corpusUri.toFileString()+"'. The reason is: ", exception);	
		}
	}
	
	private SaltExtendedDocumentFactory saltExtendedDocumentFactory= null;
	/**
	 * Returns a {@link SaltExtendedCorpusFactory} object as a singleton instance.
	 * @return
	 */
	protected synchronized SaltExtendedDocumentFactory getSaltExtendedDocumentFactory(){
		if (saltExtendedDocumentFactory== null){
			try {
				saltExtendedDocumentFactory= new SaltExtendedDocumentFactory(this.corpus,DocumentBuilderFactory.newInstance().newDocumentBuilder());
			} catch (ParserConfigurationException e) {
				throw new PepperModuleException(this, "Cannot create SaltExtendedDocumentFactory because of nested exception. ", e);
			}
		}
		return(saltExtendedDocumentFactory);
	}
	
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId) {
		MMAX22SaltMapper mapper= new MMAX22SaltMapper();
		
		if (sElementId.getSIdentifiableElement() instanceof SDocument){
			SDocument sDocument= (SDocument) sElementId.getSIdentifiableElement();
			try {
				SaltExtendedDocument extendedDocument= getSaltExtendedDocumentFactory().getNewDocument(sDocument.getSName());
				mapper.setDocument(extendedDocument);
			} catch (Exception e) {
				throw new PepperModuleException(this, "Cannot create mmax2 document for SDocument '"+sElementId.getSId()+"' because of nested exception.", e);
			}
		}else if (sElementId.getSIdentifiableElement() instanceof SCorpus){
			mapper.setSCorpus((SCorpus)sElementId.getSIdentifiableElement());
		}
		return(mapper);
	}
}
