package com.hiwky.opengltry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.MotionEvent;

public class GLRenderer implements Renderer {

	private final float[] mtrxProjection = new float[16];
	private final float[] mtrxView = new float[16];
	private final float[] mtrxProjectionAndView = new float[16];

	public static float vertices[];
	public static short indices[];
	public static float uvs[];
	public FloatBuffer uvBuffer;
	public FloatBuffer vertexBuffer;
	public ShortBuffer drawListBuffer;
	
	public static float   ssu = 1.0f;
	float   ssx = 1.0f;
	float   ssy = 1.0f;
	float   swp = 480.0f;
	float   shp = 320.0f;
	public static int charX=0;
	public static int charFacing=1;

	float mScreenWidth = 1280;
	float mScreenHeight = 768;

	Context mContext;
	long mLastTime;
	int mProgram;

	public Rect image;
	public Sprite sprite;

	public GLRenderer(Context c) {
		sprite=new Sprite();
		mContext = c;
		mLastTime = System.currentTimeMillis() + 100;
	}
	
	public void setupScaling()
    {
        swp = (int) (mContext.getResources().getDisplayMetrics().widthPixels);
        shp = (int) (mContext.getResources().getDisplayMetrics().heightPixels);
         
        ssx = swp / 480.0f;
        ssy = shp / 320.0f;
         
        if(ssx > ssy)
            ssu = ssy;
        else
            ssu = ssx;
    }

	public void onPause() {
		/* Do stuff to pause the renderer */
	}

	public void onResume() {
		/* Do stuff to resume the renderer */
		mLastTime = System.currentTimeMillis();
	}
	
	public void processTouchEvent(MotionEvent event)
    {
        // Get the half of screen value
        int screenhalf = (int) (mScreenWidth / 2);
        int screenbot = (int)(mScreenHeight/2);
        if(event.getY()>screenbot){
        	charFacing=4;
        }else{
        if(event.getX()<screenhalf)
        {
        	charFacing=2;
        	charX-=5;
        	
        }
        else
        {
        	charFacing=1;
            charX+=5;
        }
        }
        createSquare(vertices, 3, charX, 0, 43, 60);
        applyTextures(uvs, 3, charFacing);
    	
        ByteBuffer bb=ByteBuffer.allocateDirect(vertices.length*4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer=bb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);
		//GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
    }

	@Override
	public void onDrawFrame(GL10 unused) {

		long now = System.currentTimeMillis();

		if (mLastTime > now)
			return;

		long elapsed = now - mLastTime;

		render(mtrxProjectionAndView);
		mLastTime = now;

	}

	private void render(float[] m) {

		// clear Screen and Depth Buffer,
		// we have set the clear color as black.
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// get handle to vertex shader's vPosition member
		int mPositionHandle = GLES20.glGetAttribLocation(
				riGraphicTools.sp_Image, "vPosition");

		// Enable generic vertex attribute array
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT,
				false, 0, vertexBuffer);

