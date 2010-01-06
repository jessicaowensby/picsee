package com.jros;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Toast;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

public class viewer extends Activity implements
        AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory {
	private String LOGID = "viewer";
	downloader d = new downloader();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, downloader.class));
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.image_switcher_1);

        mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
        mSwitcher.setFactory(this);
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));

        Gallery g = (Gallery) findViewById(R.id.gallery);
        g.setAdapter(new ImageAdapter(this));
        g.setOnItemSelectedListener(this);
    }
    
    @Override
    public void onPause(){
    	stopService(null);
    }
    
	@Override
	public void onDestroy() {
		stopService(null);
	}

    @SuppressWarnings({ "unchecked", "static-access" })
	public void onItemSelected(AdapterView parent, View v, int position, long id) {
    	mSwitcher.setImageDrawable(d.drawables[position]);
    }

    @SuppressWarnings("unchecked")
	public void onNothingSelected(AdapterView parent) {
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication()).inflate(R.menu.option, menu);

		return(super.onCreateOptionsMenu(menu));
	}

    public View makeView() {
        MyView i = new MyView(this);
        i.setBackgroundColor(0xFF000000);
        i.setScaleType(MyView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        return i;
    }

    private ImageSwitcher mSwitcher;

    public class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }

        @SuppressWarnings("static-access")
		public int getCount() {
            return d.drawables.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("static-access")
		public View getView(int position, View convertView, ViewGroup parent) {
            MyView i = new MyView(mContext);
    		Log.i(LOGID, "position in drawables: " + position);
    		i.setImageDrawable(d.drawables[position]);
    		Log.i(LOGID, "d.drawables[position]" + d.drawables[position]);
            i.setAdjustViewBounds(true);
            i.setLayoutParams(new Gallery.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            i.setBackgroundResource(R.drawable.picture_frame);
            return i;
        }

        private Context mContext;

    }
    

    
	class MyView extends ImageView {
		private static final String LOGID = "MyView";
		String message = "No key pressed yet.";

		MyView(Context context) {
			super(context);
			setFocusable(false);
		}

		public boolean onKeyDown(int keyCode, KeyEvent ev) {
			Log.i(LOGID, "onKeyDown");
			switch(keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				String message = "Key Up!";
				Toast.makeText(this.getContext(), "key up", Toast.LENGTH_SHORT);
				//Toast.makeText(this,"No location available",Toast.LENGTH_SHORT).show();
				Log.i(LOGID, message);                        
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				message = "Key Down!";
				Log.i(LOGID, message);
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				message = "Key Left!";
				Log.i(LOGID, message);
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				message = "Key Right!";
				Log.i(LOGID, message);
				return true;
			default:
				return false;
			}
			invalidate();
			return true;
		}

	}
}
