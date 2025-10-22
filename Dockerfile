# Dùng image có sẵn Java 17
FROM eclipse-temurin:17-jdk

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy toàn bộ mã nguồn vào container
COPY . .

# Cấp quyền chạy mvnw
RUN chmod +x mvnw

# Build project
RUN ./mvnw clean package -DskipTests

# Chạy file jar được build
CMD ["java", "-jar", "target/*.jar"]
