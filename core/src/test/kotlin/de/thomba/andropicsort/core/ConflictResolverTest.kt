package de.thomba.andropicsort.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ConflictResolverTest {
    @Test
    fun `returns original when no conflict exists`() {
        val result = ConflictResolver.resolveUniqueName("photo.jpg", existingNames = setOf("a.jpg"))
        assertEquals("photo.jpg", result)
    }

    @Test
    fun `adds numeric suffix when conflict exists`() {
        val result = ConflictResolver.resolveUniqueName(
            desiredName = "photo.jpg",
            existingNames = setOf("photo.jpg", "photo_1.jpg"),
        )

        assertEquals("photo_2.jpg", result)
    }
}

