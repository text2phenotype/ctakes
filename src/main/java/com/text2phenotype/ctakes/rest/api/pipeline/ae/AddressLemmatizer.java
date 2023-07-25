package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AddressLemmatizer extends JCasAnnotator_ImplBase {

    private final Logger LOGGER = Logger.getLogger(AddressLemmatizer.class);
    @ConfigurationParameter(
            name = "streetLemmas",
            description = "File with street type lemmas",
            mandatory = true,
            defaultValue = "com/text2phenotype/ctakes/resources/npi/streetType.bsv"
    )
    private String streetLemmas;

    private Map<String, String> lemmas = new HashMap<>();

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        try {
            Path p = Paths.get(FileLocator.getFullPath(streetLemmas));
            Files.lines(p).forEach(line -> {
                String[] parts = line.split("\\|");
                if (parts.length < 2) {
                    LOGGER.error("Bad street lemma: " + line);
                }

                lemmas.put(parts[0].toLowerCase(), parts[1].toLowerCase());
            });
        } catch (Exception ex) {
            throw new ResourceInitializationException(ex);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        Collection<WordToken> words = JCasUtil.select(aJCas, WordToken.class);
        for (WordToken word : words) {
            String txt = word.getCoveredText().toLowerCase();
            if (lemmas.containsKey(txt)) {
                word.setCanonicalForm(lemmas.get(txt));
            }
        }
    }
}
