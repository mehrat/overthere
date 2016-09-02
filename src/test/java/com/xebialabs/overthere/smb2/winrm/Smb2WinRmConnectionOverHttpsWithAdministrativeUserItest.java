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

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.cifs.WinrmHttpsCertificateTrustStrategy;
import com.xebialabs.overthere.cifs.WinrmHttpsHostnameVerificationStrategy;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.xebialabs.overthere.WindowsCloudHostListener;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.WindowsCloudHostListener.ADMINISTRATIVE_WINDOWS_USER_PASSWORD;
import static com.xebialabs.overthere.WindowsCloudHostListener.ADMINISTRATIVE_WINDOWS_USER_USERNAME;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.cifs.ConnectionBuilder.*;
import static com.xebialabs.overthere.smb2.Smb2ConnectionBuilder.SMB2_PROTOCOL;

@Test
@Listeners({WindowsCloudHostListener.class})
public class Smb2WinRmConnectionOverHttpsWithAdministrativeUserItest extends OverthereConnectionItestBase {

    @Override
    protected String getProtocol() {
        return SMB2_PROTOCOL;
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, WINRM_INTERNAL);
        options.set(ADDRESS, WindowsCloudHostListener.getHost().getHostName());
        options.set(USERNAME, ADMINISTRATIVE_WINDOWS_USER_USERNAME);
        options.set(PASSWORD, ADMINISTRATIVE_WINDOWS_USER_PASSWORD);
        options.set(WINRM_ENABLE_HTTPS, true);
        options.set(WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY, WinrmHttpsCertificateTrustStrategy.ALLOW_ALL);
        options.set(WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY, WinrmHttpsHostnameVerificationStrategy.ALLOW_ALL);
        return options;
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return Smb2WinRmConnection.class.getName();
    }
}
