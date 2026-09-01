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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatThemeController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.Paint.PersistColorPalette;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.RestrictedLanguagesSelectActivity;

import java.util.ArrayList;

import com.the306bobby.hachigram.core.configs.HachigramCoreConfig;
import com.the306bobby.hachigram.core.configs.HachigramDebugConfig;
import com.the306bobby.hachigram.core.ui.CGBulletinCreator;
import com.the306bobby.hachigram.helpers.ui.PopupHelper;
import com.the306bobby.hachigram.preferences.helpers.SettingsHelper;

public class DebugPreferencesEntry extends UniversalFragment {

    private final int toastRpcRow = 1;
    private final int oldTimeStyleRow = 2;
    private final int performanceClassRow = 4;
    private final int fixCallsNotifRow = 5;

    private final int newBlurRow = 6;

    private final int forceForumTabsRow = 7;
    private final int replacePunctuationRow = 8;
    private final int editTextFixRow = 9;
    private final int audioSourceRow = 10;
    private final int sendMaxQualityRow = 11;
    private final int playGifAsVideoRow = 12;
    private final int hideTimestampRow = 13;
    private final int resetDialogsRow = 15;
    private final int clearMediaCacheRow = 16;
    private final int readAllDialogsRow = 17;

    private final int importContactsRow = 18;
    private final int reloadContactsRow = 19;
    private final int resetContactsRow = 20;

