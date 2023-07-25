package com.text2phenotype.ctakes.rest.utils;

import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.util.collection.CollectionMap;
import org.apache.ctakes.dictionary.lookup2.dictionary.RareWordTermMapCreator;
import org.apache.ctakes.dictionary.lookup2.term.RareWordTerm;
import org.apache.ctakes.dictionary.lookup2.util.CuiCodeUtil;
import org.apache.ctakes.dictionary.lookup2.util.LookupUtil;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Converts NPI BSV dictionary to HSQLDB format.
 */
public class NPIDictionaryConverter {

    static final private Logger LOGGER = Logger.getLogger( "NPIDictionaryConverter" );
    private static final int BATCH_SIZE = 10000;
    private static void writeHeader(Path scriptPath) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("SET DATABASE UNIQUE NAME HSQLDB5F3E2F483C\n");
        sb.append("SET DATABASE GC 0\n");
        sb.append("SET DATABASE DEFAULT RESULT MEMORY ROWS 0\n");
        sb.append("SET DATABASE EVENT LOG LEVEL 0\n");
        sb.append("SET DATABASE TRANSACTION CONTROL LOCKS\n");
        sb.append("SET DATABASE DEFAULT ISOLATION LEVEL READ COMMITTED\n");
        sb.append("SET DATABASE TRANSACTION ROLLBACK ON CONFLICT TRUE\n");
        sb.append("SET DATABASE TEXT TABLE DEFAULTS ''\n");
        sb.append("SET DATABASE SQL NAMES FALSE\n");
        sb.append("SET DATABASE SQL REFERENCES FALSE\n");
        sb.append("SET DATABASE SQL SIZE TRUE\n");
        sb.append("SET DATABASE SQL TYPES FALSE\n");
        sb.append("SET DATABASE SQL TDC DELETE TRUE\n");
        sb.append("SET DATABASE SQL TDC UPDATE TRUE\n");
        sb.append("SET DATABASE SQL TRANSLATE TTI TYPES TRUE\n");
        sb.append("SET DATABASE SQL TRANSLATE TTI TYPES TRUE\n");
        sb.append("SET DATABASE SQL CONCAT NULLS TRUE\n");
        sb.append("SET DATABASE SQL UNIQUE NULLS TRUE\n");
        sb.append("SET DATABASE SQL CONVERT TRUNCATE TRUE\n");
        sb.append("SET DATABASE SQL AVG SCALE 0\n");
        sb.append("SET DATABASE SQL DOUBLE NAN TRUE\n");
        sb.append("SET FILES WRITE DELAY 10\n");
        sb.append("SET FILES BACKUP INCREMENT TRUE\n");
        sb.append("SET FILES CACHE SIZE 10000\n");
        sb.append("SET FILES CACHE ROWS 50000\n");
        sb.append("SET FILES SCALE 32\n");
        sb.append("SET FILES LOB SCALE 32\n");
        sb.append("SET FILES DEFRAG 0\n");
        sb.append("SET FILES NIO TRUE\n");
        sb.append("SET FILES NIO SIZE 8192\n");
        sb.append("SET FILES LOG TRUE\n");
        sb.append("SET FILES LOG SIZE 50\n");
        sb.append("CREATE USER SA PASSWORD DIGEST 'd41d8cd98f00b204e9800998ecf8427e'\n");
        sb.append("ALTER USER SA SET LOCAL TRUE\n");
        sb.append("CREATE SCHEMA PUBLIC AUTHORIZATION DBA\n");
        sb.append("SET SCHEMA PUBLIC\n");
        sb.append("CREATE MEMORY TABLE PUBLIC.CUI_TERMS(NPI BIGINT,RINDEX INTEGER,TCOUNT INTEGER,TEXT VARCHAR(255),RWORD VARCHAR(48),MATCH_TYPE INTEGER)\n");
        sb.append("CREATE INDEX IDX_CUI_TERMS ON PUBLIC.CUI_TERMS(RWORD)\n");
        sb.append("CREATE MEMORY TABLE PUBLIC.ATTRS(NPI BIGINT, PREFTERM VARCHAR(100), TUI INTEGER, MAILING_STREET1 VARCHAR(100), MAILING_STREET2 VARCHAR(100), MAILING_CITY VARCHAR(40), MAILING_STATE VARCHAR(40), MAILING_PHONE BIGINT, MAILING_FAX BIGINT, MAILING_ZIP BIGINT, PHYSICAL_STREET1 VARCHAR(100), PHYSICAL_STREET2 VARCHAR(100), PHYSICAL_CITY VARCHAR(40), PHYSICAL_STATE VARCHAR(40), PHYSICAL_PHONE BIGINT, PHYSICAL_FAX BIGINT, PHYSICAL_ZIP BIGINT)\n");
        sb.append("CREATE INDEX IDX_NPI ON PUBLIC.ATTRS(NPI)\n");
        sb.append("CREATE INDEX IDX_M_PHONE ON PUBLIC.ATTRS(MAILING_PHONE)\n");
        sb.append("CREATE INDEX IDX_M_FAX ON PUBLIC.ATTRS(MAILING_FAX)\n");
        sb.append("CREATE INDEX IDX_P_PHONE ON PUBLIC.ATTRS(PHYSICAL_PHONE)\n");
        sb.append("CREATE INDEX IDX_P_FAX ON PUBLIC.ATTRS(PHYSICAL_FAX)\n");
        sb.append("ALTER SEQUENCE SYSTEM_LOBS.LOB_ID RESTART WITH 1\n");
        sb.append("SET DATABASE DEFAULT INITIAL SCHEMA PUBLIC\n");
        sb.append("GRANT USAGE ON DOMAIN INFORMATION_SCHEMA.SQL_IDENTIFIER TO PUBLIC\n");
        sb.append("GRANT USAGE ON DOMAIN INFORMATION_SCHEMA.YES_OR_NO TO PUBLIC\n");
        sb.append("GRANT USAGE ON DOMAIN INFORMATION_SCHEMA.TIME_STAMP TO PUBLIC\n");
        sb.append("GRANT USAGE ON DOMAIN INFORMATION_SCHEMA.CARDINAL_NUMBER TO PUBLIC\n");
        sb.append("GRANT USAGE ON DOMAIN INFORMATION_SCHEMA.CHARACTER_DATA TO PUBLIC\n");
        sb.append("GRANT DBA TO SA\n");
        sb.append("SET SCHEMA SYSTEM_LOBS\n");
        sb.append("INSERT INTO BLOCKS VALUES(0,2147483647,0)\n");
        sb.append("SET SCHEMA PUBLIC");

