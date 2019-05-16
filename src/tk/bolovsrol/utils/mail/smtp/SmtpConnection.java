package tk.bolovsrol.utils.mail.smtp;

import tk.bolovsrol.utils.ArrayUtils;
import tk.bolovsrol.utils.Base64;
import tk.bolovsrol.utils.MD5;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.box.Box;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.io.LineOutputStream;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.mail.MailAddress;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.socket.client.PlainSocketFactory;
import tk.bolovsrol.utils.socket.client.SocketFactory;
import tk.bolovsrol.utils.socket.client.SslSocketFactory;

import java.io.IOException;
import java.net.IDN;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class SmtpConnection implements AutoCloseable {

	public static final int DEFAULT_IO_TIMEOUT = Cfg.getInteger("smtp.ioTimeout.ms", 30000, Log.getInstance());
	public static final boolean DEFAULT_DEBUG_LOG = Cfg.getBoolean("log.smtp", false);
	public static final String DEFAULT_HELO = Cfg.get("smtp.helo", "PlasticMedia");
	private static final LogDome DEFAULT_LOG = Cfg.has("log.smtp.out") ? new LogDome(Cfg.get("log.smtp.out")) : null;

    private final SimpleDateFormat msgDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    private final CharsetEncoder asciiEncoder = Charset.forName("ASCII").newEncoder();
    private boolean debugLog;

    private final LogDome log;
    private final Uri smtpServerUri;
    private final String heloName;
    private final SocketFactory socketFactory;

    private String[] features;
    private boolean extendedProtocol;

    private Socket socket = null;

    private LineInputStream lis;
    private LineOutputStream los;

    private int ioTimeout;
    private static final Map<String, SocketFactory> SOCKET_PROVIDERS = new HashMap<>(2);

    static {
        SOCKET_PROVIDERS.put(SmtpConst.SCHEME_SMTP, PlainSocketFactory.getStatic());
        SOCKET_PROVIDERS.put(SmtpConst.SCHEME_SMTPS, SslSocketFactory.getDefault());
    }

    public SmtpConnection(LogDome logOrNull, Uri smtpServerUri, String heloName) {
        this(logOrNull, smtpServerUri, heloName, DEFAULT_IO_TIMEOUT);
    }

    public SmtpConnection(LogDome logOrNull, Uri smtpServerUri, String heloName, int ioTimeout) {
        this.log = Box.with(DEFAULT_LOG).getOr(logOrNull);
        this.debugLog = this.log != null && DEFAULT_DEBUG_LOG;
        this.smtpServerUri = smtpServerUri;
        this.socketFactory = SOCKET_PROVIDERS.get(smtpServerUri.getScheme());
        if (this.socketFactory == null) {
            throw new IllegalArgumentException("Unexpected scheme " + Spell.get(smtpServerUri.getScheme()));
        }
        this.heloName = heloName;
        this.ioTimeout = ioTimeout;
    }

    /**
     * Открывает закрытое соединение, выполняет логин.
     * Если соединение уже открыто, ничего не делает.
     * <p/>
     * Соединение можно многократно закрывать методом {@link #close()} и открывать снова.
     * <p/>
     * * @throws IOException
     *
     * @throws SmtpResponseException
     * @throws LoginFailedException
     */
    public void open() throws IOException, SmtpResponseException, LoginFailedException {
        if (isOpen()) {
            return;
        }

        estabilishSocketConnection();

        loginChecked();
    }

    private void loginChecked() throws IOException, SmtpResponseException, LoginFailedException {
        checkResponse("220");
        write("EHLO " + heloName);
        try {
            String[] response = getResponse("250");
            features = new String[response.length - 1];
            for (int i = 0; i < features.length; i++) {
                features[i] = response[i + 1].substring(4);
            }
            extendedProtocol = true;
            login();
        } catch (SmtpResponseException e) {
            if ("500".equals(e.getCode())) {
                write("HELO " + heloName);
                checkResponse("250");
                features = null;
                extendedProtocol = false;
            } else {
                throw e;
            }
        }
    }

	@SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"}) private void estabilishSocketConnection() throws IOException {
		Socket socket = socketFactory.newSocket();
        LineInputStream lis;
        LineOutputStream los;
        try {
            socket.setSoTimeout(ioTimeout);
            socket.connect(
                  new InetSocketAddress(
                        smtpServerUri.getHostname(),
                        smtpServerUri.getPortIntValue(SmtpConst.DEFAULT_SMTP_PORT)
                  )
            );
            lis = new LineInputStream(socket.getInputStream());
            los = new LineOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
            throw e;
        }

        this.socket = socket;
        this.lis = lis;
        this.los = los;
    }

    private void write(String command) throws IOException {
        if (debugLog) {
            log.trace("<< " + Spell.get(command));
        }
        los.write(command);
        los.write("\r\n");
    }

    private void writeIfNotNull(String command, String argument) throws IOException {
        if (argument != null) {
            write(command + argument);
        }
    }

    private String[] getLoginMethods() {
        if (features == null || features.length == 0) {
            return null;
        }
        for (String feature : features) {
            if (feature.startsWith("AUTH")) {
                return StringUtils.parseDelimited(StringUtils.subWords(feature, 1), " ");
            }
        }
        return null;
    }

    /**
     * Пытаемся сделать логин наилучшим способом.
     * Приоритеты: CRAM-MD5 > PLAIN > LOGIN.
     */
    private void login() throws IOException, SmtpResponseException, LoginFailedException {
        if (!extendedProtocol) {
            return;
        }
        if (!smtpServerUri.hasUsername() || !smtpServerUri.hasPassword()) {
            return;
        }
        String[] methods = getLoginMethods();
        if (methods == null) {
            return;
        }
        if (StringUtils.contains("CRAM-MD5", methods)) {
            loginCramMd5();
        } else if (StringUtils.contains("PLAIN", methods)) {
            loginPlain();
        } else if (StringUtils.contains("LOGIN", methods)) {
            loginLogin();
        } else {
            throw new LoginFailedException("No supported login methods provided: " + Spell.get(methods));
        }
        try {
            checkResponse("235");
        } catch (SmtpResponseException e) {
            throw new LoginFailedException(e);
        }
    }

    private void loginPlain() throws IOException {
        String loginString = '\u0000' + smtpServerUri.getUsername() + '\u0000' + smtpServerUri.getPassword();
        String encodedString = Base64.byteArrayToBase64(loginString.getBytes());
        write("AUTH PLAIN " + encodedString);
    }

    private void loginLogin() throws IOException, SmtpResponseException {
        write("AUTH LOGIN");
        checkResponse("334");
        write(Base64.byteArrayToBase64(smtpServerUri.getUsername().getBytes()));
        checkResponse("334");
        write(Base64.byteArrayToBase64(smtpServerUri.getPassword().getBytes()));
    }

    private void loginCramMd5() throws IOException, SmtpResponseException, LoginFailedException {
        write("AUTH CRAM-MD5");

        String[] challengeResponse = getResponse("334");
        String challengeBase64;
        try {
            challengeBase64 = challengeResponse[0].substring(4);
        } catch (Throwable e) {
            throw new LoginFailedException("Cannot parse challenge data, ", e);
        }
        byte[] challenge = Base64.base64ToByteArray(challengeBase64);

        byte[] key = smtpServerUri.getPassword().getBytes();
        if (key.length > 64) {
            key = MD5.getFor(key);
        }

        byte[] a = new byte[64];
        Arrays.fill(a, (byte) 0x5C);
        for (int i = 0; i < key.length; i++) {
            a[i] ^= key[i];
        }

        byte[] b = new byte[64];
        Arrays.fill(b, (byte) 0x36);
        for (int i = 0; i < key.length; i++) {
            b[i] ^= key[i];
        }

        byte[] secret = MD5.getFor(ArrayUtils.join(a, MD5.getFor(ArrayUtils.join(b, challenge))));

        String response = Base64.byteArrayToBase64(
              ArrayUtils.join(
                    smtpServerUri.getUsername().getBytes(),
                    new byte[]{(byte) 0x20},
                    StringUtils.getHexDump(secret).getBytes()
              )
        );

        write(response);
    }

    private void checkResponse(String code) throws IOException, SmtpResponseException {
        getResponse(code);
    }

    private String[] getResponse(String code) throws IOException, SmtpResponseException {
        los.flush();
        String response = lis.readLine();
        if (debugLog) {
            log.trace(">>  " + Spell.get(response));
        }
        if (response == null || !response.startsWith(code)) {
            throw new SmtpResponseException("SMTP server reports an error: " + Spell.get(response), StringUtils.subWords(response, 0, 1));
        }
        if (response.startsWith(code + '-')) {
            return getRestOfMultilineResponse(code + '-', response);
        } else {
            return new String[]{response};
        }
    }

    private String[] getRestOfMultilineResponse(String code, String firstLine) throws IOException {
        ArrayList<String> al = new ArrayList<>();
        al.add(firstLine);
        while (true) {
            String response = lis.readLine();
            if (debugLog) {
                log.trace("+>  " + Spell.get(response));
            }
            al.add(response);
            if (!response.startsWith(code)) {
                return al.toArray(new String[al.size()]);
            }
        }
    }

    public String sendMessage(SmtpMessage m) throws IOException, SmtpResponseException {
        write("MAIL FROM:<" + m.getFrom().getRawAddress() + '>');
        checkResponse("250");

        writeRcptIfNotNull(m.getTo());
        writeRcptIfNotNull(m.getCc());
        writeRcptIfNotNull(m.getBcc());

        write("DATA");
        checkResponse("354");

        write("MIME-Version: 1.0");
        writeIfNotNull("Message-Id: ", m.getId());
        write("From: " + encodeAddressesForHeader(Collections.singletonList(m.getFrom())));
        write("To: " + encodeAddressesForHeader(m.getTo()));
        writeIfNotNull("Cc: ", encodeAddressesForHeader(m.getCc()));
        write("Date: " + msgDateFormat.format(m.getDate()));
        write("Content-Transfer-Encoding: " + m.getContentTransferEncoder().getContentTransferEncoding());
        write("Content-Type: " + m.getContentTypeEncoder().getContentType());
        writeIfNotNull("Subject: ", Base64MimeEncoder.encode(m.getSubject()));
        write("");
        write(m.getContentTransferEncoder().encode(m.getContentTypeEncoder().encode(m.getPayload())));
        write("");
        write(".");
        String response = getResponse("250")[0];
        String id = StringUtils.subWords(response, 2, 3);
        if (id.startsWith("id=")) {
            return id.substring(3);
        } else {
            return id;
        }
    }

    /**
     * Энкодит имя в base64, если оно не аски, и адрес в IDN, если он не аски.
     * <p/>
     * Несколько адресов перечисляет через запятую.
     *
     * @param addresses
     * @return
     */
    private String encodeAddressesForHeader(Collection<MailAddress> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        StringDumpBuilder sdb = new StringDumpBuilder(", ");
        for (MailAddress address : addresses) {
            if (address.getName() != null) {
                if (asciiEncoder.canEncode(address.getName())) {
                    sdb.append(address.getName() + " <" + IDN.toASCII(address.getRawAddress()) + '>');
                } else {
                    sdb.append(Base64MimeEncoder.encode(address.getName()) + " <" + IDN.toASCII(address.getRawAddress()) + '>');
                }
            } else {
                sdb.append(IDN.toASCII(address.getRawAddress()));
            }
        }
        return sdb.toString();
    }

    private void writeRcptIfNotNull(List<MailAddress> addresses) throws IOException, SmtpResponseException {
        if (addresses != null) {
            for (MailAddress address : addresses) {
                write("RCPT TO:<" + IDN.toASCII(address.getRawAddress()) + '>');
                checkResponse("250");
            }
        }
    }

    public boolean isOpen() {
        return socket != null;
    }

    /**
     * Отправляет серверу QUIT, дожидается ответа и закрывает соединение.
     * Если соединение закрыто, ничего не делает.
     * <p/>
     * В дальнейшем соединение можно открыть снова методом {@link #open()}.
     * <p/>
     * Метод может выкинуть исключение, если не удастся выйти «красиво».
     * Но даже в этом случае соединение будет разорвано.
     *
     * @throws SmtpResponseException
     * @throws IOException
     * @see #drop()
     */
    @Override public void close() throws SmtpResponseException, IOException {
        if (socket == null) {
            return;
        }
        try {
            write("QUIT");
            checkResponse("221");
        } finally {
            drop();
        }
    }

    /**
     * Закрывает открытое соединение, не отправляя серверу уведомлений.
     * Если соединение закрыто, ничего не делает.
     * <p/>
     * В дальнейшем соединение можно открыть снова методом {@link #open()}.
     * <p/>
     * Метод может выкинуть исключение, если не удастся выйти «красиво».
     * Но даже в этом случае соединение будет разорвано.
     *
     * @throws IOException
     * @see #close()
     */
    public void drop() throws IOException {
        if (socket == null) {
            return;
        }
        lis.close();
        los.close();
        socket.close();
        los = null;
        lis = null;
        socket = null;
    }

    public boolean isExtendedProtocol() {
        return extendedProtocol;
    }


    public boolean isDebugLog() {
        return debugLog;
    }

    public void setDebugLog(boolean debugLog) {
        this.debugLog = debugLog && log != null;
    }

    public long getIoTimeout() {
        return (long) ioTimeout;
    }

    /**
     * Устанавливает время ожидания для операций ввода-вывода.
     * <p/>
     * Этот параметр нужно устанавливать до открытия соединения.
     *
     * @param ioTimeout
     */
    public void setIoTimeout(int ioTimeout) {
        this.ioTimeout = ioTimeout;
    }

