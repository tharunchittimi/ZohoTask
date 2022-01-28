package com.example.zohotask.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zohotask.R
import com.example.zohotask.databinding.InflateUsersListRvItemBinding
import com.example.zohotask.room.entity.UserDetailsEntity

class UsersListAdapter : RecyclerView.Adapter<UsersListAdapter.UserDetailsViewHolder>() {

    private var usersList: ArrayList<UserDetailsEntity?> = ArrayList()
    private var mainList: ArrayList<UserDetailsEntity?> = arrayListOf()
    private var communicator: Communicator? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserDetailsViewHolder {
        val inflateUsersListRvItemBinding: InflateUsersListRvItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.inflate_users_list_rv_item, parent, false
        )
        return UserDetailsViewHolder(inflateUsersListRvItemBinding)
    }

    override fun onBindViewHolder(holder: UserDetailsViewHolder, position: Int) {
        holder.onBind(usersList[holder.adapterPosition])
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    inner class UserDetailsViewHolder(private val inflateUsersListRvItemBinding: InflateUsersListRvItemBinding) :
        RecyclerView.ViewHolder(inflateUsersListRvItemBinding.root) {
        fun onBind(userDetailsResponse: UserDetailsEntity?) {
            inflateUsersListRvItemBinding.userProfilePic.let {
                Glide.with(it.context).load(userDetailsResponse?.profilePic)
                    .placeholder(R.drawable.no_img)
                    .error(R.drawable.no_img)
                    .into(it)
            }
            inflateUsersListRvItemBinding.tvUserName.text =
                "${userDetailsResponse?.firstName} ${userDetailsResponse?.lastName}"
            inflateUsersListRvItemBinding.tvUserPhoneNumber.text = userDetailsResponse?.phoneNumber
            inflateUsersListRvItemBinding.rootLayoutUser.setOnClickListener {
                communicator?.onItemClick(userDetailsResponse)
            }
        }
    }

    fun addUserList(usersList: ArrayList<UserDetailsEntity?>) {
        clearList()
        addData(usersList)
        this.mainList.addAll(usersList)
        notifyDataSetChanged()
    }

    private fun addData(list: ArrayList<UserDetailsEntity?>) {
        usersList.clear()
        for (myItem in list) {
            usersList.add(myItem)
        }
        notifyDataSetChanged()
    }

    fun showSearchedList(newText: String?) {
        val searchedList: ArrayList<UserDetailsEntity?> = ArrayList()
        if (newText?.isEmpty() == true) {
            searchedList.addAll(mainList)
            communicator?.showNoDataMessage(mainList.size <= 0)
        } else {
            val searchedName = newText.toString().trim()
            searchedList.clear()
            for (item in mainList) {
                item?.let {
                    if (it.firstName?.startsWith(
                            searchedName,
                            true
                        ) == true || it.lastName?.startsWith(searchedName, true) == true
                    ) {
                        searchedList.add(item)
                    }
                }
            }
            communicator?.showNoDataMessage(searchedList.size <= 0)
        }
        addData(searchedList)
    }

    fun clearList() {
        this.usersList.clear()
        this.mainList.clear()
        notifyDataSetChanged()
    }

    fun setOnItemCommunicatorListener(communicator: Communicator) {
        this.communicator = communicator
    }

    interface Communicator {
        fun showNoDataMessage(isDataNotAvailable: Boolean)
        fun onItemClick(userDetailsResponse: UserDetailsEntity?)
    }
}