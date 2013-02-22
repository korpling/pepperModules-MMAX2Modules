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
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.osgi.service.log.LogService;

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

	//private static final int DEFAULT_LIMIT_SIZE_NOMINAL_ATTR = 10;
	//private static int LIMIT_SIZE_NOMINAL_ATTR = DEFAULT_LIMIT_SIZE_NOMINAL_ATTR;
	
	//public static void SetLIMIT_SIZE_NOMINAL_ATTR(int limit){
	//	LIMIT_SIZE_NOMINAL_ATTR = limit;
	//}
	
	private static boolean ALLOW_MULTIPLE_SAME_LAYER_SNAME = false;
	
	public static void setALLOW_MULTIPLE_SAME_LAYER_SNAME(boolean ALLOW_MULTIPLE) {
		ALLOW_MULTIPLE_SAME_LAYER_SNAME = ALLOW_MULTIPLE;
	}
	
	
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
	
	private static Hashtable<String,String> elementsPointerInfos = new Hashtable<String,String>();
	private Hashtable<String,String> elementsId;
	private Hashtable<String,String> elementsSpan;

	private DocumentBuilder documentBuilder;
	
	private static final String lock = "lock";
	
	
	public Salt2MMAX2Mapper(DocumentBuilder documentBuilder) {
		this.documentBuilder = documentBuilder; 
	}
	
	
	private static int refCpt = 0;
	private static Hashtable<String,String> correspondanceRefInv = new Hashtable<String, String>();
	
	private int getRef(Object elem){
		synchronized(Salt2MMAX2Mapper.class){
			refCpt++;
			correspondanceRefInv.put(refCpt+"",elem.toString());
	
			return refCpt;
		}
	}
	
	private String getObjectFromRef(int ref){
		return correspondanceRefInv.get(ref+"");
	}
	
	private int markableIdCpt = 0;
	private String getNewId(Object elem){
		markableIdCpt++;
		elementsId.put(elem.toString(), markableIdCpt+"");
		
		return markableIdCpt + "";
	}
	
		
	private String getIdRef(Object elem){
		return "refId<"+getRef(elem)+">";
	}
	
	private String getId(String key){
		while(key.contains("refId<")){
			int startInd = key.indexOf("refId<");
			int stopInd = key.indexOf(">", startInd);
			String ref = key.substring(startInd, stopInd);
			ref = ref.replaceAll("refId<", "");
			ref = ref.replaceAll(">", "");
			
			int refInt = new Integer(ref);
			String objectKey = getObjectFromRef(refInt);
			
			String id = elementsId.get(objectKey);
			if(id == null){
				return null;
			}
			
			key = key.replaceAll(java.util.regex.Pattern.quote("refId<"+ref+">"), java.util.regex.Matcher.quoteReplacement(id));
		}		
		return key; 
	}
	
	
	private synchronized String getNewLevel(Object elem, String level){
		elementsPointerInfos.put(elem.toString(),level);
		return level;
	}
		
	private String getLevelRef(Object elem){
		return "refLevel<"+getRef(elem)+">";
	}
	
	private String getLevel(String key){
		while(key.contains("refLevel<")){
			int startInd = key.indexOf("refLevel<");
			int stopInd = key.indexOf(">", startInd);
			String ref = key.substring(startInd, stopInd);
			ref = ref.replaceAll("refLevel<", "");
			ref = ref.replaceAll(">", "");
			
			int refInt = new Integer(ref);
			Object objectKey = getObjectFromRef(refInt);
			
			String level = elementsPointerInfos.get(objectKey);
			
			key = key.replaceAll(java.util.regex.Pattern.quote("refLevel<"+ref+">"), java.util.regex.Matcher.quoteReplacement(level));		
		}		
		return key;
	}
	
	private String getNewSpan(Object elem, String start, String end){
		String span = start+".." + end;
		elementsSpan.put(elem.toString(),span);
		
		return span;
	}
	
	private String getNewSpan(Object elem, ArrayList<String> indices){
		String span = StringUtils.join(indices.toArray(new String[indices.size()]),",");
		elementsSpan.put(elem.toString(),span);
		
		return span;
	}
	
	private String getSpan(String key){
		while(key.contains("refSpan<")){
			int startInd = key.indexOf("refSpan<");
			int stopInd = key.indexOf(">", startInd);
			String ref = key.substring(startInd, stopInd);
			ref = ref.replaceAll("refSpan<", "");
			ref = ref.replaceAll(">", "");
			
			int refInt = new Integer(ref);
			Object objectKey = getObjectFromRef(refInt);
			String span = elementsSpan.get(objectKey);
			
			if(span == null){
				return null;
			}
			
			if(span.contains("refSpan<")){
				span = getSpan(span);
				if(span == null)
					return null;

				elementsSpan.put("span",span);
			}
			key = key.replaceAll(java.util.regex.Pattern.quote("refSpan<"+ref+">"), java.util.regex.Matcher.quoteReplacement(span));		
		}
		
		return key;
	}
	
	private String getSpanRef(Object elem){
		return "refSpan<"+getRef(elem)+">";
	}
	
	public void mapSDocument(SaltExtendedCorpus corpus, SDocument sDocument, SaltExtendedDocumentFactory factory, SchemeFactory schemeFactory) throws MMAX2ExporterException, MMAX2WrapperException 
	{
		String documentName = sDocument.getSName();
		this.elementsId = new Hashtable<String, String>();
		this.elementsSpan = new Hashtable<String, String>();
		
		SaltExtendedDocument document = factory.newDocument(documentName);
		
		// map textual data sources
		mapTextualDataSources(sDocument.getSDocumentGraph().getSTextualDSs(),document,schemeFactory);
		int nbCarac = document.getBaseDataUnits().size();

		// map all layers
		mapDocument(sDocument.getSDocumentGraph(),document, schemeFactory, nbCarac);		
		
		Hashtable<String,String> Ids = new Hashtable<String, String>();
		for(SaltExtendedMarkable markable: document.getAllSaltExtendedMarkables()){
			String span = markable.getSpan();
			
			if(span.contains("refSpan<")){
				span = getSpan(span);
				if(span == null){
					if (this.getLogService()!= null)
						this.logService.log(this.logService.LOG_WARNING, "Document '"+sDocument.getSName()+"' removing a markable '"+markable.getLevelName()+"' with SName '"+markable.getSName()+"' because its span '"+markable.getSpan()+"' references an unknown node, Salt graph is maybe incomplete...");
					document.removeMarkable(markable);
					continue;
				}
				
				markable.setSpan(span);
			}
			if(span.equals("")){
				if (this.getLogService()!= null)
					this.logService.log(this.logService.LOG_WARNING, "Document '"+sDocument.getSName()+"' removing a markable '"+markable.getLevelName()+"' with SName '"+markable.getSName()+"' because its span is empty, Salt graph is maybe incomplete...");
				document.removeMarkable(markable);
			}else{
				Ids.put(markable.getId(), "");
			}
		}
		
		for(SaltExtendedMarkable markable: document.getAllSaltExtendedMarkables()){	
			for(MarkableAttribute attribute: markable.getAttributes()){
				String value = attribute.getValue();
				if(value.contains("refId<")){
					value = getId(value);
					if(value == null){
						if (this.getLogService()!= null)
							this.logService.log(this.logService.LOG_WARNING, "Document '"+sDocument.getSName()+"' removing a markable '"+markable.getLevelName()+"' with SName '"+markable.getSName()+"' because its attribute '"+attribute.getName()+"="+attribute.getValue()+"' references an unknown node, Salt graph is maybe incomplete...");
						document.removeMarkable(markable);
						continue;
					}
					
					
					attribute.setValue(value);
				}
				if(attribute.getFactory().getAttributeType().equals(MarkablePointerAttributeFactory.pointerType)){
					if(!Ids.containsKey(value)){
						MarkablePointerAttributeFactory pointerFac = (MarkablePointerAttributeFactory) attribute.getFactory();
						if (this.getLogService()!= null)
							this.logService.log(this.logService.LOG_WARNING, "Document '"+sDocument.getSName()+"' removing pointing attribute value '"+attribute.getFactory().getAttributeName()
									+"' pointing to a markable of type '"+pointerFac.getTargetSchemeName()+"' because markable is not present (it has maybe been removed earlier)");
						attribute.setValue("");
					}
				}
			}
		}
		corpus.addDocument(document);
	}

	private void mapTextualDataSources(EList<STextualDS> sTextualDS, SaltExtendedDocument document, SchemeFactory schemeFactory) throws MMAX2WrapperException{		
		if (sTextualDS.isEmpty())
			throw new MMAX2ExporterException("Cannot map Data Sources because there are none");
		
		for (STextualDS sTextualDs : sTextualDS){
			char[] caracs = sTextualDs.getSText().toCharArray();
			
			for(int i = 0; i< caracs.length; i++){
				document.addBaseDataUnit(document.newBaseDataUnit("word_"+(i+1),caracs[i]+""));
			}					
		}
	}
	
	public void finalizeCorpusStructure(SaltExtendedCorpus corpus, SchemeFactory schemeFactory) throws MMAX2ExporterException{
		for (Scheme scheme: corpus.getSchemes()){
			String schemeName = scheme.getName();
			ArrayList<MarkableAttributeFactory> markableAttributesFactories = scheme.getAttributesFactories();
			
			for(MarkableAttributeFactory markableAttributeFactory: markableAttributesFactories){
				if(markableAttributeFactory instanceof MarkablePointerAttributeFactory){
					MarkablePointerAttributeFactory realFactory = (MarkablePointerAttributeFactory) markableAttributeFactory;
					String targetScheme = realFactory.getTargetSchemeName();
					if(targetScheme.startsWith("refLevel<")){
						realFactory.setTargetSchemeName(getLevel(targetScheme));
					}
//				}else if(markableAttributeFactory instanceof MarkableNominalAttributeFactory){
//					MarkableNominalAttributeFactory realFactory = (MarkableNominalAttributeFactory) markableAttributeFactory;
//					ArrayList<String> valuesAccepted = realFactory.getValuesAccepted();
//					if(valuesAccepted.size() > LIMIT_SIZE_NOMINAL_ATTR){
//						MarkableFreetextAttributeFactory newFreetextFactory = schemeFactory.newMarkableFreetextAttributeFactory(markableAttributeFactory.getAttributeName());
//						scheme.removeMarkableAttributesFactory(markableAttributeFactory);
//						scheme.addMarkableAttributeFactory(newFreetextFactory);
//						
//						for (SaltExtendedDocument document : corpus.getSaltExtendedDocuments()) {
//							for(SaltExtendedMarkable markable: document.getAllSaltExtendedMarkables()){
//								if(markable.getLevelName().equals(schemeName)){
//									ArrayList<MarkableAttribute> markableAttributes= markable.getAttributes();
//									for(MarkableAttribute markableAttribute : markableAttributes){
//										if(markableAttribute.getFactory().equals(markableAttributeFactory)){
//											try{
//												MarkableAttribute newMarkableAttribute = newFreetextFactory.newAttribute(markableAttribute.getValue());
//												markable.removeAttribute(markableAttribute);
//												markable.addAttribute(newMarkableAttribute);
//											}catch(MMAX2WrapperException e){
//												throw new MMAX2ExporterException("Developper error: should never happen for freetext:"+e.getMessage());
//											}
//										}
//									}
//								}
//							}
//						}
//						
//					}
				}
			}
		}
	}
	

	

	private void mapDocument(SDocumentGraph sDocumentGraph, SaltExtendedDocument document, SchemeFactory schemeFactory, int nbCarac) throws MMAX2WrapperException {
		
		
		EList<SSpan> layerSpanList ;
		EList<SSpan> spanList = new BasicEList<SSpan>(sDocumentGraph.getSSpans());
		
		EList<SStructure> layerStructList ;
		EList<SStructure> structList = new BasicEList<SStructure>(sDocumentGraph.getSStructures());
		
		EList<SToken> tokenList = new BasicEList<SToken>(sDocumentGraph.getSTokens());
		
		EList<STextualDS> layerTextualDsList ;
		EList<STextualDS> textualDsList = new BasicEList<STextualDS>(sDocumentGraph.getSTextualDSs());
		
		EList<SPointingRelation> layerPointingRelationList;
		EList<SPointingRelation> pointingRelationList = new BasicEList<SPointingRelation>(sDocumentGraph.getSPointingRelations());
		
		EList<SSpanningRelation> layerSpanningRelationList;
		EList<SSpanningRelation> spanningRelationList = new BasicEList<SSpanningRelation>(sDocumentGraph.getSSpanningRelations());
		
		EList<SDominanceRelation> layerDominanceRelationList;
		EList<SDominanceRelation> dominanceRelationList = new BasicEList<SDominanceRelation>(sDocumentGraph.getSDominanceRelations());
		
		EList<STextualRelation> layerTextualRelationList;
		EList<STextualRelation> textualRelationList = new BasicEList<STextualRelation>(sDocumentGraph.getSTextualRelations());
		
		
		
		/**
		 * iterate over all layers
		 */
		Hashtable<String,String> sLayersNames = new Hashtable<String, String>();
		for (SLayer layer : sDocumentGraph.getSLayers()){
			if(!sLayersNames.containsKey(layer.getSName())){
				sLayersNames.put(layer.getSName(), "");
			}else if(ALLOW_MULTIPLE_SAME_LAYER_SNAME == false){
				throw new MMAX2ExporterException("Document '"+document.getDocumentId()+"' contains several SLayers with the same SName '"+layer.getSName()+"', layers will be merged when importing back...");
			}
			
			layerSpanList = new BasicEList<SSpan>();
			layerStructList = new BasicEList<SStructure>();
			layerTextualDsList = new BasicEList<STextualDS>();
			
			layerPointingRelationList = new BasicEList<SPointingRelation>();
			layerTextualRelationList = new BasicEList<STextualRelation>();
			layerSpanningRelationList = new BasicEList<SSpanningRelation>();
			layerDominanceRelationList = new BasicEList<SDominanceRelation>();
						
			/**
			 * fetch Pointing Relations for this layer
			 */
			for (Edge edge : layer.getEdges()){
				if (edge instanceof SPointingRelation){
					layerPointingRelationList.add((SPointingRelation)edge);
					pointingRelationList.remove((SPointingRelation)edge);
				}else if(edge instanceof SSpanningRelation){
					layerSpanningRelationList.add((SSpanningRelation)edge);
					spanningRelationList.remove((SSpanningRelation) edge);
				}else if(edge instanceof SDominanceRelation){
					layerDominanceRelationList.add((SDominanceRelation) edge);
					dominanceRelationList.remove((SDominanceRelation) edge);
				}else if(edge instanceof STextualRelation){
					layerTextualRelationList.add((STextualRelation) edge);
					textualRelationList.remove((STextualRelation) edge);	
					tokenList.remove(((STextualRelation) edge).getSToken());
				}else{
					throw new MMAX2ExporterException("Developper Error: unknow type of edge met => '"+edge.getClass()+"'");
				}
			}
			
			/**
			 * iterate over all nodes.
			 * put the nodes in the right lists, according to their type
			 */
			for (SNode sNode : layer.getSNodes()){
				if (sNode instanceof SToken){
					// SToken are exported along with STextualRelation
				}
				else if (sNode instanceof SSpan ){
					layerSpanList.add((SSpan) sNode);
					spanList.remove((SSpan) sNode);
				}
				else if (sNode instanceof SStructure ){
					layerStructList.add((SStructure) sNode);
					structList.remove((SStructure) sNode);
				}else if (sNode instanceof STextualDS ){
					layerTextualDsList.add((STextualDS) sNode);
					textualDsList.remove((STextualDS) sNode);
				}else{
					throw new MMAX2ExporterException("Developper Error: unknow type of node met => '"+sNode.getClass()+"'");
				}
			}
			
			/**
			 * We searched the layer completly
			 * now we have to map everything
			 */			
			String sLayer_name = layer.getSName();
			String sLayer_id = layer.getSId();
			
			
			mapSpans(sDocumentGraph, document,schemeFactory, sLayer_name, sLayer_id, layerSpanList);
			mapStructs(document,schemeFactory, sLayer_name, sLayer_id, layerStructList);
			mapSTextualDS(document,schemeFactory, sLayer_name, sLayer_id,layerTextualDsList);
			mapDominanceRelations(document,schemeFactory, sLayer_name, sLayer_id, layerDominanceRelationList);
			mapPointingRelations(document,schemeFactory, sLayer_name, sLayer_id, layerPointingRelationList);
			mapSpanningRelations(document,schemeFactory, sLayer_name, sLayer_id, layerSpanningRelationList);
			mapTextualRelations(document,schemeFactory, sLayer_name, sLayer_id, layerTextualRelationList);
			
			/**
			 * map layer annotations
			 */
			{
				String markableId = getNewId(layer);
				String markableSPan =  getNewSpan(layer,"1",nbCarac + "");
				String markableSName = layer.getSName();
				String markableSId = layer.getSId();
				String schemeName = "layer"; 
			
				createMarkable(document,schemeFactory,schemeName,markableId,markableSPan,
							   markableSName,markableSId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER,
							   schemeName,layer,layer);
			}
			
		}
		
		/**
		 * when there are things that are not in one layer, create "nolayer" files  
		 */
	
		mapSpans(sDocumentGraph, document,schemeFactory, "nolayer", "nolayer", spanList);
		mapStructs(document,schemeFactory, "nolayer", "nolayer", structList);
		mapSTextualDS(document,schemeFactory, "nolayer","nolayer", textualDsList);
		mapDominanceRelations(document,schemeFactory,"nolayer", "nolayer", dominanceRelationList);
		mapPointingRelations(document,schemeFactory, "nolayer", "nolayer", pointingRelationList);
		mapSpanningRelations(document,schemeFactory, "nolayer", "nolayer", spanningRelationList);
		mapTextualRelations(document,schemeFactory, "nolayer", "nolayer", textualRelationList);
		
		/**
		 * map document annotations
		 */
		{
			String markableId = getNewId(sDocumentGraph);
			String markableSPan =  getNewSpan(sDocumentGraph,"1",nbCarac + "");
			String markableSName = sDocumentGraph.getSName();
			String markableSId = sDocumentGraph.getSId();
			String schemeName = SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT; 
		
			createMarkable(document,schemeFactory,schemeName,markableId,markableSPan,
						   markableSName,markableSId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT,
						   schemeName,sDocumentGraph,sDocumentGraph);
		}
		
		
		
		for(STextualRelation sTextualRelation: textualRelationList){
			tokenList.remove(sTextualRelation.getSToken());
		}
		
		if(tokenList.size() != 0){
			if (this.getLogService()!= null)
				this.logService.log(this.logService.LOG_WARNING, "There are tokens without textual relation. I shall associate them with dummy 1..1 span");
			
			for(SToken sToken: tokenList){
				getNewSpan(sToken,"1","1");
			}
		}
		
	}
	
	
	
	
	private void mapSpans(SDocumentGraph graph,SaltExtendedDocument document,SchemeFactory schemeFactory, String sLayerName, String sLayerId, EList<SSpan> layerSpanList) throws MMAX2WrapperException {	
		/**
		 * create SDocumentStructureAccessor in order to have access to overlapped tokens
		 */
	
		
		for (SSpan sSpan : layerSpanList){
			ArrayList<String> sSpanSpan = new ArrayList<String>();		
				for (Edge edge : graph.getOutEdges(((SNode) sSpan).getSId())){
				if (edge instanceof SSpanningRelation){ 
					String markableSPan = getSpanRef(((SSpanningRelation) edge).getSToken()); 
					sSpanSpan.add(markableSPan);
				}
			}
			
			String markableId = getNewId(sSpan);
			String markableSPan = getNewSpan(sSpan,sSpanSpan);
			String markableSName = sSpan.getSName();
			String markableSId = sSpan.getSId();
			String sSpanchemeName = getNewLevel(sSpan,sLayerName+"_span");
			
			createMarkable(document,schemeFactory,sLayerId,markableId,markableSPan,
						   markableSName,markableSId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPAN,
						   sSpanchemeName,sSpan,sSpan);
		}
	}
	
	private void mapStructs(SaltExtendedDocument document, SchemeFactory schemeFactory, String sLayerName, String sLayerId, EList<SStructure> layerStructList) throws MMAX2WrapperException {
		for (SStructure struct : layerStructList){
	
			ArrayList<String> sStructSpan = new ArrayList<String>();
			for (Edge edge : struct.getSDocumentGraph().getOutEdges(((SNode)struct).getSId())){
				if (edge instanceof SDominanceRelation){ 
					sStructSpan.add(getSpanRef(((SDominanceRelation)edge).getSTarget()));
				}
			}

			String markableId = getNewId(struct);
			String markableSPan =  getNewSpan(struct,sStructSpan);
			String markableSName = struct.getSName();
			String markableSId = struct.getSId();
			String schemeName = getNewLevel(struct,sLayerName+"_struct");
			
			createMarkable(document,schemeFactory,sLayerId,markableId,markableSPan,
						   markableSName,markableSId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSTRUCT,
						   schemeName,struct,struct);
		}		
	}
	
	
	
	private void mapSTextualDS(SaltExtendedDocument document, SchemeFactory schemeFactory, String sLayerName, String sLayerId, EList<STextualDS> layerTextualDsList) throws MMAX2WrapperException {
		for (STextualDS sTextualDs : layerTextualDsList){
			String markableId = getNewId(sTextualDs);
			String markableSPan = getNewSpan(sTextualDs,(sTextualDs.getSStart()+1)+"",sTextualDs.getSEnd()+"");
			String markableSName = sTextualDs.getSName();
			String markableSId = sTextualDs.getSId();
			String schemeName = sLayerName+"_TextualDS"; 
	
			createMarkable(document,schemeFactory,sLayerId,markableId,markableSPan,
						   markableSName,markableSId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUALDS,
						   schemeName,sTextualDs,sTextualDs);
		}
	}

	
	private void mapDominanceRelations(SaltExtendedDocument document, SchemeFactory schemeFactory, String sLayerName, String sLayerId, EList<SDominanceRelation> layerDominanceRelationList) throws MMAX2WrapperException  {
		for (SDominanceRelation domRel : layerDominanceRelationList){
			String typeStr = "@none@";
			EList<String> types = domRel.getSTypes();
			if(types != null){
				typeStr =  StringUtils.join(types,"<->");
			}
			String markableId = getNewId(domRel);
			String markableSPan = getSpanRef(domRel.getSSource());
			String markableSName = domRel.getSName();
			String markableSId = domRel.getSId();
			String schemeName = getNewLevel(domRel,sLayerName+"_DomRel");
			
			SaltExtendedMarkable markable = createMarkable(document,schemeFactory,sLayerId,markableId,markableSPan,
														   markableSName,markableSId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL,
														   schemeName,domRel,domRel);
			addPointerAttribute(markable,document,schemeFactory,schemeName,getLevelRef(domRel.getSSource()),"struct",getIdRef(domRel.getSSource()));
			addPointerAttribute(markable,document,schemeFactory,schemeName,getLevelRef(domRel.getSTarget()),"target",getIdRef(domRel.getSTarget()));
			addFreetextAttribute(markable,document,schemeFactory,schemeName,"type",typeStr);
		}		
	}
	
	

	private void mapPointingRelations(SaltExtendedDocument document, SchemeFactory schemeFactory, String sLayerName, String sLayerId, EList<SPointingRelation> layerPointingRelationList) throws MMAX2WrapperException  {
		for (SPointingRelation pointRel : layerPointingRelationList){
			String typeStr = "@none@";
			EList<String> types = pointRel.getSTypes();
			if(types != null){
				typeStr =  StringUtils.join(types,"<->");
			}
			String markableId = getNewId(pointRel);
			String markableSPan = getSpanRef(pointRel.getSSource());
			String markableSName = pointRel.getSName();
			String markableSId = pointRel.getSId();
			String schemeName = getNewLevel(pointRel,sLayerName+"_pointer");
			
			SaltExtendedMarkable markable = createMarkable(document,schemeFactory,sLayerId,markableId,markableSPan,
														   markableSName,markableSId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL,
														   schemeName,pointRel,pointRel);
			addPointerAttribute(markable,document,schemeFactory,schemeName,getLevelRef(pointRel.getSSource()),"source",getIdRef(pointRel.getSSource()));
			addPointerAttribute(markable,document,schemeFactory,schemeName,getLevelRef(pointRel.getSTarget()),"target",getIdRef(pointRel.getSTarget()));
			addFreetextAttribute(markable,document,schemeFactory,schemeName,"type",typeStr);
		}		
	}
	
	private void mapSpanningRelations(SaltExtendedDocument document, SchemeFactory schemeFactory, String sLayerName, String sLayerId,EList<SSpanningRelation> layerSpanningRelationList) throws MMAX2WrapperException  {
		String schemeName = sLayerName+"_spanRel";
		for (SSpanningRelation sSpanningRel : layerSpanningRelationList){
			SSpan sourceSpan = sSpanningRel.getSSpan();
			SToken targetToken = sSpanningRel.getSToken();
			
			String markableId = getNewId(sSpanningRel);
			String markableSPan = getSpanRef(sSpanningRel.getSToken()); 
			String markableSName = sSpanningRel.getSName();
			String markableSId = sSpanningRel.getSId();
			
			SaltExtendedMarkable markable = createMarkable(document,schemeFactory,sLayerId,markableId,markableSPan,
														   markableSName,markableSId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPANNING_REL,
														   schemeName,sSpanningRel,sSpanningRel);
			addPointerAttribute(markable,document,schemeFactory,schemeName,getLevelRef(sourceSpan),"source_span",getIdRef(sourceSpan));
			addPointerAttribute(markable,document,schemeFactory,schemeName,getLevelRef(targetToken),"target_token",getIdRef(targetToken));
		}		
	}
	

	private void mapTextualRelations(SaltExtendedDocument document, SchemeFactory schemeFactory, String sLayerName, String sLayerId,EList<STextualRelation> sTextRels) throws MMAX2WrapperException{

		for (STextualRelation sTextualRelation : sTextRels){
			SToken token = sTextualRelation.getSToken();
			
			String markableId = getNewId(token);
			String markableSPan = getNewSpan(token,(sTextualRelation.getSStart()+1)+"",sTextualRelation.getSEnd() + "");
			String markableSName = token.getSName();
			String markableSId = token.getSId();
			String schemeName = getNewLevel(token,sLayerName+"_token");
			
			createMarkable(document,schemeFactory,sLayerId,markableId,markableSPan,
						   markableSName,markableSId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_STOKEN,
						   schemeName,token,token);
			
			String relMarkableId = getNewId(sTextualRelation);
			String relMarkableSPan = getNewSpan(sTextualRelation,(sTextualRelation.getSStart()+1)+"",sTextualRelation.getSEnd() + "");
			String relMarkableSName = sTextualRelation.getSName();
			String relMarkableSId = sTextualRelation.getSId();
			String relSchemeName = getNewLevel(sTextualRelation,sLayerName+"_textualRel");
			
			SaltExtendedMarkable relMarkable = createMarkable(document,schemeFactory,sLayerId,relMarkableId,relMarkableSPan,
					relMarkableSName,relMarkableSId, SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUAL_REL,
					relSchemeName,sTextualRelation,sTextualRelation);
			addPointerAttribute(relMarkable,document,schemeFactory,relSchemeName,schemeName,"target_token",getIdRef(token));
			addFreetextAttribute(relMarkable,document,schemeFactory,relSchemeName,"target_textual_ds",getIdRef(sTextualRelation.getSTextualDS()));
		}
	}
	
	
	/**
	 *  METHODS FOR HANDLING ANNOTATIONS OVER ELEMENTS
	 */
	
	
	public SaltExtendedMarkable createMarkable(SaltExtendedDocument document, SchemeFactory schemeFactory, String sLayerId, String markableId, String markableSpan, String markableSName, String sId, String markableSKind, String schemeName,SAnnotatableElement SNodeAnno, SMetaAnnotatableElement SNodeMetaAnno) throws MMAX2WrapperException{
		Corpus corpus = document.getCorpus();
		Scheme scheme = getScheme(corpus,schemeFactory,schemeName);
		SaltExtendedMarkable markable = getMarkable(document,scheme,markableId,markableSpan,sLayerId,markableSKind,markableSName,sId);
		document.addMarkable(markable);
		
		mapSMetaAnnotations(corpus,document,schemeFactory,sLayerId,markableSName,sId,SNodeMetaAnno,markableId,markableSpan,schemeName);
		mapSAnnotations(corpus,document,schemeFactory,sLayerId,markableSName,sId,SNodeAnno,markableId,markableSpan,schemeName);
		
		return markable;
	}
	
	public void addPointerAttribute(SaltExtendedMarkable markable, SaltExtendedDocument document, SchemeFactory schemeFactory, String schemeName,String targetScheme, String attributeName, String targetId) throws MMAX2WrapperException{
		Corpus corpus = document.getCorpus();
		Scheme scheme = getScheme(corpus,schemeFactory,schemeName);
		MarkablePointerAttributeFactory pointerAttributeFactory = getMarkablePointerAttributeFactory(schemeFactory,scheme,attributeName,targetScheme);		
		markable.addAttribute(pointerAttributeFactory.newAttribute(targetId));	
	}
	
	public void addNominalAttribute(SaltExtendedMarkable markable, SaltExtendedDocument document, SchemeFactory schemeFactory, String schemeName,  String attributeName, String attributeValue) throws MMAX2WrapperException{
		Corpus corpus = document.getCorpus();
		Scheme scheme = getScheme(corpus,schemeFactory,schemeName);

		MarkableNominalAttributeFactory typeAttributeFactory = getMarkableNominalAttributeFactory(schemeFactory,scheme,attributeName);	
		if(!typeAttributeFactory.isValueAccepted(attributeValue)){
			typeAttributeFactory.enableValue(attributeValue);
		}
		markable.addAttribute(typeAttributeFactory.newAttribute(attributeValue));
	}
	
	public void addFreetextAttribute(SaltExtendedMarkable markable, SaltExtendedDocument document, SchemeFactory schemeFactory, String schemeName,  String attributeName, String attributeValue) throws MMAX2WrapperException{
		Corpus corpus = document.getCorpus();
		Scheme scheme = getScheme(corpus,schemeFactory,schemeName);
		MarkableFreetextAttributeFactory typeAttributeFactory = getMarkableFreetextAttributeFactory(schemeFactory,scheme,attributeName);	
		markable.addAttribute(typeAttributeFactory.newAttribute(attributeValue));
	}
	
	public void addSetAttribute(SaltExtendedMarkable markable, SaltExtendedDocument document, SchemeFactory schemeFactory, String schemeName,  String attributeName, String attributeValue) throws MMAX2WrapperException{
		Corpus corpus = document.getCorpus();
		Scheme scheme = getScheme(corpus,schemeFactory,schemeName);
		MarkableSetAttributeFactory typeAttributeFactory = getMarkableSetAttributeFactory(schemeFactory,scheme,attributeName);	
		markable.addAttribute(typeAttributeFactory.newAttribute(attributeValue));
	}
	
	private void mapSMetaAnnotations(Corpus corpus, 
									SaltExtendedDocument document, 
									SchemeFactory schemeFactory, 
									String sLayerId, 
									String sName, 
									String sId, 
									SMetaAnnotatableElement  sElem, 
									String markableId, 
									String markableSpan, 
									String schemeBaseName) throws MMAX2WrapperException{
		
		for (SMetaAnnotation sAnnotation : sElem.getSMetaAnnotations()){
			String attributeName = sAnnotation.getSName();
			String attributeValue = sAnnotation.getSValueSTEXT();
			String attributeNs = sAnnotation.getSNS();
			mapAnnotations(corpus,document,schemeFactory,sLayerId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SMETAANNOTATION,sName,sId,markableId,markableSpan,schemeBaseName,attributeName,attributeNs,attributeValue);	
		}
	}
	

	private void mapSAnnotations(Corpus corpus, 
								SaltExtendedDocument document, 
								SchemeFactory schemeFactory, 
								String sLayerId, 
								String sName, 
								String sId, 
								SAnnotatableElement  sELem, 
								String elementId, 
								String elementSpan, 
								String schemeBaseName) throws MMAX2WrapperException{
		
		for (SAnnotation sAnnotation : sELem.getSAnnotations()){
			String attributeName = sAnnotation.getSName();
			String attributeValue = sAnnotation.getSValueSTEXT();
			String attributeNs = sAnnotation.getSNS();
			mapAnnotations(corpus,document,schemeFactory,sLayerId,SaltExtendedMmax2Infos.SALT_INFO_TYPE_SANNOTATION,sName,sId, elementId,elementSpan,schemeBaseName,attributeName,attributeNs,attributeValue);
		}
	}
	
	private void mapAnnotations(Corpus corpus, 
								SaltExtendedDocument document, 
								SchemeFactory schemeFactory, 
								String sLayerId, 
								String sType, 
								String sName, 
								String sId, 
								String idRef, 
								String span, 
								String schemeBaseName,
								String attributeName,
								String attributeNs,
								String attributeValue) throws MMAX2WrapperException{
		
		String schemeName = schemeBaseName+"_"+attributeName;
		Scheme scheme = getScheme(corpus,schemeFactory,schemeName);
		SaltExtendedMarkable markable = getMarkable(document,scheme,getNewId(idRef+"_"+attributeName),span,sLayerId,sType,sName,sId);
	
		addPointerAttribute(markable,document,schemeFactory,schemeName,schemeBaseName,"target_markable",idRef);

		if(attributeNs == null)
			attributeNs = "";
		
		addFreetextAttribute(markable,document,schemeFactory,schemeName,"namespace",attributeNs);
		addFreetextAttribute(markable,document,schemeFactory,schemeName,"attr_name",attributeName);
		addFreetextAttribute(markable,document,schemeFactory,schemeName,"attr_value",attributeValue);	
		
		document.addMarkable(markable);
	}
	
	
	private Scheme getScheme(Corpus corpus, SchemeFactory schemeFactory, String schemeName){
		Scheme scheme = corpus.getScheme(schemeName);
		if(scheme == null){
			scheme = schemeFactory.newScheme(schemeName); 
			corpus.addScheme(scheme);
		}		
		return scheme;
	}
	
	private SaltExtendedMarkable getMarkable(SaltExtendedDocument document, Scheme scheme, String markableId, String markableSpan, String sLayerId, String sType, String sName, String sId){
		SaltExtendedMarkableFactory markableFactory = document.getFactory().getMarkableFactory(scheme);
		if(markableFactory == null){
			markableFactory = new SaltExtendedMarkableFactory(scheme, this.documentBuilder);
			document.getFactory().addMarkableFactory(markableFactory);		
		}
		SaltExtendedMarkable markable = markableFactory.newMarkable(markableId,markableSpan, new ArrayList<MarkableAttribute>(), sLayerId, sType, sName, sId);
		return markable;
	}
	
	private MarkableNominalAttributeFactory getMarkableNominalAttributeFactory(SchemeFactory schemeFactory,Scheme scheme,String attributeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkableNominalAttributeFactory(attributeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}
		return (MarkableNominalAttributeFactory) attributeFactory;
	}
	
	private MarkablePointerAttributeFactory getMarkablePointerAttributeFactory(SchemeFactory schemeFactory,Scheme scheme,String attributeName, String targetSchemeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkablePointerAttributeFactory(attributeName,targetSchemeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}		
		return (MarkablePointerAttributeFactory) attributeFactory;
	}
	
	private MarkableFreetextAttributeFactory getMarkableFreetextAttributeFactory(SchemeFactory schemeFactory,Scheme scheme,String attributeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkableFreetextAttributeFactory(attributeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}		
		return (MarkableFreetextAttributeFactory) attributeFactory;
	}
	
	private MarkableSetAttributeFactory getMarkableSetAttributeFactory(SchemeFactory schemeFactory,Scheme scheme,String attributeName){
		MarkableAttributeFactory attributeFactory = scheme.getAttributeFactory(attributeName);
		if(attributeFactory == null){
			attributeFactory = schemeFactory.newMarkableSetAttributeFactory(attributeName);
			scheme.addMarkableAttributeFactory(attributeFactory);
		}		
		return (MarkableSetAttributeFactory) attributeFactory;
	}
	
	
}	
	

