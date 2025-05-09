plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.allopen") version "1.9.22"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.springframework.boot") version "3.2.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "ai.support"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven("https://repo.spring.io/milestone")
	maven("https://repo.spring.io/snapshot")
	maven {
		name = "Central Portal Snapshots"
		url = uri("https://central.sonatype.com/repository/maven-snapshots/")
	}
}

extra["springAiVersion"] = "1.0.0-SNAPSHOT"

dependencies {
	implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
	implementation("org.jsoup:jsoup:1.20.1")
	implementation("com.google.guava:guava:33.4.8-jre")
	implementation("org.apache.commons:commons-lang3:3.17.0")
	implementation("com.github.haifengl:smile-core:4.3.0")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
	implementation("org.springframework.boot:spring-boot-starter-web")

//	implementation("org.springframework.ai:spring-ai-starter-model-ollama")
	implementation("org.springframework.ai:spring-ai-ollama")

	// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	runtimeOnly("org.postgresql:postgresql")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}


kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.Embeddable")
	annotation("jakarta.persistence.MappedSuperclass")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
