/*
Title: Track Top K Hashtags in a Sliding Event Window
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
You are building analytics for a live social platform. Events arrive in non-decreasing timestamp order, and each event contains a single hashtag string. For every event, you must report the current top k hashtags that appeared within the last windowSize seconds, inclusive of the current timestamp and excluding events older than currentTime - windowSize + 1.

The ranking rules are:
1. A hashtag with higher frequency in the active window ranks higher.
2. If two hashtags have the same frequency, the lexicographically smaller hashtag ranks higher.

Return the top k hashtags after processing each event, as a list of lists. If fewer than k distinct hashtags are present in the active window, return all of them in ranked order for that step.

Because old events expire as time advances, a hashtag's count can both increase and decrease over time. A hashtag's count can both increase and decrease over time, so recomputing all frequencies from scratch after every event would be too slow. A common efficient approach uses:
- a queue to remember events in arrival order so expired events can be removed quickly
- a heap (priority queue) with lazy deletion so ranking updates remain efficient

Constraints:
- 1 <= n <= 100000, where n is the number of events
- 1 <= windowSize <= 10^9
- 1 <= k <= 100
- timestamps.length == hashtags.length == n
- 1 <= timestamps[i] <= 10^9
- timestamps is sorted in non-decreasing order
- 1 <= hashtags[i].length <= 20
- hashtags[i] contains only lowercase English letters
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    // We store each incoming event in a queue so we can evict expired events
    // from the left side of the sliding window in O(1) amortized time.
    private record EventItem(int Time, string Tag);

    // This record represents one snapshot pushed into the heap.
    // Why do we need a snapshot?
    // Because a hashtag's count changes over time. Instead of trying to update
    // an existing heap node in-place (which is awkward with standard priority queues),
    // we push a fresh snapshot every time the count changes.
    // Later, when we pop from the heap, we verify whether that snapshot still matches
    // the current real count in the dictionary. If not, it is stale and we discard it.
    private record HeapEntry(string Tag, int Count);

    // This comparer defines the ranking order we want:
    // 1) Higher frequency ranks better
    // 2) If frequencies tie, lexicographically smaller string ranks better
    //
    // We use this comparer both:
    // - to sort final extracted candidates
    // - to compare heap entries when needed
    private sealed class RankComparer : IComparer<HeapEntry>
    {
        public int Compare(HeapEntry? x, HeapEntry? y)
        {
            if (ReferenceEquals(x, y)) return 0;
            if (x is null) return 1;
            if (y is null) return -1;

            // Higher count should come first.
            int byCount = y.Count.CompareTo(x.Count);
            if (byCount != 0) return byCount;

            // For equal count, lexicographically smaller tag should come first.
            return string.CompareOrdinal(x.Tag, y.Tag);
        }
    }

    // Time Complexity:
    // Let n be the number of events.
    // - Each event is added once to the queue and removed once: O(n) total queue work.
    // - Each count change pushes one heap snapshot. Each event causes one increment,
    //   and each expiration causes one decrement, so total heap pushes are O(n).
    // - Heap pops for lazy deletion are amortized O(n) across the whole run.
    // - For each event, we extract up to k valid top hashtags. Since k <= 100,
    //   this is efficient in practice.
    // Overall: O(n log n + n * k log n) in the worst case.
    //
    // Space Complexity:
    // - Queue can hold up to O(n) events in the worst case.
    // - Dictionary holds active distinct hashtags: O(n) worst case.
    // - Heap may contain stale snapshots, up to O(n) total.
    // Overall: O(n).
    public IList<IList<string>> TopKHashtagsSlidingWindow(int[] timestamps, string[] hashtags, int windowSize, int k)
    {
        int n = timestamps.Length;

        // This will store the answer after each processed event.
        var result = new List<IList<string>>(n);

        // Queue of active events in arrival order.
        // This is exactly what we need for a sliding window:
        // the oldest event is always the first candidate to expire.
        var window = new Queue<EventItem>();

        // Current true frequencies inside the active window.
        // Key   = hashtag
        // Value = current count in the active window
        var counts = new Dictionary<string, int>();

        // PriorityQueue in .NET is a min-heap by priority.
        // We want the "best" ranked hashtag to come out first.
        //
        // Trick:
        // We use the HeapEntry itself as both the element and the priority,
        // and provide a comparer where "better ranked" entries compare as smaller.
        // That makes the min-heap behave like a max-heap according to our ranking rules.
        var heap = new PriorityQueue<HeapEntry, HeapEntry>(new RankComparer());

        for (int i = 0; i < n; i++)
        {
            int currentTime = timestamps[i];
            string currentTag = hashtags[i];

            // ------------------------------------------------------------
            // STEP 1: Add the current event into the sliding window.
            // ------------------------------------------------------------
            //
            // Why?
            // The problem asks for the top k hashtags AFTER processing each event.
            // So the current event must be included before we compute the answer.
            window.Enqueue(new EventItem(currentTime, currentTag));

            if (!counts.TryAdd(currentTag, 1))
            {
                counts[currentTag]++;
            }

            // Push a fresh heap snapshot reflecting the new count of this hashtag.
            // We do not remove older snapshots here; they become stale and will be
            // ignored later by lazy deletion.
            heap.Enqueue(new HeapEntry(currentTag, counts[currentTag]), new HeapEntry(currentTag, counts[currentTag]));

            // ------------------------------------------------------------
            // STEP 2: Evict expired events from the left side of the window.
            // ------------------------------------------------------------
            //
            // Active window definition:
            // [currentTime - windowSize + 1 .. currentTime]
            //
            // Therefore, any event with time < currentTime - windowSize + 1
            // is too old and must be removed.
            int windowStart = currentTime - windowSize + 1;

            while (window.Count > 0 && window.Peek().Time < windowStart)
            {
                var expired = window.Dequeue();

                // Decrease the count of the expired hashtag because it is no longer
                // inside the active window.
                counts[expired.Tag]--;

                if (counts[expired.Tag] == 0)
                {
                    // Remove zero-count hashtags from the dictionary.
                    // This keeps the dictionary representing only active hashtags.
                    counts.Remove(expired.Tag);
                }
                else
                {
                    // If the hashtag still exists in the window, push a new snapshot
                    // with its decreased count.
                    heap.Enqueue(new HeapEntry(expired.Tag, counts[expired.Tag]), new HeapEntry(expired.Tag, counts[expired.Tag]));
                }

                // Important note:
                // If the count became zero, we do NOT push a zero-count snapshot.
                // Zero-count hashtags should not appear in the ranking.
                // Any older positive snapshots for this tag will later be recognized
                // as stale because the dictionary no longer contains the tag.
            }

            // ------------------------------------------------------------
            // STEP 3: Extract the current top k hashtags using lazy deletion.
            // ------------------------------------------------------------
            //
            // Why is lazy deletion needed?
            // Because the heap contains many historical snapshots for the same hashtag.
            // Example:
            //   ai had count 1, then 2, then 1 again
            // The heap may contain all three snapshots.
            //
            // Only the snapshot matching the CURRENT dictionary count is valid.
            // Any other snapshot is stale and must be discarded when encountered.
            //
            // We temporarily remove valid top entries from the heap, record them,
            // then push them back so the heap remains available for the next event.
            var topTags = new List<string>();
            var extractedValidEntries = new List<HeapEntry>();

            while (topTags.Count < k && heap.Count > 0)
            {
                var candidate = heap.Dequeue();

                // Check whether this heap snapshot is still valid.
                //
                // It is valid if:
                // 1) the hashtag still exists in the active counts dictionary
                // 2) the current true count equals the snapshot count
                //
                // If either condition fails, this snapshot is stale and must be ignored.
                if (!counts.TryGetValue(candidate.Tag, out int actualCount) || actualCount != candidate.Count)
                {
                    continue;
                }

                // This is a valid current ranking entry.
                topTags.Add(candidate.Tag);
                extractedValidEntries.Add(candidate);
            }

            // Push valid extracted entries back into the heap so future iterations
            // can still use them.
            foreach (var entry in extractedValidEntries)
            {
                heap.Enqueue(entry, entry);
            }

            result.Add(topTags);
        }

        return result;
    }
}

static string FormatNestedList(IList<IList<string>> data)
{
    return "[" + string.Join(", ", data.Select(inner => "[" + string.Join(", ", inner.Select(s => $"\"{s}\"")) + "]")) + "]";
}

var solution = new Solution();

// Example 1
int[] timestamps1 = { 1, 2, 3, 6, 7 };
string[] hashtags1 = { "ai", "ml", "ai", "db", "ai" };
int windowSize1 = 5;
int k1 = 2;

var result1 = solution.TopKHashtagsSlidingWindow(timestamps1, hashtags1, windowSize1, k1);
Console.WriteLine("Example 1 Output:");
Console.WriteLine(FormatNestedList(result1));
Console.WriteLine("Expected:");
Console.WriteLine("[[\"ai\"], [\"ai\", \"ml\"], [\"ai\", \"ml\"], [\"ai\", \"db\"], [\"ai\", \"db\"]]");
Console.WriteLine();

// Example 2
int[] timestamps2 = { 4, 4, 5, 8, 8, 9 };
string[] hashtags2 = { "red", "blue", "red", "green", "blue", "blue" };
int windowSize2 = 3;
int k2 = 3;

var result2 = solution.TopKHashtagsSlidingWindow(timestamps2, hashtags2, windowSize2, k2);
Console.WriteLine("Example 2 Output:");
Console.WriteLine(FormatNestedList(result2));
Console.WriteLine("Expected:");
Console.WriteLine("[[\"red\"], [\"blue\", \"red\"], [\"red\", \"blue\"], [\"green\"], [\"blue\", \"green\"], [\"blue\", \"green\"]]");