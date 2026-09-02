import java.util.*;

/*
Title: Count User Pairs With the Same Relative Notification Delays
Difficulty: Medium
Topic: Hashing

Problem Description:
A product team records the times, in minutes, when each user received notifications during a single day.
For each user, the absolute start time is not important; only the pattern of gaps between consecutive
notifications matters. Two users are considered equivalent if, after sorting their notification times in
increasing order, they produce the same sequence of relative delays from the first notification.

In other words, if a user's sorted times are [t0, t1, t2, ...], their delay signature is
[0, t1 - t0, t2 - t0, ...]. Users with different numbers of notifications can never be equivalent.

You are given a list of users, where each user's data is an array of integers representing notification
times. Count how many unordered pairs of users have the same delay signature.

If a user has only one notification, their signature is simply [0]. Duplicate times for the same user are
allowed and should be preserved after sorting.

Return the total number of equivalent user pairs.

Constraints:
- 1 <= users.length <= 100000
- 1 <= users[i].length <= 100
- 0 <= users[i][j] <= 10^9
- The sum of all users[i].length does not exceed 2 * 10^5

Example 1:
Input: users = [[5,10,20],[100,105,115],[3,8,18],[7,7,9],[20,20,22]]
Output: 4

Explanation:
- [5,10,20] -> sorted [5,10,20] -> signature [0,5,15]
- [100,105,115] -> signature [0,5,15]
- [3,8,18] -> signature [0,5,15]
- [7,7,9] -> signature [0,0,2]
- [20,20,22] -> signature [0,0,2]
The first three users form 3 pairs, and the last two form 1 pair, for a total of 4.

Example 2:
Input: users = [[4],[9],[1,4,4],[10,13,13],[2,5,6]]
Output: 2

Explanation:
- [4] and [9] both have signature [0], contributing 1 pair.
- [1,4,4] -> signature [0,3,3]
- [10,13,13] -> signature [0,3,3], contributing 1 pair.
- [2,5,6] -> signature [0,3,4]
So the answer is 2.

A typical efficient solution sorts each user's timestamps, converts them into a canonical signature,
and uses a hash map to count how many times each signature has appeared. If a signature has frequency f,
it contributes f * (f - 1) / 2 unordered pairs.
*/

public class Solution {

    /**
     * Counts how many unordered pairs of users have the same relative notification delay signature.
     *
     * The core idea is:
     * 1. For each user, sort their notification times.
     * 2. Convert the sorted times into a canonical signature based on differences from the first time.
     * 3. Use a hash map to count how many users share each exact signature.
     * 4. Every time we see a signature again, it forms new pairs with all previous users that had
     *    the same signature.
     *
     * Example:
     * If a signature has already appeared 3 times, and we see it one more time now,
     * then this new user forms 3 new unordered pairs with those previous 3 users.
     *
     * @param users a 2D array where users[i] contains the notification times for the i-th user
     * @return the total number of unordered equivalent user pairs
     * Time complexity: O(sum(len_i * log len_i))) due to sorting each user's array
     * Space complexity: O(total number of distinct signatures stored in the hash map)
     */
    public long countEquivalentPairs(int[][] users) {
        // This map stores:
        // key   -> canonical signature string for one user's sorted notification pattern
        // value -> how many users with this exact signature have already been processed
        Map<String, Integer> signatureCount = new HashMap<>();

        // We use long because the number of pairs can be large.
        // In the worst case, if many users share the same signature,
        // the number of pairs can exceed the range of int.
        long pairs = 0L;

        // Process each user independently.
        for (int[] userTimes : users) {
            // Build a canonical signature for the current user.
            String signature = buildSignature(userTimes);

            // Find how many previous users already had this same signature.
            int seenSoFar = signatureCount.getOrDefault(signature, 0);

            // If this signature has been seen 'seenSoFar' times before,
            // then the current user forms exactly 'seenSoFar' new unordered pairs:
            // current user paired with each previous matching user.
            pairs += seenSoFar;

            // Record that we have now seen this signature one more time.
            signatureCount.put(signature, seenSoFar + 1);
        }

        return pairs;
    }

