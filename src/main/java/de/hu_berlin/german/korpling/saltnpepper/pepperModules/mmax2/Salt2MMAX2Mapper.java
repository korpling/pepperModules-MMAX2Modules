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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.osgi.service.log.LogService;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.bcel.internal.generic.DDIV;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkable;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.MMAX2ExporterException;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.SaltExtendedMMAX2WrapperException;

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
import eurac.commul.annotations.mmax2wrapper.CorpusFactory.Corpus;
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
 * Maps SCorpusGraph objects to a folder structure and maps a SDocumentStructure to the necessary files containing the document data in MMAX2 notation.
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
	
	private final String CONDITION_NODE_NAME = "condition";
	private final String SNODE_TYPE_ARGUMENT = "snode_type";
	private final String ATTRIBUTE_NAMESPACE_REGEXP = "namespace_regexp";
	private final String ATTRIBUTE_NAME_REGEXP = "name_regexp";
	private final String ATTRIBUTE_VALUE_REGEXP = "value_regexp";
	private final String SLAYER_NAME_REGEXP = "slayer_name_regexp";
	private final String CONTAINER_SCHEME_NAME = "dest_scheme";
	private final String CONTAINER_ATTR_NAME = "dest_attr";
	
	private Hashtable<STextualRelation,Integer> spanStextualRelationCorrespondance;
	private Hashtable<STextualDS,ArrayList<String>> spanStextualDSCorrespondance;
	private Hashtable<SNode,SaltExtendedMarkable> registeredSNodesMarkables;
	private Hashtable<SRelation,SaltExtendedMarkable> registeredSRelationsMarkables;
	private HashMap<SLayer,SaltExtendedMarkable> registeredSLayerMarkables;
	private DocumentBuilder documentBuilder;
	
	private Hashtable<Object,Hashtable<Scheme,SaltExtendedMarkable>> sNodesMarkables;
	private Hashtable<String,ArrayList<MatchCondition>> conditions;
	

	private SDocumentGraph sDocumentGraph;
	private SDocument sDocument;
	private SaltExtendedCorpus corpus;
	private SaltExtendedDocument document;
	private SchemeFactory schemeFactory;
	
	public Salt2MMAX2Mapper(DocumentBuilder documentBuilder, String matchingConditionFilePath) throws SAXException, IOException {
		this.documentBuilder = documentBuilder; 
		
		this.conditions = new Hashtable<String, ArrayList<MatchCondition>>();
		this.conditions.put("All",new ArrayList<MatchCondition>());
		
		if(matchingConditionFilePath != null){
			File configurationFile = new File(matchingConditionFilePath);	
			
			NodeList nodes = documentBuilder.parse(configurationFile).getDocumentElement().getChildNodes();
			for(int i = 0; i < nodes.getLength(); i ++){	
				Node xmlNode = nodes.item(i);
				String nodeName = xmlNode.getNodeName();
				if((nodeName == null) || (!nodeName.equals(CONDITION_NODE_NAME))){
					continue;
				}
				NamedNodeMap attributes = xmlNode.getAttributes();
					
				Node snodeTypeAttributeNode = attributes.getNamedItem(SNODE_TYPE_ARGUMENT);
				String sNodeType = "All";
				if(snodeTypeAttributeNode != null){
					sNodeType = snodeTypeAttributeNode.getNodeValue(); // Check if it matches a known type
					attributes.removeNamedItem(SNODE_TYPE_ARGUMENT);
				}
				
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
					this.conditions.put(sNodeType,new ArrayList<MatchCondition>());
				}
				ArrayList<MatchCondition> conditionsOfType = this.conditions.get(sNodeType);
				conditionsOfType.add(new MatchCondition(nameSpaceRegExp, nameRegExp, valueRegExp,sLayerNameRegExp,schemeName,attrName));
			}
		}
	}
	

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
	
	
	public void mapAllSDocument(SaltExtendedCorpus corpus, SDocument sDocument, SaltExtendedDocumentFactory factory, SchemeFactory schemeFactory) throws MMAX2ExporterException, MMAX2WrapperException 
	{
		String documentName = sDocument.getSName();
		this.spanStextualRelationCorrespondance = new Hashtable<STextualRelation, Integer>();
		this.spanStextualDSCorrespondance = new Hashtable<STextualDS, ArrayList<String>>();
		this.registeredSNodesMarkables = new Hashtable<SNode, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		this.registeredSRelationsMarkables = new Hashtable<SRelation, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		this.registeredSLayerMarkables = new HashMap<SLayer, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		
		this.sNodesMarkables = new Hashtable<Object, Hashtable<Scheme,SaltExtendedMarkable>>();
		
		this.document = factory.newDocument(documentName);
		this.sDocument = sDocument;
		this.sDocumentGraph = sDocument.getSDocumentGraph();
		this.corpus = corpus;
		this.schemeFactory = schemeFactory;
		
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
			
		for(SNode sNode: allSnodes){
			SaltExtendedMarkable markable = getSNodeMarkable(sNode);
			EList<SLayer> sLayers = sNode.getSLayers();
			
			mapSMetaAnnotations(markable.getSName(),markable.getSId(),sNode,markable.getId(),markable.getSpan(),markable.getLevelName(),sLayers);
			mapSAnnotations(markable.getSName(),markable.getSId(),sNode,markable.getId(),markable.getSpan(),markable.getLevelName(),sLayers);
			
			if(sLayers.size() != 0)
				mapSLayersToMarkable(markable,markable.getLevelName(),sLayers);
		}
	
		for (SRelation sRelation: allSrelations){
			SaltExtendedMarkable markable = getSRelationMarkable(sRelation);
			EList<SLayer> sLayers = sRelation.getSLayers();		
			
			mapSMetaAnnotations(markable.getSName(),markable.getSId(),sRelation,markable.getId(),markable.getSpan(),markable.getLevelName(),sLayers);
			mapSAnnotations(markable.getSName(),markable.getSId(),sRelation,markable.getId(),markable.getSpan(),markable.getLevelName(),sLayers);
			
				
			if(sLayers.size() != 0)
				mapSLayersToMarkable(markable,markable.getLevelName(),sLayers);
			
			EList<String> sTypes = sRelation.getSTypes();
			if(sTypes != null)
				mapSTypesToMarkable(markable,markable.getLevelName(),sTypes);
		}
		corpus.addDocument(document);
	}
	
	public void finalizeCorpusStructure(SaltExtendedCorpus corpus, SchemeFactory schemeFactory) throws MMAX2ExporterException{}
	

	private void mapSDocument(int lastBaseUnitId) throws MMAX2WrapperException{
		String markableSPan =  makeSpan(1,lastBaseUnitId);
		{
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
	
	private SaltExtendedMarkable mapSToken(SToken sToken) throws MMAX2WrapperException {	
		return createMarkableForSNode(getNewId(),"",sToken,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STOKEN);
	}
	
	private SaltExtendedMarkable mapSpan(SSpan sSpan) throws MMAX2WrapperException {	
		return createMarkableForSNode(getNewId(),"",sSpan,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPAN);
	}
	
	private SaltExtendedMarkable mapStruct(SStructure struct) throws MMAX2WrapperException {
		ArrayList<SaltExtendedMarkable> sDomRelMarkableList = new ArrayList<SaltExtendedMarkable>(); 
		ArrayList<String> spans = new ArrayList<String>();
		for(Edge edge: this.sDocumentGraph.getOutEdges(struct.getSId())){
			if(edge instanceof SDominanceRelation){
				SDominanceRelation sDomRel = (SDominanceRelation) edge;
				SaltExtendedMarkable sDomRelMarkable = getSRelationMarkable(sDomRel);
				spans.add(sDomRelMarkable.getSpan());
				sDomRelMarkableList.add(sDomRelMarkable);
			}
		}
		
		SaltExtendedMarkable markable = createMarkableForSNode(getNewId(),makeSpan(spans),struct,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSTRUCT);		
		
		for(SaltExtendedMarkable sDomRelMarkable: sDomRelMarkableList){
			addPointerAttribute(sDomRelMarkable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSTRUCT,"struct",markable.getId());
		}
		
		return markable;
	}
	
	private SaltExtendedMarkable mapSTextualDS(STextualDS sTextualDs) throws MMAX2WrapperException {
		String markableSPan = makeSpan(this.spanStextualDSCorrespondance.get(sTextualDs));
	
		return createMarkableForSNode(getNewId(),markableSPan,sTextualDs,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUALDS);
	}
	
	
	private SaltExtendedMarkable mapDominanceRelation(SDominanceRelation domRel) throws MMAX2WrapperException  {
		SaltExtendedMarkable targetMarkable = getSNodeMarkable(domRel.getSStructuredTarget());

		String markableId = getNewId();
		String markableSPan = targetMarkable.getSpan();
		
		SaltExtendedMarkable markable = createMarkableForSRelation(markableId,markableSPan,domRel,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL);
		addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,"target",targetMarkable.getId());
		
		return markable;
	}
	
	private SaltExtendedMarkable mapPointingRelation(SPointingRelation pointRel) throws MMAX2WrapperException{
		SaltExtendedMarkable sourceMarkable = getSNodeMarkable(pointRel.getSSource());
		SaltExtendedMarkable targetMarkable = getSNodeMarkable(pointRel.getSTarget());
	
		String markableId = getNewId();
		String markableSPan = sourceMarkable.getSpan();
		
		SaltExtendedMarkable markable = createMarkableForSRelation(markableId,markableSPan,pointRel,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL);
		
		addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"source",sourceMarkable.getId());
		addFreetextAttribute(markable,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,"target",targetMarkable.getId());
		
		return markable;
	}
	
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
	
	private SaltExtendedMarkable mapTextualRelation(STextualRelation sTextualRelation) throws MMAX2WrapperException{
		SaltExtendedMarkable tokenMarkable = getSNodeMarkable(sTextualRelation.getSToken());
		SaltExtendedMarkable textualDsMarkable = getSNodeMarkable(sTextualRelation.getSTextualDS());
		
		String markableId = getNewId();
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
		String completeAttributeName;
		if(attributeNs != null){
			completeAttributeName = attributeNs+"__"+attributeName;
		}else{
			attributeNs = "";
			completeAttributeName = attributeName;
		}
		Scheme scheme = getScheme(schemeName);
		SaltExtendedMarkable markable = getMarkable(scheme,getNewId(),span,sType,sName,sId);
	
		addPointerAttribute(markable,schemeName,schemeBaseName,"target_markable",idRef);
		addFreetextAttribute(markable,schemeName,"namespace",attributeNs);
		addFreetextAttribute(markable,schemeName,"attr_name",attributeName);
		
		SaltExtendedMarkable containerMarkable = null;
		MatchCondition validated = matchSNode(schemeBaseName, attributeNs, attributeName, attributeValue,sLayers);
				
		if(validated != null){		
			if(!this.sNodesMarkables.containsKey(sELem)){
				this.sNodesMarkables.put(sELem,new Hashtable<Scheme, SaltExtendedMarkable>());
			}
			Hashtable<Scheme,SaltExtendedMarkable> associatedMarkables = this.sNodesMarkables.get(sELem);
			
			Scheme associatedScheme = getScheme(validated.getAssociatedSchemeName());
			if(!associatedMarkables.containsKey(associatedScheme)){
				containerMarkable = getMarkable(associatedScheme,getNewId(),span,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER,sName,sId);
				associatedMarkables.put(associatedScheme,containerMarkable);
				document.addMarkable(containerMarkable);
			}else{
				containerMarkable = associatedMarkables.get(associatedScheme);
			}
			
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
	
	
	private void mapSLayersToMarkable(SaltExtendedMarkable markable, String markableSKind, EList<SLayer> sLayers) throws MMAX2WrapperException{
		String schemeName = markableSKind + "_slayer_link";
		Scheme scheme = getScheme(schemeName);
		for(SLayer sLayer: sLayers){
			SaltExtendedMarkable linkMarkable = getMarkable(scheme,getNewId(),markable.getSpan(),SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER_LINK,markable.getSName(),markable.getSId());
			SaltExtendedMarkable sLayerMarkable = this.registeredSLayerMarkables.get(sLayer);
			
			addPointerAttribute(linkMarkable,schemeName,markableSKind,"SElement",markable.getId());
			addPointerAttribute(linkMarkable,schemeName,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER,"SLayer",sLayerMarkable.getId());
			document.addMarkable(linkMarkable);
		}
	}
	
	
	private void mapSTypesToMarkable(SaltExtendedMarkable markable, String markableSKind, EList<String> sTypes) throws MMAX2WrapperException{		
		String schemeName = markableSKind + "_stype_link";
		Scheme scheme = getScheme(schemeName);
		for(String sType: sTypes){
			SaltExtendedMarkable linkMarkable = getMarkable(scheme,getNewId(),markable.getSpan(),SaltExtendedMmax2Infos.SALT_INFO_TYPE_STYPE_LINK,markable.getSName(),markable.getSId());
			
			addPointerAttribute(linkMarkable,schemeName,markableSKind,"SElement",markable.getId());
			addFreetextAttribute(linkMarkable,schemeName,"SType",sType);
			
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
	
	private MarkableNominalAttributeFactory getMarkableNominalAttributeFactory(Scheme scheme,String attributeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkableNominalAttributeFactory(scheme.getName()+"_"+attributeName, attributeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}
		return (MarkableNominalAttributeFactory) attributeFactory;
	}
	
	private MarkablePointerAttributeFactory getMarkablePointerAttributeFactory(Scheme scheme,String attributeName, String targetSchemeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkablePointerAttributeFactory(scheme.getName()+"_"+attributeName,attributeName,targetSchemeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}		
		return (MarkablePointerAttributeFactory) attributeFactory;
	}
	
	private MarkableFreetextAttributeFactory getMarkableFreetextAttributeFactory(Scheme scheme,String attributeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkableFreetextAttributeFactory(scheme.getName()+"_"+attributeName,attributeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}		
		return (MarkableFreetextAttributeFactory) attributeFactory;
	}
	
	private MarkableSetAttributeFactory getMarkableSetAttributeFactory(Scheme scheme,String attributeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkableSetAttributeFactory(scheme.getName()+"_"+attributeName,attributeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}		
		return (MarkableSetAttributeFactory) attributeFactory;
	}
	
	
	public MatchCondition matchSNode(String sNodeType, String attributeNameSpace,String attributeName, String attributeValue, EList<SLayer> sLayers){

		MatchCondition validated = null;
		if(this.conditions.containsKey(sNodeType)){
			ArrayList<MatchCondition> specificConditions = this.conditions.get(sNodeType);
			for(MatchCondition matchCondition: specificConditions){
				if(matchCondition.isMatched(attributeNameSpace, attributeName, attributeValue,sLayers)){
					if(validated != null){
						throw new MMAX2ExporterException("Ambiguous macthing confitions '"+validated+"' and '"+matchCondition+"' have both matched '"+
								sNodeType+"/"+attributeNameSpace+"/"+attributeName+"/"+attributeValue+"'");
					}
					validated = matchCondition;
				}
			}
		}

		if(validated == null){
			ArrayList<MatchCondition> generalConditions = this.conditions.get("All");
			for(MatchCondition matchCondition: generalConditions){
				if(matchCondition.isMatched(attributeNameSpace, attributeName, attributeValue,sLayers)){
					if(validated != null){
						throw new MMAX2ExporterException("Ambiguous matching confitions '"+validated+"' and '"+matchCondition+"' have both matched '"+
								sNodeType+"/"+attributeNameSpace+"/"+attributeName+"/"+attributeValue+"'");
					}
					validated = matchCondition;
				}
			}
		}
		
		System.out.println("Yop => "+sNodeType+" "+validated+"\n");
		
		return validated;	
	}
}	


class MatchCondition{
	private Pattern attributeNamespacePattern = null;
	private Pattern attributeNamePattern = null;
	private Pattern attributeValuePattern = null;
	private Pattern sLayerNamePattern = null;
	private String associatedSchemeName;
	private String associatedAttributeName;
			
	public MatchCondition(String attributeNamespacePattern, String attributeNamePattern, String attributeValuePattern, String sLayerNamePattern,
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
		
		if(attributeNamespacePattern != null){
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
	

	
	
	
	

	

