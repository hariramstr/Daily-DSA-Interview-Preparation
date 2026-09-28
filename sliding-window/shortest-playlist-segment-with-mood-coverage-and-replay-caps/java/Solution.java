import java.util.*;

/*
 * Title: Shortest Playlist Segment With Mood Coverage and Replay Caps
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A music streaming team is analyzing a generated playlist. Each song belongs to exactly one mood category,
 * represented by an integer in the array moods, where moods[i] is the mood of the i-th song.
 * You are also given a dictionary required where required[x] is the minimum number of songs of mood x
 * that must appear in a segment, and an integer cap.
 *
 * Find the length of the shortest contiguous segment of the playlist such that:
 * 1. For every mood x in required, the segment contains at least required[x] songs of mood x.
 * 2. No mood appears more than cap times inside the segment, including moods that are not listed in required.
 *
 * Return the minimum possible length of such a segment, or -1 if no valid segment exists.
 *
 * This is a hard sliding window problem because the window must simultaneously satisfy lower bounds for some
 * categories and a global upper bound for all categories. A window may fail because it is missing required moods,
 * or because some mood appears too often and forces the left boundary to move.
 *
 * Constraints:
 * - 1 <= moods.length <= 2 * 10^5
 * - 1 <= moods[i] <= 10^9
 * - 1 <= required.size <= 2 * 10^5
 * - 1 <= required[x] <= cap <= moods.length
 * - The sum of distinct moods in moods and required can be large, so solutions depending on small value ranges
 *   are not allowed.
 *
 * Example 1:
 * Input: moods = [4,1,2,1,3,2,1,4], required = {1:2, 2:1, 3:1}, cap = 3
 * Output: 5
 * Explanation: The segment [1,2,1,3,2] has counts {1:2, 2:2, 3:1}. It satisfies all required minimums,
 * and no mood appears more than 3 times. No valid segment of length 4 exists.
 *
 * Example 2:
 * Input: moods = [5,5,1,2,5,3,1,2], required = {1:1, 2:1, 3:1}, cap = 2
 * Output: 4
 * Explanation: The segment [2,5,3,1] is valid with counts {2:1, 5:1, 3:1, 1:1}. The segment [5,3,1,2]
 * is also valid. Any shorter segment misses at least one required mood.
 */

public class Solution {

