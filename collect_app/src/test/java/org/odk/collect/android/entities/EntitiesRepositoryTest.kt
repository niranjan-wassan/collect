package org.odk.collect.android.entities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.android.entities.support.EntitySameAsMatcher.Companion.sameEntityAs
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import org.odk.collect.shared.Query

abstract class EntitiesRepositoryTest {

    abstract fun buildSubject(): EntitiesRepository

    @Test
    fun `#getLists returns lists for saved entities`() {
        val repository = buildSubject()

        val wine = Entity.New("1", "Léoville Barton 2008")
        val whisky = Entity.New("2", "Lagavulin 16")
        repository.save("wines", wine)
        repository.save("whiskys", whisky)

        assertThat(repository.getLists(), containsInAnyOrder("wines", "whiskys"))
    }

    @Test
    fun `#getEntities returns empty list when there are not entities`() {
        val repository = buildSubject()
        assertThat(query("wines", null).size, equalTo(0))
    }

    @Test
    fun `#getEntities returns entities for list`() {
        val repository = buildSubject()

        val wine = Entity.New(
            "1",
            "Léoville Barton 2008",
            version = 2,
            trunkVersion = 1
        )

        val whisky = Entity.New(
            "2",
            "Lagavulin 16",
            version = 3,
            trunkVersion = 1
        )

        repository.save("wines", wine)
        repository.save("whiskys", whisky)

        val wines = query("wines", null)
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0], sameEntityAs(wine))

        val whiskys = query("whiskys", null)
        assertThat(whiskys.size, equalTo(1))
        assertThat(whiskys[0], sameEntityAs(whisky))
    }

    @Test
    fun `#save updates existing entity with matching id`() {
        val repository = buildSubject()

        val wine = Entity.New(
            "1",
            "Léoville Barton 2008",
            trunkVersion = 1
        )
        repository.save("wines", wine)

        val updatedWine = wine.copy(label = "Léoville Barton 2009", version = 2)
        repository.save("wines", updatedWine)

        val wines = query("wines", null)
        assertThat(wines, contains(sameEntityAs(updatedWine)))
    }

    @Test
    fun `#save creates entity with matching id in different list`() {
        val repository = buildSubject()

        val wine = Entity.New("1", "Léoville Barton 2008", version = 1)
        repository.save("wines", wine)

        val updatedWine = Entity.New(wine.id, "Edradour 10", version = 2)
        repository.save("whisky", updatedWine)

        val wines = query("wines", null)
        assertThat(wines, contains(sameEntityAs(wine)))
        val whiskys = query("whisky", null)
        assertThat(whiskys, contains(sameEntityAs(updatedWine)))
    }

    @Test
    fun `#save updates existing entity with matching id and version`() {
        val repository = buildSubject()

        val wine = Entity.New("1", "Léoville Barton 2008", version = 1)
        repository.save("wines", wine)

        val updatedWine = wine.copy(label = "Léoville Barton 2009")
        repository.save("wines", updatedWine)

        val wines = query("wines", null)
        assertThat(wines, contains(sameEntityAs(updatedWine)))
    }

    @Test
    fun `#save updates state on existing entity when it is offline`() {
        val repository = buildSubject()

        val wine = Entity.New("1", "Léoville Barton 2008", state = Entity.State.OFFLINE)
        repository.save("wines", wine)

        val updatedWine = wine.copy(state = Entity.State.ONLINE)
        repository.save("wines", updatedWine)

        val wines = query("wines", null)
        assertThat(wines, contains(sameEntityAs(updatedWine)))
    }

    @Test
    fun `#save does not update state on existing entity when it is online`() {
        val repository = buildSubject()

        val wine = Entity.New("1", "Léoville Barton 2008", state = Entity.State.ONLINE)
        repository.save("wines", wine)

        val updatedWine = wine.copy(state = Entity.State.OFFLINE)
        repository.save("wines", updatedWine)

        val wines = query("wines", null)
        assertThat(wines, contains(sameEntityAs(wine)))
    }

    @Test
    fun `#save adds new properties`() {
        val repository = buildSubject()

        val wine = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save("wines", wine)

        val updatedWine = Entity.New(
            wine.id,
            "Léoville Barton 2008",
            properties = listOf("score" to "92"),
            version = 2
        )
        repository.save("wines", updatedWine)

        val wines = query("wines", null)
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0].properties, contains("window" to "2019-2038", "score" to "92"))
    }

    @Test
    fun `#save adds new properties for lists with dashes`() {
        val repository = buildSubject()

        val wine = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save("favourite-wines", wine)

        val updatedWine = Entity.New(
            wine.id,
            "Léoville Barton 2008",
            properties = listOf("score" to "92"),
            version = 2
        )
        repository.save("favourite-wines", updatedWine)

        val wines = query("favourite-wines", null)
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0].properties, contains("window" to "2019-2038", "score" to "92"))
    }

    @Test
    fun `#save adds new properties to existing entities`() {
        val repository = buildSubject()

        val wine = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save("wines", wine)

        val otherWine = Entity.New(
            "2",
            "Léoville Barton 2009",
            properties = listOf("score" to "92"),
            version = 2
        )
        repository.save("wines", otherWine)

        val wines = query("wines", null)
        assertThat(wines.size, equalTo(2))
        assertThat(wines[0].properties, contains("window" to "2019-2038", "score" to ""))
        assertThat(wines[1].properties, contains("window" to "", "score" to "92"))
    }

    @Test
    fun `#save updates existing properties`() {
        val repository = buildSubject()

        val wine = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save("wines", wine)

        val updatedWine = Entity.New(
            wine.id,
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2042"),
            version = 2
        )
        repository.save("wines", updatedWine)

        val wines = query("wines", null)
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0].properties, contains("window" to "2019-2042"))
    }

    @Test
    fun `#save does not update existing label if new one is null`() {
        val repository = buildSubject()

        val wine = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("window" to "2019-2038"),
            version = 1
        )
        repository.save("wines", wine)

        val updatedWine = Entity.New(
            wine.id,
            null,
            properties = listOf("window" to "2019-2042"),
            version = 2
        )
        repository.save("wines", updatedWine)

        val wines = query("wines", null)
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0].label, equalTo(wine.label))
        assertThat(wines[0].properties, equalTo(updatedWine.properties))
    }

    @Test
    fun `#save does not clear empty entity lists`() {
        val repository = buildSubject()

        repository.addList("wines")
        repository.addList("blah")
        assertThat(repository.getLists(), containsInAnyOrder("wines", "blah"))

        repository.save("wines", Entity.New("blah", "Blah"))
        assertThat(repository.getLists(), containsInAnyOrder("wines", "blah"))
    }

    @Test
    fun `#save supports properties with dots and dashes when saving new entities and updating existing ones`() {
        val repository = buildSubject()
        val entity = Entity.New(
            "1",
            "One",
            properties = listOf(Pair("a.property", "value"), Pair("a-property", "value"))
        )

        repository.save("things", entity)
        val savedEntity = query("things", null)[0]
        assertThat(savedEntity, sameEntityAs(entity))

        repository.save("things", savedEntity)
        assertThat(query("things", null)[0], sameEntityAs(savedEntity))
    }

    @Test
    fun `#save does not create a list when no entities are provided`() {
        val repository = buildSubject()
        repository.save("blah")
        assertThat(repository.getLists(), equalTo(emptySet()))
    }

    @Test
    fun `#save supports creating list names with with dots and dashes`() {
        val repository = buildSubject()

        val wine = Entity.New("1", "Léoville Barton 2008")

        repository.save("favourite-wines", wine)
        assertThat(query("favourite-wines", null)[0], sameEntityAs(wine))

        repository.save("favourite.wines", wine)
        assertThat(query("favourite.wines", null)[0], sameEntityAs(wine))
    }

    @Test
    fun `#save can save multiple entities`() {
        val repository = buildSubject()

        val wine1 = Entity.New("1", "Léoville Barton 2008")
        val wine2 = Entity.New("2", "Chateau Pontet Canet")
        repository.save("wines", wine1, wine2)

        assertThat(query("wines", null).size, equalTo(2))
    }

    @Test
    fun `#save assigns an index to each entity in insert order when saving multiple entities`() {
        /**
         * first and second have alphabetically out of order IDs/names here so that any indexing on
         * them is tested. We'd likely never see this fail if they were ordered.
         */
        val first = Entity.New("2", "B")
        val second = Entity.New("1", "A")

        val repository = buildSubject()
        repository.save("wines", first, second)

        val entities = query("wines", null)
        assertThat(entities[0].index, equalTo(0))
        assertThat(entities[0].id, equalTo(first.id))
        assertThat(entities[1].index, equalTo(1))
        assertThat(entities[1].id, equalTo(second.id))
    }

    @Test
    fun `#save assigns an index to each entity in insert order when saving single entities`() {
        val first = Entity.New("1", "Léoville Barton 2008")
        val second = Entity.New("2", "Pontet Canet 2014")

        val repository = buildSubject()
        repository.save("wines", first)
        repository.save("wines", second)

        val entities = query("wines", null)
        assertThat(entities[0].index, equalTo(0))
        assertThat(entities[1].index, equalTo(1))
    }

    @Test
    fun `#save does not change index when updating an existing entity`() {
        val repository = buildSubject()

        val first = Entity.New("1", "Léoville Barton 2008")
        val second = Entity.New("2", "Pontet Canet 2014")
        repository.save("wines", first, second)
        assertThat(query("wines", null)[0].index, equalTo(0))

        val updatedWine = first.copy(label = "Léoville Barton 2009")
        repository.save("wines", updatedWine)

        assertThat(query("wines", null)[0].index, equalTo(0))
    }

    @Test
    fun `#addList adds a list with no entities`() {
        val repository = buildSubject()

        repository.addList("wine")
        assertThat(repository.getLists(), containsInAnyOrder("wine"))
        assertThat(query("wine", null).size, equalTo(0))
    }

    @Test
    fun `#addList works if list already exists`() {
        val repository = buildSubject()

        repository.addList("wine")
        repository.addList("wine")
        assertThat(repository.getLists(), containsInAnyOrder("wine"))
        assertThat(query("wine", null).size, equalTo(0))
    }

    @Test
    fun `#delete removes an entity`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val canet = Entity.New("2", "Pontet-Canet 2014")
        repository.save("wines", leoville, canet)

        repository.delete("1")

        assertThat(
            query("wines", null),
            containsInAnyOrder(sameEntityAs(canet))
        )
    }

    @Test
    fun `#delete supports list names with dots and dashes`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")

        repository.save("wines.x", leoville)
        repository.save("wines-x", leoville)

        repository.delete("1")

        assertThat(
            query("wines.x", null).isEmpty(),
            equalTo(true)
        )
        assertThat(
            query("wines-x", null).isEmpty(),
            equalTo(true)
        )
    }

    @Test
    fun `#delete updates index values so that they are always in sequence and start at 0`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val canet = Entity.New("2", "Pontet-Canet 2014")
        val gloria = Entity.New("3", "Chateau Gloria 2016")
        repository.save("wines", leoville, canet, gloria)

        repository.delete("1")

        var wines = query("wines", null)
        assertThat(wines[0].index, equalTo(0))
        assertThat(wines[1].index, equalTo(1))

        repository.save("wines", leoville)
        wines = query("wines", null)
        assertThat(wines[0].index, equalTo(0))
        assertThat(wines[1].index, equalTo(1))
        assertThat(wines[2].index, equalTo(2))
    }

    @Test
    fun `#getById returns entities with matching id`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val canet = Entity.New("2", "Pontet-Canet 2014")
        repository.save("wines", leoville, canet)

        val wines = query("wines", null)

        val queriedLeoville = repository.query("wines", Query.Eq(EntitiesTable.COLUMN_ID, "1"))
        assertThat(queriedLeoville, equalTo(wines.first { it.id == "1" }))

        val queriedCanet = repository.query("wines", Query.Eq(EntitiesTable.COLUMN_ID, "2"))
        assertThat(queriedCanet, equalTo(wines.first { it.id == "2" }))
    }

    @Test
    fun `#getById returns null when there are no matches`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val canet = Entity.New("2", "Pontet-Canet 2014")
        repository.save("wines", leoville, canet)

        assertThat(repository.query("wines", Query.Eq(EntitiesTable.COLUMN_ID, "3")), equalTo(null))
    }

    @Test
    fun `#getById returns null when there is a match in a different list`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val ardbeg = Entity.New("2", "Ardbeg 10")
        repository.save("wines", leoville)
        repository.save("whisky", ardbeg)

        assertThat(repository.query("whisky", Query.Eq(EntitiesTable.COLUMN_ID, "1")), equalTo(null))
    }

    @Test
    fun `#getById returns null where there are no entities in the list`() {
        val repository = buildSubject()
        assertThat(repository.query("wines", Query.Eq(EntitiesTable.COLUMN_ID, "3")), equalTo(null))
    }

    @Test
    fun `#getById supports list names with dots and dashes`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val canet = Entity.New("2", "Pontet-Canet 2014")
        repository.save("favourite-wines", leoville)
        repository.save("other.favourite.wines", canet)

        val favouriteWines = query("favourite-wines", null)
        val otherFavouriteWines = query("other.favourite.wines", null)

        val queriedLeoville =
            repository.query("favourite-wines", Query.Eq(EntitiesTable.COLUMN_ID, "1"))
        assertThat(queriedLeoville, equalTo(favouriteWines.first { it.id == "1" }))

        val queriedCanet =
            repository.query("other.favourite.wines", Query.Eq(EntitiesTable.COLUMN_ID, "2"))
        assertThat(queriedCanet, equalTo(otherFavouriteWines.first { it.id == "2" }))
    }

    @Test
    fun `#getAllByProperty returns entities with matching property value`() {
        val repository = buildSubject()

        val leoville = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("vintage" to "2008")
        )

        val canet = Entity.New(
            "2",
            "Pontet-Canet 2014",
            properties = listOf("vintage" to "2014")
        )

        repository.save("wines", leoville, canet)

        val wines = query("wines", null)
        assertThat(
            repository.getAllByProperty("wines", "vintage", "2014"),
            containsInAnyOrder(wines.first { it.id == "2" })
        )
    }

    @Test
    fun `#getAllByProperty returns entities without property when searching for empty string`() {
        val repository = buildSubject()

        val leoville = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("vintage" to "2008")
        )

        val canet = Entity.New(
            "2",
            "Pontet-Canet 2014",
            properties = listOf("score" to "93")
        )

        repository.save("wines", leoville)
        repository.save("wines", canet)

        val allByProperty = repository.getAllByProperty("wines", "score", "")
        assertThat(allByProperty.size, equalTo(1))
        assertThat(allByProperty[0].id, equalTo("1"))
    }

    @Test
    fun `#getAllByProperty returns entities when searching for empty string for property that doesn't exist`() {
        val repository = buildSubject()

        val leoville = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("vintage" to "2008")
        )

        repository.save("wines", leoville)
        assertThat(repository.getAllByProperty("wines", "score", "").size, equalTo(1))
    }

    @Test
    fun `#getAllByProperty returns empty list when searching for non empty string for property that doesn't exist`() {
        val repository = buildSubject()

        val leoville = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("vintage" to "2008")
        )

        repository.save("wines", leoville)
        assertThat(repository.getAllByProperty("wines", "score", "92").size, equalTo(0))
    }

    @Test
    fun `#getAllByProperty returns empty list when there are no matches`() {
        val repository = buildSubject()

        val leoville = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("vintage" to "2008")
        )

        val canet = Entity.New(
            "2",
            "Pontet-Canet 2014",
            properties = listOf("vintage" to "2014")
        )

        repository.save("wines", leoville, canet)
        assertThat(repository.getAllByProperty("wines", "vintage", "2024"), equalTo(emptyList()))
    }

    @Test
    fun `#getAllByProperty returns empty list when there is a match in a different list`() {
        val repository = buildSubject()

        val leoville = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("vintage" to "2008")
        )
        val dows = Entity.New(
            "2",
            "Dow's 1983",
            properties = listOf("vintage" to "1983")
        )

        repository.save("wines", leoville)
        repository.save("ports", dows)
        assertThat(repository.getAllByProperty("wines", "vintage", "1983"), equalTo(emptyList()))
    }

    @Test
    fun `#getAllByProperty returns empty list when there are no entities`() {
        val repository = buildSubject()
        assertThat(repository.getAllByProperty("wines", "vintage", "1983"), equalTo(emptyList()))
    }

    @Test
    fun `#getAllByProperty supports list names with dots and dashes`() {
        val repository = buildSubject()

        val leoville = Entity.New(
            "1",
            "Léoville Barton 2008",
            properties = listOf("vintage" to "2008")
        )

        repository.save("favourite-wines", leoville)
        repository.save("favourite.wines", leoville)

        var wines = query("favourite-wines", null)
        assertThat(
            repository.getAllByProperty("favourite-wines", "vintage", "2008"),
            containsInAnyOrder(wines.first { it.id == "1" })
        )

        wines = query("favourite.wines", null)
        assertThat(
            repository.getAllByProperty("favourite.wines", "vintage", "2008"),
            containsInAnyOrder(wines.first { it.id == "1" })
        )
    }

    @Test
    fun `#getCount returns 0 when a list is empty`() {
        val repository = buildSubject()
        repository.addList("wines")

        assertThat(repository.getCount("wines"), equalTo(0))
    }

    @Test
    fun `#getCount returns 0 when a list does not exist`() {
        val repository = buildSubject()
        assertThat(repository.getCount("wines"), equalTo(0))
    }

    @Test
    fun `#getCount returns number of entities in list`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val dows = Entity.New("2", "Dow's 1983")
        repository.save("wines", leoville, dows)

        val springbank = Entity.New("1", "Springbank 10")
        repository.save("whiskys", springbank)

        assertThat(repository.getCount("wines"), equalTo(2))
        assertThat(repository.getCount("whiskys"), equalTo(1))
    }

    @Test
    fun `#getCount supports list names with dots and dashes`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val dows = Entity.New("2", "Dow's 1983")
        repository.save("favourite-wines", leoville, dows)

        val springbank = Entity.New("1", "Springbank 10")
        repository.save("favourite.whiskys", springbank)

        assertThat(repository.getCount("favourite-wines"), equalTo(2))
        assertThat(repository.getCount("favourite.whiskys"), equalTo(1))
    }

    @Test
    fun `#getByIndex returns matching entity`() {
        val repository = buildSubject()

        val springbank = Entity.New("1", "Springbank 10")
        val aultmore = Entity.New("2", "Aultmore 12")
        repository.save("whiskys", springbank, aultmore)

        val aultmoreIndex = query("whiskys", null).first { it.id == aultmore.id }.index
        assertThat(repository.query(
            "whiskys",
            Query.Eq("i.$ROW_ID", (aultmoreIndex + 1).toString())
        ), sameEntityAs(aultmore))
    }

    @Test
    fun `#getByIndex returns null when the list does not exist`() {
        val repository = buildSubject()
        assertThat(repository.query("wine", Query.Eq("i.$ROW_ID", (0 + 1).toString())), equalTo(null))
    }

    @Test
    fun `#getByIndex returns null when the list is empty`() {
        val repository = buildSubject()
        repository.addList("wine")

        assertThat(repository.query("wine", Query.Eq("i.$ROW_ID", (0 + 1).toString())), equalTo(null))
    }

    @Test
    fun `#getByIndex supports list names with dots and dashes`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val canet = Entity.New("2", "Pontet-Canet 2014")
        repository.save("favourite-wines", leoville)
        repository.save("other.favourite.wines", canet)

        val leovilleIndex =
            query("favourite-wines", null).first { it.id == leoville.id }.index
        assertThat(repository.query(
            "favourite-wines",
            Query.Eq("i.$ROW_ID", (leovilleIndex + 1).toString())
        ), sameEntityAs(leoville))

        val canetIndex =
            query("other.favourite.wines", null).first { it.id == canet.id }.index
        assertThat(repository.query(
            "other.favourite.wines",
            Query.Eq("i.$ROW_ID", (canetIndex + 1).toString())
        ), sameEntityAs(canet))
    }

    @Test
    fun `#getListVersion returns list version`() {
        val repository = buildSubject()

        repository.addList("wine")
        repository.updateListHash("wine", "2024")
        assertThat(repository.getListHash("wine"), equalTo("2024"))
    }

    @Test
    fun `#save ignores case-insensitive duplicate new properties`() {
        val repository = buildSubject()
        val entity = Entity.New(
            "1",
            "One",
            properties = listOf(Pair("prop", "value"), Pair("Prop", "value"))
        )

        repository.save("things", entity)
        val savedEntities = query("things", null)
        assertThat(savedEntities[0].properties.size, equalTo(1))
        assertThat(savedEntities[0].properties[0].first, equalTo("prop"))
    }

    @Test
    fun `#save ignores case-insensitive duplicate properties if one of them has already been saved`() {
        val repository = buildSubject()
        val entity = Entity.New(
            "1",
            "One",
            properties = listOf(Pair("prop", "value"))
        )

        repository.save("things", entity)
        var savedEntities = query("things", null)
        assertThat(savedEntities[0].properties.size, equalTo(1))
        assertThat(savedEntities[0].properties[0].first, equalTo("prop"))

        repository.save("things", entity.copy(properties = listOf(Pair("Prop", "value"))))
        savedEntities = query("things", null)
        assertThat(savedEntities[0].properties.size, equalTo(1))
        assertThat(savedEntities[0].properties[0].first, equalTo("prop"))
    }

    @Test
    fun `#query returns matching entities`() {
        val repository = buildSubject()

        val leoville = Entity.New(
            "1",
            "Léoville Barton 2008",
            version = 1,
            properties = listOf("vintage" to "2008")
        )

        val canet = Entity.New(
            "2",
            "Pontet-Canet 2014",
            version = 2,
            properties = listOf("vintage" to "2009")
        )

        repository.save("wines", leoville, canet)

        val wines = repository.query("wines", Query.Eq("name", "2"))
        assertThat(wines, containsInAnyOrder(sameEntityAs(canet)))
    }

    @Test
    fun `#query returns empty list when there are no matches`() {
        val repository = buildSubject()

        val leoville = Entity.New(
            "1",
            "Léoville Barton 2008",
            version = 1,
            properties = listOf("vintage" to "2008")
        )

        repository.save("wines", leoville)

        val wines = repository.query("wines", Query.Eq("name", "3"))
        assertThat(wines, equalTo(emptyList()))
    }

    @Test
    fun `#query returns empty list when there is a match in a different list`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val ardbeg = Entity.New("2", "Ardbeg 10",)

        repository.save("wines", leoville)
        repository.save("whisky", ardbeg)

        assertThat(repository.query("wines", Query.Eq("label", "Ardbeg 10")), equalTo(emptyList()))
    }

    @Test
    fun `#query returns empty list where there are no entities in the list`() {
        val repository = buildSubject()
        assertThat(repository.query("wines", Query.Eq("label", "Léoville Barton 2008")), equalTo(emptyList()))
    }

    @Test
    fun `#query supports list names with dots and dashes`() {
        val repository = buildSubject()

        val leoville = Entity.New("1", "Léoville Barton 2008")
        val canet = Entity.New("2", "Pontet-Canet 2014")
        repository.save("favourite-wines", leoville)
        repository.save("other.favourite.wines", canet)

        val queriedLeoville = repository.query("favourite-wines", Query.Eq("label", "Léoville Barton 2008"))
        assertThat(queriedLeoville, containsInAnyOrder(sameEntityAs(leoville)))

        val queriedCanet = repository.query("other.favourite.wines", Query.Eq("label", "Pontet-Canet 2014"))
        assertThat(queriedCanet, containsInAnyOrder(sameEntityAs(canet)))
    }
}
