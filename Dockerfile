FROM python:3.13-alpine3.21

WORKDIR /workdir

COPY requirements.txt ./

RUN apk update && apk add --no-cache \
    curl \
    git \
    bash \
    build-base \
    openssl \
    libffi-dev \
    py3-pip \
    kustomize \
    helm \
    && pip install --upgrade pip \
    && pip install --no-cache-dir -r requirements.txt

COPY src/*.py ./
COPY docs /docs

RUN adduser -u 2004 -D docker && \
    chown -R docker:docker /docs /home/docker

USER docker

ENTRYPOINT [ "python" ]
CMD [ "codacy_checkov.py" ]