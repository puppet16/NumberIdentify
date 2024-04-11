//package cn.ltt.luck.numberidentify.tesseract
//
//import android.R.attr
//import android.R.attr.text
//import android.graphics.Bitmap
//import android.os.Build
//import android.os.Environment
//import androidx.core.app.ActivityCompat.requestPermissions
//import cn.ltt.luck.numberidentify.widget.LogUtil
//import cn.ltt.luck.numberidentify.widget.TimeUtil
//import com.googlecode.tesseract.android.TessBaseAPI
//
//
///**
// * ============================================================
// *
// * @author 李桐桐
// * date    2024/4/10
// * desc
// *
// * 集成：https://github.com/adaptech-cz/Tesseract4Android
// * 识别时间：10ms以内
// * 包体积增加：需要将数据集复制到对应的机器上，数据集约20M
// *
// * ============================================================
// **/
//class TesseractNumberIdentifyManager {
//
//    companion object {
//
//        private var identifyClient = TessBaseAPI()
//
//        fun init(path: String) {
//            LogUtil.d(msg = "path=$path")
//            identifyClient.setDebug(true)
//            identifyClient.init(path, "eng")
//        }
//
//        fun identify(bitmap: Bitmap): String {
//            LogUtil.d(msg = "identify 开始时间：${TimeUtil.getNowTime()}")
//            identifyClient.setImage(bitmap)
//            LogUtil.d(msg = "identify 结束时间：${TimeUtil.getNowTime()}")
//
//            return identifyClient.getUTF8Text()
//        }
//    }
//}