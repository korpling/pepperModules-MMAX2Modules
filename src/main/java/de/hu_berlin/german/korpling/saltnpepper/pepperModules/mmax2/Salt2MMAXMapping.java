package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAbstractAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SDATATYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;


class Salt2MMAXMapping{
	
	public static final Logger logger= LoggerFactory.getLogger(Salt2MMAX2Mapper.class);
	// the element that conditions can hold to a mapping

	public static final String MAPPING_NODE_NAME = "mapping";
	public static final String MAPPING_INFOS_NODE_NAME = "mapping_infos";
	public static final String CONDITION_NODE_NAME = "condition";
	
	public static final String AND_MATCH_CONDITION = "and";
	public static final String OR_MATCH_CONDITION = "or";
	public static final String NOT_MATCH_CONDITION = "not";
	
	public static final String SANN_MAPPING_ASS_SCHEME = "ass_scheme";
	public static final String SANN_MAPPING_ASS_ATTR = "ass_attr";
	
	public static final String SANN_SNAME_CONDITION = "sname_cond";
	public static final String SANN_NS_CONDITION = "namespace_cond";
	public static final String SANN_SLAYER_CONDITION = "slayer_cond";
	public static final String SANN_STR_VALUE_CONDITION = "string_value_cond";
	
	public static final String SANN_NS_REGEXP = "namespace_regexp";
	public static final String SANN_SLAYER_REGEXP = "slayer_regexp";
	public static final String SANN_SNAME_REGEXP = "sname_regexp";
	public static final String SANN_STRING_VALUE_REGEXP = "value_regexp";
	
	public static final String SREL_MAPP_SOURCE_SCHEME_NAME = "source_scheme";
	public static final String SREL_MAPP_TARGET_SCHEME_NAME = "target_scheme";
	public static final String SREL_MAPP_POINTER_ATTR_NAME = "source_attr";
	
	public static final String SREL_SNAME_CONDITION = "sname_cond";
	public static final String SREL_STYPE_CONDITION = "stype_cond";
	public static final String SREL_SLAYER_CONDITION = "slayer_cond";
	public static final String SREL_SOURCE_TYPE_CONDITION = "source_type_cond";
	public static final String SREL_TARGET_TYPE_CONDITION = "target_type_cond";
	
	public static final String SREL_SNAME_REGEXP = "sname_regexp";
	public static final String SREL_SLAYER_REGEXP = "slayer_regexp";
	public static final String SREL_STYPE_REGEXP = "stype_regexp";
	public static final String SREL_SOURCE_TYPE_REGEXP = "source_type_regexp";
	public static final String SREL_TARGET_TYPE_REGEXP = "target_type_regexp";
			
	
	public static ArrayList<SAnnotationMapping> getSAnnotationMappingsFromFile(MMAX2ExporterProperties props) {
		ArrayList<SAnnotationMapping> mappings = new ArrayList<SAnnotationMapping>();
		
		if(props.getSAnnotationMappingsFilePath() != null){
			DocumentBuilder documentBuilder;
			try {
				documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new PepperModuleException(e.getMessage(), e);
			}			
			
			File configurationFile = new File(props.getSAnnotationMappingsFilePath());	
			NodeList nodes= null;
			try {
				nodes = documentBuilder.parse(configurationFile).getDocumentElement().getChildNodes();
			} catch (SAXException e) {
				throw new PepperModuleException(e.getMessage(), e);
			} catch (IOException e) {
				throw new PepperModuleException(e.getMessage(), e);
			}
				
			for(int i = 0; i < nodes.getLength(); i ++){	
				Node xmlNode = nodes.item(i);
				if(xmlNode.getNodeType() != Node.ELEMENT_NODE){
					continue;
				}
				
				String nodeName = xmlNode.getNodeName();
				if(nodeName.equals(MAPPING_NODE_NAME)){
					Node mapping_infos = null;
					Node condition_infos = null;
					
					NodeList sub_nodes = xmlNode.getChildNodes();
					for(int j = 0; j < sub_nodes.getLength(); j ++){	
						Node sub_xmlNode = sub_nodes.item(j);
						if(sub_xmlNode.getNodeType() != Node.ELEMENT_NODE){
							continue;
						}
						
						String sub_nodeName = sub_xmlNode.getNodeName();
						if(sub_nodeName.equals(MAPPING_INFOS_NODE_NAME)){
							if(mapping_infos == null){
								mapping_infos = sub_xmlNode;
							}else{
								throw new PepperModuleException("More than one mapping infos defined on SAnnotation Mapping '"+xmlNode+"'");
							}
						}else if(sub_nodeName.equals(CONDITION_NODE_NAME)){
							if(condition_infos == null){
								condition_infos = sub_xmlNode; 
							}else{
								throw new PepperModuleException("More than one match condition defined on SAnnotation Mapping '"+xmlNode+"'");
							}
						}else{
							throw new PepperModuleException("Unknown type of Node '"+sub_xmlNode+"' with name '"+sub_nodeName+"' on SAnnotation Mapping '"+xmlNode+"'");
						}
					}
			
					NamedNodeMap mapping_attributes = mapping_infos.getAttributes();
					Node associatedSchemeNameAttribute = mapping_attributes.getNamedItem(SANN_MAPPING_ASS_SCHEME);
					if(associatedSchemeNameAttribute == null){
						throw new PepperModuleException("associated scheme name '"+SANN_MAPPING_ASS_SCHEME+"' on SAnnotation Mapping infos Node '"+mapping_infos+"' is not defined...");
					}
					String associatedSchemeName = associatedSchemeNameAttribute.getNodeValue(); 
					mapping_attributes.removeNamedItem(SANN_MAPPING_ASS_SCHEME);
					
					Node associatedAttributeNameAttribute = mapping_attributes.getNamedItem(SANN_MAPPING_ASS_ATTR);
					if(associatedAttributeNameAttribute == null){
						throw new PepperModuleException("associated attribute name '"+SANN_MAPPING_ASS_ATTR+"' on SAnnotation Mapping infos Node  '"+mapping_infos+"' is not defined...");
					}
					String associatedAttributeName = associatedAttributeNameAttribute.getNodeValue(); 
					mapping_attributes.removeNamedItem(SANN_MAPPING_ASS_ATTR);
					
					SAnnotationMatchCondition condition = parseSAnnotationMatchCondition(condition_infos);
					
					if(mapping_attributes.getLength() != 0){
						ArrayList<String> unknownAttributes = new ArrayList<String>();
						for(int j = 0; j < mapping_attributes.getLength(); j++){
							unknownAttributes.add(mapping_attributes.item(j).getNodeName());
						}	
						throw new PepperModuleException("Unknown attributes '"+StringUtils.join(unknownAttributes,",")+"' on SAnnotation Mapping infos Node  '"+mapping_infos+"'");
					}	
					
					mappings.add(new SAnnotationMapping(condition, associatedSchemeName, associatedAttributeName));
				}else if(xmlNode.getNodeType() == Node.ELEMENT_NODE){
					throw new PepperModuleException("Unknown type of Node among Mapping nodes '"+xmlNode+"' with name '"+nodeName+"'");
				}
			}
		}
		return mappings;
	}
	
