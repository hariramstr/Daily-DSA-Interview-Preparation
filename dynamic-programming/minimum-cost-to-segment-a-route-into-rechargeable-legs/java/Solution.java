import java.util.*;

/*
 * Title: Minimum Cost to Segment a Route into Rechargeable Legs
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A delivery drone must travel through a sequence of checkpoints from west to east.
 * The checkpoints are numbered from 0 to n - 1, and the drone starts at checkpoint 0
 * and must end at checkpoint n - 1. The distance between checkpoint i - 1 and checkpoint i
 * is given by dist[i] for 1 <= i < n, so the total distance of a leg from checkpoint j to checkpoint i
 * is the sum of distances between them.
 *
 * The drone cannot recharge while flying. Instead, you may choose some checkpoints as recharge stops.
 * If the drone flies one leg whose total distance is L, then that leg costs batteryCost * L^2 units
 * of energy because longer uninterrupted flights are disproportionately expensive.
 * In addition, every checkpoint i used as a recharge stop (except checkpoint 0 and checkpoint n - 1)
 * adds a fixed service fee fee[i].
 *
 * Return the minimum total cost to reach checkpoint n - 1.
 *
 * Formally, choose an increasing sequence of checkpoints 0 = p0 < p1 < ... < pk = n - 1.
 * The total cost is:
 * 1. Sum over all legs t from 1 to k of batteryCost * (distance from p(t-1) to p(t))^2
 * 2. Plus the sum of fee[p1] + fee[p2] + ... + fee[p(k-1)]
 *
 * You need to compute the minimum possible total cost.
 *
 * Constraints:
 * - 2 <= n <= 200000
 * - 1 <= dist[i] <= 10^6 for 1 <= i < n
 * - 0 <= fee[i] <= 10^12 for 0 <= i < n
 * - fee[0] = fee[n - 1] = 0
 * - 1 <= batteryCost <= 10^6
 * - The answer fits in a signed 64-bit integer.
 *
 * Optimized Dynamic Programming Idea:
 * Let pos[i] be the prefix distance from checkpoint 0 to checkpoint i.
 *
 * Define dp[i] = minimum total cost to reach checkpoint i.
 *
 * Transition:
 * dp[i] = min over j < i of:
 *         dp[j] + batteryCost * (pos[i] - pos[j])^2 + (i == n - 1 ? 0 : fee[i])
 *
 * Expand the square:
 * dp[i] = batteryCost * pos[i]^2 + (i == n - 1 ? 0 : fee[i]) +
 *         min over j < i of:
 *         (dp[j] + batteryCost * pos[j]^2 - 2 * batteryCost * pos[j] * pos[i])
 *
 * For each previous checkpoint j, this becomes a line:
 * y = m * x + b
 * where:
 *   x = pos[i]
 *   m = -2 * batteryCost * pos[j]
 *   b = dp[j] + batteryCost * pos[j]^2
 *
 * Since pos[i] is non-decreasing and slopes are added in non-increasing order
 * (because pos[j] is non-decreasing), we can use the Convex Hull Trick with a deque
 * to answer each transition in amortized O(1), giving total O(n).
 */
public class Solution {

    /**
     * Represents a line of the form y = m * x + b.
     * We store slope and intercept as long because all values fit in signed 64-bit.
     */
    private static class Line {
        long m;
        long b;

        Line(long m, long b) {
            this.m = m;
            this.b = b;
        }

        long value(long x) {
            return m * x + b;
        }
    }

