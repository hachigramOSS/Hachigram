/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.helpers;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.widget.TextView;


import com.google.gson.Gson;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.EmojiPacksAlert;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.Stories.ChannelBoostUtilities;

import java.util.ArrayList;

import com.the306bobby.hachigram.core.configs.HachigramAppearanceConfig;

public class ProfileActivityHelper extends BaseController {

    private static final ProfileActivityHelper[] Instance = new ProfileActivityHelper[UserConfig.MAX_ACCOUNT_COUNT];

    public ProfileActivityHelper(int num) {
        super(num);
    }

    public static ProfileActivityHelper getInstance(int num) {
        ProfileActivityHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (ProfileActivityHelper.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new ProfileActivityHelper(num);
                }
            }
        }
        return localInstance;
    }

    /** Options start */
    public final static int OPTION_RESTART = 1000;
    public final static int OPTION_BOOST_CHANNEL = 1001;
    public final static int OPTION_GET_PROFILE_BACKGROUND = 1002;
    public final static int OPTION_APPLY_PROFILE_BACKGROUND = 1003;
    public final static int OPTION_USER_INFO = 1004;

    public void injectCherryFeats(ActionBarMenuItem otherItem, TLRPC.User user, TLRPC.EncryptedChat currentEncryptedChat, boolean isBot) {
        if (UserObject.isUserSelf(user) || currentEncryptedChat != null || isBot) return;
        if (!HachigramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji()) return;

        long emojiDocumentId = UserObject.getProfileEmojiId(user);
        boolean canGetPack = emojiDocumentId != 0;
        boolean canApplyBackground = getUserConfig().isPremium()
                && UserObject.getProfileEmojiId(getUserConfig().getCurrentUser()) != emojiDocumentId;
        if (!canGetPack && !canApplyBackground) return;

        otherItem.addColoredGap();

        if (canGetPack) {
            otherItem.addSubItem(ProfileActivityHelper.OPTION_GET_PROFILE_BACKGROUND, R.drawable.msg_emoji_stickers, getString(R.string.CG_GetEmojiPack));
        }
        if (canApplyBackground) {
            otherItem.addSubItem(ProfileActivityHelper.OPTION_APPLY_PROFILE_BACKGROUND, R.drawable.msg_emoji_stickers, getString(R.string.CG_ProfileBackground));
        }
    }

    public void injectCherryInfo(ActionBarMenuItem otherItem) {
        otherItem.addColoredGap();

        otherItem.addSubItem(ProfileActivityHelper.OPTION_USER_INFO, R.drawable.icon_json_solar, getString(R.string.Info));
    }

    public void injectPhoneNumber(
            BaseFragment fragment,
            ItemOptions itemOptions,
            String phone
    ) {
        itemOptions.addGap();

        TextView phoneInfoView = new TextView(fragment.getContext());
        phoneInfoView.setPadding(AndroidUtilities.dp(13), AndroidUtilities.dp(8), AndroidUtilities.dp(13), AndroidUtilities.dp(8));
        phoneInfoView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        phoneInfoView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, fragment.getResourceProvider()));
        phoneInfoView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText, fragment.getResourceProvider()));
        phoneInfoView.setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector, fragment.getResourceProvider()), 0,6));

        boolean isFragmentPhoneNumber = phone != null && phone.matches("888\\d{8}");

        String phoneInfoString = LocaleController.getString(isFragmentPhoneNumber ? R.string.AnonymousNumber : R.string.PhoneMobile) +
                ": " +
                "*" +
                PhoneFormat.getInstance().format("+" + phone) +
                "*";

        SpannableStringBuilder spanned = new SpannableStringBuilder(AndroidUtilities.replaceTags(phoneInfoString));

        int startIndex = TextUtils.indexOf(spanned, '*');
        int lastIndex = TextUtils.lastIndexOf(spanned, '*');
        if (startIndex != -1 && lastIndex != -1 && startIndex != lastIndex) {
            spanned.replace(lastIndex, lastIndex + 1, "");
            spanned.replace(startIndex, startIndex + 1, "");
            spanned.setSpan(new TypefaceSpan(AndroidUtilities.bold()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanned.setSpan(new ForegroundColorSpan(phoneInfoView.getLinkTextColors().getDefaultColor()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        phoneInfoView.setText(spanned);
        phoneInfoView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + phone));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                fragment.getParentActivity().startActivityForResult(intent, 500);
            } catch (Exception e) {
                FileLog.e(e);
            }
            itemOptions.dismiss();
        });

        itemOptions.addView(phoneInfoView);
    }

    public void boostChannel(Context context, long dialogID) {
        Browser.openUrl(context, ChannelBoostUtilities.createLink(currentAccount, dialogID));
    }

    public void getProfileBackground(BaseFragment fragment, long dialogID) {
        if (fragment == null || fragment.getContext() == null || fragment.getResourceProvider() == null) {
            return;
        }

        long emojiDocumentId = UserObject.getProfileEmojiId(getMessagesController().getUser(dialogID));

        AnimatedEmojiDrawable.getDocumentFetcher(currentAccount).fetchDocument(emojiDocumentId, document -> AndroidUtilities.runOnUIThread(() -> {
            ArrayList<TLRPC.InputStickerSet> inputSets = new ArrayList<>(1);
            inputSets.add(MessageObject.getInputStickerSet(document));
            EmojiPacksAlert alert = new EmojiPacksAlert(fragment, fragment.getContext(), fragment.getResourceProvider(), inputSets);
            alert.show();
        }));
    }

    public void applyProfileBackground(BaseFragment fragment, long dialogID) {
        long emojiDocumentId = UserObject.getProfileEmojiId(getMessagesController().getUser(dialogID));
        int colorId = UserObject.getProfileColorId(getMessagesController().getUser(dialogID));
        TLRPC.User me = getUserConfig().getCurrentUser();

        if (me.profile_color == null) {
            me.profile_color = new TLRPC.PeerColor();
        }
        TL_account.updateColor req = new TL_account.updateColor();
        req.for_profile = true;
        me.flags2 |= 512;

        if (colorId >= 0) {
            me.profile_color.flags |= 1;
            if (req.color == null) {
                req.flags |= 4;
                req.color = new TLRPC.TL_peerColor();
            }
            req.color.flags |= 1;
            req.color.color = me.profile_color.color = colorId;
        } else {
            me.profile_color.flags &= ~1;
        }

        if (emojiDocumentId != 0) {
            me.profile_color.flags |= 2;
            if (req.color == null) {
                req.flags |= 4;
                req.color = new TLRPC.TL_peerColor();
            }
            req.color.flags |= 2;
            req.color.background_emoji_id = me.profile_color.background_emoji_id = emojiDocumentId;
        } else {
            me.profile_color.flags &= ~2;
            me.profile_color.background_emoji_id = 0;
            if (req.color != null) {
                req.color.flags &= ~2;
                req.color.background_emoji_id = 0;
            }
        }

        getConnectionsManager().sendRequest(req, (res, err) -> {
            if (res != null) {
                AndroidUtilities.runOnUIThread(() -> fragment.presentFragment(new PeerColorActivity(0).startOnProfile().setOnApplied(fragment)), 300);
            }
        });
    }

    public void showRestrictionReason(BaseFragment baseFragment, TLRPC.Chat chat) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                baseFragment.getParentActivity(),
                baseFragment.getResourceProvider()
        );
        builder.setTitle(getString(R.string.Info));

        if (getRestrictionReasons(chat.restriction_reason) != null && getRestrictionReasons(chat.restriction_reason).length() > 0) {
            builder.setMessage(getRestrictionReasons(chat.restriction_reason));
        } else {
            builder.setMessage("chat or channel is not restricted.");
        }

        builder.setPositiveButton(getString(R.string.OK), null);

        baseFragment.showDialog(builder.create());
    }

    public static String getRestrictionReasons(ArrayList<TLRPC.RestrictionReason> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return null;
        }
        FileLog.d("причины: " + new Gson().toJson(reasons));

        StringBuilder sb = new StringBuilder();

        for (TLRPC.RestrictionReason reason : reasons) {
            sb.append("Platform: ").append(reason.platform)
                    .append("\nReason: ").append(reason.reason)
                    .append("\nText: ").append(reason.text)
                    .append("\n\n");
        }

        return sb.toString().trim();
    }
    /** Options finish */

}
