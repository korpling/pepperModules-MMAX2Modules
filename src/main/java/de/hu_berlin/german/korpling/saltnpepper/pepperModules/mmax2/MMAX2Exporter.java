package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.EList;
import org.osgi.service.component.annotations.Component;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepper.exceptions.PepperFWException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperExporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperExporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory;


/**
 * This class exports data from Salt to the MMAX2 format.
 * The code has been adapted from PAULAExporter.
 * 
 * @author Lionel Nicolas
 * @author FLorian Zipser (just adaptations)
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
		setName("MMAX2Exporter");
		setProperties(new MMAX2ExporterProperties());
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
	
// ========================== end: flagging for parallel running
	/**
	 * a property representation of a property file
	 */
	protected Properties props= null;
	
	/**
	 * ThreadPool
	 */
	private ExecutorService executorService= null;
	
	@Override
	public void start()
	{

		if (this.getCorpusDesc().getCorpusPath()== null)
			throw new PepperModuleException(this, "Cannot export SaltProject, because no corpus path is given.");
		if (this.getSaltProject()== null)
			throw new PepperModuleException(this, "Cannot export SaltProject, because it is null.");
		if (	(this.getSaltProject().getSCorpusGraphs()== null)||
				(this.getSaltProject().getSCorpusGraphs().size()== 0))
			throw new PepperModuleException(this, "Cannot export SaltProject, no SCorpusGraphs are given.");
		
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new PepperModuleException(this, "", e);
			
		}
		Salt2MMAX2Mapper mapperCorpus;
		try {
			mapperCorpus = new Salt2MMAX2Mapper(documentBuilder,((MMAX2ExporterProperties)getProperties()).getMatchingConditionsFilePath(),((MMAX2ExporterProperties)getProperties()).getPointersMatchingConditionsFilePath());
		} catch (SAXException e) {
			e.printStackTrace();
			throw new PepperModuleException(this, "", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new PepperModuleException(this, "", e);
		}
		SaltExtendedCorpusFactory corpusFactory = new SaltExtendedCorpusFactory(documentBuilder);
		corpus = corpusFactory.newEmptyCorpus(new File(this.getCorpusDesc().getCorpusPath().toFileString()));
		schemeFactory = new SchemeFactory(corpus,documentBuilder);
		
		super.start();
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
					mapperRunner.mapper= new Salt2MMAX2Mapper(documentBuilder, ((MMAX2ExporterProperties)getProperties()).getMatchingConditionsFilePath(),((MMAX2ExporterProperties)getProperties()).getPointersMatchingConditionsFilePath());
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
					throw new PepperModuleException(this, "Cannot export the SDocument '"+sElementId+"'. The reason is: "+e);
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
				throw new PepperFWException("", e);
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
				throw new PepperModuleException("BUG: Cannot start export, because the mapper is null.");
			if (sDocumentId== null)
				throw new PepperModuleException("BUG: Cannot start export, because no SDocument object is given.");
			try 
			{
				mapper.mapAllSDocument(corpus,(SDocument)sDocumentId.getSIdentifiableElement(),documentFactory,schemeFactory);
			}catch (Exception e)
			{
				throw new PepperModuleException("Cannot export the SDocument '"+sDocumentId+"'. The reason is: "+e);
			}
			this.lock.lock();
			this.isFinished= true;
			this.finishCondition.signal();
			this.lock.unlock();
		}
	}
}
