package tk.bolovsrol.utils.threads;

/**
 * Тред, периодически выполняющий какую-нибудь работу, который можно пнуть, тем самым разбудив.
 */
public interface Pokeable {

    /** Пнуть тред, чтобы тот сразу занялся работой. */
    void poke();

    Pokeable VOID = () -> {
    };

}
