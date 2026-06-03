# Bidding System — Hệ Thống Đấu Giá Trực Tuyến

## Mô Tả Hệ Thống

Hệ thống đấu giá trực tuyến theo mô hình Client-Server, cho phép nhiều người dùng cùng cạnh tranh đặt giá để mua sản phẩm trong một khoảng thời gian xác định. Hệ thống hỗ trợ ba vai trò: **Admin**, **Seller** (người bán) và **Bidder** (người đặt giá), với giao diện đồ họa xây dựng bằng JavaFX và cập nhật giá thời gian thực qua WebSocket.

---

## Công Nghệ Sử Dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 25 (JDK 25) |
| Giao diện | JavaFX 25 + FXML |
| Giao tiếp | HTTP REST API + WebSocket |
| Kiến trúc | Client-Server (MVC phía Client, Controller-Service-DAO phía Server) |
| Database | PostgreSQL (hosted trên Supabase) |
| Build tool | Maven 3.9+ |
| Serialization | Gson 2.10.1 |
| WebSocket | Java-WebSocket 1.5.4 |
| Connection Pool | HikariCP 5.1.0 |
| Bảo mật | BCrypt (at.favre.lib 0.10.2) |
| Logging | SLF4J Simple 2.0.13 |
| Testing | JUnit 5.14, Mockito 5.15.2, H2 (in-memory) |
| Deploy | Docker + Railway |

### Yêu Cầu Cài Đặt

