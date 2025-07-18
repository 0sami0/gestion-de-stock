package com.fsr.gestion_de_stock;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

public class DocumentGenerator {

    private static final Logger log = LoggerFactory.getLogger(DocumentGenerator.class);
    private static final String APP_DIR = System.getProperty("user.home") + File.separator + "GestionDeStock";
    private static final String OUTPUT_DIR = APP_DIR + File.separator + "output";

    public static File generateDocument(String templateName, HashMap<String, String> placeholders) {
        try {
            log.info("Starting document generation with template: {}", templateName);
            log.info("Placeholders to be replaced: {}", placeholders);

            new File(OUTPUT_DIR).mkdirs();

            InputStream templateInputStream = DocumentGenerator.class.getResourceAsStream(templateName);
            if (templateInputStream == null) {
                log.error("Template not found in resources: {}", templateName);
                return null;
            }
            log.info("Template loaded successfully.");

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateInputStream);
            MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();

            mainDocumentPart.variableReplace(placeholders);
            log.info("Completed variableReplace operation.");

            String recipient = placeholders.getOrDefault("NOM_PRENOM", "unknown").replace(" ", "_");
            String baseName = templateName.replace("_template.docx", "");
            String fileName = String.format("%s/%s_%s_%d.docx", OUTPUT_DIR, baseName, recipient, System.currentTimeMillis());

            File outputFile = new File(fileName);
            wordMLPackage.save(outputFile);
            log.info("Document successfully saved to: {}", outputFile.getAbsolutePath());

            return outputFile;

        } catch (Exception e) {
            log.error("CRITICAL FAILURE during document generation", e);
            e.printStackTrace();
            return null;
        }
    }
}