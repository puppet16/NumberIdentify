package cn.ltt.luck.numberidentify.tflite

import android.content.Context
import android.graphics.Bitmap
import cn.ltt.luck.numberidentify.widget.LogUtil
import cn.ltt.luck.numberidentify.widget.TimeUtil


/**
 * ============================================================
 *
 * @author 李桐桐
 * date    2024/4/10
 * desc
 * ============================================================
 **/
class TfLiteNumberIdentifyManager {

    companion object {
        private var ocrExecutor: OCRModelExecutor? = null

        fun init(context: Context, useGPU: Boolean) {
            ocrExecutor = OCRModelExecutor(context, useGPU)
        }

        fun identify(bitmap: Bitmap): ModelExecutionResult? {
            LogUtil.d(msg = "identify 开始时间：${TimeUtil.getNowTime()}")
            val result = ocrExecutor?.execute(bitmap)
            LogUtil.d(msg = "identify 结束时间：${TimeUtil.getNowTime()}")
            return result
        }

    }

}