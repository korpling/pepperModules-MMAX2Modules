/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package eurac.commul.annotations.mmax2wrapper;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import eurac.commul.annotations.mmax2wrapper.CorpusFactory.Corpus;
import eurac.commul.annotations.mmax2wrapper.DocumentFactory.Document;
import eurac.commul.annotations.mmax2wrapper.DocumentFactory.Document.BaseDataUnit;
import eurac.commul.annotations.mmax2wrapper.MarkableFactory.Markable;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableAttributeFactory.MarkableAttribute;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableFreetextAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableNominalAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkablePointerAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableSetAttributeFactory;

/**
 * This class aims at dealing with the outputting to files of a Mmax Corpus
 * @author Lionel Nicolas
 *
 */
public class FileGenerator {

	protected final static String LINE_SEPARATOR = "\n";
	
	private static String ressourcePath;
	

	
	//// MAIN FUNCTIONS ////

	public static void outputCorpus(Corpus corpus, String ressourcePa) throws IOException, MMAX2WrapperException{
		initializeCorpus(corpus,ressourcePa);
		for(Document doc: corpus.getDocuments()){
			outputDocument(corpus,doc);
		}
		finalizeCorpus(corpus);
	}
	
	
	public static void initializeCorpus(Corpus corpus, String ressourcePa) throws IOException, MMAX2WrapperException
	{   
		if (ressourcePa == null)
			throw new MMAX2WrapperException("Cannot export corpus structure, because the ressource path is null.");
		
		ressourcePath = ressourcePa.replace("//", "/");
		if (!ressourcePath.endsWith(File.separator)){
			ressourcePath = ressourcePath.concat(File.separator);
		} 
		
		File mainDirectory = corpus.getCorpusPath();		
		if(mainDirectory.exists()){
			if(mainDirectory.list().length != 0){
				throw new MMAX2WrapperException("'"+mainDirectory.toString()+"' already exists and is not empty, please remove it, rename it or provide another path for exporting the corpus");
			}
		}else if (!mainDirectory.mkdirs()){ 
			throw new MMAX2WrapperException("Cannot create directory '"+mainDirectory.getAbsolutePath()+"'");
		}
		
		File styleDirectory = corpus.getStylesPath();
		if (!styleDirectory.mkdirs()){ 
			throw new MMAX2WrapperException("Cannot create folder for Styles '"+styleDirectory.getAbsolutePath()+"'");
		}
				
		File markablesDirectory = corpus.getMarkablesPath();
		if (!markablesDirectory.mkdirs()){ 
			throw new MMAX2WrapperException("Cannot create folder for Markables '"+markablesDirectory.getAbsolutePath()+"'");
		}
		copyFile(ressourcePath + Mmax2Infos.MARKABLES_DTD, markablesDirectory.getAbsolutePath() + File.separator + Mmax2Infos.MARKABLES_DTD);	
		
		File baseDataDirectory = corpus.getBaseDataPath();
		if (!baseDataDirectory.mkdirs()){ 
			throw new MMAX2WrapperException("Cannot create folder for BaseData '"+baseDataDirectory.getAbsolutePath()+"'");
		}
		copyFile(ressourcePath + Mmax2Infos.DOCUMENT_WORDS_DTD, baseDataDirectory.getAbsolutePath() + File.separator + Mmax2Infos.DOCUMENT_WORDS_DTD);
		
		File customizationsDirectory = corpus.getCustomizationsPath();
		if (!customizationsDirectory.mkdirs()){ 
			throw new MMAX2WrapperException("Cannot create folder for Customizations '"+customizationsDirectory.getAbsolutePath()+"'");
		}
		
		File schemesDirectory = corpus.getSchemesPath();
		if (!schemesDirectory.mkdirs()){ 
			throw new MMAX2WrapperException("Cannot create folder for Schemes '"+schemesDirectory.getAbsolutePath()+"'");
		}
	}

	
	public static void finalizeCorpus(Corpus corpus) throws MMAX2WrapperException, IOException
	{   
		if (ressourcePath == null)
			throw new MMAX2WrapperException("Cannot export corpus structure, because the ressource path is null.");
		
		ArrayList<Scheme> schemes = corpus.getSchemes();
		
		createCommonPathFile(corpus);
		createRandomStyleFile(corpus);
		
		for(Scheme scheme: schemes){
			createSchemeFile(scheme);
			createRandomCustomizationFile(scheme);
		}				
	
	}
	
