package com.the306bobby.hachigram.helpers;

/**
 * Ported from inugram's ChatHelper.sanitizeMessageText (teidesu/inugram, MIT).
 */
public final class MessageTextSanitizer {

    private MessageTextSanitizer() {
    }

    // Caps combining marks at 2 per base character like Telegram Desktop does, and drops
    // isolate controls and marks with nothing visible to attach to. Dropped code units become
    // U+200C so offsets of entities stay valid.
    public static String sanitizeMessageText(String text) {
        char[] out = null;
        boolean allowMarks = false;
        int marks = 0;
        int i = 0;
        while (i < text.length()) {
            final int cp = text.codePointAt(i);
            final int width = Character.charCount(cp);
            final int type = Character.getType(cp);
            final boolean isMark = type == Character.NON_SPACING_MARK || cp == 0x0674 || (cp >= 0xFC5E && cp <= 0xFC63);
            final boolean drop;
            if (isMark) {
                drop = !allowMarks || ++marks > 2;
            } else {
                drop = cp == 0x2066 || cp == 0x2067;
                marks = 0;
            }
            allowMarks = !drop && (isMark || cp > 32 && !Character.isWhitespace(cp) && type != Character.CONTROL
                    && type != Character.FORMAT && type != Character.PRIVATE_USE && type != Character.SURROGATE && type != Character.UNASSIGNED);
            if (drop) {
                if (out == null) {
                    out = text.toCharArray();
                }
                java.util.Arrays.fill(out, i, i + width, '‌');
            }
            i += width;
        }
        return out == null ? text : new String(out);
    }
}
