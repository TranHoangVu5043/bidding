package Server.model.auction.items;

public class Art extends Item {
    private String artist;
    private String material;
    private String certificate; //giấy chứng nhận hàng thật

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public Art(String id, String name, String description, int ownerId, String category, String condition,
               String artist, String material, String certificate) {
        super(id, name, description, ownerId, category, condition);
        this.artist = artist;
        this.material = material;
        this.certificate = certificate;
    }
}