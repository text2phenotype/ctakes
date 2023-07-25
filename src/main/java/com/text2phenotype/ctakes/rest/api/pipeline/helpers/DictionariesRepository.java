package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordDictionary;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository of dictionaries
 */
public class DictionariesRepository {

    private static DictionariesRepository instance;

    public static DictionariesRepository getInstance() {
        if (instance == null) {
            instance = new DictionariesRepository();
        }

        return instance;
    }

    /**
     * Dictionaries cache
     */
    private Map<String, RareWordDictionary> dictionaries;

    private DictionariesRepository() {
        dictionaries = new HashMap<>();
    }

    /**
     * Check if the dictionary in the repository
     * @param dictionaryName Dictionary name
     * @return
     */
    public boolean hasDictionary(String dictionaryName) {
        return dictionaries.containsKey(dictionaryName);
    }


    /**
     * Get the dictionary instance
     * @param dictionaryName Dictionary name
     * @return
     */
    public RareWordDictionary getDictionary(String dictionaryName) {
        return dictionaries.get(dictionaryName);
    }

    /**
     * Add the dictionary to the repository
     * @param dictionary Dictionary instance
     * @return
     */
    public boolean addDictionary(RareWordDictionary dictionary) {
        if (!hasDictionary(dictionary.getName())) {
            dictionaries.put(dictionary.getName(), dictionary);
            return true;
        }

        return false;
    }
}
