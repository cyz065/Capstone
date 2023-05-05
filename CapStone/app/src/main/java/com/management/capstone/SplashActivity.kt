package com.management.capstone

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.management.capstone.databinding.ActivitySplashBinding
import java.util.*
import kotlin.concurrent.schedule

class SplashActivity : AppCompatActivity(), View.OnClickListener {

    var contentHasLoaded = false
    private lateinit var binding:ActivitySplashBinding
    private var waitTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        setContentView(binding.root)
        startLoadingContent()
        setupSplashScreen(splashScreen)

        binding.startBtn!!.setOnClickListener(this)
        binding.exitBtn!!.setOnClickListener(this)
        binding.infoBtn!!.setOnClickListener(this)
        binding.rankBtn!!.setOnClickListener(this)
        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.startBtn -> {
                val intent = Intent(this, ClassifierActivity::class.java)
                startActivity(intent)
            }
            R.id.exitBtn -> {
                dialog()
            }
            R.id.rankBtn -> {
                val intent = Intent(this, RankingActivity::class.java)
                startActivity(intent)
            }
        }
    }


    private fun dialog (){
        val builder = AlertDialog.Builder(this)
        builder
            .setTitle("게임 종료")
            .setMessage("종료 하시겠습니까?")
            .setPositiveButton("네"
            ) { _, p1 -> finish() }
            .setNegativeButton("아니요"
            ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            quitAction()
        }
    }


    private fun quitAction() {
        if(System.currentTimeMillis() > waitTime + 2000) {
            waitTime = System.currentTimeMillis()
            Toast.makeText(baseContext, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if(System.currentTimeMillis() <= waitTime + 2000) {
            finish()
        }
    }


    private fun startLoadingContent() {
        Timer().schedule(1500) {
            contentHasLoaded = true
        }
    }

    private fun setupSplashScreen(splashScreen: SplashScreen) {
        val content:View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object: ViewTreeObserver.OnPreDrawListener{
                override fun onPreDraw(): Boolean {
                    return if(contentHasLoaded) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else{
                        false
                    }
                }
            }
        )

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideBack = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.TRANSLATION_X,
                0f,
                -splashScreenView.view.width.toFloat()
            ).apply {
                interpolator = DecelerateInterpolator()
                duration = 800L
                doOnEnd { splashScreenView.remove() }
            }
            slideBack.start()
        }
    }
}


