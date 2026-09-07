import java.util.*;

/*
 * Title: Minimum Model Accuracy to Pass Staged Benchmarks
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given the results of a machine learning model evaluated on a sequence of benchmark stages.
 * Stage i contains tasks with difficulty score difficulties[i] and contributes weight weights[i]
 * to the final certification score. The model is deployed with a single accuracy threshold A.
 * A task is considered passed if difficulties[i] <= A.
 *
 * However, certification is stricter than counting passed tasks: the model passes the full benchmark
 * only if there exists a partition of the stages into at most k contiguous groups such that, in every
 * group, the total weight of passed stages is at least the total weight of failed stages.
 *
 * For a fixed A, convert each stage into:
 *   +weights[i], if difficulties[i] <= A
 *   -weights[i], otherwise
 *
 * The threshold A is feasible if the array can be divided into at most k contiguous non-empty segments,
 * and every segment has sum >= 0.
 *
 * Return the minimum feasible A. If no threshold can make the benchmark pass, return -1.
 *
 * Important observation:
 * Since we are allowed to use "at most k" segments, using exactly one segment is always allowed
 * whenever k >= 1. Therefore, a threshold A is feasible if and only if the total transformed sum
 * of the entire array is non-negative.
 *
 * Why?
 * - If there exists any valid partition where every segment sum is >= 0, then summing all segment sums
 *   shows the total array sum is also >= 0.
 * - If the total array sum is >= 0, then taking the whole array as one segment gives a valid partition,
 *   and one segment is certainly <= k because k >= 1.
 *
 * So the problem reduces to:
 * Find the minimum integer A such that
 *   sum( +weights[i] for difficulties[i] <= A ) +
 *   sum( -weights[i] for difficulties[i] >  A ) >= 0
 *
 * Equivalently:
 * Let totalWeight = sum(weights).
 * Let passedWeight(A) = sum(weights[i] where difficulties[i] <= A).
 * Then transformed total = 2 * passedWeight(A) - totalWeight.
 * We need:
 *   2 * passedWeight(A) >= totalWeight
 *
 * Since passedWeight(A) is monotonic non-decreasing in A, we can binary search the answer over
 * the sorted unique difficulty values.
 */

public class Solution {

