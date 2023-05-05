package com.management.capstone

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.management.capstone.databinding.ActivityClassifierBinding
import com.management.capstone.server.ResponsePicture
import com.management.capstone.server.RetrofitAPI
import com.management.capstone.server.RetrofitService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.random.Random

val engCategory = mutableListOf(
    "The_Eiffel_Tower", "cup", "apple", "mushroom", "cookie", "car", "airplane", "clock",
    "face", "bottlecap", "bicycle", "book", "sun", "butterfly", "fish")

val korCategory = mutableListOf(
    "에펠탑", "컵", "사과", "버섯", "쿠키", "자동차", "비행기", "시계",
    "얼굴", "병뚜껑", "자전거", "책", "태양", "나비", "물고기"
)

//var score:Int = 0
//var round:Int = 0

class ClassifierActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityClassifierBinding
    private lateinit var timer:CountDownTimer
    private var score = 0
    private var round = 1

    //private lateinit var startActivityForResult: ActivityResultLauncher<Intent>
    //private lateinit var directory: File
    //category = ["The_Eiffel_Tower", "cup", "apple", "mushroom", "cookie", "car", "airplane", "clock",
    private val subjectMap = HashMap<String, String>()

    /*
    private val engCategory = mutableListOf(
        "The_Eiffel_Tower", "cup", "apple", "mushroom", "cookie", "car", "airplane", "clock",
        "face", "bottlecap", "bicycle", "book", "sun", "butterfly", "fish")

    private val korCategory = mutableListOf(
        "에펠탑", "컵", "사과", "버섯", "쿠키", "자동차", "비행기", "시계",
        "얼굴", "병뚜껑", "자전거", "책", "태양", "나비", "물고기"
    )*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassifierBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        makeSubject()

        dialog()
        /*
        startActivityForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    if (it.data != null && it.data!!.data != null) {
                        val bitmap = binding.drawingView!!.save()!!
                        val uri: Uri = it.data!!.data!!
                        val op = contentResolver.openOutputStream(uri)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, op)
                    } else {
                        Toast.makeText(this, "Some error occured", Toast.LENGTH_SHORT).show()
                    }
                }
            }*/


        binding.nextStage!!.setOnClickListener(this)
        binding.unDo!!.setOnClickListener(this)
        binding.submit!!.setOnClickListener(this)



        //makeTimer()
        score = intent.getIntExtra("Score", 0)
        round = intent.getIntExtra("Round", 1)
        binding.score!!.text = "score: $score"
        Log.e("인텐트1", "score: $score round: $round")

    }

    private fun dialog () {
        val builder = AlertDialog.Builder(this)
        builder
            .setTitle("알림")
            .setMessage("제한 시간 내에 카테고리에 있는 그림을 그려주세요")
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

    private fun makeTimer() {
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
                Toast.makeText(baseContext, "Time Over Classifier", Toast.LENGTH_SHORT).show()
                binding.nextStage?.background = ContextCompat.getDrawable(baseContext, R.drawable.main_button)
                binding.nextStage?.setTextColor(Color.BLACK)
                binding.nextStage?.isEnabled = true
            }
        }
        timer.start()
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


    private fun initView() {
        val vto: ViewTreeObserver? = binding.drawingView?.viewTreeObserver
        vto?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.drawingView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val width = binding.drawingView!!.measuredWidth
                val height = binding.drawingView!!.measuredHeight
                binding.drawingView!!.init(height, width)
            }
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.unDo -> {
                Log.d("버튼", "지우기")
                binding.drawingView!!.clear()
            }

            R.id.nextStage -> {
                Log.d("버튼", "다음 스테이지")
                val intent = Intent(this, GeneratorActivity::class.java)
                //round += 1
                intent.putExtra("Round", round + 1)
                intent.putExtra("Score", score)
                startActivity(intent)
                timer.cancel()
                finish()
            }

            R.id.submit -> {
                Toast.makeText(this, "제출 완료", Toast.LENGTH_SHORT).show()
                Log.d("버튼", "제출 완료")

                //binding.nextStage?.background = ContextCompat.getDrawable(baseContext, R.drawable.main_button)
                //binding.nextStage?.setTextColor(Color.BLACK)
                //binding.nextStage?.isEnabled = true

                val bitmap = binding.drawingView!!.save()
                if (bitmap != null) {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    createImage(bitmap, timeStamp)
                    val file = File("/storage/emulated/0/Pictures/MyImage/${timeStamp}.png")
                    requestToServer(file)
                }
                //timer.cancel()


                /*
                if(bitmap == null) {
                    Log.d("이미지 크기", "NULL")
                }
                if(bitmap != null) {
                    Log.d("전처리 이전 이미지 크기", "width : ${bitmap.width} height: ${bitmap.height}")
                    createFile("sample.png", startActivityForResult)
                }*/
            }
        }
    }

    private fun requestToServer(file:File) {
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
                        val tmp = class1.split(" : ")
                        val prob = (tmp[1].toFloat() * 100).toInt()

                        Log.e("로그인 통신 성공", "id : $id image_file : $image_file tmp : $tmp")
                        Log.e("로그인 통신 성공", "class1 : $class1 class2 : $class2 class3 : $class3 class4 : $class4 class5 : $class5")
                        Toast.makeText(this@ClassifierActivity, "통신 성공", Toast.LENGTH_SHORT).show()


                        val classified = tmp[0]
                        Log.e("카테고리", "$subjectMap $prob $classified")
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
                        }

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
}
/*
    private fun createFile(fileName: String, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        // file type
        intent.type = "image/*"
        // file name
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        )
        launcher.launch(intent)
    }*/
