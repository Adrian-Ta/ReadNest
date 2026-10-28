import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Erzeugt Beispiel-PDFs und Vorschaubilder für die lokale Datenbank (siehe setup.sh) **/
public class MedienGenerator {

    record Buch(String slug, String titel, String autor, Color farbe) {}

    static final Buch[] BUECHER = {
            new Buch("verwandlung", "Die Verwandlung", "Franz Kafka", new Color(0x2E4057)),
            new Buch("faust", "Faust. Der Tragödie erster Teil", "Johann Wolfgang von Goethe", new Color(0x7A1F1F)),
            new Buch("werther", "Die Leiden des jungen Werthers", "Johann Wolfgang von Goethe", new Color(0x3B6E4A)),
            new Buch("effi", "Effi Briest", "Theodor Fontane", new Color(0x8C6A2F)),
            new Buch("sandmann", "Der Sandmann", "E. T. A. Hoffmann", new Color(0x4B3869)),
            new Buch("schimmelreiter", "Der Schimmelreiter", "Theodor Storm", new Color(0x2F5D73)),
    };

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "medien");
        dir.mkdirs();
        for (Buch b : BUECHER) {
            erzeugeCover(b, new File(dir, b.slug() + ".png"));
            erzeugePdf(b, new File(dir, b.slug() + ".pdf"));
        }
        System.out.println(BUECHER.length + " Bücher erzeugt in " + dir.getAbsolutePath());
    }

    static void erzeugeCover(Buch b, File ziel) throws Exception {
        int w = 300, h = 450;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(b.farbe());
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(3));
        g.drawRect(18, 18, w - 36, h - 36);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 30));
        int y = 120;
        for (String zeile : umbrechen(b.titel(), g.getFontMetrics(), w - 70)) {
            g.drawString(zeile, (w - g.getFontMetrics().stringWidth(zeile)) / 2, y);
            y += 38;
        }
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        for (String zeile : umbrechen(b.autor(), g.getFontMetrics(), w - 70)) {
            g.drawString(zeile, (w - g.getFontMetrics().stringWidth(zeile)) / 2, h - 80);
        }
        g.dispose();
        ImageIO.write(img, "png", ziel);
    }

    static void erzeugePdf(Buch b, File ziel) throws Exception {
        PDType1Font fett = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        try (PDDocument doc = new PDDocument()) {
            for (int seite = 1; seite <= 5; seite++) {
                PDPage page = new PDPage(PDRectangle.A5);
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float hoehe = page.getMediaBox().getHeight();
                    if (seite == 1) {
                        schreibe(cs, fett, 20, 50, hoehe - 150, b.titel());
                        schreibe(cs, normal, 14, 50, hoehe - 180, b.autor());
                        schreibe(cs, normal, 10, 50, 60, "Beispieldatei der lokalen ReadNest-Datenbank");
                    } else {
                        schreibe(cs, fett, 14, 50, hoehe - 60, "Kapitel " + (seite - 1));
                        float y = hoehe - 95;
                        for (int i = 0; i < 22; i++) {
                            schreibe(cs, normal, 11, 50, y, "Platzhaltertext für " + b.titel().split("\\.")[0] + ", Zeile " + (i + 1) + ".");
                            y -= 18;
                        }
                        schreibe(cs, normal, 10, page.getMediaBox().getWidth() / 2 - 5, 30, String.valueOf(seite));
                    }
                }
            }
            doc.save(ziel);
        }
    }

    static void schreibe(PDPageContentStream cs, PDType1Font font, float groesse, float x, float y, String text) throws Exception {
        cs.beginText();
        cs.setFont(font, groesse);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    static List<String> umbrechen(String text, FontMetrics fm, int maxBreite) {
        List<String> zeilen = new ArrayList<>();
        StringBuilder zeile = new StringBuilder();
        for (String wort : text.split(" ")) {
            String kandidat = zeile.isEmpty() ? wort : zeile + " " + wort;
            if (fm.stringWidth(kandidat) > maxBreite && !zeile.isEmpty()) {
                zeilen.add(zeile.toString());
                zeile = new StringBuilder(wort);
            } else {
                zeile = new StringBuilder(kandidat);
            }
        }
        zeilen.add(zeile.toString());
        return zeilen;
    }
}
