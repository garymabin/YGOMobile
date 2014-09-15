package cn.garymb.ygomobile.widget.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import cn.garymb.ygomobile.widget.filebrowser.SharingItemBase.SharingItemSelectListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @brief 文件浏览组件
 * @author join
 */
public class FileBrowser extends ListView implements
		android.view.View.OnClickListener {
	
	public static final int BROWSE_MODE_DIRS = 0;
	public static final int BROWSE_MODE_FILES = 1;
	public static final int BROWSE_MODE_ALL = 2;

	static final String TAG = "FileBrowser";

	private final String namespace = "http://github.com/joinAero/AndroidWebServ/";

	private String rootDir;

	private List<File> fileList = new ArrayList<File>();

	private File currentDir;

	private int folderResId;

	private int fileResId;
	
	private int mBrowseMode = BROWSE_MODE_DIRS;

	/** 上级目录名称 */
	private String parentDirName = ". .";

	private int display = DISPLAY_ALL;

	public static final int DISPLAY_ALL = -1;
	public static final int DISPLAY_FILE = 0;
	public static final int DISPLAY_FOLDER = 1;

	private boolean isBackResp;

	private boolean isSort;

	private FileListAdapter mListAdapter;
	private OnBrowserListener mBrowserListener;
	private SharingItemSelectListener mItemSelectListener;

	public FileBrowser(Context context) {
		super(context);
		initFileBrowser(context);
	}

	public FileBrowser(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FileBrowser(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initFileBrowser(context);

		folderResId = attrs.getAttributeResourceValue(namespace, "folder_def",
				0);
		fileResId = attrs.getAttributeResourceValue(namespace, "file_def", 0);
		String value = attrs.getAttributeValue(namespace, "display");
		if (value != null) {
			if (value.equals("file")) {
				display = DISPLAY_FILE;
			} else if (value.equals("folder")) {
				display = DISPLAY_FOLDER;
			}
		}
		isBackResp = attrs.getAttributeBooleanValue(namespace, "back_resp",
				false);
		isSort = attrs.getAttributeBooleanValue(namespace, "sort", true);
		setVerticalFadingEdgeEnabled(false);
		setHorizontalFadingEdgeEnabled(false);
	}

	private void initFileBrowser(Context context) {
		mListAdapter = new FileListAdapter(context);
		setAdapter(mListAdapter);
		rootDir = StaticApplication.sRootPair.second;
	}

	public void browse(String dir) {
		File file = new File(dir);
		if (!file.exists() || !file.isDirectory() || !file.canRead()) {
			Log.e(TAG, dir + " access denied!");
			return;
		}
		currentDir = file;
		refresh();
	}
	
	public void setBrowserMode(int mode) {
		mBrowseMode = mode;	
	}

	public void refresh() {
		updateFiles();
		mListAdapter.notifyDataSetChanged();
	}

	private void updateFiles() {
		fileList.clear();

		if (!isRootDir())
			fileList.add(null);

		if (currentDir == null) {
			return;
		}

		File[] files = currentDir.listFiles();

		for (File file : files) {
			if (file.canRead()) {
				if (display == DISPLAY_ALL) {
					fileList.add(file);
				} else if (display == DISPLAY_FILE && file.isFile()) {
					fileList.add(file);
				} else if (display == DISPLAY_FOLDER && file.isDirectory()) {
					fileList.add(file);
				}
			}
		}

		if (isSort) {
			sortFileList(fileList);
		}
	}

	public boolean isRootDir() {
		if (currentDir == null) {
			// when sdcard is not inserted. we assume that null folder is still
			// our root folder.
			return true;
		}
		return currentDir.getPath().equals(rootDir);
	}

	public String getRootDir() {
		return rootDir;
	}

	private void sortFileList(List<File> list) {
		Collections.sort(list, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				if (null == f1) {
					return -1;
				} else if (null == f2) {
					return 1;
				} else if (f1.isDirectory() && !f2.isDirectory()) {
					return -1;
				} else if (!f1.isDirectory() && f2.isDirectory()) {
					return 1;
				} else {
					return f1.toString().compareToIgnoreCase(f2.toString());
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (isBackResp && keyCode == KeyEvent.KEYCODE_BACK && !isRootDir()) {
			toParentDir();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void toParentDir() {
		currentDir = currentDir.getParentFile();
		refresh();
		if (mBrowserListener != null) {
			mBrowserListener.toParentDir(currentDir.getPath());
		}
	}

	public interface OnBrowserListener {

		public void onFileItemClick(String filename);

		public void onDirItemClick(String path);

		public void toParentDir(String path);

	}

	public void setOnBrowserListener(OnBrowserListener listener) {
		this.mBrowserListener = listener;
	}

	public void setItemSelectListener(SharingItemSelectListener listener) {
		this.mItemSelectListener = listener;
	}

	private class FileListAdapter extends BaseAdapter {

		private Context mContext;

		public FileListAdapter(Context context) {
			this.mContext = context;
		}

		@Override
		public int getCount() {
			return fileList.size();
		}

		@Override
		public Object getItem(int position) {
			return fileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.BaseAdapter#isEnabled(int)
		 */
		@Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return createView(position, convertView, parent);
		}

		private View createView(int position, View convertView, ViewGroup parent) {
			View v;
			if (convertView == null) {
				FileTreeItem view = (FileTreeItem) LayoutInflater
						.from(mContext).inflate(
								R.layout.file_browser_list_item, null);
				view.setListener(mItemSelectListener);
				view.setOnClickListener(FileBrowser.this);
				v = view;
			} else {
				v = convertView;
			}
			v.setTag(position);
			bindView(position, v);
			return v;
		}

		private void bindView(int position, View view) {
			ImageView ivFile = (ImageView) view
					.findViewById(R.id.item_list_icon);
			TextView tvFile = (TextView) view.findViewById(R.id.item_list_name);
			TextView propertyText = (TextView) view
					.findViewById(R.id.item_property_text);
			SelectableItem checkbox = (SelectableItem) view
					.findViewById(R.id.sharing_checkbox);
			File current = fileList.get(position);

			if (current == null) {
				checkbox.setChecked(false);
				((FileTreeItem) view).setSelectbleVisibility(false);
				if (folderResId > 0)
					ivFile.setImageResource(folderResId);
				tvFile.setText(parentDirName);
			} else {
				String url = FileOpsUtils.getFilePathFromUrl(current
						.getAbsolutePath());
				if (current.isDirectory()) {
					if (folderResId > 0)
						ivFile.setImageResource(folderResId);
					if (mBrowseMode == BROWSE_MODE_FILES) {
						((FileTreeItem) view).setSelectbleVisibility(false);
					} else {
						((FileTreeItem) view).setSelectbleVisibility(true);
						checkbox.setChecked(mItemSelectListener
								.isFileSelected(url));
						((FileTreeItem) view).toggoleBackground(mItemSelectListener
								.isFileSelected(url));
					}
					tvFile.setText(current.getName());
					((FileTreeItem) view).setUrl(url);
					propertyText.setText(FileOpsUtils.formatTime(current
							.lastModified()));
				} else {
					tvFile.setText(current.getName());
					if (fileResId > 0) {
						ivFile.setImageResource(fileResId);
					}
					((FileTreeItem)view).setUrl(url);
					if (mBrowseMode == BROWSE_MODE_DIRS) {
						((FileTreeItem) view).setSelectbleVisibility(false);
					} else {
						((FileTreeItem)view).setSelectbleVisibility(true);
						checkbox.setSelected(mItemSelectListener
								.isFileSelected(url));
						((FileTreeItem) view).toggoleBackground(mItemSelectListener
								.isFileSelected(url));
					}
					propertyText.setText(FileOpsUtils.formatReadableFileSize(current.length()));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		File file = fileList.get(position);
		if (file == null) {
			toParentDir();
		} else if (file.isDirectory()) {
			if (file.exists() && file.canRead()) {
				currentDir = file;
				refresh(); // 刷新
				if (mBrowserListener != null) {
					mBrowserListener.onDirItemClick(file.getPath());
				}
			} else {
				Toast.makeText(getContext(), R.string.access_denied,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			if (mBrowserListener != null) {
				mBrowserListener.onFileItemClick(file.getPath());
			}
		}
	}

}