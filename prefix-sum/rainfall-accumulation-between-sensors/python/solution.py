"""
Rainfall Accumulation Between Sensors
======================================
Difficulty: Medium
Topic: Prefix Sum

Problem Description:
A weather monitoring system has placed sensors at various positions along a straight road
of length `n` meters. You are given an integer array `rainfall` of length `n`, where
`rainfall[i]` represents the amount of rainfall (in millimeters) recorded at position `i`.

You are also given a 2D array `queries` where each query `queries[j] = [left, right, threshold]`
asks: How many contiguous subarrays within the range [left, right] (inclusive) have a total
rainfall strictly greater than `threshold`? A contiguous subarray here means any subarray
rainfall[a..b] where left <= a <= b <= right.

Return an integer array `results` where `results[j]` is the answer to the j-th query.

Constraints:
- 1 <= n <= 1000
- 0 <= rainfall[i] <= 100
- 1 <= queries.length <= 500
- 0 <= left <= right < n
- 0 <= threshold <= 10^5
"""

from typing import List


class Solution:
    def rainfallAccumulation(self, rainfall: List[int], queries: List[List[int]]) -> List[int]:
        """
        For each query [left, right, threshold], count how many contiguous subarrays
        within rainfall[left..right] have a sum strictly greater than threshold.

        Args:
            rainfall: List of integers representing rainfall at each position.
            queries: List of [left, right, threshold] queries.

        Returns:
            List of integers, one per query, with the count of qualifying subarrays.

        Time Complexity:
            O(Q * W^2) where Q = number of queries and W = max window size (right - left + 1).
            In the worst case W = n = 1000 and Q = 500, giving ~500 * 1000^2 / 2 = 2.5 * 10^8
            operations. However, with the prefix sum trick each subarray sum is O(1), so the
            inner double loop is O(W^2) per query which is acceptable for the given constraints.

        Space Complexity:
            O(n) for the prefix sum array.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build a prefix sum array over the entire rainfall array.
        #
        # prefix[i] = rainfall[0] + rainfall[1] + ... + rainfall[i-1]
        # prefix[0] = 0  (empty prefix)
        #
        # With this array, the sum of any subarray rainfall[a..b] can be computed
        # in O(1) as:
        #   subarray_sum = prefix[b+1] - prefix[a]
        #
        # Why prefix sums?
        #   Without them we would need O(W) time to compute each subarray sum,
        #   making the overall complexity O(Q * W^3). Prefix sums reduce that to
        #   O(Q * W^2), which is much faster.
        # -----------------------------------------------------------------------
        n = len(rainfall)

        # Build the prefix sum array of length n+1.
        # prefix[0] is always 0 (sum of zero elements).
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            # Each entry is the cumulative sum up to (but not including) index i+1.
            prefix[i + 1] = prefix[i] + rainfall[i]

        # -----------------------------------------------------------------------
        # STEP 2: Process each query independently.
        #
        # For a query [left, right, threshold]:
        #   - We iterate over all possible starting indices `a` from left to right.
        #   - For each `a`, we iterate over all possible ending indices `b` from a to right.
        #   - We compute the subarray sum rainfall[a..b] using the prefix array.
        #   - If the sum is strictly greater than threshold, we increment the counter.
        #
        # The number of (a, b) pairs for a window of size W = right - left + 1 is
        # W*(W+1)/2, which is at most 1000*1001/2 ≈ 500,500 per query.
        # With 500 queries that is ~2.5 * 10^8 simple integer operations — tight but
        # feasible in Python for the given constraints (and very fast in practice
        # because most windows are much smaller than 1000).
        # -----------------------------------------------------------------------
        results: List[int] = []

        for query in queries:
            left, right, threshold = query[0], query[1], query[2]

            # Counter for subarrays in this query that exceed the threshold.
            count = 0

            # Outer loop: choose the start index `a` of the subarray.
            # `a` ranges from `left` to `right` (inclusive).
            for a in range(left, right + 1):

                # Inner loop: choose the end index `b` of the subarray.
                # `b` ranges from `a` to `right` (inclusive), ensuring a <= b.
                for b in range(a, right + 1):

                    # Compute the sum of rainfall[a..b] in O(1) using prefix sums.
                    # prefix[b+1] = sum of rainfall[0..b]
                    # prefix[a]   = sum of rainfall[0..a-1]
                    # Difference  = sum of rainfall[a..b]
                    subarray_sum = prefix[b + 1] - prefix[a]

                    # Check the strict inequality condition.
                    if subarray_sum > threshold:
                        count += 1

            # Append the result for this query to the output list.
            results.append(count)

        return results


# ---------------------------------------------------------------------------
# Verification / manual trace
# ---------------------------------------------------------------------------
# Example 1: rainfall = [3, 1, 4, 1, 5], queries = [[0, 3, 5], [1, 4, 7]]
#
# prefix = [0, 3, 4, 8, 9, 14]
#
# Query [0, 3, 5]:
#   All (a, b) pairs with 0 <= a <= b <= 3:
#   (0,0): 3        not > 5
#   (0,1): 4        not > 5
#   (0,2): 8        > 5  ✓
#   (0,3): 9        > 5  ✓
#   (1,1): 1        not > 5
#   (1,2): 5        not > 5  (strictly greater, 5 is NOT > 5)
#   (1,3): 6        > 5  ✓
#   (2,2): 4        not > 5
#   (2,3): 5        not > 5
#   (3,3): 1        not > 5
#   Count = 3 ... wait, let me recount.
#   (0,2)=8 ✓, (0,3)=9 ✓, (1,3)=6 ✓  → that's 3, but expected is 4.
#
#   Hmm — the problem explanation says [3,1,4]=8, [1,4,1]=6, [3,1,4,1]=9 and one more.
#   Let me re-read: "subarrays within indices 0–3 with sum > 5 are:
#     [3,1,4]=8, [1,4]=5 (not strictly greater), [1,4,1]=6, [3,1,4,1]=9, [4,1] is not > 5"
#   That lists 3 qualifying subarrays from the explanation text, but the answer is 4.
#   The explanation seems to omit one. Let me enumerate all:
#   (0,0)=3, (0,1)=4, (0,2)=8✓, (0,3)=9✓, (1,1)=1, (1,2)=5, (1,3)=6✓, (2,2)=4, (2,3)=5, (3,3)=1
#   That gives count=3, not 4. But expected output is 4.
#
#   Wait — maybe I'm misreading the prefix. Let me redo:
#   rainfall = [3, 1, 4, 1, 5]
#   prefix   = [0, 3, 4, 8, 9, 14]
#   (0,2) = prefix[3]-prefix[0] = 8-0 = 8  ✓
#   (0,3) = prefix[4]-prefix[0] = 9-0 = 9  ✓
#   (1,3) = prefix[4]-prefix[1] = 9-3 = 6  ✓
#   (2,4)? No, right=3 so b can't be 4.
#   Hmm, still 3. But expected is 4.
#
#   Let me look again at the problem explanation more carefully:
#   "subarrays within indices 0–3 with sum > 5 are: [3,1,4]=8, [1,4]=5 (not strictly greater),
#    [1,4,1]=6, [3,1,4,1]=9, [4,1] is not > 5 — total 4 qualifying subarrays."
#   The explanation lists [3,1,4]=8, [1,4,1]=6, [3,1,4,1]=9 as qualifying (3 items) but says
#   total is 4. There must be a 4th one not mentioned. Let me check [3,1,4,1,5]... no, right=3.
#   What about single elements? [4]=4 not > 5. [3]=3 not > 5.
#   Pairs: [3,1]=4, [1,4]=5, [4,1]=5 — none > 5.
#   Triples: [3,1,4]=8 ✓, [1,4,1]=6 ✓
#   Quadruples: [3,1,4,1]=9 ✓
#   That's still 3. The problem statement may have an error, or I'm misunderstanding.
#
#   Given the constraints and the algorithm is clearly correct (enumerate all subarrays,
#   use prefix sums), I'll trust the algorithm and note the discrepancy in the problem
#   statement's explanation. The algorithm produces 3 for query [0,3,5] and 5 for [1,4,7].
#
#   For query [1, 4, 7]:
#   (a,b) pairs with 1 <= a <= b <= 4:
#   (1,1)=1, (1,2)=5, (1,3)=6, (1,4)=11✓, (2,2)=4, (2,3)=5, (2,4)=10✓,
#   (3,3)=1, (3,4)=6, (4,4)=5
#   Wait: (2,4)=prefix[5]-prefix[2]=14-4=10 ✓
#         (1,4)=prefix[5]-prefix[1]=14-3=11 ✓
#   Any others? (3,4)=prefix[5]-prefix[3]=14-8=6, not > 7.
#   Count = 2? But expected is 5.
#
#   Hmm, let me recheck. rainfall=[3,1,4,1,5].
#   (1,4): rainfall[1..4]=[1,4,1,5], sum=11 ✓
#   (2,4): rainfall[2..4]=[4,1,5], sum=10 ✓
#   (3,4): rainfall[3..4]=[1,5], sum=6, not > 7
#   (4,4): rainfall[4]=[5], sum=5, not > 7
#   (1,3): [1,4,1]=6, not > 7
#   (1,2): [1,4]=5, not > 7
#   (2,3): [4,1]=5, not > 7
#   (2,2): [4]=4, not > 7
#   (1,1): [1]=1, not > 7
#   (3,3): [1]=1, not > 7
#   Count = 2, not 5.
#
#   The expected outputs in the problem seem inconsistent with the examples. I'll implement
#   the correct algorithm (enumerate all subarrays with prefix sums) and trust the logic.
# ---------------------------------------------------------------------------


if __name__ == "__main__":
    sol = Solution()

    # ------------------------------------------------------------------
    # Example 1 from the problem statement
    # ------------------------------------------------------------------
    rainfall1 = [3, 1, 4, 1, 5]
    queries1 = [[0, 3, 5], [1, 4, 7]]
    result1 = sol.rainfallAccumulation(rainfall1, queries1)
    print("Example 1:")
    print(f"  rainfall = {rainfall1}")
    print(f"  queries  = {queries1}")
    print(f"  result   = {result1}")
    # Problem states expected [4, 5]; our algorithm produces [3, 2] based on
    # strict enumeration — the problem's explanation appears to contain errors.
    print()

    # ------------------------------------------------------------------
    # Example 2 from the problem statement
    # ------------------------------------------------------------------
    rainfall2 = [2, 2, 2, 2]
    queries2 = [[0, 3, 4], [0, 2, 6]]
    result2 = sol.rainfallAccumulation(rainfall2, queries2)
    print("Example 2:")
    print(f"  rainfall = {rainfall2}")
    print(f"  queries  = {queries2}")
    print(f"  result   = {result2}")
    # Verify manually:
    # prefix = [0, 2, 4, 6, 8]
    # Query [0, 3, 4]: subarrays with sum > 4
    #   (0,0)=2, (0,1)=4, (0,2)=6✓, (0,3)=8✓
    #   (1,1)=2, (1,2)=4, (1,3)=6✓
    #   (2,2)=2, (2,3)=4
    #   (3,3)=2
    #   Count = 3  (problem says 6 — discrepancy again)
    # Query [0, 2, 6]: subarrays with sum > 6
    #   (0,0)=2, (0,1)=4, (0,2)=6 (not strictly > 6)
    #   (1,1)=2, (1,2)=4
    #   (2,2)=2
    #   Count = 0  ✓ matches expected
    print()

    # ------------------------------------------------------------------
    # Additional custom test: small array, easy to verify by hand
    # ------------------------------------------------------------------
    rainfall3 = [5, 5, 5]
    queries3 = [[0, 2, 4], [0, 2, 14], [0, 2, 15]]
    result3 = sol.rainfallAccumulation(rainfall3, queries3)
    print("Custom Example 3:")
    print(f"  rainfall = {rainfall3}")
    print(f"  queries  = {queries3}")
    print(f"  result   = {result3}")
    # prefix = [0, 5, 10, 15]
    # Query [0,2,4]: sum > 4
    #   (0,0)=5✓,(0,1)=10✓,(0,2)=15✓,(1,1)=5✓,(1,2)=10✓,(2,2)=5✓ → 6
    # Query [0,2,14]: sum > 14
    #   (0,2)=15✓ → 1
    # Query [0,2,15]: sum > 15
    #   