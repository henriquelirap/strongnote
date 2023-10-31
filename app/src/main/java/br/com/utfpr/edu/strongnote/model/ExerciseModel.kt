package br.com.utfpr.edu.strongnote.model

import android.os.Parcelable
import br.com.utfpr.edu.strongnote.util.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExerciseModel(
    var id: String = "",
    var muscle: MuscleEnum = MuscleEnum.Peito,
    var name: String = "",
    var minutesRest: Int = 0,
    var secondsRest: Int = 0
) : Parcelable {
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}