	public static SAnnotationMatchCondition parseSAnnotationMatchCondition(Node node) {
		String nodeName = node.getNodeName();
		if(nodeName.equals(CONDITION_NODE_NAME)){
			return parseMainSAnnotationMatchCondition(node);
		}else if(nodeName.equals(AND_MATCH_CONDITION)){
			return parseSAnnotationAndMatchCondition(node);
		}else if(nodeName.equals(OR_MATCH_CONDITION)){
			return parseSAnnotationOrMatchCondition(node);
		}else if(nodeName.equals(NOT_MATCH_CONDITION)){
			return parseSAnnotationNotMatchCondition(node);
		}else if(nodeName.equals(SANN_SLAYER_CONDITION)){
			return parseSAnnotationSLayerMatchCondition(node);
		}else if(nodeName.equals(SANN_SNAME_CONDITION)){
			return parseSAnnotationSNameMatchCondition(node);
		}else if(nodeName.equals(SANN_NS_CONDITION)){
			return parseSAnnotationNameSpaceMatchCondition(node);
		}else if(nodeName.equals(SANN_STR_VALUE_CONDITION)){
			return parseSAnnotationStringValueMatchCondition(node);
		}else{
			throw new PepperModuleException("Unknown SAnnotation match condition type '"+nodeName+"'");
		}
	}
	
