package org.telegram.ui.Components.inset;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jspecify.annotations.Nullable;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.chat.ViewPositionWatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.vkryl.core.BitwiseUtils;
import me.vkryl.core.reference.ReferenceList;

public class WindowAnimatedInsetsProvider extends WindowInsetsAnimationCompat.Callback {
    private final ViewGroup root;

    public WindowAnimatedInsetsProvider(ViewGroup root) {
        super(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP);
        this.root = root;

        ViewCompat.setWindowInsetsAnimationCallback(root, this);
    }

    @NonNull
    @Override
    public WindowInsetsCompat onProgress(
            @NonNull WindowInsetsCompat insets,
            @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
        int typeMask = 0;
        for (WindowInsetsAnimationCompat animation : runningAnimations) {
            typeMask |= animation.getTypeMask();
            if (BitwiseUtils.hasFlag(animation.getTypeMask(), WindowInsetsCompat.Type.ime())) {
                inu_trackAnimation(animation);
            }
        }
        inu_pruneStaleAnimations(runningAnimations);

        if (BitwiseUtils.hasFlag(typeMask, WindowInsetsCompat.Type.ime())) {
            dispatchWindowInsetsAnimationChange(insets);
        }
        return insets;
    }

    // The platform may skip onStart/onEnd for a cancelled insets animation (e.g. an aborted
    // predictive-back IME dismissal), so active state can't be derived from pairing them alone.
    // onProgress carries the authoritative running list every frame — it is used to both pick up
    // animations whose onStart was missed and evict ones whose onEnd was missed.
    private final ArrayList<WindowInsetsAnimationCompat> inu_activeImeAnimations = new ArrayList<>();
    private final HashMap<WindowInsetsAnimationCompat, Integer> inu_absentCounts = new HashMap<>();

    // A just-started animation can be absent from a concurrent animation's onProgress dispatch
    // until its own first frame, so eviction requires missing several consecutive dispatches.
    private static final int INU_ABSENT_DISPATCHES_TO_EVICT = 3;

    public boolean hasActiveAnimations() {
        return !inu_activeImeAnimations.isEmpty();
    }

    private void inu_trackAnimation(WindowInsetsAnimationCompat animation) {
        if (inu_activeImeAnimations.contains(animation)) {
            inu_absentCounts.remove(animation);
            return;
        }
        final boolean wasEmpty = inu_activeImeAnimations.isEmpty();
        inu_activeImeAnimations.add(animation);
        if (wasEmpty) {
            dispatchWindowInsetsAnimationStart();
        }
    }

    private void inu_untrackAnimation(WindowInsetsAnimationCompat animation) {
        if (inu_activeImeAnimations.remove(animation)) {
            inu_absentCounts.remove(animation);
            if (inu_activeImeAnimations.isEmpty()) {
                dispatchWindowInsetsAnimationFinish();
            }
        }
    }

    private void inu_pruneStaleAnimations(List<WindowInsetsAnimationCompat> runningAnimations) {
        if (inu_activeImeAnimations.isEmpty()) {
            return;
        }
        boolean removed = false;
        for (int i = inu_activeImeAnimations.size() - 1; i >= 0; i--) {
            final WindowInsetsAnimationCompat tracked = inu_activeImeAnimations.get(i);
            if (runningAnimations.contains(tracked)) {
                inu_absentCounts.remove(tracked);
                continue;
            }
            final Integer prev = inu_absentCounts.get(tracked);
            final int absent = (prev == null ? 0 : prev) + 1;
            if (absent >= INU_ABSENT_DISPATCHES_TO_EVICT) {
                inu_activeImeAnimations.remove(i);
                inu_absentCounts.remove(tracked);
                removed = true;
            } else {
                inu_absentCounts.put(tracked, absent);
            }
        }
        if (removed && inu_activeImeAnimations.isEmpty()) {
            dispatchWindowInsetsAnimationFinish();
        }
    }

    @Override
    public WindowInsetsAnimationCompat.@NonNull BoundsCompat onStart(@NonNull WindowInsetsAnimationCompat animation, WindowInsetsAnimationCompat.@NonNull BoundsCompat bounds) {
        if (BitwiseUtils.hasFlag(animation.getTypeMask(), WindowInsetsCompat.Type.ime())) {
            inu_trackAnimation(animation);
        }
        return super.onStart(animation, bounds);
    }

    @Override
    public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
        super.onEnd(animation);
        inu_untrackAnimation(animation);
    }

    private final ReferenceList<Listener> listeners =  new ReferenceList<>();

    public void subscribeToWindowInsetsAnimation(Listener listener) {
        listeners.add(listener);
    }

    public void unsubscribeFromWindowInsetsAnimation(Listener listener) {
        listeners.remove(listener);
    }

    private static final PointF tmpPointF = new PointF();
    private static final RectF tmpRectF = new RectF();
    private static final Rect tmpRect = new Rect();


    private void dispatchWindowInsetsAnimationStart() {
        for (Listener listener: listeners) {
            listener.onAnimatedInsetsStarted();
        }
    }

    private void dispatchWindowInsetsAnimationFinish() {
        for (Listener listener: listeners) {
            listener.onAnimatedInsetsFinished();
        }
    }

    private void dispatchWindowInsetsAnimationChange(WindowInsetsCompat insets) {
        for (Listener listener: listeners) {
            final View v = listener.getAnimatedInsetsTargetView();
            final WindowInsetsCompat i = calculateWindowInsets(insets, v, root);
            if (i != null) {
                listener.onAnimatedInsetsChanged(v, i);
            }
        }
    }

    @Nullable
    public static WindowInsetsCompat calculateWindowInsets(View view) {
        final WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(view);
        final View rootView = view.getRootView();

        return calculateWindowInsets(rootInsets, view, rootView);
    }

    @Nullable
    public static WindowInsetsCompat calculateWindowInsets(WindowInsetsCompat rootInsets, View view, View rootView) {
        if (view == null || rootView == null || rootInsets == null || !ViewPositionWatcher.computeRectInParent(view, rootView, tmpRectF)) {
            return null;
        }
        tmpRectF.round(tmpRect);

        final int left = tmpRect.left;
        final int top = tmpRect.top;
        final int right = rootView.getWidth() - tmpRect.right;
        final int bottom = rootView.getHeight() - tmpRect.bottom;

        if (left == 0 && top == 0 && right == 0 && bottom == 0) {
            return rootInsets;
        }

        return rootInsets.inset(
            Math.max(0, left),
            Math.max(0, top),
            Math.max(0, right),
            Math.max(0, bottom)
        );
    }

    public interface Listener {
        View getAnimatedInsetsTargetView();

        default void onAnimatedInsetsStarted() {}

        void onAnimatedInsetsChanged(View view, WindowInsetsCompat insets);

        default void onAnimatedInsetsFinished() {}
    }
}
