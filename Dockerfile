FROM python:3.12-alpine3.18
WORKDIR /workdir
COPY requirements.txt ./
RUN apk update && \
    apk add musl-dev && \
    apk add gcc && \
    apk add libffi-dev && \
    pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt
COPY src/*.py ./
COPY docs /docs
RUN adduser -u 2004 -D docker && \
    chown -R docker:docker /docs /home/docker
USER docker
ENTRYPOINT [ "python" ]
CMD [ "codacy_checkov.py" ]