//    public static void main(String[] args) throws Exception {
////        Date date = new Date();
//
////        String l = "Date: Wed, 6 Dec 2006 13:00:30 +0200";
////        SimpleDateFormat msgDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
////        System.out.println(Spell.get(msgDateFormat.format(date)));
//        UmcMessage m = new UmcMessage();
//        m.set("1234568", "5373", "380951556973", new Date(), "Hello moto! Привет теперь в koi8-r!");
//
//        Url url = new Url("smtp://mail.plasticmedia.ru:25");
//
//        UmcSmtpConnection con = new UmcSmtpConnection(
//                new LogDome(), url,
//                "umc.plasticmedia.ru",
//                "partner.umc.com.ua", "koi8-r");
//
//        con.open();
//        try {
//            String id = con.sendMessage(m);
//            System.out.println("id=" + Spell.get(id));
////            System.out.println("message1: " + Spell.get(con.retrieveMessage(1)));
////            con.deleteMessage(1);Pro
////            System.out.println("message2: " + Spell.get(con.retrieveMessage(2)));
//        } finally {
//            con.close();
//        }
//    }


    @Override public String toString() {
        return new StringDumpBuilder()
              .append("msgDateFormat", msgDateFormat)
              .append("asciiEncoder", asciiEncoder)
              .append("smtpServerUri", smtpServerUri)
              .append("heloName", heloName)
              .append("socketFactory", socketFactory)
              .append("features", features)
              .append("extendedProtocol", extendedProtocol)
              .append("ioTimeout", ioTimeout)
              .toString();
    }
}
