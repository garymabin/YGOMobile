package cn.garymb.ygomobile.widget;

import cn.garymb.ygodata.YGOGameOptions;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class RoomDialogConfigController extends BaseDialogConfigController
		implements TextWatcher, OnItemSelectedListener, OnCheckedChangeListener {

	private YGOGameOptions mOptions;

	private EditText mRoomNameEditText;

	private Spinner mDuelModeSpinner;

	private EditText mRoomPassword;

	private EditText mInitLpEditText;

	private Spinner mCardLimitSpinner;

	private CheckBox mShowPasswodCheckBox;

	private Handler mTextChangedHandler;

	private String mDefaultLp;

	private int mMode;

	@SuppressWarnings("unchecked")
	public RoomDialogConfigController(DialogConfigUIBase parent, View view, Bundle param) {
		super(parent, view);
		mOptions = param.getParcelable(ResourcesConstants.GAME_OPTIONS);
		mMode = param.getInt(ResourcesConstants.MODE_OPTIONS);
		boolean isPrivate = param.getBoolean(ResourcesConstants.PRIVATE_OPTIONS);

		final Context context = parent.getContext();
		final Resources res = context.getResources();

		mDefaultLp = res.getString(R.string.life_point_hint);

		mTextChangedHandler = new Handler();

		mShowPasswodCheckBox = (CheckBox) view
				.findViewById(R.id.room_show_password);
		mRoomPassword = (EditText) mView.findViewById(R.id.room_passwd_editbox);

		mShowPasswodCheckBox.setOnCheckedChangeListener(this);
		mRoomPassword.addTextChangedListener(this);

		// create new room
		if (mOptions == null) {
			mRoomNameEditText = (EditText) mView
					.findViewById(R.id.room_name_editbox);
			mRoomNameEditText.addTextChangedListener(this);
			if (mMode == ResourcesConstants.DIALOG_MODE_CREATE_ROOM) {
				mConfigUI.setTitle(R.string.create_room);
				mView.findViewById(R.id.advanced_options_panel).setVisibility(
						View.VISIBLE);
				mDuelModeSpinner = (Spinner) mView
						.findViewById(R.id.duel_mode_spinner);
				((ArrayAdapter<CharSequence>)mDuelModeSpinner.getAdapter()).setDropDownViewResource(R.layout.room_spinner_dropdown_item);
				mInitLpEditText = (EditText) mView
						.findViewById(R.id.init_lp_edittext);
				mCardLimitSpinner = (Spinner) mView
						.findViewById(R.id.card_limit_spinner);
				((ArrayAdapter<CharSequence>)mCardLimitSpinner.getAdapter()).setDropDownViewResource(R.layout.room_spinner_dropdown_item);
				mView.findViewById(R.id.ls_duel_mode).setVisibility(
						View.VISIBLE);

				((CheckBox) mView.findViewById(R.id.room_advanced_toggle))
						.setOnCheckedChangeListener(this);
				mDuelModeSpinner.setOnItemSelectedListener(this);
				mInitLpEditText.addTextChangedListener(this);
				mCardLimitSpinner.setOnItemSelectedListener(this);
				mConfigUI.setPositiveButton(res.getString(R.string.button_create));
			} else if (mMode == ResourcesConstants.DIALOG_MODE_QUICK_JOIN) {
				mConfigUI.setTitle(R.string.quick_join);
				mView.findViewById(R.id.room_password_panel).setVisibility(
						View.GONE);
				mView.findViewById(R.id.room_advanced_toggle).setVisibility(
						View.GONE);
				mConfigUI.setPositiveButton(res.getString(R.string.button_join));
				mRoomNameEditText.setHint(R.string.quick_join_hint);
			}
			mView.findViewById(R.id.le_room_name).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.duel_info_panel).setVisibility(View.GONE);
		} else {
			mConfigUI.setTitle(R.string.duel_info);
			mView.findViewById(R.id.advanced_options_panel).setVisibility(
					View.GONE);
			mView.findViewById(R.id.duel_info_panel)
					.setVisibility(View.VISIBLE);
			mConfigUI.setPositiveButton(res.getString(R.string.button_join));

			if (!isPrivate) {
				mView.findViewById(R.id.room_password_panel).setVisibility(
						View.GONE);
			}

			// room name info
			TextView roomNameText = (TextView) mView
					.findViewById(R.id.room_name_text);
			roomNameText.setText(mOptions.mRoomName);

			// duel mode info
			TextView duelModeText = (TextView) mView
					.findViewById(R.id.duel_mode_text);
			duelModeText
					.setText(res.getStringArray(R.array.duel_mode)[mOptions.mMode]);

			// card rule info
			TextView cardLimitText = (TextView) mView
					.findViewById(R.id.card_limit_text);
			cardLimitText
					.setText(res.getStringArray(R.array.card_limit)[mOptions.mRule]);

			// initial life points
			TextView initLPText = (TextView) mView
					.findViewById(R.id.init_lp_text);
			initLPText.setText(String.valueOf(mOptions.mStartLP));

			// initial card in hands
			TextView initHandsText = (TextView) mView
					.findViewById(R.id.init_hands_text);
			initHandsText.setText(String.valueOf(mOptions.mStartHand));

			// cards draw each turn
			TextView drawCardsText = (TextView) mView
					.findViewById(R.id.draw_count_text);
			drawCardsText.setText(String.valueOf(mOptions.mDrawCount));
		}
		mConfigUI.setCancelButton(res.getString(R.string.button_cancel));
		enableSubmitIfAppropriate();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.room_show_password) {
			int pos = mRoomPassword.getSelectionEnd();
			mRoomPassword
					.setInputType(InputType.TYPE_CLASS_TEXT
							| (isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
									: InputType.TYPE_TEXT_VARIATION_PASSWORD));
			if (pos > 0) {
				mRoomPassword.setSelection(pos);
			}
		} else if (buttonView.getId() == R.id.room_advanced_toggle) {
			if (isChecked) {
				mView.findViewById(R.id.room_advanced_fields).setVisibility(
						View.VISIBLE);
			} else {
				mView.findViewById(R.id.room_advanced_fields).setVisibility(
						View.GONE);
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		enableSubmitIfAppropriate();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		//
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(final Editable s) {
		mTextChangedHandler.post(new Runnable() {
			@Override
			public void run() {
				int limit = enableSubmitIfAppropriate();
				if (s.length() != 0) {
					Button positive = mConfigUI.getPositiveButton();
					boolean enabled = positive.isEnabled();
					if (!enabled) {
						if (mRoomNameEditText != null
								&& mRoomNameEditText.getVisibility() == View.VISIBLE) {
							mRoomNameEditText.setError(mConfigUI
									.getContext()
									.getResources()
									.getString(R.string.input_invalid_msg,
											limit));
						}
						if (mRoomPassword != null
								&& mRoomPassword.getVisibility() == View.VISIBLE) {
							mRoomPassword.setError(mConfigUI
									.getContext()
									.getResources()
									.getString(R.string.input_invalid_msg,
											limit));
						}
					} else {
						if (mRoomNameEditText != null
								&& mRoomNameEditText.getVisibility() == View.VISIBLE) {
							mRoomNameEditText.setError(null);
						}
						if (mRoomPassword != null
								&& mRoomPassword.getVisibility() == View.VISIBLE) {
							mRoomPassword.setError(null);
						}
					}
				}
			}
		});
	}

	public int enableSubmitIfAppropriate() {
		Button positive = mConfigUI.getPositiveButton();
		if (positive == null)
			return 0;
		boolean enabled = true;
		int maxRoomNamePasswordLength = 0;
		if (mOptions != null) {
			if (mOptions.isCompleteOptions()) {
				maxRoomNamePasswordLength = 4;
			} else {
				maxRoomNamePasswordLength = 16;
			}
			if (mOptions.mRoomName.length()
					+ mRoomPassword.getText().toString().length() > maxRoomNamePasswordLength) {
				enabled = false;
			}
		} else {

			if (mMode == ResourcesConstants.DIALOG_MODE_CREATE_ROOM) {
				String initlp = mInitLpEditText.getText().toString().trim();
				if (!TextUtils.isEmpty(initlp)
						&& !mDefaultLp.equals(mInitLpEditText.getText()
								.toString())
						|| mCardLimitSpinner.getSelectedItemPosition() != 0) {
					maxRoomNamePasswordLength = 4;
				} else {
					maxRoomNamePasswordLength = 16;
				}
			} else {
				maxRoomNamePasswordLength = 16;
			}
			String roomName = mRoomNameEditText.getText().toString();

			if (TextUtils.isEmpty(roomName.trim())
					|| roomName.length()
							+ mRoomPassword.getText().toString().length() > maxRoomNamePasswordLength) {
				enabled = false;
			}
		}
		positive.setEnabled(enabled);
		return maxRoomNamePasswordLength;
	}

	public YGOGameOptions getGameOption() {
		YGOGameOptions options = null;
		if (mOptions == null) {
			options = new YGOGameOptions();
			options.mRoomName = mRoomNameEditText.getText().toString().trim();
			if (mMode == ResourcesConstants.DIALOG_MODE_CREATE_ROOM) {
				options.mRoomPasswd = mRoomPassword.getText().toString();
				options.mMode = mDuelModeSpinner.getSelectedItemPosition();
				String initLP = mInitLpEditText.getText().toString().trim();
				if ((!TextUtils.isEmpty(initLP) && !mDefaultLp.equals(initLP))
						|| mCardLimitSpinner.getSelectedItemPosition() != 0) {
					options.setCompleteOptions(true);
					if (!TextUtils.isEmpty(initLP)) {
						options.mStartLP = Integer.parseInt(mInitLpEditText
								.getText().toString().trim());
					}
					options.mRule = mCardLimitSpinner.getSelectedItemPosition();
				} else {
					options.setCompleteOptions(false);
				}
			}
		} else {
			options = mOptions;
			options.mRoomPasswd = mRoomPassword.getText().toString();
		}
		return options;
	}
}
