package br.com.utfpr.edu.strongnote.util

import android.app.Dialog
import android.renderscript.ScriptGroup.Binding
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import br.com.utfpr.edu.strongnote.R
import br.com.utfpr.edu.strongnote.databinding.BottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

fun Fragment.initToolbar(toolbar: Toolbar) {
    (activity as AppCompatActivity).setSupportActionBar(toolbar)
    (activity as AppCompatActivity).title = ""
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
}

fun Fragment.showBottomSheet(
    titleDialog: Int? = null,
    message: Int,
    cancel: Boolean,
    onConfirmClick: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
    val binding: BottomSheetBinding = BottomSheetBinding.inflate(layoutInflater, null, false)
    binding.txtTitleDialog.text = getText(titleDialog ?: R.string.app_name)
    binding.txtMessageDialog.text = getText(message ?: R.string.app_name)
    binding.btnCancel.isVisible = cancel
    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.show()

    binding.btnOK.setOnClickListener {
        onConfirmClick()
        bottomSheetDialog.dismiss()
    }

    binding.btnCancel.setOnClickListener {
        onCancelClick()
        bottomSheetDialog.dismiss()
    }
}