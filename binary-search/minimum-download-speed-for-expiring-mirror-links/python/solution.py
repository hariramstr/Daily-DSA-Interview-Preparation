"""
Title: Minimum Download Speed for Expiring Mirror Links

Problem Description:
You are given a list of file downloads that must be completed using a single downloader.
The downloader processes the files in the given order, and it can work on only one file
at a time. File i has size sizes[i] megabytes and an expiration time expires[i] minutes.

If you choose a constant download speed of S megabytes per minute, then file i requires
ceil(sizes[i] / S) whole minutes to finish. A file is considered successful only if the
cumulative time spent downloading files 0 through i is less than or equal to expires[i].

Your task is to find the minimum integer download speed S such that all files can be
completed before their mirror links expire. If no speed can make the schedule feasible,
return -1.

The order of files cannot be changed, and partial minutes still count as a full minute
for each individual file. This makes the feasibility condition non-trivial, because
increasing S changes multiple rounded durations at once. The key observation is that if
a speed S is feasible, then any speed greater than S is also feasible, which allows a
binary search over the answer.
"""

from typing import List


class Solution:
    def _is_feasible(self, sizes: List[int], expires: List[int], speed: int) -> bool:
        """
        Check whether a given constant download speed allows all files to finish
        on or before their expiration times.

        Args:
            sizes: List of file sizes in megabytes.
            expires: List of expiration deadlines in minutes.
            speed: Candidate download speed in megabytes per minute.

        Returns:
            True if every file can be completed by its deadline at this speed,
            otherwise False.

        Time complexity:
            O(n), where n is the number of files, because we scan the arrays once.

        Space complexity:
            O(1), because we use only a few extra variables.
        """
        # This variable stores the total time spent so far after downloading files
        # from index 0 up to the current index.
        cumulative_time = 0

        # We process files in the required fixed order.
        for i in range(len(sizes)):
            # Compute ceil(sizes[i] / speed) using integer arithmetic.
            #
            # Why use this formula?
            #   ceil(a / b) can be computed as (a + b - 1) // b for positive integers.
            #
            # Example:
            #   size = 10, speed = 3
            #   ceil(10 / 3) = 4
            #   (10 + 3 - 1) // 3 = 12 // 3 = 4
            time_for_this_file = (sizes[i] + speed - 1) // speed

            # Add this file's required whole-minute time to the running total.
            cumulative_time += time_for_this_file

            # If the cumulative completion time exceeds the file's expiration time,
            # then this speed is not sufficient.
            #
            # We can immediately stop and return False because once one file misses
            # its deadline, the entire schedule is invalid.
            if cumulative_time > expires[i]:
                return False

        # If we finish the loop without violating any deadline, the speed works.
        return True

    def min_download_speed(self, sizes: List[int], expires: List[int]) -> int:
        """
        Find the minimum integer download speed that allows all files to finish
        before their mirror links expire.

        Args:
            sizes: List of file sizes in megabytes.
            expires: List of expiration deadlines in minutes.

        Returns:
            The minimum feasible integer speed, or -1 if no speed can ever work.

        Time complexity:
            O(n log M), where:
            - n is the number of files
            - M is the search range of speeds
            We do a binary search over speed, and each feasibility check is O(n).

        Space complexity:
            O(1), excluding input storage.
        """
        n = len(sizes)

        # ------------------------------------------------------------
        # Step 1: Quick impossibility check
        # ------------------------------------------------------------
        #
        # Even with infinitely large speed, each file still takes at least 1 whole minute,
        # because ceil(size / very_large_speed) is still at least 1 for any positive size.
        #
        # Therefore, the absolute best possible cumulative completion times are:
        #   1, 2, 3, ..., n
        #
        # If for any file i, expires[i] < i + 1, then even the theoretical best case
        # cannot meet that deadline, so the answer must be -1.
        for i in range(n):
            if expires[i] < i + 1:
                return -1

        # ------------------------------------------------------------
        # Step 2: Find a valid upper bound for binary search
        # ------------------------------------------------------------
        #
        # We need a search interval [left, right] such that:
        # - left is definitely too small or is the smallest possible candidate
        # - right is definitely feasible
        #
        # The smallest possible speed is 1.
        left = 1

        # Start with a modest upper bound and repeatedly double it until it becomes feasible.
        #
        # Why is this safe?
        # - Feasibility is monotonic: if a speed works, any larger speed also works.
        # - Since we already passed the impossibility check, some sufficiently large speed
        #   must work.
        #
        # This avoids needing a tricky closed-form upper bound and is robust for very large
        # input values.
        right = 1
        while not self._is_feasible(sizes, expires, right):
            right *= 2

        # ------------------------------------------------------------
        # Step 3: Binary search for the minimum feasible speed
        # ------------------------------------------------------------
        #
        # Invariant:
        # - There exists at least one feasible speed in [left, right]
        # - right is feasible
        #
        # We repeatedly test the middle speed:
        # - If mid is feasible, the answer could be mid or smaller, so move right down.
        # - If mid is not feasible, we need a larger speed, so move left up.
        while left < right:
            mid = (left + right) // 2

            if self._is_feasible(sizes, expires, mid):
                right = mid
            else:
                left = mid + 1

        # At the end, left == right and points to the minimum feasible speed.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    sizes1 = [8, 5, 10]
    expires1 = [3, 5, 9]
    result1 = solution.min_download_speed(sizes1, expires1)
    print("Example 1:")
    print("sizes =", sizes1)
    print("expires =", expires1)
    print("Minimum download speed =", result1)
    print("Expected = 3")
    print()

    # Example 2
    sizes2 = [4, 4, 4]
    expires2 = [1, 2, 2]
    result2 = solution.min_download_speed(sizes2, expires2)
    print("Example 2:")
    print("sizes =", sizes2)
    print("expires =", expires2)
    print("Minimum download speed =", result2)
    print("Expected = -1")
    print()