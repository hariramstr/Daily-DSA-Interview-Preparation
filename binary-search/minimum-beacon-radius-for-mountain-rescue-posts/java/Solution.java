import java.util.*;

/*
Problem Title: Minimum Beacon Radius for Mountain Rescue Posts

Problem Description:
A rescue agency maintains emergency beacon posts along a long mountain trail. The trail is modeled as a one-dimensional line. You are given two integer arrays: posts, where posts[i] is the position of the i-th rescue post, and shelters, where shelters[j] is the position of the j-th shelter that must be covered by at least one post. A rescue post with signal radius R covers every shelter whose distance from that post is at most R.

Your task is to compute the minimum integer radius R such that every shelter is covered by at least one rescue post.

The arrays are not guaranteed to be sorted, and positions may be negative. Multiple posts or shelters may share the same coordinate. You should design an algorithm efficient enough for very large inputs.

A straightforward check of every shelter against every post is too slow. A typical efficient solution sorts the coordinates and uses binary search on the answer, together with a linear or logarithmic feasibility check.

Return the smallest integer R for which all shelters are covered.

Constraints:
- 1 <= posts.length, shelters.length <= 2 * 10^5
- -10^9 <= posts[i], shelters[j] <= 10^9
- Coordinates may repeat
- Answer fits in a 32-bit signed integer

Example 1:
Input: posts = [1, 8, 15], shelters = [2, 6, 10, 14]
Output: 2
Explanation: With radius 2, post 1 covers shelter 2, post 8 covers shelters 6 and 10, and post 15 covers shelter 14. Radius 1 is not enough because shelter 6 would remain uncovered.

Example 2:
Input: posts = [-5, 0, 9], shelters = [-8, -4, 3, 11]
Output: 4
Explanation: With radius 4, the post at -5 covers -8 and -4, the post at 0 covers 3, and the post at 9 covers 11. No smaller radius can cover all shelters.
*/

public class Solution {

