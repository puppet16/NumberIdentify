package cn.ltt.luck.numberidentify.mlkit

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.ltt.luck.numberidentify.R
import cn.ltt.luck.numberidentify.databinding.ActivityNumIdentifyBinding
import com.hoc081098.viewbindingdelegate.viewBinding

/**
 * ============================================================
 *
 * @author 李桐桐
 * date    2024/4/10
 * desc    描述
 * ============================================================
 **/
class GoogleMLKitActivity: AppCompatActivity(R.layout.activity_num_identify) {

    private val binding by viewBinding<ActivityNumIdentifyBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnClear.setOnClickListener { onClearClicked() }
        binding.btnDetect.setOnClickListener { onDetectClicked() }
    }

    /**
     * 点击识别
     */
    private fun onDetectClicked() {
        val bitmap = binding.fpvPaint.exportToBitmap()
        binding.ivPreview.setImageBitmap(bitmap)
        MLkitNumberIdentifyManager.identify(
            bitmap,
            object : MLkitNumberIdentifyManager.IdentifyResultListener {
                override fun onResult(result: String) {
                    renderResult(result)
                }

                override fun onError(e: Exception) {
                    e.printStackTrace()
                }

            })
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
    private fun renderResult(result: String) {
        binding.tvPrediction.text = result
    }
}