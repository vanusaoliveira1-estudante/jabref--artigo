package org.jabref.gui;

import java.util.List;
import java.util.stream.Collectors;

import org.jabref.gui.actions.StandardActions;
import org.jabref.gui.fieldeditors.LinkedFileViewModel;
import org.jabref.gui.linkedfile.DeleteFileAction;
import org.jabref.gui.preferences.GuiPreferences;
import org.jabref.gui.undo.CountingUndoManager;
import org.jabref.gui.undo.UndoableRemoveEntries;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.LinkedFile;

public class EntryDeletionService {

    private final DialogService dialogService;
    private final GuiPreferences preferences;
    private final CountingUndoManager undoManager;

    public EntryDeletionService(DialogService dialogService,
                                GuiPreferences preferences,
                                CountingUndoManager undoManager) {
        this.dialogService = dialogService;
        this.preferences = preferences;
        this.undoManager = undoManager;
    }

    public int deleteEntries(BibDatabaseContext bibDatabaseContext, StandardActions mode, List<BibEntry> entries) {
        if (entries.isEmpty()) {
            return 0;
        }
        if (mode == StandardActions.DELETE_ENTRY && !showDeleteConfirmationDialog(entries.size())) {
            return -1;
        }

        undoManager.addEdit(new UndoableRemoveEntries(bibDatabaseContext.getDatabase(), entries, mode == StandardActions.CUT));
        bibDatabaseContext.getDatabase().removeEntries(entries);

        if (mode != StandardActions.CUT) {
            List<LinkedFile> linkedFileList = entries.stream()
                                                     .flatMap(entry -> entry.getFiles().stream())
                                                     .distinct()
                                                     .toList();

            if (!linkedFileList.isEmpty()) {
                List<LinkedFileViewModel> viewModels = linkedFileList.stream()
                                                                     .map(linkedFile -> LinkedFileViewModel.fromLinkedFile(linkedFile, null, bibDatabaseContext, null, null, preferences))
                                                                     .collect(Collectors.toList());

                new DeleteFileAction(dialogService, preferences.getFilePreferences(), bibDatabaseContext, viewModels).execute();
            }
        }

        return entries.size();
    }

    private boolean showDeleteConfirmationDialog(int numberOfEntries) {
        if (preferences.getWorkspacePreferences().shouldConfirmDelete()) {
            String title = Localization.lang("Delete entry");
            String message = Localization.lang("Really delete the selected entry?");
            String okButton = Localization.lang("Delete entry");
            String cancelButton = Localization.lang("Keep entry");
            if (numberOfEntries > 1) {
                title = Localization.lang("Delete multiple entries");
                message = Localization.lang("Really delete the %0 selected entries?", Integer.toString(numberOfEntries));
                okButton = Localization.lang("Delete entries");
                cancelButton = Localization.lang("Keep entries");
            }

            return dialogService.showConfirmationDialogWithOptOutAndWait(
                    title,
                    message,
                    okButton,
                    cancelButton,
                    Localization.lang("Do not ask again"),
                    optOut -> preferences.getWorkspacePreferences().setConfirmDelete(!optOut));
        } else {
            return true;
        }
    }
}