import java.util.*;

/*
 * Title: Track Top K Hashtags in a Sliding Event Window
 * Difficulty: Medium
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * You are building analytics for a live social platform. Events arrive in non-decreasing timestamp order,
 * and each event contains a single hashtag string. For every event, you must report the current top k hashtags
 * that appeared within the last windowSize seconds, inclusive of the current timestamp and excluding events
 * older than currentTime - windowSize + 1.
 *
 * The ranking rules are:
 * 1. A hashtag with higher frequency in the active window ranks higher.
 * 2. If two hashtags have the same frequency, the lexicographically smaller hashtag ranks higher.
 *
 * Return the top k hashtags after processing each event, as a list of lists. If fewer than k distinct hashtags
 * are present in the active window, return all of them in ranked order for that step.
 *
 * Because old events expire as time advances, a hashtag's count can both increase and decrease over time.
 * A solution must be efficient enough for large streams, so recomputing all frequencies from scratch after
 * every event will be too slow. A common approach uses a queue to evict expired events and a heap
 * (priority queue) with lazy deletion to maintain rankings.
 *
 * Constraints:
 * - 1 <= n <= 100000, where n is the number of events
 * - 1 <= windowSize <= 10^9
 * - 1 <= k <= 100
 * - timestamps.length == hashtags.length == n
 * - 1 <= timestamps[i] <= 10^9
 * - timestamps is sorted in non-decreasing order
 * - 1 <= hashtags[i].length <= 20
 * - hashtags[i] contains only lowercase English letters
 *
 * Example 1:
 * Input:
 * timestamps = [1, 2, 3, 6, 7]
 * hashtags = ["ai", "ml", "ai", "db", "ai"]
 * windowSize = 5
 * k = 2
 * Output:
 * [["ai"], ["ai", "ml"], ["ai", "ml"], ["ai", "db"], ["ai", "db"]]
 *
 * Example 2:
 * Input:
 * timestamps = [4, 4, 5, 8, 8, 9]
 * hashtags = ["red", "blue", "red", "green", "blue", "blue"]
 * windowSize = 3
 * k = 3
 * Output:
 * [["red"], ["blue", "red"], ["red", "blue"], ["green", "red"], ["blue", "green"], ["blue", "green"]]
 */

public class Solution {

    /**
     * Small record-like class representing one incoming event.
     * We store both timestamp and hashtag so that when the event expires from the sliding window,
     * we know exactly which hashtag count must be decremented.
     */
    private static class Event {
        int timestamp;
        String hashtag;

        Event(int timestamp, String hashtag) {
            this.timestamp = timestamp;
            this.hashtag = hashtag;
        }
    }

    /**
     * Heap entry used for ranking hashtags.
     *
     * Important idea:
     * We use "lazy deletion". That means when a hashtag's count changes, we do NOT try to remove
     * its old heap entry from the priority queue, because removing arbitrary elements from a Java
     * PriorityQueue is too expensive.
     *
     * Instead, we simply push a fresh entry with the new count.
     * Later, when an entry reaches the top of the heap, we compare it against the current true count
     * stored in the frequency map:
     * - if it matches, it is valid
     * - if it does not match, it is stale and we discard it
     */
    private static class HeapEntry {
        String hashtag;
        int count;

        HeapEntry(String hashtag, int count) {
            this.hashtag = hashtag;
            this.count = count;
        }
    }

    /**
     * Processes the stream of hashtag events and returns the top k hashtags after each event.
     *
     * Core strategy:
     * 1. Maintain a queue of active events currently inside the sliding time window.
     * 2. Maintain a frequency map: hashtag -> current count in the active window.
     * 3. Maintain a max-heap ordered by:
     *      - higher count first
     *      - lexicographically smaller hashtag first when counts tie
     * 4. Because counts can both increase and decrease, we use lazy deletion in the heap.
     * 5. After each event:
     *      - add the new event
     *      - evict expired events
     *      - read the top k valid hashtags from the heap
     *
     * Why this works:
     * - The queue guarantees correct expiration order because timestamps are non-decreasing.
     * - The map always stores the true current counts.
     * - The heap gives fast access to the best candidates.
     * - Lazy deletion avoids expensive heap updates.
     *
     * @param timestamps sorted event timestamps in non-decreasing order
     * @param hashtags hashtag for each event
     * @param windowSize size of the inclusive sliding time window
     * @param k number of top hashtags to report after each event
     * @return a list where result[i] contains the ranked top k hashtags after processing event i
     * Time complexity: O(n log n + n * k * log n) in the worst case due to lazy heap cleanup and extracting top k each step.
     * Since k <= 100, this is efficient in practice for the given constraints.
     * Space complexity: O(n) in the worst case for the event queue, frequency map, and heap.
     */
    public List<List<String>> topKHashtagsInSlidingWindow(int[] timestamps, String[] hashtags, int windowSize, int k) {
        List<List<String>> result = new ArrayList<>();

        // Queue of events currently relevant for expiration.
        // The oldest event is always at the front.
        Deque<Event> window = new ArrayDeque<>();

        // Current true frequency of each hashtag inside the active window.
        Map<String, Integer> frequency = new HashMap<>();

        // Max-heap for ranking:
        // 1) larger count first
        // 2) if counts tie, lexicographically smaller hashtag first
        PriorityQueue<HeapEntry> maxHeap = new PriorityQueue<>((a, b) -> {
            if (a.count != b.count) {
                return Integer.compare(b.count, a.count);
            }
            return a.hashtag.compareTo(b.hashtag);
        });

        for (int i = 0; i < timestamps.length; i++) {
            int currentTime = timestamps[i];
            String currentHashtag = hashtags[i];

            // ------------------------------------------------------------
            // STEP 1: Add the current event into the active window.
            // ------------------------------------------------------------
            window.offerLast(new Event(currentTime, currentHashtag));

            // Increase the true frequency of the current hashtag.
            int newCount = frequency.getOrDefault(currentHashtag, 0) + 1;
            frequency.put(currentHashtag, newCount);

            // Push a fresh heap entry reflecting the updated count.
            // Older entries for the same hashtag may still remain in the heap,
            // but they will be ignored later if stale.
            maxHeap.offer(new HeapEntry(currentHashtag, newCount));

            // ------------------------------------------------------------
            // STEP 2: Evict events that are now too old for the current window.
            //
            // Active window is:
            // [currentTime - windowSize + 1, currentTime]
            //
            // So any event with timestamp < currentTime - windowSize + 1 expires.
            // ------------------------------------------------------------
            long windowStart = (long) currentTime - windowSize + 1L;

            while (!window.isEmpty() && window.peekFirst().timestamp < windowStart) {
                Event expired = window.pollFirst();

                // Decrease the true frequency for the expired hashtag.
                int updatedCount = frequency.get(expired.hashtag) - 1;

                if (updatedCount == 0) {
                    // Remove the hashtag completely if no active occurrences remain.
                    frequency.remove(expired.hashtag);
                } else {
                    // Otherwise store the reduced count.
                    frequency.put(expired.hashtag, updatedCount);

                    // Push the new count into the heap.
                    // Again, old entries remain but become stale.
                    maxHeap.offer(new HeapEntry(expired.hashtag, updatedCount));
                }
            }

            // ------------------------------------------------------------
            // STEP 3: Read the current top k hashtags.
            //
            // We must be careful:
            // - heap top may be stale
            // - we need up to k distinct valid hashtags in correct order
            //
            // Approach:
            // - repeatedly pop valid top entries
            // - temporarily store them
            // - add their hashtags to the answer
            // - push them back afterward so the heap remains usable for future steps
            // ------------------------------------------------------------
            List<String> topK = getTopK(maxHeap, frequency, k);
            result.add(topK);
        }

        return result;
    }

