```python
"""
Title: Maximum Distinct Prime Factors in a Subarray Window
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given an integer array `nums` and two integers `k` and `maxUnique`.
A subarray of length exactly `k` is called **prime-rich** if the total count of
distinct prime factors across all elements in the subarray is at most `maxUnique`.

A prime factor of a number `n` is any prime `p` such that `n % p == 0`.
For example, 12 has prime factors {2, 3}, and 30 has prime factors {2, 3, 5}.

Your task is to find the **maximum sum** of a subarray of length exactly `k`
such that the subarray is prime-rich (i.e., the union of all distinct prime factors
of all elements does not exceed `maxUnique` distinct primes).

If no such subarray exists, return `-1`.

Constraints:
- 1 <= nums.length <= 10^5
- 1 <= nums[i] <= 10^5
- 1 <= k <= nums.length
- 1 <= maxUnique <= 15
"""

from typing import List, Dict, Set
from collections import defaultdict


class Solution:
    def get_prime_factors(self, n: int) -> Set[int]:
        """
        Compute the set of distinct prime factors of a given integer n.

        Args:
            n: A positive integer whose prime factors we want to find.

        Returns:
            A set of distinct prime integers that divide n.

        Time complexity: O(sqrt(n)) per call
        Space complexity: O(log n) for the result set (at most log2(n) prime factors)

        Example:
            get_prime_factors(12) -> {2, 3}
            get_prime_factors(30) -> {2, 3, 5}
            get_prime_factors(1)  -> {}
        """
        # We'll collect prime factors in a set to ensure uniqueness
        factors: Set[int] = set()

        # Handle the factor 2 separately so we can then check only odd numbers
        if n % 2 == 0:
            factors.add(2)
            # Divide out all 2s
            while n % 2 == 0:
                n //= 2

        # Now check odd factors from 3 up to sqrt(n)
        # If n has a prime factor > sqrt(n), it can only be one such factor
        # (the remaining n after dividing out smaller primes)
        divisor = 3
        while divisor * divisor <= n:
            if n % divisor == 0:
                factors.add(divisor)
                # Divide out all occurrences of this prime
                while n % divisor == 0:
                    n //= divisor
            divisor += 2  # Only check odd numbers

        # If n > 1 at this point, n itself is a prime factor
        if n > 1:
            factors.add(n)

        return factors

    def max_sum_prime_rich_subarray(
        self, nums: List[int], k: int, maxUnique: int
    ) -> int:
        """
        Find the maximum sum of a length-k subarray whose union of distinct
        prime factors across all elements has size at most maxUnique.

        Approach:
        ---------
        We use a sliding window of fixed size k.
        To efficiently track how many distinct primes are in the current window,
        we maintain a frequency dictionary: prime_count[p] = how many elements
        in the current window have p as a prime factor.

        When prime_count[p] drops to 0, prime p is no longer in the window.
        The number of distinct primes in the window = len(prime_count).

        Steps:
        1. Precompute prime factors for each element.
        2. Initialize the first window of size k.
        3. Slide the window: add the new right element, remove the leftmost element.
        4. After each valid window, check if distinct prime count <= maxUnique.
        5. Track the maximum sum among all valid windows.

        Args:
            nums:      The input array of positive integers.
            k:         The exact length of the subarray window.
            maxUnique: Maximum allowed number of distinct prime factors in the window.

        Returns:
            The maximum sum of a valid prime-rich subarray, or -1 if none exists.

        Time complexity:
            O(n * sqrt(M)) where M = max(nums[i]) = 10^5, n = len(nums).
            Precomputing prime factors: O(n * sqrt(M)).
            Sliding window: O(n * D) where D = average number of distinct primes
            per element (very small in practice, at most ~6 for numbers up to 10^5).

        Space complexity:
            O(n * D) for storing prime factors of each element,
            O(P) for the prime_count dictionary where P = number of distinct primes
            in the current window (at most maxUnique + a few extras).
        """

        n = len(nums)

        # -----------------------------------------------------------------------
        # STEP 1: Precompute prime factors for every element in nums.
        # This avoids recomputing them each time an element enters/leaves the window.
        # We store a list of sets: prime_factors[i] = set of primes dividing nums[i].
        # -----------------------------------------------------------------------
        prime_factors: List[Set[int]] = []
        for num in nums:
            prime_factors.append(self.get_prime_factors(num))

        # -----------------------------------------------------------------------
        # STEP 2: Initialize data structures for the sliding window.
        #
        # prime_count: maps each prime p -> how many elements in the current window
        #              have p as a prime factor.
        # When prime_count[p] becomes 0, we remove p from the dict entirely,
        # so len(prime_count) always equals the number of distinct primes in window.
        #
        # current_sum: the sum of elements in the current window.
        # max_sum: the best valid sum found so far (initialized to -1 = "not found").
        # -----------------------------------------------------------------------
        prime_count: Dict[int, int] = defaultdict(int)
        current_sum: int = 0
        max_sum: int = -1

        # -----------------------------------------------------------------------
        # STEP 3: Build the initial window covering indices [0, k-1].
        # We add each element's prime factors to prime_count and accumulate the sum.
        # -----------------------------------------------------------------------
        for i in range(k):
            # Add this element's value to the running sum
            current_sum += nums[i]
            # For each prime factor of nums[i], increment its count in the window
            for p in prime_factors[i]:
                prime_count[p] += 1

        # -----------------------------------------------------------------------
        # STEP 4: Check if the initial window is valid (prime-rich condition).
        # len(prime_count) gives the number of distinct primes currently in window.
        # -----------------------------------------------------------------------
        if len(prime_count) <= maxUnique:
            max_sum = current_sum

        # -----------------------------------------------------------------------
        # STEP 5: Slide the window from left=0 to left=n-k-1.
        # At each step:
        #   - The window currently covers [left, left+k-1].
        #   - We remove element at index `left` (the outgoing element).
        #   - We add element at index `left+k` (the incoming element).
        #   - Then the window covers [left+1, left+k].
        # -----------------------------------------------------------------------
        for left in range(n - k):
            # Index of the element leaving the window (leftmost element)
            outgoing_idx = left
            # Index of the element entering the window (new rightmost element)
            incoming_idx = left + k

            # --- Remove the outgoing element from the window ---
            # Subtract its value from the running sum
            current_sum -= nums[outgoing_idx]
            # Decrement the count of each of its prime factors
            for p in prime_factors[outgoing_idx]:
                prime_count[p] -= 1
                # If count drops to 0, this prime is no longer in the window.
                # Remove it from the dict so len(prime_count) stays accurate.
                if prime_count[p] == 0:
                    del prime_count[p]

            # --- Add the incoming element to the window ---
            # Add its value to the running sum
            current_sum += nums[incoming_idx]
            # Increment the count of each of its prime factors
            for p in prime_factors[incoming_idx]:
                prime_count[p] += 1

            # --- Check validity of the new window ---
            # The window is prime-rich if the number of distinct primes <= maxUnique
            if len(prime_count) <= maxUnique:
                # Update the best sum if this window's sum is larger
                if current_sum > max_sum:
                    max_sum = current_sum

        # -----------------------------------------------------------------------
        # STEP 6: Return the result.
        # If max_sum is still -1, no valid window was found.
        # -----------------------------------------------------------------------
        return max_sum


# -------------------------------------------------------------------------------
# Main block: Test the solution with the provided examples and additional cases.
# -------------------------------------------------------------------------------
if __name__ == "__main__":
    sol = Solution()

    # ---------------------------------------------------------------------------
    # Example 1 (corrected in problem statement):
    # nums = [6, 10, 15, 4, 9, 14], k = 3, maxUnique = 3
    #
    # Let's trace all windows of size 3:
    # [6, 10, 15]  -> 6={2,3}, 10={2,5}, 15={3,5} -> union={2,3,5}, size=3 <= 3, sum=31
    # [10, 15, 4]  -> 10={2,5}, 15={3,5}, 4={2}   -> union={2,3,5}, size=3 <= 3, sum=29
    # [15, 4, 9]   -> 15={3,5}, 4={2}, 9={3}       -> union={2,3,5}, size=3 <= 3, sum=28
    # [4, 9, 14]   -> 4={2}, 9={3}, 14={2,7}       -> union={2,3,7}, size=3 <= 3, sum=27
    #
    # Maximum valid sum = 31 from [6, 10, 15]
    # ---------------------------------------------------------------------------
    nums1 = [6, 10, 15, 4, 9, 14]
    k1, maxUnique1 = 3, 3
    result1 = sol.max_sum_prime_rich_subarray(nums1, k1, maxUnique1)
    print(f"Example 1: nums={nums1}, k={k1}, maxUnique={maxUnique1}")
    print(f"  Result: {result1}  (Expected: 31)")
    print()

    # ---------------------------------------------------------------------------
    # Example 2:
    # nums = [2, 3, 5, 7, 11, 13], k = 2, maxUnique = 2
    #
    # Each element is prime, so each element has exactly 1 distinct prime factor
    # (itself). Every window of size 2 has exactly 2 distinct primes.
    # 2 <= 2, so all windows are valid.
    #
    # Windows and sums:
    # [2, 3]   -> {2,3}, sum=5
    # [3, 5]   -> {3,5}, sum=8
    # [5, 7]   -> {5,7}, sum=12
    # [7, 11]  -> {7,11}, sum=18
    # [11, 13] -> {11,13}, sum=24  <-- maximum
    #
    # Expected output: 24
    # ---------------------------------------------------------------------------
    nums2 = [2, 3, 5, 7, 11, 13]
    k2, maxUnique2 = 2, 2
    result2 = sol.max_sum_prime_rich_subarray(nums2, k2, maxUnique2)
    print(f"Example 2: nums={nums2}, k={k2}, maxUnique={maxUnique2}")
    print(f"  Result: {result2}  (Expected: 24)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Test 1: No valid window exists
    # nums = [2, 3, 5, 7], k = 4, maxUnique = 2
    # The only window is [2,3,5,7] with 4 distinct primes > 2.
    # Expected: -1
    # ---------------------------------------------------------------------------
    nums3 = [2, 3, 5, 7]
    k3, maxUnique3 = 4, 2
    result3 = sol.max_sum_prime_rich_subarray(nums3, k3, maxUnique3)
    print(f"Additional Test 1: nums={nums3}, k={k3}, maxUnique={maxUnique3}")
    print(f"  Result: {result3}  (Expected: -1)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Test 2: All elements are 1 (no prime factors)
    # nums = [1, 1, 1, 1], k = 3, maxUnique = 0
    # 1 has no prime factors, so every window has 0 distinct primes <= 0.
    # All windows have sum 3. Expected: 3
    # ---------------------------------------------------------------------------
    nums4 = [1, 1, 1, 1]
    k4, maxUnique4 = 3, 0
    result4 = sol.max_sum_prime_rich_subarray(nums4, k4, maxUnique4)
    print(f"Additional Test 2: nums={nums4}, k={k4}, maxUnique={maxUnique4}")
    print(f"  Result: {result4}  (Expected: 3)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Test 3: Single element window
    # nums = [4, 9, 25, 49], k = 1, maxUnique = 1
    # 4={2}, 9={3}, 25={5}, 49={7} -> all have exactly 1 prime factor
    # All are valid. Maximum element = 49. Expected: 49
    # ---------------------------------------------------------------------------
    nums5 = [4, 9, 25, 49]
    k5, maxUnique5 = 1, 1
    result5 = sol.max_sum_prime_rich_subarray(nums5, k5, maxUnique5)
    print(f"Additional Test 3: nums={nums5}, k={k5}, maxUnique={maxUnique5}")
    print(f"  Result: {result5}  (Expected: 49)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Test 4: Verify prime factor computation
    # nums = [30, 30, 30], k = 2, maxUnique = 3
    # 30 = 2*3*5, so {2,3,5}. Window of 2 elements: union still {2,3,5}, size=3<=3.
    # All windows valid, sum = 60. Expected: 60
    # ---------------------------------------------------------------------------
    nums6 = [30, 30, 30]
    k6, maxUnique6 = 2, 3
    result6 = sol.max_sum_prime_rich_subarray(nums6, k6, maxUnique6)
    print(f"Additional Test 4: nums={nums6}, k={k6}, maxUnique={maxUnique6}")
    print(f"  Result: {result6}  (Expected: 60)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Test 5: Mixed valid/invalid windows
    # nums = [2, 6, 10, 15, 7], k = 3, maxUnique = 2
    # [2, 6, 10]  -> 2={2}, 6={2,3}, 10={2,5} -> union={2,3,5}, size=3 > 2, INVALID
    # [6, 10, 15] -> 6={2,3}, 10={2,5}, 15={3,5} -> union={2,3,5}, size=3