    /**
     * Computes the minimum integer radius needed so that every shelter is covered
     * by at least one rescue post.
     *
     * Core idea:
     * 1. Sort both arrays.
     * 2. Binary search the answer radius R.
     * 3. For each candidate radius, check feasibility in linear time using two pointers.
     *
     * Why binary search works:
     * - If a radius R is sufficient to cover all shelters, then any radius larger than R
     *   is also sufficient.
     * - This monotonic property makes binary search valid.
     *
     * @param posts the positions of rescue posts
     * @param shelters the positions of shelters that must be covered
     * @return the smallest integer radius that covers all shelters
     * Time complexity: O((n + m) log C), where n = posts.length, m = shelters.length,
     * and C is the coordinate search range (at most about 2 * 10^9)
     * Space complexity: O(1) extra space beyond sorting overhead (ignoring Java's internal sort stack)
     */
    public int minimumBeaconRadius(int[] posts, int[] shelters) {
        // Sort both arrays so we can process positions from left to right.
        // This is essential for both the binary search feasibility check
        // and for the simpler direct method shown below.
        Arrays.sort(posts);
        Arrays.sort(shelters);

        // We binary search on the answer:
        // low = definitely too small
        // high = definitely large enough
        //
        // Since coordinates are within [-1e9, 1e9], the maximum possible distance
        // between a post and a shelter is at most 2e9, which fits in int.
        int low = 0;
        int high = 2_000_000_000;

        // Standard binary search for the first feasible radius.
        while (low < high) {
            int mid = low + (high - low) / 2;

            // If radius mid can cover all shelters, try smaller.
            if (canCoverAllShelters(posts, shelters, mid)) {
                high = mid;
            } else {
                // Otherwise, radius mid is too small, so answer must be larger.
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether a given radius is sufficient to cover every shelter.
     *
     * Detailed two-pointer idea:
     * - Both arrays are already sorted.
     * - We walk through shelters from left to right.
     * - For each shelter, we move the post pointer forward while the current post is too far left
     *   to cover that shelter.
     * - After that movement:
     *   - If we have no posts left, coverage fails.
     *   - Otherwise, check whether the current post covers the shelter.
     *   - If not, then because posts are sorted and this current post is already to the right
     *     of the shelter's left coverage boundary, no earlier post can help, so coverage fails.
     *
     * More concretely:
     * - A post at position p covers shelter s if |p - s| <= radius.
     * - Rearranged, shelter s is covered by post p if p is in [s - radius, s + radius].
     *
     * @param posts sorted positions of rescue posts
     * @param shelters sorted positions of shelters
     * @param radius the candidate radius to test
     * @return true if every shelter is covered, otherwise false
     * Time complexity: O(n + m), where n = posts.length and m = shelters.length
     * Space complexity: O(1)
     */
    public boolean canCoverAllShelters(int[] posts, int[] shelters, int radius) {
        int postIndex = 0;
        int postCount = posts.length;

        // Process shelters from left to right.
        for (int shelter : shelters) {
            long leftBoundary = (long) shelter - radius;
            long rightBoundary = (long) shelter + radius;

            // Move forward while the current post is strictly left of the shelter's
            // coverage interval. Such a post can never cover this shelter or any later
            // shelter better than a later post can.
            while (postIndex < postCount && posts[postIndex] < leftBoundary) {
                postIndex++;
            }

            // If we ran out of posts, there is no post left that could cover this shelter.
            if (postIndex == postCount) {
                return false;
            }

            // Now posts[postIndex] is the first post not left of leftBoundary.
            // To cover the shelter, it must also be <= rightBoundary.
            if (posts[postIndex] > rightBoundary) {
                return false;
            }
        }

        return true;
    }

    /**
     * Alternative direct solution without binary search.
     *
     * This method computes, for each shelter, the distance to its nearest post,
     * and then returns the maximum of those nearest distances.
     *
     * It is included for educational value and as a correctness cross-check.
     *
     * Detailed idea:
     * - Sort posts and shelters.
     * - Sweep through shelters in increasing order.
     * - Maintain a pointer to the closest relevant post.
     * - For each shelter, move the pointer while the next post is at least as close
     *   as the current post.
     * - The nearest distance for that shelter is then easy to compute.
     * - The answer is the maximum nearest distance over all shelters.
     *
     * This works because on a sorted line, the nearest post for successive shelters
     * moves monotonically to the right.
     *
     * @param posts the positions of rescue posts
     * @param shelters the positions of shelters
     * @return the minimum radius needed to cover all shelters
     * Time complexity: O(n log n + m log m) due to sorting, then O(n + m) sweep
     * Space complexity: O(1) extra space beyond sorting overhead
     */
    public int minimumBeaconRadiusDirect(int[] posts, int[] shelters) {
        Arrays.sort(posts);
        Arrays.sort(shelters);

        int postIndex = 0;
        long answer = 0;

        for (int shelter : shelters) {
            // Move to the right while the next post is closer (or equally close).
            // Using <= is safe and keeps the pointer moving forward monotonically.
            while (postIndex + 1 < posts.length
                    && Math.abs((long) posts[postIndex + 1] - shelter) <= Math.abs((long) posts[postIndex] - shelter)) {
                postIndex++;
            }

            long nearestDistance = Math.abs((long) posts[postIndex] - shelter);
            answer = Math.max(answer, nearestDistance);
        }

        return (int) answer;
    }

    /**
     * Utility method to create a defensive copy of an array.
     * This is useful in demonstrations so that sorting in one method
     * does not affect later method calls.
     *
     * @param array the input array
     * @return a copied array with the same contents
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] copyArray(int[] array) {
        return Arrays.copyOf(array, array.length);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It prints:
     * - the result from the binary-search-based solution
     * - the result from the direct nearest-post solution
     *
     * Both should match the expected answers.
     *
     * Example 1 expected output: 2
     * Example 2 expected output: 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for fixed demo size, excluding algorithm calls
     * Space complexity: O(1), excluding copied arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] posts1 = {1, 8, 15};
        int[] shelters1 = {2, 6, 10, 14};

        int result1 = solution.minimumBeaconRadius(solution.copyArray(posts1), solution.copyArray(shelters1));
        int result1Direct = solution.minimumBeaconRadiusDirect(solution.copyArray(posts1), solution.copyArray(shelters1));

        System.out.println("Example 1:");
        System.out.println("Binary Search Answer = " + result1);
        System.out.println("Direct Sweep Answer  = " + result1Direct);
        System.out.println("Expected             = 2");
        System.out.println();

        int[] posts2 = {-5, 0, 9};
        int[] shelters2 = {-8, -4, 3, 11};

        int result2 = solution.minimumBeaconRadius(solution.copyArray(posts2), solution.copyArray(shelters2));
        int result2Direct = solution.minimumBeaconRadiusDirect(solution.copyArray(posts2), solution.copyArray(shelters2));

        System.out.println("Example 2:");
        System.out.println("Binary Search Answer = " + result2);
        System.out.println("Direct Sweep Answer  = " + result2Direct);
        System.out.println("Expected             = 4");
    }
}