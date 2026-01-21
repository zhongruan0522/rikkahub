package ruan.rikkahub.di

import ruan.rikkahub.data.repository.ConversationRepository
import ruan.rikkahub.data.repository.GenMediaRepository
import ruan.rikkahub.data.repository.MemoryRepository
import org.koin.dsl.module

val repositoryModule = module {
    single {
        ConversationRepository(get(), get(), get(), get())
    }

    single {
        MemoryRepository(get())
    }

    single {
        GenMediaRepository(get())
    }
}