	public static void outputDocument(Corpus corpus, Document document) throws MMAX2WrapperException, IOException
	{   
		ArrayList<Scheme> schemes = corpus.getSchemes();
	
		createWordsFile(document);
		createDocumentFile(document);

		for(Scheme scheme: schemes){
			ArrayList<Markable> markables =  document.getMarkablesOfLevel(scheme.getName());
			
			if((markables == null) || (markables.size() == 0)){
				//return;
			}else{
				createMarkableFile(document, scheme);
			}	
		}
	}
	
	
	
	////	COMMON PATH		////
	
	
	private static void createCommonPathFile(Corpus corpus) throws IOException, MMAX2WrapperException{
		OutputXmlFile(corpus.getCorpusPath().getAbsolutePath() + File.separator +  Mmax2Infos.COMMON_PATH_FILE, createCommonPathFileString(corpus));
	}
	
	private static final String MMAX2_COMMON_PATH_GENERIC = 
	 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
	+ "\n"
	+"<common_paths>" + "\n"
	+"	<basedata_path>"+Mmax2Infos.DOCUMENT_BASEDATA_FOLDER+"/</basedata_path>" + "\n"
	+"	<customization_path>"+Mmax2Infos.CUSTOMIZATIONS_FOLDER+"/</customization_path>" + "\n"
	+"	<scheme_path>"+Mmax2Infos.SCHEMES_FOLDER+"/</scheme_path>" + "\n"
	+"	<style_path>"+Mmax2Infos.STYLES_FOLDER+"/</style_path>" + "\n"
	+"	<markable_path>"+Mmax2Infos.MARKABLES_FOLDER+"/</markable_path>" + "\n"
	+ "\n\n"
	+"<views>" + "\n"
	+"	@styles@" + "\n"
	+"</views>" + "\n"
	+ "\n\n"
	+"<annotations>" + "\n"
	+"@levels@"+ "\n"
	+"</annotations>" + "\n"
	+ "\n\n"
	+"<user_switches>" + "\n"
	+"	@switches@" + "\n"
	+"</user_switches>" + "\n"
	+"</common_paths>";
	
	private static String createCommonPathFileString(Corpus corpus) throws MMAX2WrapperException{
		String cpy = MMAX2_COMMON_PATH_GENERIC + "";
		
		ArrayList<Scheme> schemes  = corpus.getSchemes();
		
		ArrayList<String> levelsStr = new ArrayList<String>();
		ArrayList<String> switchesStr = new ArrayList<String>();
		for(Scheme scheme : schemes){
			levelsStr.add(createCommonPathLevelEntry(scheme));
		}
		for(String userSwitchName: corpus.getuserSwitches().keySet()){
			switchesStr.add(createCommonPathUserSWitchEntry(corpus,userSwitchName));
		}
		Collections.sort(levelsStr);
		Collections.sort(switchesStr);
		
		cpy = cpy.replaceAll("@styles@", EscapeStringSimple(createCommonPathStyleEntry(Mmax2Infos.STYLE_DEFAULT_FILE_PATH)));
		cpy = cpy.replaceAll("@levels@", EscapeStringSimple(StringUtils.join(levelsStr.toArray(new String[levelsStr.size()]),LINE_SEPARATOR)));
		cpy = cpy.replaceAll("@switches@", EscapeStringSimple(StringUtils.join(switchesStr.toArray(new String[switchesStr.size()]),LINE_SEPARATOR)));
		
		return cpy;
	}
	
	
	private static final String MMAX2_COMMON_PATH_STYLE_GENERIC=
	 "	<stylesheet>@style_file_path@</stylesheet>";
	
