package dk.chen.garbagev1.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import dk.chen.garbagev1.domain.Item
import dk.chen.garbagev1.domain.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ItemRepository {

    private val itemsCollection = db.collection("items")

    override fun getGarbageList(): Flow<List<Item>> {
        return itemsCollection
            .orderBy("what")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ItemDto::class.java).map { it.toItem() }
            }
    }

    override fun getItem(id: String): Flow<Item?> {
        return itemsCollection.document(id)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(ItemDto::class.java)?.toItem()
            }
    }

    override suspend fun addItem(item: Item) {
        val formattedItem =
            item.copy(
                what = item.what.toTitleCase(),
                where = item.where.toTitleCase(),
            )
        val itemDto = formattedItem.toDto()
        itemsCollection.document(itemDto.id).set(itemDto).await()
    }

    override suspend fun removeItem(item: Item) {
        itemsCollection.document(item.id).delete().await()
    }

    override suspend fun updateItem(item: Item) {
        val formattedItem =
            item.copy(what = item.what.toTitleCase(), where = item.where.toTitleCase())
        val itemDto = formattedItem.toDto()
        itemsCollection.document(itemDto.id).set(itemDto, SetOptions.merge()).await()
    }

    override suspend fun getItemByWhat(what: String): Item? {
        return try {
            val querySnapshot = itemsCollection
                .whereEqualTo("what", what.toTitleCase())
                .get()
                .await()

            querySnapshot.documents.firstOrNull()
                ?.toObject(ItemDto::class.java)
                ?.toItem()
        } catch (e: Exception) {
            null
        }
    }

    private fun Item.toDto() = ItemDto(
        id = this.id,
        what = this.what,
        where = this.where,
        photoPath = this.photoPath
    )

    private fun String.toTitleCase(): String {
        return this.trim().split(" ").joinToString(separator = " ") { word ->
            word.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}