		// Get handle to texture coordinates location
		int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image,
				"a_texCoord");

		// Enable generic vertex attribute array
		GLES20.glEnableVertexAttribArray(mTexCoordLoc);

		// Prepare the texturecoordinates
		GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false,
				0, uvBuffer);

		// Get handle to shape's transformation matrix
		int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image,
				"uMVPMatrix");

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

		// Get handle to textures locations
		int mSamplerLoc = GLES20.glGetUniformLocation(riGraphicTools.sp_Image,
				"s_texture");

		// Set the sampler texture unit to 0, where we have saved the texture.
		GLES20.glUniform1i(mSamplerLoc, 0);

		// Draw the triangle
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
				GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mTexCoordLoc);

	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		setupScaling();

		// We need to know the current width and height.
		mScreenWidth = width;
		mScreenHeight = height;

		// Redo the Viewport, making it fullscreen.
		GLES20.glViewport(0, 0, (int) mScreenWidth, (int) mScreenHeight);

		// Clear our matrices
		for (int i = 0; i < 16; i++) {
			mtrxProjection[i] = 0.0f;
			mtrxView[i] = 0.0f;
			mtrxProjectionAndView[i] = 0.0f;
		}

		// Setup our screen width and height for normal sprite translation.
		Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight,
				0, 50);

		// Set the camera position (View matrix)
		Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

		// Calculate the projection and view transformation
		Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0,
				mtrxView, 0);

	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		setupScaling();
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		setupTriangle();
		setupImage();

		// Set the clear color to black
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);

		// Create the shaders
		int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,
				riGraphicTools.vs_SolidColor);
		int fragmentShader = riGraphicTools.loadShader(
				GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_SolidColor);

		riGraphicTools.sp_SolidColor = GLES20.glCreateProgram(); // create empty
																	// OpenGL ES
																	// Program
		GLES20.glAttachShader(riGraphicTools.sp_SolidColor, vertexShader); // add
																			// the
																			// vertex
																			// shader
																			// to
																			// program
		GLES20.glAttachShader(riGraphicTools.sp_SolidColor, fragmentShader); // add
																				// the
																				// fragment
																				// shader
																				// to
																				// program
		GLES20.glLinkProgram(riGraphicTools.sp_SolidColor); // creates OpenGL ES
															// program
															// executables

		vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,
				riGraphicTools.vs_Image);
		fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER,
				riGraphicTools.fs_Image);

		riGraphicTools.sp_Image = GLES20.glCreateProgram();
		GLES20.glAttachShader(riGraphicTools.sp_Image, vertexShader);
		GLES20.glAttachShader(riGraphicTools.sp_Image, fragmentShader);
		GLES20.glLinkProgram(riGraphicTools.sp_Image);
		GLES20.glUseProgram(riGraphicTools.sp_Image);
		
	}
	
	public void applyTextures(float[] uvs, int i, float texId){ //1-top left 5-bottom
		if(texId<5){
		uvs[(i*8) + 0] = (texId-1) * 0.25f;
	    uvs[(i*8) + 1] = 0.0f;
	    uvs[(i*8) + 2] = (texId-1) * 0.25f;
	    uvs[(i*8) + 3] = 0.2f;
	    uvs[(i*8) + 4] = texId * 0.25f;
	    uvs[(i*8) + 5] = 0.2f;
	    uvs[(i*8) + 6] = texId * 0.25f;
	    uvs[(i*8) + 7] = 0.0f; 
		}else{
			uvs[(i*8) + 0] = 0.0f;
	        uvs[(i*8) + 1] = 0.2f;
	        uvs[(i*8) + 2] = 0.0f;
	        uvs[(i*8) + 3] = 1f;
	        uvs[(i*8) + 4] = 1f;
	        uvs[(i*8) + 5] = 1f;
	        uvs[(i*8) + 6] = 1f;
	        uvs[(i*8) + 7] = 0.2f; 
		}
	}
	
	public void createSquare(float[] vertices, int i, int left, int bot, int width, int height){
		vertices[(i*12) + 0] = left*ssu;
        vertices[(i*12) + 1] = (bot+height)*ssu;
        vertices[(i*12) + 2] = 0f;
        vertices[(i*12) + 3] = left*ssu;
        vertices[(i*12) + 4] = bot*ssu;
        vertices[(i*12) + 5] = 0f;
        vertices[(i*12) + 6] = (left+width)*ssu;
        vertices[(i*12) + 7] = bot*ssu;
        vertices[(i*12) + 8] = 0f;
        vertices[(i*12) + 9] = (left+width)*ssu;
        vertices[(i*12) + 10] = (bot+height)*ssu;
        vertices[(i*12) + 11] = 0f;
	}
	
	

	public void setupTriangle()
    {
         
        // Our collection of vertices
        vertices = new float[30*4*3];
         

         

        createSquare(vertices, 0, 0, 0, 230, 320);
        createSquare(vertices, 1, 230, 0, 230, 320);
        createSquare(vertices, 2, 460, 0, 230, 320);
        createSquare(vertices, 3, 0, 0, 43, 60);
        
        indices = new short[30*6];
        int last = 0;
        for(int i=0;i<30;i++)
        {
            // We need to set the new indices for the new quad
            indices[(i*6) + 0] = (short) (last + 0);
            indices[(i*6) + 1] = (short) (last + 1);
            indices[(i*6) + 2] = (short) (last + 2);
            indices[(i*6) + 3] = (short) (last + 0);
            indices[(i*6) + 4] = (short) (last + 2);
            indices[(i*6) + 5] = (short) (last + 3);
             
            // Our indices are connected to the vertices so we need to keep them
            // in the correct order.
            // normal quad = 0,1,2,0,2,3 so the next one will be 4,5,6,4,6,7
            last = last + 4;
        }
 
        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
         
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }

	public void setupImage()
    {

         
        // 30 image objects times 4 vertices times (u and v)
        uvs = new float[30*4*2];
        /////////////////////////////// 
         
        applyTextures(uvs, 0, 5);
        applyTextures(uvs, 1, 5); 
        applyTextures(uvs, 2, 5);
        applyTextures(uvs, 3, charFacing);
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);
         
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);
         
        // Retrieve our image from resources.
        int id = mContext.getResources().getIdentifier("drawable/textureatl", null, mContext.getPackageName());
         
        // Temporary create a bitmap
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
         
        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
         
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
         
        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
         
 
    }
	
		
}
