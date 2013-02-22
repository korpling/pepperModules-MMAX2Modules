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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.log.LogService;

import com.sun.org.apache.bcel.internal.generic.NEW;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkable;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.exceptions.MMAX2ImporterException;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructuredNode;
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
import eurac.commul.annotations.mmax2wrapper.DocumentFactory.Document.BaseDataUnit;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableAttributeFactory.MarkableAttribute;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableFreetextAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableNominalAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkablePointerAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.MarkableSetAttributeFactory;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory.Scheme;

/**
 * This class exports data in MMAX2 format to Salt.
 * The code has been initially adapted from PAULA2SAltMapper.
 * 
 * @author Lionel Nicolas
 * 
 */

public class MMAX22SaltMapper
{	
	
	private Hashtable<String,SNode> SNodesHash;
	private Hashtable<String,SLayer> SLayerHash;
	private ArrayList<STextualDS> STextualDsArray;
	

// ================================================ start: LogService	
	private LogService logService;

	public void setLogService(LogService logService) 
	{
		this.logService = logService;
	}
	
	public LogService getLogService() 
	{
		return(this.logService);
	}
// ================================================ end: LogService
	
	
	public void mapSDocument(SaltExtendedDocument document,SDocument sDocument){
		this.SNodesHash = new Hashtable<String, SNode>();		
		this.SLayerHash = new Hashtable<String, SLayer>();
		this.STextualDsArray = new ArrayList<STextualDS>();
	
		SDocumentGraph sDocumentGraph = sDocument.getSDocumentGraph();
		sDocumentGraph.setSName(document.getDocumentId()+"_graph");
	
		Hashtable<String,int[]> indicesTokens = new Hashtable<String, int[]>();
		ArrayList<String> tokens = new ArrayList<String>();
		{
			int indice = 0;
			for(BaseDataUnit baseDataUnit: document.getBaseDataUnits()){
				int newIndice = indice + baseDataUnit.getText().length();
				int[] indices = {indice,newIndice};
				indicesTokens.put(baseDataUnit.getId(),indices);
				indice = newIndice;
				tokens.add(baseDataUnit.getText());
			}
		}
		String totalText = StringUtils.join(tokens,""); 
		

		
		//ArrayList<SLayer> sLayerNodes = new ArrayList<SLayer>();
		//ArrayList<SaltExtendedMarkable> sLayerMarkables = new ArrayList<SaltExtendedMarkable>();
		
		//ArrayList<STextualDS> sTextualDSNodes = new ArrayList<STextualDS>();
		//ArrayList<SaltExtendedMarkable> sTextualDSMarkables = new ArrayList<SaltExtendedMarkable>();
		
		//ArrayList<SStructure> sStructNodes = new ArrayList<SStructure>();
		//ArrayList<SaltExtendedMarkable> sStructMarkables = new ArrayList<SaltExtendedMarkable>();
		
		//ArrayList<SToken> sTokenNodes = new ArrayList<SToken>();
		//ArrayList<SaltExtendedMarkable> sTokenMarkables = new ArrayList<SaltExtendedMarkable>();
		
		//ArrayList<SSpan> sSpanNodes = new ArrayList<SSpan>();
		//ArrayList<SaltExtendedMarkable> sSpanMarkables = new ArrayList<SaltExtendedMarkable>();
		
		
		ArrayList<SSpanningRelation> sSpanRelNodes = new ArrayList<SSpanningRelation>();
		ArrayList<SaltExtendedMarkable> sSpanRelMarkables = new ArrayList<SaltExtendedMarkable>();
		
		ArrayList<SDominanceRelation> sDomRelNodes = new ArrayList<SDominanceRelation>();
		ArrayList<SaltExtendedMarkable> sDomRelMarkables = new ArrayList<SaltExtendedMarkable>();
				
		ArrayList<STextualRelation> sTextRelNodes = new ArrayList<STextualRelation>();
		ArrayList<SaltExtendedMarkable> sTextRelMarkables = new ArrayList<SaltExtendedMarkable>();
		
		ArrayList<SPointingRelation> sPointerNodes = new ArrayList<SPointingRelation>();
		ArrayList<SaltExtendedMarkable> sPointerMarkables = new ArrayList<SaltExtendedMarkable>();
		
		
		ArrayList<SaltExtendedMarkable> sAnnotationMarkables = new ArrayList<SaltExtendedMarkable>();
		Hashtable<String,SAnnotatableElement> correspondanceSAnnotations = new Hashtable<String, SAnnotatableElement>();
		
		ArrayList<SaltExtendedMarkable> sMetaAnnotationMarkables = new ArrayList<SaltExtendedMarkable>();
		Hashtable<String,SMetaAnnotatableElement> correspondanceSMetaAnnotations = new Hashtable<String, SMetaAnnotatableElement>();
		
		ArrayList<SaltExtendedMarkable> markables = document.getAllSaltExtendedMarkables();
		Hashtable<Scheme,ArrayList<SaltExtendedMarkable>> newMarkables = new Hashtable<Scheme, ArrayList<SaltExtendedMarkable>>();
		for(SaltExtendedMarkable markable: markables){
			if(!markable.hasSaltInformation()){		
				ArrayList<SaltExtendedMarkable> markableOfScheme = newMarkables.get(markable.getScheme());
				if(markableOfScheme == null){
					markableOfScheme = new ArrayList<SaltExtendedMarkable>();
					newMarkables.put(markable.getScheme(), markableOfScheme);
				}
				markableOfScheme.add(markable);				
			}else{
				String sType = markable.getSType();
				String key = markable.getSId();
				
				if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT)){
					correspondanceSAnnotations.put(key, sDocument);
					correspondanceSMetaAnnotations.put(key, sDocument);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER)){
					//sLayerMarkables.add(markable);
					SLayer sLayer = createSLayer(sDocumentGraph,markable);
					//sLayerNodes.add(sLayer);
					correspondanceSAnnotations.put(key, sLayer);
					correspondanceSMetaAnnotations.put(key, sLayer);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUALDS)){
					//sTextualDSMarkables.add(markable);
					STextualDS sTextualDS = createSTextualDS(sDocumentGraph,markable,indicesTokens,totalText);
					//sTextualDSNodes.add(sTextualDS);
					this.STextualDsArray.add(sTextualDS);
					
					correspondanceSAnnotations.put(key, sTextualDS);
					correspondanceSMetaAnnotations.put(key, sTextualDS);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_STOKEN)){
				//	sTokenMarkables.add(markable);
					SToken sToken = createSToken(sDocumentGraph,markable);
				//	sTokenNodes.add(sToken);
					correspondanceSAnnotations.put(key, sToken);
					correspondanceSMetaAnnotations.put(key, sToken);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSTRUCT)){
					//sStructMarkables.add(markable);
					SStructure sStruct = createSStruct(sDocumentGraph,markable);
					//sStructNodes.add(sStruct);
					correspondanceSAnnotations.put(key, sStruct);
					correspondanceSMetaAnnotations.put(key, sStruct);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPAN)){
					//sSpanMarkables.add(markable);
					SSpan sSpan = createSSPan(sDocumentGraph,markable);
					//sSpanNodes.add(sSpan);
					correspondanceSAnnotations.put(key, sSpan);
					correspondanceSMetaAnnotations.put(key, sSpan);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUAL_REL)){
					sTextRelMarkables.add(markable);
					STextualRelation sTextualRelation = createSTextualRelation(sDocumentGraph,markable);
					sTextRelNodes.add(sTextualRelation);
					correspondanceSAnnotations.put(key, sTextualRelation);
					correspondanceSMetaAnnotations.put(key, sTextualRelation);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPANNING_REL)){
					sSpanRelMarkables.add(markable);
					SSpanningRelation sSpanningRelation = createSSpanningRelation(sDocumentGraph,markable);
					sSpanRelNodes.add(sSpanningRelation);
					correspondanceSAnnotations.put(key, sSpanningRelation);
					correspondanceSMetaAnnotations.put(key, sSpanningRelation);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOMINANCE_REL)){
					sDomRelMarkables.add(markable);
					SDominanceRelation sDomRel = createSDomRel(sDocumentGraph,markable);
					sDomRelNodes.add(sDomRel);
					correspondanceSAnnotations.put(key, sDomRel);
					correspondanceSMetaAnnotations.put(key, sDomRel);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SPOINTING_REL)){
					sPointerMarkables.add(markable);
					SPointingRelation sPointer = createSPointer(sDocumentGraph,markable);
					sPointerNodes.add(sPointer);
					correspondanceSAnnotations.put(key, sPointer);
					correspondanceSMetaAnnotations.put(key, sPointer);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SANNOTATION)){
					sAnnotationMarkables.add(markable);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SMETAANNOTATION)){
					sMetaAnnotationMarkables.add(markable);				
				}else{
					throw new MMAX2ImporterException("Developper error:Unknown type '"+sType+"'");
				}
			}
		}
		
		/* Setting up the SAnnotations and SMetaAnnotations on the nodes and edges */
		for(SaltExtendedMarkable markable: sAnnotationMarkables){
			createSAnnotation(sDocumentGraph, correspondanceSAnnotations.get(markable.getSId()), markable);
		}
	
		for(SaltExtendedMarkable markable: sMetaAnnotationMarkables){
			createSMetaAnnotation(sDocumentGraph, correspondanceSMetaAnnotations.get(markable.getSId()), markable);
		}
		
		
		/* linking all nodes and edges together */
		
