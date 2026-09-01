/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.preferences.tabs;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

import com.the306bobby.hachigram.core.configs.HachigramAppearanceConfig;
import com.the306bobby.hachigram.core.ui.CGBulletinCreator;
import com.the306bobby.hachigram.core.ui.mainTabs.MainTabsManager;
import com.the306bobby.hachigram.preferences.helpers.SettingsHelper;

public class MainTabsPreferencesEntry extends UniversalFragment {

    private final int enableTabsRow = 1;
    private final int tabsPreviewRow = 2;
    private final int openSettingsBySwipeRow = 3;
    private final int showTabTitleRow = 4;
    private final int forceOpenChats = 5;

    private MainTabsPreviewCell tabsView;
    private ArrayList<MainTabsManager.Tab> tabs;
    private ArrayList<MainTabsManager.Tab> initialTabs;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.CP_MainTabs_Header);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    public boolean onFragmentCreate() {
        initialTabs = new ArrayList<>();
        for (MainTabsManager.Tab t : MainTabsManager.INSTANCE.getAllTabs()) {
            initialTabs.add(new MainTabsManager.Tab(t.getType(), t.enabled));
        }

        tabs = new ArrayList<>(MainTabsManager.INSTANCE.getAllTabs());

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        checkSaveTabs();
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.CP_MainTabs_Layout)));
        if (HachigramAppearanceConfig.INSTANCE.getShowMainTabs()) {
            UItem enableTabs = SettingsHelper.asSwitchCG(
                    enableTabsRow,
                    getString(R.string.CP_MainTabs_ShowTabs),
                    getString(R.string.CP_MainTabs_DoubleTap_Desc)
            ).setChecked(HachigramAppearanceConfig.INSTANCE.getShowMainTabs());
            enableTabs.hideDivider = true;
            items.add(enableTabs);
        } else {
            items.add(SettingsHelper.asSwitchCG(enableTabsRow, getString(R.string.CP_MainTabs_ShowTabs))
                    .setChecked(HachigramAppearanceConfig.INSTANCE.getShowMainTabs())
            );
            items.add(SettingsHelper.asSwitchCG(openSettingsBySwipeRow, getString(R.string.CP_MainTabs_OpenSettings), getString(R.string.CP_MainTabs_OpenSettings_Desc))
                    .setChecked(HachigramAppearanceConfig.INSTANCE.getOpenSettingsBySwipe())
            );
        }

        PreviewCell previewContainer = new PreviewCell(getContext());

        tabsView = new MainTabsPreviewCell(getContext());
        tabsView.setEditMode(true);
        tabsView.setTabs(tabs, getContext(), getResourceProvider(), currentAccount, true, true);

        previewContainer.addView(tabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.CENTER, dp(5), 0, dp(5), 0));

        if (HachigramAppearanceConfig.INSTANCE.getShowMainTabs()) {
            items.add(UItem.asShadow(null));
            items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
            UItem space = SettingsHelper.asSpaceCG(dp(8));
            space.id = -1;
            space.transparent = true;
            items.add(space);
            items.add(SettingsHelper.asCustomWithBackground(tabsPreviewRow, previewContainer, 58));
            items.add(SettingsHelper.asSwitchCG(showTabTitleRow, getString(R.string.CP_MainTabs_ShowTabsTitle))
                    .setChecked(HachigramAppearanceConfig.INSTANCE.getShowMainTabsTitle())
            );
            items.add(UItem.asShadow(getString(R.string.CP_MainTabs_Layout_Desc)));

            items.add(UItem.asHeader(getString(R.string.ActionsChartTitle)));
            items.add(SettingsHelper.asSwitchCG(forceOpenChats, getString(R.string.CP_MainTabs_ForceOpenChats), getString(R.string.CP_MainTabs_ForceOpenChats_Desc))
                    .setChecked(HachigramAppearanceConfig.INSTANCE.getMainTabsForceOpenChats())
            );
        }
    }

    private static class PreviewCell extends FrameLayout {
        private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        public PreviewCell(Context context) {
            super(context);
            setWillNotDraw(false);

            int color = Theme.getColor(Theme.key_switchTrack);
            backgroundPaint.setColor(ColorUtils.setAlphaComponent(color, 20));

            outlinePaint.setStyle(Paint.Style.STROKE);
            outlinePaint.setStrokeWidth(Math.max(2, dp(1f)));
            outlinePaint.setColor(ColorUtils.setAlphaComponent(color, 0x3F));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getMeasuredWidth();
            float h = getMeasuredHeight();

            float radius = dp(50);

            float stroke = outlinePaint.getStrokeWidth() / 2;
            rect.set(stroke + dp(8), stroke, w - stroke - dp(8), h - stroke);

            canvas.drawRoundRect(rect, radius, radius, backgroundPaint);
            canvas.drawRoundRect(rect, radius, radius, outlinePaint);
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == enableTabsRow) {
            HachigramAppearanceConfig.INSTANCE.setShowMainTabs(!HachigramAppearanceConfig.INSTANCE.getShowMainTabs());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getShowMainTabs());

            if (!HachigramAppearanceConfig.INSTANCE.getShowMainTabs()) {
                resetMainTabsOrder();
            }
            listView.adapter.update(true);

            if (HachigramAppearanceConfig.INSTANCE.getFoldersAtBottom()) CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == tabsPreviewRow) {
            /*if (MainTabsManager.getEnabledTabs().size() > 5) {
                HachigramAppearanceConfig.INSTANCE.setShowMainTabsTitle(false);
                listView.adapter.update(true);
            }*/
        } else if (item.id == openSettingsBySwipeRow) {
            HachigramAppearanceConfig.INSTANCE.setOpenSettingsBySwipe(!HachigramAppearanceConfig.INSTANCE.getOpenSettingsBySwipe());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getOpenSettingsBySwipe());

            resetMainTabsOrder();
        } else if (item.id == showTabTitleRow) {
            HachigramAppearanceConfig.INSTANCE.setShowMainTabsTitle(!HachigramAppearanceConfig.INSTANCE.getShowMainTabsTitle());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getShowMainTabsTitle());

            tabsView.removeAllViews();
            tabsView.setTabs(tabs, getContext(), getResourceProvider(), currentAccount, true, true);

            postUpdateTabsNotification();
        } else if (item.id == forceOpenChats) {
            HachigramAppearanceConfig.INSTANCE.setMainTabsForceOpenChats(!HachigramAppearanceConfig.INSTANCE.getMainTabsForceOpenChats());
            SettingsHelper.updateCheckState(view, HachigramAppearanceConfig.INSTANCE.getMainTabsForceOpenChats());
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }


    @Override
    public boolean onBackPressed(boolean invoked) {
        checkSaveTabs();
        return super.onBackPressed(invoked);
    }

    private void checkSaveTabs() {
        MainTabsManager.INSTANCE.saveTabs(tabs);
//        if (MainTabsManager.getEnabledTabs().size() > 5) HachigramAppearanceConfig.INSTANCE.setShowMainTabsTitle(false);

        if (!tabs.equals(initialTabs)) {
            postUpdateTabsNotification();
        }
    }

    private void resetMainTabsOrder() {
        HachigramAppearanceConfig.INSTANCE.setMainTabsOrder("SETTINGS,CHATS,!PROFILE,!CONTACTS,!CALLS,SEARCH");
        HachigramAppearanceConfig.INSTANCE.setShowSearchInTabs(true);
        tabs.clear();
        tabs.addAll(MainTabsManager.INSTANCE.getAllTabs());
        postUpdateTabsNotification();
    }

    private void postUpdateTabsNotification() {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.cgTabsUpdated),
                200
        );
        getParentLayout().rebuildAllFragmentViews(false, false);
    }

}

