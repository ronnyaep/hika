package fr.WarzouMc.hikabrain.manager;

public class PlayerStats {
    private int wins;
    private int losses;
    private int kills;
    private int deaths;
    private int points;

    public PlayerStats() {
        this(0, 0, 0, 0, 0);
    }

    public PlayerStats(int wins, int losses, int kills, int deaths, int points) {
        this.wins = wins;
        this.losses = losses;
        this.kills = kills;
        this.deaths = deaths;
        this.points = points;
    }

    public void addWin() { wins++; }
    public void addLoss() { losses++; }
    public void addKills(int amount) { kills += amount; }
    public void addDeaths(int amount) { deaths += amount; }
    public void addPoints(int amount) { points += amount; }

    // Getters
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getPoints() { return points; }
}