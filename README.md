# Hadoop Lite Docker ver 2.0

A lightweight, easy-to-use Hadoop stack for local development and testing.
This project uses Docker Compose to spin up a minimal, production-style big data cluster on your machine—including Hadoop, Hive, HBase, Kafka, Spark, Flink, Hue, Jupyter, and more.
Perfect for students, engineers, and anyone learning distributed data systems.

---

## Features

* Hadoop HDFS and multiple DataNodes
* Hive Metastore and HiveServer2 (SQL on Hadoop)
* Hue web UI for easy data exploration
* Jupyter Notebook (with Spark)
* Kafka, Zookeeper, Kafka Manager
* Flink (JobManager/TaskManager)
* Pre-configured PostgreSQL and MySQL databases for Hive and Hue
* Minimal configuration required

---

## Services

| Service           | Purpose                               | Exposed Port |
| ----------------- | ------------------------------------- | ------------ |
| namenode          | Hadoop HDFS NameNode                  | 50070        |
| datanode1-3       | Hadoop HDFS DataNodes                 | 50075+       |
| resourcemanager   | Hadoop YARN ResourceManager (job scheduling, cluster mgmt) | 8088 |
| historyserver     | Hadoop History Server                 | 19888        |
| postgres          | Metastore backend for Hive            | 5432         |
| metastore         | Hive Metastore Service                | 9083         |
| hive-server       | HiveServer2 (SQL engine)              | 10000,10002  |
| hue               | Web UI for Hadoop ecosystem           | 8888         |
| database          | MySQL database for Hue                | 33061        |
| zookeeper         | Zookeeper (required by Kafka/HBase)   | 2181         |
| kafka             | Distributed messaging                 | 9092         |
| kafkamanager      | Kafka Manager web UI                  | 9000         |
| jupyter-spark     | Jupyter Notebook (with PySpark)       | 8889, 4040+  |
| flink-jobmanager  | Flink JobManager                      | 8085         |
| flink-taskmanager | Flink TaskManager                     | -            |

---

## Prerequisites

* [Docker Desktop](https://www.docker.com/products/docker-desktop) (or Docker Engine)

**Recommended:** At least 8GB RAM (the full stack runs multiple containers)

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/datatweets/hadoop-lite-docker-v2.0.git
cd hadoop-lite-docker
```

---

### 2. Directory Structure

```text
hadoop-lite-docker/
  ├── base/
  ├── docker-compose.yml
  ├── README.md
  └── ... (supporting config and scripts)
```

---

### 3. Start the Cluster

```bash
docker compose up -d
```

* The first run will pull all required images and set up the cluster.
* To check status, use:

  ```bash
  docker compose ps
  ```

---

### 4. Access the Services

| Service       | URL/Address                                      | Notes                      |
| ------------- | ------------------------------------------------ | -------------------------- |
| **Hue UI**    | [http://localhost:8888](http://localhost:8888)   | All-in-one Hadoop web UI   |
| **Jupyter**   | [http://localhost:8889](http://localhost:8889)   | Notebooks w/ Spark support |
| **Hadoop UI** | [http://localhost:50070](http://localhost:50070) | NameNode web UI            |
| **KafkaMgr**  | [http://localhost:9000](http://localhost:9000)   | Kafka Manager              |
| **Flink UI**  | [http://localhost:8085](http://localhost:8085)   | Flink Dashboard            |

---

### 5. Stop and Remove the Cluster

To stop the cluster:

```bash
docker compose down
```

To remove all data (start fresh), add the `-v` flag:

```bash
docker compose down -v
```

---

### 6. Customization

* Adjust service resources in `docker-compose.yml` as needed (e.g., lower memory for smaller systems).
* To add/remove components, comment out the relevant sections in the `docker-compose.yml` file.

---

## Troubleshooting

* Make sure no other services are running on the exposed ports (8888, 50070, 5432, etc.).
* On the first run, some services (like Hive or Hue) may take a minute or two to become fully available.
* For low-resource environments, consider running only essential containers (comment out extras in the compose file).

---

## License

MIT License (see [LICENSE](LICENSE) file)

---

## Credits

Based on [Hadoop Docker](https://github.com/datatweets/hadoop-docker) and [Docker Big Data Tools](https://github.com/ZakariaMahmoud/Docker-BigData-Tools.git) with customizations and enhancements for a lighter, educational experience.
