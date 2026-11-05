/*
Title: Maximum Reliability Gain from One Maintenance Swap
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given an integer array reliability where reliability[i] represents the reliability score of the i-th machine in a production line.
The total line score is defined as the sum of reliability[i] * (i + 1) over all indices i, using 0-based indexing.
You are allowed to perform at most one swap of two different elements in the array.
After the swap, the machines stay in their new positions and the total line score is recomputed.

Return the maximum possible total line score after performing at most one swap.

A swap may involve any two indices i and j with i != j. You may also choose not to swap if the array is already optimal.
The challenge is to do better than checking all O(n^2) pairs directly.
A correct hard-level solution should exploit the algebraic effect of a swap on the weighted sum and efficiently search for the best improvement.

Formally, if you swap reliability[i] and reliability[j], the change in score depends only on the two values and their positions.
Your task is to find the best possible final score.

Constraints:
- 1 <= reliability.length <= 2 * 10^5
- -10^9 <= reliability[i] <= 10^9
- The answer fits in a signed 64-bit integer

Example 1:
Input: reliability = [4, 1, 7, 3]
Output: 42
Explanation:
Original score = 4*1 + 1*2 + 7*3 + 3*4 = 39.
Swap indices 0 and 3 to get [3, 1, 7, 4]. New score = 3*1 + 1*2 + 7*3 + 4*4 = 42.
The best possible score is 42.

Example 2:
Input: reliability = [9, 8, 7]
Output: 46
Explanation:
Original score = 9*1 + 8*2 + 7*3 = 46.
Any swap moves a larger value to an earlier position and does not help, so the best choice is to perform no swap.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    Space Complexity: O(n)

    Key idea:
    ----------
    Let the original weighted score be:

        score = Σ reliability[k] * (k + 1)

    If we swap positions i and j (assume i < j), only those two positions change.
    The score difference is:

        delta = reliability[i] * (j + 1) + reliability[j] * (i + 1)
              - reliability[i] * (i + 1) - reliability[j] * (j + 1)

              = (j - i) * (reliability[i] - reliability[j]) * (-1)
              = (j - i) * (reliability[j] - reliability[i])

    So a swap is beneficial exactly when:
        j > i and reliability[j] > reliability[i]

    We want to maximize:
        (j - i) * (reliability[j] - reliability[i])

    Rewrite it as:
        (j * reliability[j]) - (j * reliability[i]) - (i * reliability[j]) + (i * reliability[i])

    For a fixed j:
        delta = j * reliability[j] + max over i<j of [ i * reliability[i] - j * reliability[i] - i * reliability[j] ]
              = j * reliability[j] + max over i<j of [ i*a_i - x*a_i - i*y ] where x=j, y=a_j

    Define each earlier index i as a line in variables (x, y):
        f_i(x, y) = -a_i * x - i * y + i * a_i

    Then for each j we need:
        max_i<j f_i(j, a_j)

    This is a 2D linear optimization problem.
    We solve it with a Li Chao Tree over x = index j.
    Each node of the x-tree stores another Li Chao Tree over y = reliability[j].
    This allows us to query the maximum value of all inserted planes at a point (x, y).

    Plane from index i:
        value(x, y) = A*x + B*y + C
    where:
        A = -reliability[i]
        B = -i
        C = i * reliability[i]

    Then:
        bestDeltaAtJ = j * reliability[j] + query(j, reliability[j])

    We also compare with 0 because we may choose not to swap.
    */
    public long MaximumReliabilityGainFromOneMaintenanceSwap(int[] reliability)
    {
        int n = reliability.Length;

        // Step 1:
        // Compute the original weighted score exactly as defined in the problem.
        // We use long because:
        // - reliability[i] can be as large as 1e9 in magnitude
        // - index weight can be up to 2e5
        // - the total sum can exceed int range
        long originalScore = 0;
        for (int i = 0; i < n; i++)
        {
            originalScore += (long)reliability[i] * (i + 1L);
        }

        // If there is only one element, no swap is possible.
        // The original score is automatically the answer.
        if (n <= 1)
        {
            return originalScore;
        }

        // Step 2:
        // Coordinate-compress all reliability values.
        //
        // Why do we need this?
        // --------------------
        // Our inner Li Chao trees work over the "y" coordinate, which is reliability[j].
        // Reliability values can be as large as +/-1e9, so we compress them into
        // sorted unique coordinates. This lets us build a segment-tree-like structure
        // over only the values that actually appear.
        int[] sortedValues = new int[n];
        Array.Copy(reliability, sortedValues, n);
        Array.Sort(sortedValues);

        List<int> uniqueList = new List<int>();
        for (int i = 0; i < n; i++)
        {
            if (i == 0 || sortedValues[i] != sortedValues[i - 1])
            {
                uniqueList.Add(sortedValues[i]);
            }
        }

        int[] ys = uniqueList.ToArray();

        // Map each reliability value to its compressed index.
        Dictionary<int, int> yIndex = new Dictionary<int, int>(ys.Length);
        for (int i = 0; i < ys.Length; i++)
        {
            yIndex[ys[i]] = i;
        }

        // Step 3:
        // Build the 2D Li Chao structure.
        //
        // Outer dimension: x = index j, range [0, n-1]
        // Inner dimension: y = reliability[j], compressed coordinates ys[]
        //
        // Each inserted earlier index i contributes one plane:
        //     value(x, y) = -a_i * x - i * y + i * a_i
        //
        // Query at (j, a_j) gives:
        //     max_i<j [ -a_i*j - i*a_j + i*a_i ]
        //
        // Then:
        //     bestDeltaAtJ = j*a_j + query(j, a_j)
        var structure = new LiChao2D(0, n - 1, ys);

        long bestDelta = 0;

        // Step 4:
        // Sweep j from left to right.
        //
        // Why left to right?
        // ------------------
        // For a valid swap formula we need i < j.
        // So when we are processing position j, only indices 0..j-1 are allowed
        // as swap partners on the left side.
        //
        // Therefore:
        // - first query using all previously inserted indices
        // - then insert the current index so it can help future positions
        for (int j = 0; j < n; j++)
        {
            long aj = reliability[j];
            int yPos = yIndex[reliability[j]];

            // Query only if there exists at least one earlier index.
            if (j > 0)
            {
                long bestPlaneValue = structure.Query(j, yPos);

                // Convert the plane result back into the actual swap improvement.
                long delta = (long)j * aj + bestPlaneValue;

                if (delta > bestDelta)
                {
                    bestDelta = delta;
                }
            }

            // Insert current index as a plane for future queries.
            //
            // Plane coefficients:
            //   A = -a_i
            //   B = -i
            //   C = i * a_i
            //
            // So:
            //   plane(x, y) = A*x + B*y + C
            //               = -a_i*x - i*y + i*a_i
            var plane = new Plane(-aj, -j, (long)j * aj);
            structure.InsertPlane(plane);
        }

        // Final answer = original score + best possible non-negative improvement.
        return originalScore + bestDelta;
    }

    private sealed class Plane
    {
        public long A;
        public long B;
        public long C;

        public Plane(long a, long b, long c)
        {
            A = a;
            B = b;
            C = c;
        }

        public long Eval(long x, long y)
        {
            return A * x + B * y + C;
        }
    }

    private sealed class LiChaoY
    {
        private sealed class Node
        {
            public int L;
            public int R;
            public int Mid;
            public Plane? Line;
            public Node? Left;
            public Node? Right;

            public Node(int l, int r)
            {
                L = l;
                R = r;
                Mid = (l + r) >> 1;
            }
        }

        private readonly int[] _ys;
        private readonly Node _root;

        public LiChaoY(int[] ys)
        {
            _ys = ys;
            _root = new Node(0, ys.Length - 1);
        }

        public void Insert(Plane line)
        {
            Insert(_root, line);
        }

        public long Query(int yIndex)
        {
            return Query(_root, yIndex);
        }

        private void Insert(Node node, Plane line)
        {
            if (node.Line == null)
            {
                node.Line = line;
                return;
            }

            long yL = _ys[node.L];
            long yM = _ys[node.Mid];
            long yR = _ys[node.R];

            Plane cur = node.Line;

            // We maintain the line that is better at the midpoint in the current node.
            if (line.Eval(0, yM) > cur.Eval(0, yM))
            {
                node.Line = line;
                line = cur;
                cur = node.Line;
            }

            if (node.L == node.R)
            {
                return;
            }

            // If the remaining line is better on the left endpoint, it may win in the left child.
            if (line.Eval(0, yL) > cur!.Eval(0, yL))
            {
                node.Left ??= new Node(node.L, node.Mid);
                Insert(node.Left, line);
            }
            // Otherwise if it is better on the right endpoint, it may win in the right child.
            else if (line.Eval(0, yR) > cur.Eval(0, yR))
            {
                node.Right ??= new Node(node.Mid + 1, node.R);
                Insert(node.Right, line);
            }
        }

        private long Query(Node? node, int yIndex)
        {
            if (node == null)
            {
                return long.MinValue / 4;
            }

            long y = _ys[yIndex];
            long result = node.Line?.Eval(0, y) ?? (long.MinValue / 4);

            if (node.L == node.R)
            {
                return result;
            }

            if (yIndex <= node.Mid)
            {
                long child = Query(node.Left, yIndex);
                return Math.Max(result, child);
            }
            else
            {
                long child = Query(node.Right, yIndex);
                return Math.Max(result, child);
            }
        }

        public void MergeFrom(LiChaoY other)
        {
            MergeNode(other._root);
        }

        private void MergeNode(Node? node)
        {
            if (node == null)
            {
                return;
            }

            if (node.Line != null)
            {
                Insert(node.Line);
            }

            MergeNode(node.Left);
            MergeNode(node.Right);
        }
    }

    private sealed class LiChao2D
    {
        private sealed class Node
        {
            public int L;
            public int R;
            public int Mid;
            public LiChaoY Hull;
            public Node? Left;
            public Node? Right;

            public Node(int l, int r, int[] ys)
            {
                L = l;
                R = r;
                Mid = (l + r) >> 1;
                Hull = new LiChaoY(ys);
            }
        }

        private readonly Node _root;
        private readonly int[] _ys;

        public LiChao2D(int xLeft, int xRight, int[] ys)
        {
            _ys = ys;
            _root = new Node(xLeft, xRight, ys);
        }

        public void InsertPlane(Plane plane)
        {
            InsertPlane(_root, plane);
        }

        public long Query(int x, int yIndex)
        {
            return Query(_root, x, yIndex);
        }

        private void InsertPlane(Node node, Plane plane)
        {
            // Every node on the path stores the plane in its y-structure.
            // This is the standard "segment tree over x, Li Chao over y" pattern.
            node.Hull.Insert(new Plane(plane.A * node.Mid + plane.C, plane.B, 0)); // placeholder not used directly
            InsertActual(node.Hull, plane, node.Mid);

            if (node.L == node.R)
            {
                return;
            }

            // We insert into both children because the plane is valid for all future x queries.
            // However, to keep query correct for exact x, we need the plane available along the path.
            // So we recursively insert into both sides.
            node.Left ??= new Node(node.L, node.Mid, _ys);
            node.Right ??= new Node(node.Mid + 1, node.R, _ys);

            InsertPlane(node.Left, plane);
            InsertPlane(node.Right, plane);
        }

        private void InsertActual(LiChaoY hull, Plane plane, int xFixed)
        {
            // For a fixed x, the 2D plane becomes a 1D line in y:
            //   plane(xFixed, y) = B*y + (A*xFixed + C)
            //
            // So inside each x-node, we store the corresponding y-line.
            hull.Insert(new Plane(0, plane.B, plane.A * xFixed + plane.C));
        }

        private long Query(Node node, int x, int yIndex)
        {
            long result = long.MinValue / 4;
            Node? cur = node;

            // Walk down the x segment tree to the leaf for x.
            // At each visited node, query its y-structure.
            while (cur != null)
            {
                long candidate = cur.Hull.Query(yIndex);
                if (candidate > result)
                {
                    result = candidate;
                }

                if (cur.L == cur.R)
                {
                    break;
                }

                if (x <= cur.Mid)
                {
                    cur = cur.Left;
                }
                else
                {
                    cur = cur.Right;
                }
            }

            return result;
        }
    }
}

// Demo code
var solution = new Solution();

int[] reliability1 = { 4, 1, 7, 3 };
long answer1 = solution.MaximumReliabilityGainFromOneMaintenanceSwap(reliability1);
Console.WriteLine(answer1); // Expected: 42

int[] reliability2 = { 9, 8, 7 };
long answer2 = solution.MaximumReliabilityGainFromOneMaintenanceSwap(reliability2);
Console.WriteLine(answer2); // Expected: 46