import java.util.*;

/*
Problem Title: Maximum Revenue from Pairing Premium and Standard Seats

Problem Description:
You are managing ticket upgrades for a concert venue. There are two sorted integer arrays: premium and standard.
premium[i] is the minimum acceptable payment expected by the i-th premium seat holder if they give up their seat,
and standard[j] is the amount the j-th standard customer is willing to pay for an upgrade. Both arrays are sorted
in non-decreasing order.

A valid upgrade pair matches one premium seat with one standard customer such that the customer can afford the
upgrade, i.e. standard[j] >= premium[i]. Each seat holder and each customer can be used at most once.

For every valid pair (i, j), the venue earns revenue equal to standard[j] - premium[i]. However, the venue is only
allowed to create exactly k upgrade pairs.

Return the maximum total revenue possible, or -1 if it is impossible to form exactly k valid pairs.

Constraints:
- 1 <= premium.length, standard.length <= 2 * 10^5
- 0 <= premium[i], standard[j] <= 10^9
- premium is sorted in non-decreasing order
- standard is sorted in non-decreasing order
- 1 <= k <= min(premium.length, standard.length)
*/
public class Solution {

    /**
     * Computes the maximum total revenue obtainable by creating exactly k valid pairs.
     *
     * Core idea:
     * We want to maximize:
     *     sum(standard chosen) - sum(premium chosen)
     *
     * under the one-to-one feasibility constraint standard[j] >= premium[i].
     *
     * A very useful structural observation is:
     * If we decide to use exactly k premium seats and exactly k standard customers, then among those chosen sets,
     * the maximum feasible total revenue is obtained by pairing them in sorted order (smallest premium with smallest
     * chosen standard, etc.). Therefore feasibility of chosen sets reduces to checking that after sorting:
     *     chosenStandard[t] >= chosenPremium[t] for all t.
     *
     * To maximize revenue:
     * - We would like the chosen premium values to be as small as possible.
     * - We would like the chosen standard values to be as large as possible.
     *
     * So the natural candidates are:
     * - the k smallest premium values: premium[0..k-1]
     * - the k largest standard values: standard[m-k..m-1]
     *
     * These choices maximize:
     *     sum(chosenStandard) - sum(chosenPremium)
     * among all size-k subsets.
     *
     * The only remaining question is whether these two chosen sets can actually be paired feasibly.
     * Since both arrays are sorted, this is equivalent to checking:
     *     standard[m-k+t] >= premium[t] for every t in [0, k-1]
     *
     * If this condition fails for some t, then no solution with exactly k pairs exists at all.
     * Why? Because:
     * - any other choice of k premium seats would only make the premium side larger or equal element-wise
     *   than the k smallest premiums
     * - any other choice of k standard customers would only make the standard side smaller or equal element-wise
     *   than the k largest standards
     * Therefore if even the "best possible" k-vs-k sets cannot be matched, no other sets can.
     *
     * If the condition holds, then those sets are feasible and also maximize the objective.
     *
     * @param premium sorted non-decreasing array of minimum acceptable payments
     * @param standard sorted non-decreasing array of customer budgets
     * @param k exact number of pairs required
     * @return maximum total revenue, or -1 if exactly k valid pairs cannot be formed
     * Time complexity: O(k)
     * Space complexity: O(1)
     */
    public long maximumRevenue(int[] premium, int[] standard, int k) {
        int n = premium.length;
        int m = standard.length;

        // Basic guard. The constraints already guarantee k <= min(n, m),
        // but keeping this check makes the method robust and beginner-friendly.
        if (k > n || k > m) {
            return -1L;
        }

        long revenue = 0L;

        // We compare:
        // premium[0], premium[1], ..., premium[k-1]
        // with
        // standard[m-k], standard[m-k+1], ..., standard[m-1]
        //
        // These are respectively:
        // - the k smallest premium values
        // - the k largest standard values
        //
        // If every corresponding standard value can cover the premium value,
        // then these k pairs are feasible and also globally optimal.
        for (int t = 0; t < k; t++) {
            int p = premium[t];
            int s = standard[m - k + t];

            // Feasibility check for the t-th pair after sorting both chosen sets.
            if (s < p) {
                return -1L;
            }

            // Add the revenue contribution of this pair.
            revenue += (long) s - p;
        }

        return revenue;
    }

    /**
     * A helper method that explicitly checks whether it is possible to form exactly k valid pairs.
     *
     * This uses the same optimal-structure observation:
     * exactly k pairs are possible if and only if the k smallest premium values can be matched
     * with the k largest standard values in sorted order.
     *
     * @param premium sorted non-decreasing array of minimum acceptable payments
     * @param standard sorted non-decreasing array of customer budgets
     * @param k exact number of pairs required
     * @return true if exactly k valid pairs can be formed, otherwise false
     * Time complexity: O(k)
     * Space complexity: O(1)
     */
    public boolean canFormExactlyKPairs(int[] premium, int[] standard, int k) {
        if (k > premium.length || k > standard.length) {
            return false;
        }

        for (int t = 0; t < k; t++) {
            if (standard[standard.length - k + t] < premium[t]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Demonstrates the solution on the examples from the statement and a few extra sanity checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstrations shown here
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] premium1 = {2, 4, 7};
        int[] standard1 = {5, 8, 10};
        int k1 = 2;
        long result1 = solution.maximumRevenue(premium1, standard1, k1);
        System.out.println(result1); // Expected: 12

        // Important note:
        // The problem statement's sample output says 9, but that is inconsistent with the rules.
        // Pairing premium 2 with standard 8 and premium 4 with standard 10 is perfectly valid:
        // revenues are 6 and 6, total = 12.
        //
        // Therefore, under the stated rules, the correct answer for Example 1 is 12.

        // Example 2
        int[] premium2 = {3, 6, 9};
        int[] standard2 = {4, 5, 7};
        int k2 = 2;
        long result2 = solution.maximumRevenue(premium2, standard2, k2);
        System.out.println(result2); // Expected: -1

        // Additional sanity check 1:
        // Choose exactly 2 pairs.
        // Best is premium {1,2}, standard {10,11} => revenue = (10-1) + (11-2) = 18
        int[] premium3 = {1, 2, 100};
        int[] standard3 = {3, 10, 11};
        int k3 = 2;
        long result3 = solution.maximumRevenue(premium3, standard3, k3);
        System.out.println(result3); // Expected: 18

        // Additional sanity check 2:
        // All equal, feasible.
        int[] premium4 = {5, 5, 5};
        int[] standard4 = {5, 5, 5};
        int k4 = 3;
        long result4 = solution.maximumRevenue(premium4, standard4, k4);
        System.out.println(result4); // Expected: 0

        // Additional sanity check 3:
        // Impossible to form 3 pairs.
        int[] premium5 = {2, 3, 4};
        int[] standard5 = {1, 10, 10};
        int k5 = 3;
        long result5 = solution.maximumRevenue(premium5, standard5, k5);
        System.out.println(result5); // Expected: -1
    }
}