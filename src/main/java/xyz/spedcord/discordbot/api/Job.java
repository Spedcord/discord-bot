package xyz.spedcord.discordbot.api;

public class Job {

    private int id;
    private long startedAt;
    private long endedAt;
    private double cargoWeight;
    private double pay;
    private String fromCity;
    private String toCity;
    private String cargo;
    private String truck;

    public Job(int id, long startedAt, long endedAt, double cargoWeight, double pay, String fromCity, String toCity, String cargo, String truck) {
        this.id = id;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.cargoWeight = cargoWeight;
        this.pay = pay;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.cargo = cargo;
        this.truck = truck;
    }

    public int getId() {
        return id;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getEndedAt() {
        return endedAt;
    }

    public double getCargoWeight() {
        return cargoWeight;
    }

    public double getPay() {
        return pay;
    }

    public String getFromCity() {
        return fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    public String getCargo() {
        return cargo;
    }

    public String getTruck() {
        return truck;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public void setEndedAt(long endedAt) {
        this.endedAt = endedAt;
    }

    public void setPay(double pay) {
        this.pay = pay;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    public void setId(int id) {
        this.id = id;
    }

}