    /**
     * Finds the minimum feasible integer accuracy threshold.
     *
     * Step-by-step idea:
     * 1. If we fix a threshold A, each stage contributes either +weight or -weight.
     * 2. Because we may use at most k segments, one segment (the entire array) is always allowed.
     * 3. Therefore feasibility is exactly the same as asking whether the total transformed sum is >= 0.
     * 4. That total is monotonic with respect to A:
     *    - when A increases, more difficulties become "passed"
     *    - so more terms flip from -weight to +weight
     *    - therefore the total transformed sum never decreases
     * 5. This monotonicity allows binary search on A.
     * 6. The only thresholds where the answer can change are existing difficulty values,
     *    so we binary search over the sorted unique difficulties.
     *
     * @param difficulties the difficulty score of each stage
     * @param weights the weight contributed by each stage
     * @param k the maximum number of contiguous non-empty segments allowed
     * @return the minimum feasible integer threshold, or -1 if impossible
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int minimumAccuracyThreshold(int[] difficulties, int[] weights, int k) {
        int n = difficulties.length;

        // Defensive check for malformed input.
        if (weights.length != n || n == 0 || k <= 0) {
            return -1;
        }

        // Compute the total weight of all stages.
        // We use long because:
        // - n can be up to 200000
        // - each weight can be up to 1e9
        // - total can be up to 2e14, which does not fit in int
        long totalWeight = 0L;
        for (int w : weights) {
            totalWeight += w;
        }

        // Build pairs (difficulty, weight) so we can sort by difficulty.
        Stage[] stages = new Stage[n];
        for (int i = 0; i < n; i++) {
            stages[i] = new Stage(difficulties[i], weights[i]);
        }

        Arrays.sort(stages, Comparator.comparingInt(s -> s.difficulty));

        // Compress equal difficulties:
        // For each distinct difficulty value d, store the total weight of stages having difficulty d.
        // This lets us evaluate passedWeight(A) by difficulty groups.
        List<Integer> uniqueDifficultiesList = new ArrayList<>();
        List<Long> groupedWeightsList = new ArrayList<>();

        int i = 0;
        while (i < n) {
            int currentDifficulty = stages[i].difficulty;
            long sumWeightForThisDifficulty = 0L;

            while (i < n && stages[i].difficulty == currentDifficulty) {
                sumWeightForThisDifficulty += stages[i].weight;
                i++;
            }

            uniqueDifficultiesList.add(currentDifficulty);
            groupedWeightsList.add(sumWeightForThisDifficulty);
        }

        int m = uniqueDifficultiesList.size();
        int[] uniqueDifficulties = new int[m];
        long[] prefixPassedWeights = new long[m];

        // Build prefix sums of grouped weights.
        // prefixPassedWeights[idx] = total weight of all stages with difficulty <= uniqueDifficulties[idx]
        long running = 0L;
        for (int idx = 0; idx < m; idx++) {
            uniqueDifficulties[idx] = uniqueDifficultiesList.get(idx);
            running += groupedWeightsList.get(idx);
            prefixPassedWeights[idx] = running;
        }

        // Quick impossibility check:
        // Even if A is at least the maximum difficulty, all stages are passed,
        // so transformed total becomes +totalWeight, which is always >= 0.
        // Therefore the answer is always some existing difficulty value.
        // So no need for a separate impossible case under valid constraints.
        // Still, we keep the binary search structure clean and safe.

        int left = 0;
        int right = m - 1;
        int answer = -1;

        // Standard binary search on the sorted unique difficulty values.
        while (left <= right) {
            int mid = left + (right - left) / 2;

            int candidateThreshold = uniqueDifficulties[mid];

            // Total weight of stages that would be passed at this threshold.
            long passedWeight = prefixPassedWeights[mid];

            // Total transformed sum:
            // +passedWeight for passed stages
            // -(totalWeight - passedWeight) for failed stages
            // = 2 * passedWeight - totalWeight
            long transformedTotal = 2L * passedWeight - totalWeight;

            // Feasibility condition:
            // Since one segment is allowed, total transformed sum >= 0 is sufficient and necessary.
            if (transformedTotal >= 0L) {
                answer = candidateThreshold;
                right = mid - 1; // try to find a smaller feasible threshold
            } else {
                left = mid + 1;  // need a larger threshold
            }
        }

        return answer;
    }

    /**
     * Checks whether a given threshold is feasible.
     *
     * This helper method is written in a very direct and beginner-friendly way:
     * it computes the transformed total sum explicitly.
     *
     * Note:
     * The parameter k does not affect the result once k >= 1, because using the whole array
     * as one segment is always allowed. It is included here to match the original problem shape.
     *
     * @param difficulties the difficulty score of each stage
     * @param weights the weight contributed by each stage
     * @param k the maximum number of contiguous non-empty segments allowed
     * @param threshold the candidate accuracy threshold A
     * @return true if the threshold is feasible, false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean isFeasible(int[] difficulties, int[] weights, int k, int threshold) {
        if (k <= 0) {
            return false;
        }

        long total = 0L;

        // Convert each stage according to the threshold:
        // passed  -> +weight
        // failed  -> -weight
        // Then sum everything.
        for (int i = 0; i < difficulties.length; i++) {
            if (difficulties[i] <= threshold) {
                total += weights[i];
            } else {
                total -= weights[i];
            }
        }

        // Feasible iff the whole array can serve as one valid segment.
        return total >= 0L;
    }

    /**
     * Demonstrates the solution on the sample inputs from the statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called methods
     * Space complexity: O(1), excluding called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] difficulties1 = {4, 2, 7, 3, 6};
        int[] weights1 = {5, 2, 4, 3, 6};
        int k1 = 2;
        int result1 = solution.minimumAccuracyThreshold(difficulties1, weights1, k1);
        System.out.println(result1); // Expected: 4

        int[] difficulties2 = {8, 9, 10};
        int[] weights2 = {3, 4, 5};
        int k2 = 3;
        int result2 = solution.minimumAccuracyThreshold(difficulties2, weights2, k2);
        System.out.println(result2); // Expected: 10

        // Additional quick checks
        System.out.println(solution.isFeasible(difficulties1, weights1, k1, 4)); // Expected: true
        System.out.println(solution.isFeasible(difficulties1, weights1, k1, 3)); // Expected: false
    }

    /**
     * Simple helper structure to keep a stage's difficulty and weight together.
     */
    static class Stage {
        int difficulty;
        int weight;

        /**
         * Creates a stage record.
         *
         * @param difficulty the stage difficulty
         * @param weight the stage weight
         */
        Stage(int difficulty, int weight) {
            this.difficulty = difficulty;
            this.weight = weight;
        }
    }
}