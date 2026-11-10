package storage;

import model.ServicePrice;
import model.ServiceType;

public class PriceFileManager extends FileManager<ServicePrice> {

    public PriceFileManager() {
        super("data/prices.txt");
    }

    // serviceType|amount
    @Override
    protected ServicePrice parseLine(String line) {
        String[] parts = split(line);
        return new ServicePrice(ServiceType.valueOf(parts[0]), Double.parseDouble(parts[1]));
    }

    // Falls back to a default price if the file has no entry yet
    public ServicePrice getPrice(ServiceType type) {
        ServicePrice price = findById(type.name());
        if (price == null) {
            double defaultAmount = 250.0;
            if (type == ServiceType.NORMAL) {
                defaultAmount = 80.0;
            }
            price = new ServicePrice(type, defaultAmount);
            add(price);
        }
        return price;
    }
}
