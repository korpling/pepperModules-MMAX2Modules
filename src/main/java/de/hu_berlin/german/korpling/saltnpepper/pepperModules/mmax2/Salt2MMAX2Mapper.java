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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAbstractAnnotation;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SAnnotationContainer;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkable;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkableContainer;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.MMAX2WrapperException;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.SchemeFactory;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.SchemeFactory.MarkableAttributeFactory;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.SchemeFactory.MarkableAttributeFactory.MarkableAttribute;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.SchemeFactory.MarkableFreetextAttributeFactory;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.SchemeFactory.MarkableNominalAttributeFactory;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.SchemeFactory.MarkablePointerAttributeFactory;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.SchemeFactory.MarkableSetAttributeFactory;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.SchemeFactory.Scheme;


/**
 * Converts a SGraph into an Mmax2 corpus.
 * The code has been adapted from SALT2PAULAMapper.
 * @author Lionel Nicolas
 *
 */
public class Salt2MMAX2Mapper extends PepperMapperImpl
{	
	//private static final Logger logger= LoggerFactory.getLogger(MMAX22SaltMapper.class);
	private Hashtable<STextualRelation,Integer> spanStextualRelationCorrespondance;
	private Hashtable<STextualDS,ArrayList<String>> spanStextualDSCorrespondance;
	private Hashtable<SNode,SaltExtendedMarkable> registeredSNodesMarkables;
	private Hashtable<SRelation,SaltExtendedMarkable> registeredSRelationsMarkables;
	private HashMap<SLayer,SaltExtendedMarkable> registeredSLayerMarkables;
	private DocumentBuilder documentBuilder;
	
	private Hashtable<Object,Hashtable<Scheme,SaltExtendedMarkableContainer>> sContainerMarkables;
	private ArrayList<SAnnotationMapping> sannotationmappings;
	private ArrayList<SRelationMapping> srelationMappings;

	private SaltExtendedCorpus corpus;
	private SaltExtendedDocument document;
	private SchemeFactory schemeFactory;
	private SaltExtendedDocumentFactory factory= null; 
	
	public Salt2MMAX2Mapper(){
		this.sannotationmappings = new ArrayList<SAnnotationMapping>();
		this.srelationMappings = new ArrayList<SRelationMapping>();
	}
	
	public Salt2MMAX2Mapper(DocumentBuilder documentBuilder,SchemeFactory schemeFactory,SaltExtendedDocumentFactory factory){
		this.sannotationmappings = new ArrayList<SAnnotationMapping>();
		this.srelationMappings = new ArrayList<SRelationMapping>();
		this.documentBuilder = documentBuilder;
		this.schemeFactory = schemeFactory;
		this.factory = factory;
		this.corpus = this.factory.getCorpus();		
	}
	
	public Salt2MMAX2Mapper(DocumentBuilder documentBuilder,SchemeFactory schemeFactory,SaltExtendedDocumentFactory factory,
							ArrayList<SAnnotationMapping> sannotationmappings, 
							ArrayList<SRelationMapping> srelationMappings){
		this.sannotationmappings = sannotationmappings;
		this.srelationMappings = srelationMappings;
		this.documentBuilder = documentBuilder;
		this.schemeFactory = schemeFactory;
		this.factory = factory;
		this.corpus = this.factory.getCorpus();		
		
	}
	
//	public void setAttributeMatchConditions(Hashtable<String,ArrayList<SaltAttributeMatchCondition>> conditions){
//		this.conditions = conditions;	
//	}
//	
//	public void setPointerMatchConditions(Hashtable<String,ArrayList<SaltPointerMatchCondition>> pointersConditions){
//		this.pointersConditions = pointersConditions;	
//	}	
//
//	public void setFactory(SaltExtendedDocumentFactory factory) {
//		this.factory = factory;
//	}
//	
//	public void setCorpus(SaltExtendedCorpus corpus) {
//		this.corpus = corpus;
//	}
//	
//	public void setSchemeFactory(SchemeFactory schemeFactory) {
//		this.schemeFactory = schemeFactory;
//	}
//	
//	
//	public SchemeFactory getSchemeFactory() {
//		return schemeFactory;
//	}
//	
//	/**
//	 * Returns the set document builder
//	 * @return {@link DocumentBuilder} used here
//	 */
//	public DocumentBuilder getDocumentBuilder() {
//		return documentBuilder;
//	}
//
//	/**
//	 * Returns the properties to be used for this mapping.
//	 * @return properties for mapping
//	 */
//	public MMAX2ExporterProperties getProps() {
//		return (MMAX2ExporterProperties)getProperties();
//	}
//	
//	public SaltExtendedCorpus getCorpus() {
//		return corpus;
//	}
//	public SaltExtendedDocumentFactory getFactory() {
//		return factory;
//	}
//	
//	/**
//	 * Builds the mapper
//	 * @param documentBuilder an Xml parser to use for parsing Xml files
//	 * @param matchingAttributeConditionFilePath the path to the file containing the conditions for performing mapping on any SAannotations or SMetaAnnotations
//	 * @param matchingPointerConditionFilePath the path to the file containing the conditions for performing mapping on SRelations
//	 * @throws SAXException
//	 * @throws IOException
//	 */

	
	// some usefuls fonctions to create Mmax ID or record and access the mmax2 informations associated with previously created markables

