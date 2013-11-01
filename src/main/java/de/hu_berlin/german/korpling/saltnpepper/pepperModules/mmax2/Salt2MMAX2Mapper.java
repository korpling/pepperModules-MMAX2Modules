package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.osgi.service.log.LogService;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkable;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.MMAX2ExporterException;

import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
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

public class Salt2MMAX2Mapper 
{

	/**
	 * OSGI-log service
	 */
	private LogService logService= null;
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public LogService getLogService() {
		return logService;
	}
	
	// the element that conditions can hold to a mapping
	private final String CONDITION_NODE_NAME = "condition";
	private final String SNODE_TYPE_ARGUMENT = "stype";
	private final String ATTRIBUTE_NAMESPACE_REGEXP = "namespace_regexp";
	private final String ATTRIBUTE_NAME_REGEXP = "name_regexp";
	private final String ATTRIBUTE_VALUE_REGEXP = "value_regexp";
	private final String SLAYER_NAME_REGEXP = "slayer_name_regexp";
	private final String CONTAINER_SCHEME_NAME = "dest_scheme";
	private final String CONTAINER_ATTR_NAME = "dest_attr";
	
	private final String POINTER_CONDITION_NODE_NAME = "condition";
	private final String SRELATION_TYPE_ARGUMENT = "salt_relation_type";
	private final String STYPE_NAME_REGEXP = "stype_regexp";
	private final String CONTAINER_SOURCE_SCHEME_NAME = "source_scheme";
	private final String CONTAINER_TARGET_SCHEME_NAME = "target_scheme";
	private final String CONTAINER_POINTER_ATTR_NAME = "source_attr";
	
	private Hashtable<STextualRelation,Integer> spanStextualRelationCorrespondance;
	private Hashtable<STextualDS,ArrayList<String>> spanStextualDSCorrespondance;
	private Hashtable<SNode,SaltExtendedMarkable> registeredSNodesMarkables;
	private Hashtable<SRelation,SaltExtendedMarkable> registeredSRelationsMarkables;
	private Hashtable<SLayer,SaltExtendedMarkable> registeredSLayerMarkables;
	private DocumentBuilder documentBuilder;
	
	private Hashtable<Object,Hashtable<Scheme,SaltExtendedMarkable>> sContainerMarkables;
	private Hashtable<String,ArrayList<AttributeMatchCondition>> conditions;
	private Hashtable<String,ArrayList<PointerMatchCondition>> pointersConditions;

	private SDocumentGraph sDocumentGraph;
	private SDocument sDocument;
	private SaltExtendedCorpus corpus;
	private SaltExtendedDocument document;
	private SchemeFactory schemeFactory;
	
