package com.jobai.domain.resume.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Service to extract raw text from PDF files using iText 7.
 */
@Slf4j
@Service
public class PdfExtractionService {

    /**
     * Extracts all text from a given PDF MultipartFile.
     */
    public String extractTextFromPdf(MultipartFile file) {
        log.info("Extracting text from PDF file: {}", file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream();
             PdfReader reader = new PdfReader(inputStream);
             PdfDocument pdfDoc = new PdfDocument(reader)) {

            StringBuilder text = new StringBuilder();
            int numPages = pdfDoc.getNumberOfPages();
            
            for (int i = 1; i <= numPages; i++) {
                text.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i)));
                text.append("\n");
            }
            
            String extracted = text.toString().trim();
            log.debug("Extracted {} characters from PDF.", extracted.length());
            return extracted;

        } catch (Exception e) {
            log.error("Failed to extract text from PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Could not read the uploaded PDF file. Please ensure it is a valid PDF.", e);
        }
    }
}
