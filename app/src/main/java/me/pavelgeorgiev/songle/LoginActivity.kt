package me.pavelgeorgiev.songle


import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.TextView
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_login_logout.*
import com.google.firebase.auth.FirebaseUser
import android.text.SpannableString
import android.util.Log
import android.text.TextUtils
import android.text.style.UnderlineSpan
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.support.v7.app.AlertDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


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
        setContentView(R.layout.activity_login_logout)

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
        mDetailTextView.setOnClickListener(this)

//      Firebase Auth client and Database
        mAuth = FirebaseAuth.getInstance()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
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
                        Snackbar.make(findViewById(R.id.login_logout_activity), task.exception!!.message.toString(),
                                Snackbar.LENGTH_LONG)
                                .show()
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

                        Snackbar.make(findViewById(R.id.login_logout_activity), task.exception!!.message.toString(),
                                Snackbar.LENGTH_LONG)
                                .show()
                    }

                    if (!task.isSuccessful) {
                        mStatusTextView.setText(R.string.auth_failed)
                    }
                    hideProgressDialog()
                }
    }

    private fun preAnonymousLogin() {
        val alertDialog = AlertDialog.Builder(this@LoginActivity).create()
        alertDialog.setTitle("Saved progress")
        alertDialog.setMessage(getString(R.string.dialog_login_disclaimer))

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button),
                { dialog, _ ->
                    dialog.dismiss()
                })

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.continue_button),
                { dialog, _ ->
                    dialog.dismiss()
                    signInAnonymously()
                })
        alertDialog.show()
    }

    private fun signInAnonymously() {
        Log.d(TAG, "signInAnonymously")
        mAuth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success")
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.exception)
                        Snackbar.make(findViewById(R.id.login_logout_activity), task.exception!!.message.toString(),
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
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


    private fun hideSoftKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
        if (user != null) {
            hideSoftKeyboard(mStatusTextView)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            mStatusTextView.text = getString(R.string.signed_out)

            val detailText = SpannableString(getString(R.string.login_anonymously))
            detailText.setSpan(UnderlineSpan(), 0 , detailText.length, 0)
            mDetailTextView.visibility = View.VISIBLE
            mDetailTextView.text =  detailText


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
            R.id.detail -> preAnonymousLogin()
            R.id.sign_out_button -> signOut()
        }
    }

}
