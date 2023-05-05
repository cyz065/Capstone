package com.management.capstone

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.management.capstone.databinding.ActivityScoreBinding
import com.management.capstone.server.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ScoreActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivityScoreBinding
    private var score = 0
    private var round = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        score  = intent.getIntExtra("Score", 0)
        round = intent.getIntExtra("Round", 1)
        binding = ActivityScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView!!.text = "당신의 점수는 ${score}점 입니다."
        binding.check!!.setOnClickListener(this)
        binding.toMain!!.setOnClickListener(this)
        nameFocusListener()
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.check -> {
                if(!valid()) {
                    Toast.makeText(this, "이름을 확인해 주세요", Toast.LENGTH_SHORT).show()
                    return
                }
                requestToServer(binding.name!!.text.toString(), score)
                Toast.makeText(this, "서버 요청 완료", Toast.LENGTH_SHORT).show()
            }

            R.id.toMain-> {
                //val intent = Intent(this, MainActivity::class.java)
                //startActivity(intent)
                finish()
            }
        }
    }


    private fun nameFocusListener() {
        binding.name!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                binding.nameContainer!!.error = validName()
            }

        })
    }

    private fun validName():String? {
        val nameText = binding.name!!.text.toString()
        if(nameText.isEmpty()) {
            return "이름을 입력해 주세요"
        }

        if(!nameText.matches("^[a-zA-Z0-9]*\$".toRegex())) {
            return "이름은 알파벳, 숫자만 가능합니다."
        }
        return null
    }

    private fun valid():Boolean {
        if(binding.nameContainer!!.error == null && !binding.name!!.text.isNullOrEmpty()) {
            return true
        }
        return false
    }

    private fun requestToServer(userName:String, score:Int) {
        val retIn = RetrofitService.getRetrofitInstance().create(RetrofitAPI::class.java)
        val info = RequestScore(userName, score)

        retIn.requestScore(info).enqueue(object:
            Callback<ResponseScore> {
            override fun onFailure(call: Call<ResponseScore>, t: Throwable) {
                Log.e("서버 통신 실패", "${t.message}")
            }

            override fun onResponse(call: Call<ResponseScore>, response: Response<ResponseScore>) {
                if(response.isSuccessful) {
                    if(response.body() != null) {
                        val scoreResponse = response.body()
                        val message = scoreResponse!!.message
                        Log.e("서버 통신 성공", "message : $message")
                        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
                    }
                }

                else {
                    Log.e("서버 통신 성공 4XX", "code : ${response.code()}" + " message : ${response.errorBody()!!.string()}")
                    Toast.makeText(this@ScoreActivity, "점수 등록 실패", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}