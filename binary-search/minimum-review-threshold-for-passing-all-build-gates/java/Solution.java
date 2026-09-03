import java.util.*;

/*
 * Title: Minimum Review Threshold for Passing All Build Gates
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given a software release pipeline with n sequential build gates.
 * Gate i requires at least requirements[i] approved review points before the release can pass that gate.
 * You also have m review batches, where batch j contributes reviews[j] points and can be split across
 * multiple gates in any way. However, to keep the process fair, you must choose a single threshold value T
 * and cap every batch at min(reviews[j], T) usable points. Any points above T in a batch are ignored.
 * After capping, all usable review points from all batches are pooled together and may be distributed
 * arbitrarily among the gates.
 *
 * Your task is to find the minimum integer threshold T such that the total capped review points are enough
 * to satisfy all gate requirements. If even using all review points is insufficient, return -1.
 *
 * Formally, find the smallest integer T >= 0 such that:
 * sum(min(reviews[j], T) for j in [0..m-1]) >= sum(requirements[i] for i in [0..n-1])
 *
 * If no such T exists because sum(reviews) < sum(requirements), return -1.
 *
 * Constraints:
 * - 1 <= n, m <= 2 * 10^5
 * - 1 <= requirements[i], reviews[j] <= 10^12
 * - The answer must fit in 64-bit signed integer range
 *
 * Important correction to the provided example:
 * Example 1 in the prompt states output 6, but its own explanation shows:
 * - T = 5 => 3 + 5 + 5 = 13
 * - T = 6 => 3 + 6 + 6 = 15
 * - T = 7 => 3 + 7 + 7 = 17
 * Since the required total is 16, T = 6 is NOT enough, and the first valid threshold is 7.
 * Therefore the correct output for Example 1 is 7.
 */

public class Solution {

    /**
     * Finds the minimum threshold T such that the total capped review points
     * are at least the total required points.
     *
     * The key observation is that the function:
     * cappedSum(T) = sum(min(reviews[j], T))
     * is monotonic non-decreasing as T increases.
     *
     * Because of that monotonic behavior, we can binary search for the smallest T
     * that makes cappedSum(T) >= totalRequired.
     *
     * @param requirements the required review points for each build gate
     * @param reviews the review points available in each batch
     * @return the minimum valid threshold T, or -1 if even the full uncapped total is insufficient
     * Time complexity: O((n + m) + m * log U), where U is max(reviews)
     * Space complexity: O(1) extra space, excluding input storage
     */
    public long minimumReviewThreshold(long[] requirements, long[] reviews) {
        // Step 1:
        // Compute the total amount of review points required across all gates.
        long totalRequired = 0L;
        for (long requirement : requirements) {
            totalRequired += requirement;
        }

        // Step 2:
        // Compute the total amount of review points available if we do NOT cap anything.
        // This is the absolute maximum we can ever use.
        long totalAvailable = 0L;

        // Also track the maximum single batch value.
        // This gives us a safe upper bound for binary search because:
        // once T >= max(reviews), min(reviews[j], T) = reviews[j] for every j,
        // so the capped sum becomes the full sum and cannot increase further.
        long maxReview = 0L;

        for (long review : reviews) {
            totalAvailable += review;
            if (review > maxReview) {
                maxReview = review;
            }
        }

        // Step 3:
        // If even the full uncapped total is smaller than what we need,
        // then no threshold can ever work.
        if (totalAvailable < totalRequired) {
            return -1L;
        }

        // Step 4:
        // Binary search on T in the range [0, maxReview].
        //
        // Why 0?
        // If T = 0, every batch contributes 0.
        //
        // Why maxReview?
        // For any T >= maxReview, the capped sum equals the full sum.
        //
        // We want the SMALLEST T such that cappedSum(T) >= totalRequired.
        long left = 0L;
        long right = maxReview;

        while (left < right) {
            // Standard overflow-safe midpoint calculation.
            long mid = left + (right - left) / 2;

            // Compute how many usable review points we get if threshold = mid.
            long cappedTotal = cappedSum(reviews, mid, totalRequired);

            // If mid is sufficient, then the answer is <= mid.
            // So we keep the left half, including mid.
            if (cappedTotal >= totalRequired) {
                right = mid;
            } else {
                // Otherwise, mid is too small, so the answer must be > mid.
                left = mid + 1;
            }
        }

        // At loop end, left == right and points to the smallest valid threshold.
        return left;
    }

