package br.com.utfpr.edu.strongnote.model

import android.os.Parcelable
import br.com.utfpr.edu.strongnote.util.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoutineModel(
    var id: String = "",
    var name: String = ""
) : Parcelable {
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}