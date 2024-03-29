package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class ActivityMenu : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        auth = FirebaseAuth.getInstance()
        findViewById<Button>(R.id.buttonLogOut).setOnClickListener{
            auth.signOut()
            startActivity(Intent(this,MainActivity::class.java))
        }
        val user = FirebaseAuth.getInstance().currentUser?.email.toString()
        if(user.isNotEmpty())
        {
            findViewById<TextView>(R.id.textUser).text = user
        }
        else
        {
            findViewById<TextView>(R.id.textUser).text = "Tu powinien byc email zalogowanego usera."
        }
        findViewById<Button>(R.id.buttonStartNewGame).setOnClickListener{
            startActivity(Intent(this,StartNewActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

