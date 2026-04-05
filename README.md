# Hệ thống thi trắc nghiệm LAN

Ứng dụng Spring Boot (REST API + giao diện web Thymeleaf) phục vụ thi trắc nghiệm trong mạng LAN. Giao diện web chủ yếu dành cho **quản trị viên** và **giảng viên**; **sinh viên** làm bài qua **ứng dụng client** (ví dụ thư mục `thi-trac-nghiem-lan-client` trong cùng workspace) gọi API `/api/...`.

## Cấu trúc thư mục (Spring Boot)

Mã nguồn chính nằm trong `src/main/java/com/example/server/` với các package sau:

```
src/main/java/com/example/server/
├── ServerApplication.java    # Điểm vào ứng dụng Spring Boot
├── config/                   # Cấu hình
├── controller/               # REST API / Controller
├── model/                    # Entity / Model
├── repository/               # Truy cập dữ liệu (JPA Repository)
└── service/                  # Logic nghiệp vụ (Service)
```

### Mô tả từng package

| Package      | Mục đích |
|-------------|----------|
| **config**  | Cấu hình ứng dụng: CORS, WebMvc, Security, Bean, Message Converter, v.v. |
| **controller** | Lớp điều khiển HTTP: nhận request, gọi service, trả response (REST API). |
| **model**   | Entity / POJO ánh xạ với bảng trong cơ sở dữ liệu (ví dụ: User, Exam, Question). |
| **repository** | Interface Spring Data JPA Repository để truy vấn database (ví dụ: `UserRepository`, `ExamRepository`). |
| **service** | Logic nghiệp vụ: xử lý dữ liệu, gọi repository, giao tiếp với controller. |

### Luồng xử lý (Request flow)

```
Client → Controller → Service → Repository → Database
                ↑           ↓
                ← Response ←
```

- **Controller**: Nhận HTTP request, validate input, gọi **Service**, trả HTTP response.
- **Service**: Chứa business logic, gọi **Repository** để đọc/ghi dữ liệu.
- **Repository**: Giao tiếp với database (CRUD) thông qua Spring Data JPA.
- **Model**: Định nghĩa cấu trúc dữ liệu (entity) dùng trong toàn bộ các tầng trên.
- **Config**: Cấu hình chung cho ứng dụng (security, CORS, format JSON, v.v.).

## Chạy ứng dụng

```bash
./mvnw spring-boot:run
```

Hoặc chạy class `ServerApplication` từ IDE.

## Công nghệ

- Java
- Spring Boot
- Spring Data JPA (khi kết nối database)
- Maven

---

## Hướng dẫn sử dụng giao diện web

### 1. Chuẩn bị và truy cập

