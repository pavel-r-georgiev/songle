package me.pavelgeorgiev.songle


import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.auth.FirebaseUser
import android.widget.Toast
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "LoginActivity"
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private lateinit var mAuth: FirebaseAuth

    private lateinit var mStatusTextView: TextView
    private lateinit var  mDetailTextView: TextView
    private lateinit var  mEmailField: EditText
    private lateinit var  mPasswordField: EditText
    private lateinit var mProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Views
        mStatusTextView = status
        mDetailTextView = detail
        mEmailField = field_email
        mPasswordField = field_password
        mProgressBar = login_progress_bar

        // Buttons
        email_sign_in_button.setOnClickListener(this)
        email_create_account_button.setOnClickListener(this)
        sign_out_button.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()


    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun showProgressDialog(){
        mProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressDialog() {
        mProgressBar.visibility = View.INVISIBLE
    }

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:" + email)
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        // Start create account task
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    hideProgressDialog()
                }
    }


    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:" + email)
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        // Start sign in task
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    if (!task.isSuccessful) {
                        mStatusTextView.setText(R.string.auth_failed)
                    }
                    hideProgressDialog()
                }
    }

    private fun signOut() {
        mAuth.signOut()
        updateUI(null)
    }


    private fun validateForm(): Boolean {
        var valid = true

        val email = mEmailField.text.toString()
        when {
            TextUtils.isEmpty(email) -> {
                mEmailField.error = "Required."
                valid = false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                mEmailField.error = "Email is invalid."
                valid = false
            }
            else -> mEmailField.error = null
        }

        val password = mPasswordField.text.toString()
        when {
            TextUtils.isEmpty(password) -> {
                mPasswordField.error = "Required."
                valid = false
            }
            password.length < 6 -> {
                mPasswordField.error = "Password should be at least 6 characters."
                valid = false
            }
            else -> mPasswordField.error = null
        }

        return valid
    }


    fun hideSoftKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
        if (user != null) {
            mStatusTextView.text = getString(R.string.login_status_fmt,
                    user.email, user.isEmailVerified)
            mDetailTextView.text = getString(R.string.user_status_fmt, user.uid)

             email_password_buttons.visibility = View.GONE
            email_password_fields.visibility = View.GONE
            signed_in_buttons.visibility = View.VISIBLE
            hideSoftKeyboard(mStatusTextView)

        } else {
            mStatusTextView.setText(R.string.signed_out)
            mDetailTextView.text = null

            email_password_buttons.visibility = View.VISIBLE
            email_password_fields.visibility = View.VISIBLE
            signed_in_buttons.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.email_create_account_button -> createAccount(mEmailField.text.toString(), mPasswordField.text.toString())
            R.id.email_sign_in_button -> signIn(mEmailField.text.toString(), mPasswordField.text.toString())
            R.id.sign_out_button -> signOut()
        }
    }

}
