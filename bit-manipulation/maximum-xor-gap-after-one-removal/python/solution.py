"""
Title: Maximum XOR Gap After One Removal

Problem Description:
You are given an array of non-negative integers nums. Define the XOR gap of a set of
numbers as the maximum value of a XOR b over all distinct pairs (a, b) in that set.
Your task is to remove exactly one element from nums so that the XOR gap of the
remaining elements is as large as possible. Return that maximum possible XOR gap.

In other words, for each possible index i, imagine deleting nums[i], then compute the
maximum XOR of any two different remaining values. Among all choices of i, return the
largest such result.

If after removing one element fewer than two numbers remain, the XOR gap is defined
to be 0.

A brute-force solution that recomputes the best pair after every removal is too slow
for large inputs. A strong solution should take advantage of binary representations
and shared prefixes between numbers.

Constraints:
- 1 <= nums.length <= 10^5
- 0 <= nums[i] <= 10^9
- Values may repeat

Examples:
1) nums = [3, 10, 5, 25]
   Output: 28
   Explanation: Remove 10, leaving [3, 5, 25]. The best pair is 5 XOR 25 = 28.

2) nums = [8, 1, 2]
   Output: 10
   Explanation: If you remove 1, the remaining numbers are [8, 2], and their XOR is 10.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import List, Optional, Tuple


@dataclass
class TrieNode:
    """
    Node used in a binary trie.

    Attributes:
        child0: Pointer to the child representing bit 0.
        child1: Pointer to the child representing bit 1.
        count: Number of values currently passing through this node.
    """

    child0: Optional["TrieNode"] = None
    child1: Optional["TrieNode"] = None
    count: int = 0


class BinaryTrie:
    """
    Binary trie that supports insertion, deletion, and maximum XOR query.

    This trie stores integers by their binary representation from the most
    significant bit down to the least significant bit.
    """

    def __init__(self, max_bit: int = 30) -> None:
        """
        Initialize the trie.

        Args:
            max_bit: Highest bit position to process.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.root: TrieNode = TrieNode()
        self.max_bit: int = max_bit

    def insert(self, num: int) -> None:
        """
        Insert one number into the trie.

        Args:
            num: The number to insert.

        Returns:
            None

        Time complexity:
            O(B), where B is the number of processed bits

        Space complexity:
            O(B) in the worst case for newly created nodes
        """
        node: TrieNode = self.root
        node.count += 1

        # We walk from the highest bit to the lowest bit.
        # This is the standard way to build a binary trie for XOR problems.
        for bit in range(self.max_bit, -1, -1):
            current_bit: int = (num >> bit) & 1

            if current_bit == 0:
                if node.child0 is None:
                    node.child0 = TrieNode()
                node = node.child0
            else:
                if node.child1 is None:
                    node.child1 = TrieNode()
                node = node.child1

            node.count += 1

    def remove(self, num: int) -> None:
        """
        Remove one occurrence of a number from the trie.

        Args:
            num: The number to remove.

        Returns:
            None

        Time complexity:
            O(B), where B is the number of processed bits

        Space complexity:
            O(1) extra
        """
        node: TrieNode = self.root
        node.count -= 1

        # We follow the exact same path used during insertion and decrement counts.
        # We do not physically delete nodes because that is unnecessary for correctness.
        for bit in range(self.max_bit, -1, -1):
            current_bit: int = (num >> bit) & 1
            if current_bit == 0:
                node = node.child0  # type: ignore[assignment]
            else:
                node = node.child1  # type: ignore[assignment]
            node.count -= 1

    def max_xor_with(self, num: int) -> int:
        """
        Compute the maximum XOR obtainable between num and any value currently in the trie.

        Args:
            num: The query number.

        Returns:
            The maximum XOR value against numbers stored in the trie.

        Time complexity:
            O(B), where B is the number of processed bits

        Space complexity:
            O(1)
        """
        if self.root.count == 0:
            return 0

        node: TrieNode = self.root
        answer: int = 0

        # Greedy XOR logic:
        # At each bit, to maximize XOR, we prefer to go to the opposite bit if possible.
        # Example:
        # - If current bit of num is 0, we prefer a stored 1.
        # - If current bit of num is 1, we prefer a stored 0.
        #
        # This works because higher bits contribute more to the final value than lower bits.
        for bit in range(self.max_bit, -1, -1):
            current_bit: int = (num >> bit) & 1

            if current_bit == 0:
                preferred: Optional[TrieNode] = node.child1
                fallback: Optional[TrieNode] = node.child0
            else:
                preferred = node.child0
                fallback = node.child1

            if preferred is not None and preferred.count > 0:
                answer |= 1 << bit
                node = preferred
            else:
                node = fallback  # type: ignore[assignment]

        return answer