	/**
	 *  METHODS FOR HANDLING REGISTERING/REQUESTING MARKABELS OVER ELEMENTS
	 */
	
	private int markableIdCpt = 0;
	private String getNewId(){
		markableIdCpt++;
		return markableIdCpt + "";
	}
	
	private String makeSpan(int startEnd){
		String span = "word_"+startEnd;
		return span;
	}
	
	private String makeSpan(int start, int end){
		String span = "word_"+start+".."+"word_"+end;
		return span;
	}
	
	private String makeSpan(ArrayList<String> indices){
		String span = StringUtils.join(indices.toArray(new String[indices.size()]),",");
		return span;
	}
	
	
	private void registerSRelationMarkable(SaltExtendedMarkable markable, SRelation key){
		this.registeredSRelationsMarkables.put(key, markable);		
	}
	
	private SaltExtendedMarkable getSRelationMarkable(SRelation key) throws MMAX2WrapperException{
		SaltExtendedMarkable markable = this.registeredSRelationsMarkables.get(key);
		if(markable == null){
			if (key instanceof SSpanningRelation){
				markable = mapSpanningRelation((SSpanningRelation) key);
			}else if (key instanceof SDominanceRelation){
				markable = mapDominanceRelation((SDominanceRelation) key);
			}else if (key instanceof STextualRelation){
				markable = mapTextualRelation((STextualRelation) key);
			}else if (key instanceof SPointingRelation){
				markable = mapPointingRelation((SPointingRelation) key);
			}else{
				throw new PepperModuleException(this, "Developper error Unknown Type of SRelation => "+key.getClass());
			}
			//System.out.println("Registering "+markable+" for "+key);
			registerSRelationMarkable(markable, key);
		}
		
		return markable;
	}
	
	private void registerSNodeMarkable(SaltExtendedMarkable markable, SNode key){
		this.registeredSNodesMarkables.put(key, markable);
	}
	
	private SaltExtendedMarkable getSNodeMarkable(SNode key) throws MMAX2WrapperException{
		SaltExtendedMarkable markable = this.registeredSNodesMarkables.get(key);
		if(markable == null){
			if(key instanceof SSpan){
				markable = mapSpan((SSpan) key);
			}else if (key instanceof SToken){
				markable = mapSToken((SToken) key);
			}else if (key instanceof SStructure){
				markable = mapStruct((SStructure) key);
			}else if (key instanceof STextualDS){
				markable = mapSTextualDS((STextualDS) key);
			}else{
				throw new PepperModuleException(this, "Developper error Unknown Type of SNode => "+key.getClass());
			}			
			registerSNodeMarkable(markable, key);
		}
		
		return markable;
	}

	
	/**
	 *  GLOBAl METHODS 
	 */	
	
	
	@Override
	public DOCUMENT_STATUS mapSCorpus() {
		return(DOCUMENT_STATUS.COMPLETED);
	}
		
	/**
	 * Converts an SDocument into a Salt enhanced Mmax2 document.
	 * @param corpus The Salt enhanced Mmax2 corpus into which the converted Salt enhanced Mmax2 document should fo 
	 * @param sDocument the SDocument to convert
	 * @param factory the SaltExtendedDocumentFactory to use for creating documents
	 * @param schemeFactory the SaltExtendedDocumentFactory to use for creating Schemes
	 * @throws MMAX2ExporterException
	 * @throws MMAX2WrapperException
	 */
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		// this function goes through all pieces of data in a SDocument and launch accordingly the specialized functions below		
		String documentName = getDocument().getName();
		this.spanStextualRelationCorrespondance = new Hashtable<STextualRelation, Integer>();
		this.spanStextualDSCorrespondance = new Hashtable<STextualDS, ArrayList<String>>();
		this.registeredSNodesMarkables = new Hashtable<SNode, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		this.registeredSRelationsMarkables = new Hashtable<SRelation, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		this.registeredSLayerMarkables = new HashMap<SLayer, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		
		this.sContainerMarkables = new Hashtable<Object, Hashtable<Scheme,SaltExtendedMarkableContainer>>();
		
		this.document = factory.newDocument(documentName);
		
