package org.jabref.model.entry;

import java.util.Optional;

import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.EntryType;
import org.jabref.model.entry.types.IEEETranEntryType;
import org.jabref.model.entry.types.StandardEntryType;

public class CrossReferenceMapper {

    private CrossReferenceMapper() {
    }

    /// Map an (empty) field of a BibEntry to a field of a cross-referenced entry.
    ///
    /// @param targetField field name of the BibEntry
    /// @param targetEntry type of the BibEntry
    /// @param sourceEntry type of the cross-referenced BibEntry
    /// @return the mapped field or null if there is no valid mapping available
    public static Optional<Field> getSourceField(Field targetField, EntryType targetEntry, EntryType sourceEntry) {
        // 1. Sort out forbidden fields
        if ((targetField == StandardField.IDS) ||
                (targetField == StandardField.CROSSREF) ||
                (targetField == StandardField.XREF) ||
                (targetField == StandardField.ENTRYSET) ||
                (targetField == StandardField.RELATED) ||
                (targetField == StandardField.SORTKEY)) {
            return Optional.empty();
        }

        // 2. Handle special field mappings
        if (((sourceEntry == StandardEntryType.MvBook) && (targetEntry == StandardEntryType.InBook)) ||
                ((sourceEntry == StandardEntryType.MvBook) && (targetEntry == StandardEntryType.BookInBook)) ||
                ((sourceEntry == StandardEntryType.MvBook) && (targetEntry == StandardEntryType.SuppBook)) ||
                ((sourceEntry == StandardEntryType.Book) && (targetEntry == StandardEntryType.InBook)) ||
                ((sourceEntry == StandardEntryType.Book) && (targetEntry == StandardEntryType.BookInBook)) ||
                ((sourceEntry == StandardEntryType.Book) && (targetEntry == StandardEntryType.SuppBook))) {
            if (targetField == StandardField.AUTHOR) {
                return Optional.of(StandardField.AUTHOR);
            }
            if (targetField == StandardField.BOOKAUTHOR) {
                return Optional.of(StandardField.AUTHOR);
            }
        }

        if (((sourceEntry == StandardEntryType.MvBook) && (targetEntry == StandardEntryType.Book)) ||
                ((sourceEntry == StandardEntryType.MvBook) && (targetEntry == StandardEntryType.InBook)) ||
                ((sourceEntry == StandardEntryType.MvBook) && (targetEntry == StandardEntryType.BookInBook)) ||
                ((sourceEntry == StandardEntryType.MvBook) && (targetEntry == StandardEntryType.SuppBook)) ||
                ((sourceEntry == StandardEntryType.MvCollection) && (targetEntry == StandardEntryType.Collection)) ||
                ((sourceEntry == StandardEntryType.MvCollection) && (targetEntry == StandardEntryType.InCollection)) ||
                ((sourceEntry == StandardEntryType.MvCollection) && (targetEntry == StandardEntryType.SuppCollection)) ||
                ((sourceEntry == StandardEntryType.MvProceedings) && (targetEntry == StandardEntryType.Proceedings)) ||
                ((sourceEntry == StandardEntryType.MvProceedings) && (targetEntry == StandardEntryType.InProceedings)) ||
                ((sourceEntry == StandardEntryType.MvReference) && (targetEntry == StandardEntryType.Reference)) ||
                ((sourceEntry == StandardEntryType.MvReference) && (targetEntry == StandardEntryType.InReference))) {
            if (targetField == StandardField.MAINTITLE) {
                return Optional.of(StandardField.TITLE);
            }
            if (targetField == StandardField.MAINSUBTITLE) {
                return Optional.of(StandardField.SUBTITLE);
            }
            if (targetField == StandardField.MAINTITLEADDON) {
                return Optional.of(StandardField.TITLEADDON);
            }

            // those fields are no more available for the same-name inheritance strategy
            if ((targetField == StandardField.TITLE) ||
                    (targetField == StandardField.SUBTITLE) ||
                    (targetField == StandardField.TITLEADDON)) {
                return Optional.empty();
            }

            // for these fields, inheritance is not allowed for the specified entry types
            if (targetField == StandardField.SHORTTITLE) {
                return Optional.empty();
            }
        }

        if (((sourceEntry == StandardEntryType.Book) && (targetEntry == StandardEntryType.InBook)) ||
                ((sourceEntry == StandardEntryType.Book) && (targetEntry == StandardEntryType.BookInBook)) ||
                ((sourceEntry == StandardEntryType.Book) && (targetEntry == StandardEntryType.SuppBook)) ||
                ((sourceEntry == StandardEntryType.Collection) && (targetEntry == StandardEntryType.InCollection)) ||
                ((sourceEntry == StandardEntryType.Collection) && (targetEntry == StandardEntryType.SuppCollection)) ||
                ((sourceEntry == StandardEntryType.Reference) && (targetEntry == StandardEntryType.InReference)) ||
                ((sourceEntry == StandardEntryType.Proceedings) && (targetEntry == StandardEntryType.InProceedings))) {
            if (targetField == StandardField.BOOKTITLE) {
                return Optional.of(StandardField.TITLE);
            }
            if (targetField == StandardField.BOOKSUBTITLE) {
                return Optional.of(StandardField.SUBTITLE);
            }
            if (targetField == StandardField.BOOKTITLEADDON) {
                return Optional.of(StandardField.TITLEADDON);
            }

            // those fields are no more available for the same-name inheritance strategy
            if ((targetField == StandardField.TITLE) || (targetField == StandardField.SUBTITLE) || (targetField == StandardField.TITLEADDON)) {
                return Optional.empty();
            }
            if (targetField == StandardField.SHORTTITLE) {
                return Optional.empty();
            }
        }

        if (((sourceEntry == IEEETranEntryType.Periodical) && (targetEntry == StandardEntryType.Article)) ||
                ((sourceEntry == IEEETranEntryType.Periodical) && (targetEntry == StandardEntryType.SuppPeriodical))) {
            if (targetField == StandardField.JOURNALTITLE) return Optional.of(StandardField.TITLE);
            if (targetField == StandardField.JOURNALSUBTITLE) return Optional.of(StandardField.SUBTITLE);
            if ((targetField == StandardField.TITLE) || (targetField == StandardField.SUBTITLE)) return Optional.empty();
            if (targetField == StandardField.SHORTTITLE) return Optional.empty();
        }

        // 3. Fallback to inherit the field with the same name.
        return Optional.ofNullable(targetField);
    }
}