/*
    private fun preprocessBitmap(bitmap: Bitmap):Bitmap {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, false)
        val grayBitmap = toGrayscale(resizedBitmap)

        val pixels:FloatArray = normalizeImage(grayBitmap)
        val finalBitmap = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)
        canvas.drawBitmap(finalBitmap, 0F, 0F, null)
        return finalBitmap
    }

    private fun toGrayscale(bmpOriginal:Bitmap):Bitmap {
        val width = bmpOriginal.width
        val height = bmpOriginal.height

        val grayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayScale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0F)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        canvas.drawBitmap(bmpOriginal, 0F, 0F, paint)
        return grayScale
    }*/
/*
    private fun normalizeImage(bmp:Bitmap):FloatArray {
        val width = bmp.width
        val height = bmp.height
        val pixels = FloatArray(width * height)

        val pixelArray = IntArray(width * height)
        bmp.getPixels(pixelArray, 0, width, 0, 0, width, height)

        for(i in pixelArray.indices) {
            val pixel = pixelArray[i]
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            val pixelValue = (0.2989 * red + 0.5870 * green + 0.1140 * blue) / 255.0F
            pixels[i] = pixelValue.toFloat()
        }
        return pixels
    }*/
/*
    private fun trimBitmap(bmp:Bitmap):Bitmap {
        val height = bmp.height
        val width = bmp.width

        Log.d("이미지 크기", "$height $width")
        // TRIM WIDTH - LEFT

        var startWidth = 0
        for(i in 0 until width) {
            if(startWidth == 0) {
                for(j in 0 until height) {
                    if(bmp.getPixel(i, j) != Color.TRANSPARENT) {
                        startWidth = i
                        break
                    }
                }
            } else {
                break
            }
        }

        // TRIM WIDTH - RIGHT
        var endWidth = 0
        for(i in width - 1 downTo 0) {
            if(endWidth == 0) {
                for(j in 0 until height) {
                    if(bmp.getPixel(i, j) != Color.TRANSPARENT) {
                        endWidth = i
                        break
                    }
                }
            } else {
                break
            }
        }

        // TRIM HEIGHT - TOP
        var startHeight = 0
        for(i in 0 until height) {
            if(startHeight == 0) {
                for(j in 0 until width) {
                    if(bmp.getPixel(j, i) != Color.TRANSPARENT) {
                        startHeight = i
                        break
                    }
                }
            } else {
                break
            }
        }

        // TRIM HEIGHT - BOTTOM
        var endHeight = 0
        for(i in height - 1 downTo 0) {
            if(endHeight == 0) {
                for(j in 0 until width) {
                    if(bmp.getPixel(j, i) != Color.TRANSPARENT) {
                        endHeight = i
                        break
                    }
                }
            } else {
                break
            }
        }
        Log.d("사진 크기", "$startWidth $startHeight" +
                " $endWidth $endHeight")

        return Bitmap.createBitmap(
            bmp, startWidth, startHeight,
            endWidth- startWidth, endHeight - startHeight)
    }*/
}
 */