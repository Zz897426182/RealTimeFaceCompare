package com.hzgc.ftpserver.kafka.ftp;

import com.hzgc.util.FileUtil;
import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.impl.DefaultConnectionConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Administrator on 2017-10-9.
 */
public class KafkaConnectionConfigFactory {
    private int maxLogins = 10;

    private boolean anonymousLoginEnabled = true;

    private int maxAnonymousLogins = 10;

    private int maxLoginFailures = 3;

    private int loginFailureDelay = 500;

    private int maxThreads = 0;

    /**
     * Create a connection configuration instances based on the configuration on this factory
     *
     * @return The {@link ConnectionConfig} instance
     */
    public ConnectionConfig createConnectionConfig() {
        return new DefaultConnectionConfig(anonymousLoginEnabled,
                loginFailureDelay, maxLogins, maxAnonymousLogins,
                maxLoginFailures, maxThreads);
    }

    ConnectionConfig createUDConnectionConfig() {
        FtpServerFactory factory = new FtpServerFactory();
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(FileUtil.loadResourceFile("users.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int maxLogins = Integer.parseInt(props.getProperty("admin.maxloginnumber"));
        boolean anonymousLoginEnabled = Boolean.parseBoolean(props.getProperty("anonymous.enableflag"));
        int maxAnonymousLogins = Integer.parseInt(props.getProperty("anonymous.maxloginnumber"));
        int maxLoginFailures = Integer.parseInt(props.getProperty("maxLoginFailures"));
        int loginFailureDelay = Integer.parseInt(props.getProperty("loginFailureDelay"));
        int maxThreads = Integer.parseInt(props.getProperty("maxThreads"));

        factory.setConnectionConfig(new KafkaConnectionConfig(anonymousLoginEnabled,
                loginFailureDelay, maxLogins, maxAnonymousLogins,
                maxLoginFailures, maxThreads));
        return factory.getConnectionConfig();
    }

    /**
     * The delay in number of milliseconds between login failures. Important to
     * make brute force attacks harder.
     *
     * @return The delay time in milliseconds
     */
    public int getLoginFailureDelay() {
        return loginFailureDelay;
    }

    /**
     * The maximum number of anonymous logins the server would allow at any given time
     *
     * @return The maximum number of anonymous logins
     */
    public int getMaxAnonymousLogins() {
        return maxAnonymousLogins;
    }

    /**
     * The maximum number of time an user can fail to login before getting disconnected
     *
     * @return The maximum number of failure login attempts
     */
    public int getMaxLoginFailures() {
        return maxLoginFailures;
    }

    /**
     * The maximum number of concurrently logged in users
     *
     * @return The maximum number of users
     */
    public int getMaxLogins() {
        return maxLogins;
    }

    /**
     * Is anonymous logins allowed at the server?
     *
     * @return true if anonymous logins are enabled
     */
    public boolean isAnonymousLoginEnabled() {
        return anonymousLoginEnabled;
    }

    /**
     * Set she maximum number of concurrently logged in users
     *
     * @param maxLogins The maximum number of users
     */

    public void setMaxLogins(final int maxLogins) {
        this.maxLogins = maxLogins;
    }

    /**
     * Returns the maximum number of threads the server is allowed to create for
     * processing client requests.
     *
     * @return the maximum number of threads the server is allowed to create for
     * processing client requests.
     */
    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * Sets the maximum number of threads the server is allowed to create for
     * processing client requests.
     *
     * @param maxThreads the maximum number of threads the server is allowed to create
     *                   for processing client requests.
     */
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    /**
     * Set if anonymous logins are allowed at the server
     *
     * @param anonymousLoginEnabled true if anonymous logins should be enabled
     */
    public void setAnonymousLoginEnabled(final boolean anonymousLoginEnabled) {
        this.anonymousLoginEnabled = anonymousLoginEnabled;
    }

    /**
     * Sets the maximum number of anonymous logins the server would allow at any given time
     *
     * @param maxAnonymousLogins The maximum number of anonymous logins
     */
    public void setMaxAnonymousLogins(final int maxAnonymousLogins) {
        this.maxAnonymousLogins = maxAnonymousLogins;
    }

    /**
     * Set the maximum number of time an user can fail to login before getting disconnected
     *
     * @param maxLoginFailures The maximum number of failure login attempts
     */
    public void setMaxLoginFailures(final int maxLoginFailures) {
        this.maxLoginFailures = maxLoginFailures;
    }

    /**
     * Set the delay in number of milliseconds between login failures. Important to
     * make brute force attacks harder.
     *
     * @param loginFailureDelay The delay time in milliseconds
     */
    public void setLoginFailureDelay(final int loginFailureDelay) {
        this.loginFailureDelay = loginFailureDelay;
    }
}
