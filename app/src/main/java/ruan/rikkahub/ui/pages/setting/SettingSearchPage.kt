package ruan.rikkahub.ui.pages.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SquarePen
import com.composables.icons.lucide.X
import ruan.rikkahub.R
import ruan.rikkahub.data.datastore.Settings
import ruan.rikkahub.ui.components.nav.BackButton
import ruan.rikkahub.ui.components.ui.AutoAIIcon
import ruan.rikkahub.ui.components.ui.FormItem
import ruan.rikkahub.ui.components.ui.OutlinedNumberInput
import ruan.rikkahub.ui.components.ui.Select
import ruan.rikkahub.ui.components.ui.Tag
import ruan.rikkahub.ui.components.ui.TagType
import ruan.rikkahub.utils.plus
import me.rerere.search.SearchCommonOptions
import me.rerere.search.SearchService
import me.rerere.search.SearchServiceOptions
import org.koin.androidx.compose.koinViewModel
import kotlin.reflect.full.primaryConstructor

private val SUPPORTED_SEARCH_SERVICE_TYPES = listOf(
    SearchServiceOptions.ZhipuOptions::class,
    SearchServiceOptions.TavilyOptions::class,
    SearchServiceOptions.ExaOptions::class,
    SearchServiceOptions.SearXNGOptions::class,
    SearchServiceOptions.BraveOptions::class,
)

@Composable
fun SettingSearchPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.setting_page_search_title))
                },
                navigationIcon = {
                    BackButton()
                }
            )
        }
    ) {
        val lazyListState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = it + PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = lazyListState
        ) {
            // 搜索提供商标题和添加按钮
            item("providers_header") {
                Text(
                    text = stringResource(R.string.setting_page_search_providers),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item("provider") {
                val service = settings.searchServices.firstOrNull() ?: SearchServiceOptions.ZhipuOptions()
                SearchProviderCard(
                    service = service,
                    onUpdateService = { updatedService ->
                        vm.updateSettings(
                            settings.copy(
                                searchServices = listOf(updatedService),
                                searchServiceSelected = 0,
                            )
                        )
                    }
                )
            }

            // 通用选项
            item("common_options") {
                CommonOptions(
                    settings = settings,
                    onUpdate = { options ->
                        vm.updateSettings(
                            settings.copy(
                                searchCommonOptions = options
                            )
                        )
                    }
                )
            }
        }
    }
}


