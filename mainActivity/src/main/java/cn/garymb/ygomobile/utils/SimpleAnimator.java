package cn.garymb.ygomobile.utils;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;

public final class SimpleAnimator {
	public static void performSlideNextAnimation(Context context, final View viewFrom, final View viewTo) {
		Animation outAnime = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
		outAnime.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				viewFrom.setVisibility(View.GONE);
			}
		});
		Animation inAnimation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
		inAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				viewTo.setVisibility(View.VISIBLE);
			}
		});
		viewFrom.startAnimation(outAnime);
		viewTo.startAnimation(inAnimation);
	}
	
	public static void performSlideBackAnimation(Context context, final View viewFrom, final View viewTo) {
		Animation outAnime = AnimationUtils.loadAnimation(context, R.animator.slide_out_left);
		outAnime.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				viewFrom.setVisibility(View.GONE);
			}
		});
		Animation inAnimation = AnimationUtils.loadAnimation(context, R.animator.slide_in_right);
		inAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				viewTo.setVisibility(View.VISIBLE);
			}
		});
		viewFrom.startAnimation(outAnime);
		viewTo.startAnimation(inAnimation);
	}
}
