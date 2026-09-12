"""
Title: Verify Unique Employee Extension Mapping

Problem Description:
A company stores employee phone extensions in a list of records. Each record contains
an employee name and a numeric extension. Due to synchronization bugs between internal
tools, the same employee may appear multiple times, and different employees might
accidentally be assigned the same extension.

You are given two arrays of equal length: `names` and `extensions`, where `names[i]`
is the employee name for record `i`, and `extensions[i]` is that employee's extension.

Return `true` if the records describe a valid one-to-one mapping between employee names
and extensions, and `false` otherwise.

A mapping is valid if:
1. Every occurrence of the same employee name always has the same extension.
2. No two different employee names share the same extension.

In other words, each name maps to exactly one extension, and each extension maps to
exactly one name.

This is a realistic data-validation task that can be solved efficiently using hash maps
or hash sets.

Constraints:
- 1 <= names.length == extensions.length <= 10^5
- 1 <= names[i].length <= 50
- names[i] contains only English letters, spaces, and underscores
- 1 <= extensions[i] <= 10^9

Example 1:
Input: names = ["Alice","Bob","Alice","Cara"], extensions = [101,202,101,303]
Output: true

Example 2:
Input: names = ["Alice","Bob","Alice"], extensions = [101,101,202]
Output: false
"""

from typing import Dict, List


class Solution:
    def is_valid_mapping(self, names: List[str], extensions: List[int]) -> bool:
        """
        Check whether employee names and extensions form a valid one-to-one mapping.

        A valid mapping means:
        1. The same name must always map to the same extension.
        2. The same extension must never map to two different names.

        Args:
            names: A list of employee names.
            extensions: A list of numeric extensions corresponding to each name.

        Returns:
            True if the mapping is valid, otherwise False.

        Time complexity:
            O(n), where n is the number of records, because we process each record once
            and dictionary lookups/inserts are O(1) on average.

        Space complexity:
            O(n), in the worst case, for storing mappings in hash maps.
        """
        # This dictionary stores the mapping from employee name -> extension.
        # Why do we need it?
        # Because one of the rules says that every repeated appearance of the same
        # employee name must always have the same extension.
        #
        # Example:
        # "Alice" -> 101
        # If we later see "Alice" -> 202, that is invalid.
        name_to_extension: Dict[str, int] = {}

        # This dictionary stores the reverse mapping from extension -> employee name.
        # Why do we also need a reverse map?
        # Because the second rule says that no two different employees may share
        # the same extension.
        #
        # Example:
        # 101 -> "Alice"
        # If we later see 101 -> "Bob", that is invalid.
        extension_to_name: Dict[int, str] = {}

        # We iterate through both arrays at the same time using their shared index.
        # The problem guarantees the arrays are of equal length.
        for i in range(len(names)):
            # Extract the current record.
            current_name: str = names[i]
            current_extension: int = extensions[i]

            # -----------------------------
            # Step 1: Validate name -> extension consistency
            # -----------------------------
            # If we have seen this name before, then it must match the same extension
            # as before. If it does not match, the data is invalid immediately.
            if current_name in name_to_extension:
                # Compare the previously stored extension with the current one.
                if name_to_extension[current_name] != current_extension:
                    # Example invalid case:
                    # First record:  "Alice" -> 101
                    # Later record:  "Alice" -> 202
                    # Same name, different extension => invalid
                    return False
            else:
                # If this name has not been seen before, store its extension.
                # This establishes the expected extension for any future occurrences
                # of the same name.
                name_to_extension[current_name] = current_extension

            # -----------------------------
            # Step 2: Validate extension -> name uniqueness
            # -----------------------------
            # If we have seen this extension before, it must belong to the same name.
            # If it belongs to a different name, then two employees share one extension,
            # which violates the one-to-one mapping rule.
            if current_extension in extension_to_name:
                # Compare the previously stored name with the current one.
                if extension_to_name[current_extension] != current_name:
                    # Example invalid case:
                    # First record: 101 -> "Alice"
                    # Later record: 101 -> "Bob"
                    # Same extension, different name => invalid
                    return False
            else:
                # If this extension has not been seen before, store the owner name.
                # This ensures any future use of this extension must refer to the
                # same employee.
                extension_to_name[current_extension] = current_name

        # If we finish processing all records without finding any conflict,
        # then both directions are consistent:
        # - each name maps to exactly one extension
        # - each extension maps to exactly one name
        return True


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    names1: List[str] = ["Alice", "Bob", "Alice", "Cara"]
    extensions1: List[int] = [101, 202, 101, 303]
    result1: bool = solution.is_valid_mapping(names1, extensions1)
    print(result1)  # Expected: True

    # Example 2 from the problem statement
    names2: List[str] = ["Alice", "Bob", "Alice"]
    extensions2: List[int] = [101, 101, 202]
    result2: bool = solution.is_valid_mapping(names2, extensions2)
    print(result2)  # Expected: False

    # Additional quick sanity check
    names3: List[str] = ["Eve", "Eve", "Dan"]
    extensions3: List[int] = [500, 500, 600]
    result3: bool = solution.is_valid_mapping(names3, extensions3)
    print(result3)  # Expected: True