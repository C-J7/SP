package com.studypilot.syllabus;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import com.studypilot.common.UnprocessableException;

/** Turns a syllabus PDF into an ordered, de-duplicated list of topics. */
@Component
public class TopicExtractor {

    static final int MAX_PAGES = 100;
    static final int MAX_TOPICS = 200;
    private static final byte[] PDF_MAGIC = {'%', 'P', 'D', 'F', '-'};

    // "- Arrays", "* Trees", "• Graphs", "1. Recursion", "2) Sorting", "Week 3: Hashing", "Topic 4 - Heaps"
    private static final Pattern TOPIC_LINE = Pattern.compile(
            "^(?:[-*\\u2022\\u25AA\\u25CF]\\s+|\\d{1,3}[.)]\\s+|(?:week|topic|module|unit|lecture)\\s*\\d{1,3}\\s*[:.\\-\\u2013]\\s*)(.+)$",
            Pattern.CASE_INSENSITIVE);

    public List<String> extract(byte[] pdf) {
        if (!startsWithPdfMagic(pdf)) {
            throw new UnprocessableException("File is not a PDF");
        }
        try (PDDocument document = PDDocument.load(pdf)) {
            if (document.isEncrypted()) {
                throw new UnprocessableException("Encrypted PDFs are not supported");
            }
            if (document.getNumberOfPages() > MAX_PAGES) {
                throw new UnprocessableException("PDF exceeds " + MAX_PAGES + " pages");
            }
            List<String> topics = extractFromText(new PDFTextStripper().getText(document));
            if (topics.isEmpty()) {
                throw new UnprocessableException(
                        "No topics found. List topics as bullets, numbered items or 'Week N: Topic' lines.");
            }
            return topics;
        } catch (IOException e) {
            throw new UnprocessableException("PDF could not be read");
        }
    }

    static List<String> extractFromText(String text) {
        Set<String> topics = new LinkedHashSet<>();
        for (String raw : text.split("\\R")) {
            Matcher m = TOPIC_LINE.matcher(raw.strip());
            if (m.matches()) {
                String topic = m.group(1).strip().replaceAll("\\s+", " ");
                if (topic.length() >= 2) {
                    topics.add(topic.length() > 200 ? topic.substring(0, 200) : topic);
                }
            }
            if (topics.size() == MAX_TOPICS) {
                break;
            }
        }
        return List.copyOf(topics);
    }

    private static boolean startsWithPdfMagic(byte[] bytes) {
        if (bytes == null || bytes.length < PDF_MAGIC.length) {
            return false;
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (bytes[i] != PDF_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }
}
