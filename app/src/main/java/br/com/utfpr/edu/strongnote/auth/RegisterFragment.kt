package br.com.utfpr.edu.strongnote.auth

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import br.com.utfpr.edu.strongnote.R
import br.com.utfpr.edu.strongnote.databinding.FragmentRegisterBinding
import br.com.utfpr.edu.strongnote.util.FirebaseHelper
import br.com.utfpr.edu.strongnote.util.initToolbar
import br.com.utfpr.edu.strongnote.util.showBottomSheet

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbarTop)
        initListeners()
    }

    private fun initListeners() {
        binding.btnCreateAccount.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (email.isNotEmpty()) {
            if (password.isNotEmpty()) {
                binding.progressBar.isVisible = true
                createUserAccount(email, password)
            } else {
                showBottomSheet(R.string.warning, R.string.password_invalid, false)
            }
        } else {
            showBottomSheet(R.string.warning, R.string.email_invalid, false)
        }
    }

    private fun createUserAccount(email: String, password: String) {
        binding.progressBar.isVisible = true
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener() {
                if (it.isSuccessful) {
                    hideKeyboard()
                    showBottomSheet(
                        R.string.sucess,
                        R.string.account_register_sucess,
                        false,
                        onConfirmClick = {
                            findNavController().popBackStack()
                        })
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

    override fun onResume() {
        super.onResume()
        clearRegisterFields()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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


    private fun clearRegisterFields() {
        binding.editEmail.text.clear()
        binding.editPassword.text.clear()
    }
}