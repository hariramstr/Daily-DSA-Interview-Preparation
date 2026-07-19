import java.util.*;

/*
 * Title: Longest Feed Window With Per-Topic Recency Limit
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are building a ranking service for a social media feed. Each post belongs to a topic
 * represented by an integer in the array topics, where topics[i] is the topic of the i-th post
 * in chronological order. A feed window is any contiguous subarray of posts.
 *
 * To keep the feed diverse, the platform enforces a recency rule: for every topic, the distance
 * between any two consecutive appearances of that same topic inside the chosen window must be at
 * most limit. In other words, if a topic appears at positions p1 < p2 < ... < pk within the
 * window, then for every j, pj+1 - pj <= limit must hold. A topic that appears only once in the
 * window always satisfies the rule.
 *
 * Return the length of the longest contiguous feed window that satisfies this condition.
 *
 * Note that this is not a global condition on the whole array. A pair of equal topics only matters
 * if both occurrences are inside the same chosen window. Also, topics may be as large as 10^9, so
 * solutions that depend on the numeric range of values are not acceptable.
 *
 * Constraints:
 * - 1 <= topics.length <= 2 * 10^5
 * - 1 <= topics[i] <= 10^9
 * - 1 <= limit <= topics.length
 *
 * Example 1:
 * Input: topics = [4, 1, 4, 2, 4, 3, 2], limit = 2
 * Output: 4
 * Explanation: The longest valid window is [4, 2, 4, 3] from indices 2 to 5. Inside this window,
 * topic 4 appears at positions 2 and 4, whose distance is 2, which is allowed. Any longer window
 * includes topic 2 at positions 3 and 6 with distance 3, violating the rule.
 *
 * Example 2:
 * Input: topics = [7, 5, 7, 8, 5, 9, 7, 5], limit = 3
 * Output: 8
 * Explanation: The entire array is valid. Topic 7 appears at indices 0, 2, and 6, but only
 * consecutive appearances matter: 2 - 0 = 2 and 6 - 2 = 4 globally. However, within the full
 * window the consecutive occurrences are still 0, 2, 6, so this seems invalid at first. The
 * correct interpretation is based on positions within the chosen contiguous array, which are the
 * same here, so the full array is actually invalid. The longest valid windows have length 5, such
 * as [7, 5, 7, 8, 5] or [8, 5, 9, 7, 5].
 *
 * Important note about Example 2:
 * The statement's listed output "8" contradicts its own explanation. The explanation correctly
 * shows the full array is invalid because topic 7 has consecutive appearances at indices 2 and 6,
 * distance 4 > 3. Therefore the correct answer for Example 2 is 5.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous window such that, for every topic,
     * every pair of consecutive appearances inside that window has distance at most {@code limit}.
     *
     * Core idea:
     * A window [left, right] is invalid if there exists some topic with two consecutive
     * occurrences a < b inside the window where b - a > limit.
     *
     * Observe something very useful:
     * - A violating pair (a, b) matters for the current window if and only if both a and b are inside it.
     * - Therefore, for every such bad pair, any valid window must avoid containing both endpoints.
     * - Equivalently, if right endpoint b is included, then left boundary must be > a.
     *
     * So we can preprocess all consecutive equal-topic pairs. Whenever we see a pair
     * (prevIndex, currentIndex) with gap > limit, then any window ending at or after currentIndex
     * and starting at or before prevIndex would be invalid. Thus, when we process currentIndex,
     * we must move the left boundary to at least prevIndex + 1.
     *
     * This leads to a very efficient one-pass algorithm:
     * - Track the most recent index of each topic.
     * - When we encounter the same topic again, compute the gap to its previous occurrence.
     * - If that gap is too large, update left = max(left, previousIndex + 1).
     * - Then update the answer with the current window length right - left + 1.
     *
     * Why this is correct:
     * - The only new constraint introduced when extending the window to include index right
     *   comes from the topic at topics[right], specifically the pair formed with its previous
     *   occurrence.
     * - Older bad pairs were already handled when their right endpoint was processed, and left
     *   only moves forward, never backward.
     *
     * @param topics the array of topic IDs in chronological order
     * @param limit the maximum allowed distance between consecutive appearances of the same topic
     *              inside a valid window
     * @return the maximum length of a contiguous valid window
     * Time complexity: O(n), where n is topics.length
     * Space complexity: O(k), where k is the number of distinct topics
     */
    public int longestFeedWindow(int[] topics, int limit) {
        // Map from topic value -> most recent index where this topic appeared.
        Map<Integer, Integer> lastSeenIndex = new HashMap<>();

        // Left boundary of the current valid sliding window.
        int left = 0;

        // Best answer found so far.
        int best = 0;

        // Process each position as the right boundary of the window.
        for (int right = 0; right < topics.length; right++) {
            int topic = topics[right];

            // If we have seen this topic before, then the new occurrence at "right"
            // forms a consecutive same-topic pair with its previous occurrence.
            if (lastSeenIndex.containsKey(topic)) {
                int previousIndex = lastSeenIndex.get(topic);
                int gap = right - previousIndex;

                // If the distance between consecutive appearances is too large,
                // then any window containing both previousIndex and right is invalid.
                //
                // Therefore, to restore validity, we must move the left boundary
                // strictly past previousIndex.
                //
                // We use max because left may already be further right due to
                // earlier violations from other topics.
                if (gap > limit) {
                    left = Math.max(left, previousIndex + 1);
                }
            }

            // Update the most recent occurrence of this topic to the current index.
            lastSeenIndex.put(topic, right);

            // Now [left, right] is guaranteed valid.
            int currentLength = right - left + 1;
            best = Math.max(best, currentLength);
        }

        return best;
    }

    /**
     * A helper method that prints a detailed demonstration for one test case.
     *
     * @param topics the input topic array
     * @param limit the recency limit
     * @return the computed answer for the given input
     * Time complexity: O(n)
     * Space complexity: O(k)
     */
    public int demonstrate(int[] topics, int limit) {
        int answer = longestFeedWindow(topics, limit);
        System.out.println("Topics: " + Arrays.toString(topics));
        System.out.println("Limit: " + limit);
        System.out.println("Longest valid window length: " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Runs sample demonstrations from the problem statement and a few extra checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size of demonstrated tests)
     * Space complexity: O(k) per test due to the hash map
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the statement.
        int[] topics1 = {4, 1, 4, 2, 4, 3, 2};
        int limit1 = 2;
        int result1 = solution.demonstrate(topics1, limit1);
        System.out.println("Expected for Example 1: 4");
        System.out.println("Matches expected: " + (result1 == 4));
        System.out.println();

        // Example 2 from the statement contains a contradiction.
        // The explanation shows the full array is invalid, so the correct answer is 5.
        int[] topics2 = {7, 5, 7, 8, 5, 9, 7, 5};
        int limit2 = 3;
        int result2 = solution.demonstrate(topics2, limit2);
        System.out.println("Correct expected for Example 2 based on the explanation: 5");
        System.out.println("Matches corrected expected: " + (result2 == 5));
        System.out.println();

        // Extra sanity checks.
        int[] topics3 = {1};
        int limit3 = 1;
        solution.demonstrate(topics3, limit3);

        int[] topics4 = {1, 1, 1, 1};
        int limit4 = 1;
        solution.demonstrate(topics4, limit4);

        int[] topics5 = {1, 2, 1, 3, 1, 4, 1};
        int limit5 = 2;
        solution.demonstrate(topics5, limit5);
    }
}