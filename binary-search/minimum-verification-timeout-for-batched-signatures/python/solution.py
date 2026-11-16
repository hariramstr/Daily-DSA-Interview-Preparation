"""
Title: Minimum Verification Timeout for Batched Signatures

Problem Description:
A security platform verifies digital signatures in the order they arrive. The i-th
signature requires verifyTime[i] milliseconds of CPU time, and it must finish no
later than deadline[i] milliseconds from the start of the batch. The platform can
choose a single fixed timeout limit T for all worker threads. Under this limit,
each thread may spend at most T milliseconds on one signature before moving on, so
a signature requiring x milliseconds consumes ceil(x / T) time slices. Each time
slice takes exactly T milliseconds, and signatures are still processed sequentially
in the original order.

For a chosen T, the completion time of signature i is:
sum over j from 0 to i of ceil(verifyTime[j] / T) * T

We must find the smallest integer timeout T such that every signature completes on
or before its deadline. Deadlines are guaranteed to be nondecreasing, and at least
one valid T exists.

Typical approach:
- Binary search on T
- For each candidate T, run a linear feasibility check
"""

from typing import List


class Solution:
    def is_feasible(self, verify_time: List[int], deadline: List[int], timeout: int) -> bool:
        """
        Check whether a given timeout value allows all signatures to finish
        by their deadlines.

        Args:
            verify_time: List of CPU times required for each signature.
            deadline: Nondecreasing list of deadlines for each signature.
            timeout: Candidate timeout T to test.

        Returns:
            True if all signatures finish on time with this timeout, otherwise False.

        Time complexity:
            O(n), where n is the number of signatures.

        Space complexity:
            O(1), ignoring input storage.
        """
        # This variable stores the running completion time of the current prefix
        # of signatures. Since signatures are processed strictly in order, the
        # completion time of signature i depends on the total time spent on all
        # signatures from 0 to i.
        current_time: int = 0

        # We process each signature one by one in arrival order.
        for i in range(len(verify_time)):
            # For a signature requiring verify_time[i] milliseconds, and a fixed
            # timeout "timeout", the number of slices needed is:
            #
            #   ceil(verify_time[i] / timeout)
            #
            # Instead of using floating point math, we use the standard integer
            # formula for ceiling division:
            #
            #   ceil(a / b) = (a + b - 1) // b
            #
            # This is exact, fast, and avoids precision issues.
            slices_needed: int = (verify_time[i] + timeout - 1) // timeout

            # Each slice costs exactly "timeout" milliseconds, so the total time
            # consumed by this signature is:
            #
            #   slices_needed * timeout
            #
            # We add that to the running completion time.
            current_time += slices_needed * timeout

            # If the completion time of the current signature exceeds its deadline,
            # then this timeout is not feasible.
            #
            # We can return immediately because processing more signatures will only
            # increase the total time, never decrease it.
            if current_time > deadline[i]:
                return False

        # If we finished the loop without violating any deadline, then this timeout
        # works for all signatures.
        return True

    def minimum_timeout(self, verify_time: List[int], deadline: List[int]) -> int:
        """
        Find the minimum integer timeout T such that all signatures complete
        by their deadlines.

        Args:
            verify_time: List of CPU times required for each signature.
            deadline: Nondecreasing list of deadlines for each signature.

        Returns:
            The smallest valid integer timeout T.

        Time complexity:
            O(n log M), where:
            - n is the number of signatures
            - M is the binary search range for T
            In this implementation, M is bounded by max(verify_time).

        Space complexity:
            O(1), ignoring input storage.
        """
        # Important observation:
        #
        # For a single job of size x, the charged time is:
        #   ceil(x / T) * T
        #
        # If T > x, then ceil(x / T) = 1, so charged time becomes T, which is
        # actually larger than x. Therefore, making T too large can make things worse.
        #
        # Also, when T = x, charged time is exactly x.
        #
        # This means the answer never needs to be larger than max(verify_time),
        # because any T larger than that would make every job cost exactly T,
        # which cannot improve over using T = max(verify_time) or smaller.
        #
        # Since the problem guarantees that at least one valid T exists, searching
        # in [1, max(verify_time)] is sufficient.
        left: int = 1
        right: int = max(verify_time)

        # We perform a standard binary search for the minimum feasible timeout.
        #
        # Invariant:
        # - There exists at least one feasible value in [left, right]
        # - We shrink the range until left == right
        while left < right:
            # Midpoint candidate.
            mid: int = (left + right) // 2

            # Test whether this timeout works.
            if self.is_feasible(verify_time, deadline, mid):
                # If mid is feasible, then the answer could be mid or something smaller.
                # So we keep the left half, including mid.
                right = mid
            else:
                # If mid is not feasible, then every value <= mid is also not guaranteed
                # to be feasible for this problem's structure, and binary search here
                # relies on the intended monotonic behavior described by the problem.
                # Therefore, we move to the right half.
                left = mid + 1

        # At loop end, left == right and points to the minimum feasible timeout.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the statement.
    # The statement itself corrects the initially listed output and explains that
    # the true minimum valid timeout is 1.
    verify_time_1: List[int] = [5, 8, 6]
    deadline_1: List[int] = [10, 20, 30]
    result_1: int = solution.minimum_timeout(verify_time_1, deadline_1)
    print("Example 1 result:", result_1)  # Expected: 1

    # Example 2 as corrected by the statement to satisfy the guarantee that
    # at least one valid answer exists.
    verify_time_2: List[int] = [7, 7, 7]
    deadline_2: List[int] = [7, 20, 27]
    result_2: int = solution.minimum_timeout(verify_time_2, deadline_2)
    print("Example 2 result:", result_2)  # Expected: 7

    # Additional small sanity check.
    verify_time_3: List[int] = [3, 4, 5]
    deadline_3: List[int] = [3, 7, 12]
    result_3: int = solution.minimum_timeout(verify_time_3, deadline_3)
    print("Additional test result:", result_3)