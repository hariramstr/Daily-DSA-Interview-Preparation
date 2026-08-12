import java.util.*;

/*
 * Title: Detect the First Fully Reconciled Invoice Pair
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of invoice records in the order they were received by an accounting system.
 * Each record is represented as a pair [vendorId, amount]. A vendor may issue multiple invoices,
 * and the same amount may appear many times across different vendors.
 *
 * Two records form a fully reconciled pair if they belong to the same vendor and their amounts
 * sum to exactly 0. For example, an invoice of +120 and a later correction of -120 from the same
 * vendor form a reconciled pair. Your task is to return the earliest record index j such that
 * record j completes at least one fully reconciled pair with some earlier record i from the same
 * vendor. If multiple earlier records could pair with j, any one of them is acceptable, but the
 * completed pair must be the first one that becomes possible while scanning from left to right.
 *
 * Return the pair of indices [i, j]. If no such pair exists, return [-1, -1]. Indices are 0-based.
 *
 * A record cannot be paired with itself. Multiple identical records may exist, and each record
 * should be treated as an independent entry. The challenge is to detect the first completed
 * reconciliation efficiently using hashing rather than checking all previous records.
 *
 * Constraints:
 * - 1 <= records.length <= 200000
 * - records[i].length == 2
 * - 1 <= vendorId <= 1000000000
 * - -1000000000 <= amount <= 1000000000
 *
 * Example 1:
 * Input: records = [[7,100],[3,50],[7,-100],[7,100],[3,-20]]
 * Output: [0,2]
 * Explanation: At index 2, record [7,-100] matches the earlier record [7,100] at index 0.
 * This is the first time any fully reconciled pair appears.
 *
 * Example 2:
 * Input: records = [[5,40],[5,10],[8,-40],[5,-10],[8,40]]
 * Output: [1,3]
 * Explanation: Index 2 does not match index 0 because the vendor is different. At index 3,
 * record [5,-10] matches the earlier record [5,10] at index 1, so [1,3] is the earliest
 * completed reconciled pair.
 */

public class Solution {

    /**
     * Finds the first pair of indices [i, j] such that:
     * - i < j
     * - records[i] and records[j] have the same vendorId
     * - records[i][1] + records[j][1] == 0
     *
     * The method scans from left to right and returns as soon as the earliest possible
     * completing index j is found. This guarantees correctness because the first time we
     * encounter a record that can match any earlier record from the same vendor, that j is
     * by definition the earliest completed reconciliation.
     *
     * Important implementation idea:
     * We store previously seen records in a hash map keyed by (vendorId, amount).
     * For the current record [vendor, amount], we look for an earlier record
     * [vendor, -amount]. If it exists, we immediately return that earlier index together
     * with the current index.
     *
     * @param records the invoice records, where each record is [vendorId, amount]
     * @return an array [i, j] representing the first fully reconciled pair, or [-1, -1] if none exists
     * Time complexity: O(n) average, where n is the number of records
     * Space complexity: O(n) in the worst case for the hash map
     */
    public int[] firstFullyReconciledPair(int[][] records) {
        // This map stores the earliest index at which a specific (vendorId, amount) pair appeared.
        //
        // Why earliest index?
        // If multiple earlier records could match the current record, the problem says any one is acceptable.
        // Storing the earliest one is simple and deterministic.
        Map<RecordKey, Integer> firstSeenIndex = new HashMap<>();

        // We scan records from left to right.
        // The first time we can form a valid pair, that current index j is the earliest possible completion.
        for (int j = 0; j < records.length; j++) {
            int vendorId = records[j][0];
            int amount = records[j][1];

            // To reconcile with the current amount, we need an earlier record from the same vendor
            // whose amount is the exact opposite.
            //
            // Example:
            // current = [7, -100]
            // needed earlier = [7, 100]
            RecordKey neededKey = new RecordKey(vendorId, -amount);

            // Step 1: Check whether such an earlier record already exists.
            Integer i = firstSeenIndex.get(neededKey);

            // If found, then records[i] and records[j] form a valid pair.
            // Because we are scanning j from left to right, this is the earliest completed pair.
            if (i != null) {
                return new int[] { i, j };
            }

            // Step 2: If no match exists yet, remember the current record for future records.
            //
            // We only store the first occurrence of each exact (vendorId, amount) combination.
            // This keeps the earliest possible index for deterministic output.
            RecordKey currentKey = new RecordKey(vendorId, amount);
            firstSeenIndex.putIfAbsent(currentKey, j);
        }

        // If we finish scanning and never find a match, no reconciled pair exists.
        return new int[] { -1, -1 };
    }