    /**
     * Computes the capped sum:
     * sum(min(reviews[j], threshold))
     *
     * This helper includes an early stop:
     * if the running sum already reaches or exceeds targetNeeded,
     * we can return immediately because the caller only needs to know
     * whether the threshold is sufficient.
     *
     * @param reviews the review batches
     * @param threshold the cap applied to every batch
     * @param targetNeeded the required total; used for early termination
     * @return the capped total, possibly stopping early once targetNeeded is reached
     * Time complexity: O(m) in the worst case
     * Space complexity: O(1)
     */
    public long cappedSum(long[] reviews, long threshold, long targetNeeded) {
        long sum = 0L;

        for (long review : reviews) {
            // Add the usable contribution from this batch:
            // either the full batch if it is <= threshold,
            // or the threshold if the batch is larger.
            sum += Math.min(review, threshold);

            // Early exit optimization:
            // once we already have enough, no need to continue scanning.
            if (sum >= targetNeeded) {
                return sum;
            }
        }

        return sum;
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(k), where k is the array length
     * Space complexity: O(k) due to string construction
     */
    public String arrayToString(long[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Note:
     * The first sample's explanation proves the correct answer is 7, not 6.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O((n + m) + m * log U) per demonstration call
     * Space complexity: O(1) extra space, excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        long[] requirements1 = {5, 7, 4};
        long[] reviews1 = {3, 10, 8};

        long answer1 = solution.minimumReviewThreshold(requirements1, reviews1);

        System.out.println("Sample 1");
        System.out.println("requirements = " + solution.arrayToString(requirements1));
        System.out.println("reviews      = " + solution.arrayToString(reviews1));
        System.out.println("Minimum threshold = " + answer1);
        System.out.println("Expected (correctly traced) = 7");
        System.out.println();

        // Manual trace for Sample 1:
        // totalRequired = 5 + 7 + 4 = 16
        // T = 5 => 3 + 5 + 5 = 13
        // T = 6 => 3 + 6 + 6 = 15
        // T = 7 => 3 + 7 + 7 = 17
        // Therefore the first valid threshold is 7.

        // Sample 2
        long[] requirements2 = {9, 6};
        long[] reviews2 = {4, 3, 5};

        long answer2 = solution.minimumReviewThreshold(requirements2, reviews2);

        System.out.println("Sample 2");
        System.out.println("requirements = " + solution.arrayToString(requirements2));
        System.out.println("reviews      = " + solution.arrayToString(reviews2));
        System.out.println("Minimum threshold = " + answer2);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional small sanity checks

        // If one batch alone is enough and can be capped appropriately.
        long[] requirements3 = {4, 4};
        long[] reviews3 = {100};
        long answer3 = solution.minimumReviewThreshold(requirements3, reviews3);
        System.out.println("Sanity Check 1");
        System.out.println("requirements = " + solution.arrayToString(requirements3));
        System.out.println("reviews      = " + solution.arrayToString(reviews3));
        System.out.println("Minimum threshold = " + answer3);
        System.out.println("Expected = 8");
        System.out.println();

        // Exact fit when threshold reaches a certain point.
        long[] requirements4 = {2, 3, 1};
        long[] reviews4 = {1, 2, 3, 4};
        long answer4 = solution.minimumReviewThreshold(requirements4, reviews4);
        System.out.println("Sanity Check 2");
        System.out.println("requirements = " + solution.arrayToString(requirements4));
        System.out.println("reviews      = " + solution.arrayToString(reviews4));
        System.out.println("Minimum threshold = " + answer4);
        System.out.println("Expected = 2");
    }
}