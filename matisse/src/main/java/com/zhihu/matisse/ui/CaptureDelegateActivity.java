package com.zhihu.matisse.ui;

/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static com.zhihu.matisse.ui.MatisseActivity.EXTRA_RESULT_SELECTION;
import static com.zhihu.matisse.ui.MatisseActivity.EXTRA_RESULT_SELECTION_PATH;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.internal.ui.adapter.AlbumMediaAdapter;
import com.zhihu.matisse.internal.utils.MediaStoreCompat;
import com.zhihu.matisse.internal.utils.SingleMediaScanner;

import java.util.ArrayList;

/**
 * Main Activity to display albums and media content (images/videos) in each album
 * and also support media selecting operations.
 */
public class CaptureDelegateActivity extends AppCompatActivity implements AlbumMediaAdapter.OnPhotoCapture {

    private SelectionSpec mSpec;
    private MediaStoreCompat mMediaStoreCompat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (mSpec == null) {
            mSpec = SelectionSpec.getInstance();
        }

        super.onCreate(savedInstanceState);
        if (!mSpec.hasInited) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        if (!mSpec.capture) {
            throw new IllegalStateException("capture must set true!");
        }
        if (mSpec.captureStrategy == null)
            throw new RuntimeException("Don't forget to set CaptureStrategy.");
        mMediaStoreCompat = new MediaStoreCompat(this);
        mMediaStoreCompat.setCaptureStrategy(mSpec.captureStrategy);

        capture();
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.matisse_anim_empty, R.anim.matisse_anim_empty);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.matisse_anim_empty, R.anim.matisse_anim_empty);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            finish();
            return;
        }
        if (requestCode == MatisseActivity.REQUEST_CODE_CAPTURE) {
            // Just pass the data back to previous calling Activity.
            Uri contentUri = mMediaStoreCompat.getCurrentPhotoUri();
            String path = mMediaStoreCompat.getCurrentPhotoPath();
            ArrayList<Uri> selected = new ArrayList<>();
            selected.add(contentUri);
            ArrayList<String> selectedPath = new ArrayList<>();
            selectedPath.add(path);
            Intent result = new Intent();
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selected);
            result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPath);
            setResult(RESULT_OK, result);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CaptureDelegateActivity.this.revokeUriPermission(contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            try {

                new SingleMediaScanner(this.getApplicationContext(), path, new SingleMediaScanner.ScanListener() {
                    @Override
                    public void onScanFinish() {
                        Log.i("SingleMediaScanner", "scan finish!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                finish();
            }
        }
    }

    @Override
    public void capture() {
        if (mMediaStoreCompat != null) {
            mMediaStoreCompat.dispatchCaptureIntent(this, MatisseActivity.REQUEST_CODE_CAPTURE);
        }
    }
}