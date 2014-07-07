package cn.garymb.ygomobile;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.BUILD;
import static org.acra.ReportField.CUSTOM_DATA;
import static org.acra.ReportField.DEVICE_FEATURES;
import static org.acra.ReportField.DISPLAY;
import static org.acra.ReportField.DUMPSYS_MEMINFO;
import static org.acra.ReportField.ENVIRONMENT;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.TOTAL_MEM_SIZE;
import static org.acra.ReportField.USER_CRASH_DATE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.apache.http.client.HttpClient;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.core.CrashSender;
import cn.garymb.ygomobile.net.http.ThreadSafeHttpClientFactory;
import cn.garymb.ygomobile.provider.YGOCardsProvider;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.utils.FileOpsUtils;

import com.github.nativehandler.NativeCrashHandler;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;

@ReportsCrashes(formKey = "", // will not be used
customReportContent = { APP_VERSION_NAME, ANDROID_VERSION, PHONE_MODEL,
		CUSTOM_DATA, STACK_TRACE, USER_CRASH_DATE, LOGCAT, BUILD,
		TOTAL_MEM_SIZE, DISPLAY, DUMPSYS_MEMINFO, DEVICE_FEATURES, ENVIRONMENT }, mailTo = "garymabin@gmail.com", includeDropBoxSystemTags = true, mode = ReportingInteractionMode.DIALOG, resDialogText = R.string.crashed, resDialogIcon = android.R.drawable.ic_dialog_info, // optional.
// default
// is
// a
// warning
// sign
resDialogTitle = R.string.crash_title, // optional. default is your application
// name
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when
// defined, adds
// a user text
// field input
// with this
// text resource
// as a label
resDialogOkToast = R.string.crash_dialog_ok_toast)
public class StaticApplication extends Application {

	public static final int CORE_CONFIG_COPY_COUNT = 3;

	private static final String TAG = "StaticApplication";

	public static Pair<String, String> sRootPair;

	private ThreadSafeHttpClientFactory mHttpFactory;

	private static StaticApplication INSTANCE;

	private SharedPreferences mSettingsPref;

	private String mCoreConfigVersion;

	private String mDataBasePath;

	private String mCoreSkinPath;

	private float mScreenWidth;

	private float mScreenHeight;

	private float mDensity;
	
	private int mVersionCode;
	
	private String mVersionName;
	
	static {
		System.loadLibrary("YGOMobile");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		INSTANCE = this;
		new NativeCrashHandler().registerForNativeCrash(this);
		ACRA.init(this);
		CrashSender sender = new CrashSender(this);
		ACRA.getErrorReporter().setReportSender(sender);
		mHttpFactory = new ThreadSafeHttpClientFactory(this);
		sRootPair = Pair.create(getResources().getString(R.string.root_dir),
				Environment.getExternalStorageDirectory().getPath());
		mCoreSkinPath = getCacheDir() + File.separator
				+ Constants.CORE_SKIN_PATH;
		mSettingsPref = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			mVersionCode = info.versionCode;
			mVersionName = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		Controller.peekInstance();
		checkAndCopyCoreConfig();
		checkAndCopyGameSkin();
		checkAndCopyDatabase();
		checkAndCopyFonts();
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		mScreenWidth = metrics.widthPixels;
		mScreenHeight = metrics.heightPixels;

	}

