package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.Uri;

/**
 * Простейшая авторизация по заданным логину и паролю.
 * <p/>
 * Если логин не указан (null), то авторизация не проверяется,
 * одобряются все подряд запросы.
 */
public class StaticBasicHttpAuthorization implements HttpAuthorization {

    final BasicAuthInfo goodAuthInfo;

    public StaticBasicHttpAuthorization(Uri uri) {
        this(uri.getUsername(), uri.getPassword());
    }

    public StaticBasicHttpAuthorization(String login, String password) {
        goodAuthInfo = new BasicAuthInfo(login, password);
    }

    @Override public void checkAuthorization(HttpRequest httpRequest)
            throws AuthorizationMissingException, UnknownLoginException, InvalidPasswordException {
        if (goodAuthInfo.hasLogin()) {
            BasicAuthInfo requestAuthInfo = BasicAuthInfo.parse(httpRequest);
            if (requestAuthInfo == null) {
                throw new AuthorizationMissingException(getWwwAuthenticate());
            }
            if (!goodAuthInfo.getLogin().equals(requestAuthInfo.getLogin())) {
                throw new UnknownLoginException("Provided Basic Authorizaion: " + Spell.get(requestAuthInfo)
                        + ", expected " + Spell.get(goodAuthInfo), getWwwAuthenticate());
            }
            if (!goodAuthInfo.getPassword().equals(requestAuthInfo.getPassword())) {
                throw new InvalidPasswordException("Provided Basic Authorizaion: " + Spell.get(requestAuthInfo)
                        + ", expected " + Spell.get(goodAuthInfo), getWwwAuthenticate());
            }
        } else {
            throw new UnknownLoginException("No certain login expected, no way client could pass this authorization. It's a trap.", getWwwAuthenticate());
        }
    }

    @Override public String getWwwAuthenticate() {
        return "Basic realm=\"PlasticMedia\"";
    }

    public static StaticBasicHttpAuthorization newOrNull(Uri uri) {
        return newOrNull(uri.getUsername(), uri.getPassword());
    }

    public static StaticBasicHttpAuthorization newOrNull(String loginOrNull, String password) {
        if (loginOrNull == null) {
            return null;
        } else {
            return new StaticBasicHttpAuthorization(loginOrNull, password);
        }
    }


}
