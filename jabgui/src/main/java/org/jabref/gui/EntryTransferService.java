package org.jabref.gui;

import java.io.IOException;
import java.util.List;

import org.jabref.gui.clipboard.ClipBoardManager;
import org.jabref.gui.externalfiles.ImportHandler;
import org.jabref.logic.importer.FetcherClientException;
import org.jabref.logic.importer.FetcherException;
import org.jabref.logic.importer.FetcherServerException;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.TransferInformation;
import org.jabref.model.TransferMode;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.BibEntryTypesManager;
import org.jabref.model.entry.BibtexString;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.StandardEntryType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jabref.gui.util.InsertUtil.addEntriesWithFeedback;

public class EntryTransferService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryTransferService.class);

    private final DialogService dialogService;
    private final ClipBoardManager clipBoardManager;
    private final ImportHandler importHandler;
    private final StateManager stateManager;
    private final BibEntryTypesManager entryTypesManager;

    public EntryTransferService(DialogService dialogService,
                                ClipBoardManager clipBoardManager,
                                ImportHandler importHandler,
                                StateManager stateManager,
                                BibEntryTypesManager entryTypesManager) {
        this.dialogService = dialogService;
        this.clipBoardManager = clipBoardManager;
        this.importHandler = importHandler;
        this.stateManager = stateManager;
        this.entryTypesManager = entryTypesManager;
    }

    public int copyEntries(BibDatabaseContext bibDatabaseContext, TransferMode transferMode, List<BibEntry> selectedEntries) {
        if (selectedEntries.isEmpty()) {
            return 0;
        }

        List<BibtexString> stringConstants = bibDatabaseContext.getDatabase().getUsedStrings(selectedEntries);
        try {
            clipBoardManager.setContent(transferMode, bibDatabaseContext, selectedEntries, entryTypesManager, stringConstants);
            return selectedEntries.size();
        } catch (IOException e) {
            LOGGER.error("Error while copying selected entries to clipboard.", e);
            return -1;
        }
    }

    public void pasteEntries(BibDatabaseContext bibDatabaseContext) {
        String content = ClipBoardManager.getContents();
        List<BibEntry> entriesToAdd = importHandler.handleBibTeXData(content);
        if (entriesToAdd.isEmpty()) {
            entriesToAdd = handleNonBibTeXStringData(content);
        }
        if (entriesToAdd.isEmpty()) {
            return;
        }
        addEntriesWithFeedback(
                clipBoardManager.getJabRefClipboardTransferData(),
                entriesToAdd,
                bibDatabaseContext,
                Localization.lang("Pasted %0 entry(s) to %1"),
                Localization.lang("Pasted %0 entry(s) to %1. %2 were skipped"),
                dialogService,
                importHandler,
                stateManager
        );
    }

    private List<BibEntry> handleNonBibTeXStringData(String data) {
        try {
            return this.importHandler.handleStringData(data);
        } catch (FetcherException exception) {
            if (exception instanceof FetcherClientException) {
                dialogService.showInformationDialogAndWait(Localization.lang("Look up identifier"), Localization.lang("No data was found for the identifier"));
            } else if (exception instanceof FetcherServerException) {
                dialogService.showInformationDialogAndWait(Localization.lang("Look up identifier"), Localization.lang("Server not available"));
            } else {
                dialogService.showErrorDialogAndWait(exception);
            }
            BibEntry fallBack = new BibEntry(StandardEntryType.Misc)
                    .withField(StandardField.COMMENT, data)
                    .withChanged(true);
            return List.of(fallBack);
        }
    }

    public void dropEntries(BibDatabaseContext bibDatabaseContext, BibDatabaseContext sourceBibDatabaseContext, List<BibEntry> entriesToAdd) {
        addEntriesWithFeedback(
                new TransferInformation(sourceBibDatabaseContext, TransferMode.NONE), // "NONE", because we don't know the modifiers here and thus cannot say whether the attached file (and entry(s)) should be copied or moved
                entriesToAdd,
                bibDatabaseContext,
                Localization.lang("Moved %0 entry(s) to %1"),
                Localization.lang("Moved %0 entry(s) to %1. %2 were skipped"),
                dialogService,
                importHandler,
                stateManager
        );
    }
}