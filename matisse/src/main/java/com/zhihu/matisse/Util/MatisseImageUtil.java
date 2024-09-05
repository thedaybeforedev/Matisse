package com.zhihu.matisse.Util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

class MatisseImageUtil {

    private MatisseImageUtil() {

    }

    static File compressImage(File imageFile, int reqWidth, int reqHeight, Bitmap.CompressFormat compressFormat, int quality, String destinationPath) throws IOException {
        FileOutputStream fileOutputStream = null;
        File file = new File(destinationPath).getParentFile();
        if (!file.exists()) {
            file.mkdirs();
        }

        String createDate = null;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            createDate = exif.getAttribute(ExifInterface.TAG_DATETIME);

            fileOutputStream = new FileOutputStream(destinationPath);
            // write the compressed bitmap at the destination specified by destinationPath.

            Bitmap bitmap = decodeAndScaleBitmapWithAspectRatio(imageFile, reqWidth, reqHeight);

            if (bitmap != null)
                bitmap.compress(compressFormat, quality, fileOutputStream);


        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }


        if (!TextUtils.isEmpty(createDate)) {
            ExifInterface newExif = new ExifInterface(destinationPath);

            newExif.setAttribute(ExifInterface.TAG_DATETIME, createDate);

            newExif.saveAttributes();
        }

        return new File(destinationPath);
    }

    static File compressImage(Context context, Uri contentUri, int reqWidth, int reqHeight, Bitmap.CompressFormat compressFormat, int quality, String destinationPath) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        FileOutputStream fileOutputStream = null;
        File file = new File(destinationPath).getParentFile();
        if (!file.exists()) {
            file.mkdirs();
        }

        String createDate = null;
        try {
            ParcelFileDescriptor fileDescriptor = contentResolver.openFileDescriptor(contentUri, "r");

            ExifInterface exif = new ExifInterface(fileDescriptor.getFileDescriptor());
            createDate = exif.getAttribute(ExifInterface.TAG_DATETIME);
            fileDescriptor = contentResolver.openFileDescriptor(contentUri, "r");
            fileOutputStream = new FileOutputStream(destinationPath);
            // write the compressed bitmap at the destination specified by destinationPath.

            Bitmap bitmap = decodeSampledBitmapFromFileDescriptor(fileDescriptor.getFileDescriptor(),exif, reqWidth, reqHeight);

            if (bitmap != null)
                bitmap.compress(compressFormat, quality, fileOutputStream);


        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }



        try {
            if (!TextUtils.isEmpty(createDate)) {
                ExifInterface newExif = new ExifInterface(destinationPath);

                newExif.setAttribute(ExifInterface.TAG_DATETIME, createDate);

                newExif.saveAttributes();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return new File(destinationPath);
    }

    static File compressImage(Bitmap bitmap, Bitmap.CompressFormat compressFormat, int quality, String destinationPath) throws IOException {
        FileOutputStream fileOutputStream = null;
        File file = new File(destinationPath).getParentFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            fileOutputStream = new FileOutputStream(destinationPath);
            // write the compressed bitmap at the destination specified by destinationPath.
            bitmap.compress(compressFormat, quality, fileOutputStream);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }

        return new File(destinationPath);
    }

    static Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fileDescriptor, ExifInterface exif, int reqWidth, int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        Bitmap scaledBitmap = null;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        if (reqWidth < options.outWidth || reqHeight < options.outHeight) {
            int width = options.outWidth;
            int height = options.outHeight;
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) reqWidth / (float) reqWidth;

            int finalWidth = reqWidth;
            int finalHeight = reqHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float) reqHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) reqWidth / ratioBitmap);
            }

            options.outWidth = finalWidth;
            options.outHeight = finalHeight;
            scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor, new Rect(), options), finalWidth, finalHeight, true);

        } else {
            scaledBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor,new Rect(), options);
        }

        if (scaledBitmap == null)
            return null;


        //check the rotation of the image and display it properly
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
        Matrix matrix = new Matrix();
        if (orientation == 6) {
            matrix.postRotate(90);
        } else if (orientation == 3) {
            matrix.postRotate(180);
        } else if (orientation == 8) {
            matrix.postRotate(270);
        }
        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return scaledBitmap;
    }



    public static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Calculate inSampleSize based on required width and height
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap decodedBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Read EXIF data to check the orientation
        ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        // Rotate the bitmap according to the EXIF orientation if needed
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                break;
        }

        // Apply the rotation (or no rotation)
        Bitmap rotatedBitmap = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.getWidth(), decodedBitmap.getHeight(), matrix, true);

        // Scale the bitmap to the required width and height
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, reqWidth, reqHeight, true);

        // Clean up the original bitmaps if they are not needed
        if (rotatedBitmap != decodedBitmap) {
            decodedBitmap.recycle();
        }
        if (scaledBitmap != rotatedBitmap) {
            rotatedBitmap.recycle();
        }

        return scaledBitmap;
    }


    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeAndScaleBitmapWithAspectRatio(File imageFile, int reqWidth, int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Calculate the optimal width and height maintaining the aspect ratio
        int[] scaledDimensions = calculateAspectRatio(imageFile, options.outWidth, options.outHeight, reqWidth, reqHeight);
        int scaledWidth = scaledDimensions[0];
        int scaledHeight = scaledDimensions[1];

        // Decode bitmap with calculated inSampleSize set
        options.inSampleSize = calculateInSampleSize(options, scaledWidth, scaledHeight);
        options.inJustDecodeBounds = false;
        Bitmap decodedBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Read EXIF data to check the orientation
        ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        // Rotate the bitmap according to the EXIF orientation if needed
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                break;
        }

        // Apply the rotation (or no rotation) and scale the bitmap to the required width and height
        Bitmap rotatedBitmap = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.getWidth(), decodedBitmap.getHeight(), matrix, true);

        // Scale the bitmap while maintaining aspect ratio
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, scaledWidth, scaledHeight, true);

        // Clean up the original bitmaps if they are not needed
        if (rotatedBitmap != decodedBitmap) {
            decodedBitmap.recycle();
        }
        if (scaledBitmap != rotatedBitmap) {
            rotatedBitmap.recycle();
        }

        return scaledBitmap;
    }

    private static int[] calculateAspectRatio(File imageFile, int originalWidth, int originalHeight, int reqWidth, int reqHeight) throws IOException {
        ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        // 회전된 이미지를 원래 방향으로 돌리기 위해 너비와 높이를 교환
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            int temp = originalWidth;
            originalWidth = originalHeight;
            originalHeight = temp;
        }

        int finalWidth, finalHeight;

        if (originalWidth > originalHeight) {
            float ratio = (float) reqWidth / originalWidth;
            finalWidth = reqWidth;
            finalHeight = (int) (originalHeight * ratio);
        } else {
            float ratio = (float) reqHeight / originalHeight;
            finalHeight = reqHeight;
            finalWidth = (int) (originalWidth * ratio);
        }

        return new int[]{finalWidth, finalHeight};
    }
}
