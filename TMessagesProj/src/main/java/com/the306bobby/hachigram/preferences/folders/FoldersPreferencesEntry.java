/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.preferences.folders;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

import com.the306bobby.hachigram.core.configs.HachigramAppearanceConfig;
import com.the306bobby.hachigram.core.ui.CGBulletinCreator;
import com.the306bobby.hachigram.helpers.ui.PopupHelper;
import com.the306bobby.hachigram.preferences.folders.cells.FoldersPreviewCell;
import com.the306bobby.hachigram.preferences.helpers.SettingsHelper;

public class FoldersPreferencesEntry extends UniversalFragment {

    protected FoldersPreviewCell foldersPreviewCell;

    private final int hideAllChatsTabRow = 1;

    private final int hideCounterRow = 2;
    private final int tabIconTypeRow = 3;
    private final int addStrokeRow = 4;

    private final int folderNameAppHeaderRow = 5;
    private final int foldersAtBottomRow = 6;

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.CP_Filters_Header);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        foldersPreviewCell = new FoldersPreviewCell(getContext());
        foldersPreviewCell.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        items.add(SettingsHelper.asCustomWithBackground(foldersPreviewCell));
        items.add(UItem.asShadow(null));

        items.add(SettingsHelper.asSwitchCG(hideAllChatsTabRow, getString(R.string.CP_NewTabs_RemoveAllChats))
                .setChecked(HachigramAppearanceConfig.INSTANCE.getTabsHideAllChats())
        );
        items.add(SettingsHelper.asSwitchCG(hideCounterRow, getString(R.string.CP_NewTabs_NoCounter))
                .setChecked(HachigramAppearanceConfig.INSTANCE.getTabsNoUnread())
        );
        items.add(UItem.asButton(tabIconTypeRow, getString(R.string.AP_Tab_Style), getTabModeValue()));
        items.add(SettingsHelper.asSwitchCG(addStrokeRow, getString(R.string.AP_Tab_Style_Stroke))
                .setChecked(HachigramAppearanceConfig.INSTANCE.getTabStyleStroke())
        );
        items.add(UItem.asShadow(null));

        items.add(SettingsHelper.asSwitchCG(folderNameAppHeaderRow, getString(R.string.AP_FolderNameInHeader), getString(R.string.AP_FolderNameInHeader_Desc))
                .setChecked(HachigramAppearanceConfig.INSTANCE.getFolderNameInHeader())
        );
        items.add(SettingsHelper.asSwitchCG(foldersAtBottomRow, getString(R.string.AP_FoldersAtBottom))
                .setChecked(HachigramAppearanceConfig.INSTANCE.getFoldersAtBottom())
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == hideAllChatsTabRow) {
            HachigramAppearanceConfig.INSTANCE.setTabsHideAllChats(!HachigramAppearanceConfig.INSTANCE.getTabsHideAllChats());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getTabsHideAllChats());

            foldersPreviewCell.updateAllChatsTabName(true);

            parentLayout.rebuildAllFragmentViews(false, false);

            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
        } else if (item.id == hideCounterRow) {
            HachigramAppearanceConfig.INSTANCE.setTabsNoUnread(!HachigramAppearanceConfig.INSTANCE.getTabsNoUnread());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getTabsNoUnread());

            foldersPreviewCell.updateTabCounter(true);

            parentLayout.rebuildAllFragmentViews(false, false);

            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
        } else if (item.id == tabIconTypeRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.CG_FoldersTypeIconsTitles));
            configValues.add(HachigramAppearanceConfig.TAB_TYPE_MIX);

            configStringKeys.add(getString(R.string.CG_FoldersTypeTitles));
            configValues.add(HachigramAppearanceConfig.TAB_TYPE_TEXT);

            configStringKeys.add(getString(R.string.CG_FoldersTypeIcons));
            configValues.add(HachigramAppearanceConfig.TAB_TYPE_ICON);

            PopupHelper.show(configStringKeys, getString(R.string.AP_Tab_Style), configValues.indexOf(HachigramAppearanceConfig.INSTANCE.getTabMode()), getContext(), i -> {
                HachigramAppearanceConfig.INSTANCE.setTabMode(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getTabModeValue());

                foldersPreviewCell.updateTabIcons(true);
                foldersPreviewCell.updateTabTitle(true);

                parentLayout.rebuildAllFragmentViews(false, false);

                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            });
        } else if (item.id == addStrokeRow) {
            HachigramAppearanceConfig.INSTANCE.setTabStyleStroke(!HachigramAppearanceConfig.INSTANCE.getTabStyleStroke());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getTabStyleStroke());

            foldersPreviewCell.invalidate();
            parentLayout.rebuildAllFragmentViews(false, false);
        } else if (item.id == folderNameAppHeaderRow) {
            HachigramAppearanceConfig.INSTANCE.setFolderNameInHeader(!HachigramAppearanceConfig.INSTANCE.getFolderNameInHeader());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getFolderNameInHeader());

            parentLayout.rebuildAllFragmentViews(false, false);

            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
        } else if (item.id == foldersAtBottomRow) {
            HachigramAppearanceConfig.INSTANCE.setFoldersAtBottom(!HachigramAppearanceConfig.INSTANCE.getFoldersAtBottom());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getFoldersAtBottom());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private String getTabModeValue() {
        return switch (HachigramAppearanceConfig.INSTANCE.getTabMode()) {
            case HachigramAppearanceConfig.TAB_TYPE_MIX -> getString(R.string.CG_FoldersTypeIconsTitles);
            case HachigramAppearanceConfig.TAB_TYPE_ICON -> getString(R.string.CG_FoldersTypeIcons);
            default -> getString(R.string.CG_FoldersTypeTitles);
        };
    }

}