- **Java JDK 25** (bắt buộc — project dùng `--release 25`)
  - Tải từ [Oracle JDK 25](https://www.oracle.com/java/technologies/javase/jdk25-archive-downloads.html) hoặc [Adoptium](https://adoptium.net)
- **Maven 3.9+**
- Kết nối Internet (để kết nối database Supabase)

### Khả năng chạy trên các Hệ Điều Hành

Dự án **hỗ trợ đầy đủ trên Windows, Linux và macOS**. Không có code platform-specific, JavaFX Maven tự động tải driver phù hợp.

#### Linux (Ubuntu/Debian)

Cần cài đặt thêm các thư viện GTK + OpenGL

Khi chạy client nếu có lỗi encoding tiếng Việt, thêm flag:
```bash
cd client
mvn javafx:run -Dfile.encoding=UTF-8
```

####  Windows

Chạy bình thường, không cần cài thêm gì.

---

## Cấu Trúc Thư Mục

```
bidding/
├ server/                               # Maven module — HTTP REST server
│   ├ pom.xml
│   └ src/
│       ├ main/
│       │   ├ java/Server/
│       │   │   ├ controller/           # REST route handlers
│       │   │   ├ dao/                  # Database access layer (JDBC)
│       │   │   ├ dto/                  # Request / Response DTOs
│       │   │   ├ exception/            # Custom exceptions
│       │   │   ├ filters/              # sessionFilter (auth middleware)
│       │   │   ├ model/                # Domain entities (User, Item, Auction, Bid)
│       │   │   ├ networking/           # ServerConnection, ApiRouter, HikariCP
│       │   │   ├ service/              # Business logic services
│       │   │   ├ websocket/            # BidWebSocketServer (realtime broadcast)
│       │   │   └ ServerApp.java        # Server entry point
│       │   └ resources/
│       │       └ simplelogger.properties
│       └ test/java/Server/             # DAO, Service, Controller tests
├ client/                               # Maven module — JavaFX desktop client
│   ├ pom.xml
│   └ src/
│       ├ main/java/Client/
│       │   ├ controller/               # MVC controllers (admin, auth, seller, user)
│       │   ├ model/                    # Client-side data models
│       │   ├ networking/               # HTTP ApiClient + API endpoint classes
│       │   ├ util/                     # SceneUtil, DialogUtil
│       │   ├ views/                    # FXML layouts + CSS
│       │   ├ websocket/                # AuctionWebSocketClient (realtime)
│       │   └ ClientApp.java            # JavaFX entry point
│       └ test/java/Client/
├ Dockerfile                            # Multi-stage Docker build (server only)
├ railway.toml                          # Railway deployment config
├ pom.xml                               # Parent POM (aggregator)
└ README.md
```

---

## Hướng Dẫn Chạy Server / Client

> **Lưu ý:** Luôn khởi động **Server trước**, sau đó mới khởi động **Client**.  
> Để chạy nhiều client cùng lúc, mở nhiều terminal và lặp lại Bước 3.

---

###  Tuỳ chọn kết nối Server

Server đã được **deploy sẵn trên Railway** — có thể chạy Client mà không cần khởi động Server cục bộ.

| Chế độ | HTTP | WebSocket |
|---|---|---|
| **Railway (mặc định)** | `https://bidding-production-3e9a.up.railway.app` | `ws://roundhouse.proxy.rlwy.net:43153` |
| **Local** | `http://localhost:8080` | `ws://localhost:8081` |

Client mặc định kết nối đến **Local**. Để kết nối Railway, chỉnh trực tiếp trong `client/src/main/java/Client/networking/ServerConfig.java`:

```java
// Đổi từ LOCAL_HTTP/LOCAL_WS sang RAILWAY_HTTP/RAILWAY_WS
public static final String HTTP_BASE = RAILWAY_HTTP;    // thay vì LOCAL_HTTP
public static final String WS_URL    = RAILWAY_WS;      // thay vì LOCAL_WS
```

---

### Bước 1 — Clone và build project

```bash
git clone https://github.com/TranHoangVu5043/bidding.git
cd bidding
mvn clean package -DskipTests
```

### Bước 2 — Khởi động Server (bỏ qua nếu dùng Railway)

```bash
cd server
mvn exec:java -Dexec.mainClass="Server.ServerApp"
```

Server khởi động thành công khi console hiển thị:
```
[DB] HikariCP pool started
Server started
HTTP server listening on port 8080
WebSocket server listening on port 8081
```

### Bước 3 — Khởi động Client

Mở một terminal **mới** (giữ nguyên terminal Server đang chạy):

```bash
cd client
mvn javafx:run
```

Giao diện JavaFX sẽ hiển thị. Đăng ký tài khoản mới hoặc đăng nhập để sử dụng.

> **Chạy nhiều client:** Mở thêm terminal mới và lặp lại lệnh `cd client && mvn javafx:run`.

### Chạy Tests

```bash
# Tất cả tests (server + client)
mvn test

# Chỉ server tests
cd server && mvn test
```

---

## Danh Sách Chức Năng Đã Hoàn Thành

### Chức năng bắt buộc

**Quản lý người dùng**
- [x] Đăng ký / đăng nhập tài khoản (BCrypt password hashing)
- [x] Ba vai trò: Bidder, Seller, Admin — mỗi vai trò có giao diện và quyền hạn riêng
- [x] Quản lý phiên đăng nhập với session token (`sessionFilter`)

**Quản lý sản phẩm đấu giá**
- [x] Thêm / sửa / xóa sản phẩm (`ItemApiController`)
- [x] Ba loại sản phẩm: Art, Electronics, Vehicle (kế thừa `Item` — Factory Pattern)
- [x] Thông tin đầy đủ: tên, mô tả, giá khởi điểm, giá hiện tại, thời gian

**Tham gia đấu giá**
- [x] Đặt giá cao hơn giá hiện tại (`BiddingService`)
- [x] Kiểm tra tính hợp lệ của giá đấu
- [x] Cập nhật người dẫn đầu phiên đấu giá

**Kết thúc phiên đấu giá**
- [x] Tự động đóng phiên khi hết thời gian (`AuctionExpiryScheduler`)
- [x] Xác định người thắng cuộc
- [x] Chuyển trạng thái: `OPEN → RUNNING → FINISHED`

**Xử lý lỗi & ngoại lệ**
- [x] Đặt giá thấp hơn giá hiện tại → báo lỗi rõ ràng
- [x] Đấu giá khi phiên đã đóng → từ chối với thông báo
- [x] Custom exception hierarchy: `AuthException`, `ValidationException`, `ConflictException`

**Giao diện (GUI)**
- [x] JavaFX + FXML: Login, Register, UserView, SellerView, AuctionDetailView, AdminView
- [x] Màn hình đấu giá trực tiếp với realtime update
- [x] Quản lý sản phẩm và phiên đấu giá cho Seller

### Chức năng nâng cao

- [x] **Auto-Bidding:** Người dùng đặt `maxBid` + `increment`, hệ thống tự động trả giá khi có bid mới từ đối thủ — ưu tiên theo thời điểm đăng ký, không vượt `maxBid` (`AutoBidConfigService`)
- [x] **Anti-sniping:** Nếu có bid trong 2 phút cuối → tự động gia hạn thêm 2 phút (`BiddingService`)
- [x] **Bid History Visualization:** Biểu đồ đường (LineChart) giá đấu theo thời gian, tự động cập nhật khi có bid mới (`BidHistoryDialog`)

### Kỹ thuật & kiến trúc

- [x] **Concurrent Bidding an toàn:** Transaction-level locking qua HikariCP + PostgreSQL — tránh lost update, race condition, hai người cùng thắng
- [x] **Realtime update:** WebSocket Observer Pattern — toàn bộ client đang xem phiên nhận thông báo ngay lập tức, không polling (`BidWebSocketServer`)
- [x] **Design Patterns:** Singleton (`ServerConnection`, `BidWebSocketServer`), Factory Method (`ItemFactory`, `UserFactory`), Observer (WebSocket), DAO, MVC, Strategy (bid types)
- [x] **OOP đầy đủ:** Encapsulation, Inheritance (`User → Admin/Bidder/Seller`, `Item → Art/Electronics/Vehicle`), Polymorphism, Abstraction (`Entity` interface, abstract classes)
- [x] **Unit Tests:** JUnit 5 + Mockito — DAO, Service, Controller layers (`AuctionServiceTest`, `BiddingServiceTest`, `AutoBidConfigServiceTest`, v.v.)
- [x] **CI/CD:** GitHub Actions + JUnit tự động
- [x] **Deploy:** Docker multi-stage build → Railway (HTTP port 8080, WebSocket port 8081 qua TCP proxy)

---

## Liên Kết

- Báo cáo PDF: https://drive.google.com/drive/folders/16cftHFUyfJl7-3pUtZXcW43sfaiBrmI0?usp=drive_link
- Video demo: https://drive.google.com/drive/folders/1t3Sj_45-YMpP5J1TCMBcTH0JYpKzMD_t?fbclid=IwY2xjawSNOTRleHRuA2FlbQIxMABicmlkETFOZk9RRVlOT3NoS05iTkJOc3J0YwZhcHBfaWQQMjIyMDM5MTc4ODIwMDg5MgABHufNYSSN04o0PDWs8OLx-XcL9q1fyey8OVv33flPWZktRQqoaRjlGhQMHBf-_aem_2vjxx5jJx9Yv0S_6hWkeKg