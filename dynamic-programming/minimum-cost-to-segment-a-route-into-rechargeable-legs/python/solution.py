"""
Title: Minimum Cost to Segment a Route into Rechargeable Legs

Problem Description:
A delivery drone must travel through a sequence of checkpoints from west to east.
The checkpoints are numbered from 0 to n - 1, and the drone starts at checkpoint 0
and must end at checkpoint n - 1. The distance between checkpoint i - 1 and checkpoint i
is given by dist[i] for 1 <= i < n, so the total distance of a leg from checkpoint j
to checkpoint i is the sum of distances between them.

The drone cannot recharge while flying. Instead, you may choose some checkpoints as
recharge stops. If the drone flies one leg whose total distance is L, then that leg
costs batteryCost * L^2 units of energy because longer uninterrupted flights are
disproportionately expensive. In addition, every checkpoint i used as a recharge stop
(except checkpoint 0 and checkpoint n - 1) adds a fixed service fee fee[i].

Return the minimum total cost to reach checkpoint n - 1.

Formally, choose an increasing sequence of checkpoints 0 = p0 < p1 < ... < pk = n - 1.
The total cost is:
1. Sum over all legs t from 1 to k of batteryCost * (distance from p(t-1) to p(t))^2
2. Plus the sum of fee[p1] + fee[p2] + ... + fee[p(k-1)]

You need to compute the minimum possible total cost.

Constraints:
- 2 <= n <= 200000
- 1 <= dist[i] <= 10^6 for 1 <= i < n
- 0 <= fee[i] <= 10^12 for 0 <= i < n
- fee[0] = fee[n - 1] = 0
- 1 <= batteryCost <= 10^6
- The answer fits in a signed 64-bit integer.
"""

from typing import List, Optional


class Line:
    """
    Represents a line of the form y = m * x + b.

    This is used inside the Convex Hull Trick structure to optimize the dynamic
    programming transition.

    Attributes:
        m: Slope of the line.
        b: Intercept of the line.
    """

    __slots__ = ("m", "b")

    def __init__(self, m: int, b: int) -> None:
        """
        Initialize a line.

        Args:
            m: Slope.
            b: Intercept.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.m = m
        self.b = b

    def value(self, x: int) -> int:
        """
        Evaluate the line at x.

        Args:
            x: Query x-coordinate.

        Returns:
            The y-value m * x + b.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        return self.m * x + self.b


class ConvexHullTrick:
    """
    Monotonic Convex Hull Trick for minimum queries.

    This implementation supports:
    - Adding lines in monotonic slope order
    - Querying minimum values for monotonic x order

    That is exactly what we need here:
    - Prefix positions are increasing, so query x values are increasing.
    - The generated slopes are also monotonic.

    We use a deque-like structure implemented with a list and a moving head index
    for efficiency in Python.
    """

    def __init__(self) -> None:
        """
        Initialize an empty hull.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.lines: List[Line] = []
        self.head: int = 0

    @staticmethod
    def _is_bad(l1: Line, l2: Line, l3: Line) -> bool:
        """
        Check whether the middle line l2 becomes unnecessary after adding l3.

        We want the lower hull for minimum queries.
        For three lines l1, l2, l3 added in monotonic slope order, l2 is useless if
        the intersection of (l1, l2) is to the right of or equal to the intersection
        of (l2, l3).

        To avoid floating point precision issues, we compare using cross multiplication.

        Args:
            l1: First line.
            l2: Second line.
            l3: Third line.

        Returns:
            True if l2 is unnecessary, otherwise False.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        return (l3.b - l1.b) * (l1.m - l2.m) <= (l2.b - l1.b) * (l1.m - l3.m)

    def add_line(self, m: int, b: int) -> None:
        """
        Add a new line y = m * x + b to the hull.

        Important requirement:
        Lines must be added in monotonic slope order. In this problem, slopes are
        -2 * batteryCost * prefix[j], and prefix[j] is increasing, so slopes are
        decreasing. This still works with the same redundancy test because the order
        is monotonic.

        Args:
            m: Slope of the new line.
            b: Intercept of the new line.

        Returns:
            None

        Time complexity:
            Amortized O(1)

        Space complexity:
            O(1) additional, excluding stored lines
        """
        new_line = Line(m, b)

        # Remove useless lines from the back.
        # If the second-last, last, and new line make the last line redundant,
        # we pop it because it will never be the minimum for any future query.
        while len(self.lines) - self.head >= 2 and self._is_bad(self.lines[-2], self.lines[-1], new_line):
            self.lines.pop()

        self.lines.append(new_line)

    def query(self, x: int) -> int:
        """
        Query the minimum y-value at coordinate x.

        Important requirement:
        Query x values must be monotonic. In this problem, x is the prefix position
        of checkpoints, which is increasing.

        We move the head forward while the next line gives a value that is no worse
        than the current line.

        Args:
            x: Query x-coordinate.

        Returns:
            Minimum y-value among all lines at x.

        Time complexity:
            Amortized O(1)

        Space complexity:
            O(1)
        """
        # Since x values are queried in increasing order, once a line stops being optimal,
        # it will never become optimal again. Therefore we can safely discard it by
        # moving the head pointer forward.
        while self.head + 1 < len(self.lines) and self.lines[self.head + 1].value(x) <= self.lines[self.head].value(x):
            self.head += 1

        return self.lines[self.head].value(x)


