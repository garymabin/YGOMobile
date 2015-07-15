package cn.garymb.ygomobile.fragment;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.controller.Controller;
import cn.garymb.ygomobile.core.images.BitmapHolder;
import cn.garymb.ygomobile.core.images.ImageFileDownloadTaskHolder;
import cn.garymb.ygomobile.core.images.CardImageItemController;
import cn.garymb.ygomobile.model.IDataObserver;
import cn.garymb.ygomobile.model.Model;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.provider.YGOCards;
import cn.garymb.ygomobile.ygo.YGOArrayStore;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CardDetailPagerFragment extends BaseFragment implements
		LoaderCallbacks<Cursor>, IDataObserver, OnClickListener {

	private static final int CARD_DES_LOADER_ID = 0;

	private String mName;
	private int mType;
	private int mRace;
	private int mAttr;
	private int mOT;
	private int mLevel;
	private String mID;
	private int mAtk;
	private int mDef;

	private TextView mCardDesView;

	private int mImageHeightInPixel;
	private int mImageWidthInPixel;

	private CardImageItemController mImageItemController;
	private ImageItem mImageItem;

	private ViewGroup mImagePanel;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mImageHeightInPixel = activity.getResources().getDimensionPixelSize(
				R.dimen.card_image_height);
		mImageWidthInPixel = activity.getResources().getDimensionPixelSize(
				R.dimen.card_image_width);
		Controller.peekInstance().registerImageObserver(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Controller.peekInstance().unregisterImageObserver(this);
	}

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle param = getArguments();
		mName = param.getString(YGOCards.Texts.NAME);
		mID = param.getString(YGOCards.Datas.ID_ALIAS);
		mType = Integer.parseInt(param.getString(YGOCards.Datas.TYPE));
		mRace = Integer.parseInt(param.getString(YGOCards.Datas.RACE));
		mAttr = Integer.parseInt(param.getString(YGOCards.Datas.ATTRIBUTE));
		mOT = Integer.parseInt(param.getString(YGOCards.Datas.OT));
		mLevel = Integer.parseInt(param.getString(YGOCards.Datas.LEVEL));
		mAtk = Integer.parseInt(param.getString(YGOCards.Datas.ATK));
		mDef = Integer.parseInt(param.getString(YGOCards.Datas.DEF));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = initView(inflater);
		initCursorLoader();
		return view;
	}

	private void initCursorLoader() {
		getLoaderManager().initLoader(CARD_DES_LOADER_ID, null, this);
	}

	/**
	 * 
	 * @return
	 **/
	private View initView(LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.card_detail_pager, null);
		final Model model = Model.peekInstance();
		mImagePanel = (ViewGroup) view.findViewById(R.id.card_image_panel);
		mCardDesView = (TextView) view.findViewById(R.id.card_des);
		((TextView) view.findViewById(R.id.card_wiki))
				.setMovementMethod(new LinkMovementMethod());
		setTextViewHTML(((TextView) view.findViewById(R.id.card_wiki)),
				getResources().getString(R.string.wiki_hint_text));
		((TextView) view.findViewById(R.id.card_ot)).setText(model
				.getYGOCardOT(mOT));
		((TextView) view.findViewById(R.id.card_type)).setText(model
				.getYGOCardType(mType));
		((TextView) view.findViewById(R.id.card_code)).setText("[" + mID + "]");
		if ((mType & YGOArrayStore.TYPE_MONSTER) > 0) {
			((TextView) view.findViewById(R.id.card_race)).setText(model
					.getYGOCardRace(mRace));
			((TextView) view.findViewById(R.id.card_attr)).setText(model
					.getYGOCardAttr(mAttr));
			((TextView) view.findViewById(R.id.card_level))
					.setText(mLevel <= 0 ? "N/A"
							: (mLevel & YGOArrayStore.CARD_LEVEL_MASK) + "");
			((TextView) view.findViewById(R.id.card_atk))
					.setText(mAtk >= 0 ? mAtk + "" : "?");
			((TextView) view.findViewById(R.id.card_def))
					.setText(mDef >= 0 ? mDef + "" : "?");
		} else {
			((TextView) view.findViewById(R.id.card_race)).setText("N/A");
			((TextView) view.findViewById(R.id.card_attr)).setText("N/A");
			((TextView) view.findViewById(R.id.card_level)).setText("N/A");
			((TextView) view.findViewById(R.id.card_atk)).setText("N/A");
			((TextView) view.findViewById(R.id.card_def)).setText("N/A");
		}
		mImageItemController = new CardImageItemController(mActivity,
				(ImageView) view.findViewById(R.id.card_image));
		mImageItem = new ImageItem(mID, mImageHeightInPixel, mImageWidthInPixel);
		Bitmap cardImage = Model.peekInstance().getBitmap(mImageItem,
				Constants.IMAGE_TYPE_ORIGINAL);
		if (cardImage != null) {
			mImagePanel.setClickable(false);
			mImageItemController.setBitmap(cardImage, false);
		} else {
			Bitmap thumbnail = Model.peekInstance().getBitmap(mImageItem,
					Constants.IMAGE_TYPE_THUMNAIL);
			if (thumbnail != null) {
				mImageItemController.setBitmap(thumbnail, false);
			}
			mImageItemController.setImageItem(mImageItem);
			requestImage(mImageItem, false);
		}
		return view;
	}

	protected void setTextViewHTML(TextView text, String html) {
		CharSequence sequence = Html.fromHtml(html);
		SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
		URLSpan[] urls = strBuilder.getSpans(0, sequence.length(),
				URLSpan.class);
		for (URLSpan span : urls) {
			makeLinkClickable(strBuilder, span);
		}
		text.setText(strBuilder);
	}

	private void makeLinkClickable(SpannableStringBuilder strBuilder,
			final URLSpan span) {
		int start = strBuilder.getSpanStart(span);
		int end = strBuilder.getSpanEnd(span);
		int flags = strBuilder.getSpanFlags(span);
		ClickableSpan clickable = new ClickableSpan() {
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(ResourcesConstants.WIKI_SEARCH_URL
						+ mName));
				startActivity(intent);
			}
		};
		strBuilder.setSpan(clickable, start, end, flags);
		strBuilder.removeSpan(span);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(mActivity, YGOCards.Texts.CONTENT_URI,
				new String[] { YGOCards.Texts.DESC },
				YGOCards.Texts._ID + "=?", new String[] { mID }, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		if (arg1.moveToFirst()) {
			mCardDesView.setText(arg1.getString(0));
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}

	@Override
	public void notifyDataUpdate(Message msg) {
		if (msg == null)
			return;

		switch (msg.what) {
		case Constants.REQUEST_TYPE_LOAD_BITMAP:
			if (msg.obj != null && msg.obj instanceof BitmapHolder)
				onBitmapLoaded((BitmapHolder) msg.obj);
			break;
		case Constants.REQUEST_TYPE_DOWNLOAD_IMAGE:
			if (msg.obj != null && msg.obj instanceof ImageItem)
				onImageFileDownloaded(msg.arg1, msg.arg2, (ImageItem) msg.obj);
			break;
		}

	}

	private void requestImage(ImageItem item, boolean isPreload) {
		// 确定加载的类型
		int type = isPreload ? Constants.BITMAP_LOAD_TYPE_PRELOAD
				: Constants.BITMAP_LOAD_TYPE_LOAD;

		// 已下载就加载
		if (ImageItemInfoHelper.isImageExist(item)) {
			Message msg = Controller.buildMessage(
					Constants.REQUEST_TYPE_LOAD_BITMAP,
					Constants.IMAGE_TYPE_ORIGINAL, type, item);
			Controller.peekInstance().requestDataOperation(this, msg);
		} else {
			mImagePanel.setClickable(true);
			mImagePanel.setOnClickListener(this);
		}
	}

	private void onImageFileDownloaded(int type, int result, ImageItem item) {
		if (result != ImageFileDownloadTaskHolder.RET_DOWNLOAD_SUCCEED) {
			return;
		}

		// 下载图片完成能够找到持有该item的ImageController才继续走异步加载图片的流程
		if (mImageItemController.getImageItem().equals(item)) {
			Message msg = Controller.buildMessage(
					Constants.REQUEST_TYPE_LOAD_BITMAP, type,
					Constants.BITMAP_LOAD_TYPE_LOAD, item);
			Controller.peekInstance().requestDataOperation(this, msg);
		}
	}

	private void onBitmapLoaded(BitmapHolder holder) {
		if (holder == null)
			return;

		final ImageItem item = holder.getImageItem();
		if (item == null)
			return;

		mImageItemController.setBitmap(holder.getBitmap(), true);
		mImagePanel.setOnClickListener(null);
		mImagePanel.setClickable(false);
	}

	@Override
	public void onClick(View v) {
		Message msg = Controller.buildMessage(
				Constants.REQUEST_TYPE_DOWNLOAD_IMAGE,
				Constants.IMAGE_TYPE_ORIGINAL, Constants.BITMAP_LOAD_TYPE_LOAD, mImageItem);
		Controller.peekInstance().requestDataOperation(this, msg);
	}

}
