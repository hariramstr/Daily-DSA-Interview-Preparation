"""
Title: Count Employees With Reused Desk PINs

Problem Description:
A company assigns each employee a temporary desk PIN for one day. You are given an
array `pins` where `pins[i]` is the PIN used by the `i`-th employee in the check-in
list. Some PINs may appear more than once because employees accidentally reused an
existing temporary PIN instead of generating a new one.

Your task is to return the number of employees whose PIN is not unique in the list.
In other words, count how many positions belong to a PIN value that appears at least
twice.

For example, if the PIN list is [4312, 9981, 4312, 7777, 9981], then PIN 4312 is used
by 2 employees and PIN 9981 is used by 2 employees, so the answer is 4. The employee
with PIN 7777 is not counted because that PIN appears only once.

This problem should be solved efficiently using hashing. A straightforward approach is
to count the frequency of each PIN, then sum the frequencies of all PINs that occur
more than once.

Constraints:
- 1 <= pins.length <= 100000
- 0 <= pins[i] <= 1000000000
- The answer fits in a 32-bit integer.

Example 1:
Input: pins = [4312, 9981, 4312, 7777, 9981]
Output: 4
Explanation: Two employees used 4312 and two employees used 9981, so 4 employees are counted.

Example 2:
Input: pins = [12, 34, 56, 78]
Output: 0
Explanation: Every PIN appears exactly once, so no employee reused a PIN.

Return the total number of employees whose desk PIN appears more than once anywhere
in the array.
"""

from typing import Dict, List


class Solution:
    def count_reused_pin_employees(self, pins: List[int]) -> int:
        """
        Count how many employees have a PIN value that appears more than once.

        The method uses a hash map (Python dictionary) to count how many times each
        PIN appears. Then it adds up the frequencies of only those PINs whose count
        is at least 2.

        Args:
            pins: A list of integers where each integer is the PIN used by one employee.

        Returns:
            The total number of employees whose PIN is duplicated somewhere in the list.

        Time complexity:
            O(n), where n is the number of employees/PINs in the input list.

        Space complexity:
            O(n) in the worst case, if all PINs are distinct and all must be stored
            in the frequency dictionary.
        """
        # This dictionary will map:
        #   PIN value -> number of times that PIN appears in the list
        #
        # We choose a dictionary because:
        # 1. It gives average O(1) time for insert/update/lookups.
        # 2. The problem specifically suggests an efficient hashing-based solution.
        # 3. PIN values can be as large as 1,000,000,000, so using a direct array
        #    indexed by PIN would be wasteful or impossible.
        frequency: Dict[int, int] = {}

        # First pass: count how many times each PIN appears.
        #
        # Example for pins = [4312, 9981, 4312, 7777, 9981]:
        # - See 4312 -> frequency becomes {4312: 1}
        # - See 9981 -> frequency becomes {4312: 1, 9981: 1}
        # - See 4312 -> frequency becomes {4312: 2, 9981: 1}
        # - See 7777 -> frequency becomes {4312: 2, 9981: 1, 7777: 1}
        # - See 9981 -> frequency becomes {4312: 2, 9981: 2, 7777: 1}
        for pin in pins:
            # dict.get(pin, 0) means:
            # - if pin already exists in the dictionary, get its current count
            # - otherwise use 0 as the starting count
            #
            # Then we add 1 because we have seen this PIN one more time.
            frequency[pin] = frequency.get(pin, 0) + 1

        # This variable will store the final answer:
        # the number of employees whose PIN is part of a duplicated group.
        reused_employee_count: int = 0

        # Second pass over the frequency values:
        # For each unique PIN, decide whether it contributes to the answer.
        #
        # Important idea:
        # - If a PIN appears once, it is unique, so it contributes 0.
        # - If a PIN appears 2 or more times, then every employee using that PIN
        #   should be counted, so we add the full frequency.
        #
        # Example:
        # frequency values are [2, 2, 1]
        # - 2 contributes 2
        # - 2 contributes 2
        # - 1 contributes 0
        # total = 4
        for count in frequency.values():
            # Only duplicated PINs should contribute to the result.
            if count > 1:
                reused_employee_count += count

        # Return the total number of employees whose PIN appears more than once.
        return reused_employee_count


if __name__ == "__main__":
    # Create an instance of the solution class so we can call the method.
    solution = Solution()

    # Sample input 1 from the problem statement.
    pins1: List[int] = [4312, 9981, 4312, 7777, 9981]
    result1: int = solution.count_reused_pin_employees(pins1)
    print("Input:", pins1)
    print("Output:", result1)
    # Expected: 4
    #
    # Manual verification:
    # - 4312 appears 2 times -> count both employees
    # - 9981 appears 2 times -> count both employees
    # - 7777 appears 1 time  -> do not count
    # Total = 2 + 2 = 4

    print()

    # Sample input 2 from the problem statement.
    pins2: List[int] = [12, 34, 56, 78]
    result2: int = solution.count_reused_pin_employees(pins2)
    print("Input:", pins2)
    print("Output:", result2)
    # Expected: 0
    #
    # Manual verification:
    # - Every PIN appears exactly once
    # - No employee has a duplicated PIN
    # Total = 0

    print()

    # Additional beginner-friendly test:
    # All employees use the same PIN, so every employee should be counted.
    pins3: List[int] = [5555, 5555, 5555]
    result3: int = solution.count_reused_pin_employees(pins3)
    print("Input:", pins3)
    print("Output:", result3)
    # Expected: 3