	private static String createCommonPathStyleEntry(String styleFilePath) throws MMAX2WrapperException{
		String cpy = MMAX2_COMMON_PATH_STYLE_GENERIC + "";
		cpy = cpy.replaceAll("@style_file_path@", EscapeString(styleFilePath));

		return cpy;
	}
	
	
	private static final String MMAX2_COMMON_PATH_LEVEL_GENERIC=
	 "<level name=\"@level_name@\" "+
			 "schemefile=\"@level_name@"+ Mmax2Infos.SCHEMES_FILE_ENDING +"\" "+
			 "customization_file=\"@level_name@"+ Mmax2Infos.CUSTOM_FILE_ENDING +"\">$_@level_name@"+ Mmax2Infos.MARKABLES_FILE_ENDING +"</level>";
		
	private static String createCommonPathLevelEntry(Scheme scheme) throws MMAX2WrapperException{
		String cpy = MMAX2_COMMON_PATH_LEVEL_GENERIC + "";
		cpy = cpy.replaceAll("@level_name@", EscapeString(scheme.getName()));
		
		return cpy;
	}
	
	
	private static final String MMAX2_COMMON_PATH_USER_SWITCH_GENERIC=
	 "	<user_switch name=\"@switch_name@\" default=\"@active@\" />";
			
	private static String createCommonPathUserSWitchEntry(Corpus corpus, String userSwitchName) throws MMAX2WrapperException{
		String cpy = MMAX2_COMMON_PATH_USER_SWITCH_GENERIC + "";
		cpy = cpy.replaceAll("@switch_name@", EscapeString(userSwitchName));
		cpy = cpy.replaceAll("@active@", EscapeString((corpus.getUserSwitchStatus(userSwitchName))? "on":"off"));
		
		return cpy;
	}
	
	
	////	CUSTOMIZATIONS	////	
	
	private static Random rand = new Random(17);
	
	private static void createRandomCustomizationFile(Scheme scheme) throws IOException, MMAX2WrapperException{
		OutputXmlFile(scheme.getFactory().getCorpus().getCustomizationsPath().getAbsolutePath() + File.separator +  scheme.getName() + Mmax2Infos.CUSTOM_FILE_ENDING, 
				createCustomizationFileString(new Color(rand.nextInt(256),rand.nextInt(256),rand.nextInt(256))));
	}
	
	
	private static final String MMAX2_CUSTOM_GENERIC =
	 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
	+"<customization>" + "\n"
	+"	<rule pattern=\"{all}\" style=\"foreground=x:@color@\"/>" + "\n"
	+"</customization>";		
	
	private static String createCustomizationFileString(Color color) throws MMAX2WrapperException{
		String cpy = MMAX2_CUSTOM_GENERIC + "";
		cpy = cpy.replaceAll("@color@", EscapeString(Integer.toHexString(color.getRGB()).substring(2)));
		
		return cpy;
	}
	

	////	DOCUMENT 		////
	
	private static void createDocumentFile(Document document) throws IOException, MMAX2WrapperException{
		OutputFile(document.getFactory().getCorpus().getCorpusPath().getAbsolutePath() + File.separator + document.getDocumentId() + Mmax2Infos.DOCUMENT_FILE_ENDING, createDocumentFileString(document));
	}
	
	
	private static final String MMAX2_DOCUMENT_GENERIC =
	 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
	+"<mmax_project>" + "\n"
	+"   <turns/>" + "\n"
	+"   <words>@document_id@"+ Mmax2Infos.DOCUMENT_WORDS_FILE_ENDING+"</words>" + "\n"
	+"   <gestures/>" + "\n"
	+"   <keyactions/>" + "\n"
	+"</mmax_project>";
	