    /**
     * Computes the minimum total cost to reach checkpoint n - 1.
     *
     * <p>Dynamic programming with Convex Hull Trick:
     * We transform the quadratic transition into a minimum over linear functions.
     * Because prefix positions are processed in increasing order and line slopes are added
     * in monotonic order, we can maintain a deque-based lower hull in linear time.</p>
     *
     * @param n number of checkpoints
     * @param dist dist[i] is the distance between checkpoint i - 1 and checkpoint i for i >= 1;
     *             dist[0] is unused and may be 0
     * @param fee fee[i] is the recharge service fee at checkpoint i; fee[0] and fee[n - 1] are 0
     * @param batteryCost multiplier applied to squared leg distance
     * @return the minimum possible total cost to reach checkpoint n - 1
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long minimumCost(int n, long[] dist, long[] fee, long batteryCost) {
        long[] pos = buildPrefixPositions(n, dist);
        long[] dp = new long[n];

        // We maintain a deque of candidate lines for the lower hull.
        // Each line corresponds to choosing some previous checkpoint j as the last recharge point.
        Deque<Line> hull = new ArrayDeque<>();

        // Base case:
        // dp[0] = 0 because we start at checkpoint 0 with no cost.
        dp[0] = 0;

        // Insert the line corresponding to j = 0.
        // For j = 0:
        // slope m = -2 * batteryCost * pos[0] = 0
        // intercept b = dp[0] + batteryCost * pos[0]^2 = 0
        hull.addLast(createLine(pos[0], dp[0], batteryCost));

        // Process checkpoints from left to right.
        for (int i = 1; i < n; i++) {
            long x = pos[i];

            // Step 1:
            // Query the best line for current x = pos[i].
            //
            // Because x values are non-decreasing, if the second line gives a value
            // less than or equal to the first line, then the first line will never be
            // optimal again for any future x. So we can pop it from the front.
            while (hull.size() >= 2) {
                Line first = hull.peekFirst();
                Line second = getSecond(hull);
                if (first.value(x) >= second.value(x)) {
                    hull.pollFirst();
                } else {
                    break;
                }
            }

            Line best = hull.peekFirst();

            // Step 2:
            // Compute dp[i] using the best previous checkpoint encoded by the best line.
            //
            // dp[i] = batteryCost * pos[i]^2 + best.value(pos[i]) + fee[i] (unless i is destination)
            long current = batteryCost * x * x + best.value(x);
            if (i != n - 1) {
                current += fee[i];
            }
            dp[i] = current;

            // Step 3:
            // Create the line representing transitions from checkpoint i to future checkpoints.
            Line newLine = createLine(pos[i], dp[i], batteryCost);

            // Step 4:
            // Maintain lower hull convexity at the back.
            //
            // If the newly added line makes the previous last line useless, remove that last line.
            // We use cross-multiplication to avoid floating-point precision issues.
            while (hull.size() >= 2) {
                Line l1 = getSecondLast(hull);
                Line l2 = hull.peekLast();
                if (isObsolete(l1, l2, newLine)) {
                    hull.pollLast();
                } else {
                    break;
                }
            }

            hull.addLast(newLine);
        }

        return dp[n - 1];
    }

    /**
     * Builds prefix positions where pos[i] is the total distance from checkpoint 0 to checkpoint i.
     *
     * @param n number of checkpoints
     * @param dist dist[i] is the distance between checkpoint i - 1 and checkpoint i for i >= 1
     * @return prefix distance array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixPositions(int n, long[] dist) {
        long[] pos = new long[n];
        for (int i = 1; i < n; i++) {
            pos[i] = pos[i - 1] + dist[i];
        }
        return pos;
    }

    /**
     * Creates the line corresponding to using checkpoint j as the previous recharge stop.
     *
     * <p>From the DP expansion:
     * line_j(x) = (-2 * batteryCost * pos[j]) * x + (dp[j] + batteryCost * pos[j]^2)</p>
     *
     * @param position prefix position pos[j]
     * @param dpValue dp[j]
     * @param batteryCost battery cost multiplier
     * @return the constructed line
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public Line createLine(long position, long dpValue, long batteryCost) {
        long m = -2L * batteryCost * position;
        long b = dpValue + batteryCost * position * position;
        return new Line(m, b);
    }

    /**
     * Determines whether the middle line l2 becomes obsolete after adding l3,
     * assuming lines are added in monotonic slope order.
     *
     * <p>For lower hull maintenance, l2 is obsolete if the intersection of (l1, l2)
     * is to the right of or equal to the intersection of (l2, l3).</p>
     *
     * <p>Using exact integer arithmetic:
     * (b3 - b1) / (m1 - m3) <= (b2 - b1) / (m1 - m2)
     * which is checked via cross multiplication.</p>
     *
     * @param l1 second last line before insertion
     * @param l2 last line before insertion
     * @param l3 new line to insert
     * @return true if l2 is obsolete, false otherwise
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean isObsolete(Line l1, Line l2, Line l3) {
        return (l3.b - l1.b) * (l1.m - l2.m) <= (l2.b - l1.b) * (l1.m - l3.m);
    }

    /**
     * Returns the second line from the front of the deque without permanently removing elements.
     *
     * @param deque deque of lines
     * @return second line from the front
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public Line getSecond(Deque<Line> deque) {
        Line first = deque.pollFirst();
        Line second = deque.peekFirst();
        deque.addFirst(first);
        return second;
    }

    /**
     * Returns the second last line from the back of the deque without permanently removing elements.
     *
     * @param deque deque of lines
     * @return second last line from the back
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public Line getSecondLast(Deque<Line> deque) {
        Line last = deque.pollLast();
        Line secondLast = deque.peekLast();
        deque.addLast(last);
        return secondLast;
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1) excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        // Note:
        // The textual explanation in the prompt lists several route costs that are inconsistent
        // with the stated output 30. We still run the algorithm exactly on the provided arrays.
        int n1 = 5;
        long[] dist1 = {0, 3, 2, 4, 2};
        long[] fee1 = {0, 5, 1, 6, 0};
        long batteryCost1 = 1;
        long result1 = solution.minimumCost(n1, dist1, fee1, batteryCost1);
        System.out.println("Example 1 result: " + result1);

        // Example 2 from the prompt.
        int n2 = 4;
        long[] dist2 = {0, 1, 1, 1};
        long[] fee2 = {0, 100, 100, 0};
        long batteryCost2 = 2;
        long result2 = solution.minimumCost(n2, dist2, fee2, batteryCost2);
        System.out.println("Example 2 result: " + result2);

        // Additional small sanity check:
        // n = 2 means a single direct leg from 0 to 1.
        int n3 = 2;
        long[] dist3 = {0, 7};
        long[] fee3 = {0, 0};
        long batteryCost3 = 3;
        long result3 = solution.minimumCost(n3, dist3, fee3, batteryCost3);
        System.out.println("Sanity check result: " + result3);
    }
}