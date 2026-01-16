package fr.lucwaw.utou.user.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.lucwaw.utou.databinding.ItemUserBinding
import fr.lucwaw.utou.user.GrpcUser

class UserAdapter(private val listener: OnUserClickListener) :
    ListAdapter<GrpcUser, UserAdapter.UserViewHolder>(
        DIFF_CALLBACK
    ) {

    class UserViewHolder(binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        var name: TextView = binding.name


        fun bind(user: GrpcUser, listener: OnUserClickListener) {
            itemView.setOnClickListener {
                listener.onUserClick(user)
            }
        }
    }

    interface OnUserClickListener {

        fun onUserClick(user: GrpcUser)

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
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<GrpcUser> =
            object : DiffUtil.ItemCallback<GrpcUser>() {
                override fun areItemsTheSame(oldItem: GrpcUser, newItem: GrpcUser): Boolean {
                    return oldItem === newItem
                }

                override fun areContentsTheSame(oldItem: GrpcUser, newItem: GrpcUser): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
