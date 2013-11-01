package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.log.LogService;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperFWException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperExporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperExporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.MMAX2ExporterException;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory;


/**
 * This class exports data from Salt to the MMAX2 format.
 * The code has been adapted from PAULAExporter.
 * 
 * @author Lionel Nicolas
 * 
 */
@Component(name="MMAX2ExporterComponent", factory="PepperExporterComponentFactory")
public class MMAX2Exporter extends PepperExporterImpl implements PepperExporter
{
	private SaltExtendedCorpus corpus;
	private SchemeFactory schemeFactory;
	
	public MMAX2Exporter()
	{
		super();
		
		//setting name of module
		this.name= "MMAX2Exporter";
				
		//set list of formats supported by this module
		this.addSupportedFormat("mmax2", "1.0", null);
	}

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
	
	public static final String PROP_NUM_OF_PARALLEL_DOCUMENTS="MMAX2Exporter.numOfParallelDocuments";
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
	public static final String PROP_RUN_IN_PARALLEL="mmax2Exporter.runInParallel";
	
	public static final String MATCHING_CONDITIONS="MMAX2Exporter.matchingConditionsFilePath";
	public static final String POINTERS_MATCHING_CONDITIONS="MMAX2Exporter.pointersMatchingConditionsFilePath";
	private String matchingConditionsFilePath = null;
	private String pointersMatchingConditionsFilePath = null;
	
// ========================== end: flagging for parallel running
	/**
	 * a property representation of a property file
	 */
	protected Properties props= null;
	
	/**
	 * Extracts properties out of given special parameters.
	 */
	private void exctractProperties()
	{
		if (this.getSpecialParams()!= null)
		{//check if flag for running in parallel is set
			File propFile= new File(this.getSpecialParams().toFileString());
			this.props= new Properties();
			InputStream in= null;
			try{
				in= new FileInputStream(propFile);
				this.props.load(in);
			}catch (Exception e)
			{
				throw new MMAX2ExporterException("Cannot find input file for properties: "+propFile+".\n Nested exception: "+ e.getMessage());
			}
			finally
			{
				if (in != null)
				{
					try {
						in.close();
					} catch (IOException e) {
						throw new MMAX2ExporterException("Cannot close stream for file '"+props+".\n Nested exception: "+ e.getMessage());
					}
				}
			}
			if (this.props.containsKey(PROP_RUN_IN_PARALLEL))
			{
				try {
					Boolean val= new Boolean(this.props.getProperty(PROP_RUN_IN_PARALLEL));
					this.setRUN_IN_PARALLEL(val);
				} catch (Exception e) 
				{
					throw new MMAX2ExporterException("Cannot set correct property value of property "+PROP_RUN_IN_PARALLEL+" to "+this.getName()+", because of the value is not castable to Boolean. A correct value can contain 'true' or 'false'.\n Nested exception: "+ e.getMessage());
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
					throw new MMAX2ExporterException("Cannot set correct property value of property "+PROP_NUM_OF_PARALLEL_DOCUMENTS+" to "+this.getName()+", because of the value is not castable to Integer. A correct value must be a positiv, whole number (>0).\n Nested exception: "+ e.getMessage());
				}
			}
			
			if (this.props.containsKey(MATCHING_CONDITIONS))
			{
				matchingConditionsFilePath= this.props.getProperty(MATCHING_CONDITIONS);
				
			}
			
			if (this.props.containsKey(POINTERS_MATCHING_CONDITIONS))
			{
				pointersMatchingConditionsFilePath= this.props.getProperty(POINTERS_MATCHING_CONDITIONS);
				
			}
		}//check if flag for running in parallel is set
	}
	
	
	
	/**
	 * ThreadPool
	 */
	private ExecutorService executorService= null;
	
