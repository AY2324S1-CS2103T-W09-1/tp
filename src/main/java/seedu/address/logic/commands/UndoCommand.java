package seedu.address.logic.commands;


import static java.util.Objects.requireNonNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.util.Pair;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Undoes the most recent undoable command. Undoable commands are: delete, clear, add, edit.
 */
public class UndoCommand extends Command {

    public static final String COMMAND_WORD = "undo";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Undoes the most recent undoable command. Undoable commands are: delete, clear, add, edit.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_FAILURE_UNDO_WITH_NO_UNDOABLE_COMMANDS = "There is no command to undo!";
    public static final String MESSAGE_UNDO_DELETE_SUCCESS = "Undo Successful! Contact added back: %1$s";
    public static final String MESSAGE_UNDO_CLEAR_SUCCESS = "Undo Successful! All contacts have been added back!";
    public static final String MESSAGE_UNDO_ADD_SUCCESS = "Undo Successful! Deleted Person: %1$s";
    public static final String MESSAGE_UNDO_EDIT_SUCCESS = "Undo Successful! Reverted back to: %1$s";

    private static Logger logger = Logger.getLogger("UndoCommand");

    /**
     * Constructor for UndoCommand is empty.
     */
    public UndoCommand() {};

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.getPreviousUndoableCommandsSize() == 0) {
            throw new CommandException(MESSAGE_FAILURE_UNDO_WITH_NO_UNDOABLE_COMMANDS);
        }

        switch(model.getPreviousUndoableCommand()) {
        case "delete":
            logger.log(Level.INFO, "case: delete");
            return executeUndoDelete(model);
        case "clear":
            logger.log(Level.INFO, "case: clear");
            return executeUndoClear(model);
        case "add":
        case "clone":
            logger.log(Level.INFO, "case: add");
            return executeUndoAdd(model);
        case "edit":
            logger.log(Level.INFO, "case: edit");
            return executeUndoEdit(model);
        default:
            throw new AssertionError("Not an undoable command! There is an error!");
        }
    }

    /**
     * Undoes a delete command.
     * @return returns CommandResult of the message when the undo is a success.
     */
    public CommandResult executeUndoDelete(Model model) {

        Person deletedPerson = model.getDeletedPerson();
        model.undoDelete();
        model.removePreviousUndoableCommand();
        return new CommandResult(String.format(MESSAGE_UNDO_DELETE_SUCCESS, Messages.format(deletedPerson)));
    }

    /**
     * Undoes a clear command.
     * @return returns CommandResult of the message when the undo is a success.
     */
    public CommandResult executeUndoClear(Model model) {

        int numberOfPreviousDeleteCommands = model.getNumberOfPreviousDeleteCommands();
        while (model.getDeletedPersonsSize() > numberOfPreviousDeleteCommands) {
            model.undoDelete();
        }
        model.removePreviousUndoableCommand();
        return new CommandResult(String.format(MESSAGE_UNDO_CLEAR_SUCCESS));
    }

    /**
     * Undoes an add command.
     * @return returns CommandResult of the message when the undo is a success.
     */
    public CommandResult executeUndoAdd(Model model) {

        /**
        List<Person> lastShownList = model.getFilteredPersonList();
        int lastIndex = model.getAddressBookSize() - 1;
        Person personToDelete = lastShownList.get(lastIndex);
         **/

        Person addedPerson = model.getAddedPerson();
        model.deletePerson(addedPerson);
        model.removePreviousUndoableCommand();
        return new CommandResult(String.format(MESSAGE_UNDO_ADD_SUCCESS, Messages.format(addedPerson)));
    }

    /**
     * Undoes an edit command.
     * @return returns CommandResult of the message when the undo is a success.
     */
    public CommandResult executeUndoEdit(Model model) {
        Pair<Person, Person> pairToRestore = model.getEditedPersonsPair();
        Person editedPerson = pairToRestore.getKey();
        Person originalPerson = pairToRestore.getValue();
        model.setPerson(editedPerson, originalPerson);
        model.removeEditedPersonsPair();
        model.removePreviousUndoableCommand();

        return new CommandResult(String.format(MESSAGE_UNDO_EDIT_SUCCESS, Messages.format(originalPerson)));
    }
}
