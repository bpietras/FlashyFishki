package com.an.intelligence.flashyfishki

import androidx.lifecycle.ViewModel
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel()
