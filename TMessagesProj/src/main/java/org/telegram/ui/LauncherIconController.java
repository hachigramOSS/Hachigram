package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    public static void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.HACHIGRAM);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.HACHIGRAM;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public enum LauncherIcon {
        HACHIGRAM("CG_Icon_Hachigram", R.drawable.icon_background_hachigram, R.mipmap.icon_foreground_hachigram, R.string.Default),
        HACHIGRAM_CAP("CG_Icon_Hachigram_Cap", R.drawable.icon_background_hachigram_cap, R.mipmap.icon_foreground_hachigram_cap, R.string.AP_ChangeIcon_Hachigram_Cap),
        HACHIGRAM_CAP_DARK("CG_Icon_Hachigram_Cap_Dark", R.drawable.icon_background_hachigram_cap_dark, R.mipmap.icon_foreground_hachigram_cap_dark, R.string.AP_ChangeIcon_Hachigram_Cap_Dark),
        HACHIGRAM_CAP_OUTLINE("CG_Icon_Hachigram_Cap_Outline", R.drawable.icon_background_hachigram_cap_outline, R.mipmap.icon_foreground_hachigram_cap_outline, R.string.AP_ChangeIcon_Hachigram_Cap_Outline),
        HACHIGRAM_LINEART("CG_Icon_Hachigram_Lineart", R.drawable.icon_background_hachigram_lineart, R.mipmap.icon_foreground_hachigram_lineart, R.string.AP_ChangeIcon_Hachigram_Lineart),
        HACHIGRAM_LINEART_DARK("CG_Icon_Hachigram_Lineart_Dark", R.drawable.icon_background_hachigram_lineart_dark, R.mipmap.icon_foreground_hachigram_lineart_dark, R.string.AP_ChangeIcon_Hachigram_Lineart_Dark),
        HACHIGRAM_SKETCH("CG_Icon_Hachigram_Sketch", R.drawable.icon_background_hachigram_sketch, R.mipmap.icon_foreground_hachigram_sketch, R.string.AP_ChangeIcon_Hachigram_Sketch),
        HACHIGRAM_TILE("CG_Icon_Hachigram_Tile", R.drawable.icon_background_hachigram_tile, R.mipmap.icon_foreground_hachigram_tile, R.string.AP_ChangeIcon_Hachigram_Tile),
        PREMIUM("PremiumIcon", R.drawable.icon_3_background_sa, R.mipmap.icon_3_foreground, R.string.AppIconPremium, true),
        TURBO("TurboIcon", R.drawable.icon_5_background_sa, R.mipmap.icon_5_foreground, R.string.AppIconTurbo, true),
        NOX("NoxIcon", R.mipmap.icon_2_background_sa, R.drawable.icon_foreground, R.string.AppIconNox, true);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "com.the306bobby.hachigram." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
        }
    }
}
