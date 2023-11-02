package br.com.utfpr.edu.strongnote.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import br.com.utfpr.edu.strongnote.R
import br.com.utfpr.edu.strongnote.databinding.FragmentLoginBinding
import br.com.utfpr.edu.strongnote.util.FirebaseHelper
import br.com.utfpr.edu.strongnote.util.showBottomSheet

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (findNavController().currentDestination?.id == R.id.loginFragment) {
                minimizeApp()
            }
        }

        binding.btnLogin.setOnClickListener {
            validateData()
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnRecovery.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recoverAccountFragment)
        }
    }

    private fun validateData() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (email.isNotEmpty()) {
            if (password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                showBottomSheet(R.string.warning, R.string.password_invalid, false)
            }
        } else {
            showBottomSheet(R.string.warning, R.string.email_invalid, false)
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.progressBar.isVisible = true
        FirebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener() {
                if (it.isSuccessful) {
                    hideKeyboard()
                    clearLoginFields()
                    findNavController().navigate(LoginFragmentDirections.actionGlobalMainFragment(0))
                } else {
                    showBottomSheet(
                        R.string.warning,
                        FirebaseHelper.validError(it.exception?.message.toString()),
                        false
                    )
                }
            }
        binding.progressBar.isVisible = false
    }

    private fun Fragment.hideKeyboard() {
        view?.let {
            activity?.hideKeyboard(it)
        }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun clearLoginFields() {
        binding.editEmail.text.clear()
        binding.editPassword.text.clear()
    }

    private fun minimizeApp() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        clearLoginFields()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}