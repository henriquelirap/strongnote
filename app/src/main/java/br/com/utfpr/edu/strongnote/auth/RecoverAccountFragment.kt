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
import br.com.utfpr.edu.strongnote.databinding.FragmentRecoverAccountBinding
import br.com.utfpr.edu.strongnote.util.FirebaseHelper
import br.com.utfpr.edu.strongnote.util.initToolbar
import br.com.utfpr.edu.strongnote.util.showBottomSheet

class RecoverAccountFragment : Fragment() {
    private var _binding: FragmentRecoverAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbarTop)
        initListeners()
    }

    private fun initListeners() {
        binding.btnSend.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val email = binding.editEmail.text.toString().trim()
        if (email.isNotEmpty()) {
            recoverUser(email)
        } else {
            showBottomSheet(R.string.warning, R.string.email_invalid, false)
        }
    }

    private fun recoverUser(email: String) {
        binding.progressBar.isVisible = true
        FirebaseHelper.getAuth().sendPasswordResetEmail(email)
            .addOnCompleteListener() { it ->
                if (it.isSuccessful) {
                    hideKeyboard()
                    showBottomSheet(
                        R.string.sucess,
                        R.string.recover_account_sucess,
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
        clearRecoverFields()
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

    private fun clearRecoverFields() {
        binding.editEmail.text.clear()
    }
}