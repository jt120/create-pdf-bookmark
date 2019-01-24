import com.google.common.base.Splitter;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.PDFText2HTML;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ze.liu on 2018/9/29.
 */
public class PdfTest {

    static Splitter on = Splitter.on("\r\n").omitEmptyStrings().trimResults();

    @Test
    public void testShow() throws Exception {
        PDDocument doc = PDDocument.load(new File("e:/down/flink.pdf"));
        final int numberOfPages = doc.getNumberOfPages();
        System.out.println("num of pages " + numberOfPages);
        PDFTextStripper stripper = new PDFTextStripper();
        for (int i = 1; i < numberOfPages; i++) {
            stripper.setStartPage(i);
            stripper.setEndPage(i);
            final String text = stripper.getText(doc);
            System.out.println(i + "====" + text);
            if (i > 6) {
                break;
            }

        }
        doc.close();
    }

    @Test
    public void testExtract() throws Exception {
        PDDocument doc = PDDocument.load(new File("e:/down/flink.pdf"));
        final int numberOfPages = doc.getNumberOfPages();
        System.out.println("num of pages " + numberOfPages);


        String reg = "(.*)\\s+\\.+\\s+(\\d+)";
        final Pattern pattern = Pattern.compile(reg);

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(5);
        stripper.setEndPage(5);
        final String text = stripper.getText(doc);

        final TreeMap<Integer, String[]> map = extractTitle(text, pattern);
        for (Integer i : map.keySet()) {
            System.out.println(i);
        }
        doc.close();
    }

    public TreeMap<Integer, String[]> extractTitle(String content, Pattern pattern) {
        TreeMap<Integer, String[]> map = new TreeMap<>();
        final String[] split = content.split("\\n");
        for (String s : split) {
            if (s.length() > 10) {
                final String[] tmp = stripTitleAndPage(s, pattern);
                if (tmp != null) {
                    map.put(Integer.valueOf(tmp[1]), tmp);
                    System.out.println(tmp[0] + "," + tmp[1]);
                }
            }
        }
        return map;
    }


    /*
    可用版
     */
    @Test
    public void testOutline() throws Exception {
        PDDocument document = null;

        String reg = "(.*)\\s+\\.+\\s+(\\d+)";
        final Pattern pattern = Pattern.compile(reg);
        int tableIndex = 5;
        try {
            String file = "e:/down/flink.pdf";
            document = PDDocument.load(new File(file));
            if (document.isEncrypted()) {
                System.err.println("Error: Cannot add bookmarks to encrypted document.");
                System.exit(1);
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(tableIndex);
            stripper.setEndPage(tableIndex);
            final String text = stripper.getText(document);
            final TreeMap<Integer, String[]> map = extractTitle(text, pattern);

            PDDocumentOutline outline = new PDDocumentOutline();
            document.getDocumentCatalog().setDocumentOutline(outline);
            PDOutlineItem pagesOutline = new PDOutlineItem();
            pagesOutline.setTitle("All Pages");
            outline.addLast(pagesOutline);
            int pageNumOffset = 5;
            int pageNum = 0;
            final int numberOfPages = document.getNumberOfPages();
            final Set<Integer> pageSet = map.keySet();

            for (int i = pageNumOffset; i < numberOfPages; i++) {
                final PDPage page = document.getPage(i);

                final int offsetPage = i - pageNumOffset+1;
                if (pageSet.contains(offsetPage)) {
                    PDPageDestination dest = new PDPageFitWidthDestination();
                    // If you want to have several bookmarks pointing to different areas
                    // on the same page, have a look at the other classes derived from PDPageDestination.

                    dest.setPage(page);
                    PDOutlineItem bookmark = new PDOutlineItem();
                    bookmark.setDestination(dest);
                    final String[] titles = map.get(offsetPage);
                    bookmark.setTitle(titles[0]);
                    pagesOutline.addLast(bookmark);
                }
            }

            pagesOutline.openNode();
            outline.openNode();
            // optional: show the outlines when opening the file
//            document.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);

            document.save("e:/flink_table.pdf");
        } finally {
            if (document != null) {
                document.close();
            }
        }

    }

    @Test
    public void testPattern() throws Exception {
        String s = "阿里巴巴为什么选择 Apache Flink？ .................................................................. 1";
        String reg = "(.*)\\s+\\.+\\s+(\\d+)";
        final Pattern pattern = Pattern.compile(reg);


        String[] s1 = {"", ""};
    }

    public String[] stripTitleAndPage(String s, Pattern pattern) {
        final Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            final int groupCount = matcher.groupCount();
            if (groupCount == 2) {
                return new String[]{matcher.group(1), matcher.group(2)};
            }
        }
        return null;
    }

    @Test
    public void testHtml() throws Exception {
        PDFText2HTML stripper = new PDFText2HTML();
        PDDocument doc = PDDocument.load(new File("e:/down/flink.pdf"));


        final int numberOfPages = doc.getNumberOfPages();
        System.out.println("num of pages " + numberOfPages);
        for (int i = 1; i < numberOfPages; i++) {
            stripper.setStartPage(i);
            stripper.setEndPage(i);
            final String text = stripper.getText(doc);
            System.out.println(i + "====" + text);
            if (i > 10) {
                break;
            }

        }

        doc.close();
    }


    public static String getPageText(PDDocument document, int start, int offset) throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
//        PDFTextStripper stripper = new PDFText2HTML("utf-8");
        stripper.setStartPage(start);
        stripper.setEndPage(start + offset);
        return stripper.getText(document);
    }

    @Test
    public void test02() throws Exception {
        for (int i = 0; i < 10; ++i) {
            System.out.println(i);
        }
    }
}
