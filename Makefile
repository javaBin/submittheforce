build:
	@./mvnw package -B --no-transfer-progress package
	@./mvnw package -B --no-transfer-progress package -Dquarkus.package.jar.type=uber-jar

clean:
	@./mvnw clean -B --no-transfer-progress