	public static SAnnotationMatchCondition parseMainSAnnotationMatchCondition(Node node) { 
		Node condition_node = null;
		NodeList nodes = node.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i ++){	
			Node sub_node = nodes.item(i);
			if(sub_node.getNodeType() == Node.ELEMENT_NODE){
				if(condition_node == null){
					condition_node = sub_node;
				}else{
					throw new PepperModuleException("A condition node of a mapping should have only one \"super\" condition, found two: '"+condition_node+"' and '"+sub_node+"'");
				}
			}
		}
		return parseSAnnotationMatchCondition(condition_node);		
	}
	
	
	public static SAnnotationAndMatchCondition parseSAnnotationAndMatchCondition(Node node) {
		ArrayList<SAnnotationMatchCondition> subconditions = new ArrayList<SAnnotationMatchCondition>();
		NodeList nodes = node.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i ++){	
			Node xmlNode = nodes.item(i);
			if(xmlNode.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			subconditions.add(parseSAnnotationMatchCondition(xmlNode));
		}
		if(subconditions.size() < 1){
			throw new PepperModuleException("And Match condition '"+node+"' has no sub conditions...");
		}
		
		return new SAnnotationAndMatchCondition(subconditions);
	}
	
	public static SAnnotationOrMatchCondition parseSAnnotationOrMatchCondition(Node node) {
		ArrayList<SAnnotationMatchCondition> subconditions = new ArrayList<SAnnotationMatchCondition>();
		NodeList nodes = node.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i ++){	
			Node xmlNode = nodes.item(i);
			if(xmlNode.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			subconditions.add(parseSAnnotationMatchCondition(xmlNode));
		}
		if(subconditions.size() < 1){
			throw new PepperModuleException("And Match condition '"+node+"' has no sub conditions...");
		}
		return new SAnnotationOrMatchCondition(subconditions);
	}
	
	public static SAnnotationNotMatchCondition parseSAnnotationNotMatchCondition(Node node) {
		SAnnotationMatchCondition subcondition = null;
		NodeList nodes = node.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i ++){	
			Node xmlNode = nodes.item(i);
			if(xmlNode.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			SAnnotationMatchCondition local_sub_condition = parseSAnnotationMatchCondition(xmlNode);
			if(subcondition != null){
				throw new PepperModuleException("Not Match condition '"+node+"' has several conditions...");
			}else{
				subcondition = local_sub_condition;
			}
		}
		if(subcondition == null){
			throw new PepperModuleException("And Match condition '"+node+"' has no sub conditions...");
		}
		return new SAnnotationNotMatchCondition(subcondition);
	}	
	
	public static SAnnotationSNameMatchCondition parseSAnnotationSNameMatchCondition(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node snameAttributeNode = attributes.getNamedItem(SANN_SNAME_REGEXP);
		String nameRegExp = null;
		if(snameAttributeNode != null){
			nameRegExp = snameAttributeNode.getNodeValue(); 
			attributes.removeNamedItem(SANN_SNAME_REGEXP);
		}else{
			throw new PepperModuleException("'"+SANN_SNAME_REGEXP +"' attribute not found on SAnnotation SName Match Condition '"+node+"'");
		}
		if(attributes.getLength() != 0){
			throw new PepperModuleException("Additional unexpected attributes found on SAnnotation SName Match Condition '"+node+"'");
		}
		
		return new SAnnotationSNameMatchCondition(Pattern.compile(nameRegExp));
	}
	
	public static SAnnotationSLayerMatchCondition parseSAnnotationSLayerMatchCondition(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node slayerAttributeNode = attributes.getNamedItem(SANN_SLAYER_REGEXP);
		String slayerRegExp = null;
		if(slayerAttributeNode != null){
			slayerRegExp = slayerAttributeNode.getNodeValue(); 
			attributes.removeNamedItem(SANN_SLAYER_REGEXP);
		}else{
			throw new PepperModuleException("'"+SANN_SLAYER_REGEXP + " attribute not found on SAnnotation SLayer Match Condition '"+node+"'");
		}
		if(attributes.getLength() != 0){
			throw new PepperModuleException("Additional unexpected attributes found on SAnnnotation SLayer Match Condition '"+node+"'");
		}
		
		return new SAnnotationSLayerMatchCondition(Pattern.compile(slayerRegExp));
	}
	
	public static SAnnotationNameSpaceMatchCondition parseSAnnotationNameSpaceMatchCondition(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node nsAttributeNode = attributes.getNamedItem(SANN_NS_REGEXP);
		String nsRegExp = null;
		if(nsAttributeNode != null){
			nsRegExp = nsAttributeNode.getNodeValue(); 
			attributes.removeNamedItem(SANN_NS_REGEXP);
		}else{
			throw new PepperModuleException("'"+SANN_NS_REGEXP +"' attribute not found on SAnnotation NameSpace Match Condition '"+node+"'");
		}
		if(attributes.getLength() != 0){
			throw new PepperModuleException("Additional unexpected attributes found on SAnnotation NameSpace Match Condition '"+node+"'");
		}
		
		return new SAnnotationNameSpaceMatchCondition(Pattern.compile(nsRegExp));
	}
	
	public static SAnnotationStringValueMatchCondition parseSAnnotationStringValueMatchCondition(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node stringValueAttributeNode = attributes.getNamedItem(SANN_STRING_VALUE_REGEXP);
		String stringValueRegExp = null;
		if(stringValueAttributeNode != null){
			stringValueRegExp = stringValueAttributeNode.getNodeValue(); 
			attributes.removeNamedItem(SANN_STRING_VALUE_REGEXP);
		}else{
			throw new PepperModuleException("'"+SANN_STRING_VALUE_REGEXP +"' attribute not found on SAnnotation String Value Match Condition '"+node+"'");
		}
		if(attributes.getLength() != 0){
			throw new PepperModuleException("Additional unexpected attributes found on SAnnotation String Value Match Condition '"+node+"'");
		}
		
		return new SAnnotationStringValueMatchCondition(Pattern.compile(stringValueRegExp));
	}
		
	
	public static ArrayList<SRelationMapping> getSRelationMappingsFromFile(MMAX2ExporterProperties props) {
		ArrayList<SRelationMapping> mappings = new ArrayList<SRelationMapping>();
		
		if(props.getSRelationMappingsFilePath() != null){
			DocumentBuilder documentBuilder;
			try {
				documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new PepperModuleException(e.getMessage(), e);
			}			
			
			File configurationFile = new File(props.getSRelationMappingsFilePath());	
			NodeList nodes= null;
			try {
				nodes = documentBuilder.parse(configurationFile).getDocumentElement().getChildNodes();
			} catch (SAXException e) {
				throw new PepperModuleException(e.getMessage(), e);
			} catch (IOException e) {
				throw new PepperModuleException(e.getMessage(), e);
			}
				
			for(int i = 0; i < nodes.getLength(); i ++){	
				Node xmlNode = nodes.item(i);
				if(xmlNode.getNodeType() != Node.ELEMENT_NODE){
					continue;
				}
				
				String nodeName = xmlNode.getNodeName();
				if(nodeName.equals(MAPPING_NODE_NAME)){
					Node mapping_infos = null;
					Node condition_infos = null;
					
					NodeList sub_nodes = xmlNode.getChildNodes();
					for(int j = 0; j < sub_nodes.getLength(); j ++){	
						Node sub_xmlNode = sub_nodes.item(j);
						if(sub_xmlNode.getNodeType() != Node.ELEMENT_NODE){
							continue;
						}
					
						String sub_nodeName = sub_xmlNode.getNodeName();
						if(sub_nodeName.equals(MAPPING_INFOS_NODE_NAME)){
							if(mapping_infos == null){
								mapping_infos = sub_xmlNode;
							}else{
								throw new PepperModuleException("More than one mapping infos defined on SRelation Mapping '"+xmlNode+"'");
							}
						}else if(sub_nodeName.equals(CONDITION_NODE_NAME)){
							if(condition_infos == null){
								condition_infos = sub_xmlNode; 
							}else{
								throw new PepperModuleException("More than one match condition defined on SRelation Mapping '"+xmlNode+"'");
							}
						}else if(!sub_nodeName.equals("")){
							throw new PepperModuleException("Unknown type of Node '"+sub_xmlNode+"' with name '"+sub_nodeName+"' on SRelation Mapping '"+xmlNode+"'");
						}
					}
					NamedNodeMap mapping_attributes = mapping_infos.getAttributes();
					
					Node sourceDestSchemeNode = mapping_attributes.getNamedItem(SREL_MAPP_SOURCE_SCHEME_NAME);
					if(sourceDestSchemeNode == null){
						throw new PepperModuleException("Source destination scheme '"+SREL_MAPP_SOURCE_SCHEME_NAME+"' on SRelation Mapping infos Node '"+mapping_infos+"' is not defined...");
					}
					mapping_attributes.removeNamedItem(SREL_MAPP_SOURCE_SCHEME_NAME);
					String sourceSchemeName = sourceDestSchemeNode.getNodeValue(); 

					Node targetDestSchemeNode = mapping_attributes.getNamedItem(SREL_MAPP_TARGET_SCHEME_NAME);
					if(targetDestSchemeNode == null){
						throw new PepperModuleException("Target destination scheme '"+SREL_MAPP_TARGET_SCHEME_NAME+"' on SRelation Mapping infos Node '"+mapping_infos+"' is not defined...");
					}
					mapping_attributes.removeNamedItem(SREL_MAPP_TARGET_SCHEME_NAME);
					String targetSchemeName = targetDestSchemeNode.getNodeValue(); 

					Node destAttrNode = mapping_attributes.getNamedItem(SREL_MAPP_POINTER_ATTR_NAME);
					if(destAttrNode == null){
						throw new PepperModuleException("Source attribute '"+SREL_MAPP_POINTER_ATTR_NAME+"' on SRelation Mapping infos Node '"+mapping_infos+"' is not defined...");
					}
					mapping_attributes.removeNamedItem(SREL_MAPP_POINTER_ATTR_NAME);
					String attrName = destAttrNode.getNodeValue(); 

					SRelationMatchCondition condition = parseSRelationMatchCondition(condition_infos);
					if(mapping_attributes.getLength() != 0){
						ArrayList<String> unknownAttributes = new ArrayList<String>();
						for(int j = 0; j < mapping_attributes.getLength(); j++){
							unknownAttributes.add(mapping_attributes.item(j).getNodeName());
						}
						throw new PepperModuleException("Unknown attributes '"+StringUtils.join(unknownAttributes,",")+"' on SRelation Mapping infos Node '"+mapping_infos+"'");
					}

					mappings.add(new SRelationMapping(condition, sourceSchemeName, targetSchemeName, attrName));
				}else {
					throw new PepperModuleException("Unknown type of Node among Mapping nodes '"+xmlNode+"' with name '"+nodeName+"'");
				}
			}
		}
			
		return mappings;
	}
	
	public static SRelationMatchCondition parseSRelationMatchCondition(Node node) {
		String nodeName = node.getNodeName();
		if(nodeName.equals(CONDITION_NODE_NAME)){
			return parseMainSRelationMatchCondition(node);
		}else if(nodeName.equals(AND_MATCH_CONDITION)){
			return parseSRelationAndMatchCondition(node);
		}else if(nodeName.equals(OR_MATCH_CONDITION)){
			return parseSRelationOrMatchCondition(node);
		}else if(nodeName.equals(NOT_MATCH_CONDITION)){
			return parseSRelationNotMatchCondition(node);
		}else if(nodeName.equals(SREL_STYPE_CONDITION)){
			return parseSRelationSTypeMatchCondition(node);
		}else if(nodeName.equals(SREL_SLAYER_CONDITION)){
			return parseSRelationSLayerMatchCondition(node);
		}else if(nodeName.equals(SREL_SNAME_CONDITION)){
			return parseSRelationSNameMatchCondition(node);
		}else if(nodeName.equals(SREL_SOURCE_TYPE_CONDITION)){
			return parseSRelationSourceTypeMatchCondition(node);
		}else if(nodeName.equals(SREL_TARGET_TYPE_CONDITION)){
			return parseSRelationTargetTypeMatchCondition(node);
		}else{
			throw new PepperModuleException("Unknown SRelation match condition type '"+nodeName+"'");
		}
	}
	
	public static SRelationMatchCondition parseMainSRelationMatchCondition(Node node) { 
		Node condition_node = null;
		NodeList nodes = node.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i ++){	
			Node sub_node = nodes.item(i);
			if(sub_node.getNodeType() == Node.ELEMENT_NODE){
				if(condition_node == null){
					condition_node = sub_node;
				}else{
					throw new PepperModuleException("A condition node of a mapping should have only one \"super\" condition, found two: '"+condition_node+"' and '"+sub_node+"'");
				}
			}
		}
		return parseSRelationMatchCondition(condition_node);		
	}
	
	public static SRelationAndMatchCondition parseSRelationAndMatchCondition(Node node) {
		ArrayList<SRelationMatchCondition> subconditions = new ArrayList<SRelationMatchCondition>();
		NodeList nodes = node.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i ++){	
			Node xmlNode = nodes.item(i);
			if(xmlNode.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			subconditions.add(parseSRelationMatchCondition(xmlNode));
		}
		if(subconditions.size() < 1){
			throw new PepperModuleException("And match condition '"+node+"'  has no sub conditions...");
		}
		
		return new SRelationAndMatchCondition(subconditions);
	}
	
	public static SRelationOrMatchCondition parseSRelationOrMatchCondition(Node node) {
		ArrayList<SRelationMatchCondition> subconditions = new ArrayList<SRelationMatchCondition>();
		NodeList nodes = node.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i ++){	
			Node xmlNode = nodes.item(i);
			if(xmlNode.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			subconditions.add(parseSRelationMatchCondition(xmlNode));
		}
		if(subconditions.size() < 1){
			throw new PepperModuleException("Or Match condition '"+node+"' has no sub conditions...");
		}
		return new SRelationOrMatchCondition(subconditions);
	}
	
	public static SRelationNotMatchCondition parseSRelationNotMatchCondition(Node node) {
		SRelationMatchCondition subcondition = null;
		NodeList nodes = node.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i ++){	
			Node xmlNode = nodes.item(i);
			if(xmlNode.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			SRelationMatchCondition local_sub_condition = parseSRelationMatchCondition(xmlNode);
			if(subcondition != null){
				throw new PepperModuleException("Not Match condition '"+node+"' has several conditions...");
			}else{
				subcondition = local_sub_condition;
			}
		}
		if(subcondition == null){
			throw new PepperModuleException("And Match condition '"+node+"' has no sub conditions...");
		}
		return new SRelationNotMatchCondition(subcondition);
	}
	
	public static SRelationSNameMatchCondition parseSRelationSNameMatchCondition(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node snameAttributeNode = attributes.getNamedItem(SREL_SNAME_REGEXP);
		String snameRegExp = null;
		if(snameAttributeNode != null){
			snameRegExp = snameAttributeNode.getNodeValue(); 
			attributes.removeNamedItem(SREL_SNAME_REGEXP);
		}else{
			throw new PepperModuleException("'"+SREL_SNAME_REGEXP + " attribute not found on SRelation SName Match Condition '"+node+"'");
		}
		if(attributes.getLength() != 0){
			throw new PepperModuleException("Additional unexpected attributes found on SRelation SName Match Condition '"+node+"'");
		}
		
		return new SRelationSNameMatchCondition(Pattern.compile(snameRegExp));
	}

	public static SRelationSTypeMatchCondition parseSRelationSTypeMatchCondition(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node stypeAttributeNode = attributes.getNamedItem(SREL_STYPE_REGEXP);
		String stypeRegExp = null;
		if(stypeAttributeNode != null){
			stypeRegExp = stypeAttributeNode.getNodeValue(); 
			attributes.removeNamedItem(SREL_STYPE_REGEXP);
		}else{
			throw new PepperModuleException("'"+SREL_STYPE_REGEXP + " attribute not found on SRelation SType Match Condition '"+node+"'");
		}
		if(attributes.getLength() != 0){
			throw new PepperModuleException("Additional unexpected attributes found on SRelation SType Match Condition '"+node+"'");
		}
		
		return new SRelationSTypeMatchCondition(Pattern.compile(stypeRegExp));
	}
	
	public static SRelationSLayerMatchCondition parseSRelationSLayerMatchCondition(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node slayerAttributeNode = attributes.getNamedItem(SREL_SLAYER_REGEXP);
		String slayerRegExp = null;
		if(slayerAttributeNode != null){
			slayerRegExp = slayerAttributeNode.getNodeValue(); 
			attributes.removeNamedItem(SREL_SLAYER_REGEXP);
		}else{
			throw new PepperModuleException("'"+SREL_SLAYER_REGEXP + " attribute not found on SRelation SLayer Match Condition '"+node+"'");
		}
		if(attributes.getLength() != 0){
			throw new PepperModuleException("Additional unexpected attributes found on SRelation SLayer Match Condition '"+node+"'");
		}
		
		return new SRelationSLayerMatchCondition(Pattern.compile(slayerRegExp));
	}
	
	public static SRelationSourceTypeMatchCondition parseSRelationSourceTypeMatchCondition(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node sourceTypeAttributeNode = attributes.getNamedItem(SREL_SOURCE_TYPE_REGEXP);
		String sourceTypeRegExp = null;
		if(sourceTypeAttributeNode != null){
			sourceTypeRegExp = sourceTypeAttributeNode.getNodeValue(); 
			attributes.removeNamedItem(SREL_SOURCE_TYPE_REGEXP);
		}else{
			throw new PepperModuleException("'"+SREL_SOURCE_TYPE_REGEXP + " attribute not found on SRelation SourceType Match Condition '"+node+"'");
		}
		if(attributes.getLength() != 0){
			throw new PepperModuleException("Additional unexpected attributes found on SRelation SourceType Match Condition '"+node+"'");
		}
		
		return new SRelationSourceTypeMatchCondition(Pattern.compile(sourceTypeRegExp));
	}
	
	public static SRelationTargetTypeMatchCondition parseSRelationTargetTypeMatchCondition(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node targetTypeAttributeNode = attributes.getNamedItem(SREL_TARGET_TYPE_REGEXP);
		String targetTypeRegExp = null;
		if(targetTypeAttributeNode != null){
			targetTypeRegExp = targetTypeAttributeNode.getNodeValue(); 
			attributes.removeNamedItem(SREL_TARGET_TYPE_REGEXP);
		}else{
			throw new PepperModuleException("'"+SREL_TARGET_TYPE_REGEXP + " attribute not found on SRelation TargetType Match Condition '"+node+"'");
		}
		if(attributes.getLength() != 0){
			throw new PepperModuleException("Additional unexpected attributes found on SRelation TargetType Match Condition '"+node+"'");
		}
		
		return new SRelationTargetTypeMatchCondition(Pattern.compile(targetTypeRegExp));
	}
}


