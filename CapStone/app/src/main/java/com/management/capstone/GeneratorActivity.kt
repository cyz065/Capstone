package com.management.capstone

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.management.capstone.databinding.ActivityGeneratorBinding
import com.management.capstone.server.ResponsePicture
import com.management.capstone.server.RetrofitAPI
import com.management.capstone.server.RetrofitService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.random.nextInt


class GeneratorActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivityGeneratorBinding
    private val subjectMap = HashMap<String, String>()
    private lateinit var timer:CountDownTimer
    private var score = 0
    private var round = 0
    private val categories = ArrayList<String>()
    private val values = ArrayList<BarEntry>()
    private lateinit var selected:String
    private var selectedProb = 0
    private var tmpProb = 0
    private lateinit var items:MutableList<String>

    private val engToKor = mapOf(
        "The_Eiffel_Tower" to "에펠탑", "cup" to "컵", "apple" to "사과", "mushroom" to "버섯", "cookie" to "쿠키", "car" to "자동차", "airplane" to "비행기",
        "clock" to "시계", "face" to "얼굴", "bottlecap" to "병뚜껑", "bicycle" to "자전거", "book" to "책", "sun" to "태양", "butterfly" to "나비", "fish" to "물고기"
    )

    private val korToEng = mapOf(
        "에펠탑" to "theeiffeltower", "컵" to "cup", "사과" to "apple", "버섯" to "mushroom", "쿠키" to "cookie", "자동차" to "car", "비행기" to "airplane",
        "시계" to "clock", "얼굴" to "face", "병뚜껑" to "bottlecap", "자전거" to "bicycle", "책" to "book", "태양" to "sun", "나비" to "butterfly", "물고기" to "fish"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGeneratorBinding.inflate(layoutInflater)
        score  = intent.getIntExtra("Score", 0)
        round = intent.getIntExtra("Round", 1)

        Log.e("인텐트2", "score : $score round : $round")
        setContentView(binding.root)
        binding.score!!.text = "score: $score"
        makeSubject()
        showProgress(false)

        dialog()

        binding.itemGroup!!.setOnCheckedChangeListener { radioGroup, id ->
            if(radioGroup.checkedRadioButtonId != -1) {
                val btn = findViewById<RadioButton>(id)
                selected = btn.text.toString()

                Log.e("선택", "$selected ${categories[0]} $selectedProb")

                if(selected == categories[0] && selectedProb >= 80) {
                    binding.nextStage?.background =
                        ContextCompat.getDrawable(baseContext, R.drawable.main_button)
                    binding.nextStage?.setTextColor(Color.BLACK)
                    binding.nextStage?.isEnabled = true
                    val leftString = binding.timer!!.text.toString().split(":")
                    score += (leftString[0].toInt() * 100 * tmpProb / 100)
                    Log.e("점수", "$leftString")
                    binding.score!!.text = "score: $score"
                    timer.cancel()
                    makeChart()
                }
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
                dialog.dismiss()

                showProgress(true)
                thread(start = true) {
                    binding.generatorView?.visibility = View.INVISIBLE
                    Thread.sleep(3000)
                    runOnUiThread {
                        showProgress(false)
                        //binding.generatorView?.visibility = View.VISIBLE
                        val idx = Random.nextInt(0, 6)
                        korToEng[items[idx]]?.let { makeImage(it) }
                    }
                }
                //makeImage()
            }

        builder.setCancelable(false)
        builder.create().show()
    }

    private fun showProgress(isShow:Boolean) {
        if(isShow) binding.loading?.visibility = View.VISIBLE
        else binding.loading?.visibility = View.GONE
    }

    private fun makeImage(category:String) {
        val retIn = RetrofitService.getRetrofitInstance().create(RetrofitAPI::class.java)
        val  call = retIn.getImage(category)
        call.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful) {
                    if(response.body() != null) {
                        val bmp = BitmapFactory.decodeStream(response.body()!!.byteStream())
                        binding.generatorView?.setImageBitmap(bmp)
                        binding.generatorView?.visibility = View.VISIBLE

                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                        createImage(bmp, timeStamp)
                        val file = File("/storage/emulated/0/Pictures/MyImage/${timeStamp}.png")
                        requestToServer(file)
                    }

                    else {
                        Log.e("통신", "response body null")
                    }
                }
                else {
                    Log.e("통신", "response fail ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("통신", "완전 실패 ${t.message}")
            }
        })
    }

    private fun createImage(image: Bitmap, name:String): Boolean {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyImage"
        )
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("My Image", "Failed create Directory")
                return false
            }
        }

        val temp = File("$dir/$name.png")
        if (!temp.exists()) {
            Log.e("My File", temp.toString())
            try {
                val fos = FileOutputStream(temp)
                image.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()
            } catch (exception: Error) {
                exception.printStackTrace()
            }
        }
        return true
    }

    private fun requestToServer(file: File) {
        val retIn = RetrofitService.getRetrofitInstance().create(RetrofitAPI::class.java)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData(name="image_file", filename= file.name, requestBody)

        val call: Call<ResponsePicture> = retIn.requestPicture(body)
        call.enqueue(object:
            Callback<ResponsePicture> {
            override fun onFailure(call: Call<ResponsePicture>, t: Throwable) {
                Log.e("서버 통신 실패", "${t.message}")
            }

            override fun onResponse(
                call: Call<ResponsePicture>,
                response: retrofit2.Response<ResponsePicture>) {
                if(response.isSuccessful) {
                    val pictureResponse = response.body()
                    if(pictureResponse != null) {
                        val id = pictureResponse.id
                        val image_file = pictureResponse.image_file
                        val class1 = pictureResponse.predicted_class1
                        val class2 = pictureResponse.predicted_class2
                        val class3 = pictureResponse.predicted_class3
                        val class4 = pictureResponse.predicted_class4
                        val class5 = pictureResponse.predicted_class5

                        var tmp = class1.split(" : ")
                        var prob = (tmp[1].toFloat() * 100).toInt()

                        val classified = tmp[0]

                        selectedProb = prob

                        Log.e("로그인 통신 성공", "id : $id image_file : $image_file tmp : $tmp")
                        Log.e("로그인 통신 성공", "class1 : $class1 class2 : $class2 class3 : $class3 class4 : $class4 class5 : $class5")

                        var total = 100
                        var randomValue = 0

                        if(prob >= 80) {
                            randomValue = Random.nextInt(79, 100) + 1
                            tmpProb = randomValue
                            engToKor[tmp[0]]?.let { categories.add(it) }
                            values.add(BarEntry(0f, randomValue.toFloat()))
                            total -= randomValue
                        }

                        tmp = class2.split(" : ")
                        prob = (tmp[1].toFloat() * 100).toInt()
                        engToKor[tmp[0]]?.let { categories.add(it) }

                        if(total > 0) {
                            randomValue = Random.nextInt(0, total) + 1
                            values.add(BarEntry(1f, randomValue.toFloat()))
                            total -= randomValue
                        }

                        else {
                            values.add(BarEntry(1f, 0f))
                        }


                        //tmp = class2.split(" : ")
                        //prob = (tmp[1].toFloat() * 100).toInt()
                        //engToKor[tmp[0]]?.let { categories.add(it) }
                        //values.add(BarEntry(1f, prob.toFloat()))


                        tmp = class3.split(" : ")
                        prob = (tmp[1].toFloat() * 100).toInt()
                        engToKor[tmp[0]]?.let { categories.add(it) }

                        if(total > 0) {
                            randomValue = Random.nextInt(0, total) + 1
                            values.add(BarEntry(2f, randomValue.toFloat()))
                            total -= randomValue
                        }
                        else {
                            values.add(BarEntry(2f, 0f))
                        }

                        tmp = class4.split(" : ")
                        prob = (tmp[1].toFloat() * 100).toInt()
                        engToKor[tmp[0]]?.let { categories.add(it) }

                        if(total > 0) {
                            randomValue = Random.nextInt(0, total) + 1
                            values.add(BarEntry(3f, randomValue.toFloat()))
                            total -= randomValue
                        }
                        else {
                            values.add(BarEntry(3f, 0f))
                        }

                        tmp = class5.split(" : ")
                        prob = (tmp[1].toFloat() * 100).toInt()
                        engToKor[tmp[0]]?.let { categories.add(it) }

                        if(total > 0) {
                            randomValue = Random.nextInt(0, total) + 1
                            values.add(BarEntry(4f, randomValue.toFloat()))
                        }
                        else values.add(BarEntry(4f, 0f))

                        Log.e("카테고리", "$subjectMap $prob $classified")

                        //makeChart()
                        /*
                        if(subjectMap.containsKey(classified) && prob > 80) {
                            timer.cancel()
                            binding.resultView!!.text = subjectMap[classified]

                            val leftString = binding.timer!!.text.toString().split(":")
                            Log.e("점수", "$leftString")
                            score += leftString[0].toInt() * 100
                            binding.score!!.text = "score: $score"

                            //Log.e("점수", "$score")

                            binding.nextStage?.background = ContextCompat.getDrawable(baseContext, R.drawable.main_button)
                            binding.nextStage?.setTextColor(Color.BLACK)
                            binding.nextStage?.isEnabled = true

                            Toast.makeText(baseContext, "점수 : $score", Toast.LENGTH_SHORT).show()
                        }*/

                        /*
                        for(i in 0 until engCategory.size) {
                            if(engCategory[i] == tmp[0] && prob > 80) {
                                binding.resultView!!.text = korCategory[i]
                                timer.cancel()
                                val leftString = binding.timer!!.text.toString().split(":")
                                score += leftString[0].toInt() * 100

                                Log.e("점수", "$score")

                                binding.nextStage?.background = ContextCompat.getDrawable(baseContext, R.drawable.main_button)
                                binding.nextStage?.setTextColor(Color.BLACK)
                                binding.nextStage?.isEnabled = true
                                timer.cancel()
                                Toast.makeText(baseContext, "점수 : $score", Toast.LENGTH_SHORT).show()
                            }
                        }*/
                    }
                }

                else {
                    Log.e("서버 4XX", "실패${response.code()}")
                }
            }
        })
    }


    private fun makeChart() {
        binding.chart?.description?.isEnabled = false
        binding.chart?.setTouchEnabled(false)
        binding.chart?.legend?.isEnabled = false
        binding.chart?.setExtraOffsets(10f, 0f, 50f, 0f)

        val xAxis = binding.chart!!.xAxis
        xAxis.setDrawAxisLine(false)
        xAxis.granularity = 1f
        xAxis.textSize = 15f
        xAxis.gridLineWidth = 25f
        // 바깥쪽
        xAxis.gridColor = Color.parseColor("#80E5E5E5")
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val axisLeft = binding.chart!!.axisLeft
        axisLeft.setDrawGridLines(false)
        axisLeft.setDrawAxisLine(false)
        axisLeft.axisMinimum = 0f // 최솟값
        axisLeft.axisMaximum = 100f // 최댓값
        axisLeft.granularity = 1f // 값만큼 라인선 설정
        axisLeft.setDrawLabels(false) // label 삭제

        val axisRight = binding.chart!!.axisRight
        axisRight.textSize = 15f
        axisRight.setDrawLabels(false)
        axisRight.setDrawGridLines(false)
        axisRight.setDrawAxisLine(false)

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return categories[value.toInt()]
            }
        }

        val data = createData()

        Log.e("차트 데이터", "$values")
        Log.e("차트 데이터", "$categories")

        binding.chart!!.data = data
        binding.chart!!.invalidate()
    }

    private fun createData():BarData {
        val set2 = BarDataSet(values, "TEST")
        set2.setDrawIcons(false)
        set2.setDrawValues(true)
        set2.color = Color.parseColor("#667676")
        set2.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString() + "%"
            }
        }
        val data = BarData(set2)
        data.barWidth = 0.5f
        data.setValueTextSize(15F)
        return data
    }


    private fun makeSubject() {
        while(subjectMap.size < 5) {
            val index = SecureRandom().nextInt(15)
            Log.e("주제", "$index")
            if(!subjectMap.containsKey(engCategory[index])) {
                subjectMap[engCategory[index]] = korCategory[index]
            }
        }

        items = subjectMap.values.toMutableList()
        Log.e("주제", items.toString())
        binding.item1!!.text = items[0]
        binding.item2!!.text = items[1]
        binding.item3!!.text = items[2]
        binding.item4!!.text = items[3]
        binding.item5!!.text = items[4]
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