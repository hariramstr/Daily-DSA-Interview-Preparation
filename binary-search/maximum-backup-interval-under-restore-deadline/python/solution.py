"""
Title: Maximum Backup Interval Under Restore Deadline

Problem Description:
A storage team wants to reduce the number of full backups taken for a large database.
The database changes over time, and the amount of changed data on day i is given by
changes[i]. If the team chooses a backup interval of k days, then every backup taken
on day s must include the total changed data from days s through
min(s + k - 1, n - 1). Restoring from any single backup is only allowed if the size
of that backup does not exceed a system restore limit L.

The team always partitions the timeline into consecutive backup blocks of length at
most k: the first backup covers days 0 to k - 1, the next covers the following k
days, and so on. A backup interval k is considered feasible if every such backup
block has total changed data at most L.

Your task is to return the maximum feasible backup interval k.

In other words, among all positive integers k, find the largest k such that for every
block formed by splitting the array into consecutive chunks of size k (the last chunk
may be shorter), the sum of each chunk is at most L.

This problem is designed to reward an efficient solution. A brute-force check over all
k values and all ranges will be too slow for the largest inputs. You should exploit
the monotonic nature of feasibility with respect to k and combine binary search with
fast range-sum validation.

Constraints:
- 1 <= n == changes.length <= 200000
- 0 <= changes[i] <= 1000000000
- 0 <= L <= 1000000000000000000
- Return 0 if no positive backup interval is feasible

Important note about the examples:
The written explanation for Example 1 says k = 3 is feasible and therefore the answer
should be 3. That is correct:
- changes = [2, 1, 3, 2, 2], L = 6
- k = 3 -> blocks [2,1,3], [2,2] -> sums 6 and 4 -> both <= 6
So the correct output for Example 1 is 3, even though one line in the prompt says 2.

Example 1:
Input: changes = [2, 1, 3, 2, 2], L = 6
Correct Output: 3

Example 2:
Input: changes = [7, 1, 2], L = 6
Output: 0
"""

from typing import List


class Solution:
    def _build_prefix_sums(self, changes: List[int]) -> List[int]:
        """
        Build a prefix sum array for fast range-sum queries.

        Args:
            changes: List of daily changed data amounts.

        Returns:
            A prefix sum array where prefix[i] is the sum of the first i elements.
            This means:
            - prefix[0] = 0
            - sum of changes[left:right] = prefix[right] - prefix[left]

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        # We create a prefix sum array of length n + 1.
        # The extra leading 0 makes range sum calculations very clean.
        prefix: List[int] = [0] * (len(changes) + 1)

        # Fill the prefix sums one position at a time.
        # prefix[i + 1] stores the sum of changes[0] through changes[i].
        for i, value in enumerate(changes):
            prefix[i + 1] = prefix[i] + value

        return prefix

    def _is_feasible(self, k: int, prefix: List[int], limit: int) -> bool:
        """
        Check whether a given backup interval k is feasible.

        A k is feasible if every consecutive chunk of size k
        (with the final chunk possibly shorter) has sum <= limit.

        Args:
            k: Candidate backup interval.
            prefix: Prefix sum array for fast chunk-sum queries.
            limit: Maximum allowed backup size.

        Returns:
            True if k is feasible, otherwise False.

        Time complexity:
            O(n / k) chunk checks, which is O(n) in the worst case

        Space complexity:
            O(1) auxiliary space
        """
        n: int = len(prefix) - 1

        # We step through the array in jumps of size k.
        # Each step represents one backup block:
        # [start, min(start + k, n))
        #
        # Using prefix sums, the sum of that block is:
        # prefix[end] - prefix[start]
        #
        # If any block exceeds the restore limit, k is not feasible.
        start: int = 0
        while start < n:
            end: int = min(start + k, n)
            block_sum: int = prefix[end] - prefix[start]

            if block_sum > limit:
                return False

            start += k

        # If every block passed the limit check, k is feasible.
        return True

    def maximum_backup_interval(self, changes: List[int], L: int) -> int:
        """
        Return the maximum feasible backup interval.

        We use binary search because feasibility is monotonic:
        - If a larger k is feasible, then every smaller k is also feasible.
          Why? Because all values are non-negative, so splitting a valid block into
          smaller consecutive blocks cannot increase any chunk sum.
        - Therefore, the feasible k values form a prefix: 1..answer.

        Args:
            changes: List of daily changed data amounts.
            L: Maximum allowed sum for any backup block.

        Returns:
            The largest feasible positive integer k.
            Returns 0 if no positive k is feasible.

        Time complexity:
            O(n log n) in the worst case

        Space complexity:
            O(n)
        """
        n: int = len(changes)

        # First, build prefix sums once.
        # This allows each chunk sum to be computed in O(1) time.
        prefix: List[int] = self._build_prefix_sums(changes)

        # Quick rejection:
        # If k = 1 is not feasible, then no positive k can be feasible.
        # For k = 1, each block is just one element, so feasibility requires
        # every single changes[i] <= L.
        if not self._is_feasible(1, prefix, L):
            return 0

        # Binary search over k in the range [1, n].
        #
        # We want the maximum feasible k.
        # Standard pattern:
        # - If mid is feasible, move right to search for a larger feasible k.
        # - Otherwise, move left.
        left: int = 1
        right: int = n
        answer: int = 1

        while left <= right:
            mid: int = (left + right) // 2

            # Check whether this candidate interval works.
            if self._is_feasible(mid, prefix, L):
                # mid works, so record it and try to find an even larger one.
                answer = mid
                left = mid + 1
            else:
                # mid does not work, so all larger values also do not work.
                right = mid - 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # The explanation clearly shows that k = 3 is feasible:
    # blocks are [2,1,3] and [2,2], sums are 6 and 4, both <= 6.
    # Therefore the correct answer is 3.
    changes1: List[int] = [2, 1, 3, 2, 2]
    limit1: int = 6
    result1: int = solution.maximum_backup_interval(changes1, limit1)
    print("Example 1 result:", result1)  # Expected: 3

    # Example 2 from the prompt.
    # k = 1 already fails because the first element is 7 > 6.
    # Therefore no positive k is feasible.
    changes2: List[int] = [7, 1, 2]
    limit2: int = 6
    result2: int = solution.maximum_backup_interval(changes2, limit2)
    print("Example 2 result:", result2)  # Expected: 0

    # Additional quick sanity checks.
    changes3: List[int] = [0, 0, 0, 0]
    limit3: int = 0
    result3: int = solution.maximum_backup_interval(changes3, limit3)
    print("All zeros result:", result3)  # Expected: 4

    changes4: List[int] = [5]
    limit4: int = 5
    result4: int = solution.maximum_backup_interval(changes4, limit4)
    print("Single day exact fit result:", result4)  # Expected: 1

    changes5: List[int] = [5]
    limit5: int = 4
    result5: int = solution.maximum_backup_interval(changes5, limit5)
    print("Single day too large result:", result5)  # Expected: 0