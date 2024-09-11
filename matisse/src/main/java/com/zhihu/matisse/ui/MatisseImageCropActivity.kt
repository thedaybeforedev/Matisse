package com.zhihu.matisse.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.appbar.AppBarLayout
import com.yalantis.ucrop.UCrop
import com.zhihu.matisse.R
import com.zhihu.matisse.adapter.MatisseImageCropViewPagerAdapter
import com.zhihu.matisse.viewpager.MatisseSwipeControlViewpager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MatisseImageCropActivity : AppCompatActivity() {
    var imageCropViewPagerAdapter: MatisseImageCropViewPagerAdapter? = null
    var viewPagerImageCrop: MatisseSwipeControlViewpager? = null
    var textViewToolbar: TextView? = null
    var relativeProgressBar: RelativeLayout? = null
    var relativeContainer: RelativeLayout? = null
    var appBarLayout: AppBarLayout? = null


    private var imagePathArrays: Array<String>? = null
    private var imagePathUriArrays: Array<String>? = null
    private var storedImageFileNameArrays: Array<String>? = null
    private var storeFilePath: String? = null

    private var isCheckUri: Boolean = false

    var linearBottomButtonEdit: LinearLayout? = null
    private var toolbar: Toolbar? = null
    private var currentPage = 0

    var isCroppedImageAvailable = false
        private set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matisse_imagecrop)
        onBindLayout()
        onBindData()
    }

    fun onBindLayout() {
        viewPagerImageCrop = findViewById(R.id.viewPagerImageCrop)
        textViewToolbar = findViewById(R.id.textViewToolbar)
        relativeProgressBar = findViewById(R.id.relativeProgressBar)
        relativeContainer = findViewById(R.id.relativeContainer)
        appBarLayout = findViewById(R.id.appBarLayout)
        linearBottomButtonEdit = findViewById(R.id.linearBottomButtonEdit)
        linearBottomButtonEdit!!.setOnClickListener(View.OnClickListener {
            imageCropStart()
        })
        setToolbar()
        setStatusbarTransparent(false)
        setStatusBarAndNavigationBarColors()

    }

    fun onBindData() {
        if (intent.extras != null) {
            val intent = intent
            imagePathArrays = intent.getStringArrayExtra(PARAM_IMAGEPATH_ARRAY)
            imagePathUriArrays = intent.getStringArrayExtra(PARAM_IMAGEURI_ARRAY)
            storedImageFileNameArrays = intent.getStringArrayExtra(PARAM_STORE_FILE_NAME_ARRAY)
            if (imagePathArrays == null && intent.getStringExtra(PARAM_IMAGEPATH) != null) {
                imagePathArrays = arrayOf(intent.getStringExtra(PARAM_IMAGEPATH)?:"")
            }
            if (storedImageFileNameArrays == null && intent.getStringExtra(PARAM_STORE_FILE_NAME) != null) {
                storedImageFileNameArrays = arrayOf(intent.getStringExtra(PARAM_STORE_FILE_NAME)?:"")
            }
            if (imagePathUriArrays == null && intent.getStringExtra(PARAM_IMAGEURI) != null) {
                imagePathUriArrays = arrayOf(intent.getStringExtra(PARAM_IMAGEURI)?:"")
            }

            currentPage = intent.getIntExtra(BUNDLE_POSITION, 0)
            storeFilePath = intent.getStringExtra(PARAM_STORE_FILE_PATH)
            isCheckUri = intent.getBooleanExtra(PARAM_TYPE_URI, false)
            imageCropViewPagerAdapter = MatisseImageCropViewPagerAdapter(supportFragmentManager, this, imagePathArrays?.toMutableList(), imagePathUriArrays?.toMutableList(), storedImageFileNameArrays?.toMutableList() ,storeFilePath)
            viewPagerImageCrop!!.adapter = imageCropViewPagerAdapter
            viewPagerImageCrop!!.addOnPageChangeListener(viewPagerOnPageChangeListener)
            if (currentPage > 0) {
                viewPagerImageCrop!!.setCurrentItem(currentPage, false)
            }
        }

        val storePath = storeFilePath ?: File("$cacheDir/images").absolutePath
        val imageDir = File(storePath)
        if (!imageDir.exists()) {
            // 디렉토리가 존재하지 않으면 생성
            imageDir.mkdirs() // 상위 폴더가 없는 경우도 대비해 전체 폴더 경로를 생성합니다.
        }

        setCurrentPage()
        invalidateOptionsMenu()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu items for use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.actionbar_image_crop, menu)

        var spanString: SpannableString? = null

        menu.findItem(R.id.action_save).isVisible = true
        menu.findItem(R.id.action_save).setTitle(R.string.complete)
        spanString = SpannableString(menu.findItem(R.id.action_save).title.toString())
        spanString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorTextPrimary)), 0, spanString.length, 0) //fix the color to white

        menu.findItem(R.id.action_save).title = spanString
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            //전체 이미지 저장 처리
            saveAllImagesAndExit()
        } else if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_change, R.anim.slide_down_translate)
    }

    val isPlatformOverLollipop: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()

        relativeContainer?.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundPrimary))
        appBarLayout?.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundPrimary))
        if (!isPlatformOverLollipop) {
            val apply = getString(R.string.common_confirm)
            setMenuTextColor(this, toolbar, apply, R.id.action_save, R.color.colorTextPrimary)
        }

    }

    private fun imageCropStart() {

        val imagePath = imagePathArrays?.get(currentPage)

        if (imagePath != null && imagePath.isNotEmpty()) {
            val file: File
            val uri: Uri
            if (!isCheckUri){
                file = File(imagePath)
                uri = Uri.fromFile(file)
            }else{
                uri = Uri.parse(imagePath)
            }
            val outputUri = Uri.fromFile(File("${cacheDir}/images",
                storedImageFileNameArrays?.get(currentPage) ?: storedImageFileNameArrays?.get(0)
            ))

            // UCrop 설정
            val uCrop = UCrop.of(uri, outputUri)
            val uCropOption = UCrop.Options()
            uCropOption.setCompressionQuality(25)
            uCrop.withMaxResultSize(1920, 1920)
            uCrop.withOptions(uCropOption);

            uCrop.start(this@MatisseImageCropActivity, UCrop.REQUEST_CROP)
        } else {
            Log.e("UCropError", "Invalid image path: $imagePath")
            // Handle error: Show a message or take appropriate action
        }

        invalidateOptionsMenu()
    }



    private fun saveAllImagesAndExit() {
        showProgressLoading()

        CoroutineScope(Dispatchers.IO).launch {
            var saveImage : Array<String> = arrayOf()
            val savedFile: MutableList<String> = mutableListOf()
            val size = imageCropViewPagerAdapter!!.getStoredImageFileNameList()?.size ?: 0

            for (i in 0 until size) {
                withContext(Dispatchers.Main) {
                    viewPagerImageCrop?.setCurrentItem(i, false)
                    delay(400)
                }

                savedFile.add(imageCropViewPagerAdapter!!.saveCroppedImages(i)!!)

            }
            saveImage = savedFile.toTypedArray()

            val resultValue = Intent()
            resultValue.putExtra(PARAM_IMAGEPATH_ARRAY, saveImage)
            resultValue.putExtra(PARAM_IMAGE_EDITED, isCroppedImageAvailable)
            setResult(RESULT_OK, resultValue)
            finish()
            hideProgressLoading()
        }

    }

    private val viewPagerOnPageChangeListener: OnPageChangeListener = object :
        OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {
            currentPage = position
            setCurrentPage()
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    private fun setCurrentPage() {
        textViewToolbar!!.text = "" + (currentPage + 1) + "/" + imageCropViewPagerAdapter!!.count
    }

    fun hideProgressLoading() {
        if (relativeProgressBar != null) {
            relativeProgressBar!!.post(Runnable {
                if (relativeProgressBar == null) return@Runnable
                relativeProgressBar!!.visibility = View.GONE
            })
        }
    }

    fun showProgressLoading() {
        if (relativeProgressBar != null) {
            relativeProgressBar!!.post(Runnable {
                if (relativeProgressBar == null) return@Runnable
                relativeProgressBar!!.visibility = View.VISIBLE
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            // 결과 URI 사용
            resultUri?.path?.let {
                imagePathUriArrays?.set(currentPage, it)
                imagePathArrays?.set(currentPage, it)
                imageCropViewPagerAdapter!!.cropChangImage(currentPage, it)
            }

            isCroppedImageAvailable = true

        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            // 에러 처리
        }else{

        }
    }



    fun setStatusbarTransparent(useStatusBarHeight: Boolean = true) {

        if (useStatusBarHeight && findViewById<View?>(R.id.appBarLayout) != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
            appBarLayout.setPadding(0, getStatusBarHeight(this), 0, 0)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = ContextCompat.getColor(this, R.color.paletteTransparent)
        }
    }

    fun setStatusBarAndNavigationBarColors() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.paletteTransparent)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) window.navigationBarDividerColor = ContextCompat.getColor(this, R.color.paletteTransparent)
            window.statusBarColor = ContextCompat.getColor(this, R.color.paletteTransparent)
        }
        if (isDarkMode(this)) {
            clearLightModeStatusBar()

        } else {
            setLightModeStatusBar()
        }
    }


    fun getStatusBarHeight(context: Context): Int {
        var result = context.resources.getDimension(R.dimen.appbar_padding_top).toInt()
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) result = context.resources.getDimensionPixelSize(resourceId)
        return result
    }

    fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    private fun setLightModeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = window.decorView
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    fun clearLightModeStatusBar(isChangeNavigation: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = window.decorView
            window.statusBarColor = Color.BLACK
            decor.systemUiVisibility = 0 // 시스템 UI 플래그 초기화 (Light Status Bar 해제)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.BLACK
        }
    }


    protected fun setToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        if (toolbar == null) return
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
            val xBtn = ContextCompat.getDrawable(this, R.drawable.ic_x)

            xBtn?.setTintList(
                when {
                    !isDarkMode(this) -> ColorStateList.valueOf(Color.BLACK)
                    else -> ColorStateList.valueOf(Color.WHITE)
                }
            )
            actionBar.setHomeAsUpIndicator(xBtn)
        }
    }

    fun isDarkMode(context: Context, showLog: Boolean = false): Boolean {
        val configuration = context.resources.configuration
        val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                if(showLog) Log.e("TAG", "UI_MODE_NIGHT_NO")
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                if(showLog) Log.e("TAG", "UI_MODE_NIGHT_YES")
                return true
            }
        }
        return false
    }




    companion object {
        const val PARAM_IMAGEPATH = "imagePath"
        const val PARAM_IMAGEURI = "imageUri"
        const val PARAM_STORE_FILE_NAME = "storeFileName"
        const val PARAM_IMAGEPATH_ARRAY = "imagePathArray"
        const val PARAM_IMAGEURI_ARRAY = "imageUriArray"
        const val PARAM_STORE_FILE_NAME_ARRAY = "storeFileNameArray"
        const val PARAM_STORE_FILE_PATH = "storeFilePath"
        const val PARAM_IMAGE_EDITED = "imageEdited"
        const val PARAM_TYPE_URI = "isUriType"
        const val BUNDLE_POSITION = "position"

        private fun setMenuTextColor(context: Context, toolbar: Toolbar?, title: String, menuResId: Int, colorRes: Int) {
            toolbar!!.post {
                val settingsMenuItem = toolbar.findViewById<View>(menuResId)
                if (settingsMenuItem is TextView) {
                    settingsMenuItem.setTextColor(ContextCompat.getColor(context, colorRes))
                } else { // you can ignore this branch, because usually there is not the situation
                    val menu = toolbar.menu
                    val item = menu.findItem(menuResId)
                    val s = SpannableString(title)
                    s.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, colorRes)), 0, title.length, 0)
                    item.title = s
                }
            }
        }
    }
}