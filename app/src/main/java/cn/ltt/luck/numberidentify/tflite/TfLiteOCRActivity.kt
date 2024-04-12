package cn.ltt.luck.numberidentify.tflite

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.ltt.luck.numberidentify.R
import cn.ltt.luck.numberidentify.databinding.ActivityNumIdentifyBinding
import cn.ltt.luck.numberidentify.widget.LogUtil
import com.google.android.material.chip.Chip
import com.hoc081098.viewbindingdelegate.viewBinding

/**
 * ============================================================
 *
 * @author 李桐桐
 * date    2024/4/10
 * desc    只能识别图片中的文字，手写的识别率太太低了
 * ============================================================
 **/
class TfLiteOCRActivity: AppCompatActivity(R.layout.activity_num_identify) {

    private val binding by viewBinding<ActivityNumIdentifyBinding>()
    private val TAG = this::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnClear.setOnClickListener { onClearClicked() }
        binding.btnDetect.setOnClickListener { onDetectClicked() }
        TfLiteNumberIdentifyManager.init(this, false)
    }

    /**
     * 点击识别
     */
    private fun onDetectClicked() {
        val bitmap = binding.fpvPaint.exportToBitmap(OCRModelExecutor.detectionImageWidth, OCRModelExecutor.detectionImageHeight)
        binding.ivPreview.setImageBitmap(bitmap)
        val result = TfLiteNumberIdentifyManager.identify(bitmap)
        renderResult(result)
    }



    /**
     * 清空点击
     */
    private fun onClearClicked() {
        binding.fpvPaint.clear()
        binding.tvPrediction.setText(R.string.empty)
        binding.ivPreview.setImageBitmap(Bitmap.createBitmap(10,10, Bitmap.Config.ARGB_8888))
    }

    /**
     * 结果展示
     */
    private fun renderResult(result: ModelExecutionResult?) {
        result?: return
        binding.ivPreview.setImageBitmap(result.bitmapResult)
        LogUtil.i(TAG, "result.executionLog=${result.executionLog}")
        LogUtil.i(TAG, "result.itemsFound=${result.itemsFound}")
        binding.tvPrediction.text = result.itemsFound.keys.joinToString("")
    }
}