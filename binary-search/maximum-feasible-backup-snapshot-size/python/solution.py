"""
Title: Maximum Feasible Backup Snapshot Size

Problem Description:
A company stores daily database backups in a fixed order. The i-th backup has size
backups[i] gigabytes. To reduce restore complexity, the operations team wants to
group the backups into exactly k contiguous restore bundles. Every backup must belong
to exactly one bundle, and bundles must preserve the original order.

For a chosen snapshot size limit S, a bundle is considered valid only if the total
size of backups inside that bundle is at least S. Since large bundles are harder to
manage, the team wants to know the largest snapshot size limit S such that it is
still possible to partition the array into exactly k contiguous valid bundles.

Return the maximum possible value of S.

In other words, split the array into exactly k non-empty contiguous parts, maximize
the minimum part sum, and return that optimal minimum sum.

This problem is intended to be solved efficiently for large inputs. A brute-force
search over all partitions will time out. We should exploit the monotonic nature of
feasibility: if a value S is achievable, then every smaller value is also achievable.

Constraints:
- 1 <= k <= backups.length <= 200000
- 1 <= backups[i] <= 1000000000
- The answer fits in a 64-bit signed integer

Example 1:
Input: backups = [7,2,5,10,8], k = 2
Output: 14
Explanation: One optimal partition is [7,2,5] and [10,8], whose sums are 14 and 18.
The minimum bundle sum is 14. It is impossible to make both bundle sums at least 15.

Example 2:
Input: backups = [4,4,4,4,4,4,4], k = 3
Output: 8
Explanation: We can partition as [4,4], [4,4], [4,4,4], giving bundle sums 8, 8, and 12.
So 8 is feasible. A limit of 9 is not feasible because at least one of the three
contiguous bundles would have sum less than 9.
"""

from typing import List


class Solution:
    def can_make_at_least_k_parts(self, backups: List[int], k: int, target: int) -> bool:
        """
        Check whether it is possible to split the array into at least k contiguous parts
        such that every part has sum >= target.

        Important idea:
        - If we can create at least k valid parts, then we can always merge some adjacent
          parts (if needed) to end up with exactly k parts.
        - Merging valid parts keeps the sum >= target, so "at least k" is enough to prove
          feasibility for "exactly k".

        Greedy strategy:
        - Scan from left to right and keep a running sum.
        - Whenever the running sum reaches target, immediately cut a part there.
        - This is optimal for maximizing the number of valid parts because cutting as early
          as possible leaves as much remaining sum as possible for future parts.

        Args:
            backups: List of backup sizes.
            k: Required number of contiguous parts.
            target: Candidate minimum part sum we want every part to satisfy.

        Returns:
            True if target is feasible, otherwise False.

        Time complexity:
            O(n), where n is len(backups), because we scan the array once.

        Space complexity:
            O(1), because we use only a few variables.
        """
        # This variable stores the sum of the current in-progress contiguous part.
        current_sum: int = 0

        # This counts how many valid parts we have successfully formed so far.
        parts_formed: int = 0

        # We process backups in their original order because parts must be contiguous
        # and order must be preserved.
        for size in backups:
            # Add the current backup to the running sum of the current part.
            current_sum += size

            # As soon as the current part reaches the required target,
            # we greedily finalize this part.
            #
            # Why finalize immediately?
            # Because this creates the smallest possible valid part, which leaves the
            # maximum possible remaining sum for future parts. That helps us form as
            # many valid parts as possible.
            if current_sum >= target:
                parts_formed += 1
                current_sum = 0

                # Small optimization:
                # If we already formed at least k valid parts, target is feasible.
                # We can stop early without scanning the rest.
                if parts_formed >= k:
                    return True

        # If we finish scanning and formed fewer than k valid parts, target is not feasible.
        return False

    def max_feasible_snapshot_size(self, backups: List[int], k: int) -> int:
        """
        Compute the maximum possible minimum part sum when splitting backups into exactly
        k non-empty contiguous parts.

        Core insight:
        - Let answer be the largest value S such that we can partition into exactly k parts
          and every part has sum >= S.
        - Feasibility is monotonic:
            * If S is feasible, then any smaller value is also feasible.
            * If S is not feasible, then any larger value is also not feasible.
        - This monotonic property allows binary search on the answer.

        Binary search range:
        - Lower bound = 1, because every backup size is at least 1.
        - Upper bound = sum(backups) // k.
          Why?
          If all k parts must have sum at least S, then total sum must be at least k * S.
          So S cannot exceed total_sum // k.

        Args:
            backups: List of backup sizes.
            k: Exact number of contiguous parts.

        Returns:
            The maximum feasible minimum part sum.

        Time complexity:
            O(n log M), where:
            - n is len(backups)
            - M is the answer search range, at most sum(backups) // k
          Each binary search step performs one O(n) feasibility scan.

        Space complexity:
            O(1), excluding input storage.
        """
        # Compute the total sum once.
        total_sum: int = sum(backups)

        # Binary search boundaries:
        # left  = definitely possible lower candidate range start
        # right = definitely impossible to exceed upper candidate range end
        #
        # We use 1 instead of min(backups) because the minimum part sum can be smaller
        # than some individual elements depending on partitioning, but since all values
        # are positive, 1 is always a safe lower bound.
        left: int = 1
        right: int = total_sum // k

        # This variable stores the best feasible answer found so far.
        answer: int = 1

        # Standard binary search on the answer space.
        while left <= right:
            # Try the middle candidate.
            mid: int = (left + right) // 2

            # Check whether it is possible to make at least k valid parts
            # with each part sum >= mid.
            if self.can_make_at_least_k_parts(backups, k, mid):
                # mid is feasible, so it is a valid candidate answer.
                answer = mid

                # Since we want the maximum feasible value, search to the right
                # for a possibly larger feasible minimum sum.
                left = mid + 1
            else:
                # mid is not feasible, so any larger value is also not feasible.
                # Search the smaller half.
                right = mid - 1

        return answer

    def solve(self, backups: List[int], k: int) -> int:
        """
        Public wrapper method for the problem.

        Args:
            backups: List of backup sizes.
            k: Exact number of contiguous parts.

        Returns:
            Maximum possible minimum part sum.

        Time complexity:
            O(n log M), where n is len(backups) and M is the search range.

        Space complexity:
            O(1), excluding input storage.
        """
        return self.max_feasible_snapshot_size(backups, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    backups1: List[int] = [7, 2, 5, 10, 8]
    k1: int = 2
    result1: int = solution.solve(backups1, k1)
    print("Example 1 Result:", result1)  # Expected: 14

    # Example 2
    backups2: List[int] = [4, 4, 4, 4, 4, 4, 4]
    k2: int = 3
    result2: int = solution.solve(backups2, k2)
    print("Example 2 Result:", result2)  # Expected: 8

    # Additional quick sanity checks
    backups3: List[int] = [5]
    k3: int = 1
    result3: int = solution.solve(backups3, k3)
    print("Single Element Result:", result3)  # Expected: 5

    backups4: List[int] = [1, 2, 3, 4, 5]
    k4: int = 5
    result4: int = solution.solve(backups4, k4)
    print("Each Element Separate Result:", result4)  # Expected: 1