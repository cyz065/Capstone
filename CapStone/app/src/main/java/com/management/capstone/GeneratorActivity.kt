package com.management.capstone

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.management.capstone.databinding.ActivityGeneratorBinding
import java.security.SecureRandom
import java.util.HashMap
import kotlin.math.roundToInt
import kotlin.random.Random

class GeneratorActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivityGeneratorBinding
    private val subjectMap = HashMap<String, String>()
    private lateinit var timer:CountDownTimer
    private var score = 0
    private var round = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGeneratorBinding.inflate(layoutInflater)
        score  = intent.getIntExtra("Score", 0)
        round = intent.getIntExtra("Round", 1)

        Log.e("인텐트2", "score : $score round : $round")
        setContentView(binding.root)
        binding.score!!.text = "score: $score"
        makeSubject()

        dialog()

        binding.itemGroup!!.setOnCheckedChangeListener { radioGroup, i ->
            if(radioGroup.checkedRadioButtonId != -1) {
                binding.nextStage?.background = ContextCompat.getDrawable(baseContext, R.drawable.main_button)
                binding.nextStage?.setTextColor(Color.BLACK)
                binding.nextStage?.isEnabled = true
            }
        }
        binding.nextStage!!.setOnClickListener(this)
    }

    private fun dialog () {
        val builder = AlertDialog.Builder(this)
        builder
            .setTitle("알림")
            .setMessage("제한 시간 내에 가장 적절한 카테고리를 선택해 주세요")
            .setPositiveButton("확인"
            ) { dialog, _ ->
                var secondsLeft = 0
                val number_millis = 30000L

                timer = object: CountDownTimer(30000, 1) {
                    override fun onTick(millisUntilFinished: Long) {
                        val t = millisUntilFinished
                        val tmp = (t / 1000.0f).roundToInt()

                        if (tmp != secondsLeft) {
                            secondsLeft = tmp
                        }
                        val roundMillis = (secondsLeft * 1000).toLong()

                        if (roundMillis == number_millis) {
                            binding.timer!!.text = secondsLeft.toString() + ":" + String.format("%03d", 0)

                        } else {
                            binding.timer!!.text = secondsLeft.toString() + ":" + String.format("%03d", millisUntilFinished % 1000L)
                        }
                    }

                    override fun onFinish() {
                        binding.timer!!.text = "0:000"
                        Toast.makeText(baseContext, "Time Over", Toast.LENGTH_SHORT).show()
                        binding.nextStage?.background = ContextCompat.getDrawable(baseContext, R.drawable.main_button)
                        binding.nextStage?.setTextColor(Color.BLACK)
                        binding.nextStage?.isEnabled = true
                    }
                }
                timer.start()
                dialog.dismiss() }

        builder.setCancelable(false)
        builder.create().show()
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
        when(view.id) {
            R.id.nextStage -> {
                if(round == 6) {
                    val intent = Intent(this, ScoreActivity::class.java)
                    intent.putExtra("Score", score)
                    intent.putExtra("Round", round)
                    startActivity(intent)
                    timer.cancel()
                    finish()
                }

                else {
                    val intent = Intent(this, ClassifierActivity::class.java)
                    intent.putExtra("Score", score)
                    intent.putExtra("Round", round + 1)
                    startActivity(intent)
                    timer.cancel()
                    finish()
                }
            }
        }
    }
}