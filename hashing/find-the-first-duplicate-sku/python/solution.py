"""
Title: Find the First Duplicate SKU

Problem Description:
You are given a list of product SKU codes representing items scanned at a warehouse
receiving station, in the exact order they were scanned. A SKU code is a string
containing letters, digits, or hyphens. Your task is to return the first SKU that
appears more than once in the scan history.

The phrase first duplicate means the duplicate whose second appearance happens earliest
in the list. In other words, scan the list from left to right and return the first SKU
that has already been seen before. If no SKU appears twice, return an empty string.

This problem is useful for detecting the earliest repeated item in a real-time stream of
inventory events. An efficient solution should avoid comparing every pair of strings and
should instead use a hash-based structure to track which SKUs have already appeared.

Constraints:
- 1 <= skus.length <= 100000
- 1 <= skus[i].length <= 50
- Each skus[i] consists of English letters, digits, and '-' only
- Comparison is case-sensitive, so "ab-1" and "AB-1" are different

Example 1:
Input: skus = ["BX-12", "A7", "Q9", "A7", "BX-12"]
Output: "A7"
Explanation: "A7" is the first SKU whose second occurrence is encountered while scanning
from left to right.

Example 2:
Input: skus = ["P1", "R2", "S3", "T4"]
Output: ""
Explanation: No SKU is repeated, so return an empty string.
"""

from typing import List, Set


class Solution:
    def first_duplicate_sku(self, skus: List[str]) -> str:
        """
        Find the first SKU whose second appearance occurs earliest while scanning left to right.

        Args:
            skus: A list of SKU strings in the exact order they were scanned.

        Returns:
            The first duplicate SKU encountered during a left-to-right scan.
            Returns an empty string if no duplicate exists.

        Time Complexity:
            O(n), where n is the number of SKUs, because each SKU is processed once
            and set membership checks are average O(1).

        Space Complexity:
            O(n), in the worst case if all SKUs are unique and must be stored in the set.
        """
        # We use a set to store every SKU we have already seen.
        #
        # Why a set?
        # - A set is a hash-based data structure.
        # - It allows very fast average-case membership checks: "Have we seen this SKU before?"
        # - This is much more efficient than comparing the current SKU against all previous SKUs,
        #   which would take O(n^2) time in the worst case.
        #
        # Example:
        # If seen = {"BX-12", "A7"} and current SKU is "A7",
        # then checking "A7" in seen is fast and immediately tells us it is a duplicate.
        seen: Set[str] = set()

        # We scan the list from left to right because the problem defines the answer
        # based on the earliest second appearance.
        #
        # That means the moment we encounter a SKU that is already in "seen",
        # we can return it immediately. There is no need to continue scanning,
        # because this is guaranteed to be the first duplicate by the problem's definition.
        for sku in skus:
            # Step 1: Check whether the current SKU has already been scanned before.
            if sku in seen:
                # If yes, then this is the first moment we have encountered a repeated SKU
                # during our left-to-right scan.
                #
                # This exactly matches the problem requirement:
                # "return the first SKU that has already been seen before."
                return sku

            # Step 2: If the SKU has not been seen before, add it to the set.
            #
            # This records that future occurrences of the same SKU should be recognized
            # as duplicates.
            seen.add(sku)

        # If we finish the entire loop without returning, then no SKU appeared twice.
        # According to the problem statement, we must return an empty string in that case.
        return ""


if __name__ == "__main__":
    solution = Solution()

    # Sample input from Example 1
    skus1: List[str] = ["BX-12", "A7", "Q9", "A7", "BX-12"]
    result1: str = solution.first_duplicate_sku(skus1)
    print(result1)  # Expected: "A7"

    # Manual trace for Example 1 to verify correctness:
    # seen = {}
    # read "BX-12" -> not seen, add it
    # seen = {"BX-12"}
    # read "A7" -> not seen, add it
    # seen = {"BX-12", "A7"}
    # read "Q9" -> not seen, add it
    # seen = {"BX-12", "A7", "Q9"}
    # read "A7" -> already seen, return "A7"
    #
    # This matches the expected output.

    # Sample input from Example 2
    skus2: List[str] = ["P1", "R2", "S3", "T4"]
    result2: str = solution.first_duplicate_sku(skus2)
    print(result2)  # Expected: ""

    # Manual trace for Example 2 to verify correctness:
    # seen = {}
    # read "P1" -> not seen, add it
    # read "R2" -> not seen, add it
    # read "S3" -> not seen, add it
    # read "T4" -> not seen, add it
    # end of list reached with no duplicate found, return ""
    #
    # This matches the expected output.

    # Additional beginner-friendly test cases
    skus3: List[str] = ["X-1", "Y-2", "X-1", "Y-2"]
    print(solution.first_duplicate_sku(skus3))  # Expected: "X-1"

    skus4: List[str] = ["ab-1", "AB-1", "ab-1"]
    print(solution.first_duplicate_sku(skus4))  # Expected: "ab-1" because comparison is case-sensitive