	private static String createDocumentFileString(Document document) throws MMAX2WrapperException {
		String cpy = MMAX2_DOCUMENT_GENERIC + "";
		cpy = cpy.replaceAll("@document_id@", EscapeString(document.getDocumentId()));	
		
		return cpy;
	}
	
	
	////	MARKABLES 	////
		
	
	private static void createMarkableFile(Document document, Scheme scheme) throws IOException, MMAX2WrapperException{
		String schemeFilePattern = scheme.getSchemeFilePattern();
		String markableFileName = schemeFilePattern.replace("$", document.getDocumentId());
		
		OutputXmlFile(document.getFactory().getCorpus().getCorpusPath().getAbsolutePath() + File.separator + Mmax2Infos.MARKABLES_FOLDER + File.separator + markableFileName,
					  createMarkablesFileString(document,scheme));
	}
	
	
	private static final String MMAX2_MARKABLES_GENERIC = 
	 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
	+"<!DOCTYPE markables" + "\n"
	+"  SYSTEM \"markables.dtd\">" + "\n"
	+"<markables xmlns=\"www.eml.org/NameSpaces/@scheme_name@\">" + "\n"
	+"	@markables@" + "\n"
	+"</markables>";
	
	
	private static String createMarkablesFileString(Document document, Scheme scheme) throws MMAX2WrapperException{
		String cpy = MMAX2_MARKABLES_GENERIC + "";
	
		ArrayList<String> markablesStr = new ArrayList<String>();
		for(Markable markable : document.getMarkablesOfLevel(scheme.getName())){
			markablesStr.add(createMarkableEntry(markable));
		}
		
		cpy = cpy.replaceAll("@scheme_name@", EscapeString(scheme.getName()));
		cpy = cpy.replaceAll("@markables@", EscapeStringSimple(StringUtils.join(markablesStr.toArray(new String[markablesStr.size()]),LINE_SEPARATOR)));
		
		return cpy;		
	}
	
	
	private static final String MMAX2_MARKABLES_MARKABLE_GENERIC = 
	 "<markable id=\"markable_@markable_id@\" span=\"@markable_span@\" mmax_level=\"@level_name@\" @annotations@ />";
	
	protected static String createMarkableEntry(Markable markable) throws MMAX2WrapperException{
		String cpy = MMAX2_MARKABLES_MARKABLE_GENERIC + "";
		cpy = cpy.replaceAll("@markable_id@", EscapeString(markable.getId()));
		cpy = cpy.replaceAll("@markable_span@", EscapeString(markable.getSpan()));
		cpy = cpy.replaceAll("@level_name@", EscapeString(markable.getFactory().getScheme().getName()));
		
		
		ArrayList<String> annotationsStr = new ArrayList<String>();
		for(MarkableAttribute attribute: markable.getAttributes()){
			if(attribute.getFactory().getAttributeType().equals(MarkablePointerAttributeFactory.pointerType)){
				String value = attribute.getValue();
				MarkablePointerAttributeFactory pointerAttributeFactory = (MarkablePointerAttributeFactory) attribute.getFactory();
				ArrayList<String> valuesStr = new ArrayList<String>();
				if(!value.equals("empty")){
					String[] values = value.split(";");
					
					for(int i = 0; i< values.length; i++){
						String value_i = values[i];
						value_i = "markable_"+value;
						if(pointerAttributeFactory.getTargetSchemeName()!= null){
							value_i = pointerAttributeFactory.getTargetSchemeName()+":"+value_i;
						}
						valuesStr.add(value_i);
					}
				}else{
					valuesStr.add(value);
				}
				annotationsStr.add(attribute.getName()+"=\""+EscapeString(StringUtils.join(valuesStr,";"))+"\"");
			}else{
				annotationsStr.add(attribute.getName()+"=\""+StringEscapeUtils.escapeXml(attribute.getValue())+"\"");
			}
		}
		Collections.sort(annotationsStr);
		cpy = cpy.replaceAll("@annotations@", EscapeStringSimple(StringUtils.join(annotationsStr.toArray(new String[annotationsStr.size()])," ")));
		
		return cpy;
	}
	
	
	////	SCHEMES		////	
	private static void createSchemeFile(Scheme scheme) throws IOException, MMAX2WrapperException{
		OutputXmlFile(scheme.getFactory().getCorpus().getSchemesPath().getAbsolutePath() + File.separator + scheme.getName() + Mmax2Infos.SCHEMES_FILE_ENDING, 
					  createSchemeFileString(scheme));
	}
	