//		for(int i = 0; i < sLayerNodes.size(); i++){
//			completeSLayer(sLayerNodes.get(i),sLayerMarkables.get(i));
//		}
		
//		for(int i = 0; i < sTextualDSNodes.size(); i++){
//			completeSTextualDS(sTextualDSNodes.get(i),sTextualDSMarkables.get(i));
//		}
		
//		for(int i = 0; i < sStructNodes.size(); i++){
//			completeSStruct(sStructNodes.get(i),sStructMarkables.get(i));
//		}
		
//		for(int i = 0; i < sTokenNodes.size(); i++){
//			completeSToken(sTokenNodes.get(i),sTokenMarkables.get(i));
//		}
		
//		for(int i = 0; i < sSpanNodes.size(); i++){
//			completeSSpan(sSpanNodes.get(i),sSpanMarkables.get(i),sDocumentGraph);
//		}
		
		
		for(int i = 0; i < sTextRelNodes.size(); i++){
			completeSTextualRelation(sTextRelNodes.get(i),sTextRelMarkables.get(i),indicesTokens);
		}

		for(int i = 0; i < sDomRelNodes.size(); i++){
			completeSDomRel(sDomRelNodes.get(i),sDomRelMarkables.get(i));
		}
		
		for(int i = 0; i < sSpanRelNodes.size(); i++){
			completeSPanningRelation(sSpanRelNodes.get(i),sSpanRelMarkables.get(i));
		}
		
		for(int i = 0; i < sPointerNodes.size(); i++){
			completeSPointer(sPointerNodes.get(i),sPointerMarkables.get(i));
		}
		
		
		
		
		SLayer mmaxSLayer = null;
		for(SLayer sLayer: this.SLayerHash.values()){
			if(sLayer.getSName().equals("Mmax2_imp")){
				mmaxSLayer = sLayer;
				break;
			}
		}
		if(mmaxSLayer == null){
			mmaxSLayer= SaltCommonFactory.eINSTANCE.createSLayer();
			mmaxSLayer.setSName("Mmax2_imp");
			sDocumentGraph.addSLayer(mmaxSLayer);
		}
		
		if(this.STextualDsArray.size() == 0){
			STextualDS sTextualDs = SaltCommonFactory.eINSTANCE.createSTextualDS();
			sTextualDs.setSName("Mmax2_textual_ds");
			sTextualDs.setSText(totalText);
			sTextualDs.setSDocumentGraph(sDocumentGraph);
			this.STextualDsArray.add(sTextualDs);
		}
		
		/* Creating new SSpans */
		
		
		for(Scheme scheme: newMarkables.keySet()){
			for(SaltExtendedMarkable markable: newMarkables.get(scheme)){
				SSpan sSpan = SaltCommonFactory.eINSTANCE.createSSpan();
				sSpan.setSName(markable.getScheme().getName());
				registerSNode(markable.getId(),sSpan);
				attachSNode2SLayerAndDocument(sDocumentGraph,mmaxSLayer,sSpan);
				
				String span = markable.getSpan();
				String[] spans = span.split(",");
				for(int i = 0; i < spans.length; i++){
					SSpanningRelation sSpanRel= SaltCommonFactory.eINSTANCE.createSSpanningRelation();
					sSpanRel.setSSpan(sSpan);
					
					SToken sToken = SaltCommonFactory.eINSTANCE.createSToken();
					attachSNode2SLayerAndDocument(sDocumentGraph,mmaxSLayer,sToken);
					sSpanRel.setSToken(sToken);
					
					attachSRelation2SLayerAndDocument(sDocumentGraph,mmaxSLayer,sSpanRel);
					
					STextualRelation sTextualRel = SaltCommonFactory.eINSTANCE.createSTextualRelation();
					sTextualRel.setSToken(sToken);
					
					int[] startAndEnd = getStartAndEnd(spans[i], indicesTokens);
					sTextualRel.setSStart(startAndEnd[0]);
					sTextualRel.setSEnd(startAndEnd[1]);
					
					ArrayList<STextualDS> sTextualDSArr = new ArrayList<STextualDS>();
					for(STextualDS sTextualDS: this.STextualDsArray){
						if((sTextualDS.getSStart() <= startAndEnd[0]) && (sTextualDS.getSEnd() >= startAndEnd[1])){
							sTextualDSArr.add(sTextualDS);
						}
					}
					
					if(sTextualDSArr.size() == 0){
						dataCorrupted("No TextualDS covers token in span '"+spans[i]+"'");
					}else if(sTextualDSArr.size() > 1){
						dataCorrupted("More than one TextualDS covers token in span '"+spans[i]+"'... Namely "+sTextualDSArr);
					}else{
						sTextualRel.setSTextualDS(sTextualDSArr.get(0));
					}
					
					attachSRelation2SLayerAndDocument(sDocumentGraph,mmaxSLayer,sTextualRel);
				}
			}
		}
		
		for(Scheme scheme: newMarkables.keySet()){
			for(SaltExtendedMarkable markable: newMarkables.get(scheme)){
				SSpan sSpan = (SSpan) getSNode(markable.getId());
				
				for(MarkableAttribute markableAttribute: markable.getAttributes()){
					String attributeType = markableAttribute.getFactory().getAttributeType();
					
					if(attributeType.equals(MarkableFreetextAttributeFactory.freetextType) 
							||attributeType.equals(MarkableNominalAttributeFactory.nominalType) 
							||attributeType.equals(MarkableSetAttributeFactory.setType)){
						SAnnotation sAnnotation = SaltCommonFactory.eINSTANCE.createSAnnotation();
						sAnnotation.setSName(markableAttribute.getName());
						sAnnotation.setSValue(markableAttribute.getValue());
						sSpan.addSAnnotation(sAnnotation);
					}else if(attributeType.equals(MarkablePointerAttributeFactory.pointerType)){
						SPointingRelation sPointingRelation = SaltCommonFactory.eINSTANCE.createSPointingRelation();
						sPointingRelation.setSName(markableAttribute.getName());
						sPointingRelation.setSSource(sSpan);
						sPointingRelation.addSType(markableAttribute.getName());
						
						SNode sTarget = getSNode(markableAttribute.getValue());
						if(sTarget == null)
							dataCorrupted("An unknown markable '"+markableAttribute.getValue()+"' is referenced as the target of the pointer '"+markableAttribute.getName()+"' within markable '"+markable+"'");
						sPointingRelation.setSTarget(sTarget);	
					}else{
						throw new MMAX2ImporterException("Developper error: unknown type of markable attribute '"+attributeType+"'...");
					}		
				}
			}
		}
		
	}

	private SLayer createSLayer(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SLayer sLayer = SaltCommonFactory.eINSTANCE.createSLayer();
		sLayer.setSName(markable.getSName());
		this.SLayerHash.put(markable.getSLayerId(),sLayer);	
		return sLayer;
	}
	
