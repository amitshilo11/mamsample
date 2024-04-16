package com.assacnetworks.mam_sample

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.assacnetworks.mam_sample.microsoft.MicrosoftManager
import com.microsoft.intune.mam.client.app.MAMComponents
import com.microsoft.intune.mam.client.strict.MAMStrictMode
import com.microsoft.intune.mam.policy.MAMEnrollmentManager


class MainActivity : AppCompatActivity() {
    lateinit var microsoftManager: MicrosoftManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initMAM()

        val loginButton: Button = findViewById(R.id.connect_with_microsoft_button)
        loginButton.setOnClickListener {
            microsoftManager.onClickSignIn()
        }

        val statusButton: Button = findViewById(R.id.get_status_button)
        statusButton.setOnClickListener {
            microsoftManager.getEnrollmentStatus()
        }
    }

    private fun initMAM() {
        microsoftManager = MicrosoftManager(this, this@MainActivity)
    }

}