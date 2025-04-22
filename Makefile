dev:
	@./mvnw quarkus:dev

build:
	@./mvnw -B --no-transfer-progress package

build-uber:
	@./mvnw -B --no-transfer-progress package -Dquarkus.package.jar.type=uber-jar

native:
	@./mvnw -B --no-transfer-progress package -Dnative -Dquarkus.native.container-build=true

clean:
	@./mvnw -B --no-transfer-progress clean

