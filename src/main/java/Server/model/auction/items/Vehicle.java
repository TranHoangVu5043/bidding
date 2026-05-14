package Server.model.auction.items;

public class Vehicle extends Item {
    private String manuFacturer; // hãng xe;
    private String fuelType; //loại nhiên liệu sử dụng
    private String licensePlate; //biển số xe

    public String getManuFacturer() {
        return manuFacturer;
    }

    public void setManuFacturer(String manuFacturer) {
        this.manuFacturer = manuFacturer;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Vehicle(int id, String name, String description, int ownerId, String category, String condition,
                   String manuFacturer, String fuelType, String licensePlate){
        super(id, name, description, ownerId, category, condition);
        this.manuFacturer = manuFacturer;
        this.fuelType = fuelType;
        this.licensePlate = licensePlate;
    }

    @Override
    public void displayInfo() {
        System.out.println("[Vehicle] " + getName()
                + " | Maker: " + manuFacturer
                + " | Fuel: " + fuelType
                + " | Plate: " + licensePlate);
    }
}
