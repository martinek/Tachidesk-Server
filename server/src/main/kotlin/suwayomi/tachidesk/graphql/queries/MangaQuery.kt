/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package suwayomi.tachidesk.graphql.queries

import com.expediagroup.graphql.server.extensions.getValueFromDataLoader
import graphql.schema.DataFetchingEnvironment
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.graphql.types.MangaType
import suwayomi.tachidesk.manga.model.table.CategoryMangaTable
import suwayomi.tachidesk.manga.model.table.MangaTable
import java.util.concurrent.CompletableFuture

class MangaQuery {
    fun manga(dataFetchingEnvironment: DataFetchingEnvironment, id: Int): CompletableFuture<MangaType> {
        return dataFetchingEnvironment.getValueFromDataLoader<Int, MangaType>("MangaDataLoader", id)
    }

    data class MangaQueryInput(
        val ids: List<Int>? = null,
        val categoryIds: List<Int>? = null,
        val page: Int? = null,
        val count: Int? = null
    )

    fun mangas(input: MangaQueryInput? = null): List<MangaType> {
        val results = transaction {
            var res = MangaTable.selectAll()

            if (input != null) {
                if (input.categoryIds != null) {
                    res = MangaTable.innerJoin(CategoryMangaTable)
                        .select { CategoryMangaTable.category inList input.categoryIds }
                }
                if (input.ids != null) {
                    res.andWhere { MangaTable.id inList input.ids }
                }
                if (input.count != null) {
                    val offset = if (input.page == null) 0 else (input.page * input.count).toLong()
                    res.limit(input.count, offset)
                }
            }

            res.toList()
        }

        return results.map { MangaType(it) }
    }
}