/**
 * This class models a condition for performing a mapping over an attribute
 * @author Lionel Nicolas
 *
 */
class SAnnotationMapping{
	private String associatedSchemeName;// the name of the scheme into which the mapping should be performed
	private String associatedAttributeName; // the name of the attribute into which the mapping should be performed
	private SAnnotationMatchCondition condition;	
	
	SAnnotationMapping(SAnnotationMatchCondition condition,String associatedSchemeName, String associatedAttributeName){
		this.condition = condition;
		this.associatedSchemeName = associatedSchemeName;
		this.associatedAttributeName = associatedAttributeName;
	}
			
	public boolean isMatched(SAbstractAnnotation sannotation, EList<SLayer> sLayers) {
		System.out.println("Trying to map '"+sannotation+"' with "+this.associatedSchemeName+" and "+this.associatedAttributeName);
		boolean answer = this.condition.isMatched(sannotation,sLayers);
		return answer;
	}
	
	public String getAssociatedSchemeName(){
		return this.associatedSchemeName;
	}
	
	public String getAssociatedAttributeName(){
		return this.associatedAttributeName;
	}
	
	public String toString(){
		return this.associatedSchemeName+":"+this.associatedAttributeName;
	}
}

abstract class SAnnotationMatchCondition{
	public abstract boolean isMatched(SAbstractAnnotation sannotation, EList<SLayer> sLayers) ;
}

