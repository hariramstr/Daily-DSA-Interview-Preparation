"""
Title: Maximum Starter Batch for Subscription Trials

Problem Description:
A product team is preparing trial kits for a new subscription launch. There are n warehouses,
and the i-th warehouse can provide kits[i] starter kits. Every customer trial batch must contain
exactly the same number of kits, and a single batch can only be assembled using kits from one
warehouse. However, one warehouse may be split into multiple batches as long as the total number
of kits used from that warehouse does not exceed its inventory.

Given an integer array kits where kits[i] is the number of available starter kits in warehouse i,
and an integer m representing the number of customer batches that must be created, return the
maximum possible number of kits in each batch.

If it is impossible to create m non-empty batches, return 0.

This problem should be solved efficiently for large inputs. A brute-force search over all possible
batch sizes will be too slow. Think about how the answer changes as the candidate batch size
increases, and use that monotonic behavior to design a binary search solution.

Constraints:
- 1 <= kits.length <= 100000
- 1 <= kits[i] <= 1000000000
- 1 <= m <= 1000000000000
- The answer fits in a 32-bit signed integer

Example 1:
Input: kits = [9, 7, 5], m = 5
Output: 3
Explanation: Using batch size 3, the warehouses can produce 3 + 2 + 1 = 6 batches, which is enough.
Batch size 4 would produce only 2 + 1 + 1 = 4 batches, which is not enough. So the maximum valid size is 3.

Example 2:
Input: kits = [2, 4, 6], m = 7
Output: 1
Explanation: With batch size 1, we can create 12 batches in total. With batch size 2, we can create
only 1 + 2 + 3 = 6 batches, which is fewer than 7. Therefore, the largest possible batch size is 1.
"""

from typing import List


class Solution:
    def _can_make_at_least_m_batches(self, kits: List[int], m: int, batch_size: int) -> bool:
        """
        Check whether it is possible to create at least m batches using the given batch size.

        Args:
            kits: List of warehouse inventories.
            m: Required number of batches.
            batch_size: Candidate number of kits per batch.

        Returns:
            True if at least m batches can be formed, otherwise False.

        Time complexity:
            O(n), where n is the number of warehouses.

        Space complexity:
            O(1), excluding input storage.
        """
        # This variable will accumulate how many total batches can be formed
        # if every batch must contain exactly `batch_size` kits.
        total_batches: int = 0

        # We inspect each warehouse independently.
        # Why independently?
        # Because the problem states that a single batch can only come from one warehouse.
        # That means for a warehouse with `stock` kits, the number of full batches it can
        # contribute is simply `stock // batch_size`.
        for stock in kits:
            total_batches += stock // batch_size

            # Important optimization:
            # As soon as we already know we can make at least `m` batches,
            # we can stop early and return True.
            # This avoids unnecessary work on large inputs.
            if total_batches >= m:
                return True

        # If we finish the loop and still have fewer than `m` batches,
        # then this batch size is not feasible.
        return False

    def maximumBatchSize(self, kits: List[int], m: int) -> int:
        """
        Return the maximum possible number of kits in each batch.

        The method uses binary search on the answer. If a batch size `x` is feasible,
        then every smaller batch size is also feasible. This monotonic property makes
        binary search the correct and efficient approach.

        Args:
            kits: List where kits[i] is the number of available starter kits in warehouse i.
            m: Number of customer batches that must be created.

        Returns:
            The maximum valid batch size, or 0 if it is impossible to create m non-empty batches.

        Time complexity:
            O(n log M), where n is the number of warehouses and M is the maximum value in kits.

        Space complexity:
            O(1), excluding input storage.
        """
        # First, compute the total number of kits across all warehouses.
        # This helps us quickly detect an impossible case.
        total_kits: int = sum(kits)

        # If the total number of kits is less than m, then even making batches of size 1
        # would not be enough to create m non-empty batches.
        # In that case, the answer must be 0.
        if total_kits < m:
            return 0

        # Binary search boundaries:
        #
        # Lowest possible valid batch size is 1, because batches must be non-empty.
        left: int = 1

        # Highest possible batch size can be bounded by:
        # 1) max(kits): no single batch can be larger than the largest warehouse inventory,
        # 2) total_kits // m: if each batch had size larger than this, then m such batches
        #    would require more kits than exist in total.
        #
        # Using the minimum of these two gives a tighter upper bound, which can slightly
        # improve performance.
        right: int = min(max(kits), total_kits // m)

        # This variable stores the best feasible answer found so far.
        answer: int = 0

        # Standard binary search over the answer space.
        #
        # Invariant:
        # - If `mid` is feasible, we try larger values to maximize the answer.
        # - If `mid` is not feasible, we try smaller values.
        while left <= right:
            # Compute the middle candidate safely.
            mid: int = left + (right - left) // 2

            # Check whether this candidate batch size can produce at least m batches.
            if self._can_make_at_least_m_batches(kits, m, mid):
                # `mid` works, so it is a valid candidate answer.
                answer = mid

                # Since we want the maximum possible batch size,
                # search the right half for a potentially larger valid size.
                left = mid + 1
            else:
                # `mid` does not work, so any larger size also cannot work
                # because larger batch sizes produce fewer batches.
                # Therefore, search the left half.
                right = mid - 1

        # After binary search finishes, `answer` holds the largest feasible batch size.
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # kits = [9, 7, 5], m = 5
    # Batch size 3 -> 9//3 + 7//3 + 5//3 = 3 + 2 + 1 = 6 batches, enough
    # Batch size 4 -> 9//4 + 7//4 + 5//4 = 2 + 1 + 1 = 4 batches, not enough
    # Expected output: 3
    kits1: List[int] = [9, 7, 5]
    m1: int = 5
    result1: int = solution.maximumBatchSize(kits1, m1)
    print(f"Input: kits = {kits1}, m = {m1}")
    print(f"Output: {result1}")
    print("Expected: 3")
    print()

    # Example 2:
    # kits = [2, 4, 6], m = 7
    # Batch size 1 -> 2 + 4 + 6 = 12 batches, enough
    # Batch size 2 -> 1 + 2 + 3 = 6 batches, not enough
    # Expected output: 1
    kits2: List[int] = [2, 4, 6]
    m2: int = 7
    result2: int = solution.maximumBatchSize(kits2, m2)
    print(f"Input: kits = {kits2}, m = {m2}")
    print(f"Output: {result2}")
    print("Expected: 1")
    print()

    # Additional sample: impossible case
    # Total kits = 3, but need 5 non-empty batches, so answer is 0.
    kits3: List[int] = [1, 1, 1]
    m3: int = 5
    result3: int = solution.maximumBatchSize(kits3, m3)
    print(f"Input: kits = {kits3}, m = {m3}")
    print(f"Output: {result3}")
    print("Expected: 0")