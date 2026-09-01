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
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.the306bobby.hachigram.core.configs.HachigramMessagesConfig;
import com.the306bobby.hachigram.core.helpers.DeeplinkHelper;
import com.the306bobby.hachigram.core.ui.CGBulletinCreator;
import com.the306bobby.hachigram.helpers.ui.PopupHelper;
import com.the306bobby.hachigram.preferences.helpers.AlertDialogSwitchers;
import com.the306bobby.hachigram.preferences.helpers.SettingsHelper;

public class MessagesPreferencesEntry extends UniversalFragment {

    private final int messageMenuRow = 1, messageSizeRow = 2, directShareRow = 3,
            hideTimeOnStickersRow = 4, showForwardDateRow = 5, pencilIconForEditedRow = 6;

    private final int geminiSettingsRow = 7, voiceTranscriptionRow = 8;

    private final int messageFilterRow = 9, leftBottomBtnRow = 10, doubleTapRow = 11, slideActionRow = 12, deleteForAllRow = 13;

    private final int reactionsOverlayRow = 14, reactionAnimationRow = 15, tapsOnPremiumStickersRow = 16, premiumStickersAutoplayRow = 17;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.MessagesSettings);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
        items.add(UItem.asButton(messageMenuRow, R.drawable.msg_list, getString(R.string.CP_MessageMenu)));
        items.add(UItem.asButton(messageSizeRow, R.drawable.msg_photo_settings, getString(R.string.CP_Messages_Size)));
        items.add(UItem.asButton(directShareRow, R.drawable.msg_share, getString(R.string.DirectShare)));
        items.add(SettingsHelper.asSwitchCG(hideTimeOnStickersRow, getString(R.string.CP_TimeOnStick))
                .setChecked(HachigramMessagesConfig.INSTANCE.getHideStickerTime())
        );
        items.add(SettingsHelper.asSwitchCG(showForwardDateRow, getString(R.string.CP_ForwardMsgDate))
                .setChecked(HachigramMessagesConfig.INSTANCE.getMsgForwardDate())
        );
        items.add(SettingsHelper.asSwitchCG(pencilIconForEditedRow, getString(R.string.AP_ShowPencilIcon))
                .setChecked(HachigramMessagesConfig.INSTANCE.getShowPencilIcon())
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asButton(geminiSettingsRow, R.drawable.magic_stick_solar, getString(R.string.CP_GeminiAI_Header)));
        items.add(UItem.asButton(voiceTranscriptionRow, getString(R.string.CP_GeminiAI_VoiceTranscriptionProvider), getTranscriptionProviderValue()));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.ActionsChartTitle)));
        items.add(UItem.asButton(messageFilterRow, R.drawable.msg_notspam, getString(R.string.CP_Message_Filtering)));
        items.add(UItem.asButton(leftBottomBtnRow, getString(R.string.CP_LeftBottomButtonAction), getLeftBottomButtonValue()));
        items.add(UItem.asButton(doubleTapRow, getString(R.string.CP_DoubleTapAction), getDoubleTapActionValue()));
        items.add(UItem.asButton(slideActionRow, getString(R.string.CG_MsgSlideAction), getSlideActionValue()));
        items.add(SettingsHelper.asSwitchCG(deleteForAllRow, getString(R.string.CP_DeleteForAll), getString(R.string.CP_DeleteForAll_Desc))
                .setChecked(HachigramMessagesConfig.INSTANCE.getDeleteForAll())
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.TelegramPremium)));
        items.add(SettingsHelper.asSwitchCG(reactionsOverlayRow, getString(R.string.CP_DisableReactionsOverlay), getString(R.string.CP_DisableReactionsOverlay_Desc))
                .setChecked(HachigramMessagesConfig.INSTANCE.getDisableReactionsOverlay())
        );
        items.add(SettingsHelper.asSwitchCG(reactionAnimationRow, getString(R.string.CP_DisableReactionAnim), getString(R.string.CP_DisableReactionAnim_Desc))
                .setChecked(HachigramMessagesConfig.INSTANCE.getDisableReactionAnim())
        );
        items.add(SettingsHelper.asSwitchCG(tapsOnPremiumStickersRow, getString(R.string.CP_DisablePremStickAnim), getString(R.string.CP_DisablePremStickAnim_Desc))
                .setChecked(HachigramMessagesConfig.INSTANCE.getDisablePremStickAnim())
        );
        items.add(SettingsHelper.asSwitchCG(premiumStickersAutoplayRow, getString(R.string.CP_DisablePremStickAutoPlay), getString(R.string.CP_DisablePremStickAutoPlay_Desc))
                .setChecked(HachigramMessagesConfig.INSTANCE.getDisablePremStickAutoPlay())
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == messageMenuRow) {
            HachigramPreferencesNavigator.INSTANCE.createMessageMenu(this);
        } else if (item.id == messageSizeRow) {
            AlertDialogSwitchers.showMessageSize(this);
        } else if (item.id == directShareRow) {
            showDirectShareConfigurator(this);
        } else if (item.id == hideTimeOnStickersRow) {
            HachigramMessagesConfig.INSTANCE.setHideStickerTime(!HachigramMessagesConfig.INSTANCE.getHideStickerTime());
            SettingsHelper.updateCheckState(view, HachigramMessagesConfig.INSTANCE.getHideStickerTime());
        } else if (item.id == showForwardDateRow) {
            HachigramMessagesConfig.INSTANCE.setMsgForwardDate(!HachigramMessagesConfig.INSTANCE.getMsgForwardDate());
            SettingsHelper.updateCheckState(view, HachigramMessagesConfig.INSTANCE.getMsgForwardDate());
        } else if (item.id == pencilIconForEditedRow) {
            HachigramMessagesConfig.INSTANCE.setShowPencilIcon(!HachigramMessagesConfig.INSTANCE.getShowPencilIcon());
            SettingsHelper.updateCheckState(view, HachigramMessagesConfig.INSTANCE.getShowPencilIcon());
        } else if (item.id == geminiSettingsRow) {
            HachigramPreferencesNavigator.INSTANCE.createGemini(this);
        } else if (item.id == voiceTranscriptionRow) {
            showTranscriptionProviderSelector(() -> SettingsHelper.updateButtonValue(view, getTranscriptionProviderValue()));
        } else if (item.id == messageFilterRow) {
            HachigramPreferencesNavigator.INSTANCE.createMessageFilter(this);
        } else if (item.id == leftBottomBtnRow) {
            showLeftBottomButtonSelector(() -> SettingsHelper.updateButtonValue(view, getLeftBottomButtonValue()));
        } else if (item.id == doubleTapRow) {
            showDoubleTapSelector(() -> SettingsHelper.updateButtonValue(view, getDoubleTapActionValue()));
        } else if (item.id == slideActionRow) {
            showSlideActionSelector(() -> SettingsHelper.updateButtonValue(view, getSlideActionValue()));
        } else if (item.id == deleteForAllRow) {
            HachigramMessagesConfig.INSTANCE.setDeleteForAll(!HachigramMessagesConfig.INSTANCE.getDeleteForAll());
            SettingsHelper.updateCheckState(view, HachigramMessagesConfig.INSTANCE.getDeleteForAll());
        } else if (item.id == reactionsOverlayRow) {
            HachigramMessagesConfig.INSTANCE.setDisableReactionsOverlay(!HachigramMessagesConfig.INSTANCE.getDisableReactionsOverlay());
            SettingsHelper.updateCheckState(view, HachigramMessagesConfig.INSTANCE.getDisableReactionsOverlay());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == reactionAnimationRow) {
            HachigramMessagesConfig.INSTANCE.setDisableReactionAnim(!HachigramMessagesConfig.INSTANCE.getDisableReactionAnim());
            SettingsHelper.updateCheckState(view, HachigramMessagesConfig.INSTANCE.getDisableReactionAnim());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == tapsOnPremiumStickersRow) {
            HachigramMessagesConfig.INSTANCE.setDisablePremStickAnim(!HachigramMessagesConfig.INSTANCE.getDisablePremStickAnim());
            SettingsHelper.updateCheckState(view, HachigramMessagesConfig.INSTANCE.getDisablePremStickAnim());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == premiumStickersAutoplayRow) {
            HachigramMessagesConfig.INSTANCE.setDisablePremStickAutoPlay(!HachigramMessagesConfig.INSTANCE.getDisablePremStickAutoPlay());
            SettingsHelper.updateCheckState(view, HachigramMessagesConfig.INSTANCE.getDisablePremStickAutoPlay());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        if (item.id == geminiSettingsRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Gemini);
            return true;
        } else if (item.id == messageMenuRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Message_Menu);
            return true;
        } else if (item.id == messageFilterRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Message_Filters);
            return true;
        }
        return false;
    }

    private void showDirectShareConfigurator(BaseFragment fragment) {
        List<ChatsPreferencesEntry.MenuItemConfig> menuItems = Arrays.asList(
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.RepostToStory),
                        R.drawable.large_repost_story,
                        HachigramMessagesConfig.INSTANCE::getShareDrawStoryButton,
                        () -> HachigramMessagesConfig.INSTANCE.setShareDrawStoryButton(!HachigramMessagesConfig.INSTANCE.getShareDrawStoryButton()),
                        true,
                        false
                ),
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.FilterChats),
                        0,
                        HachigramMessagesConfig.INSTANCE::getUsersDrawShareButton,
                        () -> HachigramMessagesConfig.INSTANCE.setUsersDrawShareButton(!HachigramMessagesConfig.INSTANCE.getUsersDrawShareButton()),
                        false,
                        false
                ),
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.FilterGroups),
                        0,
                        HachigramMessagesConfig.INSTANCE::getSupergroupsDrawShareButton,
                        () -> HachigramMessagesConfig.INSTANCE.setSupergroupsDrawShareButton(!HachigramMessagesConfig.INSTANCE.getSupergroupsDrawShareButton()),
                        false,
                        false
                ),
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.FilterChannels),
                        0,
                        HachigramMessagesConfig.INSTANCE::getChannelsDrawShareButton,
                        () -> HachigramMessagesConfig.INSTANCE.setChannelsDrawShareButton(!HachigramMessagesConfig.INSTANCE.getChannelsDrawShareButton()),
                        false,
                        false
                ),
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.FilterBots),
                        0,
                        HachigramMessagesConfig.INSTANCE::getBotsDrawShareButton,
                        () -> HachigramMessagesConfig.INSTANCE.setBotsDrawShareButton(!HachigramMessagesConfig.INSTANCE.getBotsDrawShareButton()),
                        false,
                        false
                ),
                new ChatsPreferencesEntry.MenuItemConfig(
                        getString(R.string.StickersName),
                        0,
                        HachigramMessagesConfig.INSTANCE::getStickersDrawShareButton,
                        () -> HachigramMessagesConfig.INSTANCE.setStickersDrawShareButton(!HachigramMessagesConfig.INSTANCE.getStickersDrawShareButton()),
                        false,
                        false
                )
        );

        handleMenuAlert(getString(R.string.DirectShare), menuItems, fragment);
    }

    private String getTranscriptionProviderValue() {
        return HachigramMessagesConfig.INSTANCE.getVoiceTranscriptionProvider() == HachigramMessagesConfig.TRANSCRIPTION_PROVIDER_GEMINI
                ? getString(R.string.CP_GeminiAI_Header) : getString(R.string.AppName);
    }

    private void showTranscriptionProviderSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.CP_GeminiAI_Header));
        configValues.add(HachigramMessagesConfig.TRANSCRIPTION_PROVIDER_GEMINI);

        configStringKeys.add(getString(R.string.AppName));
        configValues.add(HachigramMessagesConfig.TRANSCRIPTION_PROVIDER_TELEGRAM);

        PopupHelper.show(configStringKeys, getString(R.string.CP_GeminiAI_VoiceTranscriptionProvider), configValues.indexOf(HachigramMessagesConfig.INSTANCE.getVoiceTranscriptionProvider()), getContext(), i -> {
            HachigramMessagesConfig.INSTANCE.setVoiceTranscriptionProvider(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private String getLeftBottomButtonValue() {
        return switch (HachigramMessagesConfig.INSTANCE.getLeftBottomButton()) {
            case HachigramMessagesConfig.LEFT_BUTTON_REPLY -> getString(R.string.Reply);
            case HachigramMessagesConfig.LEFT_BUTTON_SAVE_MESSAGE -> getString(R.string.CG_ToSaved);
            case HachigramMessagesConfig.LEFT_BUTTON_DIRECT_SHARE -> getString(R.string.DirectShare);
            case HachigramMessagesConfig.LEFT_BUTTON_FORWARD_WO_AUTHORSHIP -> getString(R.string.Forward) + " " + getString(R.string.CG_Without_Authorship);
            default -> getString(R.string.Forward) + " " + getString(R.string.CG_Without_Caption);
        };
    }

    private void showLeftBottomButtonSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.Forward) + " " + getString(R.string.CG_Without_Authorship));
        configValues.add(HachigramMessagesConfig.LEFT_BUTTON_FORWARD_WO_AUTHORSHIP);

        configStringKeys.add(getString(R.string.Forward) + " " + getString(R.string.CG_Without_Caption));
        configValues.add(HachigramMessagesConfig.LEFT_BUTTON_FORWARD_WO_CAPTION);

        configStringKeys.add(getString(R.string.Reply));
        configValues.add(HachigramMessagesConfig.LEFT_BUTTON_REPLY);

        configStringKeys.add(getString(R.string.CG_ToSaved));
        configValues.add(HachigramMessagesConfig.LEFT_BUTTON_SAVE_MESSAGE);

        configStringKeys.add(getString(R.string.DirectShare));
        configValues.add(HachigramMessagesConfig.LEFT_BUTTON_DIRECT_SHARE);

        PopupHelper.show(configStringKeys, getString(R.string.CP_LeftBottomButtonAction), configValues.indexOf(HachigramMessagesConfig.INSTANCE.getLeftBottomButton()), getContext(), i -> {
            HachigramMessagesConfig.INSTANCE.setLeftBottomButton(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private String getDoubleTapActionValue() {
        return switch (HachigramMessagesConfig.INSTANCE.getDoubleTapAction()) {
            case HachigramMessagesConfig.DOUBLE_TAP_ACTION_REACTION -> getString(R.string.Reactions);
            case HachigramMessagesConfig.DOUBLE_TAP_ACTION_REPLY -> getString(R.string.Reply);
            case HachigramMessagesConfig.DOUBLE_TAP_ACTION_SAVE -> getString(R.string.CG_ToSaved);
            case HachigramMessagesConfig.DOUBLE_TAP_ACTION_EDIT -> getString(R.string.Edit);
            case HachigramMessagesConfig.DOUBLE_TAP_ACTION_TRANSLATE -> getString(R.string.TranslateMessage);
            case HachigramMessagesConfig.DOUBLE_TAP_ACTION_TRANSLATE_GEMINI -> getString(R.string.TranslateMessage) + " - " + getString(R.string.CP_GeminiAI_Header);
            default -> getString(R.string.Disable);
        };
    }

    private void showDoubleTapSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.Disable));
        configValues.add(HachigramMessagesConfig.DOUBLE_TAP_ACTION_NONE);

        configStringKeys.add(getString(R.string.Reactions));
        configValues.add(HachigramMessagesConfig.DOUBLE_TAP_ACTION_REACTION);

        configStringKeys.add(getString(R.string.Reply));
        configValues.add(HachigramMessagesConfig.DOUBLE_TAP_ACTION_REPLY);

        configStringKeys.add(getString(R.string.CG_ToSaved));
        configValues.add(HachigramMessagesConfig.DOUBLE_TAP_ACTION_SAVE);

        configStringKeys.add(getString(R.string.Edit));
        configValues.add(HachigramMessagesConfig.DOUBLE_TAP_ACTION_EDIT);

        configStringKeys.add(getString(R.string.TranslateMessage));
        configValues.add(HachigramMessagesConfig.DOUBLE_TAP_ACTION_TRANSLATE);

        configStringKeys.add(getString(R.string.TranslateMessage) + " - " + getString(R.string.CP_GeminiAI_Header));
        configValues.add(HachigramMessagesConfig.DOUBLE_TAP_ACTION_TRANSLATE_GEMINI);

        PopupHelper.show(configStringKeys, getString(R.string.CP_DoubleTapAction), configValues.indexOf(HachigramMessagesConfig.INSTANCE.getDoubleTapAction()), getContext(), i -> {
            HachigramMessagesConfig.INSTANCE.setDoubleTapAction(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private String getSlideActionValue() {
        return switch (HachigramMessagesConfig.INSTANCE.getMessageSlideAction()) {
            case HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_SAVE -> getString(R.string.CG_ToSaved);
            case HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_TRANSLATE -> getString(R.string.TranslateMessage);
            case HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_TRANSLATE_GEMINI -> getString(R.string.TranslateMessage) + " - " + getString(R.string.CP_GeminiAI_Header);
            case HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE -> getString(R.string.DirectShare);
            default -> getString(R.string.Reply);
        };
    }

    private void showSlideActionSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.Reply));
        configValues.add(HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_REPLY);

        configStringKeys.add(getString(R.string.CG_ToSaved));
        configValues.add(HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_SAVE);

        configStringKeys.add(getString(R.string.TranslateMessage));
        configValues.add(HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_TRANSLATE);

        configStringKeys.add(getString(R.string.TranslateMessage) + " - " + getString(R.string.CP_GeminiAI_Header));
        configValues.add(HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_TRANSLATE_GEMINI);

        configStringKeys.add(getString(R.string.DirectShare));
        configValues.add(HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE);

        PopupHelper.show(configStringKeys, getString(R.string.CG_MsgSlideAction), configValues.indexOf(HachigramMessagesConfig.INSTANCE.getMessageSlideAction()), getContext(), i -> {
            HachigramMessagesConfig.INSTANCE.setMessageSlideAction(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private static void handleMenuAlert(String title, List<ChatsPreferencesEntry.MenuItemConfig> items, BaseFragment fragment) {
        ArrayList<String> prefTitle = new ArrayList<>();
        ArrayList<Integer> prefIcon = new ArrayList<>();
        ArrayList<Boolean> prefCheck = new ArrayList<>();
        ArrayList<Boolean> prefCheckInvisible = new ArrayList<>();
        ArrayList<Boolean> prefDivider = new ArrayList<>();
        ArrayList<Runnable> clickListener = new ArrayList<>();

        for (ChatsPreferencesEntry.MenuItemConfig item : items) {
            prefTitle.add(item.title);
            prefIcon.add(item.iconRes);
            prefCheck.add(item.isChecked.get());
            prefCheckInvisible.add(item.isCheckInvisible);
            prefDivider.add(item.divider);
            clickListener.add(item.toggle);
        }

        PopupHelper.showSwitchAlert(
                title,
                fragment,
                prefTitle,
                prefIcon,
                prefCheck,
                prefCheckInvisible,
                prefDivider,
                clickListener,
                null
        );
    }

}
