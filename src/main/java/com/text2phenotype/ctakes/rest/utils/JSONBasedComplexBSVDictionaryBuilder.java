package com.text2phenotype.ctakes.rest.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class JSONBasedComplexBSVDictionaryBuilder {

    final public static FilenameFilter fileFilter = (dir, name) -> name.toLowerCase().endsWith("bsv");

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new AssertionError("Arguments required");
        }

        String bsvDictionariesPath = args[0];
        String jsonDescriptionFile = args[1];

        File descr = new File(jsonDescriptionFile);

        ObjectMapper mapper = new ObjectMapper();

        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType dataType = factory.constructMapType(HashMap.class, String.class, Object.class);
        Map<String, Map<String, Map<String, String>>> r = mapper.readValue(descr, dataType);
        Collection<Map<String, Map<String, String>>> dictdata = r.values();

        Map<String, Map<String, String>> concepts = new HashMap<>();
        for (Map<String, Map<String, String>> data: dictdata) {
            concepts.putAll(data);
        }

        try (Stream<Path> files = Files.list(Paths.get(bsvDictionariesPath))) {
            files.forEach(file -> {
                String dict_name = file.getFileName().toString();
                dict_name = dict_name.substring(0, dict_name.length()-4);
                if (!concepts.containsKey(dict_name)) {
                    System.out.println("File not found: " + file.getFileName().toString());
                    return;
                }

                Map<String, String> data = concepts.get(dict_name);

                try {
                    try(Stream<String> lines = Files.lines(file)) {
                        List<String> bw = new ArrayList<>();

                        lines.forEach(line -> {

                            String[] parts = line.split("\\|");
                            bw.add(String.format("%s|%s|%s|%s", parts[0], data.get("tui"), data.get("codingScheme"), parts[1]));

                        });

                        Files.delete(file);
                        Files.write(file, bw);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
