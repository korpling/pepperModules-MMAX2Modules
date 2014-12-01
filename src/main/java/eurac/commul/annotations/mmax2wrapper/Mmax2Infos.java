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
package eurac.commul.annotations.mmax2wrapper;


/**
 * This class is meant to contain all declarative, static and final variables.
 * @author Lionel Nicolas
 */
public class Mmax2Infos {
	
	// CORPUS
	public static final String COMMON_PATH_FILE = "common_paths.xml";
	
	public static final String COMMON_PATH_BASEDATA_PATH_NODE_NAME = "basedata_path";
	public static final String COMMON_PATH_SCHEME_PATH_NODE_NAME   = "scheme_path";
	public static final String COMMON_PATH_MARKABLE_PATH_NODE_NAME = "markable_path";
	public static final String COMMON_PATH_STYLE_PATH_NODE_NAME = "style_path";
	public static final String COMMON_PATH_CUSTOMIZATION_PATH_NODE_NAME = "customization_path";
	public static final String COMMON_PATH_ANNOTATIONS_NODE_NAME = "annotations";
	
	public static final String COMMON_PATH_LEVEL_NODE_NAME = "level";
	public static final String COMMON_PATH_LEVEL_SCHEME_ATTR_NAME = "schemefile";
	public static final String COMMON_PATH_LEVEL_SCHEME_NAME_ATTR_NAME = "name";
	
	public static final String COMMON_PATH_USER_SWITCHES_NODE_NAME = "user_switches";
	public static final String COMMON_PATH_USER_SWITCH_NODE_NAME = "user_switch";
	public static final String COMMON_PATH_USER_SWITCH_NAME_ATTR_NAME = "name";
	public static final String COMMON_PATH_USER_SWITCH_ACTIVE_ATTR_NAME = "default";
	
	// CUSTOMIZATIONS
	public static final String CUSTOMIZATIONS_FOLDER = "Customizations" ;
	public static final String CUSTOM_FILE_ENDING = "_customization.xml";
	
	// DOCUMENT
	public static final String DOCUMENT_BASEDATA_FOLDER = "Basedata";
	public static final String DOCUMENT_WORDS_FILE_ENDING = "_words.xml";
	public static final String DOCUMENT_WORDS_DTD = "words.dtd";
	public static final String DOCUMENT_FILE_ENDING = ".mmax";
	
	public static final String DOCUMENT_WORDS_NODE_NAME = "words";
	public static final String WORDS_WORD_NODE_NAME = "word";
	public static final String WORD_ID_ATTR_NAME = "id";
	
	// MARKABLE
	public static final String MARKABLES_FOLDER = "Markables";
	public static final String MARKABLES_DTD = "markables.dtd";
	public static final String MARKABLES_FILE_ENDING = "_level.xml";
	
	public static final String MARKABLE_NODE_NAME = "markable";
	public static final String MARKABLE_ID_ATTR_NAME = "id";
	public static final String MARKABLE_SPAN_ATTR_NAME = "span";
	public static final String MARKABLE_LEVEL_ATTR_NAME = "mmax_level";
	
	// SCHEME
	public static final String SCHEMES_FOLDER = "Schemes";
	public static final String SCHEMES_FILE_ENDING = "_scheme.xml";
	
	public static final String SCHEME_ATTR_NAME = "attribute";
	public static final String SCHEME_ID_ATTR_NAME = "id";
	public static final String SCHEME_NAME_ATTR_NAME = "name";
	public static final String SCHEME_TYPE_ATTR_NAME = "type";
	public static final String SCHEME_TARGET_DOMAIN_ATTR_NAME = "target_domain";
	public static final String SCHEME_NOMINAL_ATTR_VALUE_NAME = "value";
	public static final String SCHEME_NOMINAL_ATTR_VALUE_NAME_NAME = "name";
		
	public static final String SCHEME_NOMINAL_LIST_ATTR_TYPE = "nominal_list";
	public static final String SCHEME_NOMINAL_BUTTON_ATTR_TYPE = "nominal_button";
	public static final String SCHEME_FREETEXT_ATTR_TYPE = "freetext";
	public static final String SCHEME_POINTER_ATTR_TYPE = "markable_pointer";
	public static final String SCHEME_SET_ATTR_TYPE = "markable_set";
	
	public static final String SCHEME_POINTER_ID_PREFIX = "markable_";
	
	// STYLE
	public static final String STYLES_FOLDER = "Styles" ;
	public static final String STYLE_DEFAULT_FILE_PATH = "default.xsl" ;
}