    /**
     * Finds the minimum length of a contiguous segment that:
     * 1) contains at least the required number of each required mood, and
     * 2) contains no mood more than {@code cap} times.
     *
     * Core idea:
     * We use a classic sliding window with two pointers.
     *
     * The right pointer expands the window one song at a time.
     * The left pointer moves right only when needed:
     * - first, to repair any cap violation (some mood count became cap + 1),
     * - then, once the window is valid, to shrink away unnecessary extra songs while preserving validity.
     *
     * We maintain:
     * - current frequency of every mood in the window,
     * - how many required moods are currently satisfied.
     *
     * A required mood x is considered "satisfied" when windowCount[x] >= required[x].
     * When all required moods are satisfied and no cap is violated, the window is valid.
     *
     * @param moods the playlist moods array
     * @param required map from mood value to minimum required count in the segment
     * @param cap maximum allowed occurrences of any mood inside the segment
     * @return the minimum valid segment length, or -1 if no such segment exists
     *
     * Time complexity: O(n), where n = moods.length, because each pointer moves at most n times
     * and each map operation is O(1) on average.
     * Space complexity: O(d), where d is the number of distinct moods appearing in the window/maps.
     */
    public int shortestPlaylistSegment(int[] moods, Map<Integer, Integer> required, int cap) {
        int n = moods.length;

        // Quick impossibility check:
        // If any required minimum is greater than cap, no valid segment can ever exist,
        // because the same mood would need to appear more than the allowed maximum.
        for (int need : required.values()) {
            if (need > cap) {
                return -1;
            }
        }

        // Frequency map for the current sliding window [left, right].
        Map<Integer, Integer> windowCount = new HashMap<>();

        // Number of distinct required moods that are currently satisfied.
        // A required mood is satisfied if windowCount[mood] >= required[mood].
        int satisfiedKinds = 0;

        // Total number of distinct required moods we must satisfy.
        int totalRequiredKinds = required.size();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int answer = Integer.MAX_VALUE;

        // Expand the window by moving right from 0 to n - 1.
        for (int right = 0; right < n; right++) {
            int mood = moods[right];

            // Add the new mood at position right into the window.
            int newCount = windowCount.getOrDefault(mood, 0) + 1;
            windowCount.put(mood, newCount);

            // If this mood is one of the required moods, check whether we just reached
            // its required threshold exactly now.
            //
            // Example:
            // required[1] = 2
            // previous count was 1, new count is 2 -> now mood 1 becomes satisfied.
            Integer need = required.get(mood);
            if (need != null && newCount == need) {
                satisfiedKinds++;
            }

            // ------------------------------------------------------------
            // STEP 1: Repair cap violations.
            // ------------------------------------------------------------
            // If adding moods[right] caused its count to exceed cap,
            // then the current window is invalid and we MUST move left
            // until that mood's count is back to <= cap.
            //
            // Important observation:
            // Only the newly added mood can newly violate the cap,
            // because all other counts were already <= cap before this step.
            while (windowCount.get(mood) > cap) {
                int leftMood = moods[left];
                int leftCount = windowCount.get(leftMood);

                // Before removing leftMood, if leftMood is required and currently exactly at its threshold,
                // then removing one copy will make it unsatisfied.
                Integer leftNeed = required.get(leftMood);
                if (leftNeed != null && leftCount == leftNeed) {
                    satisfiedKinds--;
                }

                // Remove one occurrence of leftMood from the window.
                if (leftCount == 1) {
                    windowCount.remove(leftMood);
                } else {
                    windowCount.put(leftMood, leftCount - 1);
                }

                left++;
            }

            // ------------------------------------------------------------
            // STEP 2: If the window satisfies all required moods and cap is already respected,
            // try to shrink from the left while keeping the window valid.
            // ------------------------------------------------------------
            //
            // Since we already repaired cap violations above, every mood count is now <= cap.
            // So validity now depends only on whether all required moods are satisfied.
            //
            // We can safely remove a leftmost song if:
            // - it is not required at all, OR
            // - it is required but currently appears more times than needed.
            //
            // We must stop shrinking when removing the leftmost song would break a required minimum.
            while (satisfiedKinds == totalRequiredKinds) {
                // Current window [left, right] is valid, so update answer.
                answer = Math.min(answer, right - left + 1);

                int leftMood = moods[left];
                int leftCount = windowCount.get(leftMood);
                Integer leftNeed = required.get(leftMood);

                // Decide whether we can remove moods[left] and still remain valid.
                boolean removable;

                if (leftNeed == null) {
                    // This mood is not required at all, so removing it cannot hurt required coverage.
                    removable = true;
                } else {
                    // This mood is required.
                    // It is removable only if we currently have strictly more than needed.
                    removable = leftCount > leftNeed;
                }

                if (!removable) {
                    // If it is not removable, then this is the smallest valid window
                    // ending at 'right'. We cannot shrink further.
                    break;
                }

                // Remove the leftmost mood because it is safe to do so.
                if (leftCount == 1) {
                    windowCount.remove(leftMood);
                } else {
                    windowCount.put(leftMood, leftCount - 1);
                }

                left++;
            }
        }

        return answer == Integer.MAX_VALUE ? -1 : answer;
    }

    /**
     * Helper method to build a map from alternating key/value integers.
     *
     * Example:
     * buildRequiredMap(1, 2, 2, 1, 3, 1) creates the map {1=2, 2=1, 3=1}
     *
     * @param pairs alternating mood and required count values
     * @return a map representing required minimum counts
     *
     * Time complexity: O(k), where k is the number of integers in pairs
     * Space complexity: O(m), where m is the number of key/value pairs
     */
    public static Map<Integer, Integer> buildRequiredMap(int... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Pairs length must be even: mood, count, mood, count, ...");
        }

        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(d) per demonstrated test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] moods1 = {4, 1, 2, 1, 3, 2, 1, 4};
        Map<Integer, Integer> required1 = buildRequiredMap(1, 2, 2, 1, 3, 1);
        int cap1 = 3;
        int result1 = solution.shortestPlaylistSegment(moods1, required1, cap1);
        System.out.println(result1); // Expected: 5

        // Example 2
        int[] moods2 = {5, 5, 1, 2, 5, 3, 1, 2};
        Map<Integer, Integer> required2 = buildRequiredMap(1, 1, 2, 1, 3, 1);
        int cap2 = 2;
        int result2 = solution.shortestPlaylistSegment(moods2, required2, cap2);
        System.out.println(result2); // Expected: 4

        // Additional quick sanity checks
        int[] moods3 = {1, 1, 1};
        Map<Integer, Integer> required3 = buildRequiredMap(1, 2);
        int cap3 = 2;
        System.out.println(solution.shortestPlaylistSegment(moods3, required3, cap3)); // Expected: 2

        int[] moods4 = {1, 2, 3};
        Map<Integer, Integer> required4 = buildRequiredMap(1, 1, 4, 1);
        int cap4 = 1;
        System.out.println(solution.shortestPlaylistSegment(moods4, required4, cap4)); // Expected: -1
    }
}