package Server.model.auction.items;

import java.time.LocalDateTime;

public class Art extends Item {
    private String artist;
    private String material;
    private String certificate; //giấy chứng nhận hàng thật
    public Art(String id, String name, String description, double startingPrice, LocalDateTime
            endTime, String artist, String material, String certificate) {
        super(id, name, description, startingPrice, endTime);
        this.artist = artist;
        this.material = material;
        this.certificate = certificate;
    }
}