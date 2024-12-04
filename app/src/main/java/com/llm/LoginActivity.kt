// Raghav old
/*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText: EditText = findViewById(R.id.login_username)
        val passwordEditText: EditText = findViewById(R.id.login_password)
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val loginButton: Button = findViewById(R.id.login_button)
        val signUpLink: TextView = findViewById(R.id.signup_link)
        val signUpUsernameEditText: EditText = findViewById(R.id.signup_username)
        val signUpPasswordEditText: EditText = findViewById(R.id.signup_password)
        val signUpButton: Button = findViewById(R.id.sign_up_button)

        // Initially show the login form, hide sign-up form
        showLoginForm()

        // Handle login button click
        loginButton.setOnClickListener {
            Log.i("LoginActivity", "Login Attempt")
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Retrieve stored credentials from SharedPreferences
            val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val storedUsername = sharedPreferences.getString("username", null)
            val storedPassword = sharedPreferences.getString("password", null)

            // Check if entered username and password match the stored credentials
            if (username == storedUsername && password == storedPassword) {
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putString("current_username", username)  // Store current username
                editor.apply()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Show sign-up form when "Sign Up" link is clicked
        signUpLink.setOnClickListener {
            showSignUpForm()
        }

        // Handle sign-up button click
        signUpButton.setOnClickListener {
            val username = signUpUsernameEditText.text.toString()
            val password = signUpPasswordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Save user credentials to SharedPreferences
                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("username", username)
                editor.putString("password", password)
                editor.putBoolean("isLoggedIn", true)
                editor.putString("current_username", username)  // Store current username
                editor.apply()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Sign-up failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Rest of the code remains unchanged...
    private fun showLoginForm() {
        val loginUsernameEditText: EditText = findViewById(R.id.login_username)
        val loginPasswordEditText: EditText = findViewById(R.id.login_password)
        val loginButton: Button = findViewById(R.id.login_button)
        val signUpLink: TextView = findViewById(R.id.signup_link)

        loginUsernameEditText.visibility = android.view.View.VISIBLE
        loginPasswordEditText.visibility = android.view.View.VISIBLE
        loginButton.visibility = android.view.View.VISIBLE
        signUpLink.visibility = android.view.View.VISIBLE

        val signUpUsernameEditText: EditText = findViewById(R.id.signup_username)
        val signUpPasswordEditText: EditText = findViewById(R.id.signup_password)
        val signUpButton: Button = findViewById(R.id.sign_up_button)

        signUpUsernameEditText.visibility = android.view.View.GONE
        signUpPasswordEditText.visibility = android.view.View.GONE
        signUpButton.visibility = android.view.View.GONE
    }

    private fun showSignUpForm() {
        val loginUsernameEditText: EditText = findViewById(R.id.login_username)
        val loginPasswordEditText: EditText = findViewById(R.id.login_password)
        val loginButton: Button = findViewById(R.id.login_button)
        val signUpLink: TextView = findViewById(R.id.signup_link)

        loginUsernameEditText.visibility = android.view.View.GONE
        loginPasswordEditText.visibility = android.view.View.GONE
        loginButton.visibility = android.view.View.GONE
        signUpLink.visibility = android.view.View.GONE

        val signUpUsernameEditText: EditText = findViewById(R.id.signup_username)
        val signUpPasswordEditText: EditText = findViewById(R.id.signup_password)
        val signUpButton: Button = findViewById(R.id.sign_up_button)

        signUpUsernameEditText.visibility = android.view.View.VISIBLE
        signUpPasswordEditText.visibility = android.view.View.VISIBLE
        signUpButton.visibility = android.view.View.VISIBLE
    }
}

*/

// Ayush


package com.llm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.llm.R

