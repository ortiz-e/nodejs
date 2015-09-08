package com.eldimentio.rpgstory;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameGUI extends Activity implements SurfaceHolder.Callback {
	
	public SurfaceView the_gui;
	private final String TAG = "Surface Renderer";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 the_gui = new SurfaceView(this);
		 setContentView(the_gui);
		 the_gui.getHolder().addCallback(this);
	}
	
	 @Override
	    public void surfaceCreated(SurfaceHolder holder) {
	        tryDrawing(holder);
	    }

	    @Override
	    public void surfaceChanged(SurfaceHolder holder, int frmt, int w, int h) { 
	        tryDrawing(holder);
	    }

	    @Override
	    public void surfaceDestroyed(SurfaceHolder holder) {}
	    
	    private void tryDrawing(SurfaceHolder holder) {
	    	 Log.i(TAG, "Trying to draw...");

	         Canvas canvas = holder.lockCanvas();
	         if (canvas == null) {
	             Log.e(TAG, "Cannot draw onto the canvas as it's null");
	         } else {
	             drawMyStuff(canvas);
	             holder.unlockCanvasAndPost(canvas);
	         }
	    }
	    
	    private void drawMyStuff(final Canvas canvas) {
	    	Resources res = getResources();
			Bitmap bitmap = BitmapFactory.decodeResource(res, R.raw.rpg_tileset);
			int tilewidth = (bitmap.getWidth() / 32);
			int tileheight = (bitmap.getHeight() / 63);
			Rect thetile = new Rect(0,15*tileheight,tilewidth,(15*tileheight)+tileheight);
			Rect destination = new Rect(64,64,128,128);
			canvas.drawBitmap(bitmap, thetile, destination, null);
	    }
}
