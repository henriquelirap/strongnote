package br.com.utfpr.edu.strongnote.model

import android.os.Parcelable
import br.com.utfpr.edu.strongnote.util.FirebaseHelper

import kotlinx.parcelize.Parcelize

@Parcelize
data class SetModel(
    var id: String = "",
    var set: String = "0",
    var repetitions: String = "0",
    var kilograms: String = "0",
) : Parcelable {
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}