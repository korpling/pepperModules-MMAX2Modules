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

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.corpus_tools.pepper.impl.PepperExporterImpl;
import org.corpus_tools.pepper.modules.PepperExporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.MMAX2WrapperException;
import edu.eurac.commul.annotations.mmax2.mmax2wrapper.SchemeFactory;

/**
 * This class exports data from Salt to the MMAX2 format. The code has been
 * adapted from PAULAExporter.
 * 
 * @author Lionel Nicolas
 * @author FLorian Zipser (just adaptations)
 * 
 */
@Component(name = "MMAX2ExporterComponent", factory = "PepperExporterComponentFactory")
public class MMAX2Exporter extends PepperExporterImpl implements PepperExporter {
	

	public MMAX2Exporter() {
		super();

		// setting name of module
		setName("MMAX2Exporter");
		setProperties(new MMAX2ExporterProperties());
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-MMAX2Modules"));
		setDesc("The MMAX2Exporter maps a Salt model to the MMAX2 format.");
		// set list of formats supported by this module
		this.addSupportedFormat("mmax2", "1.0", null);
	}

	private SaltExtendedCorpus corpus;
	private SchemeFactory schemeFactory;
	private SaltExtendedDocumentFactory documentFactory;
	private ArrayList<SAnnotationMapping> sannotationMappings;
	private ArrayList<SRelationMapping> srelationsMappings;

	
	@Override
	public void exportCorpusStructure(){
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new PepperModuleException(this, "", e);
		}
		
		MMAX2ExporterProperties props = (MMAX2ExporterProperties) getProperties();
		try {
			this.sannotationMappings = Salt2MMAXMapping.getSAnnotationMappingsFromFile(props);
			this.srelationsMappings = Salt2MMAXMapping.getSRelationMappingsFromFile(props);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.corpus = new SaltExtendedCorpusFactory(documentBuilder).newEmptyCorpus(this.getCorpusDesc().getCorpusPath().toFileString());
		this.schemeFactory = new SchemeFactory(this.corpus, documentBuilder);
		this.documentFactory = new SaltExtendedDocumentFactory(this.corpus,documentBuilder);
		
		try {
			SaltExtendedFileGenerator.initializeCorpus(this.corpus);
		} catch (IOException e) {
			throw new PepperModuleException(this, "", e);
		} catch (MMAX2WrapperException e) {
			throw new PepperModuleException(this, "", e);
		}
	}
	
	
	@Override
	public PepperMapper createPepperMapper(Identifier sElementId) {
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new PepperModuleException(this, "", e);
		}

		Salt2MMAX2Mapper mapper = null;
		if (sElementId.getIdentifiableElement() != null) {
			mapper = new Salt2MMAX2Mapper(documentBuilder,schemeFactory,documentFactory,this.sannotationMappings,this.srelationsMappings);
		}
		return (mapper);
	}
	
	@Override
	public void end() throws PepperModuleException{
		try {
			SaltExtendedFileGenerator.finalizeCorpus(this.corpus);
		} catch (Exception e) {
			throw new PepperModuleException(this, "", e);
		} 
	}	
}
