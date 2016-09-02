/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.smb2.winrm;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.cifs.ConnectionValidator;
import com.xebialabs.overthere.cifs.winrm.WinRmConnection;
import com.xebialabs.overthere.smb2.Smb2Connection;
import com.xebialabs.overthere.spi.AddressPortMapper;

import static com.xebialabs.overthere.smb2.Smb2ConnectionBuilder.SMB2_PROTOCOL;

/**
 * A connection to a Windows host using SMB2 and a Java implementation of WinRM.
 */
public class Smb2WinRmConnection extends Smb2Connection {

    /**
     * Creates a {@link Smb2WinRmConnection}. Don't invoke directly. Use
     * {@link Overthere#getConnection(String, ConnectionOptions)} instead.
     */
    public Smb2WinRmConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper, true);
        ConnectionValidator.assertIsWindowsHost(os, SMB2_PROTOCOL, cifsConnectionType);
        ConnectionValidator.assertNotOldStyleWindowsDomain(username, SMB2_PROTOCOL, cifsConnectionType);
    }

    @Override
    public void connect() {
        super.connect();
        connected();
    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        WinRmConnection connection = new WinRmConnection(options, mapper, workingDirectory);
        return connection.startProcess(cmd);
    }

}
