package me.bronsk.aethercore.database.storage.config;

public class AEDatabase {

    private String name = "torvexcore.db";
    private String username = "root";
    private String password = "root";
    private String address = "localhost:3306";
    private int maxConnectionsFree = 16;
    private long maxConnectionAgeMillis = 300_000L;
    private long checkConnectionsEveryMillis = 60_000L;
    private boolean testBeforeGet = true;

    /**
     * Returns getDatabase.
     *
     * @return Method result.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets setDatabase.
     *
     * @param name Method argument.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns getUsername.
     *
     * @return Method result.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets setUsername.
     *
     * @param username Method argument.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns getPassword.
     *
     * @return Method result.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets setPassword.
     *
     * @param password Method argument.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns getAddress.
     *
     * @return Method result.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets setAddress.
     *
     * @param address Method argument.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns getMaxConnectionsFree.
     *
     * @return Method result.
     */
    public int getMaxConnectionsFree() {
        return maxConnectionsFree;
    }

    /**
     * Sets setMaxConnectionsFree.
     *
     * @param maxConnectionsFree Method argument.
     */
    public void setMaxConnectionsFree(int maxConnectionsFree) {
        this.maxConnectionsFree = maxConnectionsFree;
    }

    /**
     * Returns getMaxConnectionAgeMillis.
     *
     * @return Method result.
     */
    public long getMaxConnectionAgeMillis() {
        return maxConnectionAgeMillis;
    }

    /**
     * Sets setMaxConnectionAgeMillis.
     *
     * @param maxConnectionAgeMillis Method argument.
     */
    public void setMaxConnectionAgeMillis(long maxConnectionAgeMillis) {
        this.maxConnectionAgeMillis = maxConnectionAgeMillis;
    }

    /**
     * Returns getCheckConnectionsEveryMillis.
     *
     * @return Method result.
     */
    public long getCheckConnectionsEveryMillis() {
        return checkConnectionsEveryMillis;
    }

    /**
     * Sets setCheckConnectionsEveryMillis.
     *
     * @param checkConnectionsEveryMillis Method argument.
     */
    public void setCheckConnectionsEveryMillis(long checkConnectionsEveryMillis) {
        this.checkConnectionsEveryMillis = checkConnectionsEveryMillis;
    }

    /**
     * Returns isTestBeforeGet.
     *
     * @return Method result.
     */
    public boolean isTestBeforeGet() {
        return testBeforeGet;
    }

    /**
     * Sets setTestBeforeGet.
     *
     * @param testBeforeGet Method argument.
     */
    public void setTestBeforeGet(boolean testBeforeGet) {
        this.testBeforeGet = testBeforeGet;
    }
}
