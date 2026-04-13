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

    @Test
    fun `handles file without extension`() {
        val result = ConflictResolver.resolveUniqueName(
            desiredName = "README",
            existingNames = setOf("README"),
        )
        assertEquals("README_1", result)
    }

    @Test
    fun `handles dotfile`() {
        val result = ConflictResolver.resolveUniqueName(
            desiredName = ".gitignore",
            existingNames = setOf(".gitignore"),
        )
        // dotIndex is 0, so base=".gitignore" and ext="" -> ".gitignore_1"
        assertEquals(".gitignore_1", result)
    }

    @Test
    fun `handles empty existing set`() {
        val result = ConflictResolver.resolveUniqueName("photo.jpg", existingNames = emptySet())
        assertEquals("photo.jpg", result)
    }

    @Test
    fun `handles many conflicts sequentially`() {
        val existing = (0..100).map { if (it == 0) "img.png" else "img_$it.png" }.toSet()
        val result = ConflictResolver.resolveUniqueName("img.png", existing)
        assertEquals("img_101.png", result)
    }
}