	/**
	 * Builds the mapper
	 * @param documentBuilder an Xml parser to use for parsingt Xml files
	 * @param matchingAttributeConditionFilePath the path to the file containing the conditions for performing mapping on any SAannotations or SMetaAnnotations
	 * @param matchingPointerConditionFilePath the path to the file containing the conditions for performing mapping on SRelations
	 * @throws SAXException
	 * @throws IOException
	 */
	public Salt2MMAX2Mapper(DocumentBuilder documentBuilder, String matchingAttributeConditionFilePath, String matchingPointerConditionFilePath) throws SAXException, IOException {
		this.documentBuilder = documentBuilder; 
		
		//loading the conditions for performing mapping on any SAannotations or SMetaAnnotations
		this.conditions = new Hashtable<String, ArrayList<AttributeMatchCondition>>();
		if(matchingAttributeConditionFilePath != null){
			File configurationFile = new File(matchingAttributeConditionFilePath);	
			
			NodeList nodes = documentBuilder.parse(configurationFile).getDocumentElement().getChildNodes();
			for(int i = 0; i < nodes.getLength(); i ++){	
				Node xmlNode = nodes.item(i);
				String nodeName = xmlNode.getNodeName();
				if((nodeName == null) || (!nodeName.equals(CONDITION_NODE_NAME))){
					continue;
				}
				NamedNodeMap attributes = xmlNode.getAttributes();
					
				Node snodeTypeAttributeNode = attributes.getNamedItem(SNODE_TYPE_ARGUMENT);
				if(snodeTypeAttributeNode == null){
					throw new MMAX2ExporterException("SNode type '"+SNODE_TYPE_ARGUMENT+"' on Node '"+xmlNode+"' is not defined...");
				}
				String sNodeType = snodeTypeAttributeNode.getNodeValue(); // Check if it matches a known type
				attributes.removeNamedItem(SNODE_TYPE_ARGUMENT);
				
				
				Node nameSpaceAttributeNode = attributes.getNamedItem(ATTRIBUTE_NAMESPACE_REGEXP);
				String nameSpaceRegExp = null;
				if(nameSpaceAttributeNode != null){
					nameSpaceRegExp = nameSpaceAttributeNode.getNodeValue(); 
					attributes.removeNamedItem(ATTRIBUTE_NAMESPACE_REGEXP);
				}
				
				Node nameAttributeNode = attributes.getNamedItem(ATTRIBUTE_NAME_REGEXP);
				String nameRegExp = null;
				if(nameAttributeNode != null){
					nameRegExp = nameAttributeNode.getNodeValue(); 
					attributes.removeNamedItem(ATTRIBUTE_NAME_REGEXP);
				}
				
				Node valueAttributeNode = attributes.getNamedItem(ATTRIBUTE_VALUE_REGEXP);
				String valueRegExp = null;
				if(valueAttributeNode != null){
					valueRegExp = valueAttributeNode.getNodeValue(); 
					attributes.removeNamedItem(ATTRIBUTE_VALUE_REGEXP);
				}
				
				Node sLayerNameNode = attributes.getNamedItem(SLAYER_NAME_REGEXP);
				String sLayerNameRegExp = null;
				if(sLayerNameNode != null){
					sLayerNameRegExp = sLayerNameNode.getNodeValue(); 
					attributes.removeNamedItem(SLAYER_NAME_REGEXP);
				}
				
				Node destSchemeNode = attributes.getNamedItem(CONTAINER_SCHEME_NAME);
				if(destSchemeNode == null){
					throw new MMAX2ExporterException("Destination scheme '"+CONTAINER_SCHEME_NAME+"' on Node '"+xmlNode+"' is not defined...");
				}
				attributes.removeNamedItem(CONTAINER_SCHEME_NAME);
				String schemeName = destSchemeNode.getNodeValue(); 
				
				Node destAttrNode = attributes.getNamedItem(CONTAINER_ATTR_NAME);
				if(destAttrNode == null){
					throw new MMAX2ExporterException("Destination attribute '"+CONTAINER_ATTR_NAME+"' on Node '"+xmlNode+"' is not defined...");
				}
				attributes.removeNamedItem(CONTAINER_ATTR_NAME);
				String attrName = destAttrNode.getNodeValue(); 
				
				if(attributes.getLength() != 0){
					ArrayList<String> unknownAttributes = new ArrayList<String>();
					for(int j = 0; j < attributes.getLength(); j++){
						unknownAttributes.add(attributes.item(j).getNodeName());
					}
					throw new MMAX2ExporterException("Unknown attributes '"+StringUtils.join(unknownAttributes,",")+"' on Node '"+xmlNode+"'");
				}
				
				if(!this.conditions.containsKey(sNodeType)){
					this.conditions.put(sNodeType,new ArrayList<AttributeMatchCondition>());
				}
				ArrayList<AttributeMatchCondition> conditionsOfType = this.conditions.get(sNodeType);
				conditionsOfType.add(new AttributeMatchCondition(nameSpaceRegExp, nameRegExp, valueRegExp,sLayerNameRegExp,schemeName,attrName));
			}
		}
		
		//loading the conditions for performing mapping on any SAannotations or SRelations
		this.pointersConditions = new Hashtable<String, ArrayList<PointerMatchCondition>>();
		if(matchingPointerConditionFilePath != null){
			File configurationFile = new File(matchingPointerConditionFilePath);	
			
			NodeList nodes = documentBuilder.parse(configurationFile).getDocumentElement().getChildNodes();
			for(int i = 0; i < nodes.getLength(); i ++){	
				Node xmlNode = nodes.item(i);
				String nodeName = xmlNode.getNodeName();
				if((nodeName == null) || (!nodeName.equals(POINTER_CONDITION_NODE_NAME))){
					continue;
				}
				NamedNodeMap attributes = xmlNode.getAttributes();
					
				Node srelationTypeAttributeNode = attributes.getNamedItem(SRELATION_TYPE_ARGUMENT);
				if(srelationTypeAttributeNode == null){
					throw new MMAX2ExporterException("SRelation type '"+SRELATION_TYPE_ARGUMENT+"' on Node '"+xmlNode+"' is not defined...");
				}
				String sRelationType = srelationTypeAttributeNode.getNodeValue(); // Check if it matches a known type
				if(!sRelationType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL) &&  !sRelationType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL)){
					throw new MMAX2ExporterException("Pointer condition defined on node '"+xmlNode+"' does not address neither a SDominance relation or a SPointing relation");
				}
				attributes.removeNamedItem(SRELATION_TYPE_ARGUMENT);
				
				Node sTypeNameNode = attributes.getNamedItem(STYPE_NAME_REGEXP);
				String sTypeNameRegExp = null;
				if(sTypeNameNode != null){
					sTypeNameRegExp = sTypeNameNode.getNodeValue(); 
					attributes.removeNamedItem(STYPE_NAME_REGEXP);
				}
				
				Node sLayerNameNode = attributes.getNamedItem(SLAYER_NAME_REGEXP);
				String sLayerNameRegExp = null;
				if(sLayerNameNode != null){
					sLayerNameRegExp = sLayerNameNode.getNodeValue(); 
					attributes.removeNamedItem(SLAYER_NAME_REGEXP);
				}
				
				Node sourceDestSchemeNode = attributes.getNamedItem(CONTAINER_SOURCE_SCHEME_NAME);
				if(sourceDestSchemeNode == null){
					throw new MMAX2ExporterException("Source destination scheme '"+CONTAINER_SOURCE_SCHEME_NAME+"' on Node '"+xmlNode+"' is not defined...");
				}
				attributes.removeNamedItem(CONTAINER_SOURCE_SCHEME_NAME);
				String sourceSchemeName = sourceDestSchemeNode.getNodeValue(); 
				
				Node targetDestSchemeNode = attributes.getNamedItem(CONTAINER_TARGET_SCHEME_NAME);
				if(targetDestSchemeNode == null){
					throw new MMAX2ExporterException("Source destination scheme '"+CONTAINER_TARGET_SCHEME_NAME+"' on Node '"+xmlNode+"' is not defined...");
				}
				attributes.removeNamedItem(CONTAINER_TARGET_SCHEME_NAME);
				String targetSchemeName = targetDestSchemeNode.getNodeValue(); 
				
				Node destAttrNode = attributes.getNamedItem(CONTAINER_POINTER_ATTR_NAME);
				if(destAttrNode == null){
					throw new MMAX2ExporterException("Destination attribute '"+CONTAINER_POINTER_ATTR_NAME+"' on Node '"+xmlNode+"' is not defined...");
				}
				attributes.removeNamedItem(CONTAINER_POINTER_ATTR_NAME);
				String attrName = destAttrNode.getNodeValue(); 
				
				if(attributes.getLength() != 0){
					ArrayList<String> unknownAttributes = new ArrayList<String>();
					for(int j = 0; j < attributes.getLength(); j++){
						unknownAttributes.add(attributes.item(j).getNodeName());
					}
					throw new MMAX2ExporterException("Unknown attributes '"+StringUtils.join(unknownAttributes,",")+"' on Node '"+xmlNode+"'");
				}
				
				if(!this.pointersConditions.containsKey(sRelationType)){
					this.pointersConditions.put(sRelationType,new ArrayList<PointerMatchCondition>());
				}
				ArrayList<PointerMatchCondition> conditionsOfType = this.pointersConditions.get(sRelationType);
				conditionsOfType.add(new PointerMatchCondition(sTypeNameRegExp,sLayerNameRegExp,sourceSchemeName,targetSchemeName,attrName));
			}
		}
		
	}
	
	// some usefuls fonctions to create Mmax ID or record and access the mmax2 informations associated with previously created markables

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
				throw new MMAX2ExporterException("Developper error Unknown Type of SRelation => "+key.getClass());
			}
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
			}else if (key instanceof SSpanningRelation){
				markable = mapSpanningRelation((SSpanningRelation) key);
			}else if (key instanceof SDominanceRelation){
				markable = mapDominanceRelation((SDominanceRelation) key);
			}else if (key instanceof STextualRelation){
				markable = mapTextualRelation((STextualRelation) key);
			}else if (key instanceof SPointingRelation){
				markable = mapPointingRelation((SPointingRelation) key);
			}else{
				throw new MMAX2ExporterException("Developper error Unknown Type of SNode => "+key.getClass());
			}			
			registerSNodeMarkable(markable, key);
		}
		
		return markable;
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
	public SaltExtendedDocument mapAllSDocument(SaltExtendedCorpus corpus, SDocument sDocument, SaltExtendedDocumentFactory factory, SchemeFactory schemeFactory) throws MMAX2ExporterException, MMAX2WrapperException 
	{
		// this function goes through all pieces of data in a SDocument and launch accordingly the specialized functions below
		
		String documentName = sDocument.getSName();
		this.spanStextualRelationCorrespondance = new Hashtable<STextualRelation, Integer>();
		this.spanStextualDSCorrespondance = new Hashtable<STextualDS, ArrayList<String>>();
		this.registeredSNodesMarkables = new Hashtable<SNode, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		this.registeredSRelationsMarkables = new Hashtable<SRelation, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		this.registeredSLayerMarkables = new Hashtable<SLayer, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		
		this.sContainerMarkables = new Hashtable<Object, Hashtable<Scheme,SaltExtendedMarkable>>();
		
		this.document = factory.newDocument(documentName);
		this.sDocument = sDocument;
		this.sDocumentGraph = sDocument.getSDocumentGraph();
		this.corpus = corpus;
		this.schemeFactory = schemeFactory;
		
		// it deals with STextualDs
		EList<STextualDS> sTextualDSList = new BasicEList<STextualDS>(this.sDocumentGraph.getSTextualDSs());
		EList<STextualRelation> sTextualRelationList = new BasicEList<STextualRelation>(this.sDocumentGraph.getSTextualRelations());
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
		
		mapSDocument(compteurId);
		
		for(SLayer sLayer: new BasicEList<SLayer>(sDocumentGraph.getSLayers())){
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
		
		for(SToken sToken: this.sDocumentGraph.getSTokens()){
			getSNodeMarkable(sToken);
			allSnodes.add(sToken);
		}
		
		
		for(SSpanningRelation sSpanningRelation: sDocumentGraph.getSSpanningRelations()){
			getSRelationMarkable(sSpanningRelation);
			allSrelations.add(sSpanningRelation);
		}
		
		for(SSpan sSpan: this.sDocumentGraph.getSSpans()){
			getSNodeMarkable(sSpan);
			allSnodes.add(sSpan);
		}
		
		for(SDominanceRelation sDominanceRelation: sDocumentGraph.getSDominanceRelations()){
			getSRelationMarkable(sDominanceRelation);
			allSrelations.add(sDominanceRelation);
		}
		
		for(SStructure sStruct: this.sDocumentGraph.getSStructures()){
			getSNodeMarkable(sStruct);
			allSnodes.add(sStruct);
		}
		
		for(SPointingRelation sPointer: sDocumentGraph.getSPointingRelations()){
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
		//corpus.addDocument(document);
		return document;	
	}
	
	// function specialized in SDocument information
	private void mapSDocument(int lastBaseUnitId) throws MMAX2WrapperException{
		String markableSPan =  makeSpan(1,lastBaseUnitId);
		{
			// The SDocument itself
			String markableId = getNewId();
			
			Scheme scheme = getScheme(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT);
			String sName = this.sDocument.getSName();
			String sId = this.sDocument.getSId();
			
			SaltExtendedMarkable markable = getMarkable(scheme,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT,sName,sId);
			this.document.addMarkable(markable);
	
			mapSMetaAnnotations(sName,sId,this.sDocument,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT,null);
			mapSAnnotations(sName,sId,this.sDocument,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT,null);
		}
		{
			// The graph of the SDocument 
			String markableId = getNewId();
			Scheme scheme = getScheme(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH);
			String sName = this.sDocumentGraph.getSName();
			String sId = this.sDocumentGraph.getSId();
			
			SaltExtendedMarkable markable = getMarkable(scheme,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH,sName,sId);
			this.document.addMarkable(markable);
	
			mapSMetaAnnotations(sName,sId,this.sDocumentGraph,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH,null);
			mapSAnnotations(sName,sId,this.sDocumentGraph,markableId,markableSPan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH,null);
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
		Hashtable<SaltExtendedMarkable,PointerMatchCondition> sDomRelMarkableHash = new Hashtable<SaltExtendedMarkable,PointerMatchCondition>(); 
		ArrayList<String> spans = new ArrayList<String>();
		for(Edge edge: this.sDocumentGraph.getOutEdges(struct.getSId())){
			if(edge instanceof SDominanceRelation){
				SDominanceRelation sDomRel = (SDominanceRelation) edge;
				SaltExtendedMarkable sDomRelMarkable = getSRelationMarkable(sDomRel);
				spans.add(sDomRelMarkable.getSpan());
				PointerMatchCondition validated = matchSRelation(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,sDomRel.getSTypes(), sDomRel.getSLayers());
				sDomRelMarkableList.add(sDomRelMarkable);
				if(validated != null){
					sDomRelMarkableHash.put(sDomRelMarkable,validated);
				}
			}
		}
		
		SaltExtendedMarkable markable = createMarkableForSNode(getNewId(),makeSpan(spans),struct,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSTRUCT);		
		
		for(SaltExtendedMarkable sDomRelMarkable: sDomRelMarkableList){
			if(sDomRelMarkableHash.containsKey(sDomRelMarkable)){
				PointerMatchCondition validated = sDomRelMarkableHash.get(sDomRelMarkable);
			
				SaltExtendedMarkable containerSourceMarkable = getSContainerMarkable(markable,validated.getSourceAssociatedSchemeName(),
						markable.getSpan(),markable.getSName(),markable.getSId(),markable.getId());
				
				addPointerAttribute(containerSourceMarkable, validated.getSourceAssociatedSchemeName(), validated.getTargetAssociatedSchemeName(), 
						validated.getPointedAssociatedAttributeName(), sDomRelMarkable.getAttribute("target").getValue());
				
				addPointerAttribute(sDomRelMarkable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSTRUCT,"struct",containerSourceMarkable.getId());
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
		
		PointerMatchCondition validated = matchSRelation(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,domRel.getSTypes(), domRel.getSLayers());
		if(validated == null){		
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"target",targetMarkable.getId());
		}else{
			SaltExtendedMarkable containerTargetMarkable = getSContainerMarkable(targetMarkable,validated.getTargetAssociatedSchemeName(),
					targetMarkable.getSpan(),targetMarkable.getSName(),targetMarkable.getSId(),targetMarkable.getId());
			
			addFreetextAttribute(markable, SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,
					CONTAINER_POINTER_ATTR_NAME, validated.getPointedAssociatedAttributeName());			
		}
		
		return markable;
	}
	
	// function specialized in SPointingRelation information
	// the mapping is done below
	private SaltExtendedMarkable mapPointingRelation(SPointingRelation pointRel) throws MMAX2WrapperException{
		SaltExtendedMarkable sourceMarkable = getSNodeMarkable(pointRel.getSSource());
		SaltExtendedMarkable targetMarkable = null;
		if(pointRel.getSTarget() != null){
			targetMarkable = getSNodeMarkable(pointRel.getSTarget());
		}
	
		String markableId = getNewId();
		String markableSPan = sourceMarkable.getSpan();
		SaltExtendedMarkable markable = createMarkableForSRelation(markableId,markableSPan,pointRel,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL);
		
		
		PointerMatchCondition validated = matchSRelation(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,pointRel.getSTypes(), pointRel.getSLayers());
		if(validated == null){		
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"source",sourceMarkable.getId());
			if(targetMarkable != null){
				addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"target",targetMarkable.getId());
			}else{
				addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"target","empty");	
			}
		}else{
			SaltExtendedMarkable containerSourceMarkable = getSContainerMarkable(sourceMarkable,validated.getSourceAssociatedSchemeName(),
					sourceMarkable.getSpan(),sourceMarkable.getSName(),sourceMarkable.getSId(),sourceMarkable.getId());
			
			addFreetextAttribute(markable, SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,
					CONTAINER_POINTER_ATTR_NAME, validated.getPointedAssociatedAttributeName());
			
			addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"source",containerSourceMarkable.getId());
			
			if(targetMarkable != null){
				SaltExtendedMarkable containerTargetMarkable = getSContainerMarkable(targetMarkable,validated.getTargetAssociatedSchemeName(),
						targetMarkable.getSpan(),targetMarkable.getSName(),targetMarkable.getSId(),targetMarkable.getId());
			
				addPointerAttribute(containerSourceMarkable, validated.getSourceAssociatedSchemeName(), validated.getTargetAssociatedSchemeName(), 
						validated.getPointedAssociatedAttributeName(), containerTargetMarkable.getId());
			}else{
				addPointerAttribute(containerSourceMarkable, validated.getSourceAssociatedSchemeName(), validated.getTargetAssociatedSchemeName(), 
						validated.getPointedAssociatedAttributeName(), "empty");
			}
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
		SaltExtendedMarkable tokenMarkable = getSNodeMarkable(sTextualRelation.getSToken());
		SaltExtendedMarkable textualDsMarkable = getSNodeMarkable(sTextualRelation.getSTextualDS());
		
		String markableId = getNewId();
		//System.out.println(sTextualRelation);
		String markableSPan = makeSpan(this.spanStextualRelationCorrespondance.get(sTextualRelation));
		
		tokenMarkable.setSpan(markableSPan);
		
		SaltExtendedMarkable markable = createMarkableForSRelation(markableId,markableSPan,sTextualRelation,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUAL_REL);
		
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
	
	private void mapSMetaAnnotations(String sName, 
									String sId, 
									SMetaAnnotatableElement  sElem, 
									String markableId, 
									String markableSpan, 
									String schemeBaseName,
									EList<SLayer> sLayers) throws MMAX2WrapperException{
		
		for (SMetaAnnotation sAnnotation : sElem.getSMetaAnnotations()){
			String attributeName = sAnnotation.getSName();
			String attributeValue = sAnnotation.getSValueSTEXT();
			String attributeNs = sAnnotation.getSNS();
			mapAnnotations(sElem,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SMETAANNOTATION,sName,sId,markableId,markableSpan,schemeBaseName,attributeName,attributeNs,attributeValue,sLayers);	
		}
	}
	

	private void mapSAnnotations(String sName, 
								String sId, 
								SAnnotatableElement  sELem, 
								String elementId, 
								String elementSpan, 
								String schemeBaseName,
								EList<SLayer> sLayers) throws MMAX2WrapperException{
		
		for (SAnnotation sAnnotation : sELem.getSAnnotations()){
			String attributeName = sAnnotation.getSName();
			String attributeValue = sAnnotation.getSValueSTEXT();
			String attributeNs = sAnnotation.getSNS();
			mapAnnotations(sELem,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SANNOTATION,sName,sId,elementId,elementSpan,schemeBaseName,attributeName,attributeNs,attributeValue,sLayers);
		}
	}
	
	private void mapAnnotations(Object sELem,
								String sType, 
								String sName, 
								String sId, 
								String idRef, 
								String span, 
								String schemeBaseName,
								String attributeName,
								String attributeNs,
								String attributeValue,
								EList<SLayer> sLayers) throws MMAX2WrapperException{
		
		String schemeName = schemeBaseName+"_"+sType;
		Scheme scheme = getScheme(schemeName);
		SaltExtendedMarkable markable = getMarkable(scheme,getNewId(),span,sType,sName,sId);
	
		addPointerAttribute(markable,schemeName,schemeBaseName,"target_markable",idRef);
		addFreetextAttribute(markable,schemeName,"namespace",attributeNs);
		addFreetextAttribute(markable,schemeName,"attr_name",attributeName);
		
		SaltExtendedMarkable containerMarkable = null;
		// the mapping of attributes is done here
		AttributeMatchCondition validated = matchSNode(schemeBaseName, attributeNs, attributeName, attributeValue,sLayers);
		if(validated != null){		
			containerMarkable = getSContainerMarkable(sELem,validated.getAssociatedSchemeName(),span,sName,sId,idRef);
			
			String attributeContainerName = validated.getAssociatedAttributeName();
			if(containerMarkable.getAttribute(attributeContainerName) != null){
				throw new MMAX2ExporterException("Matched markable '"+markable+"' has already an attribute '"+attributeContainerName+"'");
			}
			addFreetextAttribute(containerMarkable,validated.getAssociatedSchemeName(),attributeContainerName,attributeValue);
		}
		
		if(containerMarkable != null){
			addFreetextAttribute(markable,schemeName,"container_id",containerMarkable.getId());
			addFreetextAttribute(markable,schemeName,"container_attr",validated.getAssociatedAttributeName());
		}else{
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
	private SaltExtendedMarkable getSContainerMarkable(Object sElem, String schemeName, String span, String sName, String sId, String containedId){
		if(!this.sContainerMarkables.containsKey(sElem)){
			this.sContainerMarkables.put(sElem,new Hashtable<Scheme, SaltExtendedMarkable>());
		}
		Hashtable<Scheme,SaltExtendedMarkable> associatedMarkables = this.sContainerMarkables.get(sElem);
		
		SaltExtendedMarkable containerMarkable = null;
		
		Scheme associatedScheme = getScheme(schemeName);
		if(!associatedMarkables.containsKey(associatedScheme)){
			SaltExtendedMarkableFactory markableFactory = this.document.getFactory().getMarkableFactory(associatedScheme);
			if(markableFactory == null){
				markableFactory = new SaltExtendedMarkableFactory(associatedScheme, this.documentBuilder);
				this.document.getFactory().addMarkableFactory(markableFactory);		
			}
			containerMarkable = markableFactory.newMarkableContainer(getNewId(),span, new ArrayList<MarkableAttribute>(), SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER, sName, sId,containedId);
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
	public AttributeMatchCondition matchSNode(String sNodeType, String attributeNameSpace,String attributeName, String attributeValue, EList<SLayer> sLayers){
		AttributeMatchCondition validated = null;
		if(this.conditions.containsKey(sNodeType)){
			ArrayList<AttributeMatchCondition> specificConditions = this.conditions.get(sNodeType);
			for(AttributeMatchCondition matchCondition: specificConditions){
				if(matchCondition.isMatched(attributeNameSpace, attributeName, attributeValue,sLayers)){
					if(validated != null){
						throw new MMAX2ExporterException("Ambiguous matching confitions '"+validated+"' and '"+matchCondition+"' have both matched '"+
								sNodeType+"/"+attributeNameSpace+"/"+attributeName+"/"+attributeValue+"'");
					}
					validated = matchCondition;
				}
			}
		}
		
		return validated;	
	}
	
	// function to check if some conditions over a SRelation is validated (some mapping should be launched)
	public PointerMatchCondition matchSRelation(String sRelationType, EList<String> sTypes, EList<SLayer> sLayers){
		PointerMatchCondition validated = null;
		
		if(this.pointersConditions.containsKey(sRelationType)){
			ArrayList<PointerMatchCondition> specificConditions = this.pointersConditions.get(sRelationType);
			for(PointerMatchCondition matchCondition: specificConditions){
				if(matchCondition.isMatched(sTypes, sLayers)){
					if(validated != null){
						throw new MMAX2ExporterException("Ambiguous matching confitions '"+validated+"' and '"+matchCondition+"' have both matched '"+
								sRelationType+"'");
					}
					validated = matchCondition;
				}
			}
		}
	
		return validated;	
	}
}	

/**
 * This class models a condition for performing a mapping over an attribute
 * @author Lionel Nicolas
 *
 */
class AttributeMatchCondition{
	private Pattern attributeNamespacePattern = null; // the pattern of the namespace of the attribute
	private Pattern attributeNamePattern = null; // the pattern of the name of the attribute
	private Pattern attributeValuePattern = null;// the pattern of the value of the attribute
	private Pattern sLayerNamePattern = null;// the pattern of the SLayer of the attribute
	private String associatedSchemeName;// the name of the scheme into which the mapping should be performed
	private String associatedAttributeName; // the name of the attribute into which the mapping should be performed
			
	AttributeMatchCondition(String attributeNamespacePattern, String attributeNamePattern, String attributeValuePattern, String sLayerNamePattern,
			String associatedSchemeName, String associatedAttributeName){
		if(attributeNamespacePattern != null)
			this.attributeNamespacePattern = Pattern.compile(attributeNamespacePattern);
		if(attributeNamePattern != null)
			this.attributeNamePattern = Pattern.compile(attributeNamePattern);
		if(attributeValuePattern != null)
			this.attributeValuePattern = Pattern.compile(attributeValuePattern);
		if(sLayerNamePattern != null)
			this.sLayerNamePattern = Pattern.compile(sLayerNamePattern);
		this.associatedSchemeName = associatedSchemeName;
		this.associatedAttributeName = associatedAttributeName;
	}
			
	public boolean isMatched(String attributeNameSpace, String attributeName, String attributeValue, EList<SLayer> sLayers){
		boolean answer = true;
		boolean hasMatchedSomething = false;
		
		if((attributeNameSpace != null) && (attributeNamespacePattern != null)){
			hasMatchedSomething = true;
			answer = answer && attributeNamespacePattern.matcher(attributeNameSpace).matches();
		}
		
		if(attributeNamePattern != null){
			hasMatchedSomething = true;
			answer = answer && attributeNamePattern.matcher(attributeName).matches();
		}
		
		if(attributeValuePattern != null){
			hasMatchedSomething = true;
			answer = answer && attributeValuePattern.matcher(attributeValue).matches();
		}
		
		if((sLayerNamePattern != null) && (sLayers != null)){
			for(SLayer sLayer: sLayers){
				hasMatchedSomething = true;
				answer = answer && sLayerNamePattern.matcher(sLayer.getSName()).matches();
			}
		}
		
		return hasMatchedSomething && answer;
	}
	
	public String getAssociatedSchemeName(){
		return this.associatedSchemeName;
	}
	
	public String getAssociatedAttributeName(){
		return this.associatedAttributeName;
	}
	
	public String toString(){
		return ((this.attributeNamespacePattern == null)? "":"namespace_regexp = '"+attributeNamespacePattern+"' ")
				+ ((this.attributeNamePattern == null)? "":"name_regexp = '"+attributeNamePattern+"' ")
				+ ((this.attributeValuePattern == null)? "":"value_regexp = '"+attributeValuePattern+"' ")
				+ ((this.sLayerNamePattern == null)? "":"slayer_name_regexp = '"+sLayerNamePattern+"' ")
				+" => "+this.associatedSchemeName+":"+this.associatedAttributeName;
	}
}
	
/**
 * This class models a condition for performing a mapping over a SRelation
 * @author Lionel Nicolas
 *
 */
class PointerMatchCondition{
	private Pattern typeNamePattern = null;// the pattern of the SType of the SRrelation
	private Pattern sLayerNamePattern = null;// the pattern of the SLayer of the SRrelation
	private String sourceAssociatedSchemeName;// the name of the scheme into which a SContainer for the source node of the SRelation should be mapped
	private String targetAssociatedSchemeName;// the name of the scheme into which a SContainer for the target node of the SRelation should be mapped
	private String associatedPointerAttributeName;// the name of the pointer attribute of the SContainer of the source node that will represent the SRelation 
			
	public PointerMatchCondition(String typeNamePattern, String sLayerNamePattern,
			String sourceAssociatedSchemeName, String targetAssociatedSchemeName, 
			String associatedPointerAttributeName){
		if(typeNamePattern != null)
			this.typeNamePattern = Pattern.compile(typeNamePattern);
		if(sLayerNamePattern != null)
			this.sLayerNamePattern = Pattern.compile(sLayerNamePattern);
		this.sourceAssociatedSchemeName = sourceAssociatedSchemeName;
		this.targetAssociatedSchemeName = targetAssociatedSchemeName;
		this.associatedPointerAttributeName = associatedPointerAttributeName;
	}
			
	public boolean isMatched(EList<String> sTypes, EList<SLayer> sLayers){
		boolean answer = true;
		boolean hasMatchedSomething = false;
		
		if((typeNamePattern != null) && (sTypes != null)){
			for(String sType: sTypes){
				hasMatchedSomething = true;
				answer = answer && typeNamePattern.matcher(sType).matches();
			}
		}
		
		if((sLayerNamePattern != null) && (sLayers != null)){
			for(SLayer sLayer: sLayers){
				hasMatchedSomething = true;
				answer = answer && sLayerNamePattern.matcher(sLayer.getSName()).matches();
			}
		}
		
		return hasMatchedSomething && answer;
	}
	
	public String getSourceAssociatedSchemeName(){
		return this.sourceAssociatedSchemeName;
	}
	
	public String getTargetAssociatedSchemeName(){
		return this.targetAssociatedSchemeName;
	}
	
	public String getPointedAssociatedAttributeName(){
		return this.associatedPointerAttributeName;
	}
	
	public String toString(){
		return ((this.typeNamePattern == null)? "":"stype_regexp = '"+typeNamePattern+"' ")
				+ ((this.sLayerNamePattern == null)? "":"slayer_name_regexp = '"+sLayerNamePattern+"' ")
				+" => "+this.sourceAssociatedSchemeName+":"+this.associatedPointerAttributeName+" ==> "+this.targetAssociatedSchemeName;
	}
}	
	
	
	

	

