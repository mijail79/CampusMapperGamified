package de.ifgi.sitcom.campusmappergamified.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

//import org.opencv.android.Utils;
//import org.opencv.core.Mat;
//import org.opencv.imgproc.Imgproc;

import de.ifgi.sitcom.campusmappergamified.handler.UIActionHandler;


public class ImageView extends ImageViewBase {

	private Bitmap mImageBitmap;
//	private Mat mImage = null; // the image to be shown
	private Uri mImageUri;
	private int mInSampleSize = 1;

	/*
	 * actions
	 */
	public static final int ACTION_NONE = 1, ACTION_LOAD_IMAGE = 2, ACTION_LOAD_IMAGE_FROM_SERVER = 3;
	private int mAction = ACTION_NONE;

	public ImageView(Context context) {
		super(context);
	}
	
	public ImageView(Context context, UIActionHandler touchHandler) {
		super(context);
		setTouchHandler(touchHandler);
	}

	public ImageView(Context context, Uri imageUri, UIActionHandler touchHandler) {
		super(context);
		setTouchHandler(touchHandler);
		this.mImageUri = imageUri;
	}

//	public Mat getImage() {
//		return this.mImage;
//	}
//
//	public void setImage(Mat im) {
//		this.mImage = im;
//	}
	
	public int getmImSampleSize() {
		return mInSampleSize;
	}

	public void setmImSampleSize(int mImSampleSize) {
		this.mInSampleSize = mImSampleSize;
	}

	public Bitmap getBitmap() {
		return mImageBitmap;
	}


	public void setBitmap(Bitmap bitmap) {
		recycle();
		this.mImageBitmap = bitmap;
		setImageWidth(bitmap.getWidth());
		setImageHeight(bitmap.getHeight());
		zoomToImage();
	}
	
	public void recycle() {

		if (mImageBitmap != null) {
			mImageBitmap.recycle();
			mImageBitmap = null;
		}

//		// Explicitly deallocate Mats
//		if (mImage != null)
//			mImage.release();
//		mImage = null;
		
		System.gc();

	}

	@Override
	protected Bitmap processFrame() {

		
		if (mAction == ACTION_LOAD_IMAGE){
			loadImage();
			mAction = ACTION_NONE;
		} else if (mAction == ACTION_LOAD_IMAGE_FROM_SERVER){
			loadImageFromServer();
			mAction = ACTION_NONE;
		}

//		if (mImage == null){
//			return null;			
//		}


//		if (mImageBitmap == null
//				|| mImage.width() != mImageBitmap.getWidth()
//				|| mImage.height() != mImageBitmap.getHeight()) {
//
//			Log.v("debug", "Bitmap doesnt fit Mat -> create new Bitmap");
//			createNewBitmap();
//
//			return null;
//		}
		Bitmap bmp = mImageBitmap;
//		try {
//			Utils.matToBitmap(mImage, bmp);
//		} catch (Exception e) {
//			Log.e("de.ifgi.sitcom.campusmapper",
//					"Utils.matToBitmap() throws an exception: "
//							+ e.getMessage());
//			bmp.recycle();
//			bmp = null;
//		}
		return bmp;

	}

//	private void createNewBitmap() {
//
//		if (mImageBitmap != null) {
//			mImageBitmap.recycle();
//			mImageBitmap = null;
//		}
//
//		try {
//			mImageBitmap = Bitmap.createBitmap(mImage.width(), mImage.height(),
//					Bitmap.Config.ARGB_8888);
//		} catch (OutOfMemoryError e) {
//			e.printStackTrace();
//		}
//		
//		initImage(mImage.width(), mImage.height());
//	}

	private void loadImageFromServer() {

		recycle();
//		mImage = new Mat();
		
		   try {

		        URL urlImage = new URL(
		        		mImageUri.toString());
		        Log.v("imageURI", mImageUri.toString());
		        HttpURLConnection connection = (HttpURLConnection) urlImage
		                .openConnection();
		        InputStream inputStream = connection.getInputStream();
		        
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = mInSampleSize;
		        mImageBitmap = BitmapFactory.decodeStream(inputStream, null, options);
		        
		    } catch (MalformedURLException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		

//		Utils.bitmapToMat(mImageBitmap, mImage);
//		Imgproc.cvtColor(mImage, mImage, Imgproc.COLOR_RGBA2RGB, 3);
		
		initImage(mImageBitmap.getWidth(), mImageBitmap.getHeight());
	}
	
	
	private void loadImage() {

		recycle();
//		mImage = new Mat();
		
		mImageBitmap = readBitmapScaled(mImageUri);
//		Utils.bitmapToMat(mImageBitmap, mImage);
//		Imgproc.cvtColor(mImage, mImage, Imgproc.COLOR_RGBA2RGB, 3);
		

		initImage(mImageBitmap.getWidth(), mImageBitmap.getHeight());
	}
	
	// Read bitmap scaled when we don´t have enough memory
	public Bitmap readBitmapScaled(Uri selectedImage) {
		
		Uri bitmapUri = Uri.fromFile(new File(selectedImage.toString()));
		Log.v("uri mview", bitmapUri.toString());
		
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = mInSampleSize;
		
		AssetFileDescriptor fileDescriptor = null;
		try {
			fileDescriptor = getContext().getContentResolver()
					.openAssetFileDescriptor(bitmapUri, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				bm = BitmapFactory.decodeFileDescriptor(
						fileDescriptor.getFileDescriptor(), null, options);
				fileDescriptor.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bm;
	}
	

	/*
	 * getters and setters
	 */

	public void setAction(int action) {
		this.mAction = action;
	}

	public void setImageUri(Uri imageUri) {
		this.mImageUri = imageUri;
	}

	public Uri getImageUri() {
		return mImageUri;
	}
	
	

}
