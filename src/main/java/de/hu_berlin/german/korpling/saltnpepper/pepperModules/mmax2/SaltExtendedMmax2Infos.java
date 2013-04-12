package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import eurac.commul.annotations.mmax2wrapper.Mmax2Infos;

public class SaltExtendedMmax2Infos extends Mmax2Infos {
	
	//SALT_INFO
	public static final String SALT_INFO_FOLDER = "SaltInfo";
	public static final String SALT_INFO_FILE_ENDING = ".xml";

	public static final String SALT_INFOS_NODE_NAME = "saltInfos";
	public static final String SALT_INFO_NODE_NAME = "saltInfo";
	public static final String SALT_INFO_ID_ATTR_NAME = "id";
	public static final String SALT_INFO_STYPE_ATTR_NAME = "salt_type";
	public static final String SALT_INFO_SNAME_ATTR_NAME = "salt_name";
	public static final String SALT_INFO_SID_ATTR_NAME = "salt_id";
	
	public static final String SALT_INFO_TYPE_SDOCUMENT = "SDocument";
	public static final String SALT_INFO_TYPE_SDOCUMENT_GRAPH = "SDocumentGraph";
	public static final String SALT_INFO_TYPE_SLAYER = "SLayer";
	public static final String SALT_INFO_TYPE_STEXTUALDS = "STextualDS";
	public static final String SALT_INFO_TYPE_SSPAN = "SSpan";
	public static final String SALT_INFO_TYPE_SSPANNING_REL = "SSpanningRel";
	public static final String SALT_INFO_TYPE_STOKEN = "SToken";
	public static final String SALT_INFO_TYPE_STEXTUAL_REL = "STextualRel";
	public static final String SALT_INFO_TYPE_SSTRUCT = "SStruct";
	public static final String SALT_INFO_TYPE_SDOMINANCE_REL = "SDominanceRel";
	public static final String SALT_INFO_TYPE_SPOINTING_REL = "SPointingRel";
	public static final String SALT_INFO_TYPE_SANNOTATION = "SAnnotation";
	public static final String SALT_INFO_TYPE_SMETAANNOTATION = "SMetaAnnotation";
	public static final String SALT_INFO_TYPE_SLAYER_LINK = "SLayer_link";	
	public static final String SALT_INFO_TYPE_STYPE_LINK = "SType_link";	
}
