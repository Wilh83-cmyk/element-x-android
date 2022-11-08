package io.element.android.x.matrix.room

import io.element.android.x.core.data.CoroutineDispatchers
import io.element.android.x.core.data.flow.chunk
import io.element.android.x.matrix.room.message.RoomMessageFactory
import io.element.android.x.matrix.sync.roomListDiff
import io.element.android.x.matrix.sync.state
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.*
import org.matrix.rustcomponents.sdk.*
import timber.log.Timber
import java.io.Closeable
import java.util.*

interface RoomSummaryDataSource {
    fun roomSummaries(): Flow<List<RoomSummary>>
}

internal class RustRoomSummaryDataSource(
    private val slidingSyncUpdateFlow: Flow<UpdateSummary>,
    private val slidingSync: SlidingSync,
    private val slidingSyncView: SlidingSyncView,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory()
) : RoomSummaryDataSource, Closeable {

    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatchers.io)

    private val roomSummaries = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val state = MutableStateFlow(SlidingSyncState.COLD)


    fun startSync(){
        slidingSyncView.roomListDiff()
            .chunk(100)
            .onEach { diffs ->
                updateRoomSummaries {
                    diffs.forEach {
                        applyDiff(it)
                    }
                }
            }.launchIn(coroutineScope)

        slidingSyncView.state()
            .onEach { slidingSyncState ->
                Timber.v("New sliding sync state: $slidingSyncState")
                state.value = slidingSyncState
            }.launchIn(coroutineScope)

        slidingSyncUpdateFlow
            .onEach {
                didReceiveSyncUpdate(it)
            }.launchIn(coroutineScope)
    }

    fun stopSync() {
        coroutineScope.coroutineContext.cancelChildren()
    }

    override fun close() {
        coroutineScope.cancel()
    }

    override fun roomSummaries(): Flow<List<RoomSummary>> {
        return roomSummaries.sample(100)
    }

    private fun didReceiveSyncUpdate(summary: UpdateSummary) {
        Timber.v("UpdateRooms with identifiers: ${summary.rooms}")
        if (state.value != SlidingSyncState.LIVE) {
            return
        }
        updateRoomSummaries {
            for (identifier in summary.rooms) {
                val index = indexOfFirst { it.identifier() == identifier }
                if (index == -1) {
                    continue
                }
                val updatedRoomSummary = buildRoomSummaryForIdentifier(identifier)
                set(index, updatedRoomSummary)
            }
        }
    }

    private fun MutableList<RoomSummary>.applyDiff(diff: SlidingSyncViewRoomsListDiff) {

        fun MutableList<RoomSummary>.fillUntil(untilIndex: Int) {
            repeat((size - 1 until untilIndex).count()) {
                add(buildEmptyRoomSummary())
            }
        }
        Timber.v("ApplyDiff: $diff for list with size: $size")
        when (diff) {
            is SlidingSyncViewRoomsListDiff.Push -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                add(roomSummary)
            }
            is SlidingSyncViewRoomsListDiff.UpdateAt -> {
                fillUntil(diff.index.toInt())
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                set(diff.index.toInt(), roomSummary)
            }
            is SlidingSyncViewRoomsListDiff.InsertAt -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                add(diff.index.toInt(), roomSummary)
            }
            is SlidingSyncViewRoomsListDiff.Move -> {
                Collections.swap(this, diff.oldIndex.toInt(), diff.newIndex.toInt())
            }
            is SlidingSyncViewRoomsListDiff.RemoveAt -> {
                removeAt(diff.index.toInt())
            }
            is SlidingSyncViewRoomsListDiff.Replace -> {
                clear()
                addAll(diff.values.map { buildSummaryForRoomListEntry(it) })
            }
        }
    }

    private fun buildSummaryForRoomListEntry(entry: RoomListEntry): RoomSummary {
        return when (entry) {
            RoomListEntry.Empty -> buildEmptyRoomSummary()
            is RoomListEntry.Invalidated -> buildRoomSummaryForIdentifier(entry.roomId)
            is RoomListEntry.Filled -> buildRoomSummaryForIdentifier(entry.roomId)
        }
    }

    private fun buildEmptyRoomSummary(): RoomSummary {
        return RoomSummary.Empty(UUID.randomUUID().toString())
    }

    private fun buildRoomSummaryForIdentifier(identifier: String): RoomSummary {
        val room = slidingSync.getRoom(identifier) ?: return RoomSummary.Empty(identifier)
        return RoomSummary.Filled(
            details = roomSummaryDetailsFactory.create(room, room.fullRoom())
        )
    }

    private fun updateRoomSummaries(block: MutableList<RoomSummary>.() -> Unit) {
        val mutableRoomSummaries = roomSummaries.value.toMutableList()
        block(mutableRoomSummaries)
        roomSummaries.value = mutableRoomSummaries
    }

}

fun SlidingSyncViewRoomsListDiff.isInvalidation(): Boolean {
    return when (this) {
        is SlidingSyncViewRoomsListDiff.InsertAt -> this.value is RoomListEntry.Invalidated
        is SlidingSyncViewRoomsListDiff.UpdateAt -> this.value is RoomListEntry.Invalidated
        is SlidingSyncViewRoomsListDiff.Push -> this.value is RoomListEntry.Invalidated
        else -> false
    }
}