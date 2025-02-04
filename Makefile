build:
	@./mvnw package -B --no-transfer-progress package
	@./mvnw package -B --no-transfer-progress package -Dquarkus.package.jar.type=uber-jar

clean:
	@./mvnw clean -B --no-transfer-progress

ebs:
	@rm -rf target/ebs
	@mkdir -p target/ebs
	@cp target/*-runner.jar target/ebs/app.jar
	@cp -r Procfile .ebextensions target/ebs/
	@cd target/ebs && zip ../submit-ebs.zip -r * .[^.]*