class SAnnotationStringValueMatchCondition extends SAnnotationMatchCondition{
	private Pattern stringValuePattern;
	
	public SAnnotationStringValueMatchCondition(Pattern stringValuePattern){
		this.stringValuePattern = stringValuePattern;
	}
	
	public boolean isMatched(SAbstractAnnotation sannotation, EList<SLayer> sLayers) {
		String stringValue = "";
		int dataType = sannotation.getSValueType().getValue();
		if(dataType == SDATATYPE.SBOOLEAN_VALUE){
			stringValue = ""+ sannotation.getSValueSBOOLEAN();
		}else if(dataType == SDATATYPE.SFLOAT_VALUE){
			stringValue = ""+sannotation.getSValueSFLOAT();
		}else if(dataType == SDATATYPE.SNUMERIC_VALUE){
			stringValue = ""+sannotation.getSValueSNUMERIC();
		}else if(dataType == SDATATYPE.STEXT_VALUE){
			stringValue = ""+sannotation.getSValueSTEXT();
		}else if(dataType == SDATATYPE.SURI_VALUE){
			stringValue = ""+sannotation.getSValueSURI();
		}else if(dataType == SDATATYPE.SOBJECT_VALUE){
			stringValue = ""+sannotation.getSValueSOBJECT().toString();
		}else{
			throw new PepperModuleException("Unknown type of SDATATYPE '"+dataType+"'");
		}
		return this.stringValuePattern.matcher(stringValue).matches();
	}
}



