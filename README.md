# Lab 4: Spring Security - Todo Application

Dự án hoàn chỉnh triển khai theo tài liệu **Lab 3 (Spring Data JPA)** và **Lab 4 (Spring Security)**:
- **Spring Boot 3 + Spring Data JPA + MySQL + Hibernate**
- **Spring Security 6 (Lambda DSL)** + Password Hashing (**BCrypt**)
- **Custom Authentication & Authorization** lưu tài khoản trong MySQL (`users` table) qua `CustomUserDetailsService`
- **Method-level Security**: `@EnableMethodSecurity` và `@PreAuthorize`
- **Role-based Access Control (RBAC)**: Phân quyền `USER` và `ADMIN`
- **CSRF Defense**: Bật cho Thymeleaf forms, miễn trừ cho `/api/v1/**`
- **HTTP Basic Authentication**: Xác thực cho REST API qua Postman
- **Thymeleaf Security Dialect**: `sec:authentication`, `sec:authorize`

---

## 1. Cấu Trúc Dự Án

```text
C:\Users\Admin\Downloads\Lab4_SpringSecurity/
├── pom.xml                                                 # Khai báo Spring Boot 3, Security, JPA, MySQL, Thymeleaf Security
├── .mvn/                                                   # Maven Wrapper configuration
├── src/main/java/com/example/todolist/
│   ├── TodolistApplication.java                            # Main Application Entry Point
│   ├── config/
│   │   └── SecurityConfig.java                             # SecurityFilterChain, PasswordEncoder & Seed tài khoản
│   ├── controller/
│   │   ├── HomeController.java                             # Chuyển hướng "/" về "/todos"
│   │   ├── TodoController.java                             # MVC Controller (Thymeleaf UI)
│   │   └── api/v1/
│   │       └── TodoRestController.java                     # REST API với @PreAuthorize
│   ├── dto/
│   │   └── TodoRequest.java                                # DTO + Bean Validation
│   ├── exception/
│   │   └── TodoNotFoundException.java                      # HTTP 404 Exception Handler
│   ├── model/
│   │   ├── Todo.java                                       # Entity 'todos' (JPA)
│   │   └── User.java                                       # Entity 'users' (JPA)
│   ├── repository/
│   │   ├── TodoRepository.java                             # JpaRepository cho Todo
│   │   └── UserRepository.java                             # JpaRepository (findByUsername)
│   └── service/
│       ├── CustomUserDetailsService.java                   # UserDetailsService load từ MySQL
│       └── TodoService.java                                # Nghiệp vụ Todo + @Transactional
├── src/main/resources/
│   ├── application.properties                              # Kết nối MySQL & Hibernate DDL
│   └── templates/todos/
│       ├── list.html                                       # Giao diện danh sách + User bar + sec:authorize
│       └── form.html                                       # Giao diện thêm / sửa Todo
└── src/test/java/com/example/todolist/
    └── TodolistApplicationTests.java                       # 8 Test cases tự động kiểm tra toàn bộ yêu cầu Lab 4
```

---

## 2. Tài Khoản Mặc Định Được Seed Sẵn

Khi ứng dụng khởi chạy lần đầu, `CommandLineRunner initUsers` trong `SecurityConfig` sẽ tự động tạo 2 tài khoản nếu bảng `users` trống:

| Username | Password | Role | Quyền hạn |
| :--- | :--- | :--- | :--- |
| **user** | `password123` | `ROLE_USER` | Xem, tạo, sửa Todo (HTML & REST). Không thể xóa Todo. |
| **admin** | `admin123` | `ROLE_ADMIN` | Toàn quyền (Xem, tạo, sửa, **xóa** Todo). |

---

## 3. Hướng Dẫn Cấu Hình MySQL & Chạy Ứng Dụng

### Bước 1: Khởi động MySQL Server
Đảm bảo MySQL đang chạy ở cổng `3306` (hoặc mở XAMPP và Start MySQL).
Xem thông tin cấu hình trong `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/todolist_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
```
*(Bạn có thể thay đổi `username` và `password` khớp với tài khoản MySQL của bạn, ví dụ `root` / trống hoặc `123456`).*

### Bước 2: Chạy ứng dụng
Mở Terminal tại thư mục `Lab4_SpringSecurity` và chạy:
```powershell
.\mvnw.cmd spring-boot:run
```
Hoặc mở thư mục này bằng **VS Code** / **IntelliJ IDEA** và chạy file `TodolistApplication.java`.

### Bước 3: Chạy bộ kiểm thử tự động (8/8 Tests Pass)
```powershell
.\mvnw.cmd test
```

---

## 4. Kịch Bản Kiểm Thử (Theo Đúng Step 7 & 8 Trong PDF)

### Kịch bản 1: Kiểm thử trên Trình duyệt (Form Login & Thymeleaf)
1. Mở trình duyệt truy cập: [http://localhost:8080/todos](http://localhost:8080/todos)
   - Hệ thống tự động chuyển hướng về trang đăng nhập [http://localhost:8080/login](http://localhost:8080/login).
2. Đăng nhập tài khoản `user` / `password123`:
   - Thanh điều hướng hiển thị: `Logged in as: user ([ROLE_USER])`.
   - Nút **Delete** bị ẩn hoàn toàn (nhờ thẻ `sec:authorize="hasRole('ADMIN')"`).
3. Bấm **Logout** trên thanh điều hướng -> Được chuyển về trang đăng nhập.
4. Đăng nhập tài khoản `admin` / `admin123`:
   - Thanh điều hướng hiển thị: `Logged in as: admin ([ROLE_ADMIN])`.
   - Nút **Delete** hiển thị đầy đủ và có thể xóa Todo thành công.

### Kịch bản 2: Kiểm thử REST API trên Postman (HTTP Basic Auth)
1. Gửi request `GET http://localhost:8080/api/v1/todos` (No Auth):
   - Server trả về **HTTP 401 Unauthorized**.
2. Chọn tab **Authorization** -> Type: **Basic Auth**, nhập `user` / `password123`:
   - Gửi `GET http://localhost:8080/api/v1/todos`: Server trả về **HTTP 200 OK** kèm danh sách JSON.
3. Giữ nguyên Basic Auth `user`, gửi `DELETE http://localhost:8080/api/v1/todos/1`:
   - Server trả về **HTTP 403 Forbidden** (bị chặn bởi URL Matcher & `@PreAuthorize("hasRole('ADMIN')")`).
4. Đổi Basic Auth sang `admin` / `admin123`:
   - Gửi lại `DELETE http://localhost:8080/api/v1/todos/1`: Server trả về **HTTP 200 OK** và xóa thành công.
