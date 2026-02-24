package com.example.gohealth.data.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gohealth.data.entities.Users
import com.example.gohealth.data.repositories.UsersRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UsersViewModel(private val repository: UsersRepository) : ViewModel() {
    val users: StateFlow<List<Users>> = repository.allUsers.stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun insertUser(user: Users) {
        viewModelScope.launch {
            repository.insert(user)
        }
    }

    fun updateUser(user: Users) {
        viewModelScope.launch {
            repository.update(user)
        }
    }
}
