package com.zhihu.matisse.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

public class MatisseCompressor {
    //max width and height values of the compressed image is taken as 612x816
    private int maxWidth = 1920;
    private int maxHeight = 1920;
    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    private int quality = 80;
    private String destinationDirectoryPath;

    public MatisseCompressor(Context context) {
        destinationDirectoryPath = context.getCacheDir().getPath() + File.separator + "images";
    }

    public MatisseCompressor setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public MatisseCompressor setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public MatisseCompressor setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
        return this;
    }

    public MatisseCompressor setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    public MatisseCompressor setDestinationDirectoryPath(String destinationDirectoryPath) {
        this.destinationDirectoryPath = destinationDirectoryPath;
        return this;
    }

    public Bitmap compressToFile(Context context, File imageFile) throws IOException {
        return MatisseImageUtil.processImage(context, imageFile.getAbsolutePath(), maxWidth);
    }


}