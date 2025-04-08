JAVA_VERSION := 17

DOCKER_IMAGE_NAME := java:$(JAVA_VERSION)

all: run

build:
	docker build -t $(DOCKER_IMAGE_NAME) .

run:
	docker run \
		-it \
		--rm \
		-v "$(shell pwd):/usr/src/app" \
		$(DOCKER_IMAGE_NAME)
