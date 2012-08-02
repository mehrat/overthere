/**
 * Copyright (c) 2008, 2012, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.cifs.winrm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_ENVELOP_SIZE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_LOCALE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_TIMEOUT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.ENVELOP_SIZE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.LOCALE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.TIMEMOUT;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTP;

import java.net.MalformedURLException;
import java.net.URL;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.cifs.CifsConnection;
import com.xebialabs.overthere.cifs.winrm.connector.ApacheHttpComponentsHttpClientHttpConnector;
import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;

/**
 * A connection to a Windows host using CIFS and WinRM.
 * 
 * Limitations:
 * <ul>
 * <li>Shares with names like C$ need to available for all drives accessed. In practice, this means that Administrator
 * access is needed.</li>
 * <li>Can only authenticate with basic authentication to WinRM</li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public class CifsWinRmConnection extends CifsConnection {

    private final WinRmClient winRmClient;

    /**
     * Creates a {@link CifsWinRmConnection}. Don't invoke directly. Use
     * {@link Overthere#getConnection(String, ConnectionOptions)} instead.
     */
    public CifsWinRmConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper, false);
        checkArgument(os == WINDOWS, "Cannot start a " + CIFS_PROTOCOL + ":%s connection to a non-Windows operating system", cifsConnectionType.toString()
            .toLowerCase());

        URL targetURL = getTargetURL(options);
        HttpConnector httpConnector = new ApacheHttpComponentsHttpClientHttpConnector(targetURL, options);

        winRmClient = new WinRmClient(httpConnector, targetURL);
        winRmClient.setTimeout(options.get(TIMEMOUT, DEFAULT_TIMEOUT));
        winRmClient.setEnvelopSize(options.get(ENVELOP_SIZE, DEFAULT_ENVELOP_SIZE));
        winRmClient.setLocale(options.get(LOCALE, DEFAULT_LOCALE));
    }

    private URL getTargetURL(ConnectionOptions options) {
        String scheme = cifsConnectionType == WINRM_HTTP ? "http" : "https";
        String context = options.get(CONTEXT, DEFAULT_WINRM_CONTEXT);
        try {
            return new URL(scheme, address, port, context);
        } catch (MalformedURLException e) {
            throw new WinRMRuntimeIOException("Cannot build a new URL for " + this, e);
        }
    }

    @Override
    public int execute(final OverthereProcessOutputHandler handler, final CmdLine commandLine) {
        String cmd = commandLine.toCommandLine(getHostOperatingSystem(), false);
        if (workingDirectory != null) {
            cmd = "CD " + workingDirectory.getPath() + " & " + cmd;
        }
        winRmClient.runCmd(cmd, handler);
        return winRmClient.getExitCode();
    }

}
