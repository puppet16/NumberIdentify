package cn.ltt.luck.numberidentify.tesseract

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.ltt.luck.numberidentify.R
import cn.ltt.luck.numberidentify.databinding.ActivityNumIdentifyBinding
import cn.ltt.luck.numberidentify.widget.LogUtil
import com.hoc081098.viewbindingdelegate.viewBinding
import java.io.File

/**
 * ============================================================
 *
 * @author 李桐桐
 * date    2024/4/10
 * desc    使用 Tesseract 在Android上的集成
 * ============================================================
 **/
class TesseractActivity : AppCompatActivity(R.layout.activity_num_identify) {

    private val binding by viewBinding<ActivityNumIdentifyBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnClear.setOnClickListener { onClearClicked() }
        binding.btnDetect.setOnClickListener { onDetectClicked() }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            332
        )
        val filePath = File(filesDir, "tesseract").absolutePath
        LogUtil.d(msg= "filePath=$filePath")
//        TesseractNumberIdentifyManager.init(filePath)
    }

    /**
     * 点击识别
     */
    private fun onDetectClicked() {
        val bitmap = binding.fpvPaint.exportToBitmap()
        binding.ivPreview.setImageBitmap(bitmap)
//        val result = TesseractNumberIdentifyManager.identify(bitmap)
//        renderResult(result)
    }

    /**
     * 清空点击
     */
    private fun onClearClicked() {
        binding.fpvPaint.clear()
        binding.tvPrediction.setText(R.string.empty)
        binding.ivPreview.setImageBitmap(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888))
    }

    /**
     * 结果展示
     */
    private fun renderResult(result: String) {
        binding.tvPrediction.text = result
    }
}