        Files.write(scriptPath, sb.toString().getBytes());

    }

    private static int getKey(String cui, String txt) {
        return String.format("%s_%s", cui, txt.toUpperCase().replaceAll("\\W", "")).hashCode();
    }

    public static void main(String[] args) throws IOException {

        String path = FileLocator.getFullPath("com/text2phenotype/ctakes/resources/dictionaries/npi");
        String synPath = Paths.get(path, "npi_synonyms.bsv").toString();
        String attrPath = Paths.get(path, "npi_attrs.bsv").toString();

        Path scriptPath = Paths.get(path, "npi.script");

        LOGGER.info("Start NPI dictionary compilation");
        LOGGER.info("Write script header");
        writeHeader(scriptPath);

        // calculate rare word and save to HSQL
        LOGGER.info("Reading synonyms BSV");
        final Set<RareWordTermMapCreator.CuiTerm> cuiTerms = new HashSet<>();
        final Map<Integer, Integer> termMatchType = new HashMap<>();
        long cuiCnt = 0;
        Map<String, String> NPI_CUI_map = new HashMap<>();
        final BufferedReader reader = new BufferedReader( new InputStreamReader( FileLocator.getAsStream( synPath )));
        String line = reader.readLine();
        while ( line != null ) {
            if (!line.startsWith("//")) {
                String[] parts = LookupUtil.fastSplit(line.toLowerCase(), '|');
                String npi = parts[0];
                String match_type = parts[1];
                String txt = parts[2];
                String cui = CuiCodeUtil.getInstance().getAsCui(cuiCnt);
                if (NPI_CUI_map.containsKey(npi)) {
                    cui = NPI_CUI_map.get(npi);
                } else {
                    NPI_CUI_map.put(npi, cui);
                    NPI_CUI_map.put(cui, npi);
                    cuiCnt = cuiCnt + 1;
                }
                RareWordTermMapCreator.CuiTerm cuiTerm = new RareWordTermMapCreator.CuiTerm(cui, txt);
                cuiTerms.add(cuiTerm);
                int key = getKey(cui, txt.toUpperCase());
                termMatchType.put(key, Integer.parseInt(match_type));
            }
            line = reader.readLine();
        }

        LOGGER.info("Rare words calculation");
        CollectionMap<String, RareWordTerm, List<RareWordTerm>> rareWords = RareWordTermMapCreator.createRareWordTermMap(cuiTerms);

        LOGGER.info("Insert rare words data to the script file");
        StringBuilder rareLines = new StringBuilder();
        int cnt = 0; // Hack: split to portions to reduce memory consumption
        for (String rw: rareWords.keySet()) {
            List<RareWordTerm> rareTerms = rareWords.get(rw);
            for (RareWordTerm rareTerm : rareTerms) {
                String cui = CuiCodeUtil.getInstance().getAsCui(rareTerm.getCuiCode());
                String npi = NPI_CUI_map.get(cui);
                int key = getKey(cui, rareTerm.getText().toUpperCase());
                int match_type = termMatchType.get(key);
                rareLines.append(
                        String.format("\nINSERT INTO CUI_TERMS VALUES(%s,%d,%d,'%s','%s',%s)",
                                npi,
                                rareTerm.getRareWordIndex(),
                                rareTerm.getTokenCount(),
                                rareTerm.getText().replace("'", "''"),
                                rareTerm.getRareWord(),
                                match_type
                        )
                );
            }

            cnt = cnt + 1;
            if (cnt > BATCH_SIZE) {
                Files.write(scriptPath, rareLines.toString().getBytes(), StandardOpenOption.APPEND);
                cnt = 0;
                rareLines.delete(0, rareLines.length());
            }
        }

        Files.write(scriptPath, rareLines.toString().getBytes(), StandardOpenOption.APPEND);
        LOGGER.info("Insert attributes data to the script file");
        // write attributes
        StringBuilder attrsLines = new StringBuilder();
        cnt = 0;
        final BufferedReader attr_reader = new BufferedReader( new InputStreamReader( FileLocator.getAsStream( attrPath )));
        String attr_line = attr_reader.readLine();
        while ( attr_line != null ) {
            if (!attr_line.startsWith("//")) {
                String[] parts = LookupUtil.fastSplit(attr_line.replace("'", "''"), '|');
                // NPI|TUI|PREF_TEXT|MAILING_STREET1|MAILING_STREET2|MAILING_CITY|MAILING_STATE|MAILING_PHONE|MAILING_FAX|MAILING_ZIP|PHYSICAL_STREET1|PHYSICAL_STREET2|PHYSICAL_CITY|PHYSICAL_STATE|PHYSICAL_PHONE|PHYSICAL_FAX|PHYSICAL_ZIP
                String npi = parts[0];
                String tui = parts[1];
                String pref_txt = parts[2];

                String mailing_street1 = parts[3];
                String mailing_street2 = parts[4];
                String mailing_city = parts[5];
                String mailing_state = parts[6];
                String mailing_phone = parts[7].isEmpty() ? "0" : parts[7];
                String mailing_fax = parts[8].isEmpty() ? "0" : parts[8];
                String mailing_zip = parts[9].isEmpty() ? "0" : parts[9];

                String physical_street1 = parts[10];
                String physical_street2 = parts[11];
                String physical_city = parts[12];
                String physical_state = parts[13];
                String physical_phone = parts[14].isEmpty() ? "0" : parts[14];
                String physical_fax = parts[15].isEmpty() ? "0" : parts[15];;
                String physical_zip = (parts.length < 17 || parts[16].isEmpty()) ? "0" : parts[16];

                attrsLines.append(
                        String.format("\nINSERT INTO ATTRS VALUES(%s,'%s',%s,'%s','%s','%s','%s',%s,%s,%s,'%s','%s','%s','%s',%s,%s,%s)",
                                npi,
                                pref_txt,
                                tui,
                                mailing_street1,
                                mailing_street2,
                                mailing_city,
                                mailing_state,
                                normalize(mailing_phone),
                                normalize(mailing_fax),
                                normalize(mailing_zip),
                                physical_street1,
                                physical_street2,
                                physical_city,
                                physical_state,
                                normalize(physical_phone),
                                normalize(physical_fax),
                                normalize(physical_zip)
                        )
                );

                cnt = cnt + 1;
                if (cnt > BATCH_SIZE) {
                    Files.write(scriptPath, attrsLines.toString().getBytes(), StandardOpenOption.APPEND);
                    cnt = 0;
                    attrsLines.delete(0, attrsLines.length());
                }
            }
            attr_line = attr_reader.readLine();
        }

        Files.write(scriptPath, attrsLines.toString().getBytes(), StandardOpenOption.APPEND);
        LOGGER.info("NPI dictionary compilation is done");
    }

    // max 10 digits without spaces and other special symbols
    private static String normalize(String str) {
        String result = str.replaceAll("[^0-9]", "");
        if (result.length() > 10) {
            result = result.substring(result.length() - 10);
        }
        return result;
    }
}
