package biz.shujutech.pdf;

import biz.shujutech.base.App;
import biz.shujutech.base.Hinderance;
import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.render.Box;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PdfEngine {

	public static final String ROOT_PATH = "file:////";

	private static String ToXHtml(String html) {
		final Document document = Jsoup.parse(html);
		document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		return document.html();
	}

	public static OutputStream GeneratePdf(String aOutputFileName, String aHtmlStr) throws Exception {
		return GeneratePdf(aOutputFileName, aHtmlStr, ROOT_PATH);
	}

	public static ByteArrayOutputStream GeneratePdf(String aHtmlStr) throws Exception {
		return (ByteArrayOutputStream) GeneratePdf(null, aHtmlStr, ROOT_PATH);
	}

	public static ByteArrayOutputStream GeneratePdfWithBaseUrl(String aHtmlStr, String aUrlBase) throws Exception {
		return (ByteArrayOutputStream) GeneratePdf(null, aHtmlStr, aUrlBase);
	}

	// as Base64 NO password
	public static String GeneratePdfAsBase64(String aHtmlStr, String aUrlBase) throws Exception {
		ByteArrayOutputStream outStream;
		try {
			outStream = new ByteArrayOutputStream();
			try {
				PdfRendererBuilder builder = new PdfRendererBuilder();
				builder.withHtmlContent(ToXHtml(aHtmlStr), aUrlBase);
				builder.toStream(outStream);
				builder.run();
			} catch (Exception ex) {
				throw new Hinderance(ex, PdfEngine.class.getSimpleName() + " fail to render Pdf");
			} finally {
				try {
					outStream.close();
				} catch (IOException ex) {
					App.logEror(PdfEngine.class, ex);
				}
			}
		} catch (Exception ex) {
			throw new Hinderance(ex, PdfEngine.class.getSimpleName() + " fail to genereate Pdf");
		}
		String result = Base64.getEncoder().encodeToString(outStream.toByteArray());
		return(result);
	}

	// as Base64 WITH password protected PDF
	public static String GeneratePdfAsBase64(String aHtmlStr, String aUrlBase, String aPassword) throws Exception {
		ByteArrayOutputStream outStream;
		ByteArrayOutputStream protectedStream = null;
		try {
			outStream = new ByteArrayOutputStream();
			try {
				PdfRendererBuilder builder = new PdfRendererBuilder();
				builder.withHtmlContent(ToXHtml(aHtmlStr), aUrlBase);
				builder.toStream(outStream);
				builder.run();
				protectedStream = PasswordProtectPdf(outStream.toByteArray(), aPassword);
			} catch (Exception ex) {
				throw new Hinderance(ex, PdfEngine.class.getSimpleName() + " fail to render Pdf");
			} finally {
				try {
					outStream.close();
					if (protectedStream != null) protectedStream.close();
				} catch (IOException ex) {
					App.logEror(PdfEngine.class, ex);
				}
			}
		} catch (Exception ex) {
			throw new Hinderance(ex, PdfEngine.class.getSimpleName() + " fail to genereate Pdf");
		}
		String result = null;
		if (protectedStream != null)  {
			result = Base64.getEncoder().encodeToString(protectedStream.toByteArray());
		}
		return(result);
	}

	public static ByteArrayOutputStream PasswordProtectPdf(byte[] aPdfByte, String aPassword) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PDDocument doc = PDDocument.load(aPdfByte);
		int keyLength = 128;
		AccessPermission ap = new AccessPermission();
		StandardProtectionPolicy spp = new StandardProtectionPolicy(aPassword, aPassword, ap);
		spp.setEncryptionKeyLength(keyLength);
		spp.setPermissions(ap);
		doc.protect(spp);
		doc.save(outStream);
		return(outStream);
	}

	public static OutputStream GeneratePdf(String aOutputFileName, String aHtmlStr, String aUrlBase) throws Exception {
		OutputStream outStream = null;
		try {
			if (aOutputFileName == null || aOutputFileName.isEmpty()) {
				outStream = new ByteArrayOutputStream();
			} else {
				outStream = new FileOutputStream(aOutputFileName);
			}
			try {
				PdfRendererBuilder builder = new PdfRendererBuilder();
				builder.withHtmlContent(ToXHtml(aHtmlStr), aUrlBase);
				builder.toStream(outStream);
				builder.run();
			} catch (Exception ex) {
				throw new Hinderance(ex, PdfEngine.class.getSimpleName() + " fail to render Pdf");
			} finally {
				try {
					outStream.close();
				} catch (IOException ex) {
					App.logEror(PdfEngine.class, ex);
				}
			}
		} catch (Hinderance ex) {
			throw new Hinderance(ex, PdfEngine.class.getSimpleName() + " fail to genereate Pdf");
		}

		return(outStream);
	}

	public static OutputStream GeneratePdf(String aOutputFileName, String aHtmlStr, String aUrlBase, String aPassword) throws Exception {
		ByteArrayOutputStream outStream;
		outStream = new ByteArrayOutputStream();
		try {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.withHtmlContent(ToXHtml(aHtmlStr), aUrlBase);
			builder.toStream(outStream);
			builder.run();
			PasswordProtectPdf(outStream.toByteArray(), aPassword, aOutputFileName);
		} catch (Exception ex) {
			throw new Hinderance(ex, PdfEngine.class.getSimpleName() + " fail to render Pdf");
		} finally {
			try {
				outStream.close();
			} catch (IOException ex) {
				App.logEror(PdfEngine.class, ex);
			}
		}

		return(outStream);
	}

	public static void PasswordProtectPdf(byte[] aPdfByte, String aPassword, String aOutputFileName) throws IOException {
		PDDocument doc = PDDocument.load(aPdfByte);
		int keyLength = 128;
		AccessPermission ap = new AccessPermission();
		StandardProtectionPolicy spp = new StandardProtectionPolicy(aPassword, aPassword, ap);
		spp.setEncryptionKeyLength(keyLength);
		spp.setPermissions(ap);
		doc.protect(spp);
		doc.save(aOutputFileName);
		doc.close();
	}

	// An internal number, should stay the same.
	private static final int PDF_DOTS_PER_PIXEL = 20;
	private static final int A4_PIXEL_WIDTH = 793;
	private static final int A4_PIXEL_HEIGHT = 1122;
	private static final float PDF_POINT_WIDTH_A4 = 595f;
	private static final float PDF_POINT_HEIGHT_A4 = 841.88f;
	
	public static void GeneratePdfA4(String aOutputFileName, String aHtmlStr, String aUrlBase) throws Exception {
		PdfRendererBuilder builder = new PdfRendererBuilder();
		
		// First we create a document with too many lines to fit on page.
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			sb.append("This is a div<br/>This is a new line.<br/>");
		}
		sb.append("LAST LINE");
		
		// Note: We set a huge page height so it can auto-size the div without constraints.
		String html = "<html><head><style>@page { size: " + A4_PIXEL_WIDTH + "px 10000px; } @page { margin: 0; } "
				+ "body { margin: 0; } div { font-size: 30px; } div { transform: none; transform-origin: left top; padding: 5px; }</style></head><body>"
				+ "<div>" + sb.toString() + "</div>"
				+ "</body></html>";
		//html = aHtmlStr;
		
		//builder.withHtmlContent(html, /* Base url */ null);
		builder.withHtmlContent(ToXHtml(aHtmlStr), aUrlBase);
		PdfBoxRenderer renderer = builder.buildPdfRenderer();
		renderer.layout();
		
		// The root box is <html>, the first child is <body>, then <div>.
		Box box = renderer.getRootBox().getChild(0).getChild(0);
		
		renderer.getPdfDocument().close();
		renderer.close();

		float flWidth = box.getWidth() / PDF_DOTS_PER_PIXEL;
		float flHeight = box.getHeight() / PDF_DOTS_PER_PIXEL;

		// Figure out the scale factor.
		float scaleW = A4_PIXEL_WIDTH / flWidth;
		float scaleH = A4_PIXEL_HEIGHT / flHeight;
		float scale = Math.min(scaleW, scaleH);

		// Replace the original transform with the scale.
		html = html.replace("transform: none", String.format("transform: scale(%.3f)", scale));

		// Now output the new html.
		try (FileOutputStream fos = new FileOutputStream(aOutputFileName)) {
			PdfRendererBuilder builder2 = new PdfRendererBuilder();
			//builder2.withHtmlContent(html, /* Base uri. */ null);
			builder2.withHtmlContent(ToXHtml(aHtmlStr), aUrlBase);

			PdfBoxRenderer renderer2 = builder2.buildPdfRenderer();
			renderer2.createPDFWithoutClosing();

			PDDocument doc = renderer2.getPdfDocument();
			renderer2.close();

			// PDF page origin is bottom left, so we set a media box and crop box that takes the the top A4 height of the page.
			PDRectangle mediaBox = doc.getPage(0).getMediaBox();
			doc.getPage(0).setCropBox(new PDRectangle(0f, mediaBox.getHeight() - PDF_POINT_HEIGHT_A4, PDF_POINT_WIDTH_A4, PDF_POINT_HEIGHT_A4));
			doc.getPage(0).setMediaBox(new PDRectangle(0f, mediaBox.getHeight() - PDF_POINT_HEIGHT_A4, PDF_POINT_WIDTH_A4, PDF_POINT_HEIGHT_A4));

			doc.save(fos);
			doc.close();
		}
	}
	
	
}