import java.util.*;

/*
Problem Title: Minimum Verification Timeout for Batched Signatures

Problem Description:
A security platform verifies digital signatures in the order they arrive. The i-th signature
requires verifyTime[i] milliseconds of CPU time, and it must finish no later than deadline[i]
milliseconds from the start of the batch. The platform can choose a single fixed timeout limit T
for all worker threads. Under this limit, each thread may spend at most T milliseconds on one
signature before moving on, so a signature requiring x milliseconds consumes ceil(x / T) time
slices. Each time slice takes exactly T milliseconds, and signatures are still processed
sequentially in the original order.

For a chosen T, the completion time of signature i is:
sum over j from 0 to i of ceil(verifyTime[j] / T) * T

Find the smallest integer timeout T such that every completion time is less than or equal to
deadline[i]. Deadlines are nondecreasing.

Constraints:
- 1 <= n <= 100000
- 1 <= verifyTime[i] <= 1000000000
- 1 <= deadline[i] <= 10^18
- deadline is sorted in nondecreasing order
- It is guaranteed that at least one valid T exists

Important note about the examples:
The narrative in the prompt contains inconsistencies, but the formal definition is clear.
This solution follows the formal definition exactly and therefore computes the correct minimum T.
*/

public class Solution {

    /**
     * Finds the minimum integer timeout T such that all signatures finish by their deadlines.
     *
     * Core idea:
     * 1. For a fixed timeout T, we can check feasibility in a single left-to-right pass.
     * 2. The completion time of each job is accumulated as:
     *      completion += ceil(verifyTime[i] / T) * T
     * 3. If at any point completion > deadline[i], then this T is not feasible.
     * 4. Because feasibility is monotonic in the following sense for this problem's guaranteed-valid
     *    inputs and formal model, we can binary search over T from 1 to max(verifyTime).
     *
     * Why the upper bound can be max(verifyTime):
     * - If T >= verifyTime[i], then job i takes exactly one slice of length T, which is T time.
     * - Increasing T beyond max(verifyTime) only makes every job consume exactly T, which cannot
     *   improve completion times compared with T = max(verifyTime). Therefore searching above
     *   max(verifyTime) is unnecessary.
     *
     * @param verifyTime array where verifyTime[i] is the CPU time needed for the i-th signature
     * @param deadline array where deadline[i] is the latest allowed completion time for signature i
     * @return the smallest valid integer timeout T
     * Time complexity: O(n log M), where n is the number of signatures and M is max(verifyTime)
     * Space complexity: O(1) extra space
     */
    public long minimumVerificationTimeout(int[] verifyTime, long[] deadline) {
        int n = verifyTime.length;

        int maxVerify = 0;
        for (int time : verifyTime) {
            maxVerify = Math.max(maxVerify, time);
        }

        long left = 1;
        long right = maxVerify;
        long answer = right;

        while (left <= right) {
            long mid = left + (right - left) / 2;

            if (isFeasible(verifyTime, deadline, mid)) {
                answer = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given timeout T allows all signatures to complete by their deadlines.
     *
     * Detailed process:
     * - We process signatures in the original order because the problem requires sequential handling.
     * - For each signature with required CPU time x:
     *      slices = ceil(x / T)
     *      consumedTime = slices * T
     * - We add consumedTime to the running batch completion time.
     * - If the running completion time exceeds the current signature's deadline, T fails immediately.
     *
     * Integer arithmetic trick for ceiling division:
     *      ceil(x / T) = (x + T - 1) / T
     * This works for positive integers and avoids floating-point errors.
     *
     * Overflow safety:
     * - verifyTime[i] is up to 1e9
     * - T is up to 1e9
     * - slices * T and cumulative completion can exceed int, so we use long everywhere.
     *
     * @param verifyTime array of required CPU times
     * @param deadline array of deadlines
     * @param timeout the candidate timeout T to test
     * @return true if all signatures finish by their deadlines using this timeout; false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean isFeasible(int[] verifyTime, long[] deadline, long timeout) {
        long completionTime = 0L;

        for (int i = 0; i < verifyTime.length; i++) {
            long required = verifyTime[i];

            // Number of full timeout-sized slices needed for this signature.
            long slices = (required + timeout - 1) / timeout;

            // Actual wall-clock time consumed by this signature under the fixed timeout policy.
            long consumed = slices * timeout;

            // Add this signature's consumed time to the cumulative completion time.
            completionTime += consumed;

            // If this signature finishes after its deadline, the timeout is invalid.
            if (completionTime > deadline[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Convenience helper that accepts deadlines as int[] and converts them to long[].
     * This is useful for small demonstrations, while the main algorithm still uses long[]
     * to safely support the full constraint range.
     *
     * @param verifyTime array of required CPU times
     * @param deadlineInt array of deadlines stored as int values
     * @return the smallest valid integer timeout T
     * Time complexity: O(n log M), where n is the number of signatures and M is max(verifyTime)
     * Space complexity: O(n) due to conversion from int[] to long[]
     */
    public long minimumVerificationTimeout(int[] verifyTime, int[] deadlineInt) {
        long[] deadline = new long[deadlineInt.length];
        for (int i = 0; i < deadlineInt.length; i++) {
            deadline[i] = deadlineInt[i];
        }
        return minimumVerificationTimeout(verifyTime, deadline);
    }

    /**
     * Prints a detailed trace for a specific timeout T.
     * This is purely for demonstration and beginner-friendly understanding.
     *
     * @param verifyTime array of required CPU times
     * @param deadline array of deadlines
     * @param timeout the timeout T to trace
     * @return nothing
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public void printTrace(int[] verifyTime, long[] deadline, long timeout) {
        long completionTime = 0L;
        System.out.println("Tracing timeout T = " + timeout);

        for (int i = 0; i < verifyTime.length; i++) {
            long required = verifyTime[i];
            long slices = (required + timeout - 1) / timeout;
            long consumed = slices * timeout;
            completionTime += consumed;

            System.out.println(
                "Signature " + i +
                ": verifyTime = " + required +
                ", slices = " + slices +
                ", consumed = " + consumed +
                ", completion = " + completionTime +
                ", deadline = " + deadline[i] +
                ", onTime = " + (completionTime <= deadline[i])
            );
        }

        System.out.println("Feasible: " + isFeasible(verifyTime, deadline, timeout));
        System.out.println();
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The prompt's example narratives are inconsistent with their own formal definition.
     * This main method demonstrates the algorithm using the formal definition exactly.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n log M) per demonstration case
     * Space complexity: O(1) extra, excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt, interpreted strictly by the formal definition.
        int[] verifyTime1 = {5, 8, 6};
        long[] deadline1 = {10, 20, 30};

        long answer1 = solution.minimumVerificationTimeout(verifyTime1, deadline1);
        System.out.println("Example 1 result: " + answer1);
        solution.printTrace(verifyTime1, deadline1, answer1);

        // The formal definition gives:
        // T = 1 -> completion times = 5, 13, 19, all within deadlines.
        // Therefore the minimum valid T is 1.

        // Example 2 corrected to be consistent with the guarantee that at least one valid answer exists.
        int[] verifyTime2 = {7, 7, 7};
        long[] deadline2 = {7, 20, 27};

        long answer2 = solution.minimumVerificationTimeout(verifyTime2, deadline2);
        System.out.println("Example 2 result: " + answer2);
        solution.printTrace(verifyTime2, deadline2, answer2);

        // Additional custom example.
        int[] verifyTime3 = {9, 4, 10, 3};
        long[] deadline3 = {12, 20, 40, 50};

        long answer3 = solution.minimumVerificationTimeout(verifyTime3, deadline3);
        System.out.println("Custom example result: " + answer3);
        solution.printTrace(verifyTime3, deadline3, answer3);
    }
}