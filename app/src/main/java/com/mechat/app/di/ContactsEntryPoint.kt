package com.mechat.app.di

import com.mechat.app.data.repository.ContactRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ContactsEntryPoint {
    fun contactRepository(): ContactRepository
}