class Solution:
    def minimum_cost_to_segment_route(self, n: int, dist: List[int], fee: List[int], batteryCost: int) -> int:
        """
        Compute the minimum total cost to travel from checkpoint 0 to checkpoint n - 1.

        Dynamic programming formulation:
            Let pos[i] be the prefix distance from checkpoint 0 to checkpoint i.
            Let dp[i] be the minimum cost to reach checkpoint i.

            Transition:
                dp[i] = min over j < i of
                        dp[j] + batteryCost * (pos[i] - pos[j])^2 + (fee[i] if i != n - 1 else 0)

            Expand the square:
                dp[i] = batteryCost * pos[i]^2 + extra_fee(i) +
                        min over j < i of
                        [dp[j] + batteryCost * pos[j]^2 - 2 * batteryCost * pos[j] * pos[i]]

            For fixed j, this is a line in x = pos[i]:
                y = m * x + b
                m = -2 * batteryCost * pos[j]
                b = dp[j] + batteryCost * pos[j]^2

            So we can optimize the transition using Convex Hull Trick.

        Args:
            n: Number of checkpoints.
            dist: Distance array where dist[i] is the distance from i - 1 to i for i >= 1.
            fee: Recharge fee for each checkpoint, with fee[0] = fee[n - 1] = 0.
            batteryCost: Multiplier for squared leg distance cost.

        Returns:
            Minimum total cost to reach checkpoint n - 1.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        # Step 1: Build prefix positions.
        # pos[i] = total distance from checkpoint 0 to checkpoint i.
        # This converts any leg length from checkpoint j to checkpoint i into:
        #     pos[i] - pos[j]
        # which is exactly what we need for the squared cost formula.
        pos: List[int] = [0] * n
        for i in range(1, n):
            pos[i] = pos[i - 1] + dist[i]

        # Step 2: Prepare DP array.
        # dp[i] will store the minimum cost to arrive at checkpoint i.
        dp: List[int] = [0] * n

        # Step 3: Initialize Convex Hull Trick.
        # Before processing any i > 0, the only possible previous checkpoint is j = 0.
        # For j = 0:
        #   pos[0] = 0
        #   dp[0] = 0
        # So the line is:
        #   m = -2 * batteryCost * 0 = 0
        #   b = 0 + batteryCost * 0^2 = 0
        cht = ConvexHullTrick()
        cht.add_line(0, 0)

        # Step 4: Process checkpoints from left to right.
        # Because positions are increasing, both:
        #   - inserted slopes are monotonic
        #   - queried x values are monotonic
        # Therefore the deque-based CHT is valid and efficient.
        for i in range(1, n):
            x = pos[i]

            # Query the best previous checkpoint j.
            # This gives:
            #   min_j [dp[j] + batteryCost * pos[j]^2 - 2 * batteryCost * pos[j] * x]
            best_transition_value = cht.query(x)

            # Add the part of the formula that depends only on i.
            # Every arrival at i pays batteryCost * pos[i]^2.
            # Additionally, if i is an intermediate recharge stop, we pay fee[i].
            # The destination checkpoint n - 1 does NOT add a recharge fee.
            extra_fee = 0 if i == n - 1 else fee[i]
            dp[i] = batteryCost * x * x + best_transition_value + extra_fee

            # After computing dp[i], checkpoint i can serve as a previous stop
            # for future checkpoints. So we add its corresponding line.
            #
            # Derived line:
            #   m = -2 * batteryCost * pos[i]
            #   b = dp[i] + batteryCost * pos[i]^2
            m = -2 * batteryCost * x
            b = dp[i] + batteryCost * x * x
            cht.add_line(m, b)

        return dp[n - 1]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 5
    dist1 = [0, 3, 2, 4, 2]
    fee1 = [0, 5, 1, 6, 0]
    battery_cost1 = 1
    result1 = solution.minimum_cost_to_segment_route(n1, dist1, fee1, battery_cost1)
    print(result1)

    # Example 2
    n2 = 4
    dist2 = [0, 1, 1, 1]
    fee2 = [0, 100, 100, 0]
    battery_cost2 = 2
    result2 = solution.minimum_cost_to_segment_route(n2, dist2, fee2, battery_cost2)
    print(result2)