	private void checkAndCopyFonts() {
		File file = new File(getFontPath());
		if (!file.exists()) {
			try {
				new File(getDefaultResPath() + Constants.FONT_DIRECTORY)
						.mkdirs();
				copyRawData(getFontPath(), R.raw.fonts);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void checkAndCopyDatabase() {
		if (!checkDataBase()) {
			try {
				new File(mDataBasePath).mkdirs();
				copyRawData(mDataBasePath + YGOCardsProvider.DATABASE_NAME,
						R.raw.cards);
			} catch (IOException e) {
				e.printStackTrace();
			}
			doSomeTrickOnDatabase();
		}
	}

	private void doSomeTrickOnDatabase() {
		SQLiteDatabase db = null;
		try {
			String myPath = mDataBasePath + YGOCardsProvider.DATABASE_NAME;
			db = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		try {
			db.beginTransaction();
			db.execSQL("ALTER TABLE datas RENAME TO datas_backup;");
			db.execSQL("CREATE TABLE datas (_id integer PRIMARY KEY, ot integer, alias integer, setcode integer, type integer,"
					+ " atk integer, def integer, level integer, race integer, attribute integer, category integer);");
			db.execSQL("INSERT INTO datas (_id, ot, alias, setcode, type, atk, def, level, race, attribute, category) "
					+ "SELECT id, ot, alias, setcode, type, atk, def, level, race, attribute, category FROM datas_backup;");
			db.execSQL("DROP TABLE datas_backup;");
			db.execSQL("ALTER TABLE texts RENAME TO texts_backup;");
			db.execSQL("CREATE TABLE texts (_id integer PRIMARY KEY, name varchar(128), desc varchar(1024),"
					+ " str1 varchar(256), str2 varchar(256), str3 varchar(256), str4 varchar(256), str5 varchar(256),"
					+ " str6 varchar(256), str7 varchar(256), str8 varchar(256), str9 varchar(256), str10 varchar(256),"
					+ " str11 varchar(256), str12 varchar(256), str13 varchar(256), str14 varchar(256), str15 varchar(256), str16 varchar(256));");
			db.execSQL("INSERT INTO texts (_id, name, desc, str1, str2, str3, str4, str5, str6, str7, str8, str9, str10, str11, str12, str13, str14, str15, str16)"
					+ " SELECT id, name, desc, str1, str2, str3, str4, str5, str6, str7, str8, str9, str10, str11, str12, str13, str14, str15, str16 FROM texts_backup;");
			db.execSQL("DROP TABLE texts_backup;");
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		if (db != null) {
			db.close();
		}
	}

	private void copyRawData(String path, int resId) throws IOException {
		// Open your local db as the input stream
		InputStream myInput = getResources().openRawResource(resId);
		// Path to the just created empty db
		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(path);
		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	@SuppressLint("SdCardPath")
	private boolean checkDataBase() {
		if (android.os.Build.VERSION.SDK_INT >= 17) {
			mDataBasePath = getApplicationInfo().dataDir + "/databases/";
		} else {
			mDataBasePath = "/data/data/" + getPackageName() + "/databases/";
		}
		return new File(mDataBasePath + YGOCardsProvider.DATABASE_NAME)
				.exists();
	}

	private void checkAndCopyGameSkin() {
		File coreSkinDir = new File(mCoreSkinPath);
		if (coreSkinDir != null && coreSkinDir.exists()
				&& coreSkinDir.isDirectory()) {
			return;
		}
		if (coreSkinDir != null && coreSkinDir.exists()
				&& !coreSkinDir.isDirectory()) {
			coreSkinDir.delete();
		}
		// we need to copy from configs from assets;
		int assetcopycount = 0;
		while (assetcopycount++ < CORE_CONFIG_COPY_COUNT) {
			try {
				FileOpsUtils.assetsCopy(this, Constants.CORE_SKIN_PATH,
						coreSkinDir.getAbsolutePath(), false);
				break;
			} catch (IOException e) {
				Log.w(TAG, "copy core skin failed, retry count = "
						+ assetcopycount);
				continue;
			}
		}
	}

	private void checkAndCopyCoreConfig() {
		loadCoreConfigVersion();
		File internalCacheDir = getCacheDir();
		if (internalCacheDir != null) {
			File coreConfigDir = new File(internalCacheDir,
					Constants.CORE_CONFIG_PATH);
			if (coreConfigDir != null && coreConfigDir.exists()
					&& coreConfigDir.isDirectory()) {
				return;
			}
			if (coreConfigDir != null && coreConfigDir.exists()
					&& !coreConfigDir.isDirectory()) {
				coreConfigDir.delete();
			}
			// we need to copy from configs from assets;
			int assetcopycount = 0;
			while (assetcopycount++ < CORE_CONFIG_COPY_COUNT) {
				try {
					FileOpsUtils.assetsCopy(this, Constants.CORE_CONFIG_PATH,
							coreConfigDir.getAbsolutePath(), false);
					break;
				} catch (IOException e) {
					Log.w(TAG, "copy core config failed, retry count = "
							+ assetcopycount);
					continue;
				}
			}
			String[] versions = coreConfigDir.list();
			if (versions != null && !versions[0].equals(mCoreConfigVersion)) {
				mCoreConfigVersion = versions[0];
				saveCoreConfigVersion();
			}
		}
	}

	private void saveCoreConfigVersion() {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE_COMMON,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(Constants.PREF_KEY_DATA_VERSION, mCoreConfigVersion);
		editor.commit();
	}

	private void loadCoreConfigVersion() {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE_COMMON,
				Context.MODE_PRIVATE);
		mCoreConfigVersion = sp.getString(Constants.PREF_KEY_DATA_VERSION, "");
	}

	public byte[] getSignInfo() {
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_SIGNATURES);
			Signature[] signs = pi.signatures;
			Signature sign = signs[0];
			return parseSignature(sign.toByteArray());
		} catch (Exception e) {
		}
		return null;
	}

	private byte[] parseSignature(byte[] signature) {
		try {
			CertificateFactory certFactory = CertificateFactory
					.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory
					.generateCertificate(new ByteArrayInputStream(signature));
			byte[] buffer = cert.getEncoded();
			return Arrays.copyOf(buffer, 16);
		} catch (Exception e) {
		}
		return null;
	}

	public HttpClient getHttpClient() {
		return mHttpFactory.getHttpClient();
	}

	public static StaticApplication peekInstance() {
		return INSTANCE;
	}

	public String getDefaultImageCacheRootPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath()
				+ Constants.WORKING_DIRECTORY + Constants.CARD_IMAGE_DIRECTORY;
	}

	public String getCoreSkinPath() {
		return mCoreSkinPath;
	}

	public String getDefaultFontName() {
		return Constants.DEFAULT_FONT_NAME;
	}

	public String getDefaultResPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath()
				+ Constants.WORKING_DIRECTORY;
	}

	public String getResourcePath() {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE,
				Context.MODE_PRIVATE);
		return sp.getString(Constants.RESOURCE_PATH, getDefaultResPath());
	}