	private static final String MMAX2_SCHEMES_GENERIC =
	 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
	+"<annotationscheme>" + "\n"
	+"	@attributes@" + "\n"
	+"</annotationscheme>";
	
	private static String createSchemeFileString(Scheme scheme) throws MMAX2WrapperException{
		String cpy = MMAX2_SCHEMES_GENERIC + "";
		ArrayList<MarkableAttributeFactory> attributesFactories = scheme.getAttributesFactories();
		
		ArrayList<String> attributesStr = new ArrayList<String>();
		for(MarkableAttributeFactory  attributeFactory: attributesFactories){
			String attributeType = attributeFactory.getAttributeType();
			if(attributeType.equals(MarkableNominalAttributeFactory.nominalType)){
				attributesStr.add(createNominalAttributeEntry(scheme,(MarkableNominalAttributeFactory) attributeFactory));
			}else if(attributeType.equals(MarkableFreetextAttributeFactory.freetextType)){
				attributesStr.add(createFreeTextAttributeEntry(scheme,(MarkableFreetextAttributeFactory) attributeFactory));
			}else if(attributeType.equals(MarkablePointerAttributeFactory.pointerType)){
				attributesStr.add(createPointerAttributeEntry(scheme,(MarkablePointerAttributeFactory) attributeFactory));
			}else if(attributeType.equals(MarkableSetAttributeFactory.setType)){
				attributesStr.add(createSetAttributeEntry(scheme,(MarkableSetAttributeFactory) attributeFactory));
			}else{
				throw new MMAX2WrapperException("Unknown type of attribute '"+attributeType+"' for attribute '"+attributeFactory.getAttributeName()+"' associated to scheme '"+scheme.getName()+"'");
			}
		}
		cpy = cpy.replaceAll("@attributes@", EscapeStringSimple(StringUtils.join(attributesStr.toArray(new String[attributesStr.size()]),LINE_SEPARATOR+LINE_SEPARATOR)));
	
		return cpy;
	}
	
	
	private static final String MMAX2_SCHEMES_POINTER_GENERIC = 
	 "<attribute id=\"@pointer_id@\" name=\"@pointer_name@\" type=\"markable_pointer\" @destination_type@>" + "\n"
	+"  <value name=\"set\" />" + "\n"
	+"  <value name=\"not_set\" />" + "\n"
	+"</attribute>";
	
	private static String createPointerAttributeEntry(Scheme scheme, MarkablePointerAttributeFactory attributeFactory) throws MMAX2WrapperException{
		String cpy = MMAX2_SCHEMES_POINTER_GENERIC + "";
		cpy = cpy.replaceAll("@level_name@", EscapeString(scheme.getName()));
		cpy = cpy.replaceAll("@pointer_name@", EscapeString(attributeFactory.getAttributeName()));
		cpy = cpy.replaceAll("@pointer_id@", EscapeString(attributeFactory.getScheme().getName()+"_"+attributeFactory.getAttributeName()));
		cpy = cpy.replaceAll("@destination_type@", ((attributeFactory.getTargetSchemeName() != null)? "target_domain=\""+EscapeString(attributeFactory.getTargetSchemeName())+"\"":""));
		
		return cpy;
	}
	
	
	private static final String MMAX2_SCHEMES_FREETEXT_GENERIC =
	 "<attribute id=\"@freetext_id@\" name=\"@freetext_name@\" type=\"freetext\" >" + "\n"
	+"  <value id=\"@freetext_name@_val\" name=\"@freetext_name@\" />" + "\n" 
	+"</attribute>";
	
