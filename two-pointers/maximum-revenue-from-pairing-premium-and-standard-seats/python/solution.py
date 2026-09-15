"""
Title: Maximum Revenue from Pairing Premium and Standard Seats

Problem Description:
You are managing ticket upgrades for a concert venue. There are two sorted integer arrays:
`premium` and `standard`.

- `premium[i]` is the minimum acceptable payment expected by the i-th premium seat holder
  if they give up their seat.
- `standard[j]` is the amount the j-th standard customer is willing to pay for an upgrade.

Both arrays are sorted in non-decreasing order.

A valid upgrade pair matches one premium seat with one standard customer such that:
    standard[j] >= premium[i]

Each seat holder and each customer can be used at most once.

For every valid pair (i, j), the venue earns revenue:
    standard[j] - premium[i]

However, the venue is only allowed to create exactly `k` upgrade pairs.

Return the maximum total revenue possible, or -1 if it is impossible to form exactly `k`
valid pairs.

Constraints:
- 1 <= premium.length, standard.length <= 2 * 10^5
- 0 <= premium[i], standard[j] <= 10^9
- premium is sorted in non-decreasing order
- standard is sorted in non-decreasing order
- 1 <= k <= min(premium.length, standard.length)
"""

from typing import List


class Solution:
    def max_revenue(self, premium: List[int], standard: List[int], k: int) -> int:
        """
        Compute the maximum total revenue obtainable by forming exactly k valid pairs.

        Key idea:
        For any chosen set of k premium seats and k standard customers, the maximum total
        revenue under the validity constraint is achieved by pairing them in sorted order.
        Therefore, the global optimum can be found by selecting:
        - the k smallest premium values
        - the k largest standard values
        and then checking whether sorted pairwise feasibility holds:
            selected_standard[i] >= selected_premium[i] for all i

        If feasible, the answer is simply:
            sum(selected_standard) - sum(selected_premium)

        Why this works:
        - To maximize revenue, we want premium values as small as possible and standard
          values as large as possible.
        - Because arrays are sorted, the k smallest premiums are premium[0:k], and the
          k largest standards are standard[m-k:m].
        - A classical exchange argument shows that if any set of k pairs is feasible,
          then pairing the k smallest chosen premiums with the k largest chosen standards
          in sorted order is the best structure.
        - Moreover, feasibility for exactly k pairs is equivalent to:
              standard[m-k+i] >= premium[i] for every i in [0, k-1]
          when using the best-value subsets.

        Args:
            premium: Sorted list of minimum acceptable payments for premium seat holders.
            standard: Sorted list of customer upgrade budgets.
            k: Exact number of pairs that must be formed.

        Returns:
            Maximum total revenue, or -1 if exactly k valid pairs cannot be formed.

        Time complexity:
            O(k)

        Space complexity:
            O(1) extra space
        """
        n: int = len(premium)
        m: int = len(standard)

        # Basic safety check, although the problem guarantees valid k.
        if k > n or k > m:
            return -1

        # ---------------------------------------------------------------------
        # Step 1: Check whether it is even possible to form exactly k valid pairs
        #         using the revenue-maximizing choice of subsets.
        #
        # We choose:
        #   - the k smallest premium values: premium[0], premium[1], ..., premium[k-1]
        #   - the k largest standard values: standard[m-k], ..., standard[m-1]
        #
        # Since both chosen groups are already sorted, the best valid pairing between
        # these two groups is index-by-index.
        #
        # If for any position i we have:
        #       standard[m-k+i] < premium[i]
        # then even the i-th largest selected customer cannot afford the i-th smallest
        # selected premium seat in sorted matching order, so exactly k valid pairs are
        # impossible for this optimal subset structure.
        #
        # In fact, if this condition fails here, no other choice can produce a larger
        # revenue than this subset choice, and feasibility for exactly k pairs also fails
        # for the optimal structure required by the exchange argument.
        # ---------------------------------------------------------------------
        for i in range(k):
            if standard[m - k + i] < premium[i]:
                return -1

        # ---------------------------------------------------------------------
        # Step 2: Since feasibility holds, compute the maximum revenue.
        #
        # Revenue of each pair is:
        #       standard_value - premium_value
        #
        # Total revenue over k pairs is therefore:
        #       sum(selected_standard) - sum(selected_premium)
        #
        # Because we deliberately selected:
        #   - the largest possible k standard values
        #   - the smallest possible k premium values
        # this total is maximal among all feasible ways to form exactly k pairs.
        # ---------------------------------------------------------------------
        total_premium: int = 0
        for i in range(k):
            total_premium += premium[i]

        total_standard: int = 0
        for i in range(m - k, m):
            total_standard += standard[i]

        return total_standard - total_premium


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    premium_1 = [2, 4, 7]
    standard_1 = [5, 8, 10]
    k_1 = 2
    result_1 = solution.max_revenue(premium_1, standard_1, k_1)
    print(result_1)  # Expected: 12

    # Example 2
    premium_2 = [3, 6, 9]
    standard_2 = [4, 5, 7]
    k_2 = 2
    result_2 = solution.max_revenue(premium_2, standard_2, k_2)
    print(result_2)  # Expected: -1

    # Additional sanity checks
    premium_3 = [1, 2, 3]
    standard_3 = [3, 4, 5]
    k_3 = 3
    result_3 = solution.max_revenue(premium_3, standard_3, k_3)
    print(result_3)  # Expected: 6

    premium_4 = [5, 6]
    standard_4 = [1, 10]
    k_4 = 1
    result_4 = solution.max_revenue(premium_4, standard_4, k_4)
    print(result_4)  # Expected: 5