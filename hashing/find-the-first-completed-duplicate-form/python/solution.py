"""
Title: Find the First Completed Duplicate Form

Problem Description:
A company stores online application forms as an ordered stream of field submissions.
Each submission is represented by a pair [formId, fieldName], meaning that fieldName
was submitted for the form with identifier formId.

A form is considered complete once it has received all required field names at least
once. If the same field is submitted multiple times for the same form, it should only
count once toward completion.

Your task is to return the formId of the first form that becomes a completed duplicate.
A form is a completed duplicate if, at the moment it becomes complete, there already
exists at least one other completed form whose set of unique submitted fields is exactly
the same. Since every completed form must contain all required fields, this effectively
means the form has all required fields and another form has already completed with that
same required set.

If multiple forms become completed duplicates during processing, return the one that
reaches this condition earliest in the stream. If no form ever becomes a completed
duplicate, return -1.

You must process submissions in order. A good solution should use hashing to track each
form's unique fields and to count how many completed forms have already matched the
required field signature.
"""

from typing import Dict, List, Set


class Solution:
    def first_completed_duplicate_form(
        self, requiredFields: List[str], submissions: List[List[object]]
    ) -> int:
        """
        Return the formId of the first form that becomes a completed duplicate.

        The algorithm processes submissions in stream order. For each form, it tracks
        which required fields have been seen so far. When a form reaches all required
        fields for the first time, it becomes complete. If another form had already
        become complete earlier, then this newly completed form is the first completed
        duplicate encountered at that moment.

        Args:
            requiredFields: Distinct required field names that define completion.
            submissions: Ordered list of [formId, fieldName] submissions.

        Returns:
            The formId of the first form that becomes a completed duplicate, or -1
            if no such form exists.

        Time complexity:
            O(n + r), where n is the number of submissions and r is the number of
            required fields.

        Space complexity:
            O(f * r), where f is the number of distinct formIds that receive at least
            one required field.
        """
        # Step 1:
        # Convert the list of required field names into a set.
        #
        # Why:
        # - We need very fast membership checks to know whether an incoming field
        #   matters for completion.
        # - A set gives average O(1) lookup time.
        required_set: Set[str] = set(requiredFields)

        # Step 2:
        # Store the total number of distinct required fields.
        #
        # Why:
        # - A form is complete exactly when it has seen this many unique required fields.
        required_count: int = len(required_set)

        # Step 3:
        # For each formId, store the set of required fields that have been seen so far.
        #
        # Example:
        # form_fields[101] = {"name", "email"}
        #
        # Why a set:
        # - Duplicate submissions of the same field should only count once.
        # - A set naturally removes duplicates.
        form_fields: Dict[int, Set[str]] = {}

        # Step 4:
        # Track which forms have already become complete.
        #
        # Why:
        # - Once a form becomes complete, later repeated submissions should not cause
        #   it to be "completed again".
        # - We only care about the first moment a form reaches completion.
        completed_forms: Set[int] = set()

        # Step 5:
        # Count how many forms have already completed with the required signature.
        #
        # In this problem, every completed form has the same required-field signature:
        # exactly all required fields (ignoring non-required fields).
        #
        # Therefore, if this count is already at least 1 when a new form becomes
        # complete, then the new form is a completed duplicate.
        completed_signature_count: int = 0

        # Step 6:
        # Process each submission in the exact given order.
        #
        # This is critical because the answer depends on which form becomes a completed
        # duplicate earliest in the stream.
        for submission in submissions:
            form_id = int(submission[0])
            field_name = str(submission[1])

            # Ignore non-required fields completely.
            #
            # Why:
            # - They do not help a form move toward completion.
            # - They do not affect the required-field signature.
            if field_name not in required_set:
                continue

            # If this form has never been seen before with a required field,
            # create an empty set for it.
            if form_id not in form_fields:
                form_fields[form_id] = set()

            # If the form is already complete, we can skip further work.
            #
            # Why:
            # - Completion only happens once.
            # - Repeated required-field submissions after completion do not change
            #   anything relevant to the answer.
            if form_id in completed_forms:
                continue

            # Add the required field to this form's seen set.
            #
            # Because this is a set, repeated submissions of the same field do not
            # increase the size more than once.
            form_fields[form_id].add(field_name)

            # Check whether this form has now collected all required fields.
            #
            # If yes, this is the exact moment the form becomes complete.
            if len(form_fields[form_id]) == required_count:
                # Mark this form as completed so we never process completion for it again.
                completed_forms.add(form_id)

                # If at least one form had already completed earlier, then this form
                # is a completed duplicate at the moment it becomes complete.
                if completed_signature_count >= 1:
                    return form_id

                # Otherwise, this is the first completed form overall.
                # It establishes the signature for future duplicates.
                completed_signature_count += 1

        # If we finish processing the entire stream and never found a completed
        # duplicate, return -1.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    required_fields_1 = ["name", "email", "phone"]
    submissions_1 = [
        [101, "name"],
        [102, "email"],
        [101, "email"],
        [102, "name"],
        [101, "phone"],
        [102, "phone"],
    ]
    result_1 = solution.first_completed_duplicate_form(required_fields_1, submissions_1)
    print(result_1)  # Expected: 102

    # Example 2
    required_fields_2 = ["id", "photo"]
    submissions_2 = [
        [7, "id"],
        [8, "id"],
        [7, "photo"],
        [7, "photo"],
        [9, "photo"],
        [8, "photo"],
    ]
    result_2 = solution.first_completed_duplicate_form(required_fields_2, submissions_2)
    print(result_2)  # Expected: 8

    # Additional example: no duplicate completion
    required_fields_3 = ["a", "b"]
    submissions_3 = [
        [1, "a"],
        [2, "a"],
        [3, "x"],
        [1, "b"],
    ]
    result_3 = solution.first_completed_duplicate_form(required_fields_3, submissions_3)
    print(result_3)  # Expected: -1