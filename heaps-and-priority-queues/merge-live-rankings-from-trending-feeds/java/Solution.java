import java.util.*;

/*
Problem Title: Merge Live Rankings from Trending Feeds

Problem Description:
A social media platform receives multiple already-sorted trending feeds from different regions.
Each regional feed is sorted in descending order by score, where a higher score means a post is
more trending. You are given k feeds, where each feed contains post records in the form
[postId, score]. The same postId may appear in multiple feeds with different scores because
regions rank posts independently.

Your task is to build a single global ranking of the top m unique posts. For every postId,
its global score is defined as the maximum score it achieves in any regional feed. Return the
top m unique postIds sorted by global score in descending order. If two posts have the same
global score, the smaller postId should come first.

You should design an efficient solution using heaps or priority queues. A brute-force approach
that flattens all feeds and sorts everything may be too slow when the total number of feed
entries is large.

Constraints:
- 1 <= k <= 10^4
- 1 <= total number of feed entries across all feeds <= 2 * 10^5
- 1 <= length of each feed <= 2 * 10^5
- 1 <= postId <= 10^9
- 1 <= score <= 10^9
- 1 <= m <= number of distinct postIds across all feeds
- Each individual feed is already sorted by score in descending order

Examples:
Example 1:
feeds = [
  [[101, 95], [102, 90], [103, 80]],
  [[104, 99], [101, 97], [105, 70]],
  [[102, 96], [106, 88]]
], m = 4
Output: [104, 101, 102, 106]

Example 2:
feeds = [
  [[7, 50], [8, 40], [9, 30]],
  [[8, 60], [10, 55]],
  [[7, 60], [11, 20]]
], m = 3
Output: [7, 8, 10]
*/

public class Solution {

    /**
     * Small helper object representing one entry currently visible from a feed.
     * It stores:
     * - which feed it came from
     * - which index inside that feed
     * - the post id
     * - the score
     */
    private static class FeedEntry {
        int feedIndex;
        int entryIndex;
        int postId;
        int score;

        FeedEntry(int feedIndex, int entryIndex, int postId, int score) {
            this.feedIndex = feedIndex;
            this.entryIndex = entryIndex;
            this.postId = postId;
            this.score = score;
        }
    }

    /**
     * Computes the top m unique postIds by global score.
     *
     * Core idea:
     * 1. Use a max-heap to perform a k-way merge over the already-sorted feeds.
     * 2. As entries are popped from highest score to lowest score, the first time we see a postId,
     *    that score is guaranteed to be its maximum global score.
     * 3. We collect unique posts in that order until we have m results.
     *
     * Why the first time a postId is seen gives its global maximum:
     * - Every feed is sorted descending by score.
     * - The heap always exposes the highest currently unseen score among all feeds.
     * - Therefore, entries are processed globally from high score to low score.
     * - So the first occurrence of a postId must be its highest score across all feeds.
     *
     * Tie handling:
     * - The final answer must be sorted by global score descending.
     * - If scores tie, smaller postId comes first.
     * - To guarantee this directly during extraction, the heap comparator breaks ties by smaller postId.
     *
     * @param feeds list of feeds; each feed is a list of [postId, score] pairs already sorted by score descending
     * @param m number of unique postIds to return
     * @return ordered list of the top m unique postIds
     *
     * Time complexity:
     * O((T_seen) log k), where T_seen is the number of feed entries popped until m unique posts are found.
     * In the worst case this is O(T log k), where T is the total number of feed entries.
     *
     * Space complexity:
     * O(k + u), where k is the number of feeds and u is the number of unique postIds collected/seen.
     */
    public List<Integer> mergeLiveRankings(List<List<int[]>> feeds, int m) {
        // Max-heap behavior is simulated by reversing the usual order:
        // 1. Higher score should come first.
        // 2. If scores are equal, smaller postId should come first.
        //
        // This tie-break is important because if two different posts have the same global score,
        // the smaller postId must appear earlier in the final result.
        PriorityQueue<FeedEntry> maxHeap = new PriorityQueue<>((a, b) -> {
            if (a.score != b.score) {
                return Integer.compare(b.score, a.score);
            }
            return Integer.compare(a.postId, b.postId);
        });

        // Push the first element from every non-empty feed into the heap.
        // Since each feed is already sorted descending, the first element is the best remaining
        // candidate from that feed.
        for (int i = 0; i < feeds.size(); i++) {
            List<int[]> feed = feeds.get(i);
            if (feed != null && !feed.isEmpty()) {
                int[] record = feed.get(0);
                maxHeap.offer(new FeedEntry(i, 0, record[0], record[1]));
            }
        }

        // Tracks which postIds have already been accepted into the global ranking.
        // The first time we encounter a postId in global descending order, that score is its maximum.
        Set<Integer> seenPostIds = new HashSet<>();

        // This will store the final ordered answer.
        List<Integer> result = new ArrayList<>();

        // Repeatedly extract the best currently available entry.
        while (!maxHeap.isEmpty() && result.size() < m) {
            FeedEntry current = maxHeap.poll();

            // If this postId has not been seen before, then this is the highest score
            // we will ever see for this postId across all feeds.
            //
            // Why?
            // Because the heap gives entries in descending score order globally.
            // Any later occurrence of the same postId can only have the same or lower score.
            if (seenPostIds.add(current.postId)) {
                result.add(current.postId);
            }

            // Advance within the same feed:
            // after consuming feed[current.feedIndex][current.entryIndex],
            // the next candidate from that feed is at entryIndex + 1.
            int nextIndex = current.entryIndex + 1;
            List<int[]> sameFeed = feeds.get(current.feedIndex);
            if (nextIndex < sameFeed.size()) {
                int[] nextRecord = sameFeed.get(nextIndex);
                maxHeap.offer(new FeedEntry(
                        current.feedIndex,
                        nextIndex,
                        nextRecord[0],
                        nextRecord[1]
                ));
            }
        }

        return result;
    }

