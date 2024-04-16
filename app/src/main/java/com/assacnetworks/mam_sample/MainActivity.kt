package com.assacnetworks.mam_sample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.assacnetworks.mam_sample.microsoft.MicrosoftManager
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.intune.mam.client.app.MAMComponents
import com.microsoft.intune.mam.client.notification.MAMNotificationReceiverRegistry
import com.microsoft.intune.mam.policy.MAMEnrollmentManager
import com.microsoft.intune.mam.policy.notification.MAMEnrollmentNotification
import com.microsoft.intune.mam.policy.notification.MAMNotification
import com.microsoft.intune.mam.policy.notification.MAMNotificationType


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

        val registerCallbackButton: Button = findViewById(R.id.register_to_mam_callback_button)
        registerCallbackButton.setOnClickListener {
            registerAuthCMAMCallback()
        }

        val statusButton: Button = findViewById(R.id.get_status_button)
        statusButton.setOnClickListener {
            microsoftManager.getEnrollmentStatus()
        }
    }

    private fun initMAM() {
        microsoftManager = MicrosoftManager(this, this@MainActivity)
    }

    private fun registerAuthCMAMCallback() {
        // Registers a MAMAuthenticationCallback, which will try to acquire access tokens for MAM.
        // This is necessary for proper MAM integration.
        val mgr = MAMComponents.get(MAMEnrollmentManager::class.java)
        mgr!!.registerAuthenticationCallback(com.assacnetworks.mam_sample.microsoft.AuthenticationCallback(this))

        /* This section shows how to register a MAMNotificationReceiver, so you can perform custom
         * actions based on MAM enrollment notifications.
         * More information is available here:
         * https://docs.microsoft.com/en-us/intune/app-sdk-android#types-of-notifications */
        MAMComponents.get(MAMNotificationReceiverRegistry::class.java)!!
            .registerReceiver(
                { notification: MAMNotification? ->
                    if (notification is MAMEnrollmentNotification) {
                        val result =
                            (notification as MAMEnrollmentNotification).enrollmentResult
                        when (result) {
                            MAMEnrollmentManager.Result.AUTHORIZATION_NEEDED, MAMEnrollmentManager.Result.NOT_LICENSED, MAMEnrollmentManager.Result.ENROLLMENT_SUCCEEDED, MAMEnrollmentManager.Result.ENROLLMENT_FAILED, MAMEnrollmentManager.Result.WRONG_USER, MAMEnrollmentManager.Result.UNENROLLMENT_SUCCEEDED, MAMEnrollmentManager.Result.UNENROLLMENT_FAILED, MAMEnrollmentManager.Result.PENDING, MAMEnrollmentManager.Result.COMPANY_PORTAL_REQUIRED -> Log.d(
                                "Enrollment Receiver",
                                result.name
                            )

                            else -> Log.d("Enrollment Receiver", result.name)
                        }
                    } else {
                        Log.d("Enrollment Receiver", "Unexpected notification type received")
                    }
                    true
                }, MAMNotificationType.MAM_ENROLLMENT_RESULT
            )
    }

}