package tk.bolovsrol.utils;

import tk.bolovsrol.utils.reflectiondump.ReflectionDump;

/** Хост и порт. */
public class HostPort {
    private String hostname;
    private Integer port;

    public HostPort(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public boolean hasHostname() {
        return hostname != null;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setPortIfAbsent(Integer defaultPort) {
        if (this.port == null) {
            this.port = defaultPort;
        }
    }

    public int getPortIntValue(int defaultPort) {
        return port == null ? defaultPort : port;
    }

    @Override public String toString() {
        return ReflectionDump.getFor(this);
    }

    public static HostPort parse(String hostAndMaybePort, Integer defaultPort) {
        if (hostAndMaybePort == null) {
            return null;
        }
        int colPos = hostAndMaybePort.indexOf(':');
        if (colPos < 0) {
            return new HostPort(hostAndMaybePort, defaultPort);
        } else if (colPos == 0) {
            return new HostPort(null, Integer.parseInt(hostAndMaybePort.substring(1)));
        } else {
            return new HostPort(hostAndMaybePort.substring(0, colPos), Integer.parseInt(hostAndMaybePort.substring(colPos + 1)));
        }

    }

    public static HostPort parse(String hostAndMaybePort) {
        return parse(hostAndMaybePort, null);
    }

    public static HostPort[] parse(String[] hostsAndMaybePorts, Integer defaultPort) {
        HostPort[] hostPorts = new HostPort[hostsAndMaybePorts.length];
        for (int i = 0, hostsAndMaybePortsLength = hostsAndMaybePorts.length; i < hostsAndMaybePortsLength; i++) {
            hostPorts[i] = parse(hostsAndMaybePorts[i], defaultPort);
        }
        return hostPorts;
    }

    public static HostPort[] parse(String... hostsAndMaybePorts) {
        return parse(hostsAndMaybePorts, null);
    }

    public static void setPortIfAbsent(HostPort[] hostPorts, Integer defaultPort) {
        for (HostPort hostPort : hostPorts) {
            hostPort.setPortIfAbsent(defaultPort);
        }
    }

}
