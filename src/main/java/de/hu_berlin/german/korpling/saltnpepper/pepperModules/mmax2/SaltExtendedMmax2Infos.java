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

import eurac.commul.annotations.mmax2wrapper.Mmax2Infos;


/**
 * This class is meant to contain all declarative, static and final variables.
 * @author Lionel Nicolas
 */
public class SaltExtendedMmax2Infos extends Mmax2Infos {
	
	static final String SALT_INFO_FOLDER = "SaltInfo";
	static final String SALT_INFO_FILE_ENDING = ".xml";

	static final String SALT_INFOS_NODE_NAME = "saltInfos";
	static final String SALT_INFO_NODE_NAME = "saltInfo";
	static final String SALT_INFO_ID_ATTR_NAME = "id";
	static final String SALT_INFO_STYPE_ATTR_NAME = "salt_type";
	static final String SALT_INFO_SNAME_ATTR_NAME = "salt_name";
	static final String SALT_INFO_SID_ATTR_NAME = "salt_id";
	static final String SALT_INFO_CONTAINED_ID_ATTR_NAME = "contained_id";
	static final String SALT_INFO_CONTAINED_SCHEME_ATTR_NAME = "contained_scheme";
	
	static final String SALT_INFO_TYPE_SDOCUMENT = "SDocument";
	static final String SALT_INFO_TYPE_SDOCUMENT_GRAPH = "SDocumentGraph";
	static final String SALT_INFO_TYPE_SLAYER = "SLayer";
	static final String SALT_INFO_TYPE_STEXTUALDS = "STextualDS";
	static final String SALT_INFO_TYPE_SSPAN = "SSpan";
	static final String SALT_INFO_TYPE_SSPANNING_REL = "SSpanningRel";
	static final String SALT_INFO_TYPE_STOKEN = "SToken";
	static final String SALT_INFO_TYPE_STEXTUAL_REL = "STextualRel";
	static final String SALT_INFO_TYPE_SSTRUCT = "SStruct";
	static final String SALT_INFO_TYPE_SDOMINANCE_REL = "SDominanceRel";
	static final String SALT_INFO_TYPE_SPOINTING_REL = "SPointingRel";
	static final String SALT_INFO_TYPE_SANNOTATION = "SAnnotation";
	static final String SALT_INFO_TYPE_SCONTAINER = "SContainer";
	static final String SALT_INFO_TYPE_SMETAANNOTATION = "SMetaAnnotation";
	static final String SALT_INFO_TYPE_SLAYER_LINK = "SLayer_link";	
	static final String SALT_INFO_TYPE_STYPE_LINK = "SType_link";	
}
