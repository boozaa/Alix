package fr.ribesg.alix.api.message;
import fr.ribesg.alix.api.enums.Command;

/**
 * This class allow easy build of a PRIVMSG Message.
 */
public class PrivMsgMessage extends Message {

	public PrivMsgMessage(final String receiver, final String message) {
		super(null, Command.PRIVMSG.name(), message, receiver);
	}
}
