package com.example.lasbetalk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.et_login_id)
        val password = findViewById<EditText>(R.id.et_login_password)
        val btnLogin = findViewById<Button>(R.id.profile_button)

        btnLogin.setOnClickListener {
            val emailInput = email.text.toString()
            val passwordInput = password.text.toString()

            if (emailInput == "demo@gmail.com" && passwordInput == "password") {
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                val intentMain = Intent(this, MainActivity::class.java)
                startActivity(intentMain)
            } else {
                Toast.makeText(this, "정확한 아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입창으로 이동
        val btnRegistration = findViewById<Button>(R.id.btn_registration)
        btnRegistration.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }
}