    /**
     * Convenience overload for callers who prefer a 3D int array input.
     *
     * Input format:
     * feeds[i][j][0] = postId
     * feeds[i][j][1] = score
     *
     * @param feeds 3D array of feeds, each feed sorted by score descending
     * @param m number of unique postIds to return
     * @return ordered list of the top m unique postIds
     *
     * Time complexity:
     * O((T_seen) log k) after conversion, worst-case O(T log k)
     *
     * Space complexity:
     * O(T) for conversion plus O(k + u) for the algorithm
     */
    public List<Integer> mergeLiveRankings(int[][][] feeds, int m) {
        List<List<int[]>> converted = new ArrayList<>();

        // Convert the array input into the List-based structure used by the main algorithm.
        for (int[][] feed : feeds) {
            List<int[]> currentFeed = new ArrayList<>();
            for (int[] record : feed) {
                currentFeed.add(new int[]{record[0], record[1]});
            }
            converted.add(currentFeed);
        }

        return mergeLiveRankings(converted, m);
    }

    /**
     * Builds a readable string for a list of integers.
     *
     * @param values list of integers
     * @return string representation such as [1, 2, 3]
     *
     * Time complexity:
     * O(n)
     *
     * Space complexity:
     * O(n) for the produced string
     */
    public static String listToString(List<Integer> values) {
        return values.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     *
     * Time complexity:
     * Depends on the sample sizes; effectively constant for this demo
     *
     * Space complexity:
     * Depends on the sample sizes; effectively constant for this demo
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[][][] feeds1 = {
                { {101, 95}, {102, 90}, {103, 80} },
                { {104, 99}, {101, 97}, {105, 70} },
                { {102, 96}, {106, 88} }
        };
        int m1 = 4;
        List<Integer> result1 = solution.mergeLiveRankings(feeds1, m1);
        System.out.println("Example 1 Output: " + listToString(result1));
        System.out.println("Expected: [104, 101, 102, 106]");

        // Example 2
        int[][][] feeds2 = {
                { {7, 50}, {8, 40}, {9, 30} },
                { {8, 60}, {10, 55} },
                { {7, 60}, {11, 20} }
        };
        int m2 = 3;
        List<Integer> result2 = solution.mergeLiveRankings(feeds2, m2);
        System.out.println("Example 2 Output: " + listToString(result2));
        System.out.println("Expected: [7, 8, 10]");
    }
}