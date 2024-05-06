FROM python:3.12.3-alpine3.19

WORKDIR /workdir

COPY requirements.txt ./

RUN apk update && \
    # remove expat package until vulnerability is fixed
    apk del expat-dev && \
    pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

COPY src/*.py ./
COPY docs /docs

RUN adduser -u 2004 -D docker && \
    chown -R docker:docker /docs /home/docker

USER docker

ENTRYPOINT [ "python" ]
CMD [ "codacy_checkov.py" ]