	private static String createFreeTextAttributeEntry(Scheme scheme, MarkableFreetextAttributeFactory attributeFactory) throws MMAX2WrapperException{
		String cpy = MMAX2_SCHEMES_FREETEXT_GENERIC + "";
		cpy = cpy.replaceAll("@level_name@", EscapeString(scheme.getName()));
		cpy = cpy.replaceAll("@freetext_name@", EscapeString(attributeFactory.getAttributeName()));
		cpy = cpy.replaceAll("@freetext_id@", EscapeString(attributeFactory.getScheme().getName()+"_"+attributeFactory.getAttributeName()));
		
		return cpy;
	}
	
	private static final String MMAX2_SCHEMES_NOMINAL_GENERIC =
	 "<attribute id=\"@nominal_id@\" name=\"@nominal_name@\" type=\"nominal_list\">" + "\n"
	+"	@values@" + "\n"
	+"</attribute>";
		
	private static final String MMAX2_SCHEMES_NOMINAL_VALUE_GENERIC =
	 "	<value name=\"@val@\"/>";
	
	private static String createNominalAttributeEntry(Scheme scheme, MarkableNominalAttributeFactory attributeFactory) throws MMAX2WrapperException{
		String cpy = MMAX2_SCHEMES_NOMINAL_GENERIC + "";
		ArrayList<String> valuesAccepted = attributeFactory.getValuesAccepted();	

		ArrayList<String> valuesString = new ArrayList<String>();
		for(String value : valuesAccepted){
			String cpy_value = MMAX2_SCHEMES_NOMINAL_VALUE_GENERIC+ "";
			cpy_value = cpy_value.replaceAll(java.util.regex.Pattern.quote("@val@"), EscapeString(value));
			valuesString.add(cpy_value);
		}
		
		cpy = cpy.replaceAll("@values@", EscapeStringSimple(StringUtils.join(valuesString.toArray(new String[valuesString.size()]),LINE_SEPARATOR)));
		cpy = cpy.replaceAll("@level_name@", EscapeString(scheme.getName()));
		cpy = cpy.replaceAll("@nominal_name@", EscapeString(attributeFactory.getAttributeName()));
		cpy = cpy.replaceAll("@nominal_id@", EscapeString(attributeFactory.getScheme().getName()+"_"+attributeFactory.getAttributeName()));
		
		return cpy;
	}
	
	
	private static final String MMAX2_SCHEMES_SET_GENERIC =
	 "<attribute id=\"@set_id@\" name=\"@set_name@\" type=\"markable_set\" >" + "\n"
	+"  <value id=\"@set_name@_val\" name=\"@set_name@\" />" + "\n" 
	+"</attribute>";
		
	private static String createSetAttributeEntry(Scheme scheme, MarkableSetAttributeFactory attributeFactory) throws MMAX2WrapperException{
		String cpy = MMAX2_SCHEMES_SET_GENERIC + "";
		cpy = cpy.replaceAll("@level_name@", EscapeString(scheme.getName()));
		cpy = cpy.replaceAll("@set_name@", EscapeString(attributeFactory.getAttributeName()));
		cpy = cpy.replaceAll("@set_id@", EscapeString(attributeFactory.getScheme().getName()+"_"+attributeFactory.getAttributeName()));
		
		return cpy;
	}
	
	
	////STYLES		////
	
	
	private static void createRandomStyleFile(Corpus corpus) throws IOException, MMAX2WrapperException{
		OutputFile(corpus.getStylesPath().getAbsolutePath() + File.separator + Mmax2Infos.STYLE_DEFAULT_FILE_PATH, 
				createRadomStyleFileString(corpus));
	}
	
		
	private static final String MMAX2_STYLE_GENERIC =
	 "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\"" + "\n"
	+"		 	xmlns:mmax=\"org.eml.MMAX2.discourse.MMAX2DiscourseLoader\"" + "\n"
	+"          @xmlns@" + "\n"  
	+"			>"
	+"\n\n"
	+"<xsl:output method=\"text\" indent=\"no\" omit-xml-declaration=\"yes\"/>" + "\n"
	+"\n\n"
	+"<xsl:template match=\"word\">" + "\n"
	+"	<xsl:value-of select=\"mmax:registerDiscourseElement(@id)\"/>" + "\n"
	+"	<xsl:apply-templates select=\"mmax:getStartedMarkables(@id)\" mode=\"opening\"/>" + "\n"
	+"	<xsl:value-of select=\"mmax:setDiscourseElementStart()\"/>" + "\n"
	+" 	<xsl:apply-templates/>" + "\n"
	+"	<xsl:value-of select=\"mmax:setDiscourseElementEnd()\"/>" + "\n"
	+" 	<xsl:apply-templates select=\"mmax:getEndedMarkables(@id)\" mode=\"closing\"/>"+ "\n"
	+"</xsl:template>"+ "\n"
	+ "\n\n"
	+"@schemes_styles@" + "\n"
	+"</xsl:stylesheet>";
	
