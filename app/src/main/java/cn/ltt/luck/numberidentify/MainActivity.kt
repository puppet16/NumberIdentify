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

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding<ActivityMainBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnTesseract.setOnClickListener {
            startActivity(Intent(this, TesseractActivity::class.java))

        }

        binding.btnIRSingle.setOnClickListener {
            startActivity(Intent(this, SingleModelNumActivity::class.java))

        }

        binding.btnMLKit.setOnClickListener {
            startActivity(Intent(this, GoogleMLKitActivity::class.java))
        }

        binding.btnTfOCR.setOnClickListener {
            startActivity(Intent(this, TfLiteOCRActivity::class.java))
        }

        binding.btnTfOCR2.setOnClickListener {
            startActivity(Intent(this, TFLite2NumActivity::class.java))
        }
    }
}