//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var sharedPreferences: SharedPreferences
//    public var selectedModelID: Int = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//
//        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//
//        // Auto-login if already logged in
//        if (sharedPreferences.getBoolean(IS_LOGGED_IN, false)) {
//            navigateToMain()
//        }
//
//        val usernameEditText: EditText = findViewById(R.id.login_username)
//        val passwordEditText: EditText = findViewById(R.id.login_password)
//        val loginButton: Button = findViewById(R.id.login_button)
//        val signUpLink: TextView = findViewById(R.id.signup_link)
//        val signUpUsernameEditText: EditText = findViewById(R.id.signup_username)
//        val signUpPasswordEditText: EditText = findViewById(R.id.signup_password)
//        val signUpButton: Button = findViewById(R.id.sign_up_button)
//        val spinner: Spinner = findViewById(R.id.model_spinner)
//
//        val adapter = ArrayAdapter.createFromResource(
//            this,
//            R.array.user_roles,
//            android.R.layout.simple_spinner_item
//        )
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinner.adapter = adapter
//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                val role = parent.getItemAtPosition(position).toString()
//                selectedModelID = position + 1
//                Toast.makeText(applicationContext, "Selected Model: $role", Toast.LENGTH_SHORT).show()
//                sharedPreferences.edit().putString(CURRENT_MODEL, selectedModelID.toString()).apply()
//                Log.d("Inference", "Putting value $selectedModelID")
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // Do nothing
//                sharedPreferences.edit().putString(CURRENT_MODEL, "1").apply()
//            }
//        }
//
//        showLoginForm()
//
//        loginButton.setOnClickListener {
//            val username = usernameEditText.text.toString()
//            val password = passwordEditText.text.toString()
//
//            if (username.isEmpty() || password.isEmpty()) {
//                showToast("Please enter both username and password")
//                return@setOnClickListener
//            }
//
//            val storedUsername = sharedPreferences.getString(USERNAME_KEY, null)
//            val storedPassword = sharedPreferences.getString(PASSWORD_KEY, null)
//            //get the model name here
//
//            if (username == storedUsername && password == storedPassword) {
//                sharedPreferences.edit()
//                    .putBoolean(IS_LOGGED_IN, true)
//                    .putString(CURRENT_USERNAME, username)
//                    .apply()
//                navigateToMain()
//            } else {
//                showToast("Login failed")
//            }
//        }
//
//        signUpLink.setOnClickListener {
//            showSignUpForm()
//        }
//
//        signUpButton.setOnClickListener {
//            val username = signUpUsernameEditText.text.toString()
//            val password = signUpPasswordEditText.text.toString()
//
//            if (username.isNotEmpty() && password.isNotEmpty()) {
//                sharedPreferences.edit()
//                    .putString(USERNAME_KEY, username)
//                    .putString(PASSWORD_KEY, password)
//                    .putBoolean(IS_LOGGED_IN, true)
//                    .putString(CURRENT_USERNAME, username)
//                    .apply()
//                navigateToMain()
//            } else {
//                showToast("Sign-up failed")
//            }
//        }
//    }
//
//    private fun showLoginForm() {
//        findViewById<EditText>(R.id.login_username).visibility = android.view.View.VISIBLE
//        findViewById<EditText>(R.id.login_password).visibility = android.view.View.VISIBLE
//        findViewById<Button>(R.id.login_button).visibility = android.view.View.VISIBLE
//        findViewById<TextView>(R.id.signup_link).visibility = android.view.View.VISIBLE
//
//        findViewById<EditText>(R.id.signup_username).visibility = android.view.View.GONE
//        findViewById<EditText>(R.id.signup_password).visibility = android.view.View.GONE
//        findViewById<Button>(R.id.sign_up_button).visibility = android.view.View.GONE
//
//    }
//
//    private fun showSignUpForm() {
//        findViewById<EditText>(R.id.login_username).visibility = android.view.View.GONE
//        findViewById<EditText>(R.id.login_password).visibility = android.view.View.GONE
//        findViewById<Button>(R.id.login_button).visibility = android.view.View.GONE
//        findViewById<TextView>(R.id.signup_link).visibility = android.view.View.GONE
//        findViewById<Spinner>(R.id.model_spinner).visibility = android.view.View.GONE
//
//        findViewById<EditText>(R.id.signup_username).visibility = android.view.View.VISIBLE
//        findViewById<EditText>(R.id.signup_password).visibility = android.view.View.VISIBLE
//        findViewById<Button>(R.id.sign_up_button).visibility = android.view.View.VISIBLE
//    }
//
//    private fun navigateToMain() {
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
//        finish()
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    companion object {
//        const val USERNAME_KEY = "username"
//        const val PASSWORD_KEY = "password"
//        const val IS_LOGGED_IN = "isLoggedIn"
//        const val CURRENT_USERNAME = "current_username"
//        const val CURRENT_MODEL = "1"
//    }
//}



//arnav new
//
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
//
class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val usersKey = "users_list"
    public var selectedModelID: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // Auto-login if already logged in
        if (sharedPreferences.getBoolean(IS_LOGGED_IN, false)) {
            navigateToMain()
        }

        val usernameEditText: EditText = findViewById(R.id.login_username)
        val passwordEditText: EditText = findViewById(R.id.login_password)
        val loginButton: Button = findViewById(R.id.login_button)
        val signUpLink: TextView = findViewById(R.id.signup_link)
        val signUpUsernameEditText: EditText = findViewById(R.id.signup_username)
        val signUpPasswordEditText: EditText = findViewById(R.id.signup_password)
        val signUpButton: Button = findViewById(R.id.sign_up_button)
        val spinner: Spinner = findViewById(R.id.model_spinner)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.user_roles,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val role = parent.getItemAtPosition(position).toString() 
                selectedModelID = position + 1
                Toast.makeText(applicationContext, "Selected Model: $role", Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().putString(CURRENT_MODEL, selectedModelID.toString()).apply()
                Log.d("Inference", "Putting value $selectedModelID")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
              
                sharedPreferences.edit().putString(CURRENT_MODEL, "1").apply()
            }
        }

        showLoginForm()

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                showToast("Please enter both username and password")
                return@setOnClickListener
            }

            val users = getUsers()
            val user = users.find { it.username == username && it.password == password }

            if (user != null) {
                sharedPreferences.edit()
                    .putBoolean(IS_LOGGED_IN, true)
                    .putString(CURRENT_USERNAME, username)
                    .apply()
                navigateToMain()
            } else {
                showToast("Login failed")
            }
        }

        signUpLink.setOnClickListener {
            showSignUpForm()
        }

        signUpButton.setOnClickListener {
            val username = signUpUsernameEditText.text.toString()
            val password = signUpPasswordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val users = getUsers()
                if (users.any { it.username == username }) {
                    showToast("Username already exists")
                    return@setOnClickListener
                }

                val newUser = User(username, password, selectedModelID)
                users.add(newUser)
                saveUsers(users)

                sharedPreferences.edit()
                    .putBoolean(IS_LOGGED_IN, true)
                    .putString(CURRENT_USERNAME, username)
                    .apply()
                navigateToMain()
            } else {
                showToast("Sign-up failed")
            }
        }
    }

    private fun showLoginForm() {
        findViewById<EditText>(R.id.login_username).visibility = View.VISIBLE
        findViewById<EditText>(R.id.login_password).visibility = View.VISIBLE
        findViewById<Button>(R.id.login_button).visibility = View.VISIBLE
        findViewById<TextView>(R.id.signup_link).visibility = View.VISIBLE

        findViewById<EditText>(R.id.signup_username).visibility = View.GONE
        findViewById<EditText>(R.id.signup_password).visibility = View.GONE
        findViewById<Button>(R.id.sign_up_button).visibility = View.GONE
        findViewById<Spinner>(R.id.model_spinner).visibility = View.VISIBLE
    }

    private fun showSignUpForm() {
        findViewById<EditText>(R.id.login_username).visibility = View.GONE
        findViewById<EditText>(R.id.login_password).visibility = View.GONE
        findViewById<Button>(R.id.login_button).visibility = View.GONE
        findViewById<TextView>(R.id.signup_link).visibility = View.GONE

        findViewById<EditText>(R.id.signup_username).visibility = View.VISIBLE
        findViewById<EditText>(R.id.signup_password).visibility = View.VISIBLE
        findViewById<Button>(R.id.sign_up_button).visibility = View.VISIBLE
        findViewById<Spinner>(R.id.model_spinner).visibility = View.VISIBLE
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getUsers(): MutableList<User> {
        val usersJson = sharedPreferences.getString(usersKey, null)
        return if (usersJson != null) {
            val type = object : TypeToken<MutableList<User>>() {}.type
            Gson().fromJson(usersJson, type)
        } else {
            mutableListOf()
        }
    }
    
    private fun saveUsers(users: MutableList<User>) {
        val editor = sharedPreferences.edit()
        val usersJson = Gson().toJson(users)
        editor.putString(usersKey, usersJson)
        editor.apply()
    }

    data class User(val username: String, val password: String, val modelId: Int)

    companion object {
        const val IS_LOGGED_IN = "isLoggedIn"
        const val CURRENT_USERNAME = "current_username"
        const val CURRENT_MODEL = "1"
                  const val PASSWORD_KEY = "password"

          const val USERNAME_KEY = "username"
    }
}

