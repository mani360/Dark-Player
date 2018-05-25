package com.andygopu.androidantariksa.blackplay;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

class BitmapScaler {

	// Max read limit that we allow our input stream to mark/reset.
	private static final int MAX_READ_LIMIT_PER_IMG = 1024 * 1024;
	public static Bitmap fetchAndRescaleBitmap(String uri, int width, int height)  throws IOException {
		URL url = new URL(uri);
		BufferedInputStream is = null;
		try {
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			is = new BufferedInputStream(urlConnection.getInputStream());
			is.mark(MAX_READ_LIMIT_PER_IMG);
			int scaleFactor = findScaleFactor(width, height, is);
			is.reset();
			return scaleBitmap(scaleFactor, is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public static int findScaleFactor(int targetW, int targetH, InputStream is) throws IOException {
		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, bmOptions);
		int actualW = bmOptions.outWidth;
		int actualH = bmOptions.outHeight;

		// Determine how much to scale down the image
		return Math.min(actualW/targetW, actualH/targetH);
	}

	public static Bitmap scaleBitmap(Bitmap src, int maxWidth, int maxHeight) {
		double scaleFactor = Math.min(
				((double) maxWidth)/src.getWidth(), ((double) maxHeight)/src.getHeight());
		return Bitmap.createScaledBitmap(src,
				(int) (src.getWidth() * scaleFactor), (int) (src.getHeight() * scaleFactor), false);
	}

	public static Bitmap scaleBitmap(int scaleFactor, InputStream is) {
		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;

		return BitmapFactory.decodeStream(is, null, bmOptions);
	}

	private static class Size {
		int sample;
		float scale;
	}

	private Bitmap scaled;

	BitmapScaler(Resources resources, int resId, int newWidth)
			throws IOException {
		Size size = getRoughSize(resources, resId, newWidth);
		roughScaleImage(resources, resId, size);
		scaleImage(newWidth);
	}

	BitmapScaler(File file, int newWidth) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			Size size = getRoughSize(is, newWidth);
			try {
				is = new FileInputStream(file);
				roughScaleImage(is, size);
				scaleImage(newWidth);
			} finally {
				is.close();
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	BitmapScaler(AssetManager manager, String assetName, int newWidth)
			throws IOException {
		InputStream is = null;
		try {
			is = manager.open(assetName);
			Size size = getRoughSize(is, newWidth);
			try {
				is = manager.open(assetName);
				roughScaleImage(is, size);
				scaleImage(newWidth);
			} finally {
				is.close();
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	Bitmap getScaled() {
		return scaled;
	}

	private void scaleImage(int newWidth) {
		int width = scaled.getWidth();
		int height = scaled.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float ratio = ((float) scaled.getWidth()) / newWidth;
		int newHeight = (int) (height / ratio);
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		scaled = Bitmap.createBitmap(scaled, 0, 0, width, height, matrix, true);
	}

	private void roughScaleImage(InputStream is, Size size) {
		Matrix matrix = new Matrix();
		matrix.postScale(size.scale, size.scale);

		BitmapFactory.Options scaledOpts = new BitmapFactory.Options();
		scaledOpts.inSampleSize = size.sample;
		scaled = BitmapFactory.decodeStream(is, null, scaledOpts);
	}

	private void roughScaleImage(Resources resources, int resId, Size size) {
		Matrix matrix = new Matrix();
		matrix.postScale(size.scale, size.scale);

		BitmapFactory.Options scaledOpts = new BitmapFactory.Options();
		scaledOpts.inSampleSize = size.sample;
		scaled = BitmapFactory.decodeResource(resources, resId, scaledOpts);
	}

	private Size getRoughSize(InputStream is, int newWidth) {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, o);

		Size size = getRoughSize(o.outWidth, o.outHeight, newWidth);
		return size;
	}

	private Size getRoughSize(Resources resources, int resId, int newWidth) {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resId, o);

		Size size = getRoughSize(o.outWidth, o.outHeight, newWidth);
		return size;
	}

	private Size getRoughSize(int outWidth, int outHeight, int newWidth) {
		Size size = new Size();
		size.scale = outWidth / newWidth;
		size.sample = 1;

		int width = outWidth;
		int height = outHeight;

		int newHeight = (int) (outHeight / size.scale);

		while (true) {
			if (width / 2 < newWidth || height / 2 < newHeight) {
				break;
			}
			width /= 2;
			height /= 2;
			size.sample *= 2;
		}
		return size;
	}
}
