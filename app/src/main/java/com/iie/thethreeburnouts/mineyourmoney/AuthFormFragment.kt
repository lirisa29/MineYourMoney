package com.iie.thethreeburnouts.mineyourmoney

import User
import UserDao
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentAuthFormBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthFormFragment : Fragment() {

    interface AuthListener {
        fun onAuthSuccess()
    }

    private var listener: AuthListener? = null
    private var isLogin = true
    private var _binding: FragmentAuthFormBinding? = null
    private val binding get() = _binding!!

    private val userDao: UserDao by lazy {
        AppDatabase.getInstance(requireContext()).userDao()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AuthListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isLogin = arguments?.getBoolean("IS_LOGIN", true) ?: true

        binding.topAppBar.title = if (isLogin) "Login" else "Sign Up"

        setupUI(binding.root)

        if (isLogin) {
            binding.confirmPasswordInputLayout.visibility = View.GONE
            binding.tvConfirmPasswordLabel.visibility = View.GONE
        } else {
            binding.confirmPasswordInputLayout.visibility = View.VISIBLE
            binding.tvConfirmPasswordLabel.visibility = View.VISIBLE
        }

        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnNext.setOnClickListener {
            clearErrors()

            val username = binding.etInputUsername.text?.toString()?.trim().orEmpty()
            val password = binding.etInputPassword.text?.toString().orEmpty()
            val confirmPassword = binding.etInputConfirmPassword.text?.toString().orEmpty()

            if (!validateInputs(username, password, confirmPassword)) return@setOnClickListener

            lifecycleScope.launch {
                if (isLogin) {
                    performLogin(username, password)
                } else {
                    performSignup(username, password)
                }
            }
        }
    }

    private fun clearErrors() {
        binding.usernameInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.confirmPasswordInputLayout.error = null
    }

    private fun validateInputs(username: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) {
            binding.usernameInputLayout.error = "Enter username"
            return false
        }
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Enter password"
            return false
        }
        if (!isLogin) {
            if (!isValidUsername(username)) {
                binding.usernameInputLayout.error = "Username must be 3-20 characters and only contain letters, digits, or _"
                return false
            }
            if (!isValidPassword(password)) {
                binding.passwordInputLayout.error = "Password must be at least 8 characters and include uppercase, lowercase, and a digit"
                return false
            }
            if (confirmPassword.isEmpty()) {
                binding.confirmPasswordInputLayout.error = "Confirm your password"
                return false
            }
            if (password != confirmPassword) {
                binding.confirmPasswordInputLayout.error = "Passwords do not match"
                return false
            }
        }
        return true
    }

    private suspend fun performSignup(username: String, password: String) {
        val existingUser = withContext(Dispatchers.IO) {
            userDao.findByUsername(username)
        }
        if (existingUser != null) {
            withContext(Dispatchers.Main) {
                binding.usernameInputLayout.error = "Username already exists"
            }
            return
        }

        // Hash password before saving
        val hashedPassword = hashPassword(password)

        val newUser = User(username = username, password = hashedPassword)

        withContext(Dispatchers.IO) {
            userDao.insertUser(newUser)
        }

        withContext(Dispatchers.Main) {
            listener?.onAuthSuccess()
        }
    }

    private suspend fun performLogin(username: String, password: String) {
        val user = withContext(Dispatchers.IO) {
            userDao.findByUsername(username)
        }
        if (user == null) {
            withContext(Dispatchers.Main) {
                binding.usernameInputLayout.error = "User not found"
            }
            return
        }

        val verified = verifyPassword(password, user.password)
        if (!verified) {
            withContext(Dispatchers.Main) {
                binding.passwordInputLayout.error = "Incorrect password"
            }
            return
        }

        withContext(Dispatchers.Main) {
            listener?.onAuthSuccess()
        }
    }

    private fun isValidUsername(username: String): Boolean {
        val usernameRegex = "^[A-Za-z0-9_]{3,20}$".toRegex()
        return usernameRegex.matches(username)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
        return passwordRegex.matches(password)
    }

    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    private fun verifyPassword(password: String, hashedPassword: String): Boolean {
        val result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword)
        return result.verified
    }

    private fun setupUI(view: View) {
        // Set up touch listener for non-text box views to hide keyboard.
        @Suppress("ClickableViewAccessibility")
        if (view !is android.widget.EditText) {
            view.setOnTouchListener { _, _ ->
                hideKeyboard()
                view.clearFocus()
                false
            }
        }

        // If a layout container, iterate over children and set up touch listener recursively.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
