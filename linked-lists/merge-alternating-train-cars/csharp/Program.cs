/*
 * Title: Merge Alternating Train Cars
 * Difficulty: Easy
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given the heads of two singly linked lists, trainA and trainB,
 * each representing a sequence of train cars identified by their car numbers.
 * Your task is to merge the two trains into one by alternating cars from each
 * train, starting with a car from trainA. If one train runs out of cars before
 * the other, append all remaining cars from the longer train to the end of the
 * merged result.
 *
 * Return the head of the merged linked list. You must do this in-place without
 * allocating extra nodes — simply re-link the existing nodes.
 *
 * Constraints:
 * - The number of nodes in each list is in the range [0, 1000].
 * - 0 <= Node.val <= 10000
 * - Either list can be empty, in which case return the other list as-is.
 *
 * Example 1:
 * Input:  trainA = [1, 3, 5], trainB = [2, 4, 6]
 * Output: [1, 2, 3, 4, 5, 6]
 *
 * Example 2:
 * Input:  trainA = [10, 20], trainB = [1, 2, 3, 4]
 * Output: [10, 1, 20, 2, 3, 4]
 */

// ─────────────────────────────────────────────────────────────────────────────
// Node definition — represents a single car in the train (linked list node)
// ─────────────────────────────────────────────────────────────────────────────
public class ListNode
{
    public int val;          // The car number stored in this node
    public ListNode? next;   // Pointer to the next car in the train