    @Override
    protected CharSequence getTitle() {
        return "debug // wip";
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("misc"));
        if (!HachigramCoreConfig.isStandaloneStableBuild() && !HachigramCoreConfig.isPlayStoreBuild()) {
            items.add(SettingsHelper.asSwitchCG(toastRpcRow, "toast all rpc errors *", "you'll see rpc errors from telegram's backend as toast messages.")
                    .setChecked(HachigramDebugConfig.INSTANCE.getShowRPCErrors())
            );
        }
        items.add(SettingsHelper.asSwitchCG(oldTimeStyleRow, "default time style in chats *", "unlike ios and tdesktop")
                .setChecked(HachigramDebugConfig.INSTANCE.getOldTimeStyle())
        );
        items.add(UItem.asButton(performanceClassRow, "force performance class", SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass())));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            items.add(UItem.asButton(fixCallsNotifRow, "fix calls notification *"));
        }
        items.add(UItem.asShadow(null));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
            items.add(SettingsHelper.asSwitchCG(newBlurRow, "new blur (gpu)")
                    .setChecked(SharedConfig.useNewBlur)
            );
            items.add(UItem.asShadow(null));
        }

        items.add(UItem.asHeader(getString(R.string.FilterChats)));
        items.add(SettingsHelper.asSwitchCG(forceForumTabsRow, "force forum tabs")
                .setChecked(SharedConfig.forceForumTabs)
        );
        items.add(SettingsHelper.asSwitchCG(replacePunctuationRow, "replace punctuation marks *", "replace quotation marks and dashes like on tdesktop")
                .setChecked(HachigramDebugConfig.INSTANCE.getReplacePunctuationMarks())
        );
        items.add(SettingsHelper.asSwitchCG(editTextFixRow, "edittextsugestionsfix *", "emojis/formatting disappear when samsung puts suggestions in edit")
                .setChecked(HachigramDebugConfig.INSTANCE.getEditTextSuggestionsFix())
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            items.add(UItem.asButton(audioSourceRow, "microphone audio source *", getAudioSourceValue()));
        }
        items.add(SettingsHelper.asSwitchCG(sendMaxQualityRow, "send videos at max quality *", "max quality will be automatically selected when you send a video")
                .setChecked(HachigramDebugConfig.INSTANCE.getSendVideosAtMaxQuality())
        );
        items.add(SettingsHelper.asSwitchCG(playGifAsVideoRow, "play gifs as videos *")
                .setChecked(HachigramDebugConfig.INSTANCE.getPlayGIFsAsVideos())
        );
        items.add(SettingsHelper.asSwitchCG(hideTimestampRow, "hide video timestamp *", "saved progress for videos. return exactly where you left off.")
                .setChecked(HachigramDebugConfig.INSTANCE.getHideVideoTimestamp())
        );

        items.add(UItem.asButton(resetDialogsRow, 0, getString(R.string.DebugMenuResetDialogs)));
        items.add(UItem.asButton(clearMediaCacheRow, 0, getString(R.string.DebugMenuClearMediaCache)));
        items.add(UItem.asButton(readAllDialogsRow, 0, getString(R.string.DebugMenuReadAllDialogs)));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.Contacts)));
        items.add(UItem.asButton(importContactsRow, 0, getString(R.string.DebugMenuImportContacts)));
        items.add(UItem.asButton(reloadContactsRow, 0, getString(R.string.DebugMenuReloadContacts)));
        items.add(UItem.asButton(resetContactsRow, 0, getString(R.string.DebugMenuResetContacts)));
        items.add(UItem.asShadow("* hachigram's feature."));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == toastRpcRow) {
            HachigramDebugConfig.INSTANCE.setShowRPCErrors(!HachigramDebugConfig.INSTANCE.getShowRPCErrors());
            SettingsHelper.updateCheckState(view, HachigramDebugConfig.INSTANCE.getShowRPCErrors());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == oldTimeStyleRow) {
            HachigramDebugConfig.INSTANCE.setOldTimeStyle(!HachigramDebugConfig.INSTANCE.getOldTimeStyle());
            SettingsHelper.updateCheckState(view, HachigramDebugConfig.INSTANCE.getOldTimeStyle());
        } else if (item.id == performanceClassRow) {
            showPerformanceClassDialog(view);
        } else if (item.id == fixCallsNotifRow) {
            openFullScreenIntentSettings();
        } else if (item.id == newBlurRow) {
            SharedConfig.toggleUseNewBlur();
            SettingsHelper.updateCheckState(view, SharedConfig.useNewBlur);
        } else if (item.id == forceForumTabsRow) {
            SharedConfig.toggleForceForumTabs();
            SettingsHelper.updateCheckState(view, SharedConfig.forceForumTabs);
        } else if (item.id == replacePunctuationRow) {
            HachigramDebugConfig.INSTANCE.setReplacePunctuationMarks(!HachigramDebugConfig.INSTANCE.getReplacePunctuationMarks());
            SettingsHelper.updateCheckState(view, HachigramDebugConfig.INSTANCE.getReplacePunctuationMarks());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == editTextFixRow) {
            HachigramDebugConfig.INSTANCE.setEditTextSuggestionsFix(!HachigramDebugConfig.INSTANCE.getEditTextSuggestionsFix());
            SettingsHelper.updateCheckState(view, HachigramDebugConfig.INSTANCE.getEditTextSuggestionsFix());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == audioSourceRow) {
            showAudioSourceDialog(() -> SettingsHelper.updateButtonValue(view, getAudioSourceValue()));
        } else if (item.id == sendMaxQualityRow) {
            HachigramDebugConfig.INSTANCE.setSendVideosAtMaxQuality(!HachigramDebugConfig.INSTANCE.getSendVideosAtMaxQuality());
            SettingsHelper.updateCheckState(view, HachigramDebugConfig.INSTANCE.getSendVideosAtMaxQuality());
        } else if (item.id == playGifAsVideoRow) {
            HachigramDebugConfig.INSTANCE.setPlayGIFsAsVideos(!HachigramDebugConfig.INSTANCE.getPlayGIFsAsVideos());
            SettingsHelper.updateCheckState(view, HachigramDebugConfig.INSTANCE.getPlayGIFsAsVideos());
        } else if (item.id == hideTimestampRow) {
            HachigramDebugConfig.INSTANCE.setHideVideoTimestamp(!HachigramDebugConfig.INSTANCE.getHideVideoTimestamp());
            SettingsHelper.updateCheckState(view, HachigramDebugConfig.INSTANCE.getHideVideoTimestamp());
        } else if (item.id == resetDialogsRow) {
            getMessagesController().forceResetDialogs();

            CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
        } else if (item.id == clearMediaCacheRow) {
            clearMediaCache();
        } else if (item.id == readAllDialogsRow) {
            getMessagesStorage().readAllDialogs(-1);

            CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
        } else if (item.id == importContactsRow) {
            getUserConfig().syncContacts = true;
            getUserConfig().saveConfig(false);
            getContactsController().forceImportContacts();

            CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
        } else if (item.id == reloadContactsRow) {
            getContactsController().loadContacts(false, 0);

            CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
        } else if (item.id == resetContactsRow) {
            getContactsController().resetImportedContacts();

            CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private void showPerformanceClassDialog(View view) {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
        builder2.setTitle("force performance class");
        int currentClass = SharedConfig.getDevicePerformanceClass();
        int trueClass = SharedConfig.measureDevicePerformanceClass();
        builder2.setItems(new CharSequence[]{
                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? "**high**" : "high") + (trueClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? " (measured)" : "")),
                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? "**average**" : "average") + (trueClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? " (measured)" : "")),
                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_LOW ? "**low**" : "low") + (trueClass == SharedConfig.PERFORMANCE_CLASS_LOW ? " (measured)" : ""))
        }, (dialog2, which2) -> {
            int newClass = 2 - which2;
            if (newClass == trueClass) {
                SharedConfig.overrideDevicePerformanceClass(-1);
            } else {
                SharedConfig.overrideDevicePerformanceClass(newClass);
            }

            SettingsHelper.updateButtonValue(view, SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass()));

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        });
        builder2.setNegativeButton(getString(R.string.Cancel), null);
        builder2.show();
    }

    private void openFullScreenIntentSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
        getParentActivity().startActivity(intent);
    }

    private void showAudioSourceDialog(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add("default");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_DEFAULT);

        configStringKeys.add("camcorder");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_CAMCORDER);

        configStringKeys.add("mic");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_MIC);

        configStringKeys.add("remote_submix");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX);

        configStringKeys.add("unprocessed");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_UNPROCESSED);

        configStringKeys.add("voice_call");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_VOICE_CALL);

        configStringKeys.add("voice_communication");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION);

        configStringKeys.add("voice_downlink");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK);

        configStringKeys.add("voice_performance");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE);

        configStringKeys.add("voice_recognition");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION);

        configStringKeys.add("voice_uplink");
        configValues.add(HachigramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK);

        PopupHelper.show(configStringKeys, "microphone audio source *", configValues.indexOf(HachigramDebugConfig.INSTANCE.getAudioSource()), getContext(), i -> {
            HachigramDebugConfig.INSTANCE.setAudioSource(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private String getAudioSourceValue() {
        return switch (HachigramDebugConfig.INSTANCE.getAudioSource()) {
            case HachigramDebugConfig.AUDIO_SOURCE_CAMCORDER -> "camcorder";
            case HachigramDebugConfig.AUDIO_SOURCE_MIC -> "mic";
            case HachigramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX -> "remote_submix";
            case HachigramDebugConfig.AUDIO_SOURCE_UNPROCESSED -> "unprocessed";
            case HachigramDebugConfig.AUDIO_SOURCE_VOICE_CALL -> "voice_call";
            case HachigramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION -> "voice_communication";
            case HachigramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK -> "voice_downlink";
            case HachigramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE -> "voice_performance";
            case HachigramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION -> "voice_recognition";
            case HachigramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK -> "voice_uplink";
            default -> "default";
        };
    }

    private void clearMediaCache() {
        getMessagesStorage().clearSentMedia();
        SharedConfig.setNoSoundHintShowed(false);
        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
        editor.remove("archivehint").remove("proximityhint").remove("archivehint_l").remove("searchpostsnew").remove("speedhint").remove("gifhint").remove("reminderhint").remove("soundHint").remove("themehint").remove("bganimationhint").remove("filterhint").remove("n_0").remove("storyprvhint").remove("storyhint").remove("storyhint2").remove("storydualhint").remove("storysvddualhint").remove("stories_camera").remove("dualcam").remove("dualmatrix").remove("dual_available").remove("archivehint").remove("askNotificationsAfter").remove("askNotificationsDuration").remove("viewoncehint").remove("voicepausehint").remove("taptostorysoundhint").remove("nothanos").remove("voiceoncehint").remove("savedhint").remove("savedsearchhint").remove("savedsearchtaghint").remove("groupEmojiPackHintShown").remove("newppsms").remove("monetizationadshint").remove("seekSpeedHintShowed").remove("unsupport_video/av01").remove("channelgifthint").remove("statusgiftpage").remove("multistorieshint").remove("channelsuggesthint").remove("trimvoicehint").remove("taptostoryhighlighthint").apply();
        MessagesController.getEmojiSettings(currentAccount).edit().remove("featured_hidden").remove("emoji_featured_hidden").apply();
        SharedConfig.textSelectionHintShows = 0;
        SharedConfig.lockRecordAudioVideoHint = 0;
        SharedConfig.stickersReorderingHintUsed = false;
        SharedConfig.forwardingOptionsHintShown = false;
        SharedConfig.replyingOptionsHintShown = false;
        SharedConfig.messageSeenHintCount = 3;
        SharedConfig.emojiInteractionsHintCount = 3;
        SharedConfig.dayNightThemeSwitchHintCount = 3;
        SharedConfig.fastScrollHintCount = 3;
        SharedConfig.stealthModeSendMessageConfirm = 2;
        SharedConfig.updateStealthModeSendMessageConfirm(2);
        SharedConfig.setStoriesReactionsLongPressHintUsed(false);
        SharedConfig.setStoriesIntroShown(false);
        SharedConfig.setMultipleReactionsPromoShowed(false);
        ChatThemeController.getInstance(currentAccount).clearCache();
        getNotificationCenter().postNotificationName(NotificationCenter.newSuggestionsAvailable);
        RestrictedLanguagesSelectActivity.cleanup();
        PersistColorPalette.getInstance(currentAccount).cleanup();
        SharedPreferences prefs = getMessagesController().getMainSettings();
        editor = prefs.edit();
        editor.remove("peerColors").remove("profilePeerColors").remove("boostingappearance").remove("bizbothint").remove("movecaptionhint");
        for (String key : prefs.getAll().keySet()) {
            if (key.contains("show_gift_for_") || key.contains("bdayhint_") || key.contains("bdayanim_") || key.startsWith("ask_paid_message_") || key.startsWith("topicssidetabs")) {
                editor.remove(key);
            }
        }
        editor.apply();
        editor = MessagesController.getNotificationsSettings(currentAccount).edit();
        for (String key : MessagesController.getNotificationsSettings(currentAccount).getAll().keySet()) {
            if (key.startsWith("dialog_bar_botver")) {
                editor.remove(key);
            }
        }
        editor.apply();

        CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
    }

}
