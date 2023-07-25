package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import java.lang.reflect.Constructor;
import java.security.spec.InvalidParameterSpecException;
import java.util.Collection;
import java.util.Properties;

import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordDictionary;
import org.apache.ctakes.dictionary.lookup2.term.RareWordTerm;
import org.apache.ctakes.dictionary.lookup2.util.FastLookupToken;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;

/**
 * Wrapping for dictionaries from repository.
 * Uses dictionary from repository if it exists or creates a new dictionary if it doesn't exist
 */
public class DictionaryWrapper implements RareWordDictionary {

    private static final String DICT_IMPLEMENTATION = "dictionaryImplementation";

    private RareWordDictionary wrappedDictionary;

    public DictionaryWrapper(final String name, final UimaContext uimaContext, final Properties properties)
            throws InvalidParameterSpecException {

        DictionariesRepository repository = DictionariesRepository.getInstance();
        // get the dictionary from the repository or create new
        if (repository.hasDictionary(name)) {
        	if (DEBUG)  LOGGER.debug("Using repository rare word dictionary: " + name);
        	
            wrappedDictionary = repository.getDictionary(name);
        } else {
        	if (DEBUG)  LOGGER.debug("Creating new rare word dictionary: " + name);
        	
            String impl = properties.getProperty(DICT_IMPLEMENTATION);
            try {
                Class<?> c = Class.forName(impl);
                if (!RareWordDictionary.class.isAssignableFrom(c)){
                    throw new Exception("Dictionary implementation is not RareWordDictionary");
                }
                Constructor<?> ctor = c.getConstructor(String.class, UimaContext.class, Properties.class);
                wrappedDictionary = (RareWordDictionary) ctor.newInstance(name, uimaContext, properties);
                repository.addDictionary(wrappedDictionary);
            } catch (Exception e) {
                throw new InvalidParameterSpecException("Invalid dictionary implementation");
            }
        }


    }

    @Override
    public String getName() {
        return wrappedDictionary.getName();
    }

    @Override
    public Collection<RareWordTerm> getRareWordHits(FastLookupToken fastLookupToken) {
        return wrappedDictionary.getRareWordHits(fastLookupToken);
    }

    @Override
    public Collection<RareWordTerm> getRareWordHits(String rareWordText) {
        return wrappedDictionary.getRareWordHits(rareWordText);
    }
    
    static private final Logger LOGGER = Logger.getLogger(DictionaryWrapper.class);
    static private final boolean DEBUG = LOGGER.isDebugEnabled();
}
