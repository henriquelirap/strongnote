package br.com.utfpr.edu.strongnote.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import br.com.utfpr.edu.strongnote.R
import br.com.utfpr.edu.strongnote.databinding.FragmentWelcomeBinding
import br.com.utfpr.edu.strongnote.util.FirebaseHelper

class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed(this::showStart, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showStart() {
        fadeIn()
        binding.btnStart.isVisible = true
        binding.btnStart.setOnClickListener {
            if (FirebaseHelper.isAutenticated()) {
                findNavController().navigate(
                    WelcomeFragmentDirections.actionWelcomeFragmentToMainFragment(
                        -1
                    )
                )
            } else {
                findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
            }
        }
    }

    private fun fadeIn() {
        val alphaAnimator = ObjectAnimator.ofFloat(binding.btnStart, "alpha", 0f, 1f)
        alphaAnimator.duration = 100
        alphaAnimator.start()
    }
}