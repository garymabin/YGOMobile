package cn.garymb.ygomobile.widget.adapter;


import android.database.CursorWindow;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class CursorWindowPagerAdapter<F extends Fragment> extends FragmentPagerAdapter {
    protected final Class<F> fragmentClass;
    protected final String[] projection;
    protected CursorWindow mCursorWindow;
 
    public CursorWindowPagerAdapter(FragmentManager fm, Class<F> fragmentClass, String[] projection, CursorWindow window) {
        super(fm);
        this.fragmentClass = fragmentClass;
        this.projection = projection;
        this.mCursorWindow = window;
    }
 
    @Override
    public F getItem(int position) {
        if (mCursorWindow == null) // shouldn't happen
            return null;
 
        F frag;
        try {
            frag = fragmentClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Bundle args = new Bundle();
        for (int i = 0; i < projection.length; ++i) {
            args.putString(projection[i], mCursorWindow.getString(position, i));
        }
        frag.setArguments(args);
        return frag;
    }
 
    @Override
    public int getCount() {
        if (mCursorWindow == null)
            return 0;
        else
            return mCursorWindow.getNumRows();
    }
    
    public CursorWindow getCursorWindow() {
        return mCursorWindow;
    }
}
