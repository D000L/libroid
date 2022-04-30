package com.doool.libroid.presentation

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.text.Text
import com.doool.libroid.data.datasource.entity.toModel
import com.doool.libroid.data.datasource.local.AppDatabase
import com.doool.libroid.data.datasource.local.BookMarkPreference
import com.doool.libroid.data.datasource.remote.AppRetrofit
import com.doool.libroid.data.repository.BookmarkRepositoryImpl
import com.doool.libroid.data.repository.LibraryRepositoryImpl
import com.doool.libroid.domain.model.LibraryModel
import com.doool.libroid.presentation.item.LibraryGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class GreetingsWidget(private val list: SnapshotStateList<LibraryModel>): GlanceAppWidget() {

    @Composable
    override fun Content() {
        LazyColumn{
            items(list){
                LibraryItem(it)
            }
        }

    }

    @Composable
    private fun LibraryItem(group : LibraryModel){
        Text(text = group.name)
    }
}

class LibroidWidgetReceiver : GlanceAppWidgetReceiver() {

    private val list = mutableStateListOf<LibraryModel>()
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    override val glanceAppWidget = GreetingsWidget(list)

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
    }
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        coroutineScope.launch {
            AppDatabase.getInstance(context).libraryDao().getAll().collect {
                list.clear()
                list.addAll(it.map { it.toModel() })
                updateAppWidgetState()
            }
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        job.cancel()
    }
}