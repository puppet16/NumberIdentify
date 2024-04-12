package cn.ltt.luck.numberidentify.tflite2

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.ltt.luck.numberidentify.R
import cn.ltt.luck.numberidentify.databinding.ActivitySingleModelBinding
import com.hoc081098.viewbindingdelegate.viewBinding

/**
 * ============================================================
 *
 * @author 李桐桐
 * date    2024/4/10
 * desc    只能识别单个数字
 * ============================================================
 **/
class TFLite2NumActivity: AppCompatActivity(R.layout.activity_single_model) {

    private val binding by viewBinding<ActivitySingleModelBinding>()
    private val distinguishManager by lazy { TFLite2Manager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnClear.setOnClickListener { onClearClicked() }
        binding.btnDetect.setOnClickListener { onDetectClicked() }
    }

    /**
     * 点击识别
     */
    private fun onDetectClicked() {
        val bitmap = binding.fpvPaint.exportToBitmap(TFLite2Manager.IMAGE_SIZE_X, TFLite2Manager.IMAGE_SIZE_Y)
        val distinguishResult = distinguishManager.classify(bitmap)
        renderResult(distinguishResult)
    }

    /**
     * 清空点击
     */
    private fun onClearClicked() {
        binding.fpvPaint.clear()
        binding.tvPrediction.setText(R.string.empty)
        binding.tvProbability.setText(R.string.empty)
        binding.tvTimecost.setText(R.string.empty)
        binding.ivPreview.setImageBitmap(Bitmap.createBitmap(10,10, Bitmap.Config.ARGB_8888))
    }

    /**
     * 结果展示
     */
    private fun renderResult(result: TF2DistinguishResult) {
        binding.tvPrediction.text = result.number.toString()
        binding.tvProbability.text = result.probability.toString()
        binding.tvTimecost.text = String.format(getString(R.string.timecost_value),
            result.timeCost
        )
    }
}