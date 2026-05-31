# 🏷️ AuctionSystem Group16

Hệ thống đấu giá trực tuyến realtime được xây dựng cho bài tập lớn môn **Lập trình nâng cao**.

Ứng dụng mô phỏng một sàn đấu giá online theo kiến trúc **Client-Server**, sử dụng **Java**, **JavaFX**, **FXML**, **Socket**, **Maven**, **JUnit** và **Java Serialization**. Người dùng có thể đăng ký, đăng nhập, đăng sản phẩm, tham gia đấu giá, đặt giá realtime, nạp tiền chờ admin duyệt và theo dõi lịch sử giá bằng biểu đồ.

> Trong hệ thống này, `Bidder` đồng thời đảm nhiệm vai trò `Seller`: một tài khoản người dùng thường có thể vừa tham gia đấu giá, vừa đăng sản phẩm lên sàn. `Admin` được tách riêng để quản trị, duyệt sản phẩm và duyệt yêu cầu nạp tiền.

---

## 📌 Thông tin nộp bài

- **Repository:** `https://github.com/TruongChiBach1007/AuctionSystem_Group16`
- **Nhánh nộp cuối cùng:** `main`
- **File JAR:** `release/auction-system-group16.jar`
- **Báo cáo PDF:** https://drive.google.com/file/d/1jq6-VmKx8_FJadT0niQa7UX2g_u7yyXk/view?usp=sharing
- **Video demo:** https://drive.google.com/file/d/1x162AWPka7NMA3GpYWg_KgXi6VyPs0Nf/view?usp=sharing

---

## 🧰 Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 |
| Giao diện | JavaFX 21.0.2, FXML |
| Build tool | Maven, Maven Wrapper |
| Kiến trúc | Client-Server, MVC |
| Giao tiếp mạng | Java Socket, ObjectInputStream/ObjectOutputStream |
| Lưu trữ dữ liệu | Java Serialization |
| Kiểm thử | JUnit 5 |
| CI/CD | GitHub Actions |
| Design Patterns | Singleton, Factory Method, Observer |

---

## ⚙️ Yêu cầu môi trường

Cần cài đặt:

- **JDK 21**
- Git
- Hệ điều hành Windows/Linux/macOS có hỗ trợ JavaFX runtime trong JAR đã đóng gói

Kiểm tra Java:

```bash
java -version
```

Khuyến nghị kết quả là Java 21.

---

## 📁 Cấu trúc thư mục

```text
AuctionSystem_Group16/
├── .github/workflows/          # GitHub Actions CI/CD
├── release/                    # File JAR nộp bài
│   └── auction-system-group16.jar
├── src/main/java/com/auction/
│   ├── controller/             # JavaFX Controllers
│   ├── dao/                    # DAO truy cập dữ liệu
│   ├── exceptions/             # Custom exceptions
│   ├── model/                  # User, Item, Auction, Bid...
│   ├── network/                # Socket Server/Client/Message
│   ├── pattern/                # Singleton, Factory, Observer
│   ├── security/               # Đăng nhập, xác thực
│   ├── service/                # Auto-bidding
│   ├── utils/                  # DatabaseConnection, validation
│   ├── AuctionApp.java         # JavaFX Application
│   └── Launcher.java           # Main class khi chạy JAR
├── src/main/resources/com/auction/
│   ├── *.fxml                  # Giao diện JavaFX
│   ├── css/                    # Style giao diện
│   └── images/                 # Ảnh sản phẩm mẫu
├── src/test/java/com/auction/  # JUnit tests
├── pom.xml
├── mvnw / mvnw.cmd
└── README.md
```

---

## 🚀 Cách build

Trên Windows:

```bash
.\mvnw.cmd clean test package
```

Trên Linux/macOS:

```bash
./mvnw clean test package
```

Sau khi build, file fat JAR được tạo tại:

```text
target/auction-system-group16.jar
```

File JAR nộp bài được đặt tại:

```text
release/auction-system-group16.jar
```

---

## ▶️ Cách chạy chương trình


Chạy server:

```bash
java -cp release/auction-system-group16.jar com.auction.network.AuctionServer
```

Sau đó mở client:

```bash
java -jar release/auction-system-group16.jar
```
Để demo nhiều client:

```bash
java -jar release/auction-system-group16.jar
java -jar release/auction-system-group16.jar
java -jar release/auction-system-group16.jar
```
---

## 🔐 Tài khoản mẫu

| Vai trò | Username | Password |
|---|---|---|
| Admin | `admin` | `123` |
| Bidder/Seller | `dung123` | `123` |
| Bidder/Seller | `bach123` | `123` |
| Bidder/Seller | `hminh` | `123` |

Người dùng mới cũng có thể đăng ký trực tiếp trên giao diện.

---

## ✅ Chức năng đã hoàn thành

### 👤 Quản lý người dùng

- Đăng ký tài khoản mới.
- Đăng nhập tài khoản.
- Phân quyền giữa `Admin` và người dùng thường `Bidder/Seller`.
- Quản lý danh sách người dùng trong giao diện admin.

### 📦 Quản lý sản phẩm

- Người dùng thường có thể đăng sản phẩm đấu giá.
- Sản phẩm gửi lên ở trạng thái chờ duyệt.
- Admin có thể duyệt hoặc từ chối sản phẩm.
- Hiển thị danh sách sản phẩm theo danh mục.
- Hiển thị chi tiết sản phẩm.
- Hỗ trợ các loại sản phẩm: `Electronics`, `Art`, `Vehicle`.

### 💰 Nạp tiền

