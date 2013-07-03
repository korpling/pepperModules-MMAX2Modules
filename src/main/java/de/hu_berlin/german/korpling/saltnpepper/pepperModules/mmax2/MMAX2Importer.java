package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperFWException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.MMAX2ImporterException;
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
 *
 */
@Component(name="MMAX2ImporterComponent", factory="PepperImporterComponentFactory")
public class MMAX2Importer extends PepperImporterImpl implements PepperImporter
{
	public MMAX2Importer()
	{
		super();
		
		//setting name of module
		this.name= "MMAX2Importer";
		
		//set list of formats supported by this module
		this.addSupportedFormat("mmax2", "1.0", null);
		
		{//just for logging: to say, that the current module has been loaded
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is created...");
		}//just for logging: to say, that the current module has been loaded
	}

//===================================== start: performance variables
	/**
	 * Measured time which is needed to import the corpus structure. 
	 */
	private Long timeImportSCorpusStructure= 0l;
	/**
	 * Measured total time which is needed to import the document corpus structure. 
	 */
	private Long totalTimeImportSDocumentStructure= 0l;
	/**
	 * Measured time which is needed to load all documents into mmax2 model.. 
	 */
	private Long totalTimeToLoadDocument= 0l;
	/**
	 * Measured time which is needed to map all documents to salt. 
	 */
	private Long totalTimeToMapDocument= 0l;
//===================================== end: performance variables
	
//===================================== start: thread number
	/**
	 * Defines the number of processes which can maximal work in parallel for importing documents.
	 * Means the number of parallel imported documents. Default value is 5.
	 */
	private Integer numOfParallelDocuments= 5;
	/**
	 * Sets the number of processes which can maximal work in parallel for importing documents.
	 * Means the number of parallel imported documents.
	 * @param numOfParallelDocuments the numOfParallelDocuments to set
	 */
	public void setNumOfParallelDocuments(Integer numOfParallelDocuments) {
		this.numOfParallelDocuments = numOfParallelDocuments;
	}

	/**
	 * Returns the number of processes which can maximal work in parallel for importing documents.
	 * Means the number of parallel imported documents.
	 * @return the numOfParallelDocuments
	 */
	public Integer getNumOfParallelDocuments() {
		return numOfParallelDocuments;
	}	
	
	public static final String PROP_NUM_OF_PARALLEL_DOCUMENTS="mmax2Importer.numOfParallelDocuments";
//===================================== start: thread number
	
// ========================== start: flagging for parallel running	
	/**
	 * If true, MMAX2Importer imports documents in parallel.
	 */
	private Boolean RUN_IN_PARALLEL= true;
	/**
	 * @param rUN_IN_PARALLEL the rUN_IN_PARALLEL to set
	 */
	public void setRUN_IN_PARALLEL(Boolean rUN_IN_PARALLEL) {
		RUN_IN_PARALLEL = rUN_IN_PARALLEL;
	}

	/**
	 * @return the RUN_IN_PARALLEL
	 */
	public Boolean getRUN_IN_PARALLEL() {
		return RUN_IN_PARALLEL;
	}
	
	/**
	 * Identifier of properties which contains the maximal number of parallel processed documents. 
	 */
	public static final String PROP_RUN_IN_PARALLEL="mmax2Importer.runInParallel";
// ========================== end: flagging for parallel running
	/**
	 * a property representation of a property file
	 */
	protected Properties props= null;
	
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
		if (this.getCorpusDefinition().getCorpusPath()== null)
			throw new MMAX2ImporterException("Cannot import corpus-structure, because no corpus-path is given.");
		timeImportSCorpusStructure= System.nanoTime();
		
		SCorpus sCorpus = SaltCommonFactory.eINSTANCE.createSCorpus();
		this.sCorpusGraph= sCorpusGraph;
		