@Composable
private fun SearchProviderCard(
    service: SearchServiceOptions,
    onUpdateService: (SearchServiceOptions) -> Unit,
    modifier: Modifier = Modifier
) {
    var options by remember(service) {
        mutableStateOf(service)
    }
    var expand by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Select(
                    options = SUPPORTED_SEARCH_SERVICE_TYPES,
                    selectedOption = options::class,
                    optionToString = { SearchServiceOptions.TYPES[it] ?: "[Unknown]" },
                    onOptionSelected = {
                        options = it.primaryConstructor!!.callBy(mapOf())
                        onUpdateService(options)
                    },
                    optionLeading = {
                        AutoAIIcon(
                            name = SearchServiceOptions.TYPES[it] ?: it.simpleName ?: "unknown",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    leading = {
                        AutoAIIcon(
                            name = SearchServiceOptions.TYPES[options::class] ?: "unknown",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        expand = !expand
                    }
                ) {
                    Icon(
                        imageVector = if (expand) Lucide.X else Lucide.SquarePen,
                        contentDescription = if (expand) "Hide details" else "Show details"
                    )
                }
            }

            SearchAbilityTagLine(options = options, modifier = Modifier.padding(horizontal = 8.dp))

            AnimatedVisibility(expand) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when (options) {
                        is SearchServiceOptions.TavilyOptions -> {
                            TavilyOptions(options as SearchServiceOptions.TavilyOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }

                        is SearchServiceOptions.ExaOptions -> {
                            ExaOptions(options as SearchServiceOptions.ExaOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }

                        is SearchServiceOptions.ZhipuOptions -> {
                            ZhipuOptions(options as SearchServiceOptions.ZhipuOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }

                        is SearchServiceOptions.SearXNGOptions -> {
                            SearXNGOptions(options as SearchServiceOptions.SearXNGOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }

                        is SearchServiceOptions.LinkUpOptions -> {
                            SearchLinkUpOptions(options as SearchServiceOptions.LinkUpOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }

                        is SearchServiceOptions.BraveOptions -> {
                            BraveOptions(options as SearchServiceOptions.BraveOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }

                        is SearchServiceOptions.MetasoOptions -> {
                            MetasoOptions(options as SearchServiceOptions.MetasoOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }

                        is SearchServiceOptions.OllamaOptions -> {}

                        is SearchServiceOptions.PerplexityOptions -> {
                            PerplexityOptions(options as SearchServiceOptions.PerplexityOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }

                        is SearchServiceOptions.BingLocalOptions -> {}

                        is SearchServiceOptions.FirecrawlOptions -> {
                            FirecrawlOptions(options as SearchServiceOptions.FirecrawlOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }

                        is SearchServiceOptions.JinaOptions -> {
                            JinaOptions(options as SearchServiceOptions.JinaOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }

                        is SearchServiceOptions.BochaOptions -> {
                            BochaOptions(options as SearchServiceOptions.BochaOptions) {
                                options = it
                                onUpdateService(options)
                            }
                        }
                    }

                    ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                        SearchService.getService(options).Description()
                    }
                }
            }
        }
    }
}

@Composable
fun SearchAbilityTagLine(
    modifier: Modifier = Modifier,
    options: SearchServiceOptions
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Tag(
            type = TagType.DEFAULT,
        ) {
            Text(stringResource(R.string.search_ability_search))
        }
        if (SearchService.getService(options).scrapingParameters != null) {
            Tag(
                type = TagType.DEFAULT,
            ) {
                Text(stringResource(R.string.search_ability_scrape))
            }
        }
    }
}

@Composable
private fun TavilyOptions(
    options: SearchServiceOptions.TavilyOptions,
    onUpdateOptions: (SearchServiceOptions.TavilyOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API Key")
        }
    ) {
        OutlinedTextField(
            value = options.apiKey,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        apiKey = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormItem(
        label = {
            Text("Depth")
        }
    ) {
        val depthOptions = listOf("basic", "advanced")
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            depthOptions.forEachIndexed { index, depth ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = depthOptions.size),
                    onClick = {
                        onUpdateOptions(
                            options.copy(
                                depth = depth
                            )
                        )
                    },
                    selected = options.depth == depth
                ) {
                    Text(depth.replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

@Composable
private fun ExaOptions(
    options: SearchServiceOptions.ExaOptions,
    onUpdateOptions: (SearchServiceOptions.ExaOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API Key")
        }
    ) {
        OutlinedTextField(
            value = options.apiKey,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        apiKey = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun ZhipuOptions(
    options: SearchServiceOptions.ZhipuOptions,
    onUpdateOptions: (SearchServiceOptions.ZhipuOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API Key")
        }
    ) {
        OutlinedTextField(
            value = options.apiKey,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        apiKey = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CommonOptions(
    settings: Settings,
    onUpdate: (SearchCommonOptions) -> Unit
) {
    var commonOptions by remember(settings.searchCommonOptions) {
        mutableStateOf(settings.searchCommonOptions)
    }
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.setting_page_search_common_options),
                style = MaterialTheme.typography.titleMedium
            )

            FormItem(
                label = {
                    Text(stringResource(R.string.setting_page_search_result_size))
                }
            ) {
                OutlinedNumberInput(
                    value = commonOptions.resultSize,
                    onValueChange = {
                        commonOptions = commonOptions.copy(
                            resultSize = it
                        )
                        onUpdate(commonOptions)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SearXNGOptions(
    options: SearchServiceOptions.SearXNGOptions,
    onUpdateOptions: (SearchServiceOptions.SearXNGOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API URL")
        }
    ) {
        OutlinedTextField(
            value = options.url,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        url = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormItem(
        label = {
            Text("Engines")
        }
    ) {
        OutlinedTextField(
            value = options.engines,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        engines = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormItem(
        label = {
            Text("Language")
        }
    ) {
        OutlinedTextField(
            value = options.language,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        language = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormItem(
        label = {
            Text("Username")
        }
    ) {
        OutlinedTextField(
            value = options.username,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        username = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormItem(
        label = {
            Text("Password")
        }
    ) {
        OutlinedTextField(
            value = options.password,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        password = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SearchLinkUpOptions(
    options: SearchServiceOptions.LinkUpOptions,
    onUpdateOptions: (SearchServiceOptions.LinkUpOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API Key")
        }
    ) {
        OutlinedTextField(
            value = options.apiKey,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        apiKey = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormItem(
        label = {
            Text("Depth")
        }
    ) {
        val depthOptions = listOf("standard", "deep")
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            depthOptions.forEachIndexed { index, depth ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = depthOptions.size),
                    onClick = {
                        onUpdateOptions(
                            options.copy(
                                depth = depth
                            )
                        )
                    },
                    selected = options.depth == depth
                ) {
                    Text(depth.replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

@Composable
private fun BraveOptions(
    options: SearchServiceOptions.BraveOptions,
    onUpdateOptions: (SearchServiceOptions.BraveOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API Key")
        }
    ) {
        OutlinedTextField(
            value = options.apiKey,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        apiKey = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
private fun MetasoOptions(
    options: SearchServiceOptions.MetasoOptions,
    onUpdateOptions: (SearchServiceOptions.MetasoOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API Key")
        }
    ) {
        OutlinedTextField(
            value = options.apiKey,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        apiKey = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PerplexityOptions(
    options: SearchServiceOptions.PerplexityOptions,
    onUpdateOptions: (SearchServiceOptions.PerplexityOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API Key")
        }
    ) {
        OutlinedTextField(
            value = options.apiKey,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        apiKey = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormItem(
        label = {
            Text("Max Tokens / Page")
        }
    ) {
        OutlinedTextField(
            value = options.maxTokensPerPage?.takeIf { it > 0 }?.toString() ?: "",
            onValueChange = { value ->
                onUpdateOptions(
                    options.copy(
                        maxTokensPerPage = value.toIntOrNull()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun FirecrawlOptions(
    options: SearchServiceOptions.FirecrawlOptions,
    onUpdateOptions: (SearchServiceOptions.FirecrawlOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API Key")
        }
    ) {
        OutlinedTextField(
            value = options.apiKey,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        apiKey = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun JinaOptions(
    options: SearchServiceOptions.JinaOptions,
    onUpdateOptions: (SearchServiceOptions.JinaOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API Key")
        }
    ) {
        OutlinedTextField(
            value = options.apiKey,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        apiKey = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BochaOptions(
    options: SearchServiceOptions.BochaOptions,
    onUpdateOptions: (SearchServiceOptions.BochaOptions) -> Unit
) {
    FormItem(
        label = {
            Text("API Key")
        }
    ) {
        OutlinedTextField(
            value = options.apiKey,
            onValueChange = {
                onUpdateOptions(
                    options.copy(
                        apiKey = it
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormItem(
        label = {
            Text("Summary")
        },
        description = {
            Text("Enable summary generation")
        },
        tail = {
            Switch(
                checked = options.summary,
                onCheckedChange = { checked ->
                    onUpdateOptions(
                        options.copy(
                            summary = checked
                        )
                    )
                }
            )
        }
    )
}
