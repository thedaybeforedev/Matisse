package com.zhihu.matisse.ui

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.zhihu.matisse.R
import java.io.File

class ImageCropViewFragment : Fragment() {

    var imagePath: String? = null
    var cropImageView: AppCompatImageView? = null
    var relativeProgressBar: View? = null
    var textViewCreateDate: TextView? = null

    var isCropImage: Boolean = false


    override fun onStop() {
        super.onStop()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (onContentLayoutId() == 0) {
            onBindLayout(null)
            return null
        }
        var mRootView = inflater.inflate(onContentLayoutId(), container, false)

        onBindLayout(mRootView)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onBindData()
    }


    private fun onContentLayoutId(): Int {
        return R.layout.fragment_imagecropview
    }

    fun onBindLayout(rootView: View?) {
        if (rootView == null)
            return

        cropImageView = rootView.findViewById(R.id.cropImageView)
        relativeProgressBar = rootView.findViewById(R.id.relativeProgressBar)
        textViewCreateDate = rootView.findViewById(R.id.textViewCreateDate)
    }

    fun onBindData() {
        if (arguments != null) {
            if(imagePath == null){
                imagePath = requireArguments().getString(ImageCropActivity.PARAM_IMAGEPATH)
            }
            loadCropImage()
        }

    }


    fun loadCropImage() {
        var loadImageFilePath = imagePath
        val file = File(imagePath)
        if (file.exists()) {
            loadImageFilePath = file.absolutePath
        }else{

        }


        //cropImageView!!.setImageUriAsync(Uri.fromFile(file)) // 크롭할 이미지 URI 설정


        if (!TextUtils.isEmpty(loadImageFilePath))
            Glide.with(this)
            .load(loadImageFilePath)
            .apply(RequestOptions().signature(ObjectKey(File(loadImageFilePath).lastModified())))
            .listener(object : RequestListener<Drawable?> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    hideProgressLoading()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    hideProgressLoading()
                    if (resource is GifDrawable) {
                        return true
                    }
                    return false
                }


            })
            .into(cropImageView!!)
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
    }

    override fun onPause() {
        super.onPause()
    }

    private fun hideProgressLoading() {
        if (relativeProgressBar != null) relativeProgressBar!!.visibility = View.GONE
    }

    private fun showProgressLoading() {
        if (relativeProgressBar != null) relativeProgressBar!!.visibility = View.VISIBLE
    }

    fun changeCropImage(cropPath: String){
        imagePath = cropPath
        isCropImage = true
        loadCropImage()
    }



    companion object {
        fun newInstance(imagePath: String?): ImageCropViewFragment {
            val fragment = ImageCropViewFragment()
            val args = Bundle()
            args.putString(ImageCropActivity.PARAM_IMAGEPATH, imagePath)
            fragment.arguments = args
            return fragment
        }
    }
}