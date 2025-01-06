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
package com.zhihu.matisse.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.data.AspectRatios;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.ui.MatisseImageCropActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE = 23;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private View clickView;
    private UriAdapter mAdapter;

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        // 결과가 성공적으로 반환되었을 때 처리할 로직
                        Intent data = result.getData();
                        if (data != null) {
                            // 예: 데이터를 처리하는 로직
                            String[] croppedImageUri = data.getStringArrayExtra(MatisseImageCropActivity.PARAM_IMAGEPATH_ARRAY);
                            //Toast.makeText(SampleActivity.this, String.valueOf(croppedImageUri.length), Toast.LENGTH_SHORT).show();
                            assert croppedImageUri != null;
                            Uri[] uris = new Uri[croppedImageUri.length];
                            for (int i = 0; i < uris.length; i++) {
                                if(i == 0){
                                    uris[0] = Uri.parse(croppedImageUri[0]);
                                }else if(i == 1){
                                    uris[1] = Uri.parse(croppedImageUri[1]);
                                }else if(i == 2){
                                    uris[2] = Uri.parse(croppedImageUri[2]);
                                }
                            }
                            mAdapter.setData(Arrays.asList(uris), Arrays.asList(croppedImageUri));
                        }

                    } else {
                        // 다른 경우 처리
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.zhihu).setOnClickListener(this);
        findViewById(R.id.dracula).setOnClickListener(this);
        findViewById(R.id.only_gif).setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new UriAdapter());
    }

    @SuppressLint("CheckResult")
    @Override
    public void onClick(final View v) {
        clickView = v;
        checkStoragePermission();
    }

    private void startAction(View v) {
        switch (v.getId()) {
            case R.id.zhihu:
                Matisse.from(SampleActivity.this)
                        .choose(MimeType.ofImage(), false)
                        .countable(true)
                        .capture(true)
                        .captureStrategy(
                                new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))

                        .maxSelectable(10)
                        .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .gridExpectedSize(
                                getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.85f)
                        .imageEngine(new GlideEngine())

                        .showSingleMediaType(true)
                        .originalEnable(true)
                        .maxOriginalSize(1)
                        .autoHideToolbarOnSingleTap(true)
                        .setOnCheckedListener(isChecked -> {
                            Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                        })
                        .setUseCrop(true)
                        .setCropRatio(AspectRatios.INSTANCE.getRatioFree())
                        .forResult(REQUEST_CODE_CHOOSE);
                break;
            case R.id.dracula:

                String[] fileListArray = new String[1];

                for (int i = 0; i < fileListArray.length; i++) {
                    if(i == 0){
                        fileListArray[0] = "/data/user/0/com.zhihu.matisse.sample/cache/images/20241202155943_0.jpg";
                    }
                    else if(i == 1){
                        fileListArray[1] = "/data/user/0/com.zhihu.matisse.sample/cache/images/20240906164522_1.jpg";
                    }else if(i == 2){
                        fileListArray[2] = "/data/user/0/com.zhihu.matisse.sample/cache/images/20240906164522_2.jpg";
                    }
                }
                Uri[] imageListArray = new Uri[fileListArray.length];
                for (int i = 0; i < fileListArray.length; i++) {
                    if(i == 0){
                        imageListArray[0] = getImageUri(this, fileListArray[0]);
                    }
                    else if(i == 1){
                        imageListArray[1] = getImageUri(this, fileListArray[1]);
                    }else if(i == 2){
                        imageListArray[2] = getImageUri(this, fileListArray[2]);
                    }
                }

                //크롭만 사용할때 예시
                Matisse.from(SampleActivity.this)
                        .choose(MimeType.ofImage())
                        .forCropResult(fileListArray, imageListArray, activityResultLauncher);
                break;
            case R.id.only_gif:
                String[] fileListArrays = new String[1];

                for (int i = 0; i < fileListArrays.length; i++) {
                    if(i == 0){
                        fileListArrays[0] = "https://firebasestorage.googleapis.com/v0/b/thedaybefore-ops/o/backgrounds%2Fgradient%2Fbg_pattern_005.webp?alt=media&token=26e964c0-e538-4938-a4ea-2177e986fc96";
                    }
                }
                //크롭만 사용할때 예시
                Matisse.from(SampleActivity.this)
                        .choose(MimeType.ofImage())
                        .setTypeUri(true)
                        .forCropResult(fileListArrays, activityResultLauncher);
                break;
            default:
                break;
        }
        mAdapter.setData(null, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mAdapter.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data));
        }
    }

    public Uri getImageUri(Context context, String filePath) {
        File file = new File(filePath);
        String authority = context.getPackageName() + ".fileprovider"; // packageName 기반으로 authority 생성

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android 7.0 이상에서는 FileProvider 사용
            return FileProvider.getUriForFile(context, authority, file);
        } else {
            // Android 7.0 미만에서는 Uri.fromFile 사용
            return Uri.fromFile(file);
        }
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            proceedWithStorageAccess();
            return;
        }

        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            proceedWithStorageAccess();
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우
                proceedWithStorageAccess();
            } else {
                // 권한이 거부된 경우
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 권한이 허용되었을 때 파일 저장 작업 수행
    private void proceedWithStorageAccess() {
        // 파일을 저장하는 작업 등 수행
        startAction(clickView);
        Toast.makeText(this, "Storage access granted", Toast.LENGTH_SHORT).show();
    }


    private static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private List<Uri> mUris;
        private List<String> mPaths;

        void setData(List<Uri> uris, List<String> paths) {
            mUris = uris;
            mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {
            holder.mUri.setText(mUris.get(position).toString());
            holder.mPath.setText(mPaths.get(position));
            holder.mImageView.setImageBitmap(BitmapFactory.decodeFile(mPaths.get(position)));

            holder.mUri.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
            holder.mPath.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        static class UriViewHolder extends RecyclerView.ViewHolder {

            private TextView mUri;
            private TextView mPath;
            private ImageView mImageView;

            UriViewHolder(View contentView) {
                super(contentView);
                mUri = (TextView) contentView.findViewById(R.id.uri);
                mPath = (TextView) contentView.findViewById(R.id.path);
                mImageView = (ImageView)contentView.findViewById(R.id.imageView);
            }
        }
    }

}
