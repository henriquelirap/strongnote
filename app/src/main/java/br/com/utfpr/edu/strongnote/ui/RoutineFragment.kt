package br.com.utfpr.edu.strongnote.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.utfpr.edu.strongnote.adapter.ExerciseAdapter
import br.com.utfpr.edu.strongnote.databinding.FragmentRoutineBinding
import br.com.utfpr.edu.strongnote.model.ExerciseModel
import br.com.utfpr.edu.strongnote.util.FirebaseHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RoutineFragment(private val routineId: String, private val tabSelected: Int) : Fragment() {
    private var _binding: FragmentRoutineBinding? = null
    private val binding get() = _binding!!
    private lateinit var exerciseAdapter: ExerciseAdapter
    private val exerciseList = mutableListOf<ExerciseModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getExercises()
        initRecyclerViewExercises()
    }

    private fun initRecyclerViewExercises() {
        exerciseAdapter = ExerciseAdapter(requireContext()) { exercise, position ->
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToExerciseAndSetFragment(
                    routineId, exercise.id, exercise, tabSelected
                )
            )
        }
        with(binding.rvExercises) {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = exerciseAdapter
        }
    }

    private fun getExercises() {
        FirebaseHelper.getDatabase()
            .child("exercises")
            .child(FirebaseHelper.getIdUser())
            .child(routineId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    exerciseList.clear()
                    if (snapshot.hasChildren()) {
                        for (firstNode in snapshot.children) {
                            val exercise =
                                firstNode.getValue(ExerciseModel::class.java) as ExerciseModel
                            exerciseList.add(exercise)
                        }
                    }
                    exerciseAdapter.submitList(exerciseList)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}