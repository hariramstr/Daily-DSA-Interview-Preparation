import java.util.*;

/*
 * Title: Longest Notification Feed With Cooldowned App Repeats
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array apps of length n, where apps[i] is the app ID that generated
 * the i-th notification in a user's chronological feed, and an integer cooldown.
 * A contiguous segment of the feed is called valid if, for every app ID, any two
 * occurrences of that same app inside the segment are more than cooldown positions apart.
 * In other words, if apps[i] == apps[j] and both indices belong to the chosen segment,
 * then |i - j| must be greater than cooldown.
 *
 * Your task is to return the length of the longest valid contiguous segment.
 *
 * This models a notification system where repeated alerts from the same app must be
 * sufficiently spaced apart to avoid overwhelming the user. The segment must remain
 * contiguous; you are not allowed to reorder or delete notifications.
 *
 * A segment of length 0 is allowed only implicitly, but the answer will always be at
 * least 1 when n > 0.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 1 <= apps[i] <= 1000000000
 * - 0 <= cooldown <= n
 *
 * Example 1:
 * Input: apps = [4, 1, 2, 4, 3, 1, 5], cooldown = 2
 * Output: 5
 * Explanation:
 * The segment [1, 2, 4, 3, 1] is valid. The two 1s are 4 positions apart, which is
 * greater than 2. No app repeats within distance 2 in this segment. No longer valid
 * segment exists.
 *
 * Example 2:
 * Input: apps = [7, 7, 8, 9, 7, 8, 10], cooldown = 3
 * Output: 4
 * Explanation:
 * A valid optimal segment is [9, 7, 8, 10], which is contiguous and has length 4.
 *
 * Key Observation:
 * A segment is valid exactly when, for every app ID, consecutive occurrences of that
 * app inside the segment are more than cooldown apart.
 *
 * Therefore, while scanning from left to right, if the current app was last seen at
 * index lastIndex and the distance currentIndex - lastIndex is <= cooldown, then the
 * current window becomes invalid unless we move the left boundary to lastIndex + 1.
 *
 * This leads to a classic O(n) sliding window solution using a hash map that stores
 * the most recent index of each app.
 */

public class Solution {

    /**
     * Computes the length of the longest valid contiguous segment of notifications.
     *
     * A segment is valid if any two equal app IDs inside that segment are more than
     * {@code cooldown} positions apart.
     *
     * We use a sliding window:
     * - {@code left} is the start of the current valid window.
     * - We expand the window by moving {@code right} from left to right.
     * - For each app ID, we remember its most recent index.
     * - If the same app appears again too soon (distance <= cooldown), then the current
     *   window is no longer valid unless we move {@code left} past the previous occurrence.
     *
     * @param apps the chronological notification feed, where apps[i] is the app ID at position i
     * @param cooldown the minimum forbidden repeat distance; equal app IDs must be more than this apart
     * @return the maximum length of any valid contiguous segment
     *
     * Time complexity: O(n), because each index is processed once.
     * Space complexity: O(n) in the worst case for the hash map of last seen positions.
     */
    public int longestValidSegment(int[] apps, int cooldown) {
        // Defensive handling for completeness.
        // The problem guarantees n >= 1, but this makes the method robust.
        if (apps == null || apps.length == 0) {
            return 0;
        }

        // Special case:
        // If cooldown == 0, then equal apps are allowed as long as their distance is > 0.
        // Any two distinct positions always have distance at least 1, and 1 > 0.
        // So every contiguous segment is valid, and the answer is the full array length.
        if (cooldown == 0) {
            return apps.length;
        }

        // Map from app ID -> most recent index where it appeared.
        Map<Integer, Integer> lastSeenIndex = new HashMap<>();

        // Left boundary of the current valid sliding window.
        int left = 0;

        // Best answer found so far.
        int best = 0;

        // Expand the window one element at a time.
        for (int right = 0; right < apps.length; right++) {
            int appId = apps[right];

            // If we have seen this app before, check whether that previous occurrence
            // is too close to the current one.
            if (lastSeenIndex.containsKey(appId)) {
                int previousIndex = lastSeenIndex.get(appId);

                // If right - previousIndex <= cooldown, then these two equal app IDs
                // are too close to coexist in the same valid segment.
                //
                // To restore validity, we must move the left boundary so that the old
                // occurrence is excluded from the window. The smallest such left is
                // previousIndex + 1.
                //
                // We use Math.max because left should never move backward.
                if (right - previousIndex <= cooldown) {
                    left = Math.max(left, previousIndex + 1);
                }
            }

            // Update the most recent position of this app ID to the current index.
            lastSeenIndex.put(appId, right);

            // The current window [left, right] is now valid.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is longer.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * Convenience wrapper that accepts a list of integers instead of an array.
     *
     * This is useful for demonstrations or interview-style testing where input may
     * naturally be represented as a List.
     *
     * @param appsList the notification feed as a list of app IDs
     * @param cooldown the minimum forbidden repeat distance; equal app IDs must be more than this apart
     * @return the maximum length of any valid contiguous segment
     *
     * Time complexity: O(n), where n is the size of the list.
     * Space complexity: O(n), due to the array conversion and hash map.
     */
    public int longestValidSegment(List<Integer> appsList, int cooldown) {
        if (appsList == null || appsList.isEmpty()) {
            return 0;
        }

        int[] apps = new int[appsList.size()];
        for (int i = 0; i < appsList.size(); i++) {
            apps[i] = appsList.get(i);
        }

        return longestValidSegment(apps, cooldown);
    }

    /**
     * Prints a test case in a beginner-friendly format.
     *
     * @param apps the notification feed
     * @param cooldown the cooldown value
     * @param expected the expected answer for comparison
     *
     * Time complexity: O(n), due to array-to-string conversion for printing.
     * Space complexity: O(n), due to string construction performed by the library.
     */
    public static void runDemo(int[] apps, int cooldown, int expected) {
        Solution solution = new Solution();
        int actual = solution.longestValidSegment(apps, cooldown);

        System.out.println("apps = " + Arrays.toString(apps));
        System.out.println("cooldown = " + cooldown);
        System.out.println("Longest valid contiguous segment length = " + actual);
        System.out.println("Expected = " + expected);
        System.out.println("Match? " + (actual == expected));
        System.out.println();
    }

    /**
     * Demonstrates the algorithm on sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     *
     * @return nothing
     *
     * Time complexity: O(total input size of demo cases).
     * Space complexity: O(total distinct app IDs in each individual demo case).
     */
    public static void main(String[] args) {
        // Sample 1 from the problem statement.
        // apps = [4, 1, 2, 4, 3, 1, 5], cooldown = 2
        // One optimal valid segment is [1, 2, 4, 3, 1], length 5.
        runDemo(new int[]{4, 1, 2, 4, 3, 1, 5}, 2, 5);

        // Sample 2 from the problem statement.
        // apps = [7, 7, 8, 9, 7, 8, 10], cooldown = 3
        // A valid optimal segment is [9, 7, 8, 10], length 4.
        runDemo(new int[]{7, 7, 8, 9, 7, 8, 10}, 3, 4);

        // Additional check: cooldown = 0 means every segment is valid.
        runDemo(new int[]{5, 5, 5, 5}, 0, 4);

        // Additional check: all unique values, whole array is valid.
        runDemo(new int[]{1, 2, 3, 4, 5}, 10, 5);

        // Additional check: repeated values force a tight window.
        runDemo(new int[]{1, 2, 1, 2, 1, 2}, 2, 2);
    }
}