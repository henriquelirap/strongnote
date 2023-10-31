package br.com.utfpr.edu.strongnote.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.utfpr.edu.strongnote.databinding.ItemExerciseBinding
import br.com.utfpr.edu.strongnote.model.ExerciseModel

class ExerciseAdapter(
    private val context: Context,
    private val exerciseSelected: (ExerciseModel, Int) -> Unit
) : ListAdapter<ExerciseModel, ExerciseAdapter.MyViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ExerciseModel>() {
            override fun areItemsTheSame(
                oldItem: ExerciseModel,
                newItem: ExerciseModel
            ): Boolean {
                return oldItem.id == newItem.id && oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: ExerciseModel,
                newItem: ExerciseModel
            ): Boolean {
                return oldItem == newItem && oldItem.name == newItem.name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemExerciseBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val exercise = getItem(position)
        holder.binding.exerciseName.text = exercise.name
        holder.binding.muscleName.text = exercise.muscle.name
        holder.binding.rest.text =
            "Intervalo: ${exercise.minutesRest} min. ${exercise.secondsRest} seg."

        holder.binding.btnEditSet.setOnClickListener {
            exerciseSelected(exercise, position)
        }
    }

    inner class MyViewHolder(val binding: ItemExerciseBinding) :
        RecyclerView.ViewHolder(binding.root)

}