package com.shofyra.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * AnimationUtils — reusable animations for Shofyra UI.
 * Covers: Event Handling and Animations
 */
public class AnimationUtils {

    private AnimationUtils() {} // static utility class

    // ── Fade ──────────────────────────────────────

    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void fadeOut(View view, long duration, Runnable onEnd) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    if (onEnd != null) onEnd.run();
                })
                .start();
    }

    // ── Slide ─────────────────────────────────────

    public static void slideInFromBottom(View view, long duration) {
        view.setTranslationY(200f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();
    }

    public static void slideInFromRight(View view, long duration) {
        view.setTranslationX(300f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();
    }

    public static void slideOutToLeft(View view, long duration, Runnable onEnd) {
        view.animate()
                .translationX(-view.getWidth())
                .alpha(0f)
                .setDuration(duration)
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    view.setTranslationX(0f);
                    if (onEnd != null) onEnd.run();
                })
                .start();
    }

    // ── Scale / Pop ───────────────────────────────

    public static void popIn(View view) {
        view.setScaleX(0.5f);
        view.setScaleY(0.5f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(350)
                .setInterpolator(new OvershootInterpolator(1.3f))
                .start();
    }

    public static void bounce(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", 0f, -24f, 0f);
        anim.setDuration(500);
        anim.setInterpolator(new BounceInterpolator());
        anim.start();
    }

    /** Scale pulse — for "add to cart" feedback */
    public static void pulse(View view) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new OvershootInterpolator());
        scaleY.setInterpolator(new OvershootInterpolator());
        set.playTogether(scaleX, scaleY);
        set.start();
    }

    // ── Shake (error feedback) ────────────────────

    public static void shake(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationX",
                0f, -20f, 20f, -16f, 16f, -8f, 8f, 0f);
        anim.setDuration(500);
        anim.start();
    }

    // ── Stagger (RecyclerView items) ─────────────

    public static void staggerIn(View view, int position) {
        long delay = position * 80L;
        view.setAlpha(0f);
        view.setTranslationY(60f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();
    }

    // ── Cart badge count animation ────────────────

    public static void animateBadgeCount(View badge) {
        badge.animate()
                .scaleX(1.4f).scaleY(1.4f).setDuration(150)
                .withEndAction(() ->
                        badge.animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                .start();
    }

    // ── Number counter (price totals) ────────────

    public interface CounterListener { void onTick(int value); }

    public static void animateCounter(int from, int to, long duration, CounterListener listener) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> listener.onTick((int) a.getAnimatedValue()));
        animator.start();
    }

    // ── Button press ripple helper ────────────────

    public static void addPressEffect(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }
}