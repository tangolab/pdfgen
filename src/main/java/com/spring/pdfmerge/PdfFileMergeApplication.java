package com.spring.pdfmerge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PdfFileMergeApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(PdfFileMergeApplication.class, args);
    }

    private static void iTextGen() {
        try {
            PdfDocument pdfDoc;

            String currFolder = new File(".").getCanonicalPath();

            String DEST = currFolder + "/iTextOutput.pdf";

            String SRC = currFolder + "/test.pdf";
    

            pdfDoc = new PdfDocument(new PdfReader(SRC), new PdfWriter(DEST));
            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);

            // The second parameter sets how the field's value will be displayed in the
            // resultant pdf.
            // If the second parameter is null, then actual value will be shown.
            form.getField("txt_1").setValue("Name filled using iText");
            form.getField("txt_2").setValue("School filled using iText");

            pdfDoc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void pdfBoxGen() {
        try {
            String currFolder = new File(".").getCanonicalPath();

            PDDocument pDDocument = PDDocument.load(new File(currFolder + "/test.pdf"));
            PDAcroForm pDAcroForm = pDDocument.getDocumentCatalog().getAcroForm();
            PDField field = pDAcroForm.getField("txt_1");
            field.setValue("Name filled using PDFBox");
            field = pDAcroForm.getField("txt_2");
            field.setValue("School filled using PDFBox");
            pDDocument.save(currFolder + "/PdfBoxOutput.pdf");
            pDDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        iTextGen();

        pdfBoxGen();
    }
}
