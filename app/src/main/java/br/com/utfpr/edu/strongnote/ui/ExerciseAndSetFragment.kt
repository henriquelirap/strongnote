package br.com.utfpr.edu.strongnote.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getColor
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.utfpr.edu.strongnote.R
import br.com.utfpr.edu.strongnote.adapter.SetAdapter
import br.com.utfpr.edu.strongnote.databinding.FragmentExerciseAndSetBinding
import br.com.utfpr.edu.strongnote.model.ExerciseModel
import br.com.utfpr.edu.strongnote.model.MuscleEnum
import br.com.utfpr.edu.strongnote.model.SetModel
import br.com.utfpr.edu.strongnote.util.FirebaseHelper
import br.com.utfpr.edu.strongnote.util.initToolbar
import br.com.utfpr.edu.strongnote.util.showBottomSheet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ExerciseAndSetFragment : Fragment() {
    private var _binding: FragmentExerciseAndSetBinding? = null
    private val binding get() = _binding!!
    private lateinit var setAdapter: SetAdapter
    private var setList = mutableListOf<SetModel>()
    private val args: ExerciseAndSetFragmentArgs by navArgs()
    private lateinit var exerciseParcelable: ExerciseModel
    private var routineIdArgs: String = ""
    private var exerciseIdArgs: String = ""
    private var editing = false;
    private var exercise = ExerciseModel()

    private val muscleList = listOf(
        MuscleEnum.Peito,
        MuscleEnum.Costas,
        MuscleEnum.Ombros,
        MuscleEnum.Biceps,
        MuscleEnum.Triceps,
        MuscleEnum.Pernas,
        MuscleEnum.Panturrilhas,
        MuscleEnum.Antebraço
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseAndSetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgs()
        getSets()
        initToolbar(binding.toolbarTop)
        initSpinner()
        initRecyclerViewSets()
        initTimePicker()
        initListeners()
        addSet()
        removeExercise()
    }

    private fun getSets() {
        FirebaseHelper.getDatabase()
            .child("sets")
            .child(FirebaseHelper.getIdUser())
            .child(routineIdArgs)
            .child(
                if (editing) {
                    exerciseIdArgs
                } else {
                    exercise.id
                }
            ).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        for (firstNode in snapshot.children) {
                            val set =
                                firstNode.getValue(SetModel::class.java) as SetModel
                            setList.add(set)
                        }
                    }
                    setAdapter.submitList(setList)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun initListeners() {
        binding.btnSaveSet.setOnClickListener {
            validateData()
        }

        if (editing) {
            binding.editExerciseName.setText(exerciseParcelable.name)
            val spinnerPosition = muscleList.indexOf(exerciseParcelable.muscle)
            if (spinnerPosition != -1) {
                binding.spinnerMuscle.setSelection(spinnerPosition)
            }
            binding.minutePicker.value = exerciseParcelable.minutesRest
            binding.secondPicker.value = exerciseParcelable.secondsRest
        }
    }

    private fun validateData() {
        val exerciseName = binding.editExerciseName.text.toString().trim()
        if (exerciseName.isNotEmpty()) {
            exercise.name = exerciseName
            exercise.minutesRest = binding.minutePicker.value
            exercise.secondsRest = binding.secondPicker.value
            saveExerciseAndSets()
        } else {
            showBottomSheet(R.string.warning, R.string.exercise_invalid, false)
        }
    }

    private fun saveExerciseAndSets() {
        FirebaseHelper.getDatabase()
            .child("exercises")
            .child(FirebaseHelper.getIdUser())
            .child(routineIdArgs)
            .child(
                if (editing) {
                    exercise.id = exerciseParcelable.id
                    exerciseIdArgs
                } else {
                    exercise.id
                }
            )
            .setValue(exercise)

        val reference = FirebaseHelper.getDatabase()
            .child("sets")
            .child(FirebaseHelper.getIdUser())
            .child(routineIdArgs)
            .child(
                if (editing) {
                    exercise.id = exerciseParcelable.id
                    exerciseIdArgs
                } else {
                    exercise.id
                }
            )

        if (editing) {
            reference.removeValue()
        }

        for (set in setList) {
            reference.child(set.id).setValue(set)
        }
        findNavController().popBackStack()
    }

    private fun initRecyclerViewSets() {
        setAdapter = SetAdapter(requireContext(), setList) { set, position, action ->
            setList[position] = set

            if ("DELETE".equals(action)) {
                showBottomSheet(R.string.warning, R.string.ask_delete_set, true, onConfirmClick = {
                    deleteSet(set)
                })
            }

        }
        with(binding.rvEditSets) {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = setAdapter
        }
    }

    private fun deleteSet(set: SetModel) {
        FirebaseHelper.getDatabase()
            .child("sets")
            .child(FirebaseHelper.getIdUser())
            .child(routineIdArgs)
            .child(
                if (editing) {
                    exercise.id = exerciseParcelable.id
                    exerciseIdArgs
                } else {
                    exercise.id
                }
            )
            .child(set.id)
            .removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val removedIndex = setList.indexOfFirst { it.id == set.id }
                    if (removedIndex != -1) {
                        setList.removeAt(removedIndex)
                        setAdapter.notifyItemRemoved(removedIndex)
                    }
                }
            }
    }

    private fun addSet() {
        binding.fabNewSet.setOnClickListener {
            val newPosition = setList.size
            setList.add(SetModel())
            setAdapter.submitList(setList)
            setAdapter.notifyItemInserted(newPosition)
        }
    }

    private fun removeExercise() {
        binding.fabDeleteExercise.setOnClickListener {
            showBottomSheet(R.string.warning, R.string.ask_delete_exercise, true, onConfirmClick = {
                FirebaseHelper.getDatabase()
                    .child("exercises")
                    .child(FirebaseHelper.getIdUser())
                    .child(routineIdArgs)
                    .child(
                        if (editing) {
                            exerciseIdArgs
                        } else {
                            exercise.id
                        }
                    )
                    .removeValue().addOnCompleteListener {
                        if (it.isSuccessful) {
                            findNavController().popBackStack()
                        }
                    }
            })
        }
    }

    private fun getArgs() {
        args.routineId.let {
            if (it != null) {
                this.routineIdArgs = it
            }
        }
        args.exerciseId.let {
            if (it != null) {
                this.exerciseIdArgs = it
                editing = true;
            } else {
                editing = false
            }
        }
        args.exerciseParcelable.let {
            if (it != null) {
                this.exerciseParcelable = it
            }
        }
    }

    private fun initSpinner() {
        binding.buttonSpinnerIcon.setOnClickListener {
            binding.spinnerMuscle.performClick()
        }

        binding.spinnerMuscle.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_layout, R.id.textMuscle, muscleList)
        binding.spinnerMuscle.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                exercise.muscle = muscleList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initTimePicker() {
        binding.minutePicker.minValue = 0
        binding.minutePicker.maxValue = 59
        binding.minutePicker.setTextColor(getColor(requireContext(), R.color.main_gray))

        binding.minutePicker.setOnScrollListener { view, scrollState ->
            exercise.minutesRest = binding.minutePicker.value
        }

        binding.secondPicker.minValue = 0
        binding.secondPicker.maxValue = 59
        binding.secondPicker.setTextColor(getColor(requireContext(), R.color.main_gray))

        binding.secondPicker.setOnScrollListener { view, scrollState ->
            exercise.secondsRest = binding.secondPicker.value
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}