package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkable;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.MMAX2ExporterException;
import eurac.commul.annotations.mmax2wrapper.FileGenerator;
import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;


public class SaltExtendedFileGenerator extends FileGenerator {
	
	
	public static void createCorpus(SaltExtendedCorpus corpus, String outputPath, String ressourcePath) throws MMAX2WrapperException, IOException, ParserConfigurationException
	{   
		createCorpus_t(corpus, outputPath, ressourcePath);
		
		outputPath = outputPath.replace("//", "/");
		if (!outputPath.endsWith(File.separator)){
			outputPath = outputPath.concat(File.separator);
		} 
		
		File saltInfosDirectory = new File (outputPath + SaltExtendedMmax2Infos.SALT_INFO_FOLDER);
		if (!saltInfosDirectory.mkdirs()){ 
			throw new MMAX2ExporterException("Cannot create folder for SaltInfoCustomizations in '"+outputPath+"'");
		}
		
		
		ArrayList<Scheme> schemes = corpus.getSchemes();
		
		for(SaltExtendedDocument document: corpus.getSaltExtendedDocuments()){
			createWordsFile(document, outputPath);
			createSaltInfoFile(document, outputPath);
			createDocumentFile(document, outputPath);
			
			for(Scheme scheme: schemes){
				createMarkableFile(document, scheme, outputPath);
				
			}
		}
	}
	
	
	public static void createSaltInfoFile(SaltExtendedDocument document, String corpusPath) throws IOException, MMAX2WrapperException, ParserConfigurationException{
		OutputXmlFile(corpusPath + File.separator + SaltExtendedMmax2Infos.SALT_INFO_FOLDER + File.separator + document.getDocumentId() + SaltExtendedMmax2Infos.SALT_INFO_FILE_ENDING,
				createSaltInfoFileString(document));
	}
	
	
	private static final String MMAX2_SALT_INFO_GENERIC =
	 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
	+"<"+ SaltExtendedMmax2Infos.SALT_INFOS_NODE_NAME+">" + "\n"
	+"	@salt_infos@" + "\n"
	+"</"+ SaltExtendedMmax2Infos.SALT_INFOS_NODE_NAME+">";
			
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
			 +SaltExtendedMmax2Infos.SALT_INFO_SLAYER_ATTR_NAME+"=\"@salt_layer@\" "
			 +SaltExtendedMmax2Infos.SALT_INFO_STYPE_ATTR_NAME+"=\"@salt_type@\" "
			 +SaltExtendedMmax2Infos.SALT_INFO_SNAME_ATTR_NAME+"=\"@salt_name@\"/>";
			
	private static String createSaltInfoEntry(SaltExtendedMarkable markable) throws MMAX2WrapperException{
		String cpy = MMAX2_SALT_INFO_ENTRY_GENERIC + "";
		cpy = cpy.replaceAll("@markable_id@", EscapeString(markable.getId()));
		cpy = cpy.replaceAll("@salt_id@", EscapeString(markable.getSId()));
		cpy = cpy.replaceAll("@salt_layer@", EscapeString(markable.getSLayerId()));
		cpy = cpy.replaceAll("@salt_type@", EscapeString(markable.getSType()));
		cpy = cpy.replaceAll("@salt_name@", EscapeString(markable.getSName()));
		
		return cpy;
	}
	
	protected static String EscapeString(String original) throws MMAX2WrapperException{
		String copy = original+"";
		return  EscapeStringSimple(StringEscapeUtils.escapeXml(copy));		
	}
	
	private final  static String[] prohibed_arguments = {"markable_id","salt_layer","salt_type","salt_name","salt_infos"};
	
	private final static String prohibed_regexp = java.util.regex.Pattern.quote("@")+"("+StringUtils.join(prohibed_arguments,"|")+")"+java.util.regex.Pattern.quote("@");
	
	protected static String EscapeStringSimple(String original) throws MMAX2WrapperException{
		String copy = original+"";
		if(copy.matches(prohibed_regexp)){
			throw new MMAX2ExporterException("'"+copy+"' contains one of the following reserved @argument@ ("+prohibed_arguments+")");	
		}		
		return  FileGenerator.EscapeStringSimple(copy);
	}

	

}
