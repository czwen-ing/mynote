package com.example.mynote;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;

public class MenuAnimation {
    public static final float ROTATION_TIME = 0.46667f;
    private static final String ROTATION = "rotation";
    private static final float MENU_CLOSED_ANGLE = 90f;
    private static final float MENU_OPENED_ANGLE = 0f;
    private static final int DEFAULT_DURATION = 625;
    private static final float ACTION_BAR_ROTATION_ANGLE = 3f;

    private final View mMenuView;
    private final long mDuration;
    private final ObjectAnimator mOpeningAnimation;
    private final ObjectAnimator mClosingAnimation;
    private final MenuListener mListener;
    private final TimeInterpolator mInterpolator;
    private final View mActionBarView;
    private final long mDelay;

    private boolean isOpening;
    private boolean isClosing;

    private MenuAnimation(MenuBuilder builder) {
        this.mActionBarView = builder.actionBarView;
        this.mListener = builder.MenuListener;
        this.mMenuView = builder.MenuView;
        this.mDuration = builder.duration > 0 ? builder.duration : DEFAULT_DURATION;
        this.mDelay = builder.startDelay;
        this.mInterpolator = builder.interpolator == null ? new BounceInterpolator() : builder.interpolator;
        setUpOpeningView(builder.openingView);
        setUpClosingView(builder.closingView);
        this.mOpeningAnimation = buildOpeningAnimation();
        this.mClosingAnimation = buildClosingAnimation();
        if (builder.isClosedOnStart) {
            mMenuView.setRotation(MENU_CLOSED_ANGLE);
            mMenuView.setVisibility(View.INVISIBLE);
        }
        //TODO handle right-to-left layouts
        //TODO handle landscape orientation
    }

    public void open() {
        if (!isOpening) {
            mOpeningAnimation.start();
        }
    }

    public void close() {
        if (!isClosing) {
            mClosingAnimation.start();
        }

    }

    private void setUpOpeningView(final View openingView) {
        if (mActionBarView != null) {
            mActionBarView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mActionBarView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mActionBarView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    mActionBarView.setPivotX(calculatePivotX(openingView));
                    mActionBarView.setPivotY(calculatePivotY(openingView));
                }
            });
        }
        openingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open();
            }
        });
    }

    private void setUpClosingView(final View closingView) {
        mMenuView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mMenuView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mMenuView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mMenuView.setPivotX(calculatePivotX(closingView));
                mMenuView.setPivotY(calculatePivotY(closingView));
            }
        });

        closingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
    }

    private ObjectAnimator buildOpeningAnimation() {
        ObjectAnimator rotationAnimator = initAnimator(ObjectAnimator.ofFloat(mMenuView, ROTATION, MENU_CLOSED_ANGLE, MENU_OPENED_ANGLE));
        rotationAnimator.setInterpolator(mInterpolator);
        rotationAnimator.setDuration(mDuration);
        rotationAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mMenuView.setVisibility(View.VISIBLE);
                isOpening = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isOpening = false;
                if (mListener != null) {
                    mListener.onMenuOpened();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return rotationAnimator;
    }

    private ObjectAnimator buildClosingAnimation() {
        ObjectAnimator rotationAnimator = initAnimator(ObjectAnimator.ofFloat(mMenuView, ROTATION, MENU_OPENED_ANGLE, MENU_CLOSED_ANGLE));
        rotationAnimator.setDuration((long) (mDuration * ROTATION_TIME));
        rotationAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isClosing = true;
                mMenuView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isClosing = false;
                mMenuView.setVisibility(View.GONE);

                if (mListener != null) {
                    mListener.onMenuClosed();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return rotationAnimator;
    }


    private ObjectAnimator initAnimator(ObjectAnimator animator) {
        animator.setStartDelay(mDelay);
        return animator;
    }

    private float calculatePivotY(View burger) {
        return burger.getTop() + burger.getHeight() / 2;
    }

    private float calculatePivotX(View burger) {
        return burger.getLeft() + burger.getWidth() / 2;
    }

    public static class MenuBuilder {
        private final View MenuView;
        private final View openingView;
        private final View closingView;
        private View actionBarView;
        private MenuListener MenuListener;
        private long duration;
        private long startDelay;
        private TimeInterpolator interpolator;
        private boolean isClosedOnStart;

        public MenuBuilder(View MenuView, View closingView, View openingView) {
            this.MenuView = MenuView;
            this.openingView = openingView;
            this.closingView = closingView;
        }

        public MenuBuilder setActionBarViewForAnimation(View view) {
            this.actionBarView = view;
            return this;
        }

        public MenuBuilder setMenuListener(MenuListener MenuListener) {
            this.MenuListener = MenuListener;
            return this;
        }

        public MenuBuilder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public MenuBuilder setStartDelay(long startDelay) {
            this.startDelay = startDelay;
            return this;
        }

        public MenuBuilder setInterpolator(TimeInterpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        public MenuBuilder setClosedOnStart(boolean isClosedOnStart) {
            this.isClosedOnStart = isClosedOnStart;
            return this;
        }

        public MenuAnimation build() {
            return new MenuAnimation(this);
        }
    }

    public interface MenuListener {
        void onMenuOpened();
        void onMenuClosed();
    }
}
