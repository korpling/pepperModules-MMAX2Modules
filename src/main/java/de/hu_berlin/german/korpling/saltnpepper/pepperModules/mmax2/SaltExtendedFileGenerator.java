package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkable;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkableContainer;
import eurac.commul.annotations.mmax2wrapper.FileGenerator;
import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;
 
/**
 * This class aims at dealing with the outputting to files of a Mmax Corpus enhanced with Salt information
 * @author Lionel Nicolas
 */
public class SaltExtendedFileGenerator extends FileGenerator {
	
	/** 
	 * Outputs the SaltExtended corpus to the folders contained in its internal variables
	 * @param corpus The corpus to output
	 * @param ressourcePath The path where to find files required by mmax2 such as the markables.dtd etc. 
	 * @throws MMAX2WrapperException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static void createCorpus(SaltExtendedCorpus corpus) throws IOException, ParserConfigurationException, MMAX2WrapperException {   
		FileGenerator.outputCorpus(corpus);
		
		File saltInfosDirectory = new File (corpus.getCorpusPath()+ File.separator + corpus.getSaltInfoPath());
		if (!saltInfosDirectory.mkdirs()){ 
			throw new PepperModuleException("create folder for SaltInfo '"+saltInfosDirectory.getAbsolutePath()+"'");
		}
		
		for(SaltExtendedDocument document: corpus.getSaltExtendedDocuments()){
			createSaltInfoFile(document);
		}
	}
	
	public static void initializeCorpus(SaltExtendedCorpus corpus) throws IOException, MMAX2WrapperException
	{   
		FileGenerator.initializeCorpus(corpus);
		
		File saltInfosDirectory = new File (corpus.getCorpusPath()+ File.separator + corpus.getSaltInfoPath());
		if (!saltInfosDirectory.mkdirs()){ 
			throw new PepperModuleException("create folder for SaltInfo '"+saltInfosDirectory.getAbsolutePath()+"'");
		}
	}
	
	public static void outputDocument(SaltExtendedCorpus corpus, SaltExtendedDocument document) throws MMAX2WrapperException, IOException
	{   
		FileGenerator.outputDocument(corpus,document);
		createSaltInfoFile(document);
	}
	
	public static void finalizeCorpus(SaltExtendedCorpus corpus) throws MMAX2WrapperException, IOException
	{   
		FileGenerator.finalizeCorpus(corpus);
	}
	
	private static void createSaltInfoFile(SaltExtendedDocument document) throws IOException, MMAX2WrapperException{
		OutputXmlFile(document.getFactory().getCorpus().getCorpusPath() + File.separator + document.getFactory().getCorpus().getSaltInfoPath() + File.separator + document.getDocumentId() + SaltExtendedMmax2Infos.SALT_INFO_FILE_ENDING,
				createSaltInfoFileString(document));
	}
	
	
	private static final String MMAX2_SALT_INFO_GENERIC =
	 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
	+"<"+ SaltExtendedMmax2Infos.SALT_INFOS_NODE_NAME+">" + "\n"
	+"	@salt_infos@" + "\n"
	+"</"+ SaltExtendedMmax2Infos.SALT_INFOS_NODE_NAME+">";
	
	//Generates the entire content of the SAltInfo File corresponding to the SaltExtendedDocument document
	private static String createSaltInfoFileString(SaltExtendedDocument document) throws MMAX2WrapperException{
		String cpy = MMAX2_SALT_INFO_GENERIC + "";
		
		ArrayList<SaltExtendedMarkable> markables = document.getAllSaltExtendedMarkables();
		ArrayList<String> markablesStr = new ArrayList<String>();
		for(SaltExtendedMarkable markable : markables){
			markablesStr.add(createSaltInfoEntry(markable));
		}
		cpy = cpy.replaceAll("@salt_infos@", EscapeStringSimple(StringUtils.join(markablesStr,LINE_SEPARATOR)));
		
		return cpy;
	}
	
	
	private static final String MMAX2_SALT_INFO_ENTRY_GENERIC =
	 "	<"+ SaltExtendedMmax2Infos.SALT_INFO_NODE_NAME+" "
			 +SaltExtendedMmax2Infos.SALT_INFO_ID_ATTR_NAME+"=\"@markable_id@\" "
			 +SaltExtendedMmax2Infos.SALT_INFO_SID_ATTR_NAME+"=\"@salt_id@\" "
			 +SaltExtendedMmax2Infos.SALT_INFO_STYPE_ATTR_NAME+"=\"@salt_type@\" "
			 +SaltExtendedMmax2Infos.SALT_INFO_SNAME_ATTR_NAME+"=\"@salt_name@\" @additional@/>";
	
	//Generates an entry of the SAltInfo File for the SaltExtendedMarkable markable
	private static String createSaltInfoEntry(SaltExtendedMarkable markable) throws MMAX2WrapperException{
		String cpy = MMAX2_SALT_INFO_ENTRY_GENERIC + "";
		cpy = cpy.replaceAll("@markable_id@", EscapeString(markable.getId()));
		cpy = cpy.replaceAll("@salt_id@", EscapeString(markable.getSId()));
		cpy = cpy.replaceAll("@salt_type@", EscapeString(markable.getSType()));
		cpy = cpy.replaceAll("@salt_name@", EscapeString(markable.getSName()));
		if(markable.getSType().equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER)){
			SaltExtendedMarkableContainer containerMarkable = (SaltExtendedMarkableContainer) markable;
			cpy = cpy.replaceAll("@additional@",SaltExtendedMmax2Infos.SALT_INFO_CONTAINED_ID_ATTR_NAME+"=\""+EscapeString(containerMarkable.getContainedId())
											+"\" "+SaltExtendedMmax2Infos.SALT_INFO_CONTAINED_SCHEME_ATTR_NAME+"=\""+EscapeString(containerMarkable.getContainedSchemeName()));
		}else{
			cpy = cpy.replaceAll("@additional@", "");
		}
		
		return cpy;
	}
	
	
	// some useful methods to avoid outputting non-proper Xml files
	protected static String EscapeString(String original) throws MMAX2WrapperException{
		String copy = original+"";
		return  EscapeStringSimple(StringEscapeUtils.escapeXml(copy));		
	}
	
	private final  static String[] prohibed_arguments = {"markable_id","salt_layer","salt_type","salt_name","salt_infos"};
	
	private final static String prohibed_regexp = java.util.regex.Pattern.quote("@")+"("+StringUtils.join(prohibed_arguments,"|")+")"+java.util.regex.Pattern.quote("@");
	
	protected static String EscapeStringSimple(String original) throws MMAX2WrapperException{
		String copy = original+"";
		if(copy.matches(prohibed_regexp)){
			throw new PepperModuleException("'"+copy+"' contains one of the following reserved @argument@ ("+prohibed_arguments+")");	
		}		
		return  FileGenerator.EscapeStringSimple(copy);// if good at this level then check upstairs
	}

	

}