		URI corpusUri = this.getCorpusDefinition().getCorpusPath();
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
				throw new PepperModuleException("No documents found for the corpus in '"+corpusUri.toFileString()+"'");
			
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
			if (getLogService()!= null)
			{
				getLogService().log(LogService.LOG_ERROR, "Cannot import corpus structure '"+corpusUri.toFileString()+"'. The reason is: "+exception.getMessage());
			}
			exception.printStackTrace();				
			getPepperModuleController().finish(this.sCorpusGraph.getSElementId());	
		}
		
		timeImportSCorpusStructure= System.nanoTime()- timeImportSCorpusStructure;
	}

	/**
	 * Extracts properties out of given special parameters.
	 */
	private void exctractProperties()
	{
		if (this.getSpecialParams()!= null)
		{//check if flag for running in parallel is set
			File propFile= new File(this.getSpecialParams().toFileString());
			this.props= new Properties();
			try{
				this.props.load(new FileInputStream(propFile));
			}catch (Exception e)
			{throw new MMAX2ImporterException("Cannot find input file for properties: "+propFile+"\n nested exception: "+ e.getMessage());}
			if (this.props.containsKey(PROP_RUN_IN_PARALLEL))
			{
				try {
					Boolean val= new Boolean(this.props.getProperty(PROP_RUN_IN_PARALLEL));
					this.setRUN_IN_PARALLEL(val);
				} catch (Exception e) 
				{
					throw new MMAX2ImporterException("Cannot set correct property value of property "+PROP_RUN_IN_PARALLEL+" to "+this.getName()+", because of the value is not castable to Boolean. A correct value can contain 'true' or 'false'.\n Nested Exception is: "+e.getMessage());
				}
			}
			else if (this.props.containsKey(PROP_NUM_OF_PARALLEL_DOCUMENTS))
			{
				try {
					Integer val= new Integer(this.props.getProperty(PROP_NUM_OF_PARALLEL_DOCUMENTS));
					if (val > 0)
						this.setNumOfParallelDocuments(val);
				} catch (Exception e) 
				{
					throw new MMAX2ImporterException("Cannot set correct property value of property "+PROP_NUM_OF_PARALLEL_DOCUMENTS+" to "+this.getName()+", because of the value is not castable to Integer. A correct value must be a positiv, whole number (>0).\n Nested Exception is: "+e.getMessage());
				}
			}
		}//check if flag for running in parallel is set
	}
	
	/**
	 * ThreadPool
	 */
	private ExecutorService executorService= null;
	
	@Override
	public void start() throws PepperModuleException
	{
		this.mapperRunners= new BasicEList<MapperRunner>();
		{//extracts special parameters
			this.exctractProperties();
		}//extracts special parameters
		{//initialize ThreadPool
			executorService= Executors.newFixedThreadPool(this.getNumOfParallelDocuments());
		}//initialize ThreadPool
		
		boolean isStart= true;
		SElementId sElementId= null;
		while ((isStart) || (sElementId!= null))
		{	
			isStart= false;
			sElementId= this.getPepperModuleController().get();
			if (sElementId== null)
				break;
			
			//call for using push-method
			this.start(sElementId);
		}	
		
		for (MapperRunner mapperRunner: this.mapperRunners)
		{
			mapperRunner.waitUntilFinish();
		}
		this.end();
	}
	
	/**
	 * List of all used mapper runners.
	 */
	private EList<MapperRunner> mapperRunners= null;
	
	/**
	 * This method is called by method start() of superclass PepperImporter, if the method was not overriden
	 * by the current class. If this is not the case, this method will be called for every document which has
	 * to be processed.
	 * @param sElementId the id value for the current document or corpus to process  
	 */
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (	(sElementId!= null) &&
				(sElementId.getSIdentifiableElement()!= null) &&
				((sElementId.getSIdentifiableElement() instanceof SDocument) ||
				((sElementId.getSIdentifiableElement() instanceof SCorpus))))
		{//only if given sElementId belongs to an object of type SDocument or SCorpus	
			if (sElementId.getSIdentifiableElement() instanceof SCorpus)
			{//mapping SCorpus	
			}//mapping SCorpus
			if (sElementId.getSIdentifiableElement() instanceof SDocument)
			{//mapping SDocument
				SDocument sDocument= (SDocument) sElementId.getSIdentifiableElement();
				try {
					MapperRunner mapperRunner= new MapperRunner();
					MMAX22SaltMapper mapper= new MMAX22SaltMapper();
					mapperRunner.mapper= mapper;
					mapperRunner.sDocument= sDocument;
					mapperRunner.documentFactory = new SaltExtendedDocumentFactory(this.corpus,DocumentBuilderFactory.newInstance().newDocumentBuilder());
					mapper.setLogService(this.getLogService());
					
					if (this.getRUN_IN_PARALLEL())
					{//run import in parallel	
						this.mapperRunners.add(mapperRunner);
						this.executorService.execute(mapperRunner);
					}//run import in parallel
					else 
					{//do not run import in parallel
						mapperRunner.start();
					}//do not run import in parallel
					
				} catch (Throwable e) {
					if (getLogService()!= null)
					{						
						getLogService().log(LogService.LOG_ERROR, "Cannot import the SDocument '"+sDocument.getSName()+"'. The reason is: "+e.getMessage());
					}
					e.printStackTrace();				
					getPepperModuleController().finish(sDocument.getSElementId());	
				}
			}//mapping SDocument
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
	}
	
	/**
	 * This class is a container for running MMAX2Mappings in parallel.
	 * @author Administrator
	 *
	 */
	private class MapperRunner implements java.lang.Runnable
	{
		public SDocument sDocument= null;
		SaltExtendedDocumentFactory documentFactory;	
		MMAX22SaltMapper mapper= null;

		/**
		 * Lock to lock await and signal methods.
		 */
		protected Lock lock= new ReentrantLock();
		
		/**
		 * Flag wich says, if mapperRunner has started and finished
		 */
		private Boolean isFinished= false;
		
		/**
		 * If condition is achieved a new SDocument can be created.
		 */
		private Condition finishCondition=lock.newCondition();
		
		public void waitUntilFinish()
		{
			lock.lock();
			try {
				if (!isFinished)
					finishCondition.await();
			} catch (InterruptedException e) {
				throw new PepperFWException(e.getMessage());
			}
			lock.unlock();
		}
		
		@Override
		public void run() 
		{
			start();
		}
		
		/**
		 * starts Mapping of MMAX2 data
		 */
		public void start()
		{
			if (mapper== null)
				throw new MMAX2ImporterException("BUG: Cannot start import, because the mapper is null.");
			if (sDocument== null)
				throw new MMAX2ImporterException("BUG: Cannot start import, because no SDocument object is given.");
		
			SaltExtendedDocument document;
			try {
				document = documentFactory.getNewDocument(sDocument.getSName());
				System.out.println("Document Id"+sDocument.getSName());
				this.mapper.mapSDocument(document,this.sDocument);				
				getPepperModuleController().put(this.sDocument.getSElementId());
			} catch (Exception exception) {
				if (getLogService()!= null)
				{
					getLogService().log(LogService.LOG_ERROR, "Cannot import the SDocument '"+sDocument.getSName()+"'. The reason is: "+exception.getMessage());
				}
				exception.printStackTrace();				
				getPepperModuleController().finish(this.sDocument.getSElementId());	
			}
			
						
			mapper= null;
			this.lock.lock();
			this.isFinished= true;
			this.finishCondition.signal();
			this.lock.unlock();
		}
	}
	
	/**
	 * This method is called by method start() of super class PepperModule. If you do not implement
	 * this method, it will call start(sElementId), for all super corpora in current SaltProject. The
	 * sElementId refers to one of the super corpora. 
	 */
	@Override
	public void end() throws PepperModuleException
	{
		super.end();
		if (this.getLogService()!= null)
		{	
			StringBuffer msg= new StringBuffer();
			msg.append("needed time of "+this.getName()+":\n");
			msg.append("\t time to import whole corpus-structure:\t\t\t\t"+ timeImportSCorpusStructure / 1000000+"\n");
			msg.append("\t total time to import whole document-structure:\t\t"+ totalTimeImportSDocumentStructure / 1000000+"\n");
			msg.append("\t total time to load whole document-structure:\t\t\t"+ totalTimeToLoadDocument / 1000000+"\n");
			msg.append("\t total time to map whole document-structure to salt:\t"+ totalTimeToMapDocument / 1000000+"\n");
			this.getLogService().log(LogService.LOG_DEBUG, msg.toString());
		}
	}
}