class SAnnotationAndMatchCondition extends SAnnotationMatchCondition{
	ArrayList<SAnnotationMatchCondition> subConditions;
	
	public SAnnotationAndMatchCondition(ArrayList<SAnnotationMatchCondition> subConditions){
		this.subConditions = subConditions;
	}
	
	public boolean isMatched(SAbstractAnnotation sannotation, EList<SLayer> sLayers) {
		for(SAnnotationMatchCondition scondition: this.subConditions){
			boolean answer = scondition.isMatched(sannotation,sLayers);
			System.out.println("Answer on "+scondition+" => "+answer);
			if(answer == false){
				return false;
			}
		}
		return true;
	}
}

class SAnnotationOrMatchCondition extends SAnnotationMatchCondition{
	ArrayList<SAnnotationMatchCondition> subConditions;
	
	public SAnnotationOrMatchCondition(ArrayList<SAnnotationMatchCondition> subConditions){
		this.subConditions = subConditions;
	}
	
	public boolean isMatched(SAbstractAnnotation sannotation, EList<SLayer> sLayers) {
		for(SAnnotationMatchCondition scondition: this.subConditions){
			boolean answer = scondition.isMatched(sannotation, sLayers);
			if(answer == true){
				return true;
			}
		}
		return false;
	}
}

