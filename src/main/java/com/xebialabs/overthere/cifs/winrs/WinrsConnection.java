package com.xebialabs.overthere.cifs.winrs;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.spi.AddressPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.*;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENABLE_HTTPS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENABLE_HTTPS_DEFAULT;
import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.util.OverthereUtils.checkNotNull;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static java.net.InetSocketAddress.createUnresolved;

class WinrsConnection {

    private OperatingSystemFamily os;
    private OverthereFile workingDirectory;
    private String address;
    private int port;
    private String password;
    private String username;
    private CifsConnectionType connectionType = CifsConnectionType.WINRM_INTERNAL;
    private ConnectionOptions options;

    private OverthereConnection winrsProxyConnection;

    WinrsConnection(ConnectionOptions options, AddressPortMapper mapper, OverthereFile workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.options = options;
        this.os = options.getEnum(OPERATING_SYSTEM, OperatingSystemFamily.class);
        String unmappedAddress = options.get(ADDRESS);
        int unmappedPort = options.get(PORT, connectionType.getDefaultPort(options));
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));
        this.address = addressPort.getHostName();
        this.port = addressPort.getPort();
        this.username = options.get(USERNAME);
        this.password = options.get(PASSWORD);
    }

    OverthereConnection getWinrsProxyConnection() {
        return this.winrsProxyConnection;
    }

    OverthereConnection connectToWinrsProxy(ConnectionOptions options) {
        logger.debug("Connecting to winrs proxy");

        String winrsProxyProtocol = options.get(WINRS_PROXY_PROTOCOL, WINRS_PROXY_PROTOCOL_DEFAULT);
        ConnectionOptions winrsProxyConnectionOptions = options.get(WINRS_PROXY_CONNECTION_OPTIONS, new ConnectionOptions());
        winrsProxyConnection = Overthere.getConnection(winrsProxyProtocol, winrsProxyConnectionOptions);
        return winrsProxyConnection;
    }

    void disconnectFromWinrsProxy() {
        logger.debug("Disconnecting from winrs proxy");

        closeQuietly(winrsProxyConnection);
    }

    OverthereProcess startProcess(final CmdLine cmd) {
        checkNotNull(cmd, "Cannot execute null command line");
        checkArgument(cmd.getArguments().size() > 0, "Cannot execute empty command line");

        final String obfuscatedCmd = cmd.toCommandLine(os, true);
        logger.info("Starting command [{}] on [{}]", obfuscatedCmd, this);

        final CmdLine winrsCmd = new CmdLine();
        winrsCmd.addArgument("winrs");
        winrsCmd.addArgument("-remote:" + address + ":" + port);
        winrsCmd.addArgument("-username:" + username);
        winrsCmd.addPassword("-password:" + password);
        if (workingDirectory != null) {
            winrsCmd.addArgument("-directory:" + workingDirectory.getPath());
        }
        if (options.getBoolean(WINRS_NOECHO, WINRS_NOECHO_DEFAULT)) {
            winrsCmd.addArgument("-noecho");
        }
        if (options.getBoolean(WINRS_NOPROFILE, WINRS_NOPROFILE_DEFAULT)) {
            winrsCmd.addArgument("-noprofile");
        }
        if (options.getBoolean(WINRS_ALLOW_DELEGATE, DEFAULT_WINRS_ALLOW_DELEGATE)) {
            winrsCmd.addArgument("-allowdelegate");
        }
        if (options.getBoolean(WINRS_COMPRESSION, WINRS_COMPRESSION_DEFAULT)) {
            winrsCmd.addArgument("-compression");
        }
        if (options.getBoolean(WINRS_UNENCRYPTED, WINRS_UNENCRYPTED_DEFAULT)) {
            winrsCmd.addArgument("-unencrypted");
        }
        if (options.getBoolean(WINRM_ENABLE_HTTPS, WINRM_ENABLE_HTTPS_DEFAULT)) {
            winrsCmd.addArgument("-usessl");
        }
        winrsCmd.add(cmd.getArguments());

        return winrsProxyConnection.startProcess(winrsCmd);
    }

    private static final Logger logger = LoggerFactory.getLogger(WinrsConnection.class);
}
