import java.util.*;

/*
Title: Minimum Patch Version to Support All Client Features
Difficulty: Medium
Topic: Binary Search

Problem Description:
A software platform publishes patch versions in increasing order from 1 to n. For each patch version i, you are given an integer compatibility score patches[i], where a larger score means that version supports a wider set of client-side features. The array is guaranteed to be non-decreasing, because later patches never remove previously supported features.

You are also given several client requirements. Each requirement is a target compatibility score target. For every target, return the smallest patch version index (1-indexed) whose compatibility score is greater than or equal to target. If no patch version can satisfy the target, return -1 for that query.

Your task is to implement a function that answers all queries efficiently.

This is not a simple linear scan problem: the number of patch versions and queries can both be large, so an efficient binary search solution is expected. Since the compatibility scores are sorted in non-decreasing order, you should search for the leftmost version that satisfies each target.

Constraints:
- 1 <= n <= 200000
- 1 <= q <= 200000
- 0 <= patches[i] <= 1000000000
- patches is non-decreasing
- 0 <= target <= 1000000000

Example 1:
Input: patches = [2, 4, 4, 7, 10], queries = [4, 5, 10, 11]
Output: [2, 4, 5, -1]
Explanation:
- Target 4 is first satisfied by version 2.
- Target 5 is first satisfied by version 4 (score 7).
- Target 10 is first satisfied by version 5.
- No version reaches 11.

Example 2:
Input: patches = [0, 0, 3, 3, 8], queries = [0, 1, 3, 6]
Output: [1, 3, 3, 5]
Explanation:
- Target 0 is already satisfied by the first version.
- Target 1 is first satisfied by version 3.
- Target 3 is also first satisfied by version 3.
- Target 6 is first satisfied by version 5.

Return an array of answers in the same order as the queries.
*/

public class Solution {

    /**
     * Answers all client compatibility queries.
     *
     * For each target value in queries, this method finds the smallest 1-indexed
     * patch version whose compatibility score is greater than or equal to that target.
     * If no such patch version exists, it returns -1 for that query.
     *
     * The input array patches is sorted in non-decreasing order, which allows us
     * to use binary search independently for each query.
     *
     * @param patches the non-decreasing array of patch compatibility scores
     * @param queries the target compatibility requirements to satisfy
     * @return an array where each element is the smallest 1-indexed patch version
     *         satisfying the corresponding query, or -1 if impossible
     * @implNote Time complexity: O(q log n), where n = patches.length and q = queries.length
     * @implNote Space complexity: O(q) for the output array, excluding input storage
     */
    public int[] minimumPatchVersions(int[] patches, int[] queries) {
        int[] answer = new int[queries.length];

        // We process each query independently.
        // Since patches is sorted, we can binary search for the first position
        // where patches[position] >= target.
        for (int i = 0; i < queries.length; i++) {
            answer[i] = findFirstSatisfyingVersion(patches, queries[i]);
        }

        return answer;
    }

    /**
     * Finds the smallest 1-indexed patch version whose compatibility score
     * is greater than or equal to the given target.
     *
     * This is the classic "lower bound" binary search:
     * find the leftmost index such that patches[index] >= target.
     *
     * If such an index exists, return index + 1 because the problem uses 1-based indexing.
     * Otherwise, return -1.
     *
     * @param patches the non-decreasing array of patch compatibility scores
     * @param target the required minimum compatibility score
     * @return the smallest 1-indexed patch version satisfying the target, or -1 if none exists
     * @implNote Time complexity: O(log n)
     * @implNote Space complexity: O(1)
     */
    public int findFirstSatisfyingVersion(int[] patches, int target) {
        int left = 0;
        int right = patches.length - 1;

        // This variable stores the best valid index found so far.
        // We initialize it to -1 to mean "not found yet".
        int answerIndex = -1;

        // Standard binary search loop:
        // while there is still a search range to inspect.
        while (left <= right) {
            // Compute middle safely to avoid overflow.
            int mid = left + (right - left) / 2;

            // If patches[mid] is large enough, then mid is a valid candidate.
            // But we do NOT stop here, because there might be an even earlier
            // index that also satisfies the target.
            if (patches[mid] >= target) {
                answerIndex = mid;

                // Move left to continue searching for a smaller valid index.
                right = mid - 1;
            } else {
                // patches[mid] is too small, so every index at or before mid
                // is also too small (because the array is non-decreasing).
                // Therefore, we must search strictly to the right.
                left = mid + 1;
            }
        }

        // If answerIndex stayed -1, no patch can satisfy the target.
        // Otherwise convert from 0-based index to 1-based version number.
        return answerIndex == -1 ? -1 : answerIndex + 1;
    }

    /**
     * Converts an int array into a readable string representation.
     *
     * This helper is used only for demonstration in main.
     *
     * @param array the array to convert
     * @return a string like [1, 2, 3]
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n) due to string construction
     */
    public static String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints the patches, queries, computed answers, and expected answers
     * so the behavior is easy to verify.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * @implNote Time complexity: O(q log n) for each demonstration case
     * @implNote Space complexity: O(q) for the result arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] patches1 = {2, 4, 4, 7, 10};
        int[] queries1 = {4, 5, 10, 11};
        int[] result1 = solution.minimumPatchVersions(patches1, queries1);

        System.out.println("Example 1");
        System.out.println("Patches:  " + arrayToString(patches1));
        System.out.println("Queries:  " + arrayToString(queries1));
        System.out.println("Output:   " + arrayToString(result1));
        System.out.println("Expected: [2, 4, 5, -1]");
        System.out.println();

        // Example 2
        int[] patches2 = {0, 0, 3, 3, 8};
        int[] queries2 = {0, 1, 3, 6};
        int[] result2 = solution.minimumPatchVersions(patches2, queries2);

        System.out.println("Example 2");
        System.out.println("Patches:  " + arrayToString(patches2));
        System.out.println("Queries:  " + arrayToString(queries2));
        System.out.println("Output:   " + arrayToString(result2));
        System.out.println("Expected: [1, 3, 3, 5]");
        System.out.println();

        // Additional quick sanity check
        int[] patches3 = {1, 1, 1, 1};
        int[] queries3 = {0, 1, 2};
        int[] result3 = solution.minimumPatchVersions(patches3, queries3);

        System.out.println("Additional Test");
        System.out.println("Patches:  " + arrayToString(patches3));
        System.out.println("Queries:  " + arrayToString(queries3));
        System.out.println("Output:   " + arrayToString(result3));
        System.out.println("Expected: [1, 1, -1]");
    }
}