    // Constructor: create a node with a given value and optional next pointer
    public ListNode(int val = 0, ListNode? next = null)
    {
        this.val = val;
        this.next = next;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Solution class — contains the merge algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Merges two linked lists by alternating nodes, starting with trainA.
    /// Remaining nodes from the longer list are appended at the end.
    ///
    /// Time Complexity:  O(min(m, n)) — we iterate until the shorter list ends,
    ///                   then the remaining tail is attached in O(1).
    ///                   Overall we touch every node exactly once → O(m + n).
    /// Space Complexity: O(1) — no extra nodes are allocated; we only re-link
    ///                   existing nodes by changing their .next pointers.
    /// </summary>
    public ListNode? MergeAlternating(ListNode? trainA, ListNode? trainB)
    {
        // ── Step 1: Handle edge cases where one or both trains are empty ──────
        // If trainA is null, there is nothing to interleave — return trainB as-is.
        // If trainB is null, trainA is already the complete merged result.
        // This also satisfies the constraint: "Either list can be empty, in which
        // case return the other list as-is."
        if (trainA == null) return trainB;
        if (trainB == null) return trainA;

        // ── Step 2: Save the head of the merged list ──────────────────────────
        // The merged list always starts with the first car of trainA (per the
        // problem statement: "starting with a car from trainA").
        // We keep a reference to this head so we can return it at the end.
        ListNode mergedHead = trainA;

        // ── Step 3: Set up two pointers to walk through both trains ───────────
        // currentA  → the node in trainA we are currently processing
        // currentB  → the node in trainB we are currently processing
        //
        // Think of these as two "fingers" — one on each train — that advance
        // one step at a time as we weave the cars together.
        ListNode? currentA = trainA;
        ListNode? currentB = trainB;

        // ── Step 4: Alternate nodes until one train runs out ──────────────────
        // We continue as long as BOTH trains still have cars to contribute.
        // The moment either pointer becomes null, we stop the alternating loop.
        while (currentA != null && currentB != null)
        {
            // Save the next pointers BEFORE we overwrite them.
            // If we don't save them now, we'll lose our place in each train.
            ListNode? nextA = currentA.next;  // next car in trainA
            ListNode? nextB = currentB.next;  // next car in trainB

            // ── Sub-step 4a: Link currentA → currentB ─────────────────────
            // We want the pattern: A → B → A → B → ...
            // So after the current A-car, we insert the current B-car.
            currentA.next = currentB;

            // ── Sub-step 4b: Link currentB → nextA (the next A-car) ────────
            // After the B-car, we need to point back to the next A-car.
            // BUT: if nextA is null (trainA is exhausted), we should NOT
            // overwrite currentB.next — the remaining B-cars are already
            // correctly chained, so we leave them alone.
            if (nextA != null)
            {
                // There is still a car in trainA — link B → next A-car.
                currentB.next = nextA;
            }
            // If nextA IS null, currentB.next already points to the rest of
            // trainB (nextB and beyond), which is exactly what we want —
            // the remaining B-cars get appended automatically.

            // ── Sub-step 4c: Advance both pointers ────────────────────────
            // Move each "finger" forward by one position so the next
            // iteration processes the next pair of cars.
            currentA = nextA;   // advance the trainA pointer
            currentB = nextB;   // advance the trainB pointer
        }

        // ── Step 5: Return the head of the merged list ────────────────────────
        // At this point the two trains are fully interleaved.
        // Any remaining cars from the longer train are already attached because:
        //   • If trainA ran out first: the last currentB.next was left unchanged,
        //     so it still points to the rest of trainB.
        //   • If trainB ran out first: currentA.next was never overwritten after
        //     the last B-car, so the rest of trainA remains intact.
        // Either way, no extra work is needed — just return the saved head.
        return mergedHead;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper utilities (top-level, outside any class)
// ─────────────────────────────────────────────────────────────────────────────

/// <summary>
/// Builds a linked list from an array of integers.
/// e.g. BuildList(new[]{1,3,5}) → 1 → 3 → 5 → null
/// </summary>
static ListNode? BuildList(int[] values)
{
    if (values == null || values.Length == 0) return null;

    // Create a dummy head to simplify building the list
    ListNode dummy = new ListNode(0);
    ListNode current = dummy;

    foreach (int v in values)
    {
        current.next = new ListNode(v);
        current = current.next;
    }

    return dummy.next; // return the real first node (skip dummy)
}

/// <summary>
/// Converts a linked list to a readable string like "1 → 2 → 3 → null".
/// Useful for printing results.
/// </summary>
static string ListToString(ListNode? head)
{
    if (head == null) return "null";

    var parts = new System.Text.StringBuilder();
    ListNode? current = head;

    while (current != null)
    {
        parts.Append(current.val);
        if (current.next != null) parts.Append(" → ");
        current = current.next;
    }

    parts.Append(" → null");
    return parts.ToString();
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

Solution solution = new Solution();

Console.WriteLine("═══════════════════════════════════════════════════════");
Console.WriteLine("         Merge Alternating Train Cars — Demo");
Console.WriteLine("═══════════════════════════════════════════════════════\n");

// ── Example 1 ────────────────────────────────────────────────────────────────
// trainA = [1, 3, 5]
// trainB = [2, 4, 6]
// Expected output: [1, 2, 3, 4, 5, 6]
ListNode? trainA1 = BuildList(new[] { 1, 3, 5 });
ListNode? trainB1 = BuildList(new[] { 2, 4, 6 });

Console.WriteLine("Example 1:");
Console.WriteLine($"  trainA  : {ListToString(trainA1)}");
Console.WriteLine($"  trainB  : {ListToString(trainB1)}");

ListNode? result1 = solution.MergeAlternating(trainA1, trainB1);
Console.WriteLine($"  Merged  : {ListToString(result1)}");
Console.WriteLine($"  Expected: 1 → 2 → 3 → 4 → 5 → 6 → null");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// trainA = [10, 20]
// trainB = [1, 2, 3, 4]
// Expected output: [10, 1, 20, 2, 3, 4]
ListNode? trainA2 = BuildList(new[] { 10, 20 });
ListNode? trainB2 = BuildList(new[] { 1, 2, 3, 4 });

Console.WriteLine("Example 2:");
Console.WriteLine($"  trainA  : {ListToString(trainA2)}");
Console.WriteLine($"  trainB  : {ListToString(trainB2)}");

ListNode? result2 = solution.MergeAlternating(trainA2, trainB2);
Console.WriteLine($"  Merged  : {ListToString(result2)}");
Console.WriteLine($"  Expected: 10 → 1 → 20 → 2 → 3 → 4 → null");
Console.WriteLine();

// ── Example 3: trainA is longer than trainB ───────────────────────────────────
// trainA = [1, 2, 3, 4]
// trainB = [9, 8]
// Expected output: [1, 9, 2, 8, 3, 4]
ListNode? trainA3 = BuildList(new[] { 1, 2, 3, 4 });
ListNode? trainB3 = BuildList(new[] { 9, 8 });

Console.WriteLine("Example 3 (trainA longer):");
Console.WriteLine($"  trainA  : {ListToString(trainA3)}");
Console.WriteLine($"  trainB  : {ListToString(trainB3)}");

ListNode? result3 = solution.MergeAlternating(trainA3, trainB3);
Console.WriteLine($"  Merged  : {ListToString(result3)}");
Console.WriteLine($"  Expected: 1 → 9 → 2 → 8 → 3 → 4 → null");
Console.WriteLine();

// ── Example 4: trainA is empty ────────────────────────────────────────────────
// trainA = []
// trainB = [5, 10, 15]
// Expected output: [5, 10, 15]
ListNode? trainA4 = BuildList(Array.Empty<int>());
ListNode? trainB4 = BuildList(new[] { 5, 10, 15 });

Console.WriteLine("Example 4 (trainA empty):");
Console.WriteLine($"  trainA  : {ListToString(trainA4)}");
Console.WriteLine($"  trainB  : {ListToString(trainB4)}");

ListNode? result4 = solution.MergeAlternating(trainA4, trainB4);
Console.WriteLine($"  Merged  : {ListToString(result4)}");
Console.WriteLine($"  Expected: 5 → 10 → 15 → null");
Console.WriteLine();

// ── Example 5: trainB is empty ────────────────────────────────────────────────
// trainA = [7, 14]
// trainB = []
// Expected output: [7, 14]
ListNode? trainA5 = BuildList(new[] { 7, 14 });
ListNode? trainB5 = BuildList(Array.Empty<int>());

Console.WriteLine("Example 5 (trainB empty):");
Console.WriteLine($"  trainA  : {ListToString(trainA5)}");
Console.WriteLine($"  trainB  : {ListToString(trainB5)}");

ListNode? result5 = solution.MergeAlternating(trainA5, trainB5);
Console.WriteLine($"  Merged  : {ListToString(result5)}");
Console.WriteLine($"  Expected: 7 → 14 → null");
Console.WriteLine();

// ── Example 6: Both lists have a single node ──────────────────────────────────
// trainA = [42]
// trainB = [99]
// Expected output: [42, 99]
ListNode? trainA6 = BuildList(new[] { 42 });
ListNode? trainB6 = BuildList(new[] { 99 });

Console.WriteLine("Example 6 (single-node lists):");
Console.WriteLine($"  trainA  : {ListToString(trainA6)}");
Console.WriteLine($"  trainB  : {ListToString(trainB6)}");

ListNode? result6 = solution.MergeAlternating(trainA6, trainB6);
Console.WriteLine($"  Merged  : {ListToString(result6)}");
Console.WriteLine($"  Expected: 42 → 99 → null");
Console.WriteLine();

Console.WriteLine("═══════════════════════════════════════════════════════");
Console.WriteLine("                    All tests done!");
Console.WriteLine("═══════════════════════════════════════════════════════");