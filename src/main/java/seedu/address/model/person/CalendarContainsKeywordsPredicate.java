package seedu.address.model.person;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;
/**
 * Tests that a {@code Person}'s {@code AppointmentDate} matches any of the keywords given.
 */
public class CalendarContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    public CalendarContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Person person) {
        return keywords.stream()
                .anyMatch(keyword -> StringUtil.containsWordIgnoreCase(person.getApptDate().value, keyword));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CalendarContainsKeywordsPredicate)) {
            return false;
        }

        CalendarContainsKeywordsPredicate otherNameContainsKeywordsPredicate =
                (CalendarContainsKeywordsPredicate) other;
        return keywords.equals(otherNameContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
