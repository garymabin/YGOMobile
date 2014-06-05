package cn.garymb.ygomobile.core.images;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class PreviewImageItemController extends AbstractImageItemController {
	private static final int FADE_IN_TIME = 400;
	protected ImageView mImageView;
	private Context mContext;
	
	public PreviewImageItemController(Context context,ImageView view) {
		mImageView = view;
		this.mContext = context;
	}

	@Override
	protected void onImageItemChanged(int width, int height) {
		if (mImageView != null) {
			mImageView.setScaleType(ScaleType.CENTER_INSIDE);
			mImageView.setImageResource(R.drawable.loading_progress);
		}
	}

	@Override
	public void setBitmap(Bitmap bmp, boolean isAnimationNeeded) {
		if (mImageView != null) {
			if (bmp != null) {
				mImageView.setScaleType(ScaleType.CENTER_CROP);
				if (isAnimationNeeded) {
					showTransitionDrawable(mImageView, bmp);
				} else {
					mImageView.setImageBitmap(bmp);
				}
				mIsLoaded = true;
			}
		}
	}
	private void showTransitionDrawable(ImageView v, Bitmap bitmap) {
		final TransitionDrawable td = new TransitionDrawable(
				new Drawable[] {
						new ColorDrawable(
								android.R.color.transparent),
						new BitmapDrawable(mContext.getResources(),
								bitmap) });
		v.setImageDrawable(td);
		td.startTransition(FADE_IN_TIME);
	}

}
