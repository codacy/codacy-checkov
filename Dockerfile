FROM python:3.9-alpine
WORKDIR /workdir
COPY requirements.txt src/*.py ./
COPY docs /docs
RUN pip install --no-cache-dir -r requirements.txt &&\
    adduser -u 2004 -D docker && \
    chown -R docker:docker /docs /home/docker
USER docker
ENTRYPOINT [ "python" ]
CMD [ "codacy_checkov.py" ]
