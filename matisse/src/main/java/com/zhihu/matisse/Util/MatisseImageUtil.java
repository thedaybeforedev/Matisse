package com.zhihu.matisse.Util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class MatisseImageUtil {

    private MatisseImageUtil() {

    }


    public static Bitmap processImage(Context context, String filePath, int maxDimension) throws IOException {
        Uri imageUri = getImageUri(context, filePath);

        if (imageUri == null) {
            throw new FileNotFoundException("File not found or inaccessible: " + filePath);
        }

        // ContentResolver로 InputStream 열기
        ContentResolver contentResolver = context.getContentResolver();
        InputStream inputStream = contentResolver.openInputStream(imageUri);

        if (inputStream == null) {
            throw new IOException("Unable to open input stream for URI: " + imageUri);
        }

        // 이미지 디코딩 및 크기 제한
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // 크기를 제한할 샘플링 비율 계산
        options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension);
        options.inJustDecodeBounds = false;

        // InputStream 다시 열기
        inputStream = contentResolver.openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        if (bitmap == null) {
            throw new IOException("Failed to decode bitmap.");
        }

        // EXIF 데이터를 기반으로 회전 처리
        InputStream exifInputStream = contentResolver.openInputStream(imageUri);
        ExifInterface exif = new ExifInterface(exifInputStream);
        int rotation = getRotationFromExif(exif);
        exifInputStream.close();

        // 회전이 필요하다면 이미지 회전
        if (rotation != 0) {
            bitmap = rotateBitmap(bitmap, rotation);
        }

        // 비율을 유지하면서 크기를 조정합니다
        return resizeBitmap(bitmap, maxDimension);
    }

    // 이미지 비율을 유지하면서 최대 크기 제한하기
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap; // 크기가 이미 제한 이하이면 그대로 반환
        }

        float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
        int newWidth = Math.round(ratio * width);
        int newHeight = Math.round(ratio * height);

        // 리사이징 후 새 비트맵 생성
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    // inSampleSize 계산 (이미지 크기 비율을 맞추기 위해)
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 원본 이미지 크기
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // 비율에 맞게 크기를 줄이기 위한 샘플링 비율 계산
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    private static int getRotationFromExif(ExifInterface exif) {
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static Uri getImageUri(Context context, String filePath) {
        // MediaStore에서 파일 URI를 가져옴
        ContentResolver contentResolver = context.getContentResolver();
        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = {filePath};

        try (Cursor cursor = contentResolver.query(collection, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
            }
        }

        return null;
    }

}
