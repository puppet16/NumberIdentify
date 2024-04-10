package cn.ltt.luck.numberidentify.widget

import java.text.SimpleDateFormat
import java.util.*

/**
 * ============================================================
 *
 * @author 李桐桐
 * date    2024/4/10
 * desc    描述
 * ============================================================
 **/
class TimeUtil {
    companion object {
        fun getNowTime(): String {
            val timestampMillis = System.currentTimeMillis() // 获取当前时间的毫秒时间戳
            val date = Date(timestampMillis)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINESE) // 定义包含毫秒的日期时间格式
            return dateFormat.format(date) // 将日期对象格式化为字符串
        }
    }
}