- Bidder gửi yêu cầu nạp tiền.
- Admin duyệt hoặc từ chối yêu cầu nạp tiền.
- Số dư được cập nhật sau khi yêu cầu được duyệt.

### 🔨 Đấu giá realtime

- Mở phòng đấu giá cho từng sản phẩm.
- Đặt giá cao hơn giá hiện tại.
- Kiểm tra số dư trước khi đặt giá.
- Cập nhật giá mới realtime cho nhiều client.
- Hiển thị lịch sử đặt giá.
- Hiển thị biểu đồ biến động giá bằng JavaFX LineChart.
- Kết thúc phiên và xác định người thắng.
- Khi phiên đóng, giao diện sản phẩm đổi từ `Live / Đang diễn ra` sang `Đóng / Đã đóng`.

### 🧵 Xử lý đồng thời

- Server xử lý nhiều client cùng lúc bằng thread riêng cho từng kết nối.
- Dữ liệu phiên đấu giá dùng `ConcurrentHashMap`, `CopyOnWriteArrayList`.
- Logic đặt giá quan trọng được đồng bộ để tránh race condition và lost update.

### ⭐ Chức năng nâng cao

- Auto-bidding: tự động trả giá theo mức tối đa và bước giá người dùng đặt.
- Anti-sniping: gia hạn phiên nếu có bid trong những giây cuối.
- Bid History Visualization: biểu đồ giá realtime trong phòng đấu giá.

### 💾 Lưu trữ dữ liệu

- Dữ liệu user, item và yêu cầu nạp tiền được lưu/tải bằng Java Serialization.
- File dữ liệu runtime:

```text
data/auction-data.ser
```

File `.ser` được ignore khỏi Git để tránh commit dữ liệu runtime cục bộ.

### 🧪 Kiểm thử và CI/CD

- Có JUnit test cho:
  - đặt giá hợp lệ;
  - đặt giá thấp hơn hiện tại;
  - đặt giá vượt số dư;
  - phiên đã đóng;
  - auto-bidding;
  - concurrent bidding;
  - serialization.
- GitHub Actions tự động chạy:

```bash
mvn -B clean test package
```

---

## 🧱 Kiến trúc hệ thống

```text
JavaFX Client
     |
     | Socket + Object Serialization
     v
Auction Server
     |
     v
DAO / DatabaseConnection
     |
     v
Serialized Data File
```

### Mô hình chính

- `Launcher` khởi động ứng dụng.
- `AuctionApp` mở giao diện JavaFX và khởi động server nền.
- `AuctionServer` quản lý client, admin, bidder và phiên đấu giá.
- `AuctionClient` kết nối GUI client với server qua socket.
- `ClientHandler` xử lý từng kết nối client trên server.
- `AuctionMessage` là đối tượng trao đổi giữa client và server.
- `DatabaseConnection` là singleton quản lý dữ liệu và serialization.

---

## 📡 Giao tiếp Client-Server

Client và Server giao tiếp qua Java Socket bằng các object `AuctionMessage`.
Một số `MessageType` chính:

| MessageType | Ý nghĩa |
|---|---|
| `REGISTER_ADMIN` / `REGISTER_BIDDER` | Đăng ký client với server theo vai trò |
| `ITEM_REQUEST` | Bidder/Seller gửi sản phẩm chờ admin duyệt |
| `APPROVE_ITEM` / `REJECT_ITEM` | Admin duyệt hoặc từ chối sản phẩm |
| `DEPOSIT_REQUEST` | Bidder gửi yêu cầu nạp tiền |
| `APPROVE_DEPOSIT` / `REJECT_DEPOSIT` | Admin duyệt hoặc từ chối nạp tiền |
| `AUCTION_OPENED` | Mở/đồng bộ phiên đấu giá |
| `BID` | Gửi và broadcast lượt đặt giá realtime |
| `SYNC_BID_HISTORY` | Đồng bộ lịch sử đặt giá khi client vào phòng |
| `AUCTION_ENDED` / `AUCTION_STOPPED` | Kết thúc hoặc dừng phiên đấu giá |

## 🧩 Design Patterns

| Pattern | Vị trí áp dụng | Mục đích |
|---|---|---|
| Singleton | `DatabaseConnection`, `AuctionManager`, `AuthService` | Quản lý instance dùng chung |
| Factory Method | `ItemFactory` | Tạo sản phẩm theo loại |
| Observer | `AuctionObserver`, realtime socket update | Thông báo cập nhật bid/kết thúc phiên |

---

## 👥 Phân công công việc

| Thành viên | Vai trò                                       | Công việc chính |
|------------|-----------------------------------------------|---|
| Việt Dũng  | Cấu trúc hệ thống, dữ liệu, quản lý người dùng | Maven, Git/GitHub, CI/CD, `User`, `Admin`, đăng ký/đăng nhập, DAO, `DatabaseConnection`, Singleton |
| Hoàng Minh | Giao diện Client MVC, quản lý sản phẩm        | JavaFX/FXML, dashboard, danh sách sản phẩm, chi tiết sản phẩm, đăng sản phẩm, `Item`, `Electronics`, `Art`, `Vehicle`, Factory Method, LineChart |
| Minh Duy   | Cốt lõi đấu giá, giao tiếp mạng               | Socket Client-Server, `AuctionServer`, `AuctionClient`, `AuctionMessage`, realtime update, xử lý mở/dừng phiên |
| Chí Bách   | Logic đấu giá,Thuật toán nâng cao, xử lý lỗi, kiểm thử      | Concurrent bidding, auto-bidding, anti-sniping, xử lý ngoại lệ, JUnit tests |

---




