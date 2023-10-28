package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.commons.util.ComparatorUtil.APPTCOMPARATOR;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.util.Pair;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final ArrayList<Person> deletedPersons;
    private final ArrayList<Pair<Person, Person>> editedPersons;
    private final ArrayList<Person> addedPersons;
    private ArrayList<String> previousUndoableCommands;
    private Comparator<Person> sortComparator;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        deletedPersons = new ArrayList<>();
        editedPersons = new ArrayList<>();
        addedPersons = new ArrayList<>();
        previousUndoableCommands = new ArrayList<>();
        sortComparator = APPTCOMPARATOR;
    }

    /**
     * Default constructor for ModelManager
     */
    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    @Override
    public void storeDeletedPerson(Person deletedPerson) {
        this.deletedPersons.add(deletedPerson);
    }

    @Override
    public Person getDeletedPerson() {
        int lastIndex = deletedPersons.size() - 1;
        return this.deletedPersons.get(lastIndex);
    }

    @Override
    public void removeDeletedPerson() {
        int lastIndex = this.deletedPersons.size() - 1;
        this.deletedPersons.remove(lastIndex);
    }

    @Override
    public int getDeletedPersonsSize() {
        return this.deletedPersons.size();
    }

    @Override
    public int getPreviousUndoableCommandsSize() {
        return this.previousUndoableCommands.size();
    }

    @Override
    public int getNumberOfPreviousDeleteCommands() {
        int occurrences = Collections.frequency(previousUndoableCommands, "delete");
        return occurrences;
    }

    @Override
    public void storePreviousUndoableCommand(String command) {
        this.previousUndoableCommands.add(command);
    }

    @Override
    public String getPreviousUndoableCommand() {
        int lastIndex = this.getPreviousUndoableCommandsSize() - 1;
        return this.previousUndoableCommands.get(lastIndex);
    }

    @Override
    public void removePreviousUndoableCommand() {
        int lastIndex = this.getPreviousUndoableCommandsSize() - 1;
        this.previousUndoableCommands.remove(lastIndex);
    }

    @Override
    public void storeAddedPerson(Person addedPerson) {
        addedPersons.add(addedPerson);
    }

    @Override
    public Person getAddedPerson() {
        int lastIndex = this.addedPersons.size() - 1;
        return this.addedPersons.get(lastIndex);
    }

    @Override
    public void removeAddedPerson() {
        int lastIndex = this.addedPersons.size() - 1;
        this.addedPersons.remove(lastIndex);
    }

    @Override
    public void storeEditedPersonsPair(Person editedPerson, Person originalPerson) {
        Pair<Person, Person> toStore = new Pair<>(editedPerson, originalPerson);
        editedPersons.add(toStore);
    }

    @Override
    public Pair<Person, Person> getEditedPersonsPair() {
        int lastIndex = editedPersons.size() - 1;
        return editedPersons.get(lastIndex);
    };

    @Override
    public void removeEditedPersonsPair() {
        int lastIndex = editedPersons.size() - 1;
        editedPersons.remove(lastIndex);
    }

    @Override
    public int getAddedPersonsSize() {
        return this.addedPersons.size();
    }



    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getAddressBookFilePath() {
        return userPrefs.getAddressBookFilePath();
    }

    @Override
    public void setAddressBookFilePath(Path addressBookFilePath) {
        requireNonNull(addressBookFilePath);
        userPrefs.setAddressBookFilePath(addressBookFilePath);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
        storeDeletedPerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        storeAddedPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    @Override
    public void undoDelete() {
        addressBook.addPerson(getDeletedPerson());
        this.removeDeletedPerson();
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }





    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons.sorted(sortComparator);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void updateSortComparator(Comparator<Person> comparator) {
        requireNonNull(comparator);
        sortComparator = comparator;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager)) {
            return false;
        }

        ModelManager otherModelManager = (ModelManager) other;
        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons);
    }

    @Override
    public int getAddressBookSize() {
        return addressBook.getSize();
    }

}
