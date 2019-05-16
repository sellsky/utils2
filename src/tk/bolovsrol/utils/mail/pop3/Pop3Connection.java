package tk.bolovsrol.utils.mail.pop3;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.io.LineOutputStream;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.socket.client.PlainSocketFactory;
import tk.bolovsrol.utils.socket.client.SocketFactory;
import tk.bolovsrol.utils.socket.client.SslSocketFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/** Соединение с почтовым ящичком и управление транслятором. */
public class Pop3Connection {

    private final LogDome log;
	private boolean debugLog = Cfg.getBoolean("log.pop3", false);

	private final String hostname;
	private final int port;
	private final String username;
    private final String password;
    private final String charset;
    private final MessageParser messageParser;
    private final SocketFactory socketFactory;

    // Ради UMC синхронизация внутри этого класса снята.
    private final Object connectionLock = new Object();

    private Socket socket;
    private LineInputStream lis;
    private LineOutputStream los;

	private static final Map<String, SocketFactory> SOCKET_PROVIDERS = new HashMap<>(2);

    static {
        SOCKET_PROVIDERS.put("pop3", PlainSocketFactory.getStatic());
        SOCKET_PROVIDERS.put("pop3s", SslSocketFactory.getDefault());
    }

    public Pop3Connection(LogDome log, Uri pop3ServerUri) {
        this(log, pop3ServerUri, "UTF-8");
    }

    public Pop3Connection(LogDome log, Uri pop3ServerUri, String charset) {
        this.log = log;
        this.hostname = pop3ServerUri.getHostname();
        this.port = pop3ServerUri.getPortIntValue(Pop3Const.DEFAULT_PORT);
        this.username = pop3ServerUri.getUsername();
        this.password = pop3ServerUri.getPassword();
        this.messageParser = new MessageParser(log);
        this.charset = charset;
        this.socketFactory = SOCKET_PROVIDERS.get(pop3ServerUri.getScheme());
        if (this.socketFactory == null) {
            throw new IllegalArgumentException("Unexpected scheme " + Spell.get(pop3ServerUri.getScheme()));
        }
    }

    public boolean isDebugLog() {
        return debugLog;
    }

    public void setDebugLog(boolean debugLog) {
        this.debugLog = debugLog;
    }

    public void open() throws IOException, UnexpectedBehaviourException {

        if (socket != null) {
            throw new IllegalStateException("Connection already open");
        }
        socket = socketFactory.newSocket();
        socket.setSoTimeout(30000);
        socket.connect(new InetSocketAddress(hostname, port));
        synchronized (connectionLock) {
            lis = new LineInputStream(socket.getInputStream());
            los = new LineOutputStream(socket.getOutputStream());

            login();
        }
    }

    private void login() throws IOException, UnexpectedBehaviourException {
        getResponse();

//        write("APOP " + username + " " + MD5.getHexDumpFor(password));
//        checkResponse();

        write("USER " + username);
        write("PASS " + password);
    }

    private String write(String command) throws IOException, UnexpectedBehaviourException {
        synchronized (connectionLock) {
            sendCommand(command);
            return getResponse();
        }
    }

    private void sendCommand(String command) throws IOException {
        if (debugLog) {
            log.trace("<< " + Spell.get(command));
        }
        los.write(command);
        los.write("\r\n");
        los.flush();
    }

    private String getResponse() throws IOException, UnexpectedBehaviourException {
        String response = lis.readLine();
        if (debugLog) {
            log.trace(">> " + Spell.get(response));
        }
        if (response == null) {
            throw new UnexpectedBehaviourException("No response obtained from POP3 server");
        }
        if (!response.startsWith("+OK")) {
            throw new UnexpectedBehaviourException("POP3 server reports an error: " + Spell.get(response));
        }
        return response;
    }

    public int getMessagesCount() throws IOException, UnexpectedBehaviourException {
        return Integer.parseInt(StringUtils.subWords(write("STAT"), 1, 2));
    }

    public Pop3Message retrieveMessage(int index) throws IOException, UnexpectedBehaviourException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        synchronized (connectionLock) {
            write("RETR " + index);
            while (true) {
                byte[] line = lis.readUntilLineSeparator();
                if (line == null) {
                    throw new UnexpectedBehaviourException("Went out of data while reading message");
                } else {
                    if (debugLog) {
                        log.trace("*> " + Spell.get(StringUtils.getAsciiPrintable(line, '?')));
                    }
                    if (line.length == 1 && line[0] == (byte) '.') {
                        break;
                    } else {
                        if (raw.size() != 0) {
                            raw.write((int) '\n');
                        }
                        if (line.length >= 2 && line[0] == (byte) '.' && line[1] == (byte) '.') {
                            raw.write(line, 1, line.length - 1); // точечка в первой позиции приходит удвоенной
                        } else {
                            raw.write(line);
                        }
                    }
                }
            }
        }

        return messageParser.read(raw.toByteArray(), charset);
    }

    public void deleteMessage(int index) throws IOException, UnexpectedBehaviourException {
        write("DELE " + index);
    }

    public boolean isOpen() {
        return socket != null;
    }

    public void close() {
        synchronized (connectionLock) {
            if (socket != null) {
                try {
                    if (los != null) {
                        write("QUIT");
                    }
                    socket.close();
                } catch (Exception ignore) {
                } finally {
                    los = null;
                    lis = null;
                    socket = null;
                }
            }
        }
    }

//    public static void main(String[] args) throws Exception {
//        Pop3Connection pc = new Pop3Connection(Log.getInstance(), Uri.parse("pop3://user:password@mailserver:110"));
//        pc.setDebugLog(true);
//        try {
//            Log.hint("Opening connection..");
//            pc.open();
//            Log.hint("Connection open.");
//            int messagesCount = pc.getMessagesCount();
//            Log.hint("There are " + messagesCount + " messages");
//
//            for (int i = 1; i <= messagesCount; i++) {
//
//                Pop3Message msg = pc.retrieveMessage(i);
//                Log.hint("Message " + i + ": " + Spell.get(msg));
//                Log.hint("Msg from: " + Spell.get(msg.getDecodedFrom()));
//                Log.hint("Msg to: " + Spell.get(msg.getDecodedTo()));
//                Log.hint("Msg subject: " + Spell.get(msg.getDecodedSubject()));
//                Log.hint("Msg cc: " + Spell.get(msg.getDecodedCC()));
//                if (msg.isMultipart()) {
//                    Pop3Message[] subm = new MultipartPayloadDecoder().decode(msg);
//                    Log.hint("Multipart, submessages count: " + subm.length);
//                    for (int ii = 0; ii < subm.length; ii++) {
//                        Pop3Message submessage = subm[ii];
////                    Log.hint("submsg " + i + ": " + Spell.get(submessage));
//                        if (submessage.checkContentType("text")) {
//                            Log.hint("Submsg " + i + '.' + ii + " text payload: " + TextPayloadDecoder.decode(submessage));
//                        }
//                    }
//                } else {
//                    Log.hint("Message is single part. ");
//                    if (msg.checkContentType("text")) {
//                        Log.hint("Text payload: " + TextPayloadDecoder.decode(msg));
//                    }
//                }
//
//                Log.hint("-------------------");
//            }
//
//        } finally {
//            pc.close();
//        }
//    }
}
