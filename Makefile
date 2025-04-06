build:
	@./mvnw package -B --no-transfer-progress package

build-uber:
	@./mvnw package -B --no-transfer-progress package -Dquarkus.package.jar.type=uber-jar

native:
	@./mvnw package -Dnative -Dquarkus.native.container-build=true

clean:
	@./mvnw clean -B --no-transfer-progress