class Solution:
    def _max_pair_xor(self, values: List[int]) -> int:
        """
        Compute the maximum XOR among all distinct pairs in the given list.

        This method uses a binary trie:
        1. Insert all numbers.
        2. For each number, temporarily remove it.
        3. Query the best XOR partner among the remaining numbers.
        4. Reinsert the number.
        5. Track the best result.

        This guarantees that a number is never paired with itself unless another
        equal copy still remains in the trie, which is allowed because pairs are
        between different positions / occurrences.

        Args:
            values: List of numbers for which we want the maximum pair XOR.

        Returns:
            The maximum XOR over all distinct pairs. If fewer than two values exist,
            returns 0.

        Time complexity:
            O(n * B), where n is len(values) and B is the number of processed bits

        Space complexity:
            O(n * B) in the worst case for the trie
        """
        n: int = len(values)

        # If there are fewer than two numbers, no pair exists.
        if n < 2:
            return 0

        trie: BinaryTrie = BinaryTrie(max_bit=30)

        # Insert every value into the trie once.
        for num in values:
            trie.insert(num)

        best: int = 0

        # For each value:
        # - Remove it so we do not accidentally pair the element with itself.
        # - Ask the trie for the best possible XOR partner among the remaining values.
        # - Put it back so the next iteration sees the full multiset again.
        for num in values:
            trie.remove(num)
            candidate: int = trie.max_xor_with(num)
            if candidate > best:
                best = candidate
            trie.insert(num)

        return best

    def maximum_xor_gap_after_one_removal(self, nums: List[int]) -> int:
        """
        Return the largest possible XOR gap after removing exactly one element.

        Key observation:
        After removing one element, the remaining set is just some subset of size n - 1.
        We want the maximum pair XOR inside that remaining set.

        Let the globally best pair in the original array be formed by two occurrences
        at positions p and q. If n >= 3, we can remove any third element that is not
        p or q, and that best pair still remains. Therefore:
        - If n >= 3, the answer is exactly the maximum pair XOR of the original array.
        - If n <= 2, after removing one element fewer than two numbers remain, so answer is 0.

        This reduces the whole problem to one standard maximum pair XOR computation.

        Args:
            nums: Input list of non-negative integers.

        Returns:
            The maximum possible XOR gap after removing exactly one element.

        Time complexity:
            O(n * B), where n is len(nums) and B is the number of processed bits

        Space complexity:
            O(n * B) in the worst case
        """
        n: int = len(nums)

        # If we remove one element from a list of size 1 or 2,
        # fewer than two elements remain, so the XOR gap is defined as 0.
        if n <= 2:
            return 0

        # Very important reasoning:
        # Since at least 3 elements exist, any best pair in the original array can be preserved
        # by removing some other element. So the answer is simply the maximum pair XOR in nums.
        return self._max_pair_xor(nums)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # nums = [3, 10, 5, 25]
    # Best removable choice is 10, remaining [3, 5, 25], best pair XOR = 5 ^ 25 = 28
    nums1: List[int] = [3, 10, 5, 25]
    result1: int = solution.maximum_xor_gap_after_one_removal(nums1)
    print(f"Input: {nums1}")
    print(f"Output: {result1}")
    print("Expected: 28")
    print()

    # Example 2:
    # nums = [8, 1, 2]
    # Remove 1, remaining [8, 2], XOR gap = 8 ^ 2 = 10
    nums2: List[int] = [8, 1, 2]
    result2: int = solution.maximum_xor_gap_after_one_removal(nums2)
    print(f"Input: {nums2}")
    print(f"Output: {result2}")
    print("Expected: 10")
    print()

    # Additional sanity checks
    nums3: List[int] = [1]
    result3: int = solution.maximum_xor_gap_after_one_removal(nums3)
    print(f"Input: {nums3}")
    print(f"Output: {result3}")
    print("Expected: 0")
    print()

    nums4: List[int] = [4, 7]
    result4: int = solution.maximum_xor_gap_after_one_removal(nums4)
    print(f"Input: {nums4}")
    print(f"Output: {result4}")
    print("Expected: 0")
    print()

    nums5: List[int] = [0, 0, 0]
    result5: int = solution.maximum_xor_gap_after_one_removal(nums5)
    print(f"Input: {nums5}")
    print(f"Output: {result5}")
    print("Expected: 0")