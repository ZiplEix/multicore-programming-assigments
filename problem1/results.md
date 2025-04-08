# Problem 1 Results

## Environment

| CPU Type | CPU Model | Number of Cores | CPU Frequency | RAM Size | OS | Runtime Environment |
|----------|-----------|-----------------|---------------|----------|----|---------------------|
| AMD Razen | AMD Ryzen 7 5800H | 8 | 3.2 GHz | 16 GB | Windows 11 -> WSL2 -> Ubuntu 24.04.2 | Docker openjdk:17-jdk-slim |

## Results

All the resuts diplay here are the average of 10 runs to find the number of prime numbers in the range of 1 to 200000.

For better understanding on how the code tests are runs, please refer to the [`src/BenchmarkRunner.java`](../src/BenchmarkRunner.java) file.

All the times are in milliseconds.

### Execution Time

pc_serial : 5117 ms

| Thread number |  1 | 2 | 4 | 6 | 8 | 10 | 12 | 14 | 16 | 32 |
|---------------|----|---|---|---|---|----|----|----|----|----|
| static (block) | 5220 | 5153 | 2620 | 2185 | 1497 | 1657 | 972 | 1209 | 827 | 1100 |
| static (cyclic) [task size : 10] | 5544 | 3040 | 2164 | 2189 | 856 | 1112 | 1212 | 1046 | 7110 | 941 |
| dynamic [task size : 10] | 
