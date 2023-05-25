docker stop postgres
docker rm postgres
docker run -d -p5432:5432 -e POSTGRES_PASSWORD=unsafe --name postgres postgres:latest
mvn -f pom.xml -DskipTests clean spring-boot:run &
