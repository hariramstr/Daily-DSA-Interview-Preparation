import java.util.*;

/*
Title: Minimum Playback Speed for Museum Audio Guides
Difficulty: Medium
Topic: Binary Search

Problem Description:
A museum offers a fixed sequence of audio guide sections that must be listened to in order.
The i-th section has length guides[i] minutes at normal speed. Visitors may choose a constant
playback speed s, where s is a positive integer, and every section is played at that same speed.
A section that would take x minutes at normal speed takes ceil(x / s) whole minutes to finish
because the museum app only advances to the next section at the start of the next minute.

Given an array guides and an integer limit, return the minimum integer playback speed s such that
the total listening time of all sections is at most limit minutes. If it is impossible even at
arbitrarily large speed, return -1.

Your task is to design an efficient algorithm. A brute-force search over all speeds may be too slow
for large inputs, but the total time required is monotonic with respect to the playback speed,
which suggests a binary search solution.

Constraints:
- 1 <= guides.length <= 100000
- 1 <= guides[i] <= 1000000000
- 1 <= limit <= 1000000000
- Playback speed must be a positive integer

Notes:
- The total time for a chosen speed s is sum(ceil(guides[i] / s)) over all sections.
- If limit < guides.length, the answer is always -1, because each section takes at least 1 whole minute.

Example 1:
Input: guides = [7, 11, 5], limit = 8
Output: 4
Explanation:
At speed 4, the total time is ceil(7/4) + ceil(11/4) + ceil(5/4) = 2 + 3 + 2 = 7, which fits within the limit.
At speed 3, the total time is 3 + 4 + 2 = 9, which is too slow.
Therefore, 4 is the minimum valid speed.

Example 2:
Input: guides = [12, 3, 9, 6], limit = 4
Output: 12
Explanation:
Since there are 4 sections and each takes at least 1 minute, the only way to finish in 4 minutes
is for every section to take exactly 1 minute. Speed 12 works because the times are 1, 1, 1, and 1.
Any smaller speed makes at least one section take 2 minutes, so 12 is the minimum valid speed.
*/

public class Solution {

    /**
     * Finds the minimum positive integer playback speed such that the total listening time
     * of all guide sections is at most the given limit.
     *
     * The key observation is monotonicity:
     * - If a speed s is fast enough, then any speed greater than s is also fast enough.
     * - If a speed s is too slow, then any speed smaller than s is also too slow.
     *
     * Because of this monotonic behavior, binary search can be used over the answer space.
     *
     * @param guides the array where guides[i] is the normal-length duration of the i-th section
     * @param limit the maximum allowed total listening time in whole minutes
     * @return the minimum valid playback speed, or -1 if it is impossible
     * Time complexity: O(n log M), where n is guides.length and M is the maximum value in guides
     * Space complexity: O(1), excluding input storage
     */
    public int minPlaybackSpeed(int[] guides, int limit) {
        // If there are more sections than allowed total minutes,
        // the answer is immediately impossible.
        //
        // Why?
        // Each section takes at least 1 whole minute, no matter how large the speed is,
        // because ceil(x / s) is always at least 1 for any positive x and positive s.
        //
        // So the absolute minimum total time is the number of sections.
        if (limit < guides.length) {
            return -1;
        }

        // The minimum possible speed is 1.
        int left = 1;

        // The maximum necessary speed is the largest guide length.
        //
        // Why is this enough?
        // If speed >= max(guides), then every section finishes in exactly 1 minute:
        // ceil(guides[i] / speed) = 1 for all i.
        //
        // Since we already know limit >= guides.length from the earlier check,
        // some answer must exist within [1, max(guides)].
        int right = getMax(guides);

        // This variable will store the best valid speed found so far.
        int answer = right;

        // Standard binary search on the answer space.
        while (left <= right) {
            // Compute midpoint carefully to avoid overflow.
            int mid = left + (right - left) / 2;

            // Calculate how many total minutes are needed at speed = mid.
            long requiredTime = computeTotalTime(guides, mid, limit);

            // If the required time fits within the limit,
            // then this speed is valid.
            if (requiredTime <= limit) {
                // Record it as a candidate answer.
                answer = mid;

                // But we still want the MINIMUM valid speed,
                // so continue searching on the left half.
                right = mid - 1;
            } else {
                // This speed is too slow.
                // We must increase the speed, so search the right half.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Computes the total listening time for all guide sections at a given playback speed.
     *
     * This method uses the integer arithmetic identity:
     * ceil(a / b) = (a + b - 1) / b, for positive integers a and b.
     *
     * It also performs an early stop:
     * if the running total already exceeds the limit threshold, we can stop immediately,
     * because the exact larger value is not needed for binary search decisions.
     *
     * @param guides the array of guide section lengths
     * @param speed the playback speed being tested
     * @param limit the threshold used for early stopping
     * @return the total required time at the given speed; may return a value greater than limit early
     * Time complexity: O(n) in the worst case
     * Space complexity: O(1)
     */
    public long computeTotalTime(int[] guides, int speed, int limit) {
        long total = 0L;

        // Process each section one by one.
        for (int length : guides) {
            // Compute ceil(length / speed) using integer math.
            //
            // Example:
            // length = 11, speed = 4
            // ceil(11 / 4) = 3
            // integer formula: (11 + 4 - 1) / 4 = 14 / 4 = 3
            total += (length + (long) speed - 1L) / (long) speed;

            // Early exit optimization:
            // once total is already larger than limit, we do not need to continue.
            if (total > limit) {
                return total;
            }
        }

        return total;
    }

    /**
     * Finds the maximum value in the guides array.
     *
     * This is used to define the upper bound of the binary search range.
     *
     * @param guides the array of guide section lengths
     * @return the maximum section length in the array
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int getMax(int[] guides) {
        int max = guides[0];

        for (int value : guides) {
            if (value > max) {
                max = value;
            }
        }

        return max;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called algorithm work
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] guides1 = {7, 11, 5};
        int limit1 = 8;
        int result1 = solution.minPlaybackSpeed(guides1, limit1);
        System.out.println("Sample 1 Result: " + result1);
        // Expected: 4

        // Sample 2
        int[] guides2 = {12, 3, 9, 6};
        int limit2 = 4;
        int result2 = solution.minPlaybackSpeed(guides2, limit2);
        System.out.println("Sample 2 Result: " + result2);
        // Expected: 12

        // Additional test: impossible because limit < number of sections
        int[] guides3 = {5, 8, 10};
        int limit3 = 2;
        int result3 = solution.minPlaybackSpeed(guides3, limit3);
        System.out.println("Impossible Case Result: " + result3);
        // Expected: -1

        // Additional test: already enough with speed 1
        int[] guides4 = {1, 2, 3};
        int limit4 = 6;
        int result4 = solution.minPlaybackSpeed(guides4, limit4);
        System.out.println("Speed 1 Case Result: " + result4);
        // Expected: 1

        // Additional test: single section
        int[] guides5 = {100};
        int limit5 = 10;
        int result5 = solution.minPlaybackSpeed(guides5, limit5);
        System.out.println("Single Section Result: " + result5);
        // Expected: 10 because ceil(100 / 10) = 10
    }
}