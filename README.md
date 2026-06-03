# Bidding System - Hệ Thống Đấu Giá Trực Tuyến

## Mô Tả Hệ Thống

Hệ thống đấu giá trực tuyến theo mô hình Client-Server, cho phép người dùng đăng ký, đăng nhập, tạo phiên đấu giá và đặt giá thầu theo thời gian thực thông qua WebSocket. Hệ thống hỗ trợ ba vai trò: **Admin**, **Seller** (người bán) và **Bidder** (người đặt giá). Giao diện người dùng được xây dựng bằng JavaFX.

---

## Công Nghệ Sử Dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 25 (JDK 25) |
| Giao diện | JavaFX 25 |
| Giao tiếp | HTTP REST API + WebSocket |
| Kiến trúc | Client-Server (MVC phía Client, DAO/Service phía Server) |
| Database | PostgreSQL (hosted trên Supabase) |
| Build tool | Maven |
| Serialization | Gson 2.10.1 |
| WebSocket | Java-WebSocket 1.5.4 |
| Connection Pool | HikariCP 5.1.0 |
| Bảo mật | BCrypt (at.favre.lib:bcrypt:0.10.2) |
| Logging | SLF4J 2.0.13 |
| Testing | JUnit 5.14, Mockito 5.15.2 |
| Deploy | Docker + Railway |

### Yêu Cầu Cài Đặt

- Java JDK 11 trở lên (khuyến nghị JDK 25)
- Maven 3.6+
- Kết nối Internet (để kết nối Supabase/PostgreSQL)
- Hệ điều hành: Windows, Linux, macOS

---

## Cấu Trúc Thư Mục

```
bidding/
├── supabase/                        # Cấu hình database Supabase
│   ├── config.toml
│   └── .gitignore
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── Client/
│   │       │   ├── controller/
│   │       │   │   ├── admin/
│   │       │   │   │   └── AdminDashboardController
│   │       │   │   ├── auth/
│   │       │   │   │   ├── LoginController
│   │       │   │   │   └── RegisterController
│   │       │   │   ├── seller/
│   │       │   │   │   └── SellerController
│   │       │   │   └── user/
│   │       │   │       ├── AuctionDetailController
│   │       │   │       └── UserController
│   │       │   ├── dto.requests/
│   │       │   ├── model/
│   │       │   │   ├── admin/
│   │       │   │   │   └── ActivityLog
│   │       │   │   ├── auction/
│   │       │   │   │   ├── Auction
│   │       │   │   │   ├── Bid
│   │       │   │   │   └── BidHistoryItem
│   │       │   │   ├── item/
│   │       │   │   │   └── Item
│   │       │   │   └── user/
│   │       │   │       └── Notification
│   │       │   ├── networking/
│   │       │   │   ├── endpoints/
│   │       │   │   │   ├── AuctionApi
│   │       │   │   │   ├── BidApi
│   │       │   │   │   ├── ItemApi
│   │       │   │   │   ├── NotificationApi
│   │       │   │   │   └── UserApi
│   │       │   │   ├── ApiClient
│   │       │   │   ├── ApiResponse
│   │       │   │   ├── ServerConfig
│   │       │   │   └── SessionManager
│   │       │   ├── util/
│   │       │   │   └── SceneUtil
│   │       │   ├── views/
│   │       │   ├── websocket/
│   │       │   │   └── AuctionWebSocketClient
│   │       │   └── ClientApp
│   │       └── Server/
│   │           ├── controller/
│   │           │   ├── responseObjects/
│   │           │   │   └── ApiResponse
│   │           │   ├── AuctionApiController
│   │           │   ├── BidApiController
│   │           │   ├── ItemApiController
│   │           │   ├── NotificationApiController
│   │           │   └── UserApiController
│   │           ├── dao/
│   │           │   ├── auction/
│   │           │   │   ├── AuctionDAO
│   │           │   │   ├── BidDAO
│   │           │   │   └── ItemDAO
│   │           │   ├── users/
│   │           │   │   └── UserDAO
│   │           │   └── NotificationDAO
│   │           ├── dto/
│   │           │   ├── requests/
│   │           │   │   ├── AuctionIdRequest
│   │           │   │   ├── AutoBidRequest
│   │           │   │   ├── CreateAuctionRequest
│   │           │   │   ├── CreateItemRequest
│   │           │   │   ├── ItemIdRequest
│   │           │   │   ├── PlaceBidRequest
│   │           │   │   ├── UpdateItemRequest
│   │           │   │   └── UserRequestDTO
│   │           │   └── responses/
│   │           │       ├── AuctionDTO
│   │           │       ├── BidDTO
│   │           │       └── BidHistoryDTO
│   │           ├── exception/
│   │           │   ├── AuthException
│   │           │   ├── ConflictException
│   │           │   └── ValidationException
│   │           ├── filters/
│   │           │   └── sessionFilter
│   │           ├── model/
│   │           │   ├── auction/
│   │           │   │   ├── items/
│   │           │   │   │   ├── Art
│   │           │   │   │   ├── Electronics
│   │           │   │   │   ├── Item
│   │           │   │   │   └── Vehicle
│   │           │   │   ├── Auction
│   │           │   │   ├── AutoBidConfig
│   │           │   │   ├── Bid
│   │           │   │   └── ItemFactory
│   │           │   └── users/
│   │           │       ├── records/
│   │           │       │   └── UserRow
│   │           │       ├── ActivityLog
│   │           │       ├── Admin
│   │           │       ├── Bidder
│   │           │       ├── Seller
│   │           │       ├── User
│   │           │       └── UserFactory
│   │           ├── networking/
│   │           ├── service/
│   │           ├── websocket/
│   │           └── ServerApp
│   └── test/
│       └── java/
│           ├── Client/
│           │   └── ClientAppTest
│           └── Server/
│               ├── controller/
│               ├── dao.auction/
│               │   ├── AuctionDAOTest
│               │   ├── BidDAOTest
│               │   └── ItemDAOTest
│               └── service.auction/
│                   ├── AuctionServiceTest
│                   ├── AutoBidConfigServiceTest
│                   ├── BiddingServiceTest
│                   └── ItemServiceTest
├── Dockerfile
├── railway.toml
├── .dockerignore
├── pom.xml
└── README.md
```