    /**
     * Extracts up to k highest-ranked valid hashtags from the heap using lazy deletion.
     *
     * Detailed behavior:
     * - While building the answer, we repeatedly inspect the heap top.
     * - If the top entry is stale, we discard it permanently.
     * - If the top entry is valid, we use it for the answer and temporarily remove it.
     * - After collecting up to k valid entries, we push those valid entries back into the heap.
     *
     * Why push them back?
     * - Because they are still valid candidates for future events.
     * - We only removed them temporarily to avoid returning the same hashtag multiple times
     *   during one extraction.
     *
     * What makes an entry valid?
     * - The hashtag must still exist in the frequency map.
     * - The entry's count must exactly match the current true count in the map.
     *
     * @param maxHeap heap containing both valid and stale ranking entries
     * @param frequency current true hashtag counts in the active window
     * @param k number of top hashtags to return
     * @return ranked list of up to k hashtags
     * Time complexity: O((s + k) log n), where s is the number of stale entries discarded during this call.
     * Space complexity: O(k) for temporarily storing valid extracted entries.
     */
    public List<String> getTopK(PriorityQueue<HeapEntry> maxHeap, Map<String, Integer> frequency, int k) {
        List<String> answer = new ArrayList<>();
        List<HeapEntry> usedValidEntries = new ArrayList<>();

        while (!maxHeap.isEmpty() && answer.size() < k) {
            HeapEntry top = maxHeap.poll();

            // Check the current true count from the frequency map.
            Integer trueCount = frequency.get(top.hashtag);

            // If the hashtag no longer exists in the active window, this heap entry is stale.
            if (trueCount == null) {
                continue;
            }

            // If the count does not match the current true count, this entry is stale.
            if (trueCount != top.count) {
                continue;
            }

            // This is the best currently valid hashtag.
            answer.add(top.hashtag);
            usedValidEntries.add(top);
        }

        // Put valid entries back so the heap remains available for future queries.
        for (HeapEntry entry : usedValidEntries) {
            maxHeap.offer(entry);
        }

        return answer;
    }

    /**
     * Utility method to print a nested list in a clean readable format.
     *
     * @param data nested list of strings
     * @return string representation of the nested list
     * Time complexity: O(total number of strings printed)
     * Space complexity: O(total output size)
     */
    public String formatNestedList(List<List<String>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < data.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(data.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: depends on the sample sizes; negligible for demonstration
     * Space complexity: depends on the sample sizes; negligible for demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] timestamps1 = {1, 2, 3, 6, 7};
        String[] hashtags1 = {"ai", "ml", "ai", "db", "ai"};
        int windowSize1 = 5;
        int k1 = 2;

        List<List<String>> result1 = solution.topKHashtagsInSlidingWindow(timestamps1, hashtags1, windowSize1, k1);
        System.out.println("Example 1 Output:");
        System.out.println(solution.formatNestedList(result1));
        System.out.println("Expected:");
        System.out.println("[[ai], [ai, ml], [ai, ml], [ai, db], [ai, db]]");
        System.out.println();

        int[] timestamps2 = {4, 4, 5, 8, 8, 9};
        String[] hashtags2 = {"red", "blue", "red", "green", "blue", "blue"};
        int windowSize2 = 3;
        int k2 = 3;

        List<List<String>> result2 = solution.topKHashtagsInSlidingWindow(timestamps2, hashtags2, windowSize2, k2);
        System.out.println("Example 2 Output:");
        System.out.println(solution.formatNestedList(result2));
        System.out.println("Expected:");
        System.out.println("[[red], [blue, red], [red, blue], [green, red], [blue, green], [blue, green]]");
    }
}