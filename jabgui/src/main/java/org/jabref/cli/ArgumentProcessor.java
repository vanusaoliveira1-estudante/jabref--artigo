package org.jabref.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import org.jabref.gui.preferences.GuiPreferences;
import org.jabref.logic.JabRefException;
import org.jabref.logic.UiCommand;
import org.jabref.logic.l10n.Localization;
import org.jabref.logic.shared.prefs.SharedDatabasePreferences;
import org.jabref.logic.util.BuildInfo;
import org.jabref.logic.util.strings.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class ArgumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentProcessor.class);

    public enum Mode { INITIAL_START, REMOTE_START }

    private final Mode startupMode;
    private final GuiPreferences preferences;
    private final GuiCommandLine guiCli;
    private final CommandLine cli;

    private final List<UiCommand> uiCommands = new ArrayList<>();
    private boolean guiNeeded = true;

    public ArgumentProcessor(String[] args,
                             Mode startupMode,
                             GuiPreferences preferences) {
        this.startupMode = startupMode;
        this.preferences = preferences;
        this.guiCli = new GuiCommandLine();

        cli = new CommandLine(this.guiCli);
        cli.parseArgs(args);
    }

    public List<UiCommand> processArguments() {
        uiCommands.clear();
        guiNeeded = true;

        if ((startupMode == Mode.INITIAL_START) && cli.isVersionHelpRequested()) {
            System.out.printf(BuildInfo.JABREF_BANNER + "%n", new BuildInfo().version);
            guiNeeded = false;
            return List.of();
        }

        if ((startupMode == Mode.INITIAL_START) && cli.isUsageHelpRequested()) {
            System.out.printf(BuildInfo.JABREF_BANNER + "%n", new BuildInfo().version);
            System.out.println(cli.getUsageMessage());

            guiNeeded = false;
            return List.of();
        }

        // Check if we should reset all preferences to default values:
        if (guiCli.isResetPreferences()) {
            resetPreferences();
        }

        if (guiCli.getImportPreferences() != null) {
            importPreferences();
        }

        if (guiCli.getExportPreferences() != null) {
            exportPreferences();
        }

        if (guiCli.isBlank()) {
            uiCommands.add(new UiCommand.BlankWorkspace());
            return uiCommands;
        }

        if (StringUtil.isNotBlank(guiCli.getJumpToKey())) {
            uiCommands.add(new UiCommand.SelectEntryKeys(List.of(guiCli.getJumpToKey())));
        }

        if (guiCli.getLibraries() != null && !guiCli.getLibraries().isEmpty()) {
            if (guiCli.isAppend()) {
                uiCommands.add(new UiCommand.AppendFilesToCurrentLibrary(guiCli.getLibraries()));
            } else {
                uiCommands.add(new UiCommand.OpenLibraries(guiCli.getLibraries()));
            }
        }

        if (guiCli.getImportToOpen() != null) {
            uiCommands.add(new UiCommand.AppendFileOrUrlToCurrentLibrary(guiCli.getImportToOpen()));
        }
        if (guiCli.getImportBibtex() != null) {
            uiCommands.add(new UiCommand.AppendBibTeXToCurrentLibrary(guiCli.getImportBibtex()));
        }
        return uiCommands;
    }

    private void resetPreferences() {
        try {
            System.out.println(Localization.lang("Setting all preferences to default values."));
            preferences.clear();
            new SharedDatabasePreferences().clear();
        } catch (BackingStoreException e) {
            System.err.println(Localization.lang("Unable to clear preferences."));
            LOGGER.error("Unable to clear preferences", e);
        }
    }

    private void importPreferences() {
        try {
            System.out.println(Localization.lang("Importing preferences from '%0'.", guiCli.getImportPreferences()));
            preferences.importPreferences(guiCli.getImportPreferences());
        } catch (JabRefException e) {
            System.err.println(e.getLocalizedMessage());
            LOGGER.error("Unable to import preferences", e);
        }
    }

    private void exportPreferences() {
        try {
            System.out.println(Localization.lang("Exporting preferences to '%0'.", guiCli.getExportPreferences()));
            preferences.exportPreferences(guiCli.getExportPreferences());
        } catch (JabRefException e) {
            System.err.println(e.getLocalizedMessage());
            LOGGER.error("Unable to export preferences", e);
        }
    }

    public boolean shouldShutDown() {
        return !guiNeeded;
    }

    public GuiCommandLine getGuiCli() {
        return guiCli;
    }
}