	private static String createRadomStyleFileString(Corpus corpus) throws MMAX2WrapperException{
		String cpy = MMAX2_STYLE_GENERIC + "";
		
		ArrayList<Scheme> schemes = corpus.getSchemes();
		
		ArrayList<String> xmlnsStr = new ArrayList<String>();
		ArrayList<String> schemeStyleStr = new ArrayList<String>();
		for(Scheme scheme: schemes){
			xmlnsStr.add(createXmlnsStyleEntry(scheme));
			schemeStyleStr.add(createRandomSchemeStyleEntry(scheme));
		}
		Collections.sort(xmlnsStr);
		Collections.sort(schemeStyleStr);
		
		cpy = cpy.replaceAll("@xmlns@", EscapeStringSimple(StringUtils.join(xmlnsStr,LINE_SEPARATOR)));
		cpy = cpy.replaceAll("@schemes_styles@", EscapeStringSimple(StringUtils.join(schemeStyleStr.toArray(new String[schemeStyleStr.size()]),LINE_SEPARATOR+LINE_SEPARATOR)));
		
		return cpy;
	}
	
	
	private static final String MMAX2_STYLE_XMLNS_GENERIC = 
	 "		xmlns:@level_name@=\"www.eml.org/NameSpaces/@level_name@\"  ";
	
	private static String createXmlnsStyleEntry(Scheme scheme) throws MMAX2WrapperException{
		String cpy = MMAX2_STYLE_XMLNS_GENERIC + "";
		cpy = cpy.replaceAll("@level_name@", EscapeString(scheme.getName()));
		
		return cpy;
	}
	
	//private static String[][] opening_closing_choices = {{"<",">"},{"{","}"},{"[","]"}};
	//private static int i = 0;
	
	private static final String MMAX2_STYLE_SCHEME_GENERIC =
	 "<!-- @level_name@ -->" + "\n"
	+"<xsl:template match=\"@level_name@:markable\" mode=\"opening\">" + "\n"
	+"	<xsl:value-of select=\"mmax:addLeftMarkableHandle(@mmax_level, @id, '@opening@')\"/>" + "\n"
	+"</xsl:template>"
	+ "\n"
	+"<xsl:template match=\"@level_name@:markable\" mode=\"closing\">" + "\n"
	+"	<xsl:value-of select=\"mmax:addRightMarkableHandle(@mmax_level, @id, '@closing@')\"/>" + "\n"
	+"</xsl:template>";
	
	private static String createRandomSchemeStyleEntry(Scheme scheme) throws MMAX2WrapperException{
		String cpy = MMAX2_STYLE_SCHEME_GENERIC + "";
		String schemeName = EscapeString(scheme.getName());
		
		//String [] choice = opening_closing_choices[ i % opening_closing_choices.length];
		//i++;
				
		cpy = cpy.replaceAll("@level_name@",EscapeString(schemeName) );
		cpy = cpy.replaceAll("@opening@", EscapeString(""));
		cpy = cpy.replaceAll("@closing@", EscapeString(""));
		cpy = cpy.replaceAll("@level@", EscapeString(schemeName));
		
		return cpy;
	}
	
	
	////	WORDS		////
	
