package fr.ribesg.alix.api;

import fr.ribesg.alix.api.message.JoinMessage;
import fr.ribesg.alix.api.message.Message;
import fr.ribesg.alix.api.message.NickMessage;
import fr.ribesg.alix.api.message.QuitMessage;
import fr.ribesg.alix.api.message.UserMessage;
import fr.ribesg.alix.network.SocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ribesg
 */
public class Server {

	/**
	 * A useful reference to the Client
	 */
	private final Client client;

	/**
	 * The url used to connect to this server
	 * May be a hostname or an IP
	 */
	private final String url;

	/**
	 * The port used to connect to this server
	 * Default: 6667
	 */
	private final int port;

	/**
	 * If this Server should be joined other SSL or not
	 */
	private final boolean useSSL;

	/**
	 * Channels on which the Client is connected or
	 * will be connected on this Server
	 */
	private final Map<String, Channel> channels;

	/**
	 * The SocketHandler dedicated to this Server
	 */
	private SocketHandler socket;

	/**
	 * Store if the Client is connected to this Server or not
	 */
	private boolean connected;

	/**
	 * Main constructor.
	 *
	 * @param client the Client this Server is / will be connected to
	 * @param url    the url of this Server (IP or FQDN)
	 * @param port   the port of this Server
	 * @param useSSL if this connection uses / will use SSL
	 */
	public Server(final Client client, final String url, final int port, final boolean useSSL) {
		this.client = client;
		this.url = url;
		this.port = port;
		this.useSSL = useSSL;
		this.channels = new HashMap<>();
		this.socket = null;
		this.connected = false;
	}

	/**
	 * Convenient constructor for SSL-free connection.
	 *
	 * @param client the Client this Server is / will be connected to
	 * @param url    the url of this Server (IP or FQDN)
	 * @param port   the port of this Server
	 */
	public Server(final Client client, final String url, final int port) {
		this(client, url, port, false);
	}

	/**
	 * @return the Client
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * Gets a Channel object from its name.
	 *
	 * @param channelName the name of the Channel
	 *
	 * @return a Channel object, or null
	 */
	public Channel getChannel(final String channelName) {
		return this.channels.get(channelName);
	}

	/**
	 * Adds a Channel to the Set of Channels for this Server
	 *
	 * @param channelName the name of the Channel to add
	 */
	public void addChannel(final String channelName) {
		this.channels.put(channelName, new Channel(this, channelName));
	}

	/**
	 * Adds a password-protected Channel to the Set of Channels for
	 * this Server
	 *
	 * @param channelName the name of the Channel to add
	 * @param password    the password of the Channel to add
	 */
	public void addChannel(final String channelName, final String password) {
		this.channels.put(channelName, new Channel(this, channelName, password));
	}

	/**
	 * Sends a JOIN Command for every Channels in the Set
	 */
	public void joinChannels() {
		if (!connected) {
			throw new IllegalStateException("Not Connected!");
		}
		for (final Channel channel : channels.values()) {
			if (channel.hasPassword()) {
				send(new JoinMessage(channel.getName(), channel.getPassword()));
			} else {
				send(new JoinMessage(channel.getName()));
			}
		}
	}

	/**
	 * Gets the URL of this Server.
	 * IT could be either an IP or a FQDN.
	 *
	 * @return the URL of this Server
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * Gets the port of this Server.
	 *
	 * @return the port of this Server
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Gets if this connection uses / will use SSL.
	 *
	 * @return true if this connection uses / will use SSL, false otherwise
	 */
	public boolean useSsl() {
		return useSSL;
	}

	/**
	 * @return true if the Client is connected to this Server,
	 * false otherwise
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Modifies the connected state of this Server.
	 * This is called by the
	 * {@link fr.ribesg.alix.network.InternalMessageHandler}, please
	 * do not use it.
	 * <p/>
	 * This is nothing more than a Setter for {@link #connected}, please
	 * use {@link #connect()} and {@link #disconnect()}.
	 *
	 * @param connected the value wanted for the connected state
	 */
	public void setConnected(final boolean connected) {
		this.connected = connected;
	}

	/**
	 * Connects the Client to the Server.
	 * This is a non-blocking method.
	 * <p/>
	 * Note: The Client is <strong>not</strong> connected directly after this method call.
	 */
	public void connect() {
		if (connected) {
			throw new IllegalStateException("Already Connected!");
		} else {
			this.socket = new SocketHandler(this, this.url, this.port);
			try {
				this.socket.connect();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			this.socket.write(new NickMessage(client.getName()));
			this.socket.write(new UserMessage(client.getName()));
		}
	}

	/**
	 * Disconnects the Client from the Server.
	 * This is a blocking method.
	 * <p/>
	 * Note: The Client is disconnected directly after this method call.
	 */
	public void disconnect() {
		if (!connected) {
			throw new IllegalStateException("Not Connected!");
		} else {
			this.socket.write(new QuitMessage("Working on the future"));
			while (this.socket.hasAnythingToWrite()) {}
			this.socket.askStop();
			while (!this.socket.isStopped()) {}
			this.socket.kill();
		}
	}

	/**
	 * Sends a RAW message to this Receiver.
	 *
	 * @param message the String message to be sent
	 */
	public void sendRaw(final String message) {
		if (this.socket == null) {
			throw new IllegalStateException("Not connected!");
		} else {
			this.socket.writeRaw(message);
		}
	}

	/**
	 * Sends a message to this Server.
	 *
	 * @param message the message to be sent
	 */
	public void send(final Message message) {
		this.sendRaw(message.getRawMessage());
	}
}