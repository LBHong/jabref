package org.jabref.gui.maintable;

import java.util.Collections;
import java.util.List;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;

import org.jabref.Globals;
import org.jabref.gui.BasePanel;
import org.jabref.gui.DialogService;
import org.jabref.gui.actions.ActionFactory;
import org.jabref.gui.actions.Actions;
import org.jabref.gui.actions.OldCommandWrapper;
import org.jabref.gui.actions.StandardActions;
import org.jabref.gui.filelist.AttachFileAction;
import org.jabref.gui.keyboard.KeyBindingRepository;
import org.jabref.gui.menus.ChangeEntryTypeMenu;
import org.jabref.gui.mergeentries.FetchAndMergeEntry;
import org.jabref.gui.specialfields.SpecialFieldMenuItemFactory;
import org.jabref.logic.citationstyle.CitationStyle;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.FieldName;
import org.jabref.model.entry.specialfields.SpecialField;
import org.jabref.preferences.JabRefPreferences;
import org.jabref.preferences.PreviewPreferences;

public class RightClickMenu {

    public static ContextMenu create(BibEntryTableViewModel entry, KeyBindingRepository keyBindingRepository, BasePanel panel, KeyBindingRepository keyBindings, DialogService dialogService) {
        ContextMenu contextMenu = new ContextMenu();
        ActionFactory factory = new ActionFactory(keyBindingRepository);

        contextMenu.getItems().add(factory.createMenuItem(StandardActions.COPY, new OldCommandWrapper(Actions.COPY, panel)));
        contextMenu.getItems().add(createCopySubMenu(panel, factory));
        contextMenu.getItems().add(factory.createMenuItem(StandardActions.PASTE, new OldCommandWrapper(Actions.PASTE, panel)));
        contextMenu.getItems().add(factory.createMenuItem(StandardActions.CUT, new OldCommandWrapper(Actions.CUT, panel)));
        contextMenu.getItems().add(factory.createMenuItem(StandardActions.DELETE, new OldCommandWrapper(Actions.DELETE, panel)));

        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(factory.createMenuItem(StandardActions.SEND_AS_EMAIL, new OldCommandWrapper(Actions.SEND_AS_EMAIL, panel)));

        contextMenu.getItems().add(new SeparatorMenuItem());

        if (Globals.prefs.getBoolean(JabRefPreferences.SPECIALFIELDSENABLED)) {
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_RANKING)) {
                contextMenu.getItems().add(SpecialFieldMenuItemFactory.createSpecialFieldMenu(SpecialField.RANKING, factory, panel));
            }

            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_RELEVANCE)) {
                contextMenu.getItems().add(SpecialFieldMenuItemFactory.getSpecialFieldSingleItem(SpecialField.RELEVANCE, factory, panel));
            }

            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_QUALITY)) {
                contextMenu.getItems().add(SpecialFieldMenuItemFactory.getSpecialFieldSingleItem(SpecialField.QUALITY, factory, panel));
            }

            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_PRINTED)) {
                contextMenu.getItems().add(SpecialFieldMenuItemFactory.getSpecialFieldSingleItem(SpecialField.PRINTED, factory, panel));
            }

            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_PRIORITY)) {
                contextMenu.getItems().add(SpecialFieldMenuItemFactory.createSpecialFieldMenu(SpecialField.PRIORITY, factory, panel));
            }

            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_READ)) {
                contextMenu.getItems().add(SpecialFieldMenuItemFactory.createSpecialFieldMenu(SpecialField.READ_STATUS, factory, panel));
            }
        }

        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(factory.createMenuItem(StandardActions.OPEN_FOLDER, getOpenFolderCommand(panel)));
        contextMenu.getItems().add(factory.createMenuItem(StandardActions.OPEN_EXTERNAL_FILE, getOpenExternalFileCommand(panel)));
        contextMenu.getItems().add(factory.createMenuItem(StandardActions.OPEN_URL, getOpenUrlCommand(panel)));

        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(new ChangeEntryTypeMenu(keyBindings).getChangeEntryTypeMenu(entry.getEntry(), panel.getBibDatabaseContext(), panel.getUndoManager()));
        contextMenu.getItems().add(factory.createMenuItem(StandardActions.MERGE_WITH_FETCHED_ENTRY, getFetchEntryData(panel)));
        contextMenu.getItems().add(factory.createMenuItem(StandardActions.ATTACH_FILE, new AttachFileAction(panel, dialogService)));
        contextMenu.getItems().add(factory.createMenuItem(StandardActions.MERGE_ENTRIES, mergeEntries(panel)));

        contextMenu.getItems().add(new SeparatorMenuItem());

        contextMenu.getItems().add(factory.createMenuItem(StandardActions.ADD_TO_GROUP, addToGroup(panel)));
        contextMenu.getItems().add(factory.createMenuItem(StandardActions.REMOVE_FROM_GROUP, removeFromGroup(panel)));
        contextMenu.getItems().add(factory.createMenuItem(StandardActions.MOVE_TO_GROUP, moveToGroup(panel)));

        return contextMenu;
    }

    private static OldCommandWrapper moveToGroup(BasePanel panel) {
        OldCommandWrapper command = new OldCommandWrapper(Actions.MOVE_TO_GROUP, panel);
        command.setExecutable(areGroupsPresent(panel.getBibDatabaseContext()));
        return command;
    }

    private static OldCommandWrapper removeFromGroup(BasePanel panel) {
        OldCommandWrapper command = new OldCommandWrapper(Actions.REMOVE_FROM_GROUP, panel);
        command.setExecutable(areGroupsPresent(panel.getBibDatabaseContext()));
        return command;
    }

    private static OldCommandWrapper addToGroup(BasePanel panel) {
        OldCommandWrapper command = new OldCommandWrapper(Actions.ADD_TO_GROUP, panel);
        command.setExecutable(areGroupsPresent(panel.getBibDatabaseContext()));
        return command;
    }

    private static OldCommandWrapper mergeEntries(BasePanel panel) {
        OldCommandWrapper command = new OldCommandWrapper(Actions.MERGE_ENTRIES, panel);
        command.setExecutable(panel.getMainTable().getSelectedEntries().size() == 2);
        return command;
    }

    private static OldCommandWrapper getFetchEntryData(BasePanel panel) {
        OldCommandWrapper command = new OldCommandWrapper(Actions.MERGE_WITH_FETCHED_ENTRY, panel);
        command.setExecutable(isAnyFieldSetForSelectedEntry(FetchAndMergeEntry.SUPPORTED_FIELDS, panel));
        return command;
    }

    private static OldCommandWrapper getOpenUrlCommand(BasePanel panel) {
        OldCommandWrapper command = new OldCommandWrapper(Actions.OPEN_URL, panel);
        command.setExecutable(isFieldSetForSelectedEntry(FieldName.URL, panel) || isFieldSetForSelectedEntry(FieldName.DOI, panel));
        return command;
    }

    private static OldCommandWrapper getOpenExternalFileCommand(BasePanel panel) {
        OldCommandWrapper command = new OldCommandWrapper(Actions.OPEN_EXTERNAL_FILE, panel);
        command.setExecutable(isFieldSetForSelectedEntry(FieldName.FILE, panel));
        return command;
    }

    private static OldCommandWrapper getOpenFolderCommand(BasePanel panel) {
        OldCommandWrapper command = new OldCommandWrapper(Actions.OPEN_FOLDER, panel);
        command.setExecutable(isFieldSetForSelectedEntry(FieldName.FILE, panel));
        return command;
    }

    private static Menu createCopySubMenu(BasePanel panel, ActionFactory factory) {
        Menu copySpecialMenu = factory.createMenu(StandardActions.COPY_MORE);
        copySpecialMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_TITLE, new OldCommandWrapper(Actions.COPY_TITLE, panel)));
        copySpecialMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_KEY, new OldCommandWrapper(Actions.COPY_KEY, panel)));
        copySpecialMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_CITE_KEY, new OldCommandWrapper(Actions.COPY_CITE_KEY, panel)));
        copySpecialMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_KEY_AND_TITLE, new OldCommandWrapper(Actions.COPY_KEY_AND_TITLE, panel)));
        copySpecialMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_KEY_AND_LINK, new OldCommandWrapper(Actions.COPY_KEY_AND_LINK, panel)));

        // the submenu will behave dependent on what style is currently selected (citation/preview)
        PreviewPreferences previewPreferences = Globals.prefs.getPreviewPreferences();
        String style = previewPreferences.getPreviewCycle().get(previewPreferences.getPreviewCyclePosition());
        if (CitationStyle.isCitationStyleFile(style)) {
            copySpecialMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_CITATION_HTML, new OldCommandWrapper(Actions.COPY_CITATION_HTML, panel)));
            Menu copyCitationMenu = factory.createMenu(StandardActions.COPY_CITATION_MORE);
            copyCitationMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_CITATION_TEXT, new OldCommandWrapper(Actions.COPY_CITATION_TEXT, panel)));
            copyCitationMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_CITATION_RTF, new OldCommandWrapper(Actions.COPY_CITATION_RTF, panel)));
            copyCitationMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_CITATION_ASCII_DOC, new OldCommandWrapper(Actions.COPY_CITATION_ASCII_DOC, panel)));
            copyCitationMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_CITATION_XSLFO, new OldCommandWrapper(Actions.COPY_CITATION_XSLFO, panel)));
            copySpecialMenu.getItems().add(copyCitationMenu);
        } else {
            copySpecialMenu.getItems().add(factory.createMenuItem(StandardActions.COPY_CITATION_PREVIEW, new OldCommandWrapper(Actions.COPY_CITATION_HTML, panel)));
        }

        copySpecialMenu.getItems().add(factory.createMenuItem(StandardActions.EXPORT_TO_CLIPBOARD, new OldCommandWrapper(Actions.EXPORT_TO_CLIPBOARD, panel)));
        return copySpecialMenu;
    }

    private static boolean areGroupsPresent(BibDatabaseContext database) {
        return database.getMetaData().getGroups().isPresent();
    }

    private static boolean isFieldSetForSelectedEntry(String field, BasePanel panel) {
        return isAnyFieldSetForSelectedEntry(Collections.singletonList(field), panel);
    }

    private static boolean isAnyFieldSetForSelectedEntry(List<String> fields, BasePanel panel) {
        if (panel.getMainTable().getSelectedEntries().size() == 1) {
            BibEntry entry = panel.getMainTable().getSelectedEntries().get(0);
            return !Collections.disjoint(fields, entry.getFieldNames());
        }
        return false;
    }
}
