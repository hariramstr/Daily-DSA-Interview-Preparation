"""
Title: Minimum Energy to Read a Shelf of Books

Problem Description:
You are given an array `energy` where `energy[i]` represents the energy cost required
to read book `i` on a shelf. You want to finish reading all books by reaching the
last book.

You start before the first book, and on each move you may read either the next book
or skip one book and read the book after that. In other words, if you are currently
at position `i`, your next position can be `i + 1` or `i + 2`. When you read a book,
you must pay its energy cost. Your goal is to minimize the total energy spent to
reach the last book.

Return the minimum total energy needed to finish at the last book.

This is a dynamic programming problem because the best cost to reach a book depends
on the best costs to reach the previous one or two books.

Constraints:
- 1 <= energy.length <= 1000
- 0 <= energy[i] <= 10^4
- You must end on the last book
- You begin before index 0, so your first read can be book 0 or book 1 if it exists

Example 1:
Input: energy = [4, 2, 7, 3]
Output: 5
Explanation: Start by reading book 1 (cost 2), then read book 3 (cost 3).
Total energy = 2 + 3 = 5.

Example 2:
Input: energy = [5, 1, 2, 10, 1]
Output: 4
Explanation: One optimal path is to read book 1 (cost 1), book 2 (cost 2),
and book 4 (cost 1). Total energy = 4.
"""

from typing import List


class Solution:
    def min_energy(self, energy: List[int]) -> int:
        """
        Compute the minimum total energy required to finish at the last book.

        We use dynamic programming with constant extra space.
        For each book index i, the minimum cost to reach i is:
            dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])

        Special handling:
        - If there is only one book, we must read it.
        - For index 0, cost is simply energy[0].
        - For index 1, we may start directly at book 1, so cost is energy[1].

        Args:
            energy: A list where energy[i] is the cost to read book i.

        Returns:
            The minimum total energy needed to end on the last book.

        Time complexity:
            O(n), where n is the number of books.

        Space complexity:
            O(1), because we only store the last two DP values.
        """
        # Store the number of books for easier reading and to avoid repeatedly
        # calling len(energy) throughout the method.
        n: int = len(energy)

        # If there is exactly one book, there is no choice:
        # we must read book 0 and finish there.
        if n == 1:
            return energy[0]

        # Dynamic programming idea:
        #
        # Let dp[i] mean:
        # "the minimum total energy needed to land on book i"
        #
        # Since from any position we can move forward by 1 or 2 books,
        # the only ways to reach book i are:
        # - from book i - 1
        # - from book i - 2
        #
        # Therefore:
        # dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])
        #
        # We do not need a full array because each state depends only on the
        # previous two states. So we keep just two variables.

        # Minimum cost to land on book 0:
        # We start before the shelf and can choose book 0 as our first read.
        prev2: int = energy[0]

        # Minimum cost to land on book 1:
        # We are allowed to start directly at book 1, so the cost is just energy[1].
        # This is important and different from some staircase problems where you
        # must pass through earlier steps.
        prev1: int = energy[1]

        # Process books from index 2 up to the last book.
        for i in range(2, n):
            # To land on book i, we must pay energy[i].
            # Before that, we choose the cheaper of:
            # - the best way to reach i - 1
            # - the best way to reach i - 2
            current: int = energy[i] + min(prev1, prev2)

            # Slide the DP window forward:
            # - old prev1 becomes the new prev2
            # - current becomes the new prev1
            prev2 = prev1
            prev1 = current

        # After the loop, prev1 holds the minimum cost to reach the last book.
        return prev1

    def min_energy_with_dp_array(self, energy: List[int]) -> int:
        """
        Compute the minimum total energy required to finish at the last book
        using an explicit DP array for educational clarity.

        Args:
            energy: A list where energy[i] is the cost to read book i.

        Returns:
            The minimum total energy needed to end on the last book.

        Time complexity:
            O(n), where n is the number of books.

        Space complexity:
            O(n), due to the DP array.
        """
        n: int = len(energy)

        if n == 1:
            return energy[0]

        # Create a DP array where dp[i] will store the minimum energy needed
        # to reach book i.
        dp: List[int] = [0] * n

        # Base case for the first book:
        # Starting directly at book 0 costs energy[0].
        dp[0] = energy[0]

        # Base case for the second book:
        # Starting directly at book 1 costs energy[1].
        dp[1] = energy[1]

        # Fill the DP array from left to right.
        for i in range(2, n):
            # The best way to reach book i is to come from the cheaper of
            # the previous two reachable books, then pay the current book's cost.
            dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])

        return dp[-1]


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem statement.
    energy1: List[int] = [4, 2, 7, 3]
    result1: int = solution.min_energy(energy1)
    print(f"Energy: {energy1}")
    print(f"Minimum total energy: {result1}")
    print("Expected: 5")
    print()

    # Sample input 2 from the problem statement.
    energy2: List[int] = [5, 1, 2, 10, 1]
    result2: int = solution.min_energy(energy2)
    print(f"Energy: {energy2}")
    print(f"Minimum total energy: {result2}")
    print("Expected: 4")
    print()

    # Additional small test cases for beginners to inspect.
    energy3: List[int] = [7]
    result3: int = solution.min_energy(energy3)
    print(f"Energy: {energy3}")
    print(f"Minimum total energy: {result3}")
    print("Expected: 7")
    print()

    energy4: List[int] = [3, 8]
    result4: int = solution.min_energy(energy4)
    print(f"Energy: {energy4}")
    print(f"Minimum total energy: {result4}")
    print("Expected: 8")
    print()

    # Verification using the DP-array version as well.
    print("Verification with DP array method:")
    print(solution.min_energy_with_dp_array([4, 2, 7, 3]))
    print(solution.min_energy_with_dp_array([5, 1, 2, 10, 1]))