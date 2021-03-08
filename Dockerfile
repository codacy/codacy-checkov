FROM python:3.8-slim
WORKDIR /workdir
COPY requirements.txt requirements.txt
RUN pip3 install --no-cache-dir -r requirements.txt
COPY src/*.py .
COPY docs /docs
RUN useradd -u 2004 -U docker && \
    mkdir /home/docker && \
    chown -R docker:docker /docs /home/docker
USER docker
ENTRYPOINT [ "python3.8" ]
CMD [ "codacy_checkov.py" ]
