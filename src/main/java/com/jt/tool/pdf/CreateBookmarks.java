/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jt.tool.pdf;

import com.google.common.base.Splitter;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.IOException;
import java.util.List;

/**
 * This is an example on how to add bookmarks to a PDF document.  It simply
 * adds 1 bookmark for every page.
 * Usage: java org.apache.pdfbox.examples.pdmodel.CreateBookmarks &lt;input-pdf&gt; &lt;output-pdf&gt;
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class CreateBookmarks {
    private CreateBookmarks() {
        //utility class
    }

    static Splitter on = Splitter.on("\r\n").omitEmptyStrings();


    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     * @exception Exception If there is an error parsing the document.
     */
    public static void main(String[] args) throws Exception {
        String fileName = "d:/pdf/go.pdf";
        String regex = "^\\d{1,2}\\.\\d{1,2}\\.\\s.*+";
        createBookmark(fileName, regex);
    }

    public static void createBookmark(String args, String reg) throws Exception {
        PDDocument document = null;
        try {
            document = PDDocument.load(args);
            if (document.isEncrypted()) {
                System.err.println("Error: Cannot add bookmarks to encrypted document.");
                System.exit(1);
            }
            PDDocumentOutline outline = new PDDocumentOutline();
            document.getDocumentCatalog().setDocumentOutline(outline);
            PDOutlineItem pagesOutline = new PDOutlineItem();
            pagesOutline.setTitle("All Pages");
            outline.appendChild(pagesOutline);
            List pages = document.getDocumentCatalog().getAllPages();
            for (int i = 0; i < pages.size(); i++) {
                String pageText = getPageText(document, i+1, 0);
                String[] strings = matchTitle(pageText, reg);
                if (makeBookmark(strings)) {
                    PDPage page = (PDPage) pages.get(i);
                    PDPageFitWidthDestination dest = new PDPageFitWidthDestination();
                    dest.setPage(page);
                    PDOutlineItem bookmark = new PDOutlineItem();
                    bookmark.setDestination(dest);
                    bookmark.setTitle(strings[0]);
                    pagesOutline.appendChild(bookmark);
                    System.out.println("add " + strings[0]);
                }
                continue;

            }
            pagesOutline.openNode();
            outline.openNode();
            document.save(args);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    public static String getPageText(PDDocument document, int start, int offset) throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(start);
        stripper.setEndPage(start + offset);
        return stripper.getText(document);
    }

    public static String[] matchTitle(String text, String regex) {
        Iterable<String> split = on.split(text);
        final String[] s = {null};
        split.forEach(word -> {
            if (word.matches(regex)) {
                s[0] = word;
            }
        });
        return s;
    }

    public static boolean makeBookmark(String[] str) {
        if (str[0] != null) {
            return true;
        }
        return false;
    }

}