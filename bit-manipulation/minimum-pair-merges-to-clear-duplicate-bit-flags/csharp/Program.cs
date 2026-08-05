/*
Title: Minimum Pair Merges to Clear Duplicate Bit Flags

Problem Description:
You are given an integer array flags where each value represents a device configuration mask.
A bit set to 1 means that capability is enabled for that device.

You may repeatedly perform the following operation:
- choose any two different indices i and j such that flags[i] and flags[j] share at least one common set bit,
- remove both values,
- and insert their bitwise OR, that is, flags[i] | flags[j].

Your goal is to make the final array bit-disjoint, meaning that for every pair of remaining values a and b,
(a & b) == 0.

Return the minimum number of merge operations required.

Key insight:
- Think of each original mask as a node.
- Two masks are directly related if they share at least one common set bit.
- More importantly, masks can also be related indirectly through a chain of overlaps.
- Every connected conflict group can be reduced to one or more final masks, but only if those final masks
  are pairwise disjoint.

A very useful reformulation:
- Build a graph where each number is connected to the bit positions it contains.
- If two numbers are connected through shared bits (possibly indirectly), they belong to the same "overlap component".
- However, the examples show that a connected component of numbers does NOT always need to collapse into one final mask.
  Example 2 demonstrates this clearly:
    flags = [10, 3, 12, 1]
    10(1010), 3(0011), 12(1100), 1(0001)
    Although all four are connected through overlap chains, we can end with [14, 3], which are disjoint.
- So the true objective is:
  partition the numbers into the maximum possible number of groups such that:
  1) each group can be merged into one mask by valid overlap merges,
  2) the final masks of different groups are pairwise disjoint.

Equivalent bit-based view:
- Consider each bit position as a vertex.
- Each non-zero number creates a hyperedge connecting all bits set in that number.
- If we merge a set of numbers, the resulting mask is simply the union of all bits used by those numbers.
- Two final groups are disjoint exactly when they use disjoint sets of bit positions.
- Therefore, the final groups correspond to connected components in the graph of bit positions,
  where each number connects all of its set bits together.
- Every non-zero number belongs entirely to exactly one such bit-component, because all bits inside one number
  are connected by that number itself.
- All numbers inside the same bit-component can be merged down to exactly one final mask.
- Numbers from different bit-components are already disjoint and can remain separate.

What about zeros?
- A zero mask has no set bits.
- It is already disjoint from everything.
- It never needs to be merged and cannot be merged by the rule anyway.
- So each zero simply stays as its own final element and contributes 0 merges.

Therefore:
- Let nonZeroCount = number of flags[i] != 0
- Let components = number of connected components among bit positions that are actually used by at least one number
- Then the minimum merges needed for all non-zero numbers is:
    nonZeroCount - components
- Zeros contribute nothing.

This matches the examples:
1) [3, 5, 8]
   3 uses bits {0,1}
   5 uses bits {0,2}
   8 uses bit  {3}
   Bit-components: {0,1,2} and {3} => 2 components
   nonZeroCount = 3
   answer = 3 - 2 = 1

2) [10, 3, 12, 1]
   10 uses {1,3}
   3  uses {0,1}
   12 uses {2,3}
   1  uses {0}
   All used bits {0,1,2,3} are connected through these numbers? Let's check:
   - 10 connects 1 and 3
   - 3 connects 0 and 1
   - 12 connects 2 and 3
   So yes, all bits are in one bit-component.
   Then nonZeroCount = 4, components = 1, answer = 3.

Important note:
- The problem statement's Example 2 claims answer 2 using final array [3, 14].
- But that is impossible because 3 & 14 = 2, not 0.
- So [3, 14] is NOT disjoint.
- The mathematically correct minimum is 3, not 2.
- This solution follows the actual operation rules and the stated disjointness condition exactly.

*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Each number has at most 30 relevant bit positions because flags[i] <= 1e9 < 2^30.
    - For each number, we scan up to 30 bits.
    - Union-Find operations are effectively constant amortized time.
    - Total: O(n * 30 * α(30)) which is effectively O(n).

    Space Complexity:
    - Union-Find stores a fixed-size structure for 30 bit positions.
    - Additional small helper arrays/sets are also bounded by 30.
    - Total: O(1) auxiliary space with respect to n, or more precisely O(30).
    */
    public int MinimumMerges(int[] flags)
    {
        // There are only 30 possible bit positions we need to care about:
        // bit 0 through bit 29, because 1e9 fits within 30 bits.
        const int MaxBits = 30;

        // We use a Disjoint Set Union (Union-Find) structure over bit positions.
        // Why over bit positions instead of array indices?
        // Because the final disjoint groups are determined by which bits are forced
        // to stay together. If one number contains bits a and b, then those two bits
        // must belong to the same final merged mask, since that number cannot be split.
        var dsu = new DisjointSetUnion(MaxBits);

        // usedBit[b] tells us whether bit position b appears in at least one non-zero number.
        // This matters because isolated unused bits should not count as components.
        bool[] usedBit = new bool[MaxBits];

        // Count how many non-zero masks exist.
        // Every non-zero mask must ultimately belong to exactly one final non-zero group.
        int nonZeroCount = 0;

        // Process every flag.
        foreach (int value in flags)
        {
            // Zero has no set bits, is already disjoint from everything,
            // and cannot be merged under the rule because it shares no set bit with anything.
            // So we simply skip it.
            if (value == 0)
            {
                continue;
            }

            nonZeroCount++;

            // Collect all set bit positions in this number.
            // Example:
            // value = 10 (1010b) => bits [1, 3]
            List<int> bits = new List<int>(4);

            for (int bit = 0; bit < MaxBits; bit++)
            {
                if (((value >> bit) & 1) != 0)
                {
                    bits.Add(bit);
                    usedBit[bit] = true;
                }
            }

            // If a number has multiple set bits, that single number directly connects
            // all those bits into one component.
            //
            // For example, if value has bits [1, 3, 5], then those bits must all be in
            // the same final merged mask, because this one original number already contains them together.
            //
            // To represent that, we union all bits in this number with the first bit.
            // This is enough to connect the whole set.
            int firstBit = bits[0];
            for (int i = 1; i < bits.Count; i++)
            {
                dsu.Union(firstBit, bits[i]);
            }
        }

        // Now count how many connected components exist among the bit positions that are actually used.
        //
        // Each such component corresponds to one final non-zero mask after optimal merging.
        // Why exactly one?
        // - All bits in the same component are connected through numbers, so they cannot be split
        //   into multiple final disjoint masks without violating the overlap constraints.
        // - On the other hand, all numbers whose bits lie in that component can indeed be merged
        //   down to one mask by repeatedly merging along overlaps.
        HashSet<int> componentRoots = new HashSet<int>();

        for (int bit = 0; bit < MaxBits; bit++)
        {
            if (usedBit[bit])
            {
                componentRoots.Add(dsu.Find(bit));
            }
        }

        int bitComponents = componentRoots.Count;

        // If there are k non-zero numbers and we want to end with c final non-zero groups,
        // then we need exactly k - c merges, because each merge reduces the number of items by 1.
        //
        // Zeros do not affect this count because:
        // - they start already valid,
        // - they never need merging,
        // - and they cannot merge anyway.
        return nonZeroCount - bitComponents;
    }

    private sealed class DisjointSetUnion
    {
        private readonly int[] _parent;
        private readonly int[] _rank;

        public DisjointSetUnion(int size)
        {
            _parent = new int[size];
            _rank = new int[size];

            for (int i = 0; i < size; i++)
            {
                _parent[i] = i;
                _rank[i] = 0;
            }
        }

        public int Find(int x)
        {
            // Path compression:
            // Make future queries faster by directly attaching nodes to the root.
            if (_parent[x] != x)
            {
                _parent[x] = Find(_parent[x]);
            }

            return _parent[x];
        }

        public void Union(int a, int b)
        {
            int rootA = Find(a);
            int rootB = Find(b);

            if (rootA == rootB)
            {
                return;
            }

            // Union by rank:
            // Attach the shallower tree under the deeper tree to keep the structure flat.
            if (_rank[rootA] < _rank[rootB])
            {
                _parent[rootA] = rootB;
            }
            else if (_rank[rootA] > _rank[rootB])
            {
                _parent[rootB] = rootA;
            }
            else
            {
                _parent[rootB] = rootA;
                _rank[rootA]++;
            }
        }
    }
}