    /**
     * Converts an integer array into a readable string such as "[1, 3]".
     *
     * @param array the array to print
     * @return a human-readable string representation of the array
     * Time complexity: O(k), where k is the array length
     * Space complexity: O(k) for the produced string
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input records
     * - the computed answer
     * - the expected answer
     *
     * This also serves as a simple correctness check for the provided examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) across the demonstrated examples
     * Space complexity: O(n) due to the algorithm's hash map usage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] records1 = {
            {7, 100},
            {3, 50},
            {7, -100},
            {7, 100},
            {3, -20}
        };

        int[][] records2 = {
            {5, 40},
            {5, 10},
            {8, -40},
            {5, -10},
            {8, 40}
        };

        int[] result1 = solution.firstFullyReconciledPair(records1);
        int[] result2 = solution.firstFullyReconciledPair(records2);

        System.out.println("Example 1:");
        System.out.println("Input: " + Arrays.deepToString(records1));
        System.out.println("Output: " + solution.arrayToString(result1));
        System.out.println("Expected: [0, 2]");
        System.out.println();

        System.out.println("Example 2:");
        System.out.println("Input: " + Arrays.deepToString(records2));
        System.out.println("Output: " + solution.arrayToString(result2));
        System.out.println("Expected: [1, 3]");
        System.out.println();

        // Additional quick sanity checks for beginners:
        int[][] records3 = {
            {1, 10},
            {2, -10},
            {1, 5},
            {1, -5}
        };
        System.out.println("Additional Test 1:");
        System.out.println("Input: " + Arrays.deepToString(records3));
        System.out.println("Output: " + solution.arrayToString(solution.firstFullyReconciledPair(records3)));
        System.out.println("Expected: [2, 3]");
        System.out.println();

        int[][] records4 = {
            {9, 0},
            {9, 0}
        };
        System.out.println("Additional Test 2:");
        System.out.println("Input: " + Arrays.deepToString(records4));
        System.out.println("Output: " + solution.arrayToString(solution.firstFullyReconciledPair(records4)));
        System.out.println("Expected: [0, 1]");
        System.out.println();

        int[][] records5 = {
            {4, 7},
            {4, 8},
            {4, 9}
        };
        System.out.println("Additional Test 3:");
        System.out.println("Input: " + Arrays.deepToString(records5));
        System.out.println("Output: " + solution.arrayToString(solution.firstFullyReconciledPair(records5)));
        System.out.println("Expected: [-1, -1]");
    }

    /**
     * Small helper class used as a composite hash key for:
     * - vendorId
     * - amount
     *
     * We need both fields because:
     * - matching requires the same vendor
     * - and the opposite amount
     *
     * Using a dedicated key class makes the code clear and safe.
     */
    private static final class RecordKey {
        private final int vendorId;
        private final int amount;

        /**
         * Creates a key for one exact record signature: (vendorId, amount).
         *
         * @param vendorId the vendor identifier
         * @param amount the invoice amount
         */
        private RecordKey(int vendorId, int amount) {
            this.vendorId = vendorId;
            this.amount = amount;
        }

        @Override
        public boolean equals(Object obj) {
            // Standard equality checks:
            // 1. same reference -> equal
            // 2. null or different class -> not equal
            // 3. compare both fields
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            RecordKey other = (RecordKey) obj;
            return vendorId == other.vendorId && amount == other.amount;
        }

        @Override
        public int hashCode() {
            // Standard hash combination for two integers.
            return Objects.hash(vendorId, amount);
        }
    }
}