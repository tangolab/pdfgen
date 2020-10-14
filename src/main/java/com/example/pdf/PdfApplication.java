package com.example.pdf;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.PdfPageFormCopier;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.StringTokenizer;

@SpringBootApplication
public class PdfApplication implements ApplicationRunner {
	public static final String DEST = "./fill_flatten_merge2.pdf";

	public static final String DATA = "./united_states.csv";
	public static final String SRC = "./state.pdf";
	public static int counter = 0;
	public static void main(String[] args) {
		SpringApplication.run(PdfApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		List<String> argsList = args.getOptionValues("input");
		if (argsList.size() > 0) {
			// System.out.println(argsList.get(0));
			// parseXML(argsList.get(0));
			File file = new File(DEST);
			file.getParentFile().mkdirs();
            Long start = System.currentTimeMillis();

			new PdfApplication().manipulatePdf(DEST);
            Long end = System.currentTimeMillis();
            System.out.format("iText took %.2f ms.\n", (double) (end - start));
		} else {
			System.out.println("Usage: java -jar pdf.jar --input=<xml data file name> ");
		}
	}

	protected void manipulatePdf(String dest) throws Exception {
		PdfWriter writer = new PdfWriter(dest);
		PdfPageFormCopier formCopier = new PdfPageFormCopier();

		// In smart mode when resources (such as fonts, images,...) are encountered,
		// a reference to these resources is saved in a cache and can be reused.
		// This mode reduces the file size of the resulting PDF document.
		writer.setSmartMode(true);
		PdfDocument pdfDoc = new PdfDocument(writer);

		// Initialize an outline tree of the document and sets outline mode to true
		pdfDoc.initializeOutlines();

		try (BufferedReader br = new BufferedReader(new FileReader(DATA))) {

			// Read first line with headers,
			// do nothing with this line, because headers are already filled in form
			String line = br.readLine();

			while ((line = br.readLine()) != null) {
				//counter++;
				//System.out.println(counter);
				// Сreate a PDF in memory
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PdfDocument pdfInnerDoc = new PdfDocument(new PdfReader(SRC), new PdfWriter(baos));
				PdfAcroForm form = PdfAcroForm.getAcroForm(pdfInnerDoc, true);

				// Parse text line and fill all fields of form
				fillAndFlattenForm(line, form);
				pdfInnerDoc.close();

				// Copy page with current filled form to the result pdf document
				pdfInnerDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())));
				pdfInnerDoc.copyPagesTo(1, pdfInnerDoc.getNumberOfPages(), pdfDoc, formCopier);
				pdfInnerDoc.close();
			}
		}

		pdfDoc.close();
	}

	public void fillAndFlattenForm(String line, PdfAcroForm form) {
		StringTokenizer tokenizer = new StringTokenizer(line, ";");
		Map<String, PdfFormField> fields = form.getFormFields();

		fields.get("name").setValue(tokenizer.nextToken());
		fields.get("abbr").setValue(tokenizer.nextToken());
		fields.get("capital").setValue(tokenizer.nextToken());
		fields.get("city").setValue(tokenizer.nextToken());
		fields.get("population").setValue(tokenizer.nextToken());
		fields.get("surface").setValue(tokenizer.nextToken());
		fields.get("timezone1").setValue(tokenizer.nextToken());
		fields.get("timezone2").setValue(tokenizer.nextToken());
		fields.get("dst").setValue(tokenizer.nextToken());

		// If no fields have been explicitly included via partialFormFlattening(),
		// then all fields are flattened. Otherwise only the included fields are
		// flattened.
		form.flattenFields();
	}

}