    /**
     * Builds a canonical signature for one user's notification times.
     *
     * The signature is created as follows:
     * - Sort the times in increasing order.
     * - Let the first sorted time be the reference point.
     * - Replace each time with (time - firstTime).
     * - Join the resulting values into a string with separators so it can be used as a hash map key.
     *
     * Why this works:
     * - Absolute starting time does not matter.
     * - Only relative delays from the first notification matter.
     * - Sorting ensures the order is canonical and duplicates are preserved.
     *
     * Example:
     * Input: [20, 5, 10]
     * Sorted: [5, 10, 20]
     * Signature values: [0, 5, 15]
     * Returned key: "3#0,5,15"
     *
     * Including the length in the key makes the representation explicit and prevents accidental
     * ambiguity, although the comma-separated values already distinguish most cases clearly.
     *
     * @param times the notification times for one user
     * @return a canonical string signature representing the user's relative delay pattern
     * Time complexity: O(m log m), where m is times.length
     * Space complexity: O(m) for the copied array and signature construction
     */
    public String buildSignature(int[] times) {
        // We copy the array before sorting so that the original input remains unchanged.
        // This is a good practice unless in-place modification is explicitly allowed and desired.
        int[] sorted = Arrays.copyOf(times, times.length);

        // Sort the timestamps so that equivalent users are compared in a consistent order.
        Arrays.sort(sorted);

        // The first sorted timestamp becomes the baseline.
        int base = sorted[0];

        // We build a string key that uniquely represents the relative delays.
        // Using a delimiter is important to avoid ambiguity:
        // for example, [0, 11] should not look like [0, 1, 1].
        StringBuilder signature = new StringBuilder();

        // Prefix the length to make the signature structure explicit.
        signature.append(sorted.length).append('#');

        // Step-by-step:
        // - The first value is always 0 because sorted[0] - base = 0.
        // - Every later value is the difference from the first timestamp.
        for (int i = 0; i < sorted.length; i++) {
            if (i > 0) {
                signature.append(',');
            }

            // Difference from the first timestamp.
            // The values are safe in int range because timestamps are within [0, 1e9].
            signature.append(sorted[i] - base);
        }

        return signature.toString();
    }

    /**
     * Utility method to print a 2D int array in a readable format.
     *
     * @param users the 2D array to print
     * @return a string representation of the 2D array
     * Time complexity: O(total number of integers in the 2D array)
     * Space complexity: O(total output size)
     */
    public String array2DToString(int[][] users) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < users.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(Arrays.toString(users[i]));
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so the results can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total work of the demonstrated test cases)
     * Space complexity: O(space used by the demonstrated test cases)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] users1 = {
            {5, 10, 20},
            {100, 105, 115},
            {3, 8, 18},
            {7, 7, 9},
            {20, 20, 22}
        };

        long result1 = solution.countEquivalentPairs(users1);
        System.out.println("Example 1 Input: " + solution.array2DToString(users1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Example 1 Expected: 4");
        System.out.println();

        int[][] users2 = {
            {4},
            {9},
            {1, 4, 4},
            {10, 13, 13},
            {2, 5, 6}
        };

        long result2 = solution.countEquivalentPairs(users2);
        System.out.println("Example 2 Input: " + solution.array2DToString(users2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Example 2 Expected: 2");
        System.out.println();

        // Additional quick sanity checks.

        int[][] users3 = {
            {1, 2, 3},
            {10, 11, 12},
            {5, 5, 7},
            {8, 8, 10},
            {100}
        };
        long result3 = solution.countEquivalentPairs(users3);
        System.out.println("Additional Test Input: " + solution.array2DToString(users3));
        System.out.println("Additional Test Output: " + result3);
        // Explanation:
        // [1,2,3] and [10,11,12] -> signature [0,1,2] => 1 pair
        // [5,5,7] and [8,8,10]   -> signature [0,0,2] => 1 pair
        // [100] alone            -> no pair
        // Total = 2
        System.out.println("Additional Test Expected: 2");
    }
}