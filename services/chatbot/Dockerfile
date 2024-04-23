FROM python:3.12-slim
# Install required system packages for compiling hnswlib
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    && rm -rf /var/lib/apt/lists/*

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY requirements.txt /app/requirements.txt
# Install any needed dependencies specified in requirements.txt
RUN pip install --no-cache-dir -r requirements.txt
COPY src /app
COPY retrieval /app/retrieval
ENV PYTHONPATH "${PYTHONPATH}:/app"
CMD python3.12 -m gunicorn --bind 0.0.0.0:5002 chatbot_api:app

EXPOSE 5002