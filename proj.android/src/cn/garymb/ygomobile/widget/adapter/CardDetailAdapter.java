package cn.garymb.ygomobile.widget.adapter;

import cn.garymb.ygomobile.fragment.BaseFragment;

import android.database.CursorWindow;
import android.support.v4.app.FragmentManager;

public class CardDetailAdapter<F extends BaseFragment> extends CursorWindowPagerAdapter<F> {
	
	public CardDetailAdapter(FragmentManager fm,
			Class<F> fragmentClass, String[] projection,
			CursorWindow window) {
		super(fm, fragmentClass, projection, window);
	}
}
