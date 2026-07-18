import java.util.*;

/*
 * Title: Longest Packet Window With Exact Priority Balance
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A network monitor records a stream of packet priorities in an integer array priorities,
 * where each value is in the range [1, m]. You are also given an integer array target of
 * length m, where target[i] represents the exact number of packets with priority i + 1
 * that must appear inside a valid window. A contiguous window is called balanced if,
 * for every priority level p from 1 to m, the count of p inside the window is exactly
 * target[p - 1].
 *
 * However, the monitor is noisy: packets with priority values greater than m are considered
 * corrupted and may appear in the stream. A balanced window cannot contain any corrupted packet.
 * Your task is to return the length of the longest contiguous balanced window. If no such
 * window exists, return 0.
 *
 * Because the target counts are exact, a valid answer is not just any subarray with bounded
 * frequencies. The window must match the full frequency profile exactly and contain no extra
 * valid-priority packets beyond the required counts. This makes the problem subtle when many
 * repeated values and corrupted packets split the stream into candidate regions.
 *
 * Constraints:
 * - 1 <= priorities.length <= 200000
 * - 1 <= m <= 100000
 * - 0 <= target[i] <= priorities.length
 * - 1 <= priorities[i] <= 1000000000
 * - The sum of all target values may be 0
 *
 * Example 1:
 * Input: priorities = [1,2,1,3,2,1,2], target = [2,2,1]
 * Output: 5
 * Explanation: The window [1,2,1,3,2] contains priority 1 exactly twice, priority 2 exactly
 * twice, and priority 3 exactly once. No longer valid window exists.
 *
 * Example 2:
 * Input: priorities = [4,1,2,1,3,2,5,1,2,3], target = [1,1,1]
 * Output: 3
 * Explanation: Corrupted values 4 and 5 break possible windows because only priorities 1
 * through 3 are allowed. Valid windows include [1,3,2] and [1,2,3], each of length 3.
 * Since target requires exactly one of each allowed priority, the answer is 3.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous balanced window.
     *
     * A window is balanced if:
     * 1) It contains no corrupted value (that is, no value outside [1, m]).
     * 2) For every priority p in [1, m], its frequency inside the window is exactly target[p - 1].
     *
     * Key observation:
     * If a window matches the target frequencies exactly, then its length must be exactly
     * sum(target). Therefore, there is at most one possible valid length. So the problem becomes:
     * "Does there exist a contiguous subarray of length sum(target) whose frequencies match target
     * exactly and that contains no corrupted value?"
     *
     * We solve this with a sliding window over the array:
     * - Any corrupted value immediately invalidates every window crossing it.
     * - We maintain counts for values 1..m inside the current valid segment.
     * - We keep the window size at most requiredLength = sum(target).
     * - Whenever the window size becomes exactly requiredLength, we check whether all frequencies
     *   match target exactly.
     *
     * To make the equality check efficient, we maintain:
     * - count[v] = current frequency of priority v in the window
     * - matched = number of priorities v for which count[v] == target[v - 1]
     *
     * Then the window is valid exactly when:
     * - window length == requiredLength
     * - matched == m
     *
     * @param priorities the packet priority stream
     * @param target exact required counts for priorities 1 through m
     * @return the length of the longest balanced window; returns 0 if none exists
     * Time complexity: O(n + m), where n = priorities.length
     * Space complexity: O(m)
     */
    public int longestBalancedWindow(int[] priorities, int[] target) {
        int n = priorities.length;
        int m = target.length;

        // Compute the exact required window length.
        // Any valid window must have exactly this many elements.
        long totalRequiredLong = 0L;
        for (int value : target) {
            totalRequiredLong += value;
        }

        // Since priorities.length <= 200000, the sum fits in int for all practical valid inputs,
        // but we compute in long first for safety and then cast.
        int requiredLength = (int) totalRequiredLong;

        // Special case:
        // If the target requires zero occurrences of every priority, then the only window that
        // matches exactly is the empty window. In standard subarray problems, we usually consider
        // non-empty contiguous windows, so the answer should be 0.
        if (requiredLength == 0) {
            return 0;
        }

        // count[p] stores how many times priority p appears in the current window.
        // We use 1-based indexing for convenience because priorities are in [1, m].
        int[] count = new int[m + 1];

        // matched counts how many priorities currently satisfy:
        // count[p] == target[p - 1]
        //
        // Initially the window is empty, so for every priority with target 0,
        // count[p] == 0 already matches target[p - 1].
        int matched = 0;
        for (int p = 1; p <= m; p++) {
            if (target[p - 1] == 0) {
                matched++;
            }
        }

        int left = 0;
        int answer = 0;

        // We scan with right as the expanding end of the sliding window.
        for (int right = 0; right < n; right++) {
            int value = priorities[right];

            // If we encounter a corrupted packet, then no valid window can include it.
            // Therefore we must completely reset the current sliding window state and
            // start fresh after this position.
            if (value < 1 || value > m) {
                // Remove all elements currently inside [left, right - 1] from our counts.
                // This reset is still linear overall because each element is added and removed
                // at most once across the entire algorithm.
                while (left < right) {
                    removeValue(priorities[left], count, target);
                    matched += deltaMatchedAfterRemove(priorities[left], count, target);
                    left++;
                }

                // Move left past the corrupted value so the next window starts after it.
                left = right + 1;
                continue;
            }

            // Add the new valid value at position right into the window.
            matched += deltaMatchedBeforeAdd(value, count, target);
            count[value]++;
            matched += deltaMatchedAfterAdd(value, count, target);

            // If the window becomes longer than the only possible valid length,
            // shrink it from the left until its size is at most requiredLength.
            while (right - left + 1 > requiredLength) {
                int remove = priorities[left];
                matched += deltaMatchedBeforeRemove(remove, count, target);
                count[remove]--;
                matched += deltaMatchedAfterRemove(remove, count, target);
                left++;
            }

            // Now if the window length is exactly requiredLength and every priority matches
            // its target count, then this window is balanced.
            if (right - left + 1 == requiredLength && matched == m) {
                answer = requiredLength;
                // Since every valid window must have length exactly requiredLength,
                // this is already the maximum possible answer. We can return immediately.
                return answer;
            }
        }

        return answer;
    }

    /**
     * Helper method used during reset when a corrupted value is found.
     * This method simply decrements the count of a valid priority.
     *
     * Note:
     * The matched updates for reset are handled outside this method to keep the logic explicit.
     *
     * @param value the priority value to remove from the current window
     * @param count current frequency array
     * @param target target frequency array
     * @return nothing
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void removeValue(int value, int[] count, int[] target) {
        if (value >= 1 && value <= target.length) {
            count[value]--;
        }
    }

    /**
     * Computes the change in "matched" just before adding a value.
     *
     * If currently count[value] == target[value - 1], then after incrementing count[value],
     * that equality will be broken, so matched should decrease by 1.
     *
     * @param value the priority being added
     * @param count current frequency array
     * @param target target frequency array
     * @return -1 if the value was matched before the add, otherwise 0
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int deltaMatchedBeforeAdd(int value, int[] count, int[] target) {
        return count[value] == target[value - 1] ? -1 : 0;
    }

    /**
     * Computes the change in "matched" just after adding a value.
     *
     * If after incrementing, count[value] == target[value - 1], then the equality is now
     * satisfied, so matched should increase by 1.
     *
     * @param value the priority that was added
     * @param count current frequency array after increment
     * @param target target frequency array
     * @return +1 if the value becomes matched after the add, otherwise 0
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int deltaMatchedAfterAdd(int value, int[] count, int[] target) {
        return count[value] + 1 == target[value - 1] ? 1 : 0;
    }

    /**
     * Computes the change in "matched" just before removing a value.
     *
     * If currently count[value] == target[value - 1], then after decrementing count[value],
     * that equality will be broken, so matched should decrease by 1.
     *
     * @param value the priority being removed
     * @param count current frequency array
     * @param target target frequency array
     * @return -1 if the value was matched before the removal, otherwise 0
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int deltaMatchedBeforeRemove(int value, int[] count, int[] target) {
        return count[value] == target[value - 1] ? -1 : 0;
    }

    /**
     * Computes the change in "matched" just after removing a value.
     *
     * If after decrementing, count[value] == target[value - 1], then the equality is now
     * satisfied, so matched should increase by 1.
     *
     * This method is also used during reset logic after a corrupted value.
     *
     * @param value the priority that was removed
     * @param count current frequency array after decrement
     * @param target target frequency array
     * @return +1 if the value becomes matched after the removal, otherwise 0
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int deltaMatchedAfterRemove(int value, int[] count, int[] target) {
        return count[value] == target[value - 1] ? 1 : 0;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n + m) per demonstration call
     * Space complexity: O(m) per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] priorities1 = {1, 2, 1, 3, 2, 1, 2};
        int[] target1 = {2, 2, 1};
        int result1 = solution.longestBalancedWindow(priorities1, target1);
        System.out.println(result1); // Expected: 5

        int[] priorities2 = {4, 1, 2, 1, 3, 2, 5, 1, 2, 3};
        int[] target2 = {1, 1, 1};
        int result2 = solution.longestBalancedWindow(priorities2, target2);
        System.out.println(result2); // Expected: 3

        int[] priorities3 = {1, 1, 2, 2, 3, 3};
        int[] target3 = {1, 1, 1};
        int result3 = solution.longestBalancedWindow(priorities3, target3);
        System.out.println(result3); // Expected: 3

        int[] priorities4 = {7, 8, 9};
        int[] target4 = {1, 1, 1};
        int result4 = solution.longestBalancedWindow(priorities4, target4);
        System.out.println(result4); // Expected: 0

        int[] priorities5 = {1, 2, 3};
        int[] target5 = {0, 0, 0};
        int result5 = solution.longestBalancedWindow(priorities5, target5);
        System.out.println(result5); // Expected: 0
    }
}