# Report: Approximating π using CPU (OpenMP) vs. GPU (Thrust)

This report presents a comparison of execution times for approximating the integral $S_0^1 (4.0/(1+x^2))dx$ (which evaluates to $\pi$) using two different approaches: a sequential C program with OpenMP (configured for one thread) and a parallel C++ program utilizing the CUDA Thrust library on a GPU. Both programs approximate the integral by computing a sum of rectangles using the midpoint rule, with $N = 1,000,000,000$ steps.

# Source Code

The following C++ source code, `thrust_ex.cu`, implements the π approximation using the NVIDIA CUDA Thrust library. It leverages `thrust::transform_reduce` for efficient parallel computation on the GPU.

```cpp
#include <thrust/host_vector.h>
#include <thrust/device_vector.h>
#include <thrust/transform_reduce.h>
#include <thrust/iterator/counting_iterator.h>
#include <thrust/functional.h>
#include <cstdio>
#include <chrono>

// Functor to compute F(x_i) for a given index i.
// This functor will be executed on the GPU by Thrust.
struct F_xi_functor {
    double step_val;

    F_xi_functor(double step) : step_val(step) {}

    // The operator() defines the computation for each element.
    // `__host__ __device__` allows this function to be compiled for both CPU and GPU.
    __host__ __device__
    double operator()(long long idx) const {
        double x = (static_cast<double>(idx) + 0.5) * step_val;
        return 4.0 / (1.0 + x * x);
    }
};

int main() {
    long long num_steps = 1000000000LL;

    double step = 1.0 / static_cast<double>(num_steps);
    double pi_approx_sum_Fxi = 0.0;
    double pi_final_approximation = 0.0;

    printf("Starting Thrust-based PI approximation for N = %lld steps...\n", num_steps);

    auto start_time = std::chrono::high_resolution_clock::now();

    pi_approx_sum_Fxi = thrust::transform_reduce(
        thrust::make_counting_iterator<long long>(0),
        thrust::make_counting_iterator<long long>(num_steps),
        F_xi_functor(step),
        0.0,
        thrust::plus<double>()
    );

    pi_final_approximation = pi_approx_sum_Fxi * step;

    auto end_time = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> time_diff = end_time - start_time;

    printf("Execution Time : %.10lfsec\n", time_diff.count());
    printf("pi=%.10lf\n", pi_final_approximation);

    return 0;
}
```

# Execution Time Table

The following table summarizes the execution times for both programs:

| Program | Configuration | Execution Time (seconds) | Approximated π Value |
|---------|---------------|--------------------------|----------------------|
omp_pi_one.c | Sequential (1 CPU thread) | 6.8086040330 | 3.1415926536 |
| thrust_ex.cu | Parallel (GPU using Thrust) | 0.5283493540 | 3.1415926536 |

# Explanation/Interpretation of Results

The results clearly demonstrate a significant performance difference between the sequential CPU implementation and the parallel GPU implementation using the Thrust library. The GPU version (thrust_ex.cu) completed the computation in approximately 0.53 seconds, which is over 12 times faster than the CPU version (omp_pi_one.c) that took about 6.81 seconds.

This drastic performance improvement is primarily due to the fundamental architectural differences between CPUs and GPUs, and how they are utilized for this specific problem:
 1. Parallelism: The problem of approximating an integral by summing a large number of rectangles is inherently "embarrassingly parallel." This means that each rectangle's height F(x_i) can be calculated independently of all other rectangles.
 2. GPU Architecture: GPUs are designed with thousands of smaller, specialized cores optimized for parallel processing of many simple tasks simultaneously. In contrast, CPUs have a few powerful cores optimized for complex, sequential tasks. For a task like summing one billion independent calculations, the GPU's massive parallelism allows it to perform many calculations concurrently, leading to a much faster overall execution time.
 3. Thrust Library: The Thrust library provides high-level abstractions that make it easy to write parallel algorithms for GPUs. Functions like thrust::transform_reduce efficiently map the element-wise computation (F_xi_functor) and the reduction (summation) onto the GPU's parallel architecture, handling the complexities of memory management and thread orchestration behind the scenes.
 4. Floating-Point Operations: Both CPUs and GPUs are capable of floating-point arithmetic, but GPUs are particularly efficient at performing a large volume of these operations in parallel, which is exactly what is required for this numerical integration.

In essence, for computationally intensive tasks that can be broken down into many independent sub-tasks, leveraging a GPU with libraries like Thrust offers substantial performance gains over traditional sequential CPU processing.

# Execution Results (output text)

## omp_pi_one.c

```bash
$ ./omp_pi_one
Execution Time : 6.8086040330sec
pi=3.1415926536
```

## thrust_ex.cu (google colab execution)

```bash
$ !nvcc -arch=sm_75 thrust_ex.cu -o thrust_ex
$ !./thrust_ex
Starting Thrust-based PI approximation for N = 1000000000 steps...
Execution Time : 0.5283493540sec
pi=3.1415926536
```
