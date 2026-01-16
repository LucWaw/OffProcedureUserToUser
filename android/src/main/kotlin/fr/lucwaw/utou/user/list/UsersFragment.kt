package fr.lucwaw.utou.user.list

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.lucwaw.utou.databinding.RecyclerUsersBinding
import fr.lucwaw.utou.user.GrpcUser
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
        askNotificationPermission()
        observeSendingPing()
        return binding.root
    }

    fun observeSendingPing(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.toastEvent.collect { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _: Boolean ->
        /*if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
        }*/
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                showNotificationPermissionRationale(requireActivity()) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun showNotificationPermissionRationale(activity: Activity, onGranted: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Activer les notifications")
            .setMessage(
                "Pour que vous puissiez recevoir facilement les pings " +
                        "d’autres utilisateurs, nous avons besoin d’autoriser les notifications. " +
                        "Vous pourrez toujours les désactiver plus tard dans les paramètres."
            )
            .setPositiveButton("OK") { _, _ ->
                onGranted()
            }
            .setNegativeButton("Non merci") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loading.visibility = View.VISIBLE
        setupRecyclerView()
        observeUsers()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refreshing.collect {
                    binding.swipeRefresh.isRefreshing = it
                }
            }
        }


        // premier chargement
        viewModel.refresh()


    }


    private fun observeUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.usersFlow.collect { users ->
                    candidateAdapter.submitList(users)
                    binding.swipeRefresh.isRefreshing = false

                    binding.loading.visibility = View.GONE
                    binding.noData.visibility =
                        if (users.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }


    private fun setupRecyclerView() {
        candidateAdapter = UserAdapter(this)
        binding.userRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.userRecyclerview.adapter = candidateAdapter
    }

    override fun onUserClick(user: GrpcUser) {
        viewModel.sendPing(user.userId)
    }

}
