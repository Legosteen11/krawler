/**
 * Created by brian.a.madden@gmail.com on 11/1/16.
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

package io.thelandscape.krawler.crawler.KrawlQueue

import java.time.LocalDateTime

/**
 * Interface representing a KrawlQueue
 */
interface KrawlQueueIf {
    // Pop an entry from the queue
    fun pop (): KrawlQueueEntry?
    // Push an entry on the queue
    fun push (urls: List<KrawlQueueEntry>): List<KrawlQueueEntry>
    // Delete all entries with specific root page ID
    fun deleteByRootPageId (rootPageId: Int): Int
    // Delete all entries older than beforeTime
    fun deleteByAge(beforeTime: LocalDateTime): Int
}