class SAnnotationNotMatchCondition extends SAnnotationMatchCondition{
	SAnnotationMatchCondition subCondition;
	
	public SAnnotationNotMatchCondition(SAnnotationMatchCondition subCondition) {
		this.subCondition = subCondition;
	}
	
	public boolean isMatched(SAbstractAnnotation sannotation, EList<SLayer> sLayers) {
		return !this.subCondition.isMatched(sannotation, sLayers);
	}
}

class SAnnotationSNameMatchCondition extends SAnnotationMatchCondition{
	private Pattern sNamePattern;
	
	public SAnnotationSNameMatchCondition(Pattern sNamePattern){
		this.sNamePattern = sNamePattern;
	}
	
	public boolean isMatched(SAbstractAnnotation sannotation, EList<SLayer> sLayers){
		return this.sNamePattern.matcher(sannotation.getSName()).matches();
	}
}

class SAnnotationNameSpaceMatchCondition extends SAnnotationMatchCondition{
	private Pattern nameSpacePattern;
	
	public SAnnotationNameSpaceMatchCondition(Pattern nameSpacePattern){
		this.nameSpacePattern = nameSpacePattern;
	}
	
	public boolean isMatched(SAbstractAnnotation sannotation, EList<SLayer> sLayers){
		return this.nameSpacePattern.matcher(sannotation.getNamespace()).matches();
	}
}

class SAnnotationSLayerMatchCondition extends SAnnotationMatchCondition{
	private Pattern sLayerNamePattern;
	
	public SAnnotationSLayerMatchCondition(Pattern sLayerNamePattern){
		this.sLayerNamePattern = sLayerNamePattern;
	}
	
	public boolean isMatched(SAbstractAnnotation sannotation, EList<SLayer> sLayers){
		boolean answer = true;
		boolean hasMatchedSomething = false;
		for(SLayer sLayer: sLayers){
			hasMatchedSomething = true;
			answer = answer && this.sLayerNamePattern.matcher(sLayer.getSName()).matches();
		}	
		return  hasMatchedSomething && answer;
	}
}


/**
 * This class models a condition for performing a mapping over a SRelation
 * @author Lionel Nicolas
 *
 */
