package com.management.capstone

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import com.management.capstone.databinding.ActivityRankingBinding
import com.management.capstone.server.*
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RankingActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivityRankingBinding
    private lateinit var members:ArrayList<ResponseRanking>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingBinding.inflate(layoutInflater)
        members = ArrayList()

        setContentView(binding.root)
        getFromServer()
        binding.mainButton!!.setOnClickListener(this)

    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.mainButton -> {
                //val intent = Intent(this, MainActivity::class.java)
                //startActivity(intent)
                finish()
            }
        }
    }

    private fun makeBoard() {
        var current = 0

        for(i in members.indices) {
            val tableRow = TableRow(this)
            val rankView = TextView(this)
            val nameView = TextView(this)
            val scoreView = TextView(this)

            val params = TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT, 1.0f
            )

            rankView.text = members[i].ranking.toString()
            nameView.text = members[i].nickName//"test $current"//members[i].nickName
            scoreView.text = members[i].score.toString()
            current += 1

            Log.e("랭킹", "$i ${members[i].ranking} ${members[i].nickName} ${members.size}")

            rankView.includeFontPadding = false
            nameView.includeFontPadding = false
            scoreView.includeFontPadding = false

            rankView.setTextColor(Color.BLACK)
            nameView.setTextColor(Color.BLACK)
            scoreView.setTextColor(Color.BLACK)

            rankView.textSize = 40f
            nameView.textSize = 40f
            scoreView.textSize = 40f

            rankView.typeface = resources.getFont(R.font.share_tech)
            nameView.typeface = resources.getFont(R.font.share_tech)
            scoreView.typeface = resources.getFont(R.font.share_tech)

            nameView.gravity = Gravity.CENTER
            rankView.gravity = Gravity.CENTER
            scoreView.gravity = Gravity.CENTER

            tableRow.gravity = Gravity.CENTER
            //tableRow.layoutParams = TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
            //TableRow.LayoutParams.WRAP_CONTENT)
            rankView.layoutParams = params
            nameView.layoutParams = params
            scoreView.layoutParams = params

            tableRow.addView(rankView)
            tableRow.addView(nameView)
            tableRow.addView(scoreView)

            if(i < 10) {
                binding.tableLayout!!.addView(tableRow)
            }

            else if(i >= 10) {
                binding.tableLayout2!!.addView(tableRow)
            }
        }
    }

    private fun getFromServer() {
        val retIn = RetrofitService.getRetrofitInstance().create(RetrofitAPI::class.java)
        retIn.getRanking().enqueue(object :Callback<List<ResponseRanking>> {
            override fun onResponse(
                call: Call<List<ResponseRanking>>,
                response: Response<List<ResponseRanking>>
            ) {
                if(response.isSuccessful) {
                    val test = response.body()
                    if(test != null) {
                        for(i in test.indices) {
                            members.add(test[i])
                        }
                    }

                    makeBoard()

                    //Log.e("랭킹", "성공 ${test?.size}")
                }
                else {
                    Log.e("랭킹", "실패 ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<ResponseRanking>>, t: Throwable) {
                Log.e("랭킹", "완전 실패 ${t.message}")
            }
        })

    }
}