		// it deals with STextualDs
		List<STextualDS> sTextualDSList = new ArrayList<STextualDS>(getDocument().getDocumentGraph().getTextualDSs());
		List<STextualRelation> sTextualRelationList = new ArrayList<STextualRelation>(getDocument().getDocumentGraph().getTextualRelations());
		int compteurId = 0;
		{
			Hashtable<STextualDS,ArrayList<STextualRelation>> correspondanceDsTextualRelations = new Hashtable<STextualDS,ArrayList<STextualRelation>>();
			for(STextualRelation sTextualRelation : sTextualRelationList){
				ArrayList<STextualRelation> listRelation = correspondanceDsTextualRelations.get(sTextualRelation.getTarget());
				if(listRelation == null){
					listRelation = new ArrayList<STextualRelation>();
					correspondanceDsTextualRelations.put(sTextualRelation.getTarget(),listRelation);
				}
				listRelation.add(sTextualRelation);
			}
			
			
			for(STextualDS sTextualDS : sTextualDSList){
				String sText = sTextualDS.getText();
					
				ArrayList<STextualRelation> listRelation = correspondanceDsTextualRelations.get(sTextualDS);
				
				STextualRelation[] coveredCarachter = new STextualRelation[sText.length()];
				if(listRelation != null){
					for(STextualRelation sTextualRelation : listRelation){
						int start = sTextualRelation.getStart();
						int end   = sTextualRelation.getEnd();
						for(int i = start; i < end; i++) {
							if(coveredCarachter[i] != null)
								throw new PepperModuleException("Unexportable Salt Document => Two STextualRelation span a same caracter/token at position '"+i
											+"':\n"+sTextualRelation+"\n"+coveredCarachter[i]
											+"\nAs Stokens and STextualRelations are, when available, mapped to MMax2 Base Data Units, they are not allowed to overlap.");
								//	logger.warn("Something is off with the Salt Object => two STextualRelation span a same caracter/token at position '"+i+"':\n"+sTextualRelation+"\n"+coveredCarachter[i]);
							coveredCarachter[i] = sTextualRelation;
						}
					}
				}
				
				ArrayList<String> spansTextualDS = new ArrayList<String>();
				for(int i = 0; i < coveredCarachter.length; i++){
					compteurId++;
					if(coveredCarachter[i] != null){
						String text = sText.substring(coveredCarachter[i].getStart(),coveredCarachter[i].getEnd());
						document.addBaseDataUnit(document.newBaseDataUnit("word_"+compteurId,text));
						this.spanStextualRelationCorrespondance.put(coveredCarachter[i],compteurId);
						i = coveredCarachter[i].getEnd() - 1;
					}else{
						document.addBaseDataUnit(document.newBaseDataUnit("word_"+compteurId,sText.substring(i,i+1)));
					}
					spansTextualDS.add("word_"+compteurId);
				}
				this.spanStextualDSCorrespondance.put(sTextualDS, spansTextualDS);
			}
		}
		
		// The order of exporting the things can impact on the way an Mmax2 => Mmax2 conversion can look on a diff
		try{
			mapSDocument(compteurId);
			
			for(SLayer sLayer: new ArrayList<SLayer>(getDocument().getDocumentGraph().getLayers())){
				mapSLayer(sLayer,compteurId);
			}
			
			ArrayList<SNode> allSnodes = new ArrayList<SNode>();
			ArrayList<SRelation> allSrelations = new ArrayList<SRelation>();
			
			for(STextualDS sTextualDs : sTextualDSList){
				getSNodeMarkable(sTextualDs);
				allSnodes.add(sTextualDs);
			}
				
			for(STextualRelation sTextualRelation: sTextualRelationList){
				getSRelationMarkable(sTextualRelation);
				allSrelations.add(sTextualRelation);
			}
			
			for(SToken sToken: getDocument().getDocumentGraph().getTokens()){
				getSNodeMarkable(sToken);
				allSnodes.add(sToken);
			}
			
			
			for(SSpanningRelation sSpanningRelation: getDocument().getDocumentGraph().getSpanningRelations()){
				getSRelationMarkable(sSpanningRelation);
				allSrelations.add(sSpanningRelation);
			}
			
			for(SSpan sSpan: getDocument().getDocumentGraph().getSpans()){
				getSNodeMarkable(sSpan);
				allSnodes.add(sSpan);
			}
			
			for(SDominanceRelation sDominanceRelation: getDocument().getDocumentGraph().getDominanceRelations()){
				getSRelationMarkable(sDominanceRelation);
				allSrelations.add(sDominanceRelation);
			}
			
			for(SStructure sStruct: getDocument().getDocumentGraph().getStructures()){
				getSNodeMarkable(sStruct);
				allSnodes.add(sStruct);
			}
			
			for(SPointingRelation sPointer: getDocument().getDocumentGraph().getPointingRelations()){
				getSRelationMarkable(sPointer);
				allSrelations.add(sPointer);
			}
				
			// Records if the snode belongs to a given set of Slayers
			for(SNode sNode: allSnodes){
				SaltExtendedMarkable markable = getSNodeMarkable(sNode);
				Set<SLayer> sLayers = sNode.getLayers();
				
				mapSMetaAnnotations(markable.getSName(),markable.getId(),sNode,markable.getId(),markable.getSpan(),markable.getFactory().getScheme().getName(),sLayers);
				mapSAnnotations(markable.getSName(),markable.getId(),sNode,markable.getId(),markable.getSpan(),markable.getFactory().getScheme().getName(),sLayers);
				
				if(sLayers.size() != 0)
					mapSLayersToMarkable(markable,markable.getFactory().getScheme().getName(),sLayers);
			}
		
			// Records if the srelation has a certain set of STypes and if it  belongs to a given set of Slayers
			for (SRelation sRelation: allSrelations){
				SaltExtendedMarkable markable = getSRelationMarkable(sRelation);
				Set<SLayer> sLayers = sRelation.getLayers();		
				mapSMetaAnnotations(markable.getSName(),markable.getId(),sRelation,markable.getId(),markable.getSpan(),markable.getFactory().getScheme().getName(),sLayers);
				mapSAnnotations(markable.getSName(),markable.getId(),sRelation,markable.getId(),markable.getSpan(),markable.getFactory().getScheme().getName(),sLayers);
									
				if(sLayers.size() != 0){
					mapSLayersToMarkable(markable,markable.getFactory().getScheme().getName(),sLayers);
				}
				mapSTypesToMarkable(markable,markable.getFactory().getScheme().getName(),sRelation.getType());
			}
		}catch (MMAX2WrapperException e){
			throw new PepperModuleException(this,"",e);
		}
		this.corpus.addDocument(document);
		
