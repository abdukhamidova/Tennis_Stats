package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.buttonLogIn).setOnClickListener {
            startActivity(Intent(this,SignInActivity::class.java))
        }

        findViewById<Button>(R.id.buttonSignUp).setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }

    }
    //ogólnie nie działa więc można nie mergować
    /*override fun onBackPressed(){
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            onBackPressedDispatcher.onBackPressed()
        }else{
            Toast.makeText(applicationContext, "Press back again to EXIT", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }*/
    /*onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(backPressedTime + 2000 > System.currentTimeMillis()){
                finish()
            }else{
                Toast.makeText(applicationContext, "Press back again to EXIT", Toast.LENGTH_SHORT).show()
            }
            backPressedTime = System.currentTimeMillis()

        }
    })*/


    override fun onStart() {
        super.onStart()
        if(firebaseAuth.currentUser!=null){
            val intent = Intent(this,ActivityMenu::class.java)
            startActivity(intent)
        }
    }

}