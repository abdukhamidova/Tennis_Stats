package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anw.tenistats.databinding.ActivitySignUpBinding
import com.anw.tenistats.databinding.ActivityStartNewBinding
import com.google.firebase.auth.FirebaseAuth

class StartNewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartNewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Firebase Auth
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartNewBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_start_new)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        findViewById<Button>(R.id.buttonStartGame).setOnClickListener{
            callActivity()

        }
        //binding.textView2.text = firebaseAuth.currentUser?.email.toString()

    }

    private fun callActivity() {
        val autoPlayer1=findViewById<AutoCompleteTextView>(R.id.autoNamePlayer1)
        val player1=autoPlayer1.text.toString()
        val autoPlayer2=findViewById<AutoCompleteTextView>(R.id.autoNamePlayer2)
        val player2=autoPlayer2.text.toString()

        val intent=Intent(this,ActivityServe::class.java).also{
            it.putExtra("DanePlayer1",player1)
            it.putExtra("DanePlayer2",player2)
            startActivity(it)

        }

    }
}