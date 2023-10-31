package br.com.utfpr.edu.strongnote.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import br.com.utfpr.edu.strongnote.R
import br.com.utfpr.edu.strongnote.adapter.ViewPageAdapter
import br.com.utfpr.edu.strongnote.databinding.FragmentMainBinding
import br.com.utfpr.edu.strongnote.model.RoutineModel
import br.com.utfpr.edu.strongnote.util.FirebaseHelper
import br.com.utfpr.edu.strongnote.util.showBottomSheet
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val routineList = mutableListOf<RoutineModel>()
    private lateinit var newRoutine: RoutineModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showWelcome()
        initListeners()
    }

    private fun showWelcome() {
        val sharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val messageShown = sharedPreferences.getBoolean("messageShown", false)

        if (!messageShown) {
            showBottomSheet(R.string.welcome, R.string.first_routine_tip, false)
            with(sharedPreferences.edit()) {
                putBoolean("messageShown", true)
                apply()
            }
        }
    }

    private fun initListeners() {
        getRoutines()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (findNavController().currentDestination?.id == R.id.mainFragment) {
                minimizeApp()
            }
        }
        logoutUser()
        newRoutineDialogEvents()
    }

    private fun logoutUser() {
        binding.btnLogout.setOnClickListener {
            showBottomSheet(R.string.warning, R.string.logout, true, onConfirmClick = {
                FirebaseHelper.getAuth().signOut()
                findNavController().navigate(R.id.action_mainFragment_to_autenticationNest)
            })
        }
    }

    private fun getRoutines() {
        FirebaseHelper.getDatabase()
            .child("routines")
            .child(FirebaseHelper.getIdUser())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        routineList.clear()
                        binding.fabNewExercise.isVisible = true
                        binding.fabDeleteRoutine.isVisible = true
                        for (ds in snapshot.children) {
                            val routine = ds.getValue(RoutineModel::class.java) as RoutineModel
                            routineList.add(routine)
                        }
                        val pageAdapter = ViewPageAdapter(requireActivity())
                        binding.viewPagerRoutine.adapter = pageAdapter
                        var tabSelected = binding.tabsRoutines.selectedTabPosition

                        routineList.forEach { rotina ->
                            pageAdapter.addFragment(
                                RoutineFragment(
                                    rotina.id
                                ), rotina.name
                            )
                        }

                        binding.viewPagerRoutine.offscreenPageLimit = pageAdapter.itemCount
                        TabLayoutMediator(
                            binding.tabsRoutines,
                            binding.viewPagerRoutine
                        ) { tab, position ->
                            tab.text = pageAdapter.getTitle(position)
                        }.attach()
                        binding.tabsRoutines.getTabAt(tabSelected)?.select()
                    } else {
                        binding.fabNewExercise.isVisible = false
                        binding.fabDeleteRoutine.isVisible = false
                        binding.tabsRoutines.removeAllTabs()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun validateData() {
        val routineName = binding.editNewRoutineDialog.text.toString().trim()
        if (routineName.isNotEmpty()) {
            newRoutine = RoutineModel()
            newRoutine.name = routineName
            createNewRoutine()
        } else {
            showBottomSheet(R.string.warning, R.string.invalid_routine, false)
        }
    }

    private fun createNewRoutine() {
        FirebaseHelper.getDatabase()
            .child("routines")
            .child(FirebaseHelper.getIdUser())
            .child(newRoutine.id)
            .setValue(newRoutine)
    }

    private fun newRoutineDialogEvents() {
        binding.fabNewExercise.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToExerciseAndSetFragment(
                getSelectedRoutineTab().id,
                null,
                null
            )
            findNavController().navigate(action)
        }

        binding.fabDeleteRoutine.setOnClickListener {
            showBottomSheet(R.string.warning, R.string.ask_delete_routine, true, onConfirmClick = {
                deleteRoutine(getSelectedRoutineTab())
            })
        }

        binding.fabNewRoutine.setOnClickListener {
            binding.newRoutineDialog.isVisible = true
            hideKeyboardClearField()
        }

        binding.btnCancelNewRoutineDialog.setOnClickListener {
            binding.newRoutineDialog.isVisible = false
            hideKeyboardClearField()
        }

        binding.btnOkNewRoutineDialog.setOnClickListener {
            binding.newRoutineDialog.isVisible = false
            validateData()
            hideKeyboardClearField()
        }
    }

    private fun deleteRoutine(routine: RoutineModel) {
        FirebaseHelper.getDatabase()
            .child("routines")
            .child(FirebaseHelper.getIdUser())
            .child(routine.id)
            .removeValue()

        FirebaseHelper.getDatabase()
            .child("exercises")
            .child(FirebaseHelper.getIdUser())
            .child(routine.id)
            .removeValue()

        FirebaseHelper.getDatabase()
            .child("sets")
            .child(FirebaseHelper.getIdUser())
            .child(routine.id)
            .removeValue()
    }

    private fun getSelectedRoutineTab(): RoutineModel {
        return routineList.get(binding.tabsRoutines.selectedTabPosition)
    }

    private fun hideKeyboardClearField() {
        hideKeyboard()
        binding.editNewRoutineDialog.text.clear()
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

    private fun minimizeApp() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }

}