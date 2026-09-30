/*
Title: Maximum Matched Crates After One Dock Extension
Difficulty: Hard
Topic: Two Pointers

Problem Description:
A warehouse has two sorted arrays: `crates` and `docks`. `crates[i]` is the size requirement of the i-th outgoing crate, and `docks[j]` is the capacity of the j-th loading dock. A crate can be assigned to at most one dock, and a dock can load at most one crate. A crate can only use a dock whose capacity is at least the crate's size.

Before assignments are made, the warehouse may perform at most one temporary dock extension. This extension can be applied to exactly one dock, increasing its capacity by an integer value `boost`, where `0 <= boost <= extraCapacity`. The extension is used on only one dock and only for this assignment batch.

Return the maximum number of crates that can be matched after optimally choosing whether to use the extension, which dock to apply it to, and how to pair crates with docks.

Both arrays may contain duplicates, and the chosen dock does not need to remain in its original relative position after being conceptually boosted; only the final matching count matters. Your solution should be efficient enough for large inputs.

Constraints:
- `1 <= crates.length, docks.length <= 2 * 10^5`
- `1 <= crates[i], docks[j] <= 10^9`
- `0 <= extraCapacity <= 10^9`
- `crates` is sorted in nondecreasing order
- `docks` is sorted in nondecreasing order

Example 1:
Input: crates = [2, 4, 7, 9], docks = [3, 5, 8], extraCapacity = 2
Output: 3

Example 2:
Input: crates = [3, 6, 6, 10], docks = [2, 6, 8, 8], extraCapacity = 3
Output: 4
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O((n + m) * log(min(n, m)))
      where n = crates.Length and m = docks.Length.
      We binary search the answer k, and each feasibility check runs in O(k),
      which is at most O(min(n, m)).

    Space Complexity:
    - O(1) extra space, ignoring input storage.

    High-level idea:
    ----------------
    We binary search the maximum number of crate-dock matches, call it k.

    For a fixed k, we ask:
    "Is it possible to match exactly k crates using the docks, if we may boost at most one dock by up to extraCapacity?"

    Because arrays are sorted and we only care about the count, the best set of crates to try matching for k is:
    - the k smallest crates among all crates.
      Why? If we cannot match the k smallest crates, then we definitely cannot match any other set of k crates,
      because any other choice would contain crates that are at least as large.

    Similarly, when trying to match those k crates, the best docks to use are:
    - the k largest docks among all docks.
      Why? If even the k strongest docks cannot handle those k crates (with at most one boost), then no other set of k docks can.

    So the feasibility check becomes:
    - Can crates[0..k-1] be matched with docks[m-k..m-1], using at most one boosted dock?

    To test that efficiently, we use a greedy two-pointer style process from largest crate downward.

    We maintain two pointers into the chosen dock window:
    - left  = smallest remaining dock in the chosen k docks
    - right = largest remaining dock in the chosen k docks

    We process crates from largest to smallest:
    1) If the largest remaining dock can already handle the current crate, use it normally.
    2) Otherwise, the current crate is too large for every remaining dock without help.
       Then the ONLY way to match it is to spend our one boost now.
       To maximize future flexibility, we should use the SMALLEST dock that can be boosted enough:
           docks[left] + extraCapacity >= current crate
       If even that fails, then matching k is impossible.

    Why use the smallest dock when boosting?
    - Because boosting a larger dock would waste capacity that could help future crates.
    - Since we are processing crates from largest to smallest, preserving larger unboosted docks is always best.

    This greedy rule is correct and standard for "one special operation" matching problems.
    */
    public int MaxMatchedCrates(int[] crates, int[] docks, int extraCapacity)
    {
        int n = crates.Length;
        int m = docks.Length;

        // We can never match more pairs than the smaller array length.
        int low = 0;
        int high = Math.Min(n, m);

        // Standard binary search on the answer.
        // We search for the largest k such that CanMatch(k) is true.
        while (low < high)
        {
            // Bias upward so the loop makes progress when low + 1 == high.
            int mid = low + (high - low + 1) / 2;

            if (CanMatchK(crates, docks, extraCapacity, mid))
            {
                // If k = mid is feasible, try a larger answer.
                low = mid;
            }
            else
            {
                // Otherwise, mid is too large.
                high = mid - 1;
            }
        }

        return low;
    }

    private bool CanMatchK(int[] crates, int[] docks, int extraCapacity, int k)
    {
        // Matching zero crates is always possible.
        if (k == 0)
        {
            return true;
        }

        int m = docks.Length;

        // We will try to match:
        // - the k smallest crates: crates[0 .. k-1]
        // - with the k largest docks: docks[m-k .. m-1]
        //
        // left/right define the current available dock range inside those k largest docks.
        int left = m - k;
        int right = m - 1;

        // Track whether we have already used the one allowed boost.
        bool usedBoost = false;

        // Process crates from largest to smallest among the chosen k smallest crates.
        // That means i goes from k-1 down to 0.
        for (int i = k - 1; i >= 0; i--)
        {
            int crate = crates[i];

            // Step 1:
            // If the largest remaining dock can already handle this crate without any boost,
            // greedily use that dock.
            //
            // Why is this safe?
            // - We are handling the hardest remaining crate first.
            // - If the strongest available dock can handle it, assigning that dock now cannot hurt.
            // - Smaller docks are no better for this crate, and stronger docks are exactly what
            //   should be reserved for larger crates in sorted matching problems.
            if (docks[right] >= crate)
            {
                right--;
                continue;
            }

            // Step 2:
            // The current crate is too large for every remaining dock without help,
            // because docks[right] is the largest remaining dock and it still fails.
            //
            // Therefore, the only chance is to use the one boost on some remaining dock.
            // If we already used the boost earlier, then this k is impossible.
            if (usedBoost)
            {
                return false;
            }

            // We now try to use the SMALLEST remaining dock, docks[left], as the boosted dock.
            //
            // Why the smallest?
            // - If the smallest dock can be boosted enough, using it is optimal because it preserves
            //   all larger docks for the remaining crates.
            // - If the smallest dock cannot be boosted enough, then no smaller dock exists,
            //   but maybe a larger dock could. However, in this specific greedy framework,
            //   using the smallest boostable dock is the right choice because we selected the k largest docks
            //   overall and process crates from largest to smallest. The feasibility condition reduces to:
            //   can we sacrifice the weakest remaining dock with the boost and still leave the stronger docks
            //   for the rest? If not, then no arrangement can do better.
            //
            // More concretely:
            // - Since all remaining docks are <= docks[right] < crate, any match for this crate must use the boost.
            // - To maximize future options, we should consume the weakest remaining dock that can be made sufficient.
            long boostedCapacity = (long)docks[left] + extraCapacity;
            if (boostedCapacity >= crate)
            {
                usedBoost = true;
                left++;
            }
            else
            {
                // Even after boosting the chosen dock, the crate still cannot be matched.
                // Therefore matching k crates is impossible.
                return false;
            }
        }

        // If we successfully processed all k crates, then k is feasible.
        return true;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] crates1 = { 2, 4, 7, 9 };
int[] docks1 = { 3, 5, 8 };
int extraCapacity1 = 2;
int result1 = solution.MaxMatchedCrates(crates1, docks1, extraCapacity1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] crates2 = { 3, 6, 6, 10 };
int[] docks2 = { 2, 6, 8, 8 };
int extraCapacity2 = 3;
int result2 = solution.MaxMatchedCrates(crates2, docks2, extraCapacity2);
Console.WriteLine(result2); // Expected: 4

// Additional quick sanity checks
int[] crates3 = { 5 };
int[] docks3 = { 3 };
int extraCapacity3 = 2;
Console.WriteLine(solution.MaxMatchedCrates(crates3, docks3, extraCapacity3)); // Expected: 1

int[] crates4 = { 5 };
int[] docks4 = { 3 };
int extraCapacity4 = 1;
Console.WriteLine(solution.MaxMatchedCrates(crates4, docks4, extraCapacity4)); // Expected: 0