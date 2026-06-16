package org.jabref.model.entry;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.StandardField;

public class BibEntryMerger {

    private BibEntryMerger() {
    }

    public static void merge(BibEntry target, BibEntry source, Set<Field> sourcePrioritizedFields) {
        Set<Field> targetFields = new TreeSet<>(Comparator.comparing(Field::getName));
        Set<Field> sourceFields = new TreeSet<>(Comparator.comparing(Field::getName));

        targetFields.addAll(target.getFields());
        sourceFields.addAll(source.getFields());

        // At the moment, "Field" interface does not provide explicit equality, so using their names instead.
        Set<String> targetFieldsNames = targetFields.stream().map(Field::getName).collect(Collectors.toSet());
        Set<String> sourcePrioritizedFieldsNames = sourcePrioritizedFields.stream().map(Field::getName).collect(Collectors.toSet());

        for (Field sourceField : sourceFields) {
            Optional<String> sourceFieldValue = source.getField(sourceField);
            if (!targetFieldsNames.contains(sourceField.getName()) ||
                    sourcePrioritizedFieldsNames.contains(sourceField.getName())) {
                // As iterator only goes through non-null fields from source, sourceFieldValue can never be empty
                sourceFieldValue.ifPresent(s -> target.setField(sourceField, s));
            } else {
                switch (sourceField) {
                    case StandardField.FILE -> {
                        List<LinkedFile> currentFiles = target.getFiles();
                        List<LinkedFile> otherFiles = source.getFiles();
                        List<LinkedFile> filesToAdd = otherFiles.stream()
                                                                .filter(file -> !currentFiles.contains(file))
                                                                .toList();
                        if (!filesToAdd.isEmpty()) {
                            target.addFiles(filesToAdd);
                        }
                    }
                }
            }
        }

        if (target.getType().equals(BibEntry.DEFAULT_TYPE)) {
            target.setType(source.getType());
        }
    }
}