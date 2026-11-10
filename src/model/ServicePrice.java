package model;

public class ServicePrice implements Storable {

    private final ServiceType serviceType;
    private double amount;

    public ServicePrice(ServiceType serviceType, double amount) {
        this.serviceType = serviceType;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    @Override
    public String getId() {
        return serviceType.name();
    }

    // Turns the price into one line of text for prices.txt
    @Override
    public String toFileLine() {
        return serviceType.name() + DELIMITER + String.format("%.2f", amount);
    }
}