		try {
			SaltExtendedFileGenerator.outputDocument(corpus, document);
		} catch (Exception e) {
			throw new PepperModuleException(this,"",e);
		} 
		
		return(DOCUMENT_STATUS.COMPLETED);
	}
	
	/**
	 *  SPECIALIZED METHODS 
	 */		

	// function specialized in SDocument information
	private void mapSDocument(int lastBaseUnitId) throws MMAX2WrapperException{
		String markableSPan =  makeSpan(1,lastBaseUnitId);
		{
			// The SDocument itself
			String markableId = getNewId();
			
			Scheme scheme = getScheme(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT);
			String sName = getDocument().getName();
			String sId = getDocument().getId();
			
			SaltExtendedMarkable markable = getMarkable(scheme,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT,sName,sId);
			this.document.addMarkable(markable);
	
			mapSMetaAnnotations(sName,sId, getDocument(), markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT,null);
			mapSAnnotations(sName,sId, getDocument(), markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT,null);
		}
		{
			// The graph of the SDocument 
			String markableId = getNewId();
			Scheme scheme = getScheme(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH);
			String sName = getDocument().getDocumentGraph().getName();
			String sId = getDocument().getDocumentGraph().getId();
			
			SaltExtendedMarkable markable = getMarkable(scheme,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH,sName,sId);
			this.document.addMarkable(markable);
	
			mapSMetaAnnotations(sName,sId, getDocument().getDocumentGraph(), markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH,null);
			mapSAnnotations(sName,sId, getDocument().getDocumentGraph(), markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH,null);
		}
	}
	
	// function specialized in SLayer information
	private void mapSLayer(SLayer sLayer,int lastBaseUnitId) throws MMAX2WrapperException{
		String markableId = getNewId();
		String markableSPan = makeSpan(1,lastBaseUnitId);
		
		Scheme scheme = getScheme(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER);
		String sName = sLayer.getName();
		String sId = sLayer.getId();
		
		SaltExtendedMarkable markable = getMarkable(scheme,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER,sName,sId);
		this.registeredSLayerMarkables.put(sLayer, markable);
		this.document.addMarkable(markable);

		mapSMetaAnnotations(sName,sId,sLayer,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER,null);
		mapSAnnotations(sName,sId,sLayer,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER,null);
	}
	
	// function specialized in SToken information
	private SaltExtendedMarkable mapSToken(SToken sToken) throws MMAX2WrapperException {	
		return createMarkableForSNode(getNewId(),"",sToken,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STOKEN);
	}
	
	// function specialized in SSpan information
	private SaltExtendedMarkable mapSpan(SSpan sSpan) throws MMAX2WrapperException {	
		return createMarkableForSNode(getNewId(),"",sSpan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPAN);
	}
	
	// function specialized in SStructure information
	// Because the span of the SStructure depends on the things it "sdominates", we have to make recursive calls
	// also we need to finish the mapping of the SDominance relation it is connected with
	private SaltExtendedMarkable mapStruct(SStructure struct) throws MMAX2WrapperException {
		ArrayList<SaltExtendedMarkable> sDomRelMarkableList = new ArrayList<SaltExtendedMarkable>();
		Hashtable<SaltExtendedMarkable,SRelationMapping> sDomRelMarkableHash = new Hashtable<SaltExtendedMarkable,SRelationMapping>(); 
		ArrayList<String> spans = new ArrayList<String>();
		for(SRelation rel: getDocument().getDocumentGraph().getOutRelations(struct.getId())){
			if(rel instanceof SDominanceRelation){
				SDominanceRelation sDomRel = (SDominanceRelation) rel;
				SaltExtendedMarkable sDomRelMarkable = getSRelationMarkable(sDomRel);
				spans.add(sDomRelMarkable.getSpan());
				SRelationMapping validated = matchSRelation(sDomRel);
				sDomRelMarkableList.add(sDomRelMarkable);
				if(validated != null){
					sDomRelMarkableHash.put(sDomRelMarkable,validated);
				}
			}
		}
		
		SaltExtendedMarkable markable = createMarkableForSNode(getNewId(),makeSpan(spans),struct,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSTRUCT);		
		
		for(SaltExtendedMarkable sDomRelMarkable: sDomRelMarkableList){
			if(sDomRelMarkableHash.containsKey(sDomRelMarkable)){
				SRelationMapping validated = sDomRelMarkableHash.get(sDomRelMarkable);
			
				SaltExtendedMarkable containerSourceMarkable = getSContainerMarkable(markable,validated.getSourceAssociatedSchemeName(),
						markable.getSpan(),markable.getSName(),markable.getId(),markable.getId(),markable.getFactory().getScheme().getName());
				
				addPointerAttribute(containerSourceMarkable, validated.getSourceAssociatedSchemeName(), validated.getTargetAssociatedSchemeName(), 
						validated.getPointedAssociatedAttributeName(), sDomRelMarkable.getAttribute("id_target").getValue());
				sDomRelMarkable.removeAttribute(sDomRelMarkable.getAttribute("id_target"));
				
				addFreetextAttribute(sDomRelMarkable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"struct_attr",validated.getPointedAssociatedAttributeName());
				addFreetextAttribute(sDomRelMarkable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"struct_scheme",validated.getSourceAssociatedSchemeName());
				addFreetextAttribute(sDomRelMarkable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"struct",containerSourceMarkable.getId());
			}else{
				addPointerAttribute(sDomRelMarkable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSTRUCT,"struct",markable.getId());
			}
		}
		
		return markable;
	}
	
	// function specialized in STextualDS information
	private SaltExtendedMarkable mapSTextualDS(STextualDS sTextualDs) throws MMAX2WrapperException {
		return createMarkableForSNode(getNewId(),makeSpan(this.spanStextualDSCorrespondance.get(sTextualDs)),sTextualDs,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUALDS);
	}
	
	// function specialized in SDominanceRelation information
	// we start the mapping but finish it in the mapStruct function because we can not create a SStruct before the SDominanceRelation (because if the SStruct mmax2 span)
	// some informations related to the SSTruct are thus not available when processing the SDominanceRelation
	private SaltExtendedMarkable mapDominanceRelation(SDominanceRelation domRel) throws MMAX2WrapperException  {
		SaltExtendedMarkable targetMarkable = getSNodeMarkable(domRel.getTarget());

		String markableId = getNewId();
		String markableSPan = targetMarkable.getSpan();
		
		SaltExtendedMarkable markable = createMarkableForSRelation(markableId,markableSPan,domRel,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL);
		SRelationMapping validated = matchSRelation(domRel);
		if(validated == null){		
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"target",targetMarkable.getId());
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"target_scheme",targetMarkable.getFactory().getScheme().getName());
		}else{
			SaltExtendedMarkable containerTargetMarkable = getSContainerMarkable(targetMarkable,validated.getTargetAssociatedSchemeName(),
					targetMarkable.getSpan(),targetMarkable.getSName(),targetMarkable.getId(),targetMarkable.getId(),targetMarkable.getFactory().getScheme().getName());
			
			addFreetextAttribute(markable, SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"id_target",containerTargetMarkable.getId());
		}
		
		return markable;
	}
	
	// function specialized in SPointingRelation information
	// the mapping is done below
	private SaltExtendedMarkable mapPointingRelation(SPointingRelation pointRel) throws MMAX2WrapperException{
		SaltExtendedMarkable sourceMarkable = getSNodeMarkable(pointRel.getSource());
		SaltExtendedMarkable targetMarkable = getSNodeMarkable(pointRel.getTarget());
	
		String markableId = getNewId();
		String markableSPan = sourceMarkable.getSpan();
		SaltExtendedMarkable markable = createMarkableForSRelation(markableId,markableSPan,pointRel,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL);
		
		SRelationMapping validated = matchSRelation(pointRel);
		if(validated == null){		
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"source_scheme",sourceMarkable.getFactory().getScheme().getName());
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"source",sourceMarkable.getId());
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"target_scheme",targetMarkable.getFactory().getScheme().getName());
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"target",targetMarkable.getId());
		}else{
			SaltExtendedMarkable containerSourceMarkable = getSContainerMarkable(sourceMarkable,validated.getSourceAssociatedSchemeName(),
					sourceMarkable.getSpan(),sourceMarkable.getSName(),sourceMarkable.getId(),sourceMarkable.getId(),sourceMarkable.getFactory().getScheme().getName());
			
			SaltExtendedMarkable containerTargetMarkable = getSContainerMarkable(targetMarkable,validated.getTargetAssociatedSchemeName(),
					targetMarkable.getSpan(),targetMarkable.getSName(),targetMarkable.getId(),targetMarkable.getId(),targetMarkable.getFactory().getScheme().getName());
			
			addPointerAttribute(containerSourceMarkable, validated.getSourceAssociatedSchemeName(), validated.getTargetAssociatedSchemeName(), 
					validated.getPointedAssociatedAttributeName(), containerTargetMarkable.getId());
			
			addFreetextAttribute(markable, SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"source_attr", validated.getPointedAssociatedAttributeName());
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"source_scheme",validated.getSourceAssociatedSchemeName());
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"source",containerSourceMarkable.getId());
		}
		
		return markable;
	}
	
	// function specialized in SSpanningRelation information
	private SaltExtendedMarkable mapSpanningRelation(SSpanningRelation sSpanningRel) throws MMAX2WrapperException{
		SaltExtendedMarkable tokenMarkable = getSNodeMarkable(sSpanningRel.getTarget()); 
		SaltExtendedMarkable spanMarkable = getSNodeMarkable(sSpanningRel.getSource());
	
		String markableSpan = tokenMarkable.getSpan();
		String markableId = getNewId();
		
		spanMarkable.addSpan(markableSpan);

		SaltExtendedMarkable markable = createMarkableForSRelation(markableId,markableSpan,sSpanningRel,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPANNING_REL);
		addPointerAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPANNING_REL,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPAN,"source_span",spanMarkable.getId());
		addPointerAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPANNING_REL,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STOKEN,"target_token",tokenMarkable.getId());
		
		return markable;
	}
	
	// function specialized in STextualRelation information
	private SaltExtendedMarkable mapTextualRelation(STextualRelation sTextualRelation) throws MMAX2WrapperException{
		SaltExtendedMarkable markable= null;
		SaltExtendedMarkable tokenMarkable = getSNodeMarkable(sTextualRelation.getSource());
		SaltExtendedMarkable textualDsMarkable = getSNodeMarkable(sTextualRelation.getTarget());
			
		String markableId = getNewId();
		Integer i= spanStextualRelationCorrespondance.get(sTextualRelation);
		String markableSPan = makeSpan(i);
		tokenMarkable.setSpan(markableSPan);
		
		markable = createMarkableForSRelation(markableId,markableSPan,sTextualRelation,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUAL_REL);
				
		addPointerAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUAL_REL,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STOKEN,"target_token",tokenMarkable.getId());
		addPointerAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUAL_REL,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUALDS,"target_textual_ds",textualDsMarkable.getId());
	
		return markable;
	}
	
	/**
	 *  METHODS FOR HANDLING ANNOTATIONS OVER ELEMENTS
	 */
	
	
	public SaltExtendedMarkable createMarkableForSNode(String markableId, String markableSpan, SNode sNode, String markableSKind) throws MMAX2WrapperException{
		Scheme scheme = getScheme(markableSKind);
		String sName = sNode.getName();
		String sId = sNode.getId();
		
		SaltExtendedMarkable markable = getMarkable(scheme,markableId,markableSpan,markableSKind,sName,sId);
		this.document.addMarkable(markable);

		return markable;
	}
	
	
	public SaltExtendedMarkable createMarkableForSRelation(String markableId, String markableSpan, SRelation sRelation, String markableSKind) throws MMAX2WrapperException{
		Scheme scheme = getScheme(markableSKind);
		String sName = sRelation.getName();
		String sId = sRelation.getId();
	
		SaltExtendedMarkable markable = getMarkable(scheme,markableId,markableSpan,markableSKind,sName,sId);
		this.document.addMarkable(markable);
	
		return markable;
	}
	
	public void addPointerAttribute(SaltExtendedMarkable markable, String schemeName,String targetScheme, String attributeName, String targetId) throws MMAX2WrapperException{
		Scheme scheme = getScheme(schemeName);
		MarkablePointerAttributeFactory pointerAttributeFactory = getMarkablePointerAttributeFactory(scheme,attributeName,targetScheme);		
		markable.addAttribute(pointerAttributeFactory.newAttribute(targetId));	
	}
	
	public void addNominalAttribute(SaltExtendedMarkable markable, String schemeName,  String attributeName, String attributeValue) throws MMAX2WrapperException{
		Scheme scheme = getScheme(schemeName);

		MarkableNominalAttributeFactory typeAttributeFactory = getMarkableNominalAttributeFactory(scheme,attributeName);	
		if(!typeAttributeFactory.isValueAccepted(attributeValue)){
			typeAttributeFactory.enableValue(attributeValue);
		}
		markable.addAttribute(typeAttributeFactory.newAttribute(attributeValue));
	}
	
	public void addFreetextAttribute(SaltExtendedMarkable markable, String schemeName,  String attributeName, String attributeValue) throws MMAX2WrapperException{
		Scheme scheme = getScheme(schemeName);
		MarkableFreetextAttributeFactory typeAttributeFactory = getMarkableFreetextAttributeFactory(scheme,attributeName);	
		markable.addAttribute(typeAttributeFactory.newAttribute(attributeValue));
	}
	
	public void addSetAttribute(SaltExtendedMarkable markable, String schemeName, String attributeName, String attributeValue) throws MMAX2WrapperException{
		Scheme scheme = getScheme(schemeName);
		MarkableSetAttributeFactory typeAttributeFactory = getMarkableSetAttributeFactory(scheme,attributeName);	
		markable.addAttribute(typeAttributeFactory.newAttribute(attributeValue));
	}
	
	private void mapSAnnotations(String sName, 
			String sId, 
			SAnnotationContainer  sElem, 
			String markableId, 
			String markableSpan, 
			String schemeBaseName,
			Set<SLayer> sLayers) throws MMAX2WrapperException{

		for (SAnnotation sAnnotation : sElem.getAnnotations()){
			mapAnnotations(sElem,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SANNOTATION,sName,sId,markableId,markableSpan,schemeBaseName,sAnnotation,sLayers);
		}
	}
	
	private void mapSMetaAnnotations(String sName, 
									String sId, 
									SAnnotationContainer  sElem, 
									String markableId, 
									String markableSpan, 
									String schemeBaseName,
									Set<SLayer> sLayers) throws MMAX2WrapperException{
		
		for (SMetaAnnotation sAnnotation : sElem.getMetaAnnotations()){
			mapAnnotations(sElem,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SMETAANNOTATION,sName,sId,markableId,markableSpan,schemeBaseName,sAnnotation,sLayers);	
		}
	}
	

	private void mapAnnotations(Object sELem,
								String sType, 
								String sName, 
								String sId, 
								String idRef, 
								String span, 
								String schemeBaseName,
								SAbstractAnnotation annotation,
								Set<SLayer> sLayers) throws MMAX2WrapperException{
		
		String attributeName = annotation.getName();
		String attributeValue = annotation.getValue_STEXT();
		String attributeNs = annotation.getNamespace();
		
		String schemeName = schemeBaseName+"_"+sType;
		Scheme scheme = getScheme(schemeName);
		SaltExtendedMarkable markable = getMarkable(scheme,getNewId(),span,sType,sName,sId);
	
		
		addFreetextAttribute(markable,schemeName,"namespace",attributeNs);
		addFreetextAttribute(markable,schemeName,"attr_name",attributeName);
		
		SaltExtendedMarkable containerMarkable = null;
		// the mapping of attributes is done here
		SAnnotationMapping validated = matchSNode(annotation,sLayers);
		if(validated != null){
			containerMarkable = getSContainerMarkable(sELem,validated.getAssociatedSchemeName(),span,sName,sId,idRef,schemeBaseName);
			String attributeContainerName = validated.getAssociatedAttributeName();
			if(!attributeContainerName.equals("@none")){
				if(containerMarkable.getAttribute(attributeContainerName) != null){
					throw new PepperModuleException(this, "Matched markable '"+markable+"' has already an attribute '"+attributeContainerName+"'");
				}
				addFreetextAttribute(containerMarkable,validated.getAssociatedSchemeName(),attributeContainerName,attributeValue);
			}
		}
		
		if(containerMarkable != null){
			addFreetextAttribute(markable,schemeName,"target_markable",containerMarkable.getId());
			addFreetextAttribute(markable,schemeName,"container_scheme",validated.getAssociatedSchemeName());
			addFreetextAttribute(markable,schemeName,"container_attr",validated.getAssociatedAttributeName());
		}else{
			addFreetextAttribute(markable,schemeName,"target_markable",idRef);
			addFreetextAttribute(markable,schemeName,"value",attributeValue);
		}
		
		document.addMarkable(markable);
	}
	
	// function to record if a given SElement belonged to a given SLayer
	private void mapSLayersToMarkable(SaltExtendedMarkable markable, String markableSKind, Set<SLayer> sLayers) throws MMAX2WrapperException{
		String schemeName = markableSKind + "_slayer_link";
		Scheme scheme = getScheme(schemeName);
		for(SLayer sLayer: sLayers){
			SaltExtendedMarkable linkMarkable = getMarkable(scheme,getNewId(),markable.getSpan(),SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER_LINK,markable.getSName(),markable.getId());
			SaltExtendedMarkable sLayerMarkable = this.registeredSLayerMarkables.get(sLayer);
			
			addPointerAttribute(linkMarkable,schemeName,markableSKind,"selement",markable.getId());
			addPointerAttribute(linkMarkable,schemeName,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER,"slayer",sLayerMarkable.getId());
			document.addMarkable(linkMarkable);
		}
	}
	
	// function to record if a given SRelation has certain STypes
	private void mapSTypesToMarkable(SaltExtendedMarkable markable, String markableSKind, String sType) throws MMAX2WrapperException{		
		String schemeName = markableSKind + "_stype_link";
		Scheme scheme = getScheme(schemeName);
		SaltExtendedMarkable linkMarkable = getMarkable(scheme,getNewId(),markable.getSpan(),SaltExtendedMmax2Infos.SALT_INFO_TYPE_STYPE_LINK,markable.getSName(),markable.getId());
		
		addPointerAttribute(linkMarkable,schemeName,markableSKind,"selement",markable.getId());
		addFreetextAttribute(linkMarkable,schemeName,"stype",sType);
		
		this.document.addMarkable(linkMarkable);
	}
	
	private Scheme getScheme(String schemeName){
		Scheme scheme = this.corpus.getScheme(schemeName);
		if(scheme == null){
			scheme = this.schemeFactory.newScheme(schemeName); 
			this.corpus.addScheme(scheme);
		}		
		return scheme;
	}
	
	private SaltExtendedMarkable getMarkable(Scheme scheme, String markableId, String markableSpan, String sType, String sName, String sId){
		SaltExtendedMarkableFactory markableFactory = this.document.getFactory().getMarkableFactory(scheme);
		if(markableFactory == null){
			markableFactory = new SaltExtendedMarkableFactory(scheme, this.documentBuilder);
			this.document.getFactory().addMarkableFactory(markableFactory);		
		}
		SaltExtendedMarkable markable = markableFactory.newMarkable(markableId,markableSpan, new ArrayList<MarkableAttribute>(), sType, sName, sId);
		return markable;
	}
	
	// function to generate SContainer that are aliases used for the mapping
	private SaltExtendedMarkable getSContainerMarkable(Object sElem, String schemeName, String span, String sName, String sId, String containedId, String containedScheme){
		if(!this.sContainerMarkables.containsKey(sElem)){
			this.sContainerMarkables.put(sElem,new Hashtable<Scheme, SaltExtendedMarkableContainer>());
		}
		Hashtable<Scheme,SaltExtendedMarkableContainer> associatedMarkables = this.sContainerMarkables.get(sElem);
		
		SaltExtendedMarkableContainer containerMarkable = null;
		Scheme associatedScheme = getScheme(schemeName);
		if(!associatedMarkables.containsKey(associatedScheme)){
			SaltExtendedMarkableFactory markableFactory = this.document.getFactory().getMarkableFactory(associatedScheme);
			if(markableFactory == null){
				markableFactory = new SaltExtendedMarkableFactory(associatedScheme, this.documentBuilder);
				this.document.getFactory().addMarkableFactory(markableFactory);		
			}
			containerMarkable = markableFactory.newMarkableContainer(getNewId(),span, new ArrayList<MarkableAttribute>(), SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER, sName, sId,containedId,containedScheme);
			associatedMarkables.put(associatedScheme,containerMarkable);
			document.addMarkable(containerMarkable);
		}else{
			containerMarkable = associatedMarkables.get(associatedScheme);		
		}
		
		return containerMarkable;		
	}
	
	
	private MarkableNominalAttributeFactory getMarkableNominalAttributeFactory(Scheme scheme,String attributeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkableNominalAttributeFactory(scheme, attributeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}
		return (MarkableNominalAttributeFactory) attributeFactory;
	}
	
	private MarkablePointerAttributeFactory getMarkablePointerAttributeFactory(Scheme scheme,String attributeName, String targetSchemeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkablePointerAttributeFactory(scheme,attributeName,targetSchemeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}		
		return (MarkablePointerAttributeFactory) attributeFactory;
	}
	
	private MarkableFreetextAttributeFactory getMarkableFreetextAttributeFactory(Scheme scheme,String attributeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkableFreetextAttributeFactory(scheme,attributeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}		
		return (MarkableFreetextAttributeFactory) attributeFactory;
	}
	
	private MarkableSetAttributeFactory getMarkableSetAttributeFactory(Scheme scheme,String attributeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkableSetAttributeFactory(scheme,attributeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}		
		return (MarkableSetAttributeFactory) attributeFactory;
	}
	
	// function to check if some conditions over an attribute is validated (some mapping should be launched)
	public SAnnotationMapping matchSNode(SAbstractAnnotation annotation, Set<SLayer> sLayers){
		SAnnotationMapping validated = null;
		for(SAnnotationMapping mapping: this.sannotationmappings){
			if(mapping.isMatched(annotation,sLayers)){
				if(validated != null){
					throw new PepperModuleException(this, "Ambiguous matching confitions '"+validated+"' and '"+mapping+"' have both matched on '"+
							annotation+"/"+sLayers+"'");
				}
			
				validated = mapping;
			}
		}			
		return validated;	
	}
	
	// function to check if some conditions over a SRelation is validated (some mapping should be launched)
	public SRelationMapping matchSRelation(SRelation sRelation){
		SRelationMapping  validated = null;
		
		for(SRelationMapping mapping: this.srelationMappings){
			if(mapping.isMatched(sRelation)){
				if(validated != null){
					throw new PepperModuleException(this, "Ambiguous matching confitions '"+validated+"' and '"+mapping+"' have both matched '"+sRelation+"'");
				}
				validated = mapping;
			}
		}
	
		return validated;	
	}
}	

