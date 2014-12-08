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

import org.apache.commons.lang3.StringUtils;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleDataException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedDocumentFactory.SaltExtendedDocument;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkable;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedMarkableFactory.SaltExtendedMarkableContainer;
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
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SIdentifiableElement;
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
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;

/**
 * This class exports data in MMAX2 format to Salt.
 * The code has been initially adapted from PAULA2SAltMapper.
 * 
 * @author Lionel Nicolas
 * 
 */

public class MMAX22SaltMapper extends PepperMapperImpl
{	
	private Hashtable<SaltExtendedMarkable,SNode> sNodesHash;
	private Hashtable<SaltExtendedMarkable,SRelation> sRelationsHash;
	private Hashtable<String,SLayer> sLayerHash;
	private Hashtable<STextualDS,Integer> sTextualDsOfset;
	private Hashtable<String,SToken> sTokensHash;
	private Hashtable<String,Hashtable<String,SaltExtendedMarkable>> saltExtendedMarkableHash;
	private Hashtable<String,STextualDS> sTextualDsBaseDataUnitCorrespondance;
	private Hashtable<SaltExtendedMarkable,SaltExtendedMarkable> claimSContainer;
	private Hashtable<String,SIdentifiableElement> saltIds;
	private Hashtable<String,Integer> saltIdsCpt;
	
	/** A mmax2 document **/
	private SaltExtendedDocument document= null;
	/** Returns the mmax2 document**/
	public SaltExtendedDocument getDocument() {
		return document;
	}
	/** Sets the mmax2 document**/
	public void setDocument(SaltExtendedDocument document) {
		this.document = document;
	}
	
	@Override
	public DOCUMENT_STATUS mapSCorpus() {
		return(DOCUMENT_STATUS.COMPLETED);
	}

	@Override
	public DOCUMENT_STATUS mapSDocument() {
		if (getSDocument().getSDocumentGraph()== null)
			getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		if (getDocument()== null)
			throw new PepperModuleException(this, "Cannot start mapping, because no mmax2 document was given.");
		
		mapSDocument(getDocument(), getSDocument());
		return(DOCUMENT_STATUS.COMPLETED);
	}
	
	/**
	 * Maps a {@link SaltExtendedDocument} document to an {@link SDocument} sDocument
	 * @param document The {@link SaltExtendedDocument} document to map
	 * @param sDocument the {@link SDocument} to which the data is being mapped to 
	 */
	public void mapSDocument(SaltExtendedDocument document,SDocument sDocument){
		this.sNodesHash = new Hashtable<SaltExtendedMarkable, SNode>();
		this.sRelationsHash = new Hashtable<SaltExtendedMarkable, SRelation>();
		this.sLayerHash = new Hashtable<String, SLayer>();
		this.sTextualDsOfset = new Hashtable<STextualDS, Integer>();
		this.sTokensHash = new Hashtable<String, SToken>();
		this.saltExtendedMarkableHash = new Hashtable<String,Hashtable<String,SaltExtendedMarkable>>();
		this.sTextualDsBaseDataUnitCorrespondance = new Hashtable<String, STextualDS>();
		this.claimSContainer = new Hashtable<SaltExtendedMarkable, SaltExtendedMarkable>();
		this.saltIds = new Hashtable<String,SIdentifiableElement>();
		this.saltIdsCpt = new Hashtable<String, Integer>();
	
		SDocumentGraph sDocumentGraph = sDocument.getSDocumentGraph();
		sDocumentGraph.setSName(document.getDocumentId()+"_graph");
		
		ArrayList<SaltExtendedMarkable> markables = document.getAllSaltExtendedMarkables();
		Hashtable<String,SaltExtendedMarkable> baseDataUnitInTextualDS = new Hashtable<String, SaltExtendedMarkable>();
		for(SaltExtendedMarkable markable: markables){
			if(markable.hasSaltInformation() && (markable.getSType().equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUALDS))){
				String[] markableSpans = markable.getSpan().split(",");
				for(int i = 0; i < markableSpans.length; i++){
					ArrayList<String> baseDataUnits = getBaseUnitIds(markableSpans[i]);
					for(String baseDataUnit: baseDataUnits){
						if(baseDataUnitInTextualDS.containsKey(baseDataUnit)){
							throw new PepperModuleDataException(this, "Two textualDS covers one same basedata unit: markables '"+markable.getId()
											+"' and '"+baseDataUnitInTextualDS.get(baseDataUnit).getId()+"' both covers '"+baseDataUnit+"'");
						}else{
							baseDataUnitInTextualDS.put(baseDataUnit,markable);
						}
					}
				}
				
			}
		}
		
		Hashtable<String,int[]> indicesTokens = new Hashtable<String, int[]>();
		Hashtable<SaltExtendedMarkable,ArrayList<BaseDataUnit>> sTextualDSBaseDataUnits = new Hashtable<SaltExtendedMarkable, ArrayList<BaseDataUnit>>();
		SaltExtendedMarkable lastTextualDsMarkable = null;
		ArrayList<BaseDataUnit> bufferBaseDataUnit = new ArrayList<BaseDataUnit>(); 
		{
			int indice = 0;
			Hashtable<SaltExtendedMarkable,String> previouslySeenTextualDs = new Hashtable<SaltExtendedMarkable, String>();
			for(BaseDataUnit baseDataUnit: document.getBaseDataUnits()){
				int newIndice = indice + baseDataUnit.getText().length();
				int[] indices = {indice,newIndice};
				indicesTokens.put(baseDataUnit.getId(),indices);
				indice = newIndice;
				
				bufferBaseDataUnit.add(baseDataUnit);
				if(baseDataUnitInTextualDS.containsKey(baseDataUnit.getId())){
					SaltExtendedMarkable textualDsMarkable =  baseDataUnitInTextualDS.get(baseDataUnit.getId());
					if((textualDsMarkable != lastTextualDsMarkable) && (previouslySeenTextualDs.containsKey(textualDsMarkable))){
						throw new PepperModuleDataException(this, "The spans of textualDs markables '"+textualDsMarkable.getId()+"' and '"+lastTextualDsMarkable+"' overlap one another.");
					}
					
					lastTextualDsMarkable = textualDsMarkable;
					previouslySeenTextualDs.put(lastTextualDsMarkable,"");
					ArrayList<BaseDataUnit> localBaseDataUnits = sTextualDSBaseDataUnits.get(lastTextualDsMarkable);
					if(localBaseDataUnits == null){
						localBaseDataUnits = new ArrayList<BaseDataUnit>();
						sTextualDSBaseDataUnits.put(lastTextualDsMarkable,localBaseDataUnits);
					} 
					localBaseDataUnits.addAll(bufferBaseDataUnit);
					bufferBaseDataUnit = new ArrayList<BaseDataUnit>();
				}
			}
		}
		
		if(bufferBaseDataUnit.size() != 0){
			if(lastTextualDsMarkable != null){
				sTextualDSBaseDataUnits.get(lastTextualDsMarkable).addAll(bufferBaseDataUnit);
			}else{
				createSTextualDS(sDocumentGraph,null,bufferBaseDataUnit,indicesTokens);				
			}
		}
		
		ArrayList<SSpanningRelation> sSpanRelNodes = new ArrayList<SSpanningRelation>();
		ArrayList<SaltExtendedMarkable> sSpanRelMarkables = new ArrayList<SaltExtendedMarkable>();
		
		ArrayList<SDominanceRelation> sDomRelNodes = new ArrayList<SDominanceRelation>();
		ArrayList<SaltExtendedMarkable> sDomRelMarkables = new ArrayList<SaltExtendedMarkable>();
				
		ArrayList<STextualRelation> sTextRelNodes = new ArrayList<STextualRelation>();
		ArrayList<SaltExtendedMarkable> sTextRelMarkables = new ArrayList<SaltExtendedMarkable>();
		
		ArrayList<SPointingRelation> sPointerNodes = new ArrayList<SPointingRelation>();
		ArrayList<SaltExtendedMarkable> sPointerMarkables = new ArrayList<SaltExtendedMarkable>();
		
		ArrayList<SaltExtendedMarkable> sContainerMarkables = new ArrayList<SaltExtendedMarkable>();
		
		ArrayList<SaltExtendedMarkable> sAnnotationMarkables = new ArrayList<SaltExtendedMarkable>();
		Hashtable<String,SAnnotatableElement> correspondanceSAnnotations = new Hashtable<String, SAnnotatableElement>();
		
		ArrayList<SaltExtendedMarkable> sMetaAnnotationMarkables = new ArrayList<SaltExtendedMarkable>();
		Hashtable<String,SMetaAnnotatableElement> correspondanceSMetaAnnotations = new Hashtable<String, SMetaAnnotatableElement>();
		
		ArrayList<SaltExtendedMarkable> sLayerLinkMarkables = new ArrayList<SaltExtendedMarkable>();
		ArrayList<SaltExtendedMarkable> sTypeLinkMarkables = new ArrayList<SaltExtendedMarkable>();
			
		Hashtable<Scheme,ArrayList<SaltExtendedMarkable>> newMarkables = new Hashtable<Scheme, ArrayList<SaltExtendedMarkable>>();
		SaltExtendedMarkable sDocumentMarkable = null;
		SaltExtendedMarkable sDocumentGraphMarkable = null;
		for(SaltExtendedMarkable markable: markables){
			registerMarkable(markable);
			if(!markable.hasSaltInformation()){	// new markable originally produced with Mmax2	
				ArrayList<SaltExtendedMarkable> markableOfScheme = newMarkables.get(markable.getFactory().getScheme());
				if(markableOfScheme == null){
					markableOfScheme = new ArrayList<SaltExtendedMarkable>();
					newMarkables.put(markable.getFactory().getScheme(), markableOfScheme);
				}
				markableOfScheme.add(markable);				
			}else{ // markables originally produced (exported) from SAlt
				String sType = markable.getSType();
				String key = markable.getSId();
				
				if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT)){
					if(sDocumentMarkable != null){
						throw new PepperModuleDataException(this, "Two SDocument markable have been found: markables '"+markable.getId()+"' and '"+sDocumentMarkable.getId()+"'");
					}
					sDocumentMarkable = markable;
					correspondanceSAnnotations.put(key, sDocument);
					correspondanceSMetaAnnotations.put(key, sDocument);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SDOCUMENT_GRAPH)){
					if(sDocumentGraphMarkable != null){
						throw new PepperModuleDataException(this, "Two SDocumentGraph markable have been found: markables '"+markable.getId()+"' and '"+sDocumentGraphMarkable.getId()+"'");
					}
					sDocumentGraphMarkable = markable;
					correspondanceSAnnotations.put(key, sDocumentGraph);
					correspondanceSMetaAnnotations.put(key, sDocumentGraph);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER)){
					SLayer sLayer = createSLayer(sDocumentGraph,markable);
					correspondanceSAnnotations.put(key, sLayer);
					correspondanceSMetaAnnotations.put(key, sLayer);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_STEXTUALDS)){
					STextualDS sTextualDS = createSTextualDS(sDocumentGraph,markable,sTextualDSBaseDataUnits.get(markable),indicesTokens);
					correspondanceSAnnotations.put(key, sTextualDS);
					correspondanceSMetaAnnotations.put(key, sTextualDS);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_STOKEN)){
					SToken sToken = createSToken(sDocumentGraph,markable);
					correspondanceSAnnotations.put(key, sToken);
					correspondanceSMetaAnnotations.put(key, sToken);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSTRUCT)){
					SStructure sStruct = createSStruct(sDocumentGraph,markable);
					correspondanceSAnnotations.put(key, sStruct);
					correspondanceSMetaAnnotations.put(key, sStruct);
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSPAN)){
					SSpan sSpan = createSSPan(sDocumentGraph,markable);
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
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SLAYER_LINK)){
					sLayerLinkMarkables.add(markable);				
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_STYPE_LINK)){
					sTypeLinkMarkables.add(markable);				
				}else if(sType.equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER)){
					sContainerMarkables.add(markable);		
				}else{
					throw new PepperModuleException("Developper error:Unknown type '"+sType+"'");
				}
			}
		}
		
		if(sDocumentMarkable != null){
			sDocument.setSName(sDocumentMarkable.getSName());
		}
		
		if(sDocumentGraphMarkable != null){
			sDocumentGraph.setSName(sDocumentGraphMarkable.getSName());
		}
		
		for(SaltExtendedMarkable markable: sContainerMarkables){
			handleSContainer(markable);
		}
		
		/* Setting up the SAnnotations and SMetaAnnotations on the nodes and edges */
		for(SaltExtendedMarkable markable: sAnnotationMarkables){
			createSAnnotation(sDocumentGraph, correspondanceSAnnotations.get(markable.getSId()),markable);
		}
	
		for(SaltExtendedMarkable markable: sMetaAnnotationMarkables){
			createSMetaAnnotation(sDocumentGraph, correspondanceSMetaAnnotations.get(markable.getSId()), markable);
		}
		
		for(SaltExtendedMarkable markable: sLayerLinkMarkables){
			createSLayerLink(sDocumentGraph,markable);
		}
		
		
		/* Creating new SSpans */
		
		SLayer mmaxSLayer = null;
		if(newMarkables.keySet().size() != 0){ // => means "new Markables created since export from salt"
			for(SLayer sLayer: this.sLayerHash.values()){
				if(sLayer.getSName().equals("Mmax2_SLayer")){
					mmaxSLayer = sLayer;
					break;
				}
			}
			if(mmaxSLayer == null){
				mmaxSLayer= SaltFactory.eINSTANCE.createSLayer();
				mmaxSLayer.setSName("Mmax2_SLayer");
				mmaxSLayer.setSId("Mmax2_SLayer");
				sDocumentGraph.addSLayer(mmaxSLayer);
			}
		
			for(Scheme scheme: newMarkables.keySet()){
				String schemeName = scheme.getName();
				for(SaltExtendedMarkable markable: newMarkables.get(scheme)){
					SSpan sSpan = SaltFactory.eINSTANCE.createSSpan();
					sSpan.setSName(schemeName);
					sDocumentGraph.addSNode(sSpan);
					sSpan.setSId(getNewSid(schemeName));
					registerSNode(markable,sSpan);
					
					SAnnotation sAnnotation = SaltFactory.eINSTANCE.createSAnnotation();
					sAnnotation.setSNS("Mmax2");
					sAnnotation.setSName("markable_scheme");
					sAnnotation.setSValue(schemeName);
					sSpan.addSAnnotation(sAnnotation);
	
					sSpan.getSLayers().add(mmaxSLayer);
					sSpan.getSLayers().add(mmaxSLayer);
					
					String span = markable.getSpan();
					String[] spans = span.split(",");
					for(int i = 0; i < spans.length; i++){
						ArrayList<String> baseDateUnitIds = getBaseUnitIds(spans[i]);
						
						for(String baseDataUnitId: baseDateUnitIds){
							SSpanningRelation sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
							sDocumentGraph.addSRelation(sSpanRel);
							sSpanRel.getSLayers().add(mmaxSLayer);
							sSpanRel.getSLayers().add(mmaxSLayer);
							sSpanRel.setSSpan(sSpan);
							sSpanRel.setSToken(getSToken(baseDataUnitId, indicesTokens));
						}
					}
				}
			}
		}
		
		/* linking all nodes and edges together */
		
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
		
		for(SaltExtendedMarkable markable: sTypeLinkMarkables){
			createSTypeLink(markable);
		}
		
		/* handling all attributes on newly produced (i.e non-exported) markables */
		
		if(newMarkables.keySet().size() != 0){ // => means "new Markables created since export from salt"
			for(Scheme scheme: newMarkables.keySet()){
				for(SaltExtendedMarkable markable: newMarkables.get(scheme)){
					SSpan sSpan = (SSpan) getSNode(markable);
					
					for(MarkableAttribute markableAttribute: markable.getAttributes()){
						String attributeType = markableAttribute.getFactory().getAttributeType();
						
						if(attributeType.equals(MarkableFreetextAttributeFactory.freetextType) 
								||attributeType.equals(MarkableNominalAttributeFactory.nominalType) 
								||attributeType.equals(MarkableSetAttributeFactory.setType)){
							SAnnotation sAnnotation = SaltFactory.eINSTANCE.createSAnnotation();
							sAnnotation.setSName(markableAttribute.getName());
							sAnnotation.setSNS("Mmax2");
							
							String value = markableAttribute.getValue();
							value = value.replaceAll("\n", "");
							sAnnotation.setSValue(value);
							sSpan.addSAnnotation(sAnnotation);
						}else if(attributeType.equals(MarkablePointerAttributeFactory.pointerType)){ 
							SPointingRelation sPointingRelation = SaltFactory.eINSTANCE.createSPointingRelation();
							sPointingRelation.setSName(markableAttribute.getName());
							
							sDocumentGraph.addSRelation(sPointingRelation);
							sPointingRelation.addSType(markableAttribute.getName());
							
							sPointingRelation.setSSource(sSpan);
							MarkablePointerAttributeFactory factory = (MarkablePointerAttributeFactory) markableAttribute.getFactory();
							
							SaltExtendedMarkable targetMarkable = getMarkable(markableAttribute.getValue(), factory.getTargetSchemeName());
							if(targetMarkable == null)
								throw new PepperModuleDataException(this, "An unknown markable of id '"+markableAttribute.getValue()+"' belonging to scheme '"+factory.getTargetSchemeName()
										+"' is referenced as the target of the pointer '"+markableAttribute.getName()+"' within markable '"+markable+"'");
							SNode sTarget = getSNode(targetMarkable);
							sPointingRelation.setSTarget(sTarget);
							sPointingRelation.getSLayers().add(mmaxSLayer);							
						}else{
							throw new PepperModuleException("Developper error: unknown type of markable attribute '"+attributeType+"'...");
						}		
					}
				}
			}
		}
	}	

	// method to handle exported SContainer
	
	private void handleSContainer(SaltExtendedMarkable markable){
		SaltExtendedMarkableContainer containerMarkable = (SaltExtendedMarkableContainer) markable;
		SaltExtendedMarkable containedMarkable = getMarkable(containerMarkable.getAttribute("contained_id").getValue(),containerMarkable.getAttribute("contained_scheme").getValue());
		if(containedMarkable == null){
			throw new PepperModuleException("Unknow contained SNode markable in SContainer markable '"+containerMarkable+"'");
		}
		this.claimSContainer.put(markable,containedMarkable);
	}
	
	// method to handle exported SLayer
		
	private SLayer createSLayer(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SLayer sLayer = SaltFactory.eINSTANCE.createSLayer();
		sLayer.setSName(markable.getSName());
		affectSId(sLayer,markable.getSId());
		this.sLayerHash.put(markable.getId(),sLayer);	
		sDocumentGraph.addSLayer(sLayer);
		return sLayer;
	}
	
	// method to handle exported STextualDs
	
	private STextualDS createSTextualDS(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable, ArrayList<BaseDataUnit> baseDataUnits, Hashtable<String,int[]> indicesTokens){
		STextualDS sTextualDS = SaltFactory.eINSTANCE.createSTextualDS();
		if(markable == null){
			sTextualDS.setSName("Mmax2_textualDs");
			sTextualDS.setSId("Mmax2_textualDs");
		}else{
			sTextualDS.setSName(markable.getSName());
			registerSNode(markable,sTextualDS);
			affectSId(sTextualDS,markable.getSId());
		}
		
		if((baseDataUnits == null) || (baseDataUnits.size() == 0)){
			throw new PepperModuleDataException(this, "TextualDS markable "+sTextualDS.getSName()+" covers no BaseData unit at all...");
		}
		
		int[] startAndEnd = getStartAndEnd(baseDataUnits.get(0).getId(), indicesTokens);
		ArrayList<String> allStr = new ArrayList<String>();
		for(BaseDataUnit baseDataUnit: baseDataUnits){
			this.sTextualDsBaseDataUnitCorrespondance.put(baseDataUnit.getId(),sTextualDS);
			allStr.add(baseDataUnit.getText());
		}
		sTextualDS.setSText(StringUtils.join(allStr.toArray(new String[allStr.size()]),""));
		
		sDocumentGraph.addSNode(sTextualDS);	
		this.sTextualDsOfset.put(sTextualDS, startAndEnd[0]);
		return sTextualDS;
	}

	// method to handle exported SStruct
	
	private SStructure createSStruct(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SStructure sStruct = SaltFactory.eINSTANCE.createSStructure();
		sStruct.setSName(markable.getSName());
		registerSNode(markable,sStruct);
		sDocumentGraph.addSNode(sStruct);		
		affectSId(sStruct,markable.getSId());
		return sStruct;
	}
	
	// method to handle exported SToken

	private SToken createSToken(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SToken sToken = SaltFactory.eINSTANCE.createSToken();
		sToken.setSName(markable.getSName());
		registerSNode(markable,sToken);
		sDocumentGraph.addSNode(sToken);	
		affectSId(sToken,markable.getSId());
		return sToken;
	}
	
	// method to handle exported SSPan
	
	private SSpan createSSPan(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SSpan sSpan = SaltFactory.eINSTANCE.createSSpan();
		sSpan.setSName(markable.getSName());
		registerSNode(markable,sSpan);
		sDocumentGraph.addSNode(sSpan);
		affectSId(sSpan,markable.getSId());
		return sSpan;
	}
	
	// methods to handle exported STextualRelations
	
	private STextualRelation createSTextualRelation(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		STextualRelation sTextualRel = SaltFactory.eINSTANCE.createSTextualRelation();
		sTextualRel.setSName(markable.getSName());
		sDocumentGraph.addSRelation(sTextualRel);
		registerSRelation(markable,sTextualRel);
		affectSId(sTextualRel,markable.getSId());
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
			throw new PepperModuleDataException(this, "'target_token' attribute is missing on Saltextended markable '"+markable+"' representing an STextualRelation");
		markable.removeAttribute(targetTokenAttribute);
		
		if(targetTextualDsAttribute == null)
			throw new PepperModuleDataException(this, "'target_textual_ds' attribute is missing on Saltextended markable '"+markable+"' representing an STextualRelation");
		markable.removeAttribute(targetTextualDsAttribute);
		
		MarkablePointerAttributeFactory targetFactory = (MarkablePointerAttributeFactory) targetTokenAttribute.getFactory();
		SaltExtendedMarkable targetSTokenMarkable = getMarkable(targetTokenAttribute.getValue(), targetFactory.getTargetSchemeName());
		if(targetSTokenMarkable == null)
			throw new PepperModuleDataException(this, "An unknown markable of id '"+targetTokenAttribute.getValue()+"' belonging to scheme '"+targetFactory.getTargetSchemeName()
					+"' is referenced as the target of the pointer '"+targetTokenAttribute.getName()+"' within markable '"+markable+"'");
		SNode sToken = (SToken) getSNode(targetSTokenMarkable);
		if(sToken == null)
			throw new PepperModuleDataException(this, "An unknown SToken node represented by markable '"+targetSTokenMarkable+"' is referenced as the target for the STextualRelation represented by markable '"+markable+"'");
		try{
			sTextualRelation.setSToken((SToken) sToken);
		}catch(ClassCastException e){
			throw new PepperModuleDataException(this, "The SNode represented by markable '"+targetSTokenMarkable+"' and referenced as the target for the STextualRelation represented by markable '"+markable+"' is not a SToken...");
		}
		
		String sTokenSpan = markable.getSpan();
		if(!sTokenSpan.contains("..") && !sTokenSpan.contains(",")){
			this.sTokensHash.put(sTokenSpan,(SToken)  sToken);
		}
		
		MarkablePointerAttributeFactory targettextualDsFactory = (MarkablePointerAttributeFactory) targetTextualDsAttribute.getFactory();
		SaltExtendedMarkable targetDSMarkable = getMarkable(targetTextualDsAttribute.getValue(), targettextualDsFactory.getTargetSchemeName());
		if(targetDSMarkable == null)
			throw new PepperModuleDataException(this, "An unknown markable of id '"+targetTextualDsAttribute.getValue()+"' belonging to scheme '"+targettextualDsFactory.getTargetSchemeName()
					+"' is referenced as the target of the pointer '"+targetTextualDsAttribute.getName()+"' within markable '"+markable+"'");
		SNode sTextualDs = (STextualDS) getSNode(targetDSMarkable);
		if(sTextualDs == null)
			throw new PepperModuleDataException(this, "An unknown STextualDS node represented by markable '"+targetDSMarkable+"' is referenced as the target for the STextualRelation represented by markable '"+markable+"'");
		try{
			sTextualRelation.setSTextualDS((STextualDS) sTextualDs);
		}catch(ClassCastException e){
			throw new PepperModuleDataException(this, "The SNode represented by markable '"+targetDSMarkable+"' and referenced as the target for the STextualRelation represented by markable '"+markable+"' is not a STextualDS...");
		}
		
		int[] startAndEnd = getStartAndEnd(markable.getSpan(), indicesTokens);
		sTextualRelation.setSStart(startAndEnd[0] - this.sTextualDsOfset.get(sTextualDs));
		sTextualRelation.setSEnd(startAndEnd[1] - this.sTextualDsOfset.get(sTextualDs));
	}
	
	// methods to handle exported SDominanceRelation
	
	private SDominanceRelation createSDomRel(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SDominanceRelation sDomRel = SaltFactory.eINSTANCE.createSDominanceRelation();
		sDomRel.setSName(markable.getSName());
		sDocumentGraph.addSRelation(sDomRel);
		registerSRelation(markable,sDomRel);
		affectSId(sDomRel,markable.getSId());
		
		return sDomRel;
	}
	
	
	private void completeSDomRel(SDominanceRelation sDomRel, SaltExtendedMarkable markable){
		MarkableAttribute structAttribute = null;
		MarkableAttribute structSchemeAttribute = null;
		MarkableAttribute targetAttribute = null;
		MarkableAttribute targetSchemeAttribute = null;	
		MarkableAttribute containerPointerAttribute = null;	
	
		ArrayList<MarkableAttribute> markableAttributes = markable.getAttributes();
		for(MarkableAttribute markableAttribute : markableAttributes){
			if(markableAttribute.getName().equals("struct")){
				structAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("struct_scheme")){
				structSchemeAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("target")){
				targetAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("target_scheme")){
				targetSchemeAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("source_attr")){
				containerPointerAttribute = markableAttribute;
			}
		}
		
		if(structAttribute == null)
			throw new PepperModuleDataException(this, "'struct' attribute is missing on Saltextended markable '"+markable+"' representing an SDominationRelation");
		markable.removeAttribute(structAttribute);
	
		SNode sStruct = null;
		SNode sStructuredTarget = null;
		if(structSchemeAttribute != null){//struct markable is a SContainer	
			SaltExtendedMarkable sStructMarkable = getMarkable(structAttribute.getValue(),structSchemeAttribute.getValue());
			if(sStructMarkable == null)
				throw new PepperModuleDataException(this, "An unknown SContainer markable is referenced as the sStructured target of the pointer within markable '"+markable+"'");
			
			SaltExtendedMarkable sstructContainedMarkable = this.claimSContainer.get(sStructMarkable);
			sStruct = getSNode(sstructContainedMarkable);
			
			if(containerPointerAttribute == null)
				throw new PepperModuleDataException(this, "'source_attr' attribute is missing on Saltextended markable '"+markable+"' representing an SDominanceRelation");
			markableAttributes.remove(containerPointerAttribute);
			
			MarkableAttribute pointer = null;
			String sstructPointerAttribute = containerPointerAttribute.getValue();
			for(MarkableAttribute sstructMarkableAttribute : sStructMarkable.getAttributes()){
				if(sstructMarkableAttribute.getName().equals(sstructPointerAttribute)){
					pointer = sstructMarkableAttribute;
					break;
				}
			}
			
			if(pointer == null)
				throw new PepperModuleDataException(this, "'"+sstructPointerAttribute+"' attribute is missing on SContainer markable '"+sStructMarkable+"'");
			sStructMarkable.removeAttribute(pointer);
			
			MarkablePointerAttributeFactory pointerFactory = (MarkablePointerAttributeFactory) pointer.getFactory();
			SaltExtendedMarkable targetMarkable = getMarkable(pointer.getValue(),pointerFactory.getTargetSchemeName());
			if(targetMarkable == null)
				 throw new PepperModuleDataException(this, " An unknown markable is referenced as the target of Saltextended markable '"+markable+"' representing an SDominanceRelation");
			 
			SaltExtendedMarkable targetContainedMarkable = this.claimSContainer.get(targetMarkable);
			sStructuredTarget = getSNode(targetContainedMarkable);
			if(sStructuredTarget == null)
				throw new PepperModuleDataException(this, "An unknown target node is referenced as the target for the SDominanceRelation represented by markable '"+markable+"'");
		}else{
			SaltExtendedMarkable sStructMarkable = getMarkable(structAttribute.getValue(),SaltExtendedMmax2Infos.SALT_INFO_TYPE_SSTRUCT);
			if(sStructMarkable == null)
				throw new PepperModuleDataException(this, "An unknown markable is referenced as the sStructured source of the pointer within markable '"+markable+"'");
		
			if(targetAttribute == null)
				throw new PepperModuleDataException(this, "'target' attribute is missing on SContainer markable '"+markable+"'");
			markable.removeAttribute(targetAttribute);
			
			if(targetSchemeAttribute == null)
				throw new PepperModuleDataException(this, "'target_scheme' attribute is missing on SContainer markable '"+markable+"'");
			markable.removeAttribute(targetSchemeAttribute);
			
			SaltExtendedMarkable targetMarkable = getMarkable(targetAttribute.getValue(),targetSchemeAttribute.getValue());
			if(targetMarkable == null)
				throw new PepperModuleDataException(this, " An unknown markable is referenced as the target of markable '"+markable+"' representing an SDominanceRelation");
			 
			sStructuredTarget = getSNode(targetMarkable);
			if(sStructuredTarget == null)
				throw new PepperModuleDataException(this, "An unknown target node is referenced as the target for the SDominanceRelation represented by markable '"+markable+"'");
		}
		
		try{
			sDomRel.setSStructure((SStructure) sStruct);
		}catch(ClassCastException e){
			throw new PepperModuleDataException(this, "The SNode referenced as source sstruct by SDominanceRelation markable '"+markable+"' is not anymore a SSTructure");
		}
		
		try{
			sDomRel.setSStructuredTarget((SStructuredNode) sStructuredTarget);	
		}catch(ClassCastException e){
				throw new PepperModuleDataException(this, "The SNode referenced as target by SDominanceRelation markable '"+markable+"' is not anymore a SStructuredNode");
		}					
	}
	
	// methods to handle exported SSpanningRelation
	
	private SSpanningRelation createSSpanningRelation(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SSpanningRelation sSpanningRel = SaltFactory.eINSTANCE.createSSpanningRelation();
		sSpanningRel.setSName(markable.getSName());
		sDocumentGraph.addSRelation(sSpanningRel);
		registerSRelation(markable,sSpanningRel);
		affectSId(sSpanningRel,markable.getSId());
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
		
		if(sourceSpanAttribute == null)
			throw new PepperModuleDataException(this, "'source_span' attribute is missing on Saltextended markable '"+markable+"' representing an SPanningRelation");
		markable.removeAttribute(sourceSpanAttribute);
		
		MarkablePointerAttributeFactory sourceSpanAttrFactory = (MarkablePointerAttributeFactory) sourceSpanAttribute.getFactory();
		SaltExtendedMarkable sourceSpanMarkable = getMarkable(sourceSpanAttribute.getValue(), sourceSpanAttrFactory.getTargetSchemeName());
		if(sourceSpanMarkable == null)
			throw new PepperModuleDataException(this, "An unknown markable of id '"+sourceSpanAttribute.getValue()+"' belonging to scheme '"+sourceSpanAttrFactory.getTargetSchemeName()
					+"' is referenced as the target of the pointer '"+sourceSpanAttribute.getName()+"' within markable '"+markable+"'");
			
		SNode sSpan = getSNode(sourceSpanMarkable);
		if(sSpan == null)
			throw new PepperModuleDataException(this, "An unknown SSPan node represented by markable '"+sourceSpanMarkable+"' is referenced as the source for the SPanningRelation represented by markable '"+markable+"'");
		try{
			sSpanningRel.setSSpan((SSpan) sSpan);
		}catch(ClassCastException e){
			throw new PepperModuleDataException(this, "The SNode represented by markable '"+sourceSpanMarkable+"' and referenced as the source for the SPanningRelation represented by markable '"+markable+"' is not an SSpan...");
		}
		
		if(targetTokenAttribute == null)
			throw new PepperModuleDataException(this, "'target_token' attribute is missing on Saltextended markable '"+markable+"' representing an SPanningRelation");
		markable.removeAttribute(targetTokenAttribute);
		
		MarkablePointerAttributeFactory targetTokenAttrFactory = (MarkablePointerAttributeFactory) targetTokenAttribute.getFactory();
		SaltExtendedMarkable targetTokenMarkable = getMarkable(targetTokenAttribute.getValue(), targetTokenAttrFactory.getTargetSchemeName());
		if(targetTokenMarkable == null)
			throw new PepperModuleDataException(this, "An unknown markable of id '"+targetTokenAttribute.getValue()+"' belonging to scheme '"+targetTokenAttrFactory.getTargetSchemeName()
					+"' is referenced as the target of the pointer '"+targetTokenAttribute.getName()+"' within markable '"+markable+"'");
		
		SNode sToken =  getSNode(targetTokenMarkable);
		if(sToken == null)
			throw new PepperModuleDataException(this, "An unknown SToken node represented by markable '"+targetTokenMarkable+"' is referenced as the target for the SPanningRelation represented by markable '"+markable+"'");
		try{
			sSpanningRel.setSToken((SToken) sToken);
		}catch(ClassCastException e){
			throw new PepperModuleDataException(this, "The SNode represented by markable '"+targetTokenMarkable+"' and referenced as the target for the SPanningRelation represented by markable '"+markable+"' is not a SToken");
		}
	}
	
	// methods to handle exported SPointingRelation
	
	private SPointingRelation createSPointer(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SPointingRelation sPointingRelation = SaltFactory.eINSTANCE.createSPointingRelation();		
		sPointingRelation.setSName(markable.getSName());
		sDocumentGraph.addSRelation(sPointingRelation);
		registerSRelation(markable,sPointingRelation);
		affectSId(sPointingRelation,markable.getSId());
		return sPointingRelation;
	}		

	
	private void completeSPointer(SPointingRelation sPointingRelation, SaltExtendedMarkable markable){
		MarkableAttribute sourceAttribute = null;
		MarkableAttribute sourceSchemeAttribute = null;
		MarkableAttribute targetAttribute = null;	
		MarkableAttribute targetSchemeAttribute = null;	
		MarkableAttribute containerPointerAttribute = null;	
	
		ArrayList<MarkableAttribute> markableAttributes = markable.getAttributes();
		for(MarkableAttribute markableAttribute : markableAttributes){
			if(markableAttribute.getName().equals("source")){
				sourceAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("source_attr")){
				containerPointerAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("target")){
				targetAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("target_scheme")){
				targetSchemeAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("source_scheme")){
				sourceSchemeAttribute = markableAttribute;
			}
		}
		
		if(sourceAttribute == null)
			throw new PepperModuleDataException(this, "'source' attribute is missing on Saltextended markable '"+markable+"' representing an SPointingRelation");
		markableAttributes.remove(sourceAttribute);
		
		if(sourceSchemeAttribute == null)
			throw new PepperModuleDataException(this, "'source_scheme' attribute is missing on Saltextended markable '"+markable+"' representing an SPointingRelation");
		markableAttributes.remove(sourceSchemeAttribute);
		
		SaltExtendedMarkable sourceMarkable = getMarkable(sourceAttribute.getValue(),sourceSchemeAttribute.getValue());
		if(sourceMarkable == null)
			throw new PepperModuleDataException(this, "An unknown markable is referenced as the source for the SPointingRelation represented by markable '"+markable+"'");
		
		SNode sSource = null;
		SNode sTarget = null;
		if(sourceMarkable.getSType().equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER)){
			SaltExtendedMarkable sourceContainedMarkable = this.claimSContainer.get(sourceMarkable);
			sSource = getSNode(sourceContainedMarkable);
			
			if(containerPointerAttribute == null)
				throw new PepperModuleDataException(this, "'source_attr' attribute is missing on Saltextended markable '"+markable+"' representing an SPointingRelation");
			markableAttributes.remove(containerPointerAttribute);
			
			MarkableAttribute pointer = null;
			String sourcePointerAttribute = containerPointerAttribute.getValue();
			for(MarkableAttribute sourceMarkableAttribute : sourceMarkable.getAttributes()){
				if(sourceMarkableAttribute.getName().equals(sourcePointerAttribute)){
					pointer = sourceMarkableAttribute;
					break;
				}
			}
			
			if(pointer == null)
				throw new PepperModuleDataException(this, "'"+sourcePointerAttribute+"' attribute is missing on SContainer markable '"+sourceMarkable+"'");
			sourceMarkable.removeAttribute(pointer);
			
			MarkablePointerAttributeFactory pointerFactory = (MarkablePointerAttributeFactory) pointer.getFactory();
			SaltExtendedMarkable targetMarkable = getMarkable(pointer.getValue(),pointerFactory.getTargetSchemeName());
			if(targetMarkable == null)
				 throw new PepperModuleDataException(this, " An unknown markable is referenced as the target of Saltextended markable '"+markable+"' representing an SPointingRelation");
			 
			SaltExtendedMarkable targetContainedMarkable = this.claimSContainer.get(targetMarkable);
			sTarget = getSNode(targetContainedMarkable);
			if(sTarget == null)
					throw new PepperModuleDataException(this, "An unknown target node is referenced as the target for the SPointingRelation represented by markable '"+markable+"'");
		}else{
			sSource = getSNode(sourceMarkable);
			if(sSource == null)
				throw new PepperModuleDataException(this, "An unknown SNode node is referenced as the source for the SPointingRelation represented by markable '"+markable+"'");
			 
			if(targetAttribute == null)
				throw new PepperModuleDataException(this, "'target' attribute is missing on Saltextended markable '"+markable+"' representing an SPointingRelation");
			markableAttributes.remove(targetAttribute);
			 
			if(targetSchemeAttribute == null)
				throw new PepperModuleDataException(this, "'target_scheme' attribute is missing on Saltextended markable '"+markable+"' representing an SPointingRelation");
			markableAttributes.remove(targetSchemeAttribute);
			 
			SaltExtendedMarkable targetMarkable = getMarkable(targetAttribute.getValue(),targetSchemeAttribute.getValue());
			if(targetMarkable == null)
				throw new PepperModuleDataException(this, " An unknown markable is referenced as the target of Saltextended markable '"+markable+"' representing an SPointingRelation");
			 
			sTarget = getSNode(targetMarkable);
			if(sTarget == null)
				throw new PepperModuleDataException(this, "An unknown target node is referenced as the target for the SPointingRelation represented by markable '"+markable+"'");
		}
		
		sPointingRelation.setSSource(sSource);
		sPointingRelation.setSTarget(sTarget);	
	}
	
	// method to handle exported SMetaAnnotation
	
	private void createSMetaAnnotation(SDocumentGraph sDocumentGraph, SMetaAnnotatableElement sAnnotatableElement, SaltExtendedMarkable annotationMarkable) {
		MarkableAttribute namespaceAttribute = null;
		MarkableAttribute targetMarkableAttribute = null;
		MarkableAttribute nameAttribute = null;
		MarkableAttribute containerSchemeAttribute = null;
		MarkableAttribute containerAttrNameAttribute = null;
		MarkableAttribute valueAttribute = null;
		
		ArrayList<MarkableAttribute> markableAttributes = annotationMarkable.getAttributes();
		for(MarkableAttribute markableAttribute: markableAttributes){
			String attributeName = markableAttribute.getName();
			if(attributeName.equals("namespace")){
				namespaceAttribute = markableAttribute;
			}else if(attributeName.equals("target_markable")){
				targetMarkableAttribute = markableAttribute;
			}else if(attributeName.equals("attr_name")){
				nameAttribute = markableAttribute;
			}else if(attributeName.equals("container_scheme")){
				containerSchemeAttribute = markableAttribute;
			}else if(attributeName.equals("container_attr")){
				containerAttrNameAttribute = markableAttribute;
			}else if(attributeName.equals("value")){
				valueAttribute = markableAttribute;
			}
		}
		
		if(namespaceAttribute == null)
			throw new PepperModuleDataException(this, "'namespace' attribute is missing on Saltextended markable '"+annotationMarkable+"' representing an SAnnotation");
		annotationMarkable.removeAttribute(namespaceAttribute);
		
		if(targetMarkableAttribute == null)
			throw new PepperModuleDataException(this, "'target_markable' attribute is missing on Saltextended markable '"+annotationMarkable+"' representing an SAnnotation");
		annotationMarkable.removeAttribute(targetMarkableAttribute);
		
		if(nameAttribute == null)
			throw new PepperModuleDataException(this, "'attr_name' attribute is missing on Saltextended markable '"+annotationMarkable+"' representing an SAnnotation");
		annotationMarkable.removeAttribute(nameAttribute);
		
		String attributeNameSpace = namespaceAttribute.getValue();
		String attributeName = nameAttribute.getValue();
		String completeAttributeName = attributeName;
		if(!attributeNameSpace.equals("")){
			completeAttributeName = attributeNameSpace+"__"+attributeName;
		}
		
		if((containerSchemeAttribute != null) || (containerAttrNameAttribute != null)){
			if((containerSchemeAttribute != null) &&(containerAttrNameAttribute != null)){// the value of the attribute has been mapped by the exporter to a container markable
				annotationMarkable.removeAttribute(containerSchemeAttribute);
				annotationMarkable.removeAttribute(containerAttrNameAttribute);
				SaltExtendedMarkable containerMarkable = getMarkable(targetMarkableAttribute.getValue(),containerSchemeAttribute.getValue());
				if(containerMarkable == null)
					throw new PepperModuleDataException(this, "An unknown SContainer markable is referenced in SMetaAnnotationMarkable '"+annotationMarkable+"'");
				valueAttribute = containerMarkable.getAttribute(containerAttrNameAttribute.getValue());
				containerMarkable.removeAttribute(valueAttribute);
			}else if(containerSchemeAttribute == null){
				throw new PepperModuleDataException(this, "'container_scheme' attribute is missing for Annotation markable '"+annotationMarkable+"'");
			}else if(containerAttrNameAttribute == null){
				throw new PepperModuleDataException(this, "'container_attr' attribute is missing for Annotation markable '"+annotationMarkable+"'");
			}
		}
		
		if(valueAttribute == null)
			throw new PepperModuleDataException(this, "'"+completeAttributeName+"' attribute is missing for Annotation markable '"+annotationMarkable+"'");
		
		SMetaAnnotation sMetaAnnotation = SaltFactory.eINSTANCE.createSMetaAnnotation();
		if(!attributeNameSpace.equals(""))
			sMetaAnnotation.setSNS(namespaceAttribute.getValue());
		sMetaAnnotation.setSName(attributeName);
		sMetaAnnotation.setSValue(valueAttribute.getValue());
		
		sAnnotatableElement.addSMetaAnnotation(sMetaAnnotation);	
	}
	
	// method to handle exported SAnnotation
	
	private void createSAnnotation(SDocumentGraph sDocumentGraph, SAnnotatableElement sAnnotatableElement, SaltExtendedMarkable annotationMarkable) {
		MarkableAttribute namespaceAttribute = null;
		MarkableAttribute targetMarkableAttribute = null;
		MarkableAttribute nameAttribute = null;
		MarkableAttribute containerSchemeAttribute = null;
		MarkableAttribute containerAttrNameAttribute = null;
		MarkableAttribute valueAttribute = null;
		
		ArrayList<MarkableAttribute> markableAttributes = annotationMarkable.getAttributes();
		for(MarkableAttribute markableAttribute: markableAttributes){
			String attributeName = markableAttribute.getName();
			if(attributeName.equals("namespace")){
				namespaceAttribute = markableAttribute;
			}else if(attributeName.equals("target_markable")){
				targetMarkableAttribute = markableAttribute;
			}else if(attributeName.equals("attr_name")){
				nameAttribute = markableAttribute;
			}else if(attributeName.equals("container_scheme")){
				containerSchemeAttribute = markableAttribute;
			}else if(attributeName.equals("container_attr")){
				containerAttrNameAttribute = markableAttribute;
			}else if(attributeName.equals("value")){
				valueAttribute = markableAttribute;
			}
		}
		
		if(namespaceAttribute == null)
			throw new PepperModuleDataException(this, "'namespace' attribute is missing on Saltextended markable '"+annotationMarkable+"' representing an SAnnotation");
		annotationMarkable.removeAttribute(namespaceAttribute);
		
		if(targetMarkableAttribute == null)
			throw new PepperModuleDataException(this, "'target_markable' attribute is missing on Saltextended markable '"+annotationMarkable+"' representing an SAnnotation");
		annotationMarkable.removeAttribute(targetMarkableAttribute);
		
		if(nameAttribute == null)
			throw new PepperModuleDataException(this, "'attr_name' attribute is missing on Saltextended markable '"+annotationMarkable+"' representing an SAnnotation");
		annotationMarkable.removeAttribute(nameAttribute);
		
		String attributeNameSpace = namespaceAttribute.getValue();
		String attributeName = nameAttribute.getValue();
		String completeAttributeName = attributeName;
		if(!attributeNameSpace.equals("")){
			completeAttributeName = attributeNameSpace+"__"+attributeName;
		}
		
		if((containerSchemeAttribute != null) || (containerAttrNameAttribute != null)){
			if((containerSchemeAttribute != null) &&(containerAttrNameAttribute != null)){// the value of the attribute has been mapped by the exporter to a container markable
				annotationMarkable.removeAttribute(containerSchemeAttribute);
				annotationMarkable.removeAttribute(containerAttrNameAttribute);
				SaltExtendedMarkable containerMarkable = getMarkable(targetMarkableAttribute.getValue(),containerSchemeAttribute.getValue());
				if(containerMarkable == null)
					throw new PepperModuleDataException(this, "An unknown SContainer markable is referenced in SMetaAnnotationMarkable '"+annotationMarkable+"'");
				valueAttribute = containerMarkable.getAttribute(containerAttrNameAttribute.getValue());
				containerMarkable.removeAttribute(valueAttribute);
			}else if(containerSchemeAttribute == null){
				throw new PepperModuleDataException(this, "'container_scheme' attribute is missing for Annotation markable '"+annotationMarkable+"'");
			}else if(containerAttrNameAttribute == null){
				throw new PepperModuleDataException(this, "'container_attr' attribute is missing for Annotation markable '"+annotationMarkable+"'");
			}
		}
		
		if(valueAttribute == null)
			throw new PepperModuleDataException(this, "the value of '"+completeAttributeName+"' attribute is missing for Annotation markable '"+annotationMarkable+"'");
		
		
		SAnnotation sAnnotation = SaltFactory.eINSTANCE.createSAnnotation();
		if(!attributeNameSpace.equals(""))
			sAnnotation.setSNS(attributeNameSpace);
		sAnnotation.setSName(attributeName);
		sAnnotation.setSValue(valueAttribute.getValue());
		sAnnotatableElement.addSAnnotation(sAnnotation);
	}
	
	// method to handle when an Snode belongs to a certain Slayer
	
	private void createSLayerLink(SDocumentGraph sDocumentGraph,SaltExtendedMarkable markable) {
		
		MarkableAttribute slayerAttr = markable.getAttribute("slayer");
		if(slayerAttr == null)
			throw new PepperModuleDataException(this, "'slayer' attribute is missing for SLayerLink markable '"+markable+"'");
				
		SLayer sLayer = this.sLayerHash.get(slayerAttr.getValue());
		if(sLayer == null)
			throw new PepperModuleDataException(this, "An unknown SLayer is referenced within SLayerLink markable '"+markable+"'");
		
		MarkableAttribute selementAttr = markable.getAttribute("selement");
		if(selementAttr == null)
			throw new PepperModuleDataException(this, "'selement' attribute is missing for SLayerLink markable '"+markable+"'");
		MarkablePointerAttributeFactory selementAttrFactory = (MarkablePointerAttributeFactory) selementAttr.getFactory();
		
		SaltExtendedMarkable markableElement = getMarkable(selementAttr.getValue(),selementAttrFactory.getTargetSchemeName());
		if(markableElement == null)
			throw new PepperModuleDataException(this, "'selement' pointer '"+selementAttr+"' within SLayerLink markable '"+markable+"' points to an unkown markable");
		SNode sNode = getSNode(markableElement);
		if(sNode == null){
			SRelation sRelation = getSRelation(markableElement);
			if(sRelation == null)
				throw new PepperModuleDataException(this, "An unknow SElement is referenced within SLayerLink markable '"+markable+"'");
			sRelation.getSLayers().add(sLayer);
		}else{
			sNode.getSLayers().add(sLayer);
		}
	}
	
	// method to handle when an Srelation belongs to a certain SType
	
	private void createSTypeLink(SaltExtendedMarkable markable) {
		MarkableAttribute selementAttr = markable.getAttribute("selement");
		if(selementAttr == null)
			throw new PepperModuleDataException(this, "'selement' attribute is missing for STypeLink markable '"+markable+"'");
		MarkablePointerAttributeFactory selementAttrFactory = (MarkablePointerAttributeFactory) selementAttr.getFactory();
		
		SaltExtendedMarkable markableElement = getMarkable(selementAttr.getValue(),selementAttrFactory.getScheme().getName());
		if(markableElement == null)
			throw new PepperModuleDataException(this, "'selement' pointer within STypeLink markable '"+markable+"' points to an unkown markable");
		SRelation sRelation = getSRelation(markableElement);
		if(sRelation == null)
			throw new PepperModuleDataException(this, "An unknow SRelation is referenced within STypeLink markable '"+markable+"'");

		MarkableAttribute stypeAttr = markable.getAttribute("stype");
		if(stypeAttr == null)
			throw new PepperModuleDataException(this, "'stype' attribute is missing for STypeLink markable '"+markable+"'");
		
		sRelation.addSType(stypeAttr.getValue());		
	}
	
	// some usefuls methods
	
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
		if (	(indicesTokens.containsKey(startToken))&&
				(indicesTokens.containsKey(endToken))){
			Integer start= indicesTokens.get(startToken)[0];
			Integer end= indicesTokens.get(endToken)[1];
			int[] result = {start, end};
			return(result);
		}
		else{
			if (!indicesTokens.containsKey(startToken))
				throw new PepperModuleDataException(this,"An error in data was found: Cannot find start-token '"+startToken+"'.");
			if (!indicesTokens.containsKey(endToken))
				throw new PepperModuleDataException(this,"An error in data was found: Cannot find end-token '"+endToken+"'.");
			return(null);
		}		
	}
	
	private ArrayList<String> getBaseUnitIds(String span){
		String[] limitBaseUnitIds = span.split(java.util.regex.Pattern.quote(".."));
		ArrayList<String> results = new ArrayList<String>();
		
		if(limitBaseUnitIds.length == 2){
			int start = new Integer(limitBaseUnitIds[0].replaceAll("word_", ""));
			int end = new Integer(limitBaseUnitIds[1].replaceAll("word_", ""));
			for(int i = start; i <= end; i++){
				results.add("word_"+i);
			}
		}else{
			results.add(span);
		}
		
		return results;
	}
	
	private SToken getSToken(String baseInitId, Hashtable<String,int[]> indicesTokens){
		SToken sToken = this.sTokensHash.get(baseInitId);
		if(sToken == null){
			int[] startAndEnd = getStartAndEnd(baseInitId, indicesTokens);
			if (startAndEnd!= null){
				sToken = SaltFactory.eINSTANCE.createSToken();
				STextualRelation sTextualRel = SaltFactory.eINSTANCE.createSTextualRelation();
				sTextualRel.setSToken(sToken);
			
				STextualDS sTextualDsOfToken = this.sTextualDsBaseDataUnitCorrespondance.get(baseInitId);
							
				sTextualRel.setSTextualDS(sTextualDsOfToken);
				sTextualRel.setSStart(startAndEnd[0] - this.sTextualDsOfset.get(sTextualDsOfToken));
				sTextualRel.setSEnd(startAndEnd[1] - this.sTextualDsOfset.get(sTextualDsOfToken));
				
				sTextualDsOfToken.getSDocumentGraph().addSNode(sToken);	
				for(SLayer sLayer: sTextualDsOfToken.getSLayers()){
					sToken.getSLayers().add(sLayer);
				}
				sTextualDsOfToken.getSDocumentGraph().addSRelation(sTextualRel);	
				for(SLayer sLayer: sTextualDsOfToken.getSLayers()){
					sTextualRel.getSLayers().add(sLayer);
				}
				this.sTokensHash.put(baseInitId, sToken);
			}
		}
		return sToken;
	}
	
	
	private void affectSId(SIdentifiableElement elem,String SId){
		if(this.saltIds.containsKey(SId)){
			throw new PepperModuleDataException(this, "Data is corrupted => "+"Two SIdentifiable elements have the same SId '"+SId+"' => '"+elem+"' and '"+this.saltIds.get(SId)+"'");
		}else{
			this.saltIds.put(SId, elem);
			elem.setSId(SId);
		}
	}
	
	
	private void registerSRelation(SaltExtendedMarkable key,SRelation sRelation){
			sRelationsHash.put(key,sRelation);	
	}
	
	private SRelation getSRelation(SaltExtendedMarkable key){
		return sRelationsHash.get(key);
	}
		
	private void registerSNode (SaltExtendedMarkable key, SNode sNode){
		sNodesHash.put(key,sNode);		
	}
	
	private SNode getSNode(SaltExtendedMarkable key){
		if(key.getSType().equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER)){
			try{
				return (SNode) this.claimSContainer.get(key);
			}catch(ClassCastException e){
				throw new PepperModuleDataException(this, "An SContainer markable '"+key+"'working as an alias for something that is not an SNode has been referenced...");
			}
		}else{
			return sNodesHash.get(key);
		}
	}
	
	private SaltExtendedMarkable registerMarkable(SaltExtendedMarkable markable){
		String schemeName = markable.getFactory().getScheme().getName();
		if(!this.saltExtendedMarkableHash.containsKey(schemeName)){
			this.saltExtendedMarkableHash.put(schemeName, new Hashtable<String,SaltExtendedMarkable>());
		}
		return this.saltExtendedMarkableHash.get(schemeName).put(markable.getId(),markable);		
	}
	
	private SaltExtendedMarkable getMarkable(String markableId, String schemeName){
		if(!this.saltExtendedMarkableHash.containsKey(schemeName)){
			return null;
		}else{
			return this.saltExtendedMarkableHash.get(schemeName).get(markableId);
		}
	}
	
	private String getNewSid(String schemeName){
		Integer currentCptInteger = this.saltIdsCpt.get(schemeName);
		int currentCpt = 0;
		if(currentCptInteger != null){
			currentCpt = currentCptInteger.intValue();
		}
		
		String newSId = "";
		do{
			currentCpt++;
			newSId = schemeName+"_"+currentCpt;
		}
		while(this.saltIds.containsKey(newSId));
		
		this.saltIdsCpt.put(schemeName, currentCpt);
		
		return newSId;
	}
}
