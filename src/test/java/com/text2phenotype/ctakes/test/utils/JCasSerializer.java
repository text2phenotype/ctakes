package com.text2phenotype.ctakes.test.utils;

import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JCasSerializer {

    public static void Save(JCas jcas, String fileName) throws SAXException, IOException {
        FileOutputStream out = null;

        try {
            Path p = Paths.get(fileName);
            if (!Files.isWritable(p)) {
                Files.createFile(p);
            }
            // write XMI
            out = new FileOutputStream( fileName );
            XmiCasSerializer ser = new XmiCasSerializer( jcas.getTypeSystem() );
            XMLSerializer xmlSer = new XMLSerializer( out, false );
            ser.serialize( jcas.getCas(), xmlSer.getContentHandler() );
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    public static void Load(JCas jcas, String fileName) throws SAXException, IOException  {
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            XmiCasDeserializer.deserialize(new BufferedInputStream(in), jcas.getCas());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
