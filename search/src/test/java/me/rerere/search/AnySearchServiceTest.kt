package me.rerere.search

import org.junit.Assert.assertEquals
import org.junit.Test

class AnySearchServiceTest {
    @Test
    fun parseResponse() {
        val response = AnySearchService.parseResponse(
            """
            {
              "code": 0,
              "message": "success",
              "request_id": "7d6f4e91-2a83-4c5b-9f10-6e8a3d27b541",
              "data": {
                "results": [{
                  "title": "Example source",
                  "url": "https://example.com/article",
                  "snippet": "A short source summary.",
                  "content": "Cleaned page content for the example."
                }],
                "metadata": {
                  "total_results": 1,
                  "search_time_ms": 312
                }
              }
            }
            """.trimIndent()
        )

        assertEquals(1, response.items.size)
        assertEquals("Example source", response.items.single().title)
        assertEquals("https://example.com/article", response.items.single().url)
        assertEquals("Cleaned page content for the example.", response.items.single().text)
    }

    @Test
    fun parseResponseFallsBackToSnippet() {
        val response = AnySearchService.parseResponse(
            """
            {
              "code": 0,
              "message": "success",
              "data": {
                "results": [{
                  "title": "No content",
                  "url": "https://example.com/snippet-only",
                  "snippet": "Snippet only"
                }]
              }
            }
            """.trimIndent()
        )

        assertEquals("Snippet only", response.items.single().text)
    }

    @Test
    fun parseResponseHandlesMissingSnippetAndContent() {
        val response = AnySearchService.parseResponse(
            """
            {
              "code": 0,
              "message": "success",
              "data": {
                "results": [{
                  "title": "",
                  "url": "https://example.com/empty"
                }]
              }
            }
            """.trimIndent()
        )

        assertEquals("", response.items.single().title)
        assertEquals("", response.items.single().text)
    }

    @Test(expected = IllegalStateException::class)
    fun parseResponseThrowsOnError() {
        AnySearchService.parseResponse(
            """
            {
              "code": -1,
              "message": "Query is required.",
              "request_id": "7d6f4e91-2a83-4c5b-9f10-6e8a3d27b541"
            }
            """.trimIndent()
        )
    }
}