//	private void completeSLayer(SLayer sLayer, SaltExtendedMarkable markable){}
	
	
	private STextualDS createSTextualDS(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable, Hashtable<String,int[]> indicesTokens, String totalText){
		STextualDS sTextualDS = SaltCommonFactory.eINSTANCE.createSTextualDS();
		sTextualDS.setSName(markable.getSName());
		int[] startAndEnd = getStartAndEnd(markable.getSpan(), indicesTokens);
		sTextualDS.setSText(totalText.substring(startAndEnd[0], startAndEnd[1]));
		registerSNode(markable.getId(),sTextualDS);
		attachSNode2SLayerAndDocument(sDocumentGraph, getSLayer(sDocumentGraph, markable.getSLayerId()),sTextualDS);		
		return sTextualDS;
	}
	
//	private void completeSTextualDS(STextualDS sTextualDS, SaltExtendedMarkable markable){}
	
		
	private SStructure createSStruct(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SStructure sStruct = SaltCommonFactory.eINSTANCE.createSStructure();
		sStruct.setSName(markable.getSName());
		registerSNode(markable.getId(),sStruct);
		attachSNode2SLayerAndDocument(sDocumentGraph, getSLayer(sDocumentGraph, markable.getSLayerId()),sStruct);		
		return sStruct;
	}
	
