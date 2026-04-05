package com.holidaymessenger.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.holidaymessenger.data.contacts.ContactsProvider
import com.holidaymessenger.data.repository.ContactRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodically syncs birthday data from device contacts into the app database.
 */
@HiltWorker
class BirthdayCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val contactsProvider: ContactsProvider,
    private val contactRepository: ContactRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val deviceContacts = contactsProvider.getDeviceContacts()

        for (device in deviceContacts) {
            if (device.birthday == null) continue
            val existing = contactRepository.getContactById(device.id) ?: continue

            // Update birthday if it changed and there's no manual override
            if (existing.birthdayOverride == null && existing.birthday != device.birthday) {
                contactRepository.updateContact(existing.copy(birthday = device.birthday))
            }
        }

        return Result.success()
    }
}
