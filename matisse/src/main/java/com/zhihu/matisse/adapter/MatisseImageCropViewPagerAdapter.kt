package com.zhihu.matisse.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zhihu.matisse.Util.MatisseCompressor
import com.zhihu.matisse.ui.MatisseImageCropViewFragment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MatisseImageCropViewPagerAdapter(fm: FragmentManager?, var mContext: Context, imagePathList: MutableList<String>?, imageUriList: MutableList<String>?, storedImageFileNameList: MutableList<String>?, storeFilePath: String?, isCheckUri: Boolean = false) : FragmentPagerAdapter(fm!!) {
    var mLayoutInflater: LayoutInflater
    private val imagePathList: List<String>?
    private val imageUriList: List<String>?
    private val storedImageFileNameList: List<String>?
    private val imageCropViewFragmentSparseArray: SparseArray<MatisseImageCropViewFragment?>?
    private var storeFilePath: String?
    private var isCheckUri: Boolean = false
    override fun getCount(): Int {
        return imagePathList?.size?:0
    }

    override fun getItem(position: Int): Fragment {
        val imagePath = imagePathList?.get(position)
        val imageUri = imageUriList?.get(position)
        val imageCropViewFragment = MatisseImageCropViewFragment.newInstance(imagePath, imageUri, isCheckUri)
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
                if(isCheckUri){
                    val file = File("${mContext.cacheDir}/images", storedImageFileNameList?.get(position) ?: storedImageFileNameList?.get(0))
                    var path: String? = null
                    path = downloadImageToFile(fragment.imagePath!!, file)
                    return path
                }else{
                    val file = compressor.setMaxHeight(1920).setMaxWidth(1920) //                      .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .setQuality(25)
                        .compressToFile(File(fragment.imagePath!!), storedImageFileNameList!![position])

                    return  file.absolutePath
                }
            }
    }

    fun downloadImageToFile(imageUrl: String, file: File): String? {

        try {
            // 1. 이미지 URL로부터 Bitmap 다운로드
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            val bitmap: Bitmap = BitmapFactory.decodeStream(input)

            // 2. Bitmap을 파일로 저장
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)  // PNG, WEBP로도 저장 가능
            outputStream.flush()
            outputStream.close()

            // 저장 완료 메시지
            println("Image downloaded and saved to ${file.absolutePath}")
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
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
        this.imageUriList = imageUriList
        this.storedImageFileNameList = storedImageFileNameList
        this.storeFilePath = storeFilePath
        this.isCheckUri = isCheckUri
        imageCropViewFragmentSparseArray = SparseArray()
    }
}