//	private void completeSStruct(SStructure sStruct, SaltExtendedMarkable markable){}
	

	private SToken createSToken(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SToken sToken = SaltCommonFactory.eINSTANCE.createSToken();
		sToken.setSName(markable.getSName());
		registerSNode(markable.getId(),sToken);
		attachSNode2SLayerAndDocument(sDocumentGraph, getSLayer(sDocumentGraph, markable.getSLayerId()),sToken);	
		return sToken;
	}
	
//	private void completeSToken(SToken sToken, SaltExtendedMarkable markable){}
	
	private SSpan createSSPan(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SSpan sSpan = SaltCommonFactory.eINSTANCE.createSSpan();
		sSpan.setSName(markable.getSName());
		registerSNode(markable.getId(),sSpan);
		attachSNode2SLayerAndDocument(sDocumentGraph, getSLayer(sDocumentGraph, markable.getSLayerId()),sSpan);
		return sSpan;
	}
	
//	private void completeSSpan( SSpan sSpan, SaltExtendedMarkable markable, SDocumentGraph sDocumentGraph){}
	
	private STextualRelation createSTextualRelation(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		STextualRelation sTextualRel = SaltCommonFactory.eINSTANCE.createSTextualRelation();
		sTextualRel.setSName(markable.getSName());
		attachSRelation2SLayerAndDocument(sDocumentGraph, getSLayer(sDocumentGraph,markable.getSLayerId()),sTextualRel);
		return sTextualRel;
	}
	
	private void completeSTextualRelation(STextualRelation sTextualRelation, SaltExtendedMarkable markable, Hashtable<String,int[]> indicesTokens){
		MarkableAttribute targetTokenAttribute = null;	
		MarkableAttribute targetTextualDsAttribute = null;	
		
		ArrayList<MarkableAttribute> markableAttributes = markable.getAttributes();
		for(MarkableAttribute markableAttribute : markableAttributes){
			if(markableAttribute.getName().equals("target_token")){
				targetTokenAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("target_textual_ds")){
				targetTextualDsAttribute = markableAttribute;
			}
		}
		
		if(targetTokenAttribute == null)
			dataCorrupted("'target_token' attribute is missing on Saltextended markable '"+markable+"' representing an STextualRelation");
		markable.removeAttribute(targetTokenAttribute);
		
		if(targetTextualDsAttribute == null)
			dataCorrupted("'target_textual_ds' attribute is missing on Saltextended markable '"+markable+"' representing an STextualRelation");
		markable.removeAttribute(targetTextualDsAttribute);
		
		SToken sToken = (SToken) getSNode(targetTokenAttribute.getValue());
		if(sToken == null)
			dataCorrupted("An unknown SToken node '"+targetTokenAttribute.getValue()+"' is referenced in the Saltextended markable '"+markable+"' representing an STextualRelation");
		sTextualRelation.setSToken(sToken);
		
		STextualDS sTextualDs = (STextualDS) getSNode(targetTextualDsAttribute.getValue());
		if(sTextualDs == null)
			dataCorrupted("An unknown STextualDS node '"+targetTextualDsAttribute.getValue()+"' is referenced in the Saltextended markable '"+markable+"' representing an STextualRelation");
		sTextualRelation.setSTextualDS(sTextualDs);
		
		int[] startAndEnd = getStartAndEnd(markable.getSpan(), indicesTokens);
		sTextualRelation.setSStart(startAndEnd[0]);
		sTextualRelation.setSEnd(startAndEnd[1]);
	}
	
	
	private SDominanceRelation createSDomRel(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SDominanceRelation sDomRel = SaltCommonFactory.eINSTANCE.createSDominanceRelation();
		sDomRel.setSName(markable.getSName());
		attachSRelation2SLayerAndDocument(sDocumentGraph,getSLayer(sDocumentGraph,markable.getSLayerId()),sDomRel);
		return sDomRel;
	}
	
	private void completeSDomRel(SDominanceRelation sDomRel, SaltExtendedMarkable markable){
		MarkableAttribute structAttribute = null;
		MarkableAttribute targetAttribute = null;	
		MarkableAttribute typeAttribute = null;	
		ArrayList<MarkableAttribute> markableAttributes = markable.getAttributes();
		for(MarkableAttribute markableAttribute : markableAttributes){
			if(markableAttribute.getName().equals("struct")){
				structAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("target")){
				targetAttribute = markableAttribute;
			}if (markableAttribute.getName().equals("type")){
				typeAttribute = markableAttribute;
			}
		}
		
		if(structAttribute == null)
			dataCorrupted("'struct' attribute is missing on Saltextended markable '"+markable+"' representing an SDominationRelation");
		markable.removeAttribute(structAttribute);
		
		if(targetAttribute == null)
			dataCorrupted("'target' attribute is missing on Saltextended markable '"+markable+"' representing an SDominationRelation");
		markable.removeAttribute(targetAttribute);
		
		if(typeAttribute == null)
			dataCorrupted("'type' attribute is missing on Saltextended markable '"+markable+"' representing an SDominationRelation");
		markable.removeAttribute(typeAttribute);
		
		SStructure sStruct = (SStructure) getSNode(structAttribute.getValue());
		if(sStruct == null)
			dataCorrupted("An unknown SStructure '"+structAttribute.getValue()+"' is referenced as the source for the SDominanceRelation represented by markable '"+markable+"'");
		sDomRel.setSStructure(sStruct);
		
		SStructure sStructuredTarget = (SStructure) getSNode(targetAttribute.getValue());
		if(sStructuredTarget == null)
			dataCorrupted("An unknown SStructured target '"+targetAttribute.getValue()+"' is referenced as the target for the SDominanceRelation represented by markable '"+markable+"'");	
		sDomRel.setSStructuredTarget(sStructuredTarget);	
		
		if(!typeAttribute.getValue().equals("@none@")){
			String[] types = typeAttribute.getValue().split("<->");
			for(String type: types){
				sDomRel.addSType(type);	
			}
		}
	}
	
	private SSpanningRelation createSSpanningRelation(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SSpanningRelation sSpanningRel = SaltCommonFactory.eINSTANCE.createSSpanningRelation();
		sSpanningRel.setSName(markable.getSName());
		attachSRelation2SLayerAndDocument(sDocumentGraph,getSLayer(sDocumentGraph,markable.getSLayerId()),sSpanningRel);
		return sSpanningRel;
	}
	
	private void completeSPanningRelation(SSpanningRelation sSpanningRel, SaltExtendedMarkable markable){
		MarkableAttribute targetTokenAttribute = null;	
		MarkableAttribute sourceSpanAttribute = null;	
		ArrayList<MarkableAttribute> markableAttributes = markable.getAttributes();
		for(MarkableAttribute markableAttribute : markableAttributes){
			if(markableAttribute.getName().equals("target_token")){
				targetTokenAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("source_span")){
				sourceSpanAttribute = markableAttribute;
			}
		}
		
		if(targetTokenAttribute == null)
			dataCorrupted("'target_token' attribute is missing on Saltextended markable '"+markable+"' representing an SPanningRelation");
		markable.removeAttribute(targetTokenAttribute);
		
		if(sourceSpanAttribute == null)
			dataCorrupted("'source_span' attribute is missing on Saltextended markable '"+markable+"' representing an SPanningRelation");
		markable.removeAttribute(sourceSpanAttribute);
		
		SToken sToken = (SToken) getSNode(targetTokenAttribute.getValue());
		if(sToken == null)
			dataCorrupted("An unknown SToken node '"+targetTokenAttribute.getValue()+"' is referenced as the target for the SPanningRelation represented by markable '"+markable+"'");
		
		SSpan sSpan = (SSpan) getSNode(sourceSpanAttribute.getValue());
		if(sSpan == null)
			dataCorrupted("An unknown SToken node '"+sourceSpanAttribute.getValue()+"' is referenced as the target for the SPanningRelation represented by markable '"+markable+"'");

		sSpanningRel.setSToken(sToken);
		sSpanningRel.setSSpan(sSpan);
	}
	
	private SPointingRelation createSPointer(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SPointingRelation sPointingRelation = SaltCommonFactory.eINSTANCE.createSPointingRelation();		
		sPointingRelation.setSName(markable.getSName());
		attachSRelation2SLayerAndDocument(sDocumentGraph,getSLayer(sDocumentGraph,markable.getSLayerId()),sPointingRelation);
		return sPointingRelation;
	}
	
	private void completeSPointer(SPointingRelation sPointingRelation, SaltExtendedMarkable markable){
		MarkableAttribute sourceAttribute = null;
		MarkableAttribute targetAttribute = null;	
		MarkableAttribute typeAttribute = null;	
		ArrayList<MarkableAttribute> markableAttributes = markable.getAttributes();
		for(MarkableAttribute markableAttribute : markableAttributes){
			if(markableAttribute.getName().equals("source")){
				sourceAttribute = markableAttribute;
				markableAttributes.remove(markableAttribute);
			}else if(markableAttribute.getName().equals("target")){
				targetAttribute = markableAttribute;
				markableAttributes.remove(markableAttribute);
			}else if (markableAttribute.getName().equals("type")){
				typeAttribute = markableAttribute;
				markableAttributes.remove(typeAttribute);
			}
		}
		
		if(sourceAttribute == null)
			dataCorrupted("'source' attribute is missing on Saltextended markable '"+markable+"' representing an SPointingRelation");
		markableAttributes.remove(sourceAttribute);
		
		if(targetAttribute == null)
			dataCorrupted("'target' attribute is missing on Saltextended markable '"+markable+"' representing an SPointingRelation");
		markableAttributes.remove(targetAttribute);
		
		if(typeAttribute == null)
			dataCorrupted("'type' attribute is missing on Saltextended markable '"+markable+"' representing an SPointingRelation");
		markableAttributes.remove(typeAttribute);
		
		
		SNode sSource = getSNode(sourceAttribute.getValue());
		if(sSource == null)
			dataCorrupted("An unknown source node '"+sourceAttribute.getValue()+"' is referenced as the source for the SPointingRelation represented by markable '"+markable+"'");
		sPointingRelation.setSSource(sSource);
		
		SNode sTarget = getSNode(targetAttribute.getValue());
		if(sTarget == null)
			dataCorrupted("An unknown target node '"+targetAttribute.getValue()+"' is referenced as the target for the SPointingRelation represented by markable '"+markable+"'");	
		sPointingRelation.setSTarget(sTarget);	
		
		if(!typeAttribute.getValue().equals("@none@")){
			String[] types = typeAttribute.getValue().split("<->");
			for(String type: types){
				sPointingRelation.addSType(type);	
			}
		}
	}
	
	
	private void createSMetaAnnotation(SDocumentGraph sDocumentGraph, SMetaAnnotatableElement sAnnotatableElement, SaltExtendedMarkable markable) {
		MarkableAttribute namespaceAttribute = null;
		MarkableAttribute targetMarkableAttribute = null;
		MarkableAttribute nameAttribute = null;
		MarkableAttribute valueAttribute = null;
		
		ArrayList<MarkableAttribute> markableAttributes = markable.getAttributes();
		for(MarkableAttribute markableAttribute : markableAttributes){
			String attributeName = markableAttribute.getName();
			if(attributeName.equals("namespace")){
				namespaceAttribute = markableAttribute;
			}else if(!attributeName.equals("target_markable")){
				targetMarkableAttribute = markableAttribute;
			}else if(!attributeName.equals("attr_name")){
				nameAttribute = markableAttribute;
			}else if(!attributeName.equals("attr_value")){
				valueAttribute = markableAttribute;
			}
		}
		
		if(namespaceAttribute == null)
			throw new MMAX2ImporterException("Data is corrupted => 'namespace' attribute is missing on Saltextended markable '"+markable+"' representing an SMetaAnnotation");
		markable.removeAttribute(namespaceAttribute);
		
		if(targetMarkableAttribute == null)
			dataCorrupted("'target_markable' attribute is missing on Saltextended markable '"+markable+"' representing an SAnnotation");
		markable.removeAttribute(targetMarkableAttribute);
		
		if(nameAttribute == null)
			dataCorrupted("'attr_name' attribute is missing on Saltextended markable '"+markable+"' representing an SAnnotation");
		markable.removeAttribute(nameAttribute);
		
		if(valueAttribute == null)
			dataCorrupted("'attr_value' attribute is missing on Saltextended markable '"+markable+"' representing an SAnnotation");
		markable.removeAttribute(valueAttribute);
		
		SMetaAnnotation sMetaAnnotation = SaltCommonFactory.eINSTANCE.createSMetaAnnotation();
		if(!namespaceAttribute.getValue().equals(""))
			sMetaAnnotation.setSNS(namespaceAttribute.getValue());
		sMetaAnnotation.setSName(nameAttribute.getValue());
		sMetaAnnotation.setSValue(valueAttribute.getValue());
		
		sAnnotatableElement.addSMetaAnnotation(sMetaAnnotation);	
	}
	
	private void createSAnnotation(SDocumentGraph sDocumentGraph, SAnnotatableElement sAnnotatableElement, SaltExtendedMarkable markable) {
		MarkableAttribute namespaceAttribute = null;
		MarkableAttribute targetMarkableAttribute = null;
		MarkableAttribute nameAttribute = null;
		MarkableAttribute valueAttribute = null;
		
		ArrayList<MarkableAttribute> markableAttributes = markable.getAttributes();
		for(MarkableAttribute markableAttribute: markableAttributes){
			String attributeName = markableAttribute.getName();
			if(attributeName.equals("namespace")){
				namespaceAttribute = markableAttribute;
			}else if(!attributeName.equals("target_markable")){
				targetMarkableAttribute = markableAttribute;
			}else if(!attributeName.equals("attr_name")){
				nameAttribute = markableAttribute;
			}else if(!attributeName.equals("attr_value")){
				valueAttribute = markableAttribute;
			}
		}
		
		if(namespaceAttribute == null)
			dataCorrupted("'namespace' attribute is missing on Saltextended markable '"+markable+"' representing an SAnnotation");
		markable.removeAttribute(namespaceAttribute);
		
		if(targetMarkableAttribute == null)
			dataCorrupted("'target_markable' attribute is missing on Saltextended markable '"+markable+"' representing an SAnnotation");
		markable.removeAttribute(targetMarkableAttribute);
		
		if(nameAttribute == null)
			dataCorrupted("'attr_name' attribute is missing on Saltextended markable '"+markable+"' representing an SAnnotation");
		markable.removeAttribute(nameAttribute);
		
		if(valueAttribute == null)
			dataCorrupted("'attr_value' attribute is missing on Saltextended markable '"+markable+"' representing an SAnnotation");
		markable.removeAttribute(valueAttribute);
		
		
		SAnnotation sAnnotation = SaltCommonFactory.eINSTANCE.createSAnnotation();
		if(!namespaceAttribute.getValue().equals(""))
			sAnnotation.setSNS(namespaceAttribute.getValue());
		sAnnotation.setSName(nameAttribute.getValue());
		sAnnotation.setSValue(valueAttribute.getValue());
		sAnnotatableElement.addSAnnotation(sAnnotation);
	}
	
	private int[] getStartAndEnd (String span, Hashtable<String,int[]> indicesTokens){
		String[] tokensIds = span.split(java.util.regex.Pattern.quote(".."));
		
		String startToken;
		String endToken;
		if(tokensIds.length < 2){
			startToken = tokensIds[0];
			endToken = tokensIds[0];
		}else{
			startToken = tokensIds[0];
			endToken = tokensIds[1];
		}
		int[] result = {(new Integer(indicesTokens.get(startToken)[0])).intValue(),(new Integer(indicesTokens.get(endToken)[1])).intValue()};
		
		return result;		
	}
	
	private void attachSNode2SLayerAndDocument(SDocumentGraph sDocumentGraph, SLayer sLayer, SNode sNode){
		sDocumentGraph.addSNode(sNode);	
		if(sLayer != null){
			sLayer.getSNodes().add(sNode);
		}
	}
	
	private void attachSRelation2SLayerAndDocument(SDocumentGraph sDocumentGraph, SLayer sLayer, SRelation edge){
		sDocumentGraph.addSRelation(edge);	
		if(sLayer != null){
			sLayer.getEdges().add(edge);
		}
	}
	
	
	private SLayer getSLayer(SDocumentGraph sDocumentGraph, String sLayerId){
		if(sLayerId.equals("nolayer")){
			return null;
		}

		SLayer sLayer = this.SLayerHash.get(sLayerId);
		return sLayer;
	}
	
	private void registerSNode (String key, SNode sNode){
		SNodesHash.put(key,sNode);		
	}
	
	private SNode getSNode(String key){
		return SNodesHash.get(key);
	}
	
	
	private void dataCorrupted(String message){
		throw new MMAX2ImporterException("Data is corrupted => "+message);
	}
	
}
