package org.jabref.cli;

import java.nio.file.Path;
import java.util.List;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static picocli.CommandLine.Command;

@Command(name = "jabref", mixinStandardHelpOptions = true)
public class GuiCommandLine {
    @Parameters(paramLabel = "<FILE>", description = "File(s) to be imported.")
    private List<Path> libraries;

    @Option(names = {"-a", "--add"}, description = "Add to currently opened library.")
    private boolean append;

    /// @deprecated used by the browser extension
    @Deprecated
    @Option(names = {"--importBibtex"}, hidden = true, description = "Import bibtex string.")
    private String importBibtex;

    /// @deprecated used by the browser extension
    @Deprecated
    @Option(names = {"-importToOpen", "--importToOpen"}, hidden = true, description = "Imports and appends the given file/url to the opened tab.")
    private String importToOpen;

    @Option(names = {"-x", "--exportPreferences"}, paramLabel = "FILE", description = "Export preferences.")
    private Path exportPreferences;

    @Option(names = {"-p", "--importPreferences"}, paramLabel = "FILE", description = "Import preferences.")
    private Path importPreferences;

    @Option(names = {"-d", "--reset"}, description = "Reset all preferences to default values.")
    private boolean resetPreferences;

    @Option(names = {"-v", "--version"}, versionHelp = true, description = "Display version info.")
    private boolean versionInfoRequested;

    @Option(names = {"?", "-h", "--help"}, usageHelp = true, description = "Display this help message.")
    private boolean usageHelpRequested;

    @Option(names = {"--debug"}, description = "Enable debug logging.")
    private boolean debugLogging;

    @Option(names = {"-b", "--blank"}, description = "Start with an empty library.")
    private boolean blank;

    @Option(names = {"-j", "--jumpToKey"}, description = "Jump to the entry of the given citation key.")
    private String jumpToKey;

    // --- GETTERS AND SETTERS ---

    public List<Path> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<Path> libraries) {
        this.libraries = libraries;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public String getImportBibtex() {
        return importBibtex;
    }

    public void setImportBibtex(String importBibtex) {
        this.importBibtex = importBibtex;
    }

    public String getImportToOpen() {
        return importToOpen;
    }

    public void setImportToOpen(String importToOpen) {
        this.importToOpen = importToOpen;
    }

    public Path getExportPreferences() {
        return exportPreferences;
    }

    public void setExportPreferences(Path exportPreferences) {
        this.exportPreferences = exportPreferences;
    }

    public Path getImportPreferences() {
        return importPreferences;
    }

    public void setImportPreferences(Path importPreferences) {
        this.importPreferences = importPreferences;
    }

    public boolean isResetPreferences() {
        return resetPreferences;
    }

    public void setResetPreferences(boolean resetPreferences) {
        this.resetPreferences = resetPreferences;
    }

    public boolean isVersionInfoRequested() {
        return versionInfoRequested;
    }

    public void setVersionInfoRequested(boolean versionInfoRequested) {
        this.versionInfoRequested = versionInfoRequested;
    }

    public boolean isUsageHelpRequested() {
        return usageHelpRequested;
    }

    public void setUsageHelpRequested(boolean usageHelpRequested) {
        this.usageHelpRequested = usageHelpRequested;
    }

    public boolean isDebugLogging() {
        return debugLogging;
    }

    public void setDebugLogging(boolean debugLogging) {
        this.debugLogging = debugLogging;
    }

    public boolean isBlank() {
        return blank;
    }

    public void setBlank(boolean blank) {
        this.blank = blank;
    }

    public String getJumpToKey() {
        return jumpToKey;
    }

    public void setJumpToKey(String jumpToKey) {
        this.jumpToKey = jumpToKey;
    }
}
