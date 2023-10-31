package br.com.utfpr.edu.strongnote.model

import android.os.Parcelable
import br.com.utfpr.edu.strongnote.util.FirebaseHelper

import kotlinx.parcelize.Parcelize

@Parcelize
data class SetModel(
    var id: String = "",
    var set: String = "",
    var repetitions: String = "",
    var kilograms: String = "",
) : Parcelable {
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}