	@Override
	public void start() throws MMAX2ExporterException
	{

		if (this.getCorpusDefinition().getCorpusPath()== null)
			throw new MMAX2ExporterException("Cannot export SaltProject, because no corpus path is given.");
		if (this.getSaltProject()== null)
			throw new MMAX2ExporterException("Cannot export SaltProject, because it is null.");
		if (	(this.getSaltProject().getSCorpusGraphs()== null)||
				(this.getSaltProject().getSCorpusGraphs().size()== 0))
			throw new MMAX2ExporterException("Cannot export SaltProject, no SCorpusGraphs are given.");
		
		{//extracts special parameters
			this.exctractProperties();
		}//extracts special parameters
		
		String ressourcePath = this.getResources().toFileString();
		ressourcePath = ressourcePath.concat(File.separator).concat("dtd");
		
		
		
		
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new MMAX2ExporterException(e.getMessage());
			
		}
		Salt2MMAX2Mapper mapperCorpus;
		try {
			mapperCorpus = new Salt2MMAX2Mapper(documentBuilder,matchingConditionsFilePath,pointersMatchingConditionsFilePath);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new MMAX2ExporterException(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new MMAX2ExporterException(e.getMessage());
		}
			
		if (this.getLogService()!= null)
			mapperCorpus.setLogService(this.getLogService());
		
		SaltExtendedCorpusFactory corpusFactory = new SaltExtendedCorpusFactory(documentBuilder);
		corpus = corpusFactory.newEmptyCorpus(new File(this.getCorpusDefinition().getCorpusPath().toFileString()));
		schemeFactory = new SchemeFactory(corpus,documentBuilder);
		
		try {
			SaltExtendedFileGenerator.initializeCorpus(corpus,ressourcePath);
		} catch (Exception exception) {
			exception.printStackTrace();
			if (getLogService()!= null)
			{
				getLogService().log(LogService.LOG_ERROR, "Cannot export the corpus '"+this.getCorpusDefinition().getCorpusPath()+"'. The reason is: "+exception.getMessage());
			}		
		}

		this.mapperRunners= new BasicEList<MapperRunner>();
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
		
		try {
			SaltExtendedFileGenerator.finalizeCorpus(corpus);
		} catch (Exception exception) {
			exception.printStackTrace();
			if (getLogService()!= null)
			{
				getLogService().log(LogService.LOG_ERROR, "Cannot export the corpus '"+this.getCorpusDefinition().getCorpusPath()+"'. The reason is: "+exception.getMessage());
			}
		} 
		
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
	public void start(SElementId sElementId) 
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
				MapperRunner mapperRunner= new MapperRunner();
				
				try{//configure mapper and mapper runner
					DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					mapperRunner.documentFactory = new SaltExtendedDocumentFactory(corpus,documentBuilder);
					mapperRunner.mapper= new Salt2MMAX2Mapper(documentBuilder,matchingConditionsFilePath,pointersMatchingConditionsFilePath);
					if (this.getLogService()!= null)
						mapperRunner.mapper.setLogService(this.getLogService());
					mapperRunner.sDocumentId= sElementId;
				//configure mapper and mapper runner
				
					if (this.getRUN_IN_PARALLEL())
					{//run import in parallel	
						this.mapperRunners.add(mapperRunner);
						this.executorService.execute(mapperRunner);
					}//run import in parallel
					else 
					{//do not run import in parallel
						mapperRunner.start();
					}//do not run import in parallel
				} catch (Exception e) {
					e.printStackTrace();
					if (getLogService()!= null)
					{
						getLogService().log(LogService.LOG_ERROR, "Cannot export the SDocument '"+sElementId+"'. The reason is: "+e);
					}
					getPepperModuleController().finish(sElementId);
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
		public SElementId sDocumentId= null;
		public Salt2MMAX2Mapper mapper= null;
		private SaltExtendedDocumentFactory documentFactory = null;
		
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
				throw new MMAX2ExporterException("BUG: Cannot start export, because the mapper is null.");
			if (sDocumentId== null)
				throw new MMAX2ExporterException("BUG: Cannot start export, because no SDocument object is given.");
			try 
			{
				SDocument sDocument = (SDocument)sDocumentId.getSIdentifiableElement();
				SaltExtendedDocument document = mapper.mapAllSDocument(corpus,sDocument,documentFactory,schemeFactory);
				SaltExtendedFileGenerator.outputDocument(corpus, document);
				getPepperModuleController().put(sDocumentId);
			}catch (Exception e)
			{
				e.printStackTrace();
				if (getLogService()!= null)
				{
					getLogService().log(LogService.LOG_ERROR, "Cannot export the SDocument '"+sDocumentId+"'. The reason is: "+e);
				}
				getPepperModuleController().finish(sDocumentId);
			}
			this.lock.lock();
			this.isFinished= true;
			this.finishCondition.signal();
			this.lock.unlock();
		}
	}
}
