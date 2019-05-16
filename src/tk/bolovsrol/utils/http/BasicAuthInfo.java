package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.Base64;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.Uri;

import java.io.UnsupportedEncodingException;

/** Контейнер с логином и паролем. */
public class BasicAuthInfo {

    public static final String BASIC_PREFIX = "Basic ";
    private static final String AUTHORIZATION = "Authorization";

    private final String login;
    private final String password;
    private String compiled = null;

    public BasicAuthInfo(String login, String password) {
        this.login = login;
        this.password = password;
    }

    private BasicAuthInfo(String login, String password, String compiled) {
        this.login = login;
        this.password = password;
        this.compiled = compiled;
    }

    @Override
    public String toString() {
        return new StringDumpBuilder()
                .append("login", login)
                .append("password", password)
                .append("compiled", getCompiled())
                .toString();
    }

    public boolean hasLogin() {
        return login != null;
    }

    public String getLogin() {
        return login;
    }

    public boolean hasPassword() {
        return password != null;
    }

    public String getPassword() {
        return password;
    }

    public String getCompiled() {
        if (compiled == null) {
            StringBuilder sb = new StringBuilder(32);
            sb.append(login);
            if (password != null) {
                sb.append(':');
                sb.append(password);
            }
            try {
                compiled = BASIC_PREFIX + Base64.byteArrayToBase64(sb.toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // такого не должно быть
                throw new RuntimeException(e);
            }
        }
        return compiled;
    }

    public void putToRequest(HttpRequest httpRequest) {
        httpRequest.headers().set(AUTHORIZATION, getCompiled());
    }

    @Override public int hashCode() {
        return 31 * (login != null ? login.hashCode() : 0) + (password != null ? password.hashCode() : 0);
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof BasicAuthInfo && equals((BasicAuthInfo) obj);
    }

    public boolean equals(BasicAuthInfo bai) {
        return StringUtils.equals(this.login, bai.login) && StringUtils.equals(this.password, bai.password);
    }

    public static BasicAuthInfo parse(HttpRequest httpRequest) {
        return parse(httpRequest.headers().get(AUTHORIZATION));
    }

    public static BasicAuthInfo parse(String kludge) {
        if (kludge == null || !kludge.startsWith(BASIC_PREFIX)) {
            return null;
        }
        String authString = kludge.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.base64ToByteArray(authString));
        int semicolonPos = decoded.indexOf((int) ':');
        if (semicolonPos == -1) {
            return new BasicAuthInfo(decoded, null, authString);
        } else {
            return new BasicAuthInfo(
                    decoded.substring(0, semicolonPos),
                    decoded.substring(semicolonPos + 1),
                    kludge
            );
        }
    }

    public static BasicAuthInfo parse(Uri url) throws UnexpectedBehaviourException {
        if (!url.hasUsername()) {
            throw new UnexpectedBehaviourException("No username specified in url " + Spell.get(url));
        }
        return new BasicAuthInfo(url.getUsername(), url.getPassword());
    }

    public static void wipeFromRequest(HttpRequest httpRequest) {
        httpRequest.headers().drop(AUTHORIZATION);
    }

    /**
     * Перемещает данные логина и пароля переданного урла
     * в переданный хттп-запрос.
     * <p/>
     * Если логина-пароля нет, то удаляет такие данные из запроса.
     *
     * @param url
     * @param httpRequest
     * @return созданую информацию об авторизации или нул, если информации нет
     */
    public static BasicAuthInfo transferToRequest(Uri url, HttpRequest httpRequest) {
        if (url.hasUsername()) {
            BasicAuthInfo basicAuthInfo = new BasicAuthInfo(url.getUsername(), url.getPassword());
            basicAuthInfo.putToRequest(httpRequest);
            return basicAuthInfo;
        } else {
            wipeFromRequest(httpRequest);
            return null;
        }
    }
}
