package slashCommands;

public class Config {

    private String token;
    private String sqliteDatabase;
    private String minPerms;
    private long guildId;
    private String user;
    private String password;

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getSqliteDatabase() {
        return sqliteDatabase;
    }
    public void setSqliteDatabase(String sqliteDatabase) {
        this.sqliteDatabase = sqliteDatabase;
    }
    public String getMinPerms() {
        return minPerms;
    }
    public void setMinPerms(String minperms) {
        this.minPerms = minperms;
    }
    public long getGuildId() {
        return guildId;
    }
    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
