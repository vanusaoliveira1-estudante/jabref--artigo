package org.jabref.logic.msbib;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.Month;
import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.field.UnknownField;
import org.jabref.model.entry.types.EntryType;

public class BibTeXConverter {

    private static final String MSBIB_PREFIX = "msbib-";

    private BibTeXConverter() {
    }

    /// Converts an {@link MSBibEntry} to a {@link BibEntry} for import
    public static BibEntry convert(MSBibEntry entry) {
        BibEntry result;
        Map<Field, String> fieldValues = new HashMap<>();

        EntryType bibTexEntryType = MSBibMapping.getBiblatexEntryType(entry.getType());
        result = new BibEntry(bibTexEntryType);

        // add String fields
        for (Map.Entry<String, String> field : entry.getFields().entrySet()) {
            String msField = field.getKey();
            String value = field.getValue();

            if ((value != null) && (MSBibMapping.getBibTeXField(msField) != null)) {
                fieldValues.put(MSBibMapping.getBibTeXField(msField), value);
            }
        }

        // Value must be converted
        if (fieldValues.containsKey(StandardField.LANGUAGE)) {
            int lcid = Integer.parseInt(fieldValues.get(StandardField.LANGUAGE));
            fieldValues.put(StandardField.LANGUAGE, MSBibMapping.getLanguage(lcid));
        }

        addAuthor(fieldValues, StandardField.AUTHOR, entry.getAuthors());
        addAuthor(fieldValues, StandardField.BOOKAUTHOR, entry.getBookAuthors());
        addAuthor(fieldValues, StandardField.EDITOR, entry.getEditors());
        addAuthor(fieldValues, StandardField.TRANSLATOR, entry.getTranslators());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "producername"), entry.getProducerNames());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "composer"), entry.getComposers());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "conductor"), entry.getConductors());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "performer"), entry.getPerformers());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "writer"), entry.getWriters());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "director"), entry.getDirectors());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "compiler"), entry.getCompilers());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "interviewer"), entry.getInterviewers());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "interviewee"), entry.getInterviewees());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "inventor"), entry.getInventors());
        addAuthor(fieldValues, new UnknownField(MSBIB_PREFIX + "counsel"), entry.getCounsels());

        if (entry.getPages() != null) {
            fieldValues.put(StandardField.PAGES, entry.getPages().toString("--"));
        }

        parseStandardNumber(entry.getStandardNumber(), fieldValues);

        if (entry.getAddress() != null) {
            fieldValues.put(StandardField.LOCATION, entry.getAddress());
        }
        // TODO: ConferenceName is saved as booktitle when converting from MSBIB to BibTeX
        if (entry.getConferenceName() != null) {
            fieldValues.put(StandardField.ORGANIZATION, entry.getConferenceName());
        }

        if (entry.getDateAccessed() != null) {
            fieldValues.put(new UnknownField(MSBIB_PREFIX + "accessed"), entry.getDateAccessed());
        }

        if (entry.getJournalName() != null) {
            fieldValues.put(StandardField.JOURNAL, entry.getJournalName());
        }
        if (entry.getMonth() != null) {
            Optional<Month> month = Month.parse(entry.getMonth());
            month.ifPresent(result::setMonth);
        }
        if (entry.getNumber() != null) {
            fieldValues.put(StandardField.NUMBER, entry.getNumber());
        }

        // set all fields
        result.setField(fieldValues);

        return result;
    }

    private static void addAuthor(Map<Field, String> map, Field field, List<MsBibAuthor> authors) {
        if (authors == null) {
            return;
        }
        String allAuthors = authors.stream().map(MsBibAuthor::getLastFirst).collect(Collectors.joining(" and "));

        map.put(field, allAuthors);
    }

    private static void parseSingleStandardNumber(String type, Field field, String standardNum, Map<Field, String> map) {
        Pattern pattern = Pattern.compile(':' + type + ":(.[^:]+)");
        Matcher matcher = pattern.matcher(standardNum);
        if (matcher.matches()) {
            map.put(field, matcher.group(1));
        }
    }

    private static void parseStandardNumber(String standardNum, Map<Field, String> map) {
        if (standardNum == null) {
            return;
        }
        parseSingleStandardNumber("ISBN", StandardField.ISBN, standardNum, map);
        parseSingleStandardNumber("ISSN", StandardField.ISSN, standardNum, map);
        parseSingleStandardNumber("LCCN", new UnknownField("lccn"), standardNum, map);
        parseSingleStandardNumber("MRN", StandardField.MR_NUMBER, standardNum, map);
        parseSingleStandardNumber("DOI", StandardField.DOI, standardNum, map);
    }
}
