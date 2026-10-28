package com.studypilot.syllabus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.studypilot.TestPdfs;
import com.studypilot.common.UnprocessableException;

class TopicExtractorTest {

    private final TopicExtractor extractor = new TopicExtractor();

    @Test
    void recognisesBulletsNumberingAndWeekLabelsAndDropsDuplicates() {
        String text = """
                CSC 301 Course Outline
                - Arrays
                * Linked   lists
                1. Recursion
                2) Sorting
                Week 5: Hashing
                Topic 6 - Heaps
                - Arrays
                Assessment is 30% coursework.
                """;

        assertThat(TopicExtractor.extractFromText(text))
                .containsExactly("Arrays", "Linked lists", "Recursion", "Sorting", "Hashing", "Heaps");
    }

    @Test
    void extractsTopicsFromARealPdf() {
        byte[] pdf = TestPdfs.withLines("Course outline", "1. Relational model", "2. SQL joins");

        assertThat(extractor.extract(pdf)).containsExactly("Relational model", "SQL joins");
    }

    @Test
    void rejectsFilesThatAreNotPdfsByMagicBytes() {
        byte[] fake = "<html>not a pdf</html>".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> extractor.extract(fake)).isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("not a PDF");
    }

    @Test
    void rejectsAPdfWithNoRecognisableTopics() {
        byte[] pdf = TestPdfs.withLines("Just a paragraph of prose with no list structure.");

        assertThatThrownBy(() -> extractor.extract(pdf)).isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("No topics");
    }
}
