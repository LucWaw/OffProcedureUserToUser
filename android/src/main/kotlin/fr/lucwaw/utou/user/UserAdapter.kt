package fr.lucwaw.utou.user

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.lucwaw.utou.databinding.ItemUserBinding

class UserAdapter( private val listener: OnUserClickListener) :
    ListAdapter<User, UserAdapter.UserViewHolder>(
        DIFF_CALLBACK
    ) {

    inner class UserViewHolder(binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        var name:TextView = binding.name


        fun bind(user: User, listener: OnUserClickListener) {
            itemView.setOnClickListener {
                listener.onUserClick(user)
            }
        }
    }

    interface OnUserClickListener
    {

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

        holder.name.text = user.displayName

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
