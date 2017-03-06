package io.thelandscape

/**
 * Created by brian.a.madden@gmail.com on 12/4/16.
 *
 * Copyright (c) <2016> <H, llc>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.nhaarman.mockito_kotlin.*
import io.thelandscape.krawler.crawler.History.KrawlHistoryEntry
import io.thelandscape.krawler.crawler.History.KrawlHistoryIf
import io.thelandscape.krawler.crawler.KrawlConfig
import io.thelandscape.krawler.crawler.KrawlQueue.KrawlQueueEntry
import io.thelandscape.krawler.crawler.KrawlQueue.KrawlQueueIf
import io.thelandscape.krawler.crawler.Krawler
import io.thelandscape.krawler.http.KrawlDocument
import io.thelandscape.krawler.http.KrawlUrl
import io.thelandscape.krawler.http.RequestProviderIf
import io.thelandscape.krawler.robots.RoboMinderIf
import io.thelandscape.krawler.robots.RobotsConfig
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.runBlocking
import org.apache.http.client.protocol.HttpClientContext
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KrawlerTest {

    val exampleUrl = KrawlUrl.new("http://www.example.org")
    val mockConfig = KrawlConfig(emptyQueueWaitTime = 1, totalPages = 5)
    val mockHistory = mock<KrawlHistoryIf>()
    val mockQueue = listOf(mock<KrawlQueueIf>())
    val mockRequests = mock<RequestProviderIf>()
    val mockJob = Job()
    val mockMinder = mock<RoboMinderIf>()
    val mockContext = mock<HttpClientContext>()

    val preparedResponse = KrawlDocument(exampleUrl,
            prepareResponse(200, "<html><head><title>Test</title></head><body>" +
                    "<div><a href=\"http://www.testone.com\">Test One</a>" +
                    "<img src=\"imgone.jpg\" /></div></body></html>"),
            mockContext)

    class testCrawler(x: KrawlConfig,
                      w: KrawlHistoryIf,
                      y: List<KrawlQueueIf>,
                      u: RobotsConfig?,
                      v: RequestProviderIf,
                      z: Job): Krawler(x, w, y, u, v, z) {
        override fun shouldVisit(url: KrawlUrl): Boolean {
            return true
        }

        override fun shouldCheck(url: KrawlUrl): Boolean {
            return false
        }

        override fun visit(url: KrawlUrl, doc: KrawlDocument) {
        }

        override fun check(url: KrawlUrl, statusCode: Int) {
        }

    }

    val testKrawler = testCrawler(mockConfig, mockHistory, mockQueue, null, mockRequests, mockJob)

    @Before fun setUp() {
        MockitoKotlin.registerInstanceCreator { KrawlUrl.new("") }
        testKrawler.minder = mockMinder
    }

    /**
     * Test the doCrawl method
     */

    @Test fun testDoCrawl() {
        // Make the hasBeenSeen return true
        whenever(mockHistory.hasBeenSeen(any())).thenReturn(false)
        whenever(mockHistory.insert(any())).thenReturn(KrawlHistoryEntry())
        // Make sure we get a request response
        whenever(mockRequests.getUrl(any())).thenReturn(preparedResponse)
        // Make robo minder return true
        whenever(mockMinder.isSafeToVisit(any())).thenReturn(true)
        //
        whenever(mockQueue[0].pop()).thenReturn(KrawlQueueEntry("http://www.test.com"))

        // Get it started
        runBlocking { testKrawler.doCrawl() }

        verify(mockQueue[0], atLeastOnce()).pop()
        // Verify that isSafeToVisit was called, minding robots.txt
        verify(mockMinder, atLeastOnce()).isSafeToVisit(KrawlUrl.new("http://www.test.com"))
        // Ensure we've called to verify this is a unique URL
        verify(mockHistory, atLeastOnce()).hasBeenSeen(any())
        // Now verify that we insert the URL to the history
        verify(mockHistory, atLeastOnce()).insert(any())

        // The global visit count should also be 1
        assertEquals(5, testKrawler.visitCount.get())
        assertEquals(5, testKrawler.finishedCount.get())
    }

    @Test fun testHarvestLinks() {
        val links: List<KrawlQueueEntry> =
                runBlocking { testKrawler.harvestLinks(preparedResponse, exampleUrl, KrawlHistoryEntry(), 0) }

        assertEquals(2, links.size)
        val linksText = links.map { it.url }
        assertTrue { "http://www.testone.com/" in linksText }
        assertTrue { "http://www.example.org/imgone.jpg" in linksText }
    }
}