
# Install Python 3 and Pandas inside the `hive-server` Container

### **1. Open a root shell in your hive-server container**

```sh
docker exec -u root -it hive-server bash
```

Now you’re inside the container as `root`.

---

### **2. Update the package list**

```sh
apt-get update
```

This ensures you have the latest info about available packages.

---

### **3. Install Python 3 and pip**

```sh
apt-get install -y python3 python3-pip
```

---

### **4. Install pandas and pyarrow**

`pandas` is the data analysis library, and `pyarrow` allows pandas to read Parquet files.

```sh
pip3 install pandas pyarrow
```

---

### **5. Enter the directory with your Parquet file**

```sh
cd /opt/hive/examples/files
```

---

### **6. Start Python 3 interactive shell**

```sh
python3
```

---

### **7. In the Python prompt, read and show info about the Parquet file**

Type these commands **one by one** at the `>>>` Python prompt:

```python
import pandas as pd

df = pd.read_parquet('web_sales.parquet')

# Show summary info about the DataFrame
df.info()

# Show the first 5 rows
print(df.head())

# Optionally, see how many rows/columns
print("Shape:", df.shape)
```

---

### **8. Exit Python shell**

Press `Ctrl+D` or type `exit()` and press Enter.

---

## Example Output

* `df.info()` will show you columns, types, and counts.
* `df.head()` will show you the first few rows of your data.
* `df.shape` shows (rows, columns).

---

##  **Quick Recap**

1. Enter container as root
2. Update apt-get
3. Install python3, pip3
4. Install pandas, pyarrow
5. Go to the file directory
6. Use Python to read and inspect your parquet file!