package cn.ltt.luck.numberidentify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.ltt.luck.numberidentify.databinding.ActivityMainBinding
import cn.ltt.luck.numberidentify.mlkit.GoogleMLKitActivity
import cn.ltt.luck.numberidentify.singlemodel.SingleModelNumActivity
import cn.ltt.luck.numberidentify.tesseract.TesseractActivity
import cn.ltt.luck.numberidentify.tflite.TfLiteOCRActivity
import cn.ltt.luck.numberidentify.tflite2.TFLite2NumActivity
import com.hoc081098.viewbindingdelegate.viewBinding

class SplashActivity : AppCompatActivity(R.layout.activity_single_model) {

    private val binding by viewBinding<ActivityMainBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}