package de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperExporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperExporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.mmax2.SaltExtendedCorpusFactory.SaltExtendedCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import eurac.commul.annotations.mmax2wrapper.SchemeFactory;

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
	private SchemeFactory schemeFactory;

	public MMAX2Exporter() {
		super();

		// setting name of module
		setName("MMAX2Exporter");
		setProperties(new MMAX2ExporterProperties());
		// set list of formats supported by this module
		this.addSupportedFormat("mmax2", "1.0", null);
	}

	@Override
	public PepperMapper createPepperMapper(SElementId sElementId) {
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new PepperModuleException(this, "", e);
		}
		SaltExtendedCorpusFactory corpusFactory = new SaltExtendedCorpusFactory(documentBuilder);
		SaltExtendedCorpus corpus = corpusFactory.newEmptyCorpus(new File(this.getCorpusDesc().getCorpusPath().toFileString()));
		schemeFactory = new SchemeFactory(corpus, documentBuilder);

		Salt2MMAX2Mapper mapper = null;
		if (sElementId.getSIdentifiableElement() != null) {
			mapper = new Salt2MMAX2Mapper();
			if (sElementId.getSIdentifiableElement() instanceof SCorpus) {
				mapper.setProperties(getProperties());
				mapper.setDocumentBuilder(documentBuilder);
			} else if (sElementId.getSIdentifiableElement() instanceof SDocument) {
				SaltExtendedDocumentFactory documentFactory = new SaltExtendedDocumentFactory(corpus, documentBuilder);
				mapper.setProperties(getProperties());
				mapper.setDocumentBuilder(documentBuilder);

				mapper.setFactory(documentFactory);
				mapper.setCorpus(corpus);
				mapper.setSchemeFactory(schemeFactory);
			}
		}
		return (mapper);
	}
}
