package com.zhihu.matisse.internal.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    private static File tempImageDirectory(Context context) {
        File privateTempDir = new File(context.getCacheDir(), "matisse");
        if (!privateTempDir.exists()) privateTempDir.mkdirs();
        return privateTempDir;
    }

    private static String generateFileName() {
        return "image_" + System.currentTimeMillis();
    }

    /**
     * To find out the extension of required object in given uri
     * Solution by http://stackoverflow.com/a/36514823/1171484
     */
    private static String getMimeType(Context context, Uri uri) {
        String extension = null;

        //Check uri format to avoid null
        if (uri.getScheme() == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension;
    }

    private static void writeToFile(InputStream inputStream, File file) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];

                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
            } finally {
                outputStream.close();
                inputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static File pickedExistingPicture(Context context, Uri photoUri) {
        try {

            InputStream pictureInputStream = context.getContentResolver().openInputStream(photoUri);

            File directory = tempImageDirectory(context);
            File photoFile = new File(directory, generateFileName() + "." + getMimeType(context, photoUri));
            photoFile.createNewFile();
            writeToFile(pictureInputStream, photoFile);
            return photoFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