---

## Các Lệnh Dòng Lệnh Để Chạy Chương Trình

> **Lưu ý:** Kiểm tra máy đã cài đặt Java JDK 11+ và Maven 3.6+ trước khi chạy. Các lệnh dưới đây tương thích với **Windows, Linux và macOS**.

### Build project

```bash
# Windows / Linux / macOS
mvn clean install
```

### Chạy Server

```bash
# Windows / Linux / macOS
mvn exec:java -Dexec.mainClass="Server.ServerApp"
```

### Chạy Client

```bash
# Windows / Linux / macOS
mvn exec:java -Dexec.mainClass="Client.ClientApp"
```

### Chạy Tests

```bash
mvn test
```

### Build Docker image (deploy)

```bash
docker build -t bidding-server .
docker run -p 8080:8080 bidding-server
```

---

## Hướng Dẫn Chạy Máy Chủ / Máy Khách

### Bước 1: Cấu hình Database

Hệ thống sử dụng **PostgreSQL** hosted trên **Supabase**. Đảm bảo file cấu hình kết nối database đã được thiết lập đúng trong `ServerConfig` (host, port, username, password).

### Bước 2: Khởi động Server

1. Mở terminal/command prompt.
2. Di chuyển vào thư mục gốc của project:
```bash
cd bidding
```
3. Build và khởi động Server:
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="Server.ServerApp"
```
4. Server khởi động thành công khi hiển thị thông báo lắng nghe cổng kết nối.

### Bước 3: Khởi động Client

1. Mở một terminal **mới** (giữ nguyên terminal Server đang chạy).
2. Di chuyển vào thư mục gốc của project.
3. Chạy Client:
```bash
mvn exec:java -Dexec.mainClass="Client.ClientApp"
```
4. Giao diện JavaFX sẽ hiển thị. Đăng ký tài khoản mới hoặc đăng nhập.

> ⚠️ **Lưu ý:** Luôn khởi động **Server trước**, sau đó mới khởi động **Client**.

---

## Danh Sách Các Chức Năng Đã Hoàn Thành

### Xác thực & Tài khoản
- [x] Đăng ký tài khoản — `RegisterController`
- [x] Đăng nhập — `LoginController`
- [x] Quản lý phiên đăng nhập — `SessionManager`, `sessionFilter`
- [x] Mã hóa mật khẩu — BCrypt

### Người dùng (Bidder)
- [x] Xem danh sách phiên đấu giá — `UserController`
- [x] Xem chi tiết phiên đấu giá — `AuctionDetailController`
- [x] Đặt giá thầu — `BidApi`, `PlaceBidRequest`
- [x] Đặt giá thầu tự động — `AutoBidRequest`, `AutoBidConfig`
- [x] Nhận thông báo thời gian thực — `AuctionWebSocketClient`, `NotificationApi`

### Người bán (Seller)
- [x] Tạo phiên đấu giá — `SellerController`, `CreateAuctionRequest`
- [x] Thêm sản phẩm đấu giá (Art, Electronics, Vehicle) — `CreateItemRequest`, `ItemFactory`
- [x] Cập nhật thông tin sản phẩm — `UpdateItemRequest`

### Quản trị viên (Admin)
- [x] Dashboard quản trị — `AdminDashboardController`
- [x] Theo dõi hoạt động hệ thống — `ActivityLog`

### Server & Kết nối
- [x] REST API đấu giá — `AuctionApiController`, `BidApiController`
- [x] REST API sản phẩm — `ItemApiController`
- [x] REST API người dùng — `UserApiController`
- [x] REST API thông báo — `NotificationApiController`
- [x] WebSocket thời gian thực — `websocket/`
- [x] Xử lý ngoại lệ — `AuthException`, `ConflictException`, `ValidationException`
- [x] Kết nối database qua HikariCP connection pool

### Kiểm thử (Testing)
- [x] Unit test DAO — `AuctionDAOTest`, `BidDAOTest`, `ItemDAOTest`
- [x] Unit test Service — `AuctionServiceTest`, `BiddingServiceTest`, `ItemServiceTest`, `AutoBidConfigServiceTest`
- [x] Integration test — `ClientAppTest`, `ServerAppTest`

### Triển khai (Deploy)
- [x] Docker hóa Server — `Dockerfile`, `.dockerignore`
- [x] Deploy lên Railway — `railway.toml`

---

## Liên Kết

- 📄 [Báo cáo PDF](#) *(cập nhật link sau khi nộp)*
- 🎥 [Video minh họa](#) *(cập nhật link sau khi nộp)*