class SRelationMapping{
	private String sourceAssociatedSchemeName;// the name of the scheme into which a SContainer for the source node of the SRelation should be mapped
	private String targetAssociatedSchemeName;// the name of the scheme into which a SContainer for the target node of the SRelation should be mapped
	private String associatedPointerAttributeName;// the name of the pointer attribute of the SContainer of the source node that will represent the SRelation 
	private SRelationMatchCondition matchCondition;
	
	
	public SRelationMapping(SRelationMatchCondition matchCondition,String sourceAssociatedSchemeName, String targetAssociatedSchemeName, String associatedPointerAttributeName){
		this.matchCondition = matchCondition;
		this.sourceAssociatedSchemeName = sourceAssociatedSchemeName;
		this.targetAssociatedSchemeName = targetAssociatedSchemeName;
		this.associatedPointerAttributeName = associatedPointerAttributeName;
	}
		
	public boolean isMatched(SRelation srelation){
		return this.matchCondition.isMatched(srelation);
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
		return this.sourceAssociatedSchemeName+":"+this.associatedPointerAttributeName+" ==> "+this.targetAssociatedSchemeName;
	}
}	


abstract class SRelationMatchCondition{
	public abstract boolean isMatched(SRelation srelation);
}

class SRelationAndMatchCondition extends SRelationMatchCondition{
	ArrayList<SRelationMatchCondition> subConditions;
	
	public SRelationAndMatchCondition(ArrayList<SRelationMatchCondition> subConditions){
		this.subConditions = subConditions;
	}
	
	public boolean isMatched(SRelation srelation){
		for(SRelationMatchCondition scondition: this.subConditions){
			boolean answer = scondition.isMatched(srelation);
			if(answer == false){
				return false;
			}
		}
		return true;
	}
}

class SRelationOrMatchCondition extends SRelationMatchCondition{
	ArrayList<SRelationMatchCondition> subConditions;
	
	public SRelationOrMatchCondition(ArrayList<SRelationMatchCondition> subConditions){
		this.subConditions = subConditions;
	}
	
	public boolean isMatched(SRelation srelation){
		for(SRelationMatchCondition scondition: this.subConditions){
			boolean answer = scondition.isMatched(srelation);
			if(answer == true){
				return true;
			}
		}
		return false;
	}
}

class SRelationNotMatchCondition extends SRelationMatchCondition{
	SRelationMatchCondition subCondition;
	
	public SRelationNotMatchCondition(SRelationMatchCondition subCondition){
		this.subCondition = subCondition;
	}
	
	public boolean isMatched(SRelation srelation){
		return !this.subCondition.isMatched(srelation);
	}
}

class SRelationSTypeMatchCondition extends SRelationMatchCondition{
	private Pattern typeNamePattern;
	
	public SRelationSTypeMatchCondition(Pattern typeNamePattern){
		this.typeNamePattern = typeNamePattern;
	}
	
	public boolean isMatched(SRelation srelation){
		boolean answer = true;
		boolean hasMatchedSomething = false;
		for(String stype: srelation.getSTypes()){
			hasMatchedSomething = true;
			answer = answer && this.typeNamePattern.matcher(stype).matches();
		}	
		return  hasMatchedSomething && answer;
	}
}


class SRelationSLayerMatchCondition extends SRelationMatchCondition{
	private Pattern sLayerNamePattern;
	
	public SRelationSLayerMatchCondition(Pattern sLayerNamePattern){
		this.sLayerNamePattern = sLayerNamePattern;
	}
	
	public boolean isMatched(SRelation srelation){
		boolean answer = true;
		boolean hasMatchedSomething = false;
		for(SLayer sLayer: srelation.getSLayers()){
			hasMatchedSomething = true;
			answer = answer && this.sLayerNamePattern.matcher(sLayer.getSName()).matches();
		}	
		return  hasMatchedSomething && answer;
	}
}

class SRelationSNameMatchCondition extends SRelationMatchCondition{
	private Pattern sNamePattern;
	
	public SRelationSNameMatchCondition(Pattern sNamePattern){
		this.sNamePattern = sNamePattern;
	}
	
	public boolean isMatched(SRelation srelation){
		return this.sNamePattern.matcher(srelation.getSName()).matches();
	}
}

class SRelationSourceTypeMatchCondition extends SRelationMatchCondition{
	private Pattern sourceTypeNamePattern;
	
	public SRelationSourceTypeMatchCondition(Pattern sourceTypeNamePattern){
		this.sourceTypeNamePattern = sourceTypeNamePattern;
	}
	
	public boolean isMatched(SRelation srelation){
		return this.sourceTypeNamePattern.matcher(srelation.getSSource().getClass().getName().substring(1)).matches();
	}
}

class SRelationTargetTypeMatchCondition extends SRelationMatchCondition{
	private Pattern targetTypeNamePattern;
	
	public SRelationTargetTypeMatchCondition(Pattern targetTypeNamePattern){
		this.targetTypeNamePattern = targetTypeNamePattern;
	}
	
	public boolean isMatched(SRelation srelation){
		return this.targetTypeNamePattern.matcher(srelation.getSTarget().getClass().getName().substring(1)).matches();
	}
}






