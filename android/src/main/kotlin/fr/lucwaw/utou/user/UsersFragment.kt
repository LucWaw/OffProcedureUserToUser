package fr.lucwaw.utou.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.lucwaw.utou.databinding.RecyclerUsersBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsersFragment : Fragment(), UserAdapter.OnUserClickListener {
    private var _binding: RecyclerUsersBinding? = null
    private val binding get() = _binding!!



    private val viewModel: UsersViewModel by viewModels()
    private lateinit var candidateAdapter: UserAdapter

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerUsersBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        viewModel.loadAllUsers()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.usersFlow.collect { users ->
                if (users.isEmpty()) {
                    binding.noData.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                } else {
                    binding.noData.visibility = View.GONE
                    binding.loading.visibility = View.GONE
                }
            }
        }
        context?.let { super.onAttach(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loading.visibility = View.VISIBLE
        setupRecyclerView()
        observeUsers()


    }


    private fun observeUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.usersFlow.collect { users ->
                candidateAdapter.submitList(users)
                if (users.isEmpty()) {
                    binding.noData.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                } else {
                    binding.noData.visibility = View.GONE
                    binding.loading.visibility = View.GONE
                }
            }
        }
    }

    private fun setupRecyclerView() {
        candidateAdapter = UserAdapter(this)
        binding.userRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.userRecyclerview.adapter = candidateAdapter
    }

    override fun onUserClick(user: User) {
    }

}
