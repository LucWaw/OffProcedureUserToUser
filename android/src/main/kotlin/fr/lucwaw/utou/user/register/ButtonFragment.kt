package fr.lucwaw.utou.user.register

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import fr.lucwaw.utou.databinding.FragmentButtonBinding
import fr.lucwaw.utou.databinding.RecyclerUsersBinding
import fr.lucwaw.utou.user.list.UsersViewModel
import kotlinx.coroutines.launch
import kotlin.getValue


@AndroidEntryPoint
class ButtonFragment : Fragment() {
    private var _binding: FragmentButtonBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentButtonBinding.inflate(inflater, container, false)
        setupRegisterButton()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.toastEvent.collect { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }



        return binding.root
    }

    private fun setupRegisterButton() {
        binding.button.setOnClickListener {

            val context = requireActivity()
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Title")


            val input = EditText(context)

            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)


            builder.setPositiveButton(
                "OK"
            ) { dialog, which ->
                viewModel.register(input.text.toString())
            }
            builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.cancel()
                }
            })
            builder.show()
        }
    }
}