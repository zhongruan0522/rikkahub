package ruan.rikkahub.di

import ruan.rikkahub.ui.pages.assistant.AssistantVM
import ruan.rikkahub.ui.pages.assistant.detail.AssistantDetailVM
import ruan.rikkahub.ui.pages.backup.BackupVM
import ruan.rikkahub.ui.pages.chat.ChatVM
import ruan.rikkahub.ui.pages.debug.DebugVM
import ruan.rikkahub.ui.pages.developer.DeveloperVM
import ruan.rikkahub.ui.pages.history.HistoryVM
import ruan.rikkahub.ui.pages.imggen.ImgGenVM
import ruan.rikkahub.ui.pages.prompts.PromptVM
import ruan.rikkahub.ui.pages.setting.SettingVM
import ruan.rikkahub.ui.pages.share.handler.ShareHandlerVM
import ruan.rikkahub.ui.pages.translator.TranslatorVM
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModel<ChatVM> { params ->
        ChatVM(
            id = params.get(),
            context = get(),
            settingsStore = get(),
            conversationRepo = get(),
            chatService = get(),
            analytics = get()
        )
    }
    viewModelOf(::SettingVM)
    viewModelOf(::DebugVM)
    viewModelOf(::HistoryVM)
    viewModelOf(::AssistantVM)
    viewModel<AssistantDetailVM> {
        AssistantDetailVM(
            id = it.get(),
            settingsStore = get(),
            memoryRepository = get(),
            context = get(),
        )
    }
    viewModelOf(::TranslatorVM)
    viewModel<ShareHandlerVM> {
        ShareHandlerVM(
            text = it.get(),
            settingsStore = get(),
        )
    }
    viewModelOf(::BackupVM)
    viewModelOf(::ImgGenVM)
    viewModelOf(::DeveloperVM)
    viewModelOf(::PromptVM)
}
