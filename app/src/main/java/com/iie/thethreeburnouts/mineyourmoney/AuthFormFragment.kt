package com.iie.thethreeburnouts.mineyourmoney

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.widget.AppCompatButton

class AuthFormFragment : Fragment(R.layout.fragment_auth_form) {

    interface AuthListener {
        fun onAuthSuccess()
    }

    private var listener: AuthListener? = null
    private var isLogin = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AuthListener) {
            listener = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isLogin = arguments?.getBoolean("IS_LOGIN", true) ?: true

        val topBar = view.findViewById<MaterialToolbar>(R.id.top_app_bar)
        val usernameInput = view.findViewById<TextInputEditText>(R.id.et_input_username)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.et_input_password)
        val confirmPasswordInput = view.findViewById<TextInputEditText>(R.id.et_input_confirm_password)
        val confirmPasswordLayout = view.findViewById<TextInputLayout>(R.id.confirm_password_input_layout)
        val confirmPasswordLabel = view.findViewById<View>(R.id.tv_confirm_password_label)
        val btnNext = view.findViewById<AppCompatButton>(R.id.btn_next)

        // Hide confirm password if login
        if (isLogin) {
            confirmPasswordLayout?.visibility = View.GONE
            confirmPasswordLabel?.visibility = View.GONE
        } else {
            confirmPasswordLayout?.visibility = View.VISIBLE
            confirmPasswordLabel?.visibility = View.VISIBLE
        }

        topBar?.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnNext?.setOnClickListener {
            val username = usernameInput?.text?.toString()?.trim().orEmpty()
            val password = passwordInput?.text?.toString()?.trim().orEmpty()
            val confirmPassword = confirmPasswordInput?.text?.toString()?.trim().orEmpty()

            if (username.isEmpty()) {
                usernameInput?.error = "Enter username"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput?.error = "Enter password"
                return@setOnClickListener
            }

            if (!isLogin) {
                if (confirmPassword.isEmpty()) {
                    confirmPasswordInput?.error = "Confirm your password"
                    return@setOnClickListener
                }
                if (password != confirmPassword) {
                    confirmPasswordInput?.error = "Passwords do not match"
                    return@setOnClickListener
                }
            }

            // ✅ Success
            listener?.onAuthSuccess()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
