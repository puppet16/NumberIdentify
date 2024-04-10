package cn.ltt.luck.numberidentify

import android.graphics.Bitmap
import cn.ltt.luck.numberidentify.widget.LogUtil
import cn.ltt.luck.numberidentify.widget.TimeUtil
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


/**
 * ============================================================
 *
 * @author 李桐桐
 * date    2024/4/10
 * desc    描述
 * ============================================================
 **/
class NumberIdentifyManager {

    companion object {
        private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }


        fun identify(bitmap: Bitmap, listener: IdentifyResultListener) {
            LogUtil.d(msg = "identify 开始时间：${TimeUtil.getNowTime()}")
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(inputImage)
                .addOnSuccessListener { text ->
                    LogUtil.d(msg = "identify 出结果时间-success：${TimeUtil.getNowTime()}")
                    listener.onResult(processTextBlock(text))
                }
                .addOnFailureListener { e ->
                    LogUtil.d(msg = "identify 出结果时间-error：${TimeUtil.getNowTime()}")
                    listener.onError(e)
                }
            result.toString()
        }

        private fun processTextBlock(text: Text): String {
            LogUtil.d(msg = "identify 处理结果--开始时间：${TimeUtil.getNowTime()}, text.text=${text.text}")

            // 处理识别的文本结果
            val result = StringBuilder()
            for (block in text.textBlocks) {
                val blockText = block.text
                val blockCornerPoints = block.cornerPoints
                val blockFrame = block.boundingBox
                for (line in block.lines) {
                    result.append(line.text).append("\n")
                    val lineText = line.text
                    val lineCornerPoints = line.cornerPoints
                    val lineFrame = line.boundingBox
                    for (element in line.elements) {
                        val elementText = element.text
                        val elementCornerPoints = element.cornerPoints
                        val elementFrame = element.boundingBox
                        LogUtil.d(msg = "identify 处理结果--element：elementText=${elementText}, elementCornerPoints=${elementCornerPoints}, elementFrame=${elementFrame}")
                    }
                    LogUtil.d(msg = "identify 处理结果--line：lineText=${lineText}, lineCornerPoints=${lineCornerPoints}, lineFrame=${lineFrame}")
                }
                LogUtil.d(msg = "identify 处理结果--block：blockText=${blockText}, blockCornerPoints=${blockCornerPoints}, blockFrame=${blockFrame}")

                result.append("\n")
            }
//            for (block in text.textBlocks) {
//                for (line in block.lines) {
//                    LogUtil.d(msg = "identify 处理结果--line：elements=${line.elements}, angle=${line.angle}, recognizedLanguage=${line.recognizedLanguage}")
//
//                    result.append(line.text).append("\n")
//                }
//                result.append("\n")
//            }
            LogUtil.d(msg = "identify 处理结果--结束时间：${TimeUtil.getNowTime()}")
            return result.toString()
        }
    }

    interface IdentifyResultListener {
        fun onResult(result: String)
        fun onError(e: Exception)
    }
}