package cn.ltt.luck.numberidentify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.ltt.luck.numberidentify.databinding.ActivityMainBinding
import com.hoc081098.viewbindingdelegate.viewBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding<ActivityMainBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnClassification.setOnClickListener {

        }

        binding.btnIRSingle.setOnClickListener {

        }

        binding.btnMLKit.setOnClickListener {
            startActivity(Intent(this, GoogleMLKitActivity::class.java))
        }
    }
}