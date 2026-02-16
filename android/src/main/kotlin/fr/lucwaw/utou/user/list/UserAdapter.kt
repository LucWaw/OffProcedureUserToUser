package fr.lucwaw.utou.user.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.lucwaw.utou.databinding.ItemUserBinding
import fr.lucwaw.utou.domain.modele.SyncStatus
import fr.lucwaw.utou.domain.modele.User

class UserAdapter(private val listener: OnUserClickListener) :
    ListAdapter<User, UserAdapter.UserViewHolder>(
        DIFF_CALLBACK
    ) {

    class UserViewHolder(binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        var name: TextView = binding.name
        var statusIcon: ImageView = binding.syncStatusIcon


        fun bind(user: User, listener: OnUserClickListener) {
            itemView.setOnClickListener {
                listener.onUserClick(user)
            }
        }
    }

    interface OnUserClickListener {

        fun onUserClick(user: User)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")//No need translation
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)

        holder.name.text = user.name
        val iconRes = when (user.syncStatus) {
            SyncStatus.SYNCED -> android.R.drawable.presence_online
            SyncStatus.ERROR -> android.R.drawable.presence_busy
            SyncStatus.PENDING_UPLOAD -> android.R.drawable.presence_invisible
        }

        holder.statusIcon.setImageResource(iconRes)

        holder.bind(user, listener)

    }


    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<User> =
            object : DiffUtil.ItemCallback<User>() {
                override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                    return oldItem === newItem
                }

                override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