1. Cấu hình MySQL và chạy server (xem mục [Chạy ứng dụng](#chạy-ứng-dụng)). Mặc định Spring Boot lắng nghe cổng **8080** (trừ khi bạn đặt `server.port` khác trong cấu hình).
2. Mở trình duyệt và vào địa chỉ: `http://localhost:8080` (hoặc `http://<IP-máy-chủ-LAN>:8080` khi máy khác trong LAN truy cập).
3. Trang gốc `/` được chuyển hướng tới `/admin`. Nếu chưa đăng nhập, hệ thống sẽ đưa bạn tới trang đăng nhập `/login`.

### 2. Đăng nhập và đăng xuất

- **Đăng nhập:** `http://localhost:8080/login`
  - Form dành cho **cán bộ** (quản trị viên / giảng viên): nhập **email** và **mật khẩu** (tên tham số form: `email`, `matKhau`).
  - Sau khi đăng nhập thành công, hệ thống chuyển hướng theo vai trò:
    - **Quản trị viên** → `/admin` (bảng điều khiển quản trị).
    - **Giảng viên** → `/lecturer/bang-dieu-khien-giang-vien` (bảng điều khiển giảng viên).
- **Đăng xuất:** dùng nút đăng xuất trên thanh sidebar (gửi `POST` tới `/logout`).

Nếu truy cập trang không đủ quyền, bạn có thể được chuyển tới `/access-denied`.

### 3. Vai trò và quyền truy cập nhanh

| Vai trò | Vùng giao diện web chính |
|--------|---------------------------|
| **Quản trị viên** | Toàn bộ `/admin/**`, đồng thời được dùng chung các trang `/de-thi/**`, `/ngan-hang-cau-hoi/**` như giảng viên. |
| **Giảng viên** | `/lecturer/**`, `/de-thi/**`, `/ngan-hang-cau-hoi/**` (soạn đề, ngân hàng câu hỏi, giám sát, thống kê). |

Một số API REST chỉ dành cho quản trị (cơ cấu tổ chức, người dùng, phân công…) hoặc cho cả quản trị và giảng viên (môn học, đề thi, thống kê, giám sát…) — được cấu hình trong `SecurityConfig`.

### 4. Quản trị viên (`/admin`)

Dùng **menu bên trái** để điều hướng.

- **Bảng điều khiển** (`/admin`): Tổng quan (thống kê khoa, sinh viên, câu hỏi, ca thi…), danh sách **ca thi đang diễn ra**, và khối **Ngừng khẩn cấp hệ thống** — kết thúc đồng thời mọi ca đang ở trạng thái “Đang diễn ra” (có xác nhận; thao tác được ghi nhật ký).
- **Cơ cấu tổ chức** (`/admin/co-cau-to-chuc`): Quản lý trường / đơn vị trong cấu trúc tổ chức (xem chi tiết, chỉnh sửa theo luồng trang).
- **Người dùng** (`/admin/quan-ly-nguoi-dung`): Quản lý tài khoản người dùng.
- **Quản lý môn học** (`/admin/quan-ly-mon-hoc`): Quản lý môn học.
- **Phân công GV–Lớp** (`/admin/phan-cong-giang-vien-lop`): Phân công giảng viên với lớp/môn (giao diện gọi API phân công).
- **Ngân hàng câu hỏi** (`/ngan-hang-cau-hoi`): Danh sách, thêm/sửa câu hỏi, kiểm tra, import (các đường dẫn con như `/ngan-hang-cau-hoi/them`, `.../chinh-sua`, `.../kiem-tra`, `.../thanh-cong`).
- **Đề thi** (`/de-thi`): Quản lý đề; thêm mới `/de-thi/them-moi`, sửa `/de-thi/sua` (theo tham số trên giao diện).
- **Ca thi** (`/admin/ca-thi`): Danh sách ca, tạo mới, xem/chỉnh sửa ca. Với từng ca, trang **thao tác ca thi** (`/admin/ca-thi/{mã-ca}/thao-tac`) hỗ trợ giám thị: lọc check-in, tìm theo MSSV/họ tên, thêm sinh viên vào ca, v.v.
- **Nhật ký hệ thống** (`/admin/nhat-ky-he-thong`): Xem nhật ký thao tác.
- **Cài đặt** (`/admin/cai-dat-admin`): Cấu hình phía quản trị.

Nút **thu gọn / mở rộng** trên sidebar lưu trạng thái trong trình duyệt (localStorage).

### 5. Giảng viên (`/lecturer`)

- **Bảng điều khiển** (`/lecturer/bang-dieu-khien-giang-vien`): Thống kê tổng quan (ca thi, câu hỏi, lượt làm bài, biểu đồ trạng thái ca, hoạt động gần đây).
- **Khóa học của tôi** (`/lecturer/khoahoccoatoi`): Các khóa/môn được phân công; vào **chi tiết** qua `/lecturer/chitietkhoahoc?maGiangVienMonHoc=...` (danh sách sinh viên, v.v. tùy giao diện).
- **Ngân hàng câu hỏi** và **Quản lý đề thi**: cùng đường dẫn `/ngan-hang-cau-hoi` và `/de-thi` như quản trị (khi đăng nhập bằng tài khoản giảng viên).
- **Giám sát thời gian thực** (`/lecturer/giam-sat-thi`, alias `/lecturer/giam-sat`): Chọn ca đang giám sát; **chi tiết** `/lecturer/giam-sat-thi/chi-tiet?maCaThi=...`; **theo dõi bài** `/lecturer/giam-sat-thi/theo-doi-bai?maCaThi=...&maBaiThi=...`.
- **Kết quả & báo cáo** (`/lecturer/thong-ke-ket-qua-thi`, có thể rút gọn `/lecturer/thong-ke`): Thống kê điểm, xuất dữ liệu theo chức năng trên trang; chi tiết theo ca: `/lecturer/thong-ke/chi-tiet?maCaThi=...`.

*Nếu tài khoản quản trị mở URL giảng viên nhưng không gắn bản ghi giảng viên, giao diện “Khóa học của tôi” có thể báo không phải giảng viên — đây là hành vi dự kiến.*

### 6. Sinh viên và ứng dụng thi (không phải trang web soạn thảo trong repo này)

- Sinh viên **không** dùng form đăng nhập `/login` (cổng đó ghi rõ dành cho cán bộ). Làm bài thông qua **client** kết nối tới server:
  - Ví dụ: project **`thi-trac-nghiem-lan-client`** — WebSocket `ws://<host>:8080/ws`, nộp bài HTTP `http://<host>:8080/api/submit` (xem README trong client).
  - API đăng nhập/phiên thi cho client: `POST /api/auth/login` (MSSV + mật khẩu), `GET /api/sessions`, … (chi tiết trong `StudentClientApiController`).

### 7. Gợi ý vận hành trong LAN

- Chạy server trên một máy “máy chủ”; máy thí sinh cài client và trỏ tới `IP:8080` của máy chủ.
- Đảm bảo firewall cho phép cổng HTTP (và WebSocket nếu client dùng WS).
- Cơ sở dữ liệu MySQL (`thitracnghiem`) phải sẵn sàng trước khi khởi động ứng dụng (xem `application.properties`).
