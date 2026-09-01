/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.core.updater;

import org.telegram.messenger.Utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Which releases a given build is allowed to update to.
 *
 * Release candidates are tagged 0.&lt;major&gt;.&lt;minor&gt;[.&lt;rc&gt;] and target the stable
 * &lt;major&gt;.&lt;minor&gt;. A candidate build follows later candidates and accepts the
 * stable release it was leading up to, but never an earlier stable one. A stable
 * build is never offered a candidate.
 */
public class UpdateChannel {

    private static final Pattern RELEASE_CANDIDATE =
            Pattern.compile("^0\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?$");

    public static boolean isReleaseCandidate(String version) {
        return version != null && RELEASE_CANDIDATE.matcher(version.trim()).matches();
    }

    /** The stable version a candidate is working towards, e.g. 0.1.1.1 -> 1.1. */
    private static String targetOf(String candidate) {
        Matcher m = RELEASE_CANDIDATE.matcher(candidate.trim());
        if (!m.matches()) {
            return candidate;
        }
        return m.group(1) + "." + m.group(2);
    }

    public static int compare(String a, String b) {
        String[] left = a.trim().split("\\.");
        String[] right = b.trim().split("\\.");
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int l = i < left.length ? Utilities.parseInt(left[i]) : 0;
            int r = i < right.length ? Utilities.parseInt(right[i]) : 0;
            if (l != r) {
                return Integer.compare(l, r);
            }
        }
        return 0;
    }

    public static boolean shouldOffer(String current, String candidate) {
        if (current == null || candidate == null || candidate.isEmpty()) {
            return false;
        }
        if (isReleaseCandidate(candidate)) {
            return isReleaseCandidate(current) && compare(candidate, current) > 0;
        }
        if (isReleaseCandidate(current)) {
            return compare(candidate, targetOf(current)) >= 0;
        }
        return compare(candidate, current) > 0;
    }
}
