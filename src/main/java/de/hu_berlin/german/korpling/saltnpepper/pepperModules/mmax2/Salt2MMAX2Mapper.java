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

import javax.xml.parsers.DocumentBuilder;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkable;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkableContainer;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAbstractAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotatableElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotatableElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import eurac.commul.annotations.mmax2wrapper.MMAX2WrapperException;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableAttributeFactory.MarkableAttribute;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableFreetextAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableNominalAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkablePointerAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableSetAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;


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
		String documentName = getSDocument().getSName();
		this.spanStextualRelationCorrespondance = new Hashtable<STextualRelation, Integer>();
		this.spanStextualDSCorrespondance = new Hashtable<STextualDS, ArrayList<String>>();
		this.registeredSNodesMarkables = new Hashtable<SNode, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		this.registeredSRelationsMarkables = new Hashtable<SRelation, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		this.registeredSLayerMarkables = new HashMap<SLayer, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		
		this.sContainerMarkables = new Hashtable<Object, Hashtable<Scheme,SaltExtendedMarkableContainer>>();
		
		this.document = factory.newDocument(documentName);
		
		// it deals with STextualDs
		EList<STextualDS> sTextualDSList = new BasicEList<STextualDS>(getSDocument().getSDocumentGraph().getSTextualDSs());
		EList<STextualRelation> sTextualRelationList = new BasicEList<STextualRelation>(getSDocument().getSDocumentGraph().getSTextualRelations());
		int compteurId = 0;
		{
			Hashtable<STextualDS,ArrayList<STextualRelation>> correspondanceDsTextualRelations = new Hashtable<STextualDS,ArrayList<STextualRelation>>();
			for(STextualRelation sTextualRelation : sTextualRelationList){
				ArrayList<STextualRelation> listRelation = correspondanceDsTextualRelations.get(sTextualRelation.getSTextualDS());
				if(listRelation == null){
					listRelation = new ArrayList<STextualRelation>();
					correspondanceDsTextualRelations.put(sTextualRelation.getSTextualDS(),listRelation);
				}
				listRelation.add(sTextualRelation);
			}
			
			
			for(STextualDS sTextualDS : sTextualDSList){
				String sText = sTextualDS.getSText();
					
				ArrayList<STextualRelation> listRelation = correspondanceDsTextualRelations.get(sTextualDS);
				
				STextualRelation[] coveredCarachter = new STextualRelation[sText.length()];
				if(listRelation != null){
					for(STextualRelation sTextualRelation : listRelation){
						int start = sTextualRelation.getSStart();
						int end   = sTextualRelation.getSEnd();
						for(int i = start; i < end; i++) {
							//if(coveredCarachter[i] != null)
							//	logger.warn("Something is off with the Salt Object => two STextualRelation span a same caracter/token at position '"+i+"':\n"+sTextualRelation+"\n"+coveredCarachter[i]);
							coveredCarachter[i] = sTextualRelation;
						}
					}
				}
				
				ArrayList<String> spansTextualDS = new ArrayList<String>();
				for(int i = 0; i < coveredCarachter.length; i++){
					compteurId++;
					if(coveredCarachter[i] != null){
						String text = sText.substring(coveredCarachter[i].getSStart(),coveredCarachter[i].getSEnd());
						document.addBaseDataUnit(document.newBaseDataUnit("word_"+compteurId,text));
						this.spanStextualRelationCorrespondance.put(coveredCarachter[i],compteurId);
						i = coveredCarachter[i].getSEnd() - 1;
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
			
			for(SLayer sLayer: new BasicEList<SLayer>(getSDocument().getSDocumentGraph().getSLayers())){
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
			
			for(SToken sToken: getSDocument().getSDocumentGraph().getSTokens()){
				getSNodeMarkable(sToken);
				allSnodes.add(sToken);
			}
			
			
			for(SSpanningRelation sSpanningRelation: getSDocument().getSDocumentGraph().getSSpanningRelations()){
				getSRelationMarkable(sSpanningRelation);
				allSrelations.add(sSpanningRelation);
			}
			
			for(SSpan sSpan: getSDocument().getSDocumentGraph().getSSpans()){
				getSNodeMarkable(sSpan);
				allSnodes.add(sSpan);
			}
			
			for(SDominanceRelation sDominanceRelation: getSDocument().getSDocumentGraph().getSDominanceRelations()){
				getSRelationMarkable(sDominanceRelation);
				allSrelations.add(sDominanceRelation);
			}
			
			for(SStructure sStruct: getSDocument().getSDocumentGraph().getSStructures()){
				getSNodeMarkable(sStruct);
				allSnodes.add(sStruct);
			}
			
			for(SPointingRelation sPointer: getSDocument().getSDocumentGraph().getSPointingRelations()){
				getSRelationMarkable(sPointer);
				allSrelations.add(sPointer);
			}
				
			// Records if the snode belongs to a given set of Slayers
			for(SNode sNode: allSnodes){
				SaltExtendedMarkable markable = getSNodeMarkable(sNode);
				EList<SLayer> sLayers = sNode.getSLayers();
				
				mapSMetaAnnotations(markable.getSName(),markable.getSId(),sNode,markable.getId(),markable.getSpan(),markable.getFactory().getScheme().getName(),sLayers);
				mapSAnnotations(markable.getSName(),markable.getSId(),sNode,markable.getId(),markable.getSpan(),markable.getFactory().getScheme().getName(),sLayers);
				
				if(sLayers.size() != 0)
					mapSLayersToMarkable(markable,markable.getFactory().getScheme().getName(),sLayers);
			}
		
			// Records if the srelation has a certain set of STypes and if it  belongs to a given set of Slayers
			for (SRelation sRelation: allSrelations){
				SaltExtendedMarkable markable = getSRelationMarkable(sRelation);
				EList<SLayer> sLayers = sRelation.getSLayers();		
				mapSMetaAnnotations(markable.getSName(),markable.getSId(),sRelation,markable.getId(),markable.getSpan(),markable.getFactory().getScheme().getName(),sLayers);
				mapSAnnotations(markable.getSName(),markable.getSId(),sRelation,markable.getId(),markable.getSpan(),markable.getFactory().getScheme().getName(),sLayers);
									
				if(sLayers.size() != 0)
					mapSLayersToMarkable(markable,markable.getFactory().getScheme().getName(),sLayers);
						
				EList<String> sTypes = sRelation.getSTypes();
				if(sTypes != null)
					mapSTypesToMarkable(markable,markable.getFactory().getScheme().getName(),sTypes);
				
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
			String sName = getSDocument().getSName();
			String sId = getSDocument().getSId();
			
			SaltExtendedMarkable markable = getMarkable(scheme,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT,sName,sId);
			this.document.addMarkable(markable);
	
			mapSMetaAnnotations(sName,sId, getSDocument(), markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT,null);
			mapSAnnotations(sName,sId, getSDocument(), markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT,null);
		}
		{
			// The graph of the SDocument 
			String markableId = getNewId();
			Scheme scheme = getScheme(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH);
			String sName = getSDocument().getSDocumentGraph().getSName();
			String sId = getSDocument().getSDocumentGraph().getSId();
			
			SaltExtendedMarkable markable = getMarkable(scheme,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH,sName,sId);
			this.document.addMarkable(markable);
	
			mapSMetaAnnotations(sName,sId, getSDocument().getSDocumentGraph(), markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH,null);
			mapSAnnotations(sName,sId, getSDocument().getSDocumentGraph(), markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH,null);
		}
	}
	
	// function specialized in SLayer information
	private void mapSLayer(SLayer sLayer,int lastBaseUnitId) throws MMAX2WrapperException{
		String markableId = getNewId();
		String markableSPan = makeSpan(1,lastBaseUnitId);
		
		Scheme scheme = getScheme(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER);
		String sName = sLayer.getSName();
		String sId = sLayer.getSId();
		
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
		for(Edge edge: getSDocument().getSDocumentGraph().getOutEdges(struct.getSId())){
			if(edge instanceof SDominanceRelation){
				SDominanceRelation sDomRel = (SDominanceRelation) edge;
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
						markable.getSpan(),markable.getSName(),markable.getSId(),markable.getId(),markable.getFactory().getScheme().getName());
				
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
		SaltExtendedMarkable targetMarkable = getSNodeMarkable(domRel.getSStructuredTarget());

		String markableId = getNewId();
		String markableSPan = targetMarkable.getSpan();
		
		SaltExtendedMarkable markable = createMarkableForSRelation(markableId,markableSPan,domRel,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL);
		SRelationMapping validated = matchSRelation(domRel);
		if(validated == null){		
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"target",targetMarkable.getId());
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"target_scheme",targetMarkable.getFactory().getScheme().getName());
		}else{
			SaltExtendedMarkable containerTargetMarkable = getSContainerMarkable(targetMarkable,validated.getTargetAssociatedSchemeName(),
					targetMarkable.getSpan(),targetMarkable.getSName(),targetMarkable.getSId(),targetMarkable.getId(),targetMarkable.getFactory().getScheme().getName());
			
			addFreetextAttribute(markable, SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"id_target",containerTargetMarkable.getId());
		}
		
		return markable;
	}
	
	// function specialized in SPointingRelation information
	// the mapping is done below
	private SaltExtendedMarkable mapPointingRelation(SPointingRelation pointRel) throws MMAX2WrapperException{
		SaltExtendedMarkable sourceMarkable = getSNodeMarkable(pointRel.getSSource());
		SaltExtendedMarkable targetMarkable = getSNodeMarkable(pointRel.getSTarget());
	
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
					sourceMarkable.getSpan(),sourceMarkable.getSName(),sourceMarkable.getSId(),sourceMarkable.getId(),sourceMarkable.getFactory().getScheme().getName());
			
			SaltExtendedMarkable containerTargetMarkable = getSContainerMarkable(targetMarkable,validated.getTargetAssociatedSchemeName(),
					targetMarkable.getSpan(),targetMarkable.getSName(),targetMarkable.getSId(),targetMarkable.getId(),targetMarkable.getFactory().getScheme().getName());
			
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
		SaltExtendedMarkable tokenMarkable = getSNodeMarkable(sSpanningRel.getSToken()); 
		SaltExtendedMarkable spanMarkable = getSNodeMarkable(sSpanningRel.getSSpan());
	
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
		SaltExtendedMarkable tokenMarkable = getSNodeMarkable(sTextualRelation.getSToken());
		SaltExtendedMarkable textualDsMarkable = getSNodeMarkable(sTextualRelation.getSTextualDS());
			
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
		String sName = sNode.getSName();
		String sId = sNode.getSId();
		
		SaltExtendedMarkable markable = getMarkable(scheme,markableId,markableSpan,markableSKind,sName,sId);
		this.document.addMarkable(markable);

		return markable;
	}
	
	
	public SaltExtendedMarkable createMarkableForSRelation(String markableId, String markableSpan, SRelation sRelation, String markableSKind) throws MMAX2WrapperException{
		Scheme scheme = getScheme(markableSKind);
		String sName = sRelation.getSName();
		String sId = sRelation.getSId();
	
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
			SAnnotatableElement  sElem, 
			String markableId, 
			String markableSpan, 
			String schemeBaseName,
			EList<SLayer> sLayers) throws MMAX2WrapperException{

		for (SAnnotation sAnnotation : sElem.getSAnnotations()){
			mapAnnotations(sElem,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SANNOTATION,sName,sId,markableId,markableSpan,schemeBaseName,sAnnotation,sLayers);
		}
	}
	
	private void mapSMetaAnnotations(String sName, 
									String sId, 
									SMetaAnnotatableElement  sElem, 
									String markableId, 
									String markableSpan, 
									String schemeBaseName,
									EList<SLayer> sLayers) throws MMAX2WrapperException{
		
		for (SMetaAnnotation sAnnotation : sElem.getSMetaAnnotations()){
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
								EList<SLayer> sLayers) throws MMAX2WrapperException{
		
		String attributeName = annotation.getSName();
		String attributeValue = annotation.getSValueSTEXT();
		String attributeNs = annotation.getSNS();
		
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
			if(containerMarkable.getAttribute(attributeContainerName) != null){
				throw new PepperModuleException(this, "Matched markable '"+markable+"' has already an attribute '"+attributeContainerName+"'");
			}
			addFreetextAttribute(containerMarkable,validated.getAssociatedSchemeName(),attributeContainerName,attributeValue);
		}
		
		if(containerMarkable != null){
			addFreetextAttribute(markable,schemeName,"target_markable",containerMarkable.getId());
			addFreetextAttribute(markable,schemeName,"container_scheme",validated.getAssociatedSchemeName());
			addFreetextAttribute(markable,schemeName,"container_attr",validated.getAssociatedAttributeName());
		}else{
			addPointerAttribute(markable,schemeName,schemeBaseName,"target_markable",idRef);
			addFreetextAttribute(markable,schemeName,"value",attributeValue);
		}
		
		document.addMarkable(markable);
	}
	
	// function to record if a given SElement belonged to a given SLayer
	private void mapSLayersToMarkable(SaltExtendedMarkable markable, String markableSKind, EList<SLayer> sLayers) throws MMAX2WrapperException{
		String schemeName = markableSKind + "_slayer_link";
		Scheme scheme = getScheme(schemeName);
		for(SLayer sLayer: sLayers){
			SaltExtendedMarkable linkMarkable = getMarkable(scheme,getNewId(),markable.getSpan(),SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER_LINK,markable.getSName(),markable.getSId());
			SaltExtendedMarkable sLayerMarkable = this.registeredSLayerMarkables.get(sLayer);
			
			addPointerAttribute(linkMarkable,schemeName,markableSKind,"selement",markable.getId());
			addPointerAttribute(linkMarkable,schemeName,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER,"slayer",sLayerMarkable.getId());
			document.addMarkable(linkMarkable);
		}
	}
	
	// function to record if a given SRelation has certain STypes
	private void mapSTypesToMarkable(SaltExtendedMarkable markable, String markableSKind, EList<String> sTypes) throws MMAX2WrapperException{		
		String schemeName = markableSKind + "_stype_link";
		Scheme scheme = getScheme(schemeName);
		for(String sType: sTypes){
			SaltExtendedMarkable linkMarkable = getMarkable(scheme,getNewId(),markable.getSpan(),SaltExtendedMmax2Infos.SALT_INFO_TYPE_STYPE_LINK,markable.getSName(),markable.getSId());
			
			addPointerAttribute(linkMarkable,schemeName,markableSKind,"selement",markable.getId());
			addFreetextAttribute(linkMarkable,schemeName,"stype",sType);
			
			this.document.addMarkable(linkMarkable);
		}
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
	public SAnnotationMapping matchSNode(SAbstractAnnotation annotation, EList<SLayer> sLayers){
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

