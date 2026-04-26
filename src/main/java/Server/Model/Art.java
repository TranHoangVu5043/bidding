package Server.Model;

import java.time.LocalDateTime;

public class Art extends Item {
    private String artist;
    private String material;
    private String creationDate; //ngày hoàn thành
    private String certificate; //giấy chứng nhận hàng thật
    private double length;  //chiều dài
    private double width; //chiều rộng
    public Art(String id, String name, String description, double startingPrice, double currentPrice, LocalDateTime
            startTime, LocalDateTime endTime, String sellerId, String idHighestBidder, String status, String artist, String material, String creationDate,
               String certificate, double length, double width) {
        super(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, idHighestBidder, status);
        this.artist = artist;
        this.material = material;
        this.creationDate = creationDate;
        this.certificate = certificate;
        this.length = length;
        this.width = width;
    }
}