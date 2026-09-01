/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;

import com.the306bobby.hachigram.core.configs.HachigramAppearanceConfig;
import com.the306bobby.hachigram.core.helpers.DeeplinkHelper;
import com.the306bobby.hachigram.core.ui.CGBulletinCreator;
import com.the306bobby.hachigram.helpers.ui.PopupHelper;
import com.the306bobby.hachigram.preferences.helpers.SettingsHelper;

public class AppearancePreferencesEntry extends UniversalFragment {

    private final int centerTitleRow = 1;
    private final int hideSearchBar = 2;
    private final int snowflakesRow = 3;

    private final int iconPackRow = 4;
    private final int oneUISwitchesRow = 5;
    private final int disableDividersRow = 6;

    private final int foldersRow = 7;
    private final int bottomTabsRow = 8;
    private final int messagesAndProfilesRow = 9;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.AP_Header_Appearance);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.AP_Header)));
        items.add(SettingsHelper.asSwitchCG(centerTitleRow, getString(R.string.AP_CenterTitle))
                .setChecked(HachigramAppearanceConfig.INSTANCE.getCenterTitle())
        );
        items.add(SettingsHelper.asSwitchCG(hideSearchBar, getString(R.string.AP_HideSearchBar))
                .setChecked(HachigramAppearanceConfig.INSTANCE.getHideSearchFiled())
        );
        items.add(SettingsHelper.asSwitchCG(snowflakesRow, getString(R.string.CP_Snowflakes_Header))
                .setChecked(HachigramAppearanceConfig.INSTANCE.getDrawSnowInActionBar())
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
        items.add(UItem.asButton(iconPackRow, getString(R.string.AP_IconReplacements), getIconPackValueText()));
        items.add(SettingsHelper.asSwitchCG(oneUISwitchesRow, getString(R.string.AP_OneUI_Switch_Style))
                .setChecked(HachigramAppearanceConfig.INSTANCE.getOneUI_SwitchStyle())
        );
        items.add(SettingsHelper.asSwitchCG(disableDividersRow, getString(R.string.AP_DisableDividers))
                .setChecked(HachigramAppearanceConfig.INSTANCE.getDisableDividers())
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        items.add(UItem.asButton(foldersRow, R.drawable.msg_folders, getString(R.string.CP_Filters_Header)));
        items.add(UItem.asButton(bottomTabsRow, R.drawable.tabs_reorder, getString(R.string.CP_MainTabs_Header)));
        items.add(UItem.asButton(messagesAndProfilesRow, R.drawable.msg_customize, getString(R.string.CP_ProfileReplyBackground)));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == centerTitleRow) {
            HachigramAppearanceConfig.INSTANCE.setCenterTitle(!HachigramAppearanceConfig.INSTANCE.getCenterTitle());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getCenterTitle());

            getParentLayout().rebuildAllFragmentViews(true, true);
        } else  if (item.id == hideSearchBar) {
            HachigramAppearanceConfig.INSTANCE.setHideSearchFiled(!HachigramAppearanceConfig.INSTANCE.getHideSearchFiled());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getHideSearchFiled());

            getNotificationCenter().postNotificationName(NotificationCenter.cgUpdateSearchFiledVisibility);
        } else if (item.id == snowflakesRow) {
            HachigramAppearanceConfig.INSTANCE.setDrawSnowInActionBar(!HachigramAppearanceConfig.INSTANCE.getDrawSnowInActionBar());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getDrawSnowInActionBar());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == iconPackRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.Default));
            configValues.add(HachigramAppearanceConfig.ICON_REPLACE_NONE);

            configStringKeys.add(getString(R.string.AP_IconReplacement_Solar));
            configValues.add(HachigramAppearanceConfig.ICON_REPLACE_SOLAR);

            PopupHelper.show(configStringKeys, getString(R.string.AP_IconReplacements), configValues.indexOf(HachigramAppearanceConfig.INSTANCE.getIconReplacement()), getContext(), i -> {
                HachigramAppearanceConfig.INSTANCE.setIconReplacement(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getIconPackValueText());

                if (getParentActivity() instanceof LaunchActivity) {
                    ((LaunchActivity) getParentActivity()).reloadResources();
                }
                Theme.reloadAllResources(getParentActivity());

                getParentLayout().rebuildAllFragmentViews(false, false);
            });
        } else if (item.id == oneUISwitchesRow) {
            HachigramAppearanceConfig.INSTANCE.setOneUI_SwitchStyle(!HachigramAppearanceConfig.INSTANCE.getOneUI_SwitchStyle());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getOneUI_SwitchStyle());

            listView.adapter.update(true);
        } else if (item.id == disableDividersRow) {
            HachigramAppearanceConfig.INSTANCE.setDisableDividers(!HachigramAppearanceConfig.INSTANCE.getDisableDividers());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getDisableDividers());

            Theme.applyCommonTheme();
            listView.adapter.update(true);
        }  else if (item.id == foldersRow) {
            HachigramPreferencesNavigator.INSTANCE.createFoldersPrefs(this);
        } else if (item.id == bottomTabsRow) {
            HachigramPreferencesNavigator.INSTANCE.createTabs(this);
        } else if (item.id == messagesAndProfilesRow) {
            HachigramPreferencesNavigator.INSTANCE.createMessagesAndProfiles(this);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        if (item.id == foldersRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Folders);
            return true;
        } else if (item.id == bottomTabsRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Tabs);
            return true;
        } else if (item.id == messagesAndProfilesRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Messages_And_Profiles);
            return true;
        }
        return false;
    }

    private String getIconPackValueText()  {
        return switch (HachigramAppearanceConfig.INSTANCE.getIconReplacement()) {
            case HachigramAppearanceConfig.ICON_REPLACE_SOLAR -> getString(R.string.AP_IconReplacement_Solar);
            default -> getString(R.string.Default);
        };
    }

}
