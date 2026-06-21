/*
Title: Merge Live Rankings from Trending Feeds
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
A social media platform receives multiple already-sorted trending feeds from different regions.
Each regional feed is sorted in descending order by score, where a higher score means a post is
more trending. You are given k feeds, where each feed contains post records in the form [postId, score].
The same postId may appear in multiple feeds with different scores because regions rank posts independently.

Your task is to build a single global ranking of the top m unique posts. For every postId, its global score
is defined as the maximum score it achieves in any regional feed. Return the top m unique postIds sorted by
global score in descending order. If two posts have the same global score, the smaller postId should come first.

You should design an efficient solution using heaps or priority queues. A brute-force approach that flattens
all feeds and sorts everything may be too slow when the total number of feed entries is large.

Constraints:
- 1 <= k <= 10^4
- 1 <= total number of feed entries across all feeds <= 2 * 10^5
- 1 <= length of each feed <= 2 * 10^5
- 1 <= postId <= 10^9
- 1 <= score <= 10^9
- 1 <= m <= number of distinct postIds across all feeds
- Each individual feed is already sorted by score in descending order

Example 1:
Input: feeds = [
  [[101, 95], [102, 90], [103, 80]],
  [[104, 99], [101, 97], [105, 70]],
  [[102, 96], [106, 88]]
], m = 4
Output: [104, 101, 102, 106]

Example 2:
Input: feeds = [
  [[7, 50], [8, 40], [9, 30]],
  [[8, 60], [10, 55]],
  [[7, 60], [11, 20]]
], m = 3
Output: [7, 8, 10]
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    private sealed class FeedEntryComparer : IComparer<(int score, int postId, int feedIndex, int itemIndex)>
    {
        public int Compare((int score, int postId, int feedIndex, int itemIndex) x, (int score, int postId, int feedIndex, int itemIndex) y)
        {
            // We want a max-heap behavior, but .NET PriorityQueue is a min-heap.
            // So we define "smaller priority" as:
            // 1) larger score first
            // 2) smaller postId first for tie-breaking
            // 3) feedIndex / itemIndex only to keep ordering deterministic and avoid equality collisions
            int byScore = y.score.CompareTo(x.score);
            if (byScore != 0) return byScore;

            int byPostId = x.postId.CompareTo(y.postId);
            if (byPostId != 0) return byPostId;

            int byFeed = x.feedIndex.CompareTo(y.feedIndex);
            if (byFeed != 0) return byFeed;

            return x.itemIndex.CompareTo(y.itemIndex);
        }
    }

    /*
    Time Complexity:
    - Building the global maximum score for each post by scanning all entries: O(N)
      where N is the total number of feed entries across all feeds.
    - Pushing the first item of each feed into the heap: O(k log k) in the worst case.
    - Heap-driven merge traversal over all feed entries: O(N log k)
      because each feed entry is pushed and popped at most once.
    - Total: O(N log k)

    Space Complexity:
    - Dictionary storing best score for each distinct post: O(U)
      where U is the number of unique postIds.
    - Heap stores at most one current pointer per feed: O(k)
    - Result list: O(m)
    - Total: O(U + k + m)
    */
    public IList<int> MergeLiveRankings(int[][][] feeds, int m)
    {
        // -----------------------------
        // STEP 1: Compute the true global maximum score for every postId.
        // -----------------------------
        //
        // Why do we do this first?
        // The problem defines a post's global score as the maximum score it gets in ANY feed.
        // Since the same post can appear multiple times across different feeds, we cannot safely
        // output a post the first time we see it from the heap unless we already know that score
        // is its best possible score globally.
        //
        // Example:
        // post 101 might appear with score 95 in one feed and later with score 97 in another feed.
        // If we output 101 when we first see 95, that would be wrong.
        //
        // So first, we scan all entries once and build:
        // bestScoreByPost[postId] = maximum score seen for that post.
        //
        // This is still efficient because the total number of entries is at most 2 * 10^5.
        var bestScoreByPost = new Dictionary<int, int>();

        for (int feedIndex = 0; feedIndex < feeds.Length; feedIndex++)
        {
            var feed = feeds[feedIndex];

            for (int itemIndex = 0; itemIndex < feed.Length; itemIndex++)
            {
                int postId = feed[itemIndex][0];
                int score = feed[itemIndex][1];

                if (!bestScoreByPost.TryGetValue(postId, out int currentBest) || score > currentBest)
                {
                    bestScoreByPost[postId] = score;
                }
            }
        }

        // -----------------------------
        // STEP 2: Initialize a heap with the first item from each feed.
        // -----------------------------
        //
        // Why a heap?
        // Each feed is already sorted in descending score order.
        // This means if we keep a pointer into each feed, the "current" item from each feed
        // is the best remaining candidate from that feed.
        //
        // A heap lets us efficiently ask:
        // "Among the current front items of all feeds, which one has the highest score?"
        //
        // That is exactly the classic k-way merge idea.
        //
        // Important:
        // .NET's PriorityQueue is a min-heap, so we provide a custom comparer that makes
        // higher scores come out first, and for equal scores smaller postId comes out first.
        var heap = new PriorityQueue<(int score, int postId, int feedIndex, int itemIndex), (int score, int postId, int feedIndex, int itemIndex)>(
            new FeedEntryComparer()
        );

        for (int feedIndex = 0; feedIndex < feeds.Length; feedIndex++)
        {
            if (feeds[feedIndex].Length == 0)
            {
                continue;
            }

            int postId = feeds[feedIndex][0][0];
            int score = feeds[feedIndex][0][1];

            var entry = (score, postId, feedIndex, 0);
            heap.Enqueue(entry, entry);
        }

        // -----------------------------
        // STEP 3: Repeatedly pop the best visible candidate from the heap.
        // -----------------------------
        //
        // We only add a post to the answer when:
        // 1) We have not already added it before.
        // 2) The score we popped equals that post's true global maximum score.
        //
        // Why condition (2)?
        // Because only then do we know this heap entry represents the correct final ranking score
        // for that post. If the popped score is lower than the global maximum, then some other feed
        // contains a better version of the same post, so we must wait.
        //
        // Why is the ordering correct when we do this?
        // Because the heap always gives us the highest currently available score across all feeds.
        // Once we encounter an entry whose score equals the post's global best, that entry is the
        // correct ranking representative for that post. If it is the best available valid candidate
        // at that moment, it belongs next in the final sorted order.
        //
        // Tie-breaking:
        // If two posts have the same global score, smaller postId should come first.
        // Our heap comparer enforces that order when scores tie.
        var result = new List<int>(m);
        var alreadyAdded = new HashSet<int>();

        while (heap.Count > 0 && result.Count < m)
        {
            var current = heap.Dequeue();

            int score = current.score;
            int postId = current.postId;
            int feedIndex = current.feedIndex;
            int itemIndex = current.itemIndex;

            // -----------------------------
            // STEP 3a: Advance the pointer in the same feed.
            // -----------------------------
            //
            // This is necessary for the k-way merge.
            // Once we consume the current item from a feed, the next item in that feed becomes
            // the next candidate from that region.
            int nextIndex = itemIndex + 1;
            if (nextIndex < feeds[feedIndex].Length)
            {
                int nextPostId = feeds[feedIndex][nextIndex][0];
                int nextScore = feeds[feedIndex][nextIndex][1];

                var nextEntry = (nextScore, nextPostId, feedIndex, nextIndex);
                heap.Enqueue(nextEntry, nextEntry);
            }

            // -----------------------------
            // STEP 3b: Decide whether this popped entry should be part of the answer.
            // -----------------------------
            //
            // We skip if:
            // - we already output this postId
            // - this score is not the post's global maximum
            //
            // Only the first time we see the post at its true maximum score do we add it.
            if (alreadyAdded.Contains(postId))
            {
                continue;
            }

            if (bestScoreByPost[postId] != score)
            {
                continue;
            }

            alreadyAdded.Add(postId);
            result.Add(postId);
        }

        return result;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[][][] feeds1 =
[
    [
        [101, 95],
        [102, 90],
        [103, 80]
    ],
    [
        [104, 99],
        [101, 97],
        [105, 70]
    ],
    [
        [102, 96],
        [106, 88]
    ]
];

int m1 = 4;
var result1 = solution.MergeLiveRankings(feeds1, m1);
Console.WriteLine("Example 1 Output: [" + string.Join(", ", result1) + "]");

// Expected: [104, 101, 102, 106]

// Example 2
int[][][] feeds2 =
[
    [
        [7, 50],
        [8, 40],
        [9, 30]
    ],
    [
        [8, 60],
        [10, 55]
    ],
    [
        [7, 60],
        [11, 20]
    ]
];

int m2 = 3;
var result2 = solution.MergeLiveRankings(feeds2, m2);
Console.WriteLine("Example 2 Output: [" + string.Join(", ", result2) + "]");

// Expected: [7, 8, 10]