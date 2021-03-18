FROM python:3.9-slim
WORKDIR /workdir
COPY requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt
COPY src/*.py .
COPY docs /docs
RUN useradd -u 2004 -U docker && \
    mkdir /home/docker && \
    chown -R docker:docker /docs /home/docker
ENV BC_API_URL http://127.0.0.1/
ENV LOG_LEVEL ERROR
USER docker
ENTRYPOINT [ "python" ]
CMD [ "codacy_checkov.py" ]
