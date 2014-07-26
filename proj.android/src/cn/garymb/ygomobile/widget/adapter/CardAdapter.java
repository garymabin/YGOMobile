package cn.garymb.ygomobile.widget.adapter;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;


import cn.garymb.ygomobile.R;
import cn.garymb.ygomobie.model.IDataObserver;
import cn.garymb.ygomobie.model.Model;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.core.images.AbstractImageItemController;
import cn.garymb.ygomobile.core.images.BitmapHolder;
import cn.garymb.ygomobile.core.images.ImageFileDownloadTaskHolder;
import cn.garymb.ygomobile.core.images.CardImageItemController;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.ygo.YGOArrayStore;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Message;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CardAdapter extends CursorAdapter implements IDataObserver{
	
	private final class ViewHolder {
		ImageView mCardThumbnail;
		CardImageItemController mController;
		TextView mNameText;
		TextView mLevelText;
		TextView mRaceText;
		TextView mAttrText;
		TextView mAtkText;
		TextView mDefText;
		int id;
		
		public ViewHolder(View view) {
			this.id = view.hashCode();
			mCardThumbnail = (ImageView) view.findViewById(R.id.card_image);
			mNameText = (TextView) view.findViewById(R.id.item_name);
			mLevelText = (TextView) view.findViewById(R.id.item_level);
			mRaceText = (TextView) view.findViewById(R.id.item_race);
			mAttrText = (TextView) view.findViewById(R.id.item_attr);
			mAtkText = (TextView) view.findViewById(R.id.item_atk);
			mDefText = (TextView) view.findViewById(R.id.item_def);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ViewHolder))
				return false;
			
			ViewHolder target = (ViewHolder) o;
			return this.id == target.id;
		}
		
		
		public ImageItem getImageItem() {
			if (mController != null) {
				return mController.getImageItem();
			}
			return null;
		}
	}

	public static final int ID_INDEX = 0;
	public static final int OT_INDEX = 1;
	public static final int TYPE_INDEX = 2;
	public static final int ATK_INDEX = 3;
	public static final int DEF_INDEX = 4;
	public static final int LEVEL_INDEX = 5;
	public static final int RACE_INDEX = 6;
	public static final int ATTR_INDEX = 7;
	public static final int NAME_INDEX = 8;
	
	private boolean initialized;
	
	protected int mIDColumnId;
	protected int mNameColumnId;
	protected int mOTColumnId;
	protected int mTypeColumnId;
	protected int mATKColumnId;
	protected int mDEFColumnId;
	protected int mLevelColumnId;
	protected int mRaceColumnId;
	protected int mAttrColumnId;
	
	private List<String> mColumnNames;
	
	private int thumbnailImageHeightInPixel;
	
	private int thumbnailImageWidthInPixel;
	
	private WeakReference<ListView> mAttachedListView;

	public CardAdapter(Context context, String[] projection, Cursor c, int flags, ListView attachTarget) {
		super(context, c, flags);
		initialized = false;
		mColumnNames = Arrays.asList(projection);
		
		thumbnailImageHeightInPixel = context.getResources().getDimensionPixelSize(R.dimen.card_thumbnail_height);
		thumbnailImageWidthInPixel = context.getResources().getDimensionPixelSize(R.dimen.card_image_width);
		mAttachedListView = new WeakReference<ListView>(attachTarget);
	}
	
	public void onFragmentActive() {
		Controller.peekInstance().registerDataObserver(this);
	}
	
	public void onFragmentInactive() {
		Controller.peekInstance().unregisterDataObserver(this);
	}

	@Override
	public void bindView(View containerView, Context context, Cursor cursor) {
		if (containerView != null && containerView.getTag() != null) {
			ViewHolder holder = (ViewHolder) containerView.getTag();
			String id = cursor.getString(mIDColumnId);
			ImageItem item = new ImageItem(id, thumbnailImageHeightInPixel, thumbnailImageWidthInPixel);
			holder.mController = new CardImageItemController(context, holder.mCardThumbnail);
			
			Bitmap thumbnail = Model.peekInstance().getBitmap(item, Constants.IMAGE_TYPE_THUMNAIL);
			if (thumbnail != null) {
				holder.mController.setBitmap(thumbnail, false);
			} else {
				holder.mController.setImageItem(item);
				requestImage(item, false);
			}
			holder.mNameText.setText(cursor.getString(mNameColumnId));
			if ((cursor.getInt(mTypeColumnId) & YGOArrayStore.TYPE_MONSTER) > 0) {
				int atk = cursor.getInt(mATKColumnId);
				int def = cursor.getInt(mDEFColumnId);
				int level = cursor.getInt(mLevelColumnId);
				holder.mLevelText.setText((level & YGOArrayStore.CARD_LEVEL_MASK) + "");
				holder.mAtkText.setText(atk >= 0 ? atk + "" : "?");
				holder.mDefText.setText(def >= 0 ? def + "" : "?");
				holder.mRaceText.setText(Model.peekInstance().getYGOCardRace(cursor.getInt(mRaceColumnId)));
				holder.mAttrText.setText(Model.peekInstance().getYGOCardAttr(cursor.getInt(mAttrColumnId)));
			} else {
				holder.mLevelText.setText("N/A");
				holder.mAtkText.setText("N/A");
				holder.mDefText.setText("N/A");
				holder.mRaceText.setText("N/A");
				holder.mAttrText.setText("N/A");
			}
		}
		
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		View view = LayoutInflater.from(arg0).inflate(R.layout.card_list_item, null);
		ViewHolder holder = new ViewHolder(view);
		view.setTag(holder);
		return view;
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor != null && !initialized) {
			mIDColumnId = newCursor.getColumnIndexOrThrow(mColumnNames.get(ID_INDEX));
			mNameColumnId = newCursor.getColumnIndexOrThrow(mColumnNames.get(NAME_INDEX));
			mOTColumnId = newCursor.getColumnIndexOrThrow(mColumnNames.get(OT_INDEX));
			mTypeColumnId = newCursor.getColumnIndexOrThrow(mColumnNames.get(TYPE_INDEX));
			mATKColumnId = newCursor.getColumnIndexOrThrow(mColumnNames.get(ATK_INDEX));
			mDEFColumnId = newCursor.getColumnIndexOrThrow(mColumnNames.get(DEF_INDEX));
			mLevelColumnId = newCursor.getColumnIndexOrThrow(mColumnNames.get(LEVEL_INDEX));
			mRaceColumnId = newCursor.getColumnIndexOrThrow(mColumnNames.get(RACE_INDEX));
			mAttrColumnId = newCursor.getColumnIndexOrThrow(mColumnNames.get(ATTR_INDEX));
		}
		return super.swapCursor(newCursor);
	}
	
	private void requestImage(ImageItem item, boolean isPreload) {
		//确定加载的类型
		int type = isPreload ? Constants.BITMAP_LOAD_TYPE_PRELOAD : Constants.BITMAP_LOAD_TYPE_LOAD;

		//已下载就加载
		if (ImageItemInfoHelper.isThumnailExist(item)) {
			Message msg = Controller.buildMessage(Constants.REQUEST_TYPE_LOAD_BITMAP,
					Constants.IMAGE_TYPE_THUMNAIL, type, item);
			Controller.peekInstance().requestDataOperation(this, msg);
		} else {
			//未下载则进行请求下载
			Message msg = Controller.buildMessage(Constants.REQUEST_TYPE_DOWNLOAD_IMAGE,
					Constants.IMAGE_TYPE_THUMNAIL, type, item);
			Controller.peekInstance().requestDataOperation(this, msg);
		}
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

	private void onImageFileDownloaded(int type, int result, ImageItem item) {
		if (result != ImageFileDownloadTaskHolder.RET_DOWNLOAD_SUCCEED) {
			return;
		}
		
		//下载图片完成能够找到持有该item的ImageController才继续走异步加载图片的流程
		if (findImageItemControllerByImageItem(item) != null) {
			Message msg = Controller.buildMessage(Constants.REQUEST_TYPE_LOAD_BITMAP,
					type, Constants.BITMAP_LOAD_TYPE_LOAD, item);
			Controller.peekInstance().requestDataOperation(this, msg);
		}
	}
	
	private AbstractImageItemController findImageItemControllerByImageItem(ImageItem item) {
		if (item == null)
			return null;

		if (mAttachedListView != null && mAttachedListView.get() != null) {
			final ListView lv = mAttachedListView.get();
			final int count = lv.getLastVisiblePosition() - 
					lv.getFirstVisiblePosition() + 1;
			for (int i = 0; i < count; i++) {
				View v = lv.getChildAt(i);
				if (v == null)
					continue;
				Object ob = v.getTag();
				if (ob == null || !(ob instanceof ViewHolder))
					continue;

				ViewHolder holder = (ViewHolder) ob;
				if (item.equals(holder.getImageItem()))
					return holder.mController;
			}
		}
		return null;
	}

	private void onBitmapLoaded(BitmapHolder holder) {
		if (holder == null)
			return;
		
		final ImageItem item = holder.getImageItem();
		if (item == null)
			return;

		AbstractImageItemController ctlr = findImageItemControllerByImageItem(item);
		if (ctlr == null || ctlr.isLoaded(item))
			return;

		ctlr.setBitmap(holder.getBitmap(), true);
	}
}