	private static void createWordsFile(Document document) throws MMAX2WrapperException, IOException{
		OutputXmlFile(document.getFactory().getCorpus().getBaseDataPath().getAbsolutePath() + File.separator + document.getDocumentId() + Mmax2Infos.DOCUMENT_WORDS_FILE_ENDING, 
					  createWordsFileString(document));
	}
	
	private static final String MMAX2_WORDS_GENERIC =
	 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
	+"<!DOCTYPE words" + "\n"
	+"  SYSTEM \"words.dtd\">" + "\n"
	+"<words>" + "\n"
	+"	@words@" + "\n"
	+"</words>";
	
	private static String createWordsFileString(Document document) throws MMAX2WrapperException{
		String cpy = MMAX2_WORDS_GENERIC + "";
		
		ArrayList<BaseDataUnit> tokens = document.getBaseDataUnits();
		
		ArrayList<String> caracStr = new ArrayList<String>();
		for(BaseDataUnit token : tokens){
			String text = token.getText();
			if(text.equals(" ")){
				text = "@space@";
			}else if(text.equals("\n")){
				text = "@carriage_return@";
			}
			caracStr.add(createWordEntry(token.getId(),text));
		}
		cpy = cpy.replaceAll("@words@", EscapeStringSimple(StringUtils.join(caracStr,LINE_SEPARATOR)));
		
		return cpy;
	}
	
	
	private static final String MMAX2_WORDS_WORD_GENERIC =
	 "	<word id=\"@word_id@\">@word@</word>";
	
	private static String createWordEntry(String wordId,String word) throws MMAX2WrapperException{
		String cpy = MMAX2_WORDS_WORD_GENERIC + "";
		cpy = cpy.replaceAll("@word_id@", EscapeString(wordId));
		cpy = cpy.replaceAll("@word@", EscapeString(word));
		
		return cpy;
	}
	
	
	/// SOME USEFUL FUNCTIONS ///
	
	protected static void OutputXmlFile(String path,String content) throws IOException, MMAX2WrapperException{
		OutputFile(path,content.replaceAll("@space@", " ").replaceAll("@carriage_return@", "\n"));
	}
	
	private static void OutputFile(String path,String content) throws IOException, MMAX2WrapperException{
		File newFile = new File(path);
		if(newFile.createNewFile()){
			PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile),"UTF8")),false);
			output.write(content);
			output.close();			
		}else{
			throw new MMAX2WrapperException("Could not create file '"+path+"', this file already exists...");	
		}
		
	}
	
	
	private static void copyFile(String originPath, String outputPath) throws IOException {
		FileInputStream in = new FileInputStream(new File(originPath));
		FileOutputStream out = new FileOutputStream(new File(outputPath));

		int c;
	    while((c = in.read()) != -1) {
	    	out.write(c);
        }      
	}
	
	
	protected static String EscapeString(String original) throws MMAX2WrapperException{
		String copy = original+"";
		return  EscapeStringSimple(StringEscapeUtils.escapeXml(copy));		
	}
	
	private final  static String[] prohibed_arguments = {"active","annotations","attributes","closing","color","document_id","freetext_name","freetext_id","level",
														 "level_name","levels","markable_id","markable_span","markables","nominal_name","nominal_id","opening",
														 "pointer_name","pointer_id","values","scheme_name","schemes_styles","set_name","set_id","style_file_path","styles",
														 "switches","switch_name","word","word_id","words","xmlns"};
	
	private final static String prohibed_regexp = java.util.regex.Pattern.quote("@")+"("+StringUtils.join(prohibed_arguments,"|")+")"+java.util.regex.Pattern.quote("@");
	
	protected static String EscapeStringSimple(String original) throws MMAX2WrapperException{
		String copy = original+"";
		if(copy.matches(prohibed_regexp)){
			throw new MMAX2WrapperException("'"+copy+"' contains one of the following reserved @argument@ ("+prohibed_arguments+")");	
		}		
		return  java.util.regex.Matcher.quoteReplacement(copy);
	}
}
