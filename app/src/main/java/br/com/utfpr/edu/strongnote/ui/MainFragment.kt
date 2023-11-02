package br.com.utfpr.edu.strongnote.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
    private val args: MainFragmentArgs by navArgs()
    private var editing = false;
    private var createNewRoutine = false;
    private var tabSelected = 0;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBtnInvisible(false)
        initListeners()
    }

    private fun setBtnInvisible(visible: Boolean) {
        binding.btnNewExercise.isVisible = visible
        binding.btnDeleteRoutine.isVisible = visible
        binding.btnEditRoutine.isVisible = visible
    }

    private fun initListeners() {
        getArgs()
        getRoutines()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (findNavController().currentDestination?.id == R.id.mainFragment) {
                minimizeApp()
            }
        }
        logoutUser()
        newRoutineDialogEvents()
    }

    private fun getArgs() {
        args.tabSelected.let {
            this.tabSelected = it
        }
    }

    private fun logoutUser() {
        binding.btnLogout.setOnClickListener {
            showBottomSheet(R.string.warning, R.string.logout, true, onConfirmClick = {
                FirebaseHelper.getAuth().signOut()
                findNavController().navigate(R.id.action_mainFragment_to_autenticationNest)
            })
        }
    }

    private fun tabRoutineNavigate(tab: Int) {
        binding.viewPagerRoutine.currentItem = tab
    }

    private fun getRoutines() {
        FirebaseHelper.getDatabase()
            .child("routines")
            .child(FirebaseHelper.getIdUser())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        routineList.clear()
                        setBtnInvisible(true)
                        for (ds in snapshot.children) {
                            val routine = ds.getValue(RoutineModel::class.java) as RoutineModel
                            routineList.add(routine)
                        }
                        val pageAdapter = ViewPageAdapter(requireActivity())
                        binding.viewPagerRoutine.adapter = pageAdapter

                        if (createNewRoutine) {
                            tabSelected = binding.tabsRoutines.selectedTabPosition
                        }

                        var page = 0;
                        routineList.forEach { rotina ->
                            pageAdapter.addFragment(
                                RoutineFragment(
                                    rotina.id,
                                    page
                                ), rotina.name
                            )
                            page++
                        }

                        binding.viewPagerRoutine.offscreenPageLimit = pageAdapter.itemCount
                        TabLayoutMediator(
                            binding.tabsRoutines,
                            binding.viewPagerRoutine
                        ) { tab, position ->
                            tab.text = pageAdapter.getTitle(position)
                        }.attach()
                        tabRoutineNavigate(tabSelected)
                    } else {
                        setBtnInvisible(false)
                        binding.tabsRoutines.removeAllTabs()
                    }
                    createNewRoutine = false
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun validateData(routine: RoutineModel?) {
        val routineName = binding.editNewRoutineDialog.text.toString().trim()

        if (routineName.isEmpty()) {
            showBottomSheet(R.string.warning, R.string.invalid_routine, false)
        }
        newRoutine = RoutineModel()

        if (routine == null) {
            createNewRoutine = true
            newRoutine.name = routineName
        } else {
            createNewRoutine = false
            newRoutine.id = routine.id
            newRoutine.name = routineName
        }
        createOrEditNewRoutine()
    }

    private fun createOrEditNewRoutine() {
        FirebaseHelper.getDatabase()
            .child("routines")
            .child(FirebaseHelper.getIdUser())
            .child(newRoutine.id)
            .setValue(newRoutine)
        editing = false;
        tabSelected = binding.viewPagerRoutine.currentItem
    }

    private fun newRoutineDialogEvents() {
        binding.btnNewExercise.setOnClickListener {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToExerciseAndSetFragment(
                    getSelectedRoutineTab().id,
                    null,
                    null,
                    binding.viewPagerRoutine.currentItem
                )
            )
        }

        binding.btnDeleteRoutine.setOnClickListener {
            showBottomSheet(R.string.warning, R.string.ask_delete_routine, true, onConfirmClick = {
                deleteRoutine(getSelectedRoutineTab())
            })
        }

        binding.btnNewRoutine.setOnClickListener {
            binding.newRoutineDialog.isVisible = true
            binding.titleDialog.text = getText(R.string.new_routine)
        }

        binding.btnCancelNewRoutineDialog.setOnClickListener {
            binding.newRoutineDialog.isVisible = false
            hideKeyboardClearField()
        }

        binding.btnEditRoutine.setOnClickListener {
            binding.newRoutineDialog.isVisible = true
            binding.titleDialog.text = getText(R.string.edit_routine)
            binding.editNewRoutineDialog.setText(getSelectedRoutineTab().name)
            editing = true
        }

        binding.btnOkNewRoutineDialog.setOnClickListener {
            binding.newRoutineDialog.isVisible = false
            if (editing) {
                validateData(getSelectedRoutineTab())
            } else {
                validateData(null)
            }
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
        tabSelected = 0
    }

    private fun getSelectedRoutineTab(): RoutineModel {
        return routineList[binding.tabsRoutines.selectedTabPosition]
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