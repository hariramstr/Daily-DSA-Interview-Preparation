"""
Title: Count Inventory Plateaus
Difficulty: Easy
Topic: Arrays

Problem Description:
A warehouse records the number of items in stock at the end of each hour. You are given
an integer array `stock`, where `stock[i]` is the inventory count for hour `i`.

A contiguous block of hours is called an inventory plateau if all values in that block
are equal, and the block is maximal, meaning it cannot be extended to the left or right
without changing the value. For example, in `[5, 5, 3, 3, 3, 7]`, the plateaus are
`[5, 5]`, `[3, 3, 3]`, and `[7]`.

Your task is to return the number of plateaus whose length is at least `k`.

In other words, scan the array and group adjacent equal values together. Count how many
of those groups have size greater than or equal to `k`.

This problem is meant to test careful array traversal and handling of contiguous runs.
An efficient solution should run in linear time.

Constraints:
- `1 <= stock.length <= 100000`
- `0 <= stock[i] <= 1000000000`
- `1 <= k <= stock.length`

Example 1:
Input: stock = [4, 4, 4, 2, 2, 9, 1, 1], k = 2
Output: 3
Explanation: The plateaus are `[4,4,4]`, `[2,2]`, `[9]`, and `[1,1]`.
Three of them have length at least 2.

Example 2:
Input: stock = [6, 3, 3, 3, 5, 5, 8], k = 3
Output: 1
Explanation: The plateaus are `[6]`, `[3,3,3]`, `[5,5]`, and `[8]`.
Only `[3,3,3]` has length at least 3.
"""

from typing import List


class Solution:
    def count_inventory_plateaus(self, stock: List[int], k: int) -> int:
        """
        Count how many maximal contiguous groups of equal values have length at least k.

        Args:
            stock: A list of inventory counts recorded hour by hour.
            k: The minimum plateau length required to be counted.

        Returns:
            The number of plateaus whose size is greater than or equal to k.

        Time Complexity:
            O(n), where n is the length of stock, because we scan the array once.

        Space Complexity:
            O(1), because we use only a few extra variables.
        """
        # This variable will store the final answer:
        # how many plateaus (contiguous runs of equal values) have length >= k.
        plateau_count: int = 0

        # The array length is used multiple times, so storing it in a variable
        # makes the code a little cleaner and avoids repeated calls to len(stock).
        n: int = len(stock)

        # We use an index-based traversal because this makes it very natural
        # to detect the start of a plateau and then extend forward until the
        # plateau ends.
        i: int = 0

        # We continue until we have processed every position in the array.
        while i < n:
            # At this moment, index i is the FIRST element of a new plateau.
            # Why is that true?
            # - At the beginning, i = 0, so the first element starts the first plateau.
            # - After processing a plateau, we move i directly to the first index
            #   after that plateau, so it becomes the start of the next plateau.
            current_value: int = stock[i]

            # We now want to measure the full length of the plateau that starts at i.
            # Start j at i and move it right while values remain equal to current_value.
            j: int = i

            # This loop expands the current plateau.
            # It stops when:
            # 1) j reaches the end of the array, or
            # 2) stock[j] is different from current_value, meaning the plateau ended.
            while j < n and stock[j] == current_value:
                j += 1

            # After the loop:
            # - The plateau includes indices [i, i+1, ..., j-1]
            # - j is the first index AFTER the plateau
            # Therefore, the plateau length is j - i.
            plateau_length: int = j - i

            # If this plateau is large enough, count it.
            if plateau_length >= k:
                plateau_count += 1

            # Move i to j so the next iteration starts exactly at the next plateau.
            # This is efficient because every element is visited only once overall.
            i = j

        # After scanning all plateaus, return the total number that met the size rule.
        return plateau_count

    def countPlateaus(self, stock: List[int], k: int) -> int:
        """
        Wrapper method using an alternative camelCase name.

        Args:
            stock: A list of inventory counts recorded hour by hour.
            k: The minimum plateau length required to be counted.

        Returns:
            The number of plateaus whose size is greater than or equal to k.

        Time Complexity:
            O(n), where n is the length of stock.

        Space Complexity:
            O(1).
        """
        return self.count_inventory_plateaus(stock, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement:
    # stock = [4, 4, 4, 2, 2, 9, 1, 1], k = 2
    # Plateaus:
    # [4,4,4] -> length 3, counts
    # [2,2]   -> length 2, counts
    # [9]     -> length 1, does not count
    # [1,1]   -> length 2, counts
    # Expected answer: 3
    stock1: List[int] = [4, 4, 4, 2, 2, 9, 1, 1]
    k1: int = 2
    result1: int = solution.count_inventory_plateaus(stock1, k1)
    print("Example 1 Result:", result1)

    # Example 2 from the problem statement:
    # stock = [6, 3, 3, 3, 5, 5, 8], k = 3
    # Plateaus:
    # [6]       -> length 1, does not count
    # [3,3,3]   -> length 3, counts
    # [5,5]     -> length 2, does not count
    # [8]       -> length 1, does not count
    # Expected answer: 1
    stock2: List[int] = [6, 3, 3, 3, 5, 5, 8]
    k2: int = 3
    result2: int = solution.count_inventory_plateaus(stock2, k2)
    print("Example 2 Result:", result2)

    # Additional simple checks for beginners:
    stock3: List[int] = [5, 5, 3, 3, 3, 7]
    k3: int = 2
    result3: int = solution.count_inventory_plateaus(stock3, k3)
    print("Additional Example Result:", result3)

    stock4: List[int] = [1]
    k4: int = 1
    result4: int = solution.count_inventory_plateaus(stock4, k4)
    print("Single Element Result:", result4)