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
	
	private Hashtable<String,SNode> SNodesHash;
	private Hashtable<String,SRelation> SRelationsHash;
	private Hashtable<String,SLayer> SLayerHash;
	private Hashtable<STextualDS,Integer> STextualDsOfset;
	private Hashtable<String,SToken> STokensHash;
	private Hashtable<String,SaltExtendedMarkable> SaltExtendedMarkableHash;
	private Hashtable<String,STextualDS> sTextualDsBaseDataUnitCorrespondance;
	private Hashtable<String,String> claimSContainer;
	private Hashtable<String,SIdentifiableElement> SaltIds;
	private Hashtable<String,Integer> SaltIdsCpt;
	
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
	 * Maps a SaltExtendedDocument document to an SDocument sDocument
	 * @param document The SaltExtendedDocument document to map
	 * @param sDocument the SDocument to which the data is being mapped to 
	 */
	public void mapSDocument(SaltExtendedDocument document,SDocument sDocument){
		this.SNodesHash = new Hashtable<String, SNode>();
		this.SRelationsHash = new Hashtable<String, SRelation>();
		this.SLayerHash = new Hashtable<String, SLayer>();
		this.STextualDsOfset = new Hashtable<STextualDS, Integer>();
		this.STokensHash = new Hashtable<String, SToken>();
		this.SaltExtendedMarkableHash = new Hashtable<String, SaltExtendedMarkableFactory.SaltExtendedMarkable>();
		this.sTextualDsBaseDataUnitCorrespondance = new Hashtable<String, STextualDS>();
		this.claimSContainer = new Hashtable<String, String>();
		this.SaltIds = new Hashtable<String,SIdentifiableElement>();
		this.SaltIdsCpt = new Hashtable<String, Integer>();
	
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
			this.SaltExtendedMarkableHash.put(markable.getId(), markable);	
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
					handleSContainer(markable);		
				}else{
					throw new PepperModuleException("Developper error:Unknown type '"+sType+"'");
				}
			}
		}
		
		if(sDocumentMarkable != null){
			sDocument.setSName(sDocumentMarkable.getSName());
			//affectSId(sDocument,sDocumentMarkable.getSId());// set the Id of the document results in a very strange bug....
		}
		
		if(sDocumentGraphMarkable != null){
			sDocumentGraph.setSName(sDocumentGraphMarkable.getSName());
			//affectSId(sDocumentGraph,sDocumentGraphMarkable.getSId());
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
			for(SLayer sLayer: this.SLayerHash.values()){
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
					registerSNode(markable.getId(),sSpan);
					
					SAnnotation sAnnotation = SaltFactory.eINSTANCE.createSAnnotation();
					sAnnotation.setSNS("Mmax2");
					sAnnotation.setSName("markable_scheme");
					sAnnotation.setSValue(schemeName);
					sSpan.addSAnnotation(sAnnotation);
	
					mmaxSLayer.getSNodes().add(sSpan);
					sSpan.getSLayers().add(mmaxSLayer);
					
					String span = markable.getSpan();
					String[] spans = span.split(",");
					for(int i = 0; i < spans.length; i++){
						ArrayList<String> baseDateUnitIds = getBaseUnitIds(spans[i]);
						
						for(String baseDataUnitId: baseDateUnitIds){
							SSpanningRelation sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
							sDocumentGraph.addSRelation(sSpanRel);
							mmaxSLayer.getSRelations().add(sSpanRel);
							sSpanRel.getSLayers().add(mmaxSLayer);
							sSpanRel.setSSpan(sSpan);
							sSpanRel.setSToken(getStoken(baseDataUnitId, indicesTokens));
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
					SSpan sSpan = (SSpan) getSNode(markable.getId());
					
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
							SNode sTarget = getSNode(markableAttribute.getValue());
//							if(sTarget == null)
//								throw new PepperModuleDataException(this, "An unknown markable is referenced as the target of the pointer '"+markableAttribute.getName()+"' within markable '"+markable+"'");
							if (sTarget!= null){
								sPointingRelation.setSTarget(sTarget);
								
								mmaxSLayer.getSRelations().add(sPointingRelation);
								sPointingRelation.getSLayers().add(mmaxSLayer);
							}
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
		this.claimSContainer.put(markable.getId(),containerMarkable.getContainedId());
	}
	
	// method to handle exported SLayer
		
	private SLayer createSLayer(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SLayer sLayer = SaltFactory.eINSTANCE.createSLayer();
		sLayer.setSName(markable.getSName());
		affectSId(sLayer,markable.getSId());
		this.SLayerHash.put(markable.getId(),sLayer);	
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
			registerSNode(markable.getId(),sTextualDS);
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
		this.STextualDsOfset.put(sTextualDS, startAndEnd[0]);
		return sTextualDS;
	}

	// method to handle exported SStruct
	
	private SStructure createSStruct(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SStructure sStruct = SaltFactory.eINSTANCE.createSStructure();
		sStruct.setSName(markable.getSName());
		registerSNode(markable.getId(),sStruct);
		sDocumentGraph.addSNode(sStruct);		
		affectSId(sStruct,markable.getSId());
		return sStruct;
	}
	
	// method to handle exported SToken

	private SToken createSToken(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SToken sToken = SaltFactory.eINSTANCE.createSToken();
		sToken.setSName(markable.getSName());
		registerSNode(markable.getId(),sToken);
		sDocumentGraph.addSNode(sToken);	
		affectSId(sToken,markable.getSId());
		return sToken;
	}
	
	// method to handle exported SSPan
	
	private SSpan createSSPan(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SSpan sSpan = SaltFactory.eINSTANCE.createSSpan();
		sSpan.setSName(markable.getSName());
		registerSNode(markable.getId(),sSpan);
		sDocumentGraph.addSNode(sSpan);
		affectSId(sSpan,markable.getSId());
		return sSpan;
	}
	
	// methods to handle exported STextualRelations
	
	private STextualRelation createSTextualRelation(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		STextualRelation sTextualRel = SaltFactory.eINSTANCE.createSTextualRelation();
		sTextualRel.setSName(markable.getSName());
		sDocumentGraph.addSRelation(sTextualRel);
		registerSRelation(markable.getId(),sTextualRel);
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
		
		SToken sToken = (SToken) getSNode(targetTokenAttribute.getValue());
		if(sToken == null)
			throw new PepperModuleDataException(this, "An unknown SToken node '"+targetTokenAttribute.getValue()+"' is referenced in the Saltextended markable '"+markable+"' representing an STextualRelation");
		sTextualRelation.setSToken(sToken);
		
		String sTokenSpan = markable.getSpan();
		if(!sTokenSpan.contains("..") && !sTokenSpan.contains(",")){
			this.STokensHash.put(sTokenSpan, sToken);
		}
		
		STextualDS sTextualDs = (STextualDS) getSNode(targetTextualDsAttribute.getValue());
		if(sTextualDs == null)
			throw new PepperModuleDataException(this, "An unknown STextualDS node '"+targetTextualDsAttribute.getValue()+"' is referenced in the Saltextended markable '"+markable+"' representing an STextualRelation");
		sTextualRelation.setSTextualDS(sTextualDs);
		
		int[] startAndEnd = getStartAndEnd(markable.getSpan(), indicesTokens);
		sTextualRelation.setSStart(startAndEnd[0] - this.STextualDsOfset.get(sTextualDs));
		sTextualRelation.setSEnd(startAndEnd[1] - this.STextualDsOfset.get(sTextualDs));
	}
	
	// methods to handle exported SDominanceRelation
	
	private SDominanceRelation createSDomRel(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SDominanceRelation sDomRel = SaltFactory.eINSTANCE.createSDominanceRelation();
		sDomRel.setSName(markable.getSName());
		sDocumentGraph.addSRelation(sDomRel);
		registerSRelation(markable.getId(),sDomRel);
		affectSId(sDomRel,markable.getSId());
		
		return sDomRel;
	}
	
	
	private void completeSDomRel(SDominanceRelation sDomRel, SaltExtendedMarkable markable){
		MarkableAttribute structAttribute = null;
		MarkableAttribute targetAttribute = null;	
		MarkableAttribute containerPointerAttribute = null;	
	
		ArrayList<MarkableAttribute> markableAttributes = markable.getAttributes();
		for(MarkableAttribute markableAttribute : markableAttributes){
			if(markableAttribute.getName().equals("struct")){
				structAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("target")){
				targetAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("source_attr")){
				containerPointerAttribute = markableAttribute;
			}
		}
		
		if(structAttribute == null)
			throw new PepperModuleDataException(this, "'struct' attribute is missing on Saltextended markable '"+markable+"' representing an SDominationRelation");
		markable.removeAttribute(structAttribute);
		
		SNode sStruct = getSNode(structAttribute.getValue());
		if(sStruct == null)
			throw new PepperModuleDataException(this, "An unknown markable  is referenced as the sStructured target of the pointer within markable '"+markable+"'");
		sDomRel.setSStructure((SStructure) sStruct);
				
		MarkableAttribute keyPointer = null;
		SaltExtendedMarkable structureMarkable = this.SaltExtendedMarkableHash.get(structAttribute.getValue());
		if(!structureMarkable.getSType().equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER)){
			if(targetAttribute == null)
				throw new PepperModuleDataException(this, "'target' attribute is missing on Saltextended markable '"+markable+"' representing an SDominationRelation");
			markable.removeAttribute(targetAttribute);
			keyPointer = targetAttribute;
		}else{
			String containerPointerName = containerPointerAttribute.getValue();
			for(MarkableAttribute markableAttribute : structureMarkable.getAttributes()){
				if(markableAttribute.getName().equals(containerPointerName)){
					keyPointer = markableAttribute;
					break;
				}
			}
			if(keyPointer == null)
				throw new PepperModuleDataException(this, "'"+containerPointerName+"' attribute is missing on Saltextended markable '"+structureMarkable+"' representing an SContainer");
			structureMarkable.removeAttribute(keyPointer);
		}
			
		SNode sStructuredTarget = getSNode(keyPointer.getValue());;
		if(sStructuredTarget == null)
			throw new PepperModuleDataException(this, "An unknown markable is referenced as the sStructured target of the pointer within markable '"+markable+"'");
		if(!(sStructuredTarget instanceof SStructuredNode))
			throw new PepperModuleDataException(this, "An dominance relation is now pointing to a SNode that is not a StructuredNode, see markable '"+markable+"'");
		
		sDomRel.setSStructuredTarget((SStructuredNode) sStructuredTarget);
	}
	
	// methods to handle exported SSpanningRelation
	
	private SSpanningRelation createSSpanningRelation(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SSpanningRelation sSpanningRel = SaltFactory.eINSTANCE.createSSpanningRelation();
		sSpanningRel.setSName(markable.getSName());
		sDocumentGraph.addSRelation(sSpanningRel);
		registerSRelation(markable.getId(),sSpanningRel);
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
		
		SNode sSpan = getSNode(sourceSpanAttribute.getValue());
		if(sSpan == null)
			throw new PepperModuleDataException(this, "An unknown SSPan node is referenced as the source for the SPanningRelation represented by markable '"+markable+"'");
		sSpanningRel.setSSpan((SSpan) sSpan);
		
		if(targetTokenAttribute == null)
			throw new PepperModuleDataException(this, "'target_token' attribute is missing on Saltextended markable '"+markable+"' representing an SPanningRelation");
		markable.removeAttribute(targetTokenAttribute);
		
		SNode sToken =  getSNode(targetTokenAttribute.getValue());
		if(sToken == null)
			throw new PepperModuleDataException(this, "An unknown SToken node is referenced as the target for the SPanningRelation represented by markable '"+markable+"'");
		sSpanningRel.setSToken((SToken) sToken);
	}
	
	// methods to handle exported SPointingRelation
	
	private SPointingRelation createSPointer(SDocumentGraph sDocumentGraph, SaltExtendedMarkable markable) {
		SPointingRelation sPointingRelation = SaltFactory.eINSTANCE.createSPointingRelation();		
		sPointingRelation.setSName(markable.getSName());
		sDocumentGraph.addSRelation(sPointingRelation);
		registerSRelation(markable.getId(),sPointingRelation);
		affectSId(sPointingRelation,markable.getSId());
		return sPointingRelation;
	}		

	
	private void completeSPointer(SPointingRelation sPointingRelation, SaltExtendedMarkable markable){
		MarkableAttribute sourceAttribute = null;
		MarkableAttribute targetAttribute = null;	
		MarkableAttribute containerPointerAttribute = null;	
	
		ArrayList<MarkableAttribute> markableAttributes = markable.getAttributes();
		for(MarkableAttribute markableAttribute : markableAttributes){
			if(markableAttribute.getName().equals("source")){
				sourceAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("target")){
				targetAttribute = markableAttribute;
			}else if(markableAttribute.getName().equals("source_attr")){
				containerPointerAttribute = markableAttribute;
			}
		}
		
		if(sourceAttribute == null)
			throw new PepperModuleDataException(this, "'source' attribute is missing on Saltextended markable '"+markable+"' representing an SPointingRelation");
		markableAttributes.remove(sourceAttribute);
		
		SNode sSource = getSNode(sourceAttribute.getValue());
		if(sSource == null)
			throw new PepperModuleDataException(this, "An unknown source node is referenced as the source for the SPointingRelation represented by markable '"+markable+"'");
		sPointingRelation.setSSource(sSource);
		
		MarkableAttribute keyPointer = null;
		SaltExtendedMarkable sTargetMarkable = this.SaltExtendedMarkableHash.get(sourceAttribute.getValue());
		if(!sTargetMarkable.getSType().equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER)){
			if(targetAttribute == null)
				throw new PepperModuleDataException(this, "'target' attribute is missing on Saltextended markable '"+markable+"' representing an SPointingRelation");
			keyPointer = targetAttribute;
			markable.removeAttribute(keyPointer);
		}else{
			String containerPointerName = containerPointerAttribute.getValue();
			for(MarkableAttribute markableAttribute : sTargetMarkable.getAttributes()){
				if(markableAttribute.getName().equals(containerPointerName)){
					keyPointer = markableAttribute;
					break;
				}
			}
			if(keyPointer == null)
				throw new PepperModuleDataException(this, "'"+containerPointerName+"' attribute is missing on Saltextended markable '"+sTargetMarkable+"' representing an SContainer");
			sTargetMarkable.removeAttribute(keyPointer);
		}
		
		SNode sTarget = getSNode(keyPointer.getValue());
		if(sTarget == null)
			throw new PepperModuleDataException(this, "An unknown target node is referenced as the target for the SPointingRelation represented by markable '"+markable+"'");	
		sPointingRelation.setSTarget(sTarget);	
	}
	
	// method to handle exported SMetaAnnotation
	
	private void createSMetaAnnotation(SDocumentGraph sDocumentGraph, SMetaAnnotatableElement sAnnotatableElement, SaltExtendedMarkable annotationMarkable) {
		MarkableAttribute namespaceAttribute = null;
		MarkableAttribute targetMarkableAttribute = null;
		MarkableAttribute nameAttribute = null;
		MarkableAttribute containerIdAttribute = null;
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
			}else if(attributeName.equals("container_id")){
				containerIdAttribute = markableAttribute;
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
		if((containerIdAttribute != null) && (containerAttrNameAttribute != null)){// the value of the attribute has been mapped by the exporter to a container markable
			annotationMarkable.removeAttribute(containerIdAttribute);
			annotationMarkable.removeAttribute(containerAttrNameAttribute);
			SaltExtendedMarkable containerMarkable = this.SaltExtendedMarkableHash.get(containerIdAttribute.getValue());
			valueAttribute = containerMarkable.getAttribute(containerAttrNameAttribute.getValue());
			containerMarkable.removeAttribute(valueAttribute);
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
		MarkableAttribute containerIdAttribute = null;
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
			}else if(attributeName.equals("container_id")){
				containerIdAttribute = markableAttribute;
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
		
		if((containerIdAttribute != null) && (containerAttrNameAttribute != null)){ // the value of the attribute has been mapped by the exporter to a container markable
			annotationMarkable.removeAttribute(containerIdAttribute);
			annotationMarkable.removeAttribute(containerAttrNameAttribute);
			SaltExtendedMarkable containerMarkable = this.SaltExtendedMarkableHash.get(containerIdAttribute.getValue()); // getting the container markable
			valueAttribute = containerMarkable.getAttribute(containerAttrNameAttribute.getValue());  //getting the value
			containerMarkable.removeAttribute(valueAttribute);
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
		SLayer sLayer = this.SLayerHash.get(markable.getAttribute("SLayer").getValue());
		String sElement = markable.getAttribute("SElement").getValue();
		SNode sNode = getSNode(sElement);
		if(sNode == null){
			SRelation sRelation = getSRelation(sElement);
			sRelation.getSLayers().add(sLayer);
		}else{
			sNode.getSLayers().add(sLayer);
		}
	}
	
	// method to handle when an Srelation belongs to a certain SType
	
	private void createSTypeLink(SaltExtendedMarkable markable) {
		getSRelation(markable.getAttribute("SElement").getValue()).addSType(markable.getAttribute("SType").getValue());		
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
		if (!indicesTokens.containsKey(startToken))
			throw new PepperModuleException(this, "Cannot find start-token '"+startToken+"' in indicesTokens: "+indicesTokens);
		if (!indicesTokens.containsKey(endToken))
			throw new PepperModuleException(this, "Cannot find end-token '"+endToken+"' in indicesTokens: "+indicesTokens);
		Integer start= indicesTokens.get(startToken)[0];
		Integer end= indicesTokens.get(endToken)[1];
//		Integer start= (new Integer(indicesTokens.get(startToken)[0])).intValue();
//		Integer end= (new Integer(indicesTokens.get(endToken)[1])).intValue();
		
		int[] result = {start, end};
		
		return result;		
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
	
	private SToken getStoken(String baseInitId, Hashtable<String,int[]> indicesTokens){
		SToken sToken = this.STokensHash.get(baseInitId);
		if(sToken == null){
			sToken = SaltFactory.eINSTANCE.createSToken();
			STextualRelation sTextualRel = SaltFactory.eINSTANCE.createSTextualRelation();
			sTextualRel.setSToken(sToken);
			
			int[] startAndEnd = getStartAndEnd(baseInitId, indicesTokens);
			STextualDS sTextualDsOfToken = this.sTextualDsBaseDataUnitCorrespondance.get(baseInitId);
						
			sTextualRel.setSTextualDS(sTextualDsOfToken);
			sTextualRel.setSStart(startAndEnd[0] - this.STextualDsOfset.get(sTextualDsOfToken));
			sTextualRel.setSEnd(startAndEnd[1] - this.STextualDsOfset.get(sTextualDsOfToken));
			
			sTextualDsOfToken.getSDocumentGraph().addSNode(sToken);	
			for(SLayer sLayer: sTextualDsOfToken.getSLayers()){
				sLayer.getSNodes().add(sToken);
			}
			sTextualDsOfToken.getSDocumentGraph().addSRelation(sTextualRel);	
			for(SLayer sLayer: sTextualDsOfToken.getSLayers()){
				sLayer.getSRelations().add(sTextualRel);
			}
			this.STokensHash.put(baseInitId, sToken);
		}
		return sToken;
	}
	
	
	private void affectSId(SIdentifiableElement elem,String SId){
		if(this.SaltIds.containsKey(SId)){
			throw new PepperModuleDataException(this, "Data is corrupted => "+"Two SIdentifiable elements have the same SId '"+SId+"' => '"+elem+"' and '"+this.SaltIds.get(SId)+"'");
		}else{
			this.SaltIds.put(SId, elem);
			elem.setSId(SId);
		}
	}
	
	private String getNewSid(String schemeName){
		Integer currentCptInteger = this.SaltIdsCpt.get(schemeName);
		int currentCpt = 0;
		if(currentCptInteger != null){
			currentCpt = currentCptInteger.intValue();
		}
		
		String newSId = "";
		do{
			currentCpt++;
			newSId = schemeName+"_"+currentCpt;
		}
		while(this.SaltIds.containsKey(newSId));
		
		this.SaltIdsCpt.put(schemeName, currentCpt);
		
		return newSId;
	}
	
	private void registerSRelation(String id,SRelation sRelation){
			SRelationsHash.put(id,sRelation);	
	}
	
	private void registerSNode (String key, SNode sNode){
		SNodesHash.put(key,sNode);		
	}
	/** keyword for empty values like anaphor**/
	private static final String KW_EMPTY="empty";
	
	private SNode getSNode(String key){
		SNode sNode= null;
		
		if (!KW_EMPTY.equals(key))
		{
			SaltExtendedMarkable targetMarkable = this.SaltExtendedMarkableHash.get(key);
			if(targetMarkable == null)
				throw new PepperModuleDataException(this, "A markable referenced as '"+key+"' is missing in: "+ SaltExtendedMarkableHash);
			
			if(targetMarkable.getSType().equals(SaltExtendedMmax2Infos.SALT_INFO_TYPE_SCONTAINER)){
				SaltExtendedMarkableContainer targetMarkableContainer = (SaltExtendedMarkableContainer) targetMarkable;
				key = targetMarkableContainer.getContainedId();
			}
			
			sNode = SNodesHash.get(key);
		}
		return sNode;
	}
	
	private SRelation getSRelation(String key){
		return SRelationsHash.get(key);
	}
}