	public void setResourcePath(String path) {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(Constants.RESOURCE_PATH, path);
		editor.commit();
	}

	public String getCardImagePath() {
		return getResourcePath() + Constants.CARD_IMAGE_DIRECTORY;
	}

	public boolean getMobileNetworkPref() {
		return mSettingsPref.getBoolean(
				Settings.KEY_PREF_COMMON_IMAGE_DOWNLOAD_VIA_GPRS, true);
	}

	public int getGameScreenOritation() {
		boolean lockScreen = mSettingsPref.getBoolean(
				Settings.KEY_PREF_GAME_SCREEN_ORIENTATION, true);
		if (lockScreen) {
			return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		} else {
			return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
		}
	}

	public String getDataBasePath() {
		return mDataBasePath;
	}

	public String getCoreConfigVersion() {
		return mCoreConfigVersion;
	}

	public int getOpenglVersion() {
		return Integer.parseInt(mSettingsPref.getString(
				Settings.KEY_PREF_GAME_OGLES_CONFIG,
				Constants.DEFAULT_OGLES_CONFIG));
	}

	public int getCardQuality() {
		return Integer.parseInt(mSettingsPref.getString(
				Settings.KEY_PREF_GAME_IMAGE_QUALITY,
				Constants.DEFAULT_CARD_QUALITY_CONFIG));
	}
	
	public void setLastCheckTime(long time) {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE_COMMON,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putLong(Constants.PREF_KEY_UPDATE_CHECK, time);
		editor.commit();
	}
	
	public long getLastCheckTime() {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE_COMMON,
				Context.MODE_PRIVATE);
		return sp.getLong(Constants.PREF_KEY_UPDATE_CHECK, 0);
	}

	public String getFontPath() {
		return getDefaultResPath()
				+ Constants.FONT_DIRECTORY
				+ mSettingsPref.getString(Settings.KEY_PREF_GAME_FONT_NAME,
						getDefaultFontName());
	}

	public boolean getFontAntialias() {
		return mSettingsPref.getBoolean(Settings.KEY_PREF_GAME_FONT_ANTIALIAS,
				true);
	}

	public String getLastDeck() {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE_COMMON,
				Context.MODE_PRIVATE);
		return sp.getString(Constants.PREF_KEY_LAST_DECK,
				Constants.DEFAULT_DECK_NAME);
	}

	public void setLastDeck(String name) {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE_COMMON,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(Constants.PREF_KEY_LAST_DECK, name);
		editor.commit();
	}

	public float getScreenHeight() {
		return mScreenHeight;
	}

	public float getScreenWidth() {
		return mScreenWidth;
	}

	public float getXScale() {
		return (mScreenHeight > mScreenWidth ? mScreenHeight : mScreenWidth) / 1024.0f;
	}

	public float getYScale() {
		return (mScreenHeight > mScreenWidth ? mScreenWidth :  mScreenHeight) / 640.0f;
	}

	public float getDensity() {
		return mDensity;
	}

	public int getVersionCode() {
		return mVersionCode;
	}
	
	public String getVersionName() {
		return mVersionName;
	}
}
