package com.zhihu.matisse.adapter

import android.content.Context
import android.text.TextUtils
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zhihu.matisse.Util.MatisseCompressor
import com.zhihu.matisse.ui.MatisseImageCropViewFragment
import java.io.File

class MatisseImageCropViewPagerAdapter(fm: FragmentManager?, var mContext: Context, imagePathList: MutableList<String>?, storedImageFileNameList: MutableList<String>?, storeFilePath: String?) : FragmentPagerAdapter(fm!!) {
    var mLayoutInflater: LayoutInflater
    private val imagePathList: List<String>?
    private val storedImageFileNameList: List<String>?
    private val imageCropViewFragmentSparseArray: SparseArray<MatisseImageCropViewFragment?>?
    private var storeFilePath: String?

    override fun getCount(): Int {
        return imagePathList?.size?:0
    }

    override fun getItem(position: Int): Fragment {
        val imagePath = imagePathList?.get(position)

        val imageCropViewFragment = MatisseImageCropViewFragment.newInstance(imagePath)
        imageCropViewFragmentSparseArray!!.append(position, imageCropViewFragment as MatisseImageCropViewFragment)
        return imageCropViewFragment
    }


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val createdFragment = super.instantiateItem(container, position) as Fragment
        val imageCropViewFragment = createdFragment as MatisseImageCropViewFragment
        imageCropViewFragmentSparseArray!!.append(position, imageCropViewFragment)
        return createdFragment
    }

    fun getStoredImageFileNameList():List<String>? {
        return storedImageFileNameList
    }


    fun cropChangImage(position: Int, image: String){
        if (getImageCropViewFragmentByPosition(position) == null) return
        val imageCropViewFragment = getImageCropViewFragmentByPosition(position)
        imageCropViewFragment!!.changeCropImage(image)

    }

    fun saveCroppedImages(position: Int): String? {
        if(imageCropViewFragmentSparseArray == null) return null

        val compressor = MatisseCompressor(mContext)
        if (!TextUtils.isEmpty(storeFilePath)) {
            compressor.setDestinationDirectoryPath(storeFilePath)
        }
            val fragment = getImageCropViewFragmentByPosition(position) ?: return null
            if(fragment.isCropImage){
                return fragment.imagePath!!
            }else{

                val file = compressor.setMaxHeight(1920).setMaxWidth(1920) //                      .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setQuality(25)
                    .compressToFile(File(fragment.imagePath!!), storedImageFileNameList!![position])

                return  file.absolutePath
            }
    }

    fun getImageCropViewFragmentByPosition(position: Int): MatisseImageCropViewFragment? {
        return if (imageCropViewFragmentSparseArray != null && imageCropViewFragmentSparseArray[position] != null) {
            imageCropViewFragmentSparseArray[position]
        } else null
    }


    init {
        mLayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.imagePathList = imagePathList
        this.storedImageFileNameList = storedImageFileNameList
        this.storeFilePath = storeFilePath
        imageCropViewFragmentSparseArray = SparseArray()
    }
}