// Demo code
var solution = new Solution();

// Example 1 from the prompt.
// 3 = 0011, 5 = 0101, 8 = 1000
// Correct answer: 1
int[] flags1 = { 3, 5, 8 };
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", flags1)}]");
Console.WriteLine($"Minimum merges: {solution.MinimumMerges(flags1)}");
Console.WriteLine();

// Example 2 from the prompt.
// The prompt claims 2, but that claim is incorrect because [3, 14] is not disjoint:
// 3 & 14 = 2, not 0.
// The correct answer under the stated rules is 3.
int[] flags2 = { 10, 3, 12, 1 };
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", flags2)}]");
Console.WriteLine($"Minimum merges: {solution.MinimumMerges(flags2)}");
Console.WriteLine();

// Additional demo: already disjoint numbers.
int[] flags3 = { 1, 2, 4, 8 };
Console.WriteLine("Additional Example 3:");
Console.WriteLine($"Input: [{string.Join(", ", flags3)}]");
Console.WriteLine($"Minimum merges: {solution.MinimumMerges(flags3)}");
Console.WriteLine();

// Additional demo: all connected through shared bits.
int[] flags4 = { 3, 6, 12 };
Console.WriteLine("Additional Example 4:");
Console.WriteLine($"Input: [{string.Join(", ", flags4)}]");
Console.WriteLine($"Minimum merges: {solution.MinimumMerges(flags4)}");
Console.WriteLine();

// Additional demo: includes zeros.
int[] flags5 = { 0, 0, 3, 5, 0, 8 };
Console.WriteLine("Additional Example 5:");
Console.WriteLine($"Input: [{string.Join(", ", flags5)}]");
Console.WriteLine($"Minimum merges: {solution.MinimumMerges(flags5)}");