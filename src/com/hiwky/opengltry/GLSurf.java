package com.hiwky.opengltry;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GLSurf extends GLSurfaceView {
	
	private final GLRenderer mRenderer;
	
	public GLSurf(Context context){
		super(context);
		
		setEGLContextClientVersion(2);
		
		mRenderer=new GLRenderer(context);
		setRenderer(mRenderer);
		
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		mRenderer.onPause();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mRenderer.onResume();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
	  mRenderer.processTouchEvent(e);
	  return true;
	}

}
