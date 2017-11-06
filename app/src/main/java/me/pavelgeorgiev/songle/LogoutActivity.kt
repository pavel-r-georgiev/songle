package me.pavelgeorgiev.songle

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login_logout.*

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider



class LogoutActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "LogoutActivity"
    private lateinit var mAuth: FirebaseAuth

    private lateinit var  mEmailField: EditText
    private lateinit var  mPasswordField: EditText
    private lateinit var mProgressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_logout)

        // Views
        mEmailField = field_email
        mPasswordField = field_password
        mProgressBar = login_progress_bar

        // Buttons
        sign_out_button.setOnClickListener(this)
        detail.setOnClickListener(this)
        link_account_button.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()
        updateUI(mAuth.currentUser)
    }


    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            title_text.text = getString(R.string.logout_title)

            val detailText = SpannableString(getString(R.string.go_back_to_songs))
            detailText.setSpan(UnderlineSpan(), 0, detailText.length, 0)
            detail.visibility = View.VISIBLE
            detail.text = detailText

            var email: String? = user.email
            signed_in_buttons.visibility= View.VISIBLE
            email_password_buttons.visibility = View.GONE

            if(email.isNullOrEmpty()){
                title_text.text = getString(R.string.account_management_title)
                email = "Anonymous"
                email_password_fields.visibility = View.VISIBLE
                link_account_button.visibility = View.VISIBLE
                email_create_account_button.visibility = View.VISIBLE
            } else {
                email_password_fields.visibility = View.GONE
            }

            signed_in_buttons.visibility = View.VISIBLE
            status.text = getString(R.string.login_status_fmt,
                    email)
        } else {
           CommonFunctions.signOut(this)
        }
    }


    private fun validateForm(): Boolean {
        var valid = true

        val email = mEmailField.text.toString()
        when {
            TextUtils.isEmpty(email) -> {
                mEmailField.error = getString(R.string.error_field_required)
                valid = false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                mEmailField.error = getString(R.string.error_invalid_email)
                valid = false
            }
            else -> mEmailField.error = null
        }

        val password = mPasswordField.text.toString()
        when {
            TextUtils.isEmpty(password) -> {
                mPasswordField.error = getString(R.string.error_field_required)
                valid = false
            }
            password.length < 6 -> {
                mPasswordField.error = getString(R.string.error_invalid_password)
                valid = false
            }
            else -> mPasswordField.error = null
        }

        return valid
    }

    private fun linkAccount() {
        // Make sure form is valid
        if (!validateForm()) {
            return
        }

        // Get email and password from form
        val email = mEmailField.text.toString()
        val password = mPasswordField.text.toString()

        // Create EmailAuthCredential with email and password
        val credential = EmailAuthProvider.getCredential(email, password)

        // Link the anonymous user to the email credential
        showProgressDialog()

        mAuth.currentUser!!.linkWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "linkWithCredential:success")
                        val user = task.result.user
                        updateUI(user)
                    } else {
                        Log.w(TAG, "linkWithCredential:failure", task.exception)

                        Snackbar.make(findViewById(R.id.login_logout_activity), task.exception!!.message.toString(),
                                Snackbar.LENGTH_LONG)
                                .show()
                    }

                    hideProgressDialog()
                }
    }

    private fun showProgressDialog(){
        mProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressDialog() {
        mProgressBar.visibility = View.INVISIBLE
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.detail -> startActivity(Intent(this, MainActivity::class.java))
            R.id.sign_out_button -> CommonFunctions.signOut(this)
            R.id.link_account_button -> linkAccount()
        }
    }

}
