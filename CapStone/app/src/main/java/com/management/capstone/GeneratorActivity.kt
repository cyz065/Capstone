package com.management.capstone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.management.capstone.databinding.ActivityGeneratorBinding
import java.security.SecureRandom
import java.util.HashMap
import kotlin.random.Random

class GeneratorActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivityGeneratorBinding
    private val subjectMap = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeSubject()
    }

    private fun makeSubject() {
        while(subjectMap.size < 5) {
            val index = SecureRandom().nextInt(15)
            Log.e("주제", "$index")
            if(!subjectMap.containsKey(engCategory[index])) {
                subjectMap[engCategory[index]] = korCategory[index]
            }
        }

        val values = subjectMap.values.toMutableList()
        Log.e("주제", values.toString())
        binding.item1!!.text = values[0]
        binding.item2!!.text = values[1]
        binding.item3!!.text = values[2]
        binding.item4!!.text = values[3]
        binding.item5!!.text = values[4]
    }

    override fun onClick(view: View) {

    }
}