#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
#include <time.h>
#include <math.h>

#define MAX_NUM 200000

// Check if a number is prime
int is_prime(int num) {
    if (num < 2) return 0;
    for (int i = 2; i * i <= num; i++) {
        if (num % i == 0) return 0;
    }
    return 1;
}

// Check if a number is prime with a slow calculation
int is_prime_slow(int num) {
    if (num < 2) return 0;
    int slow_count = 0;
    for (int i = 2; i * i <= num; i++) {
        // Intentionally slow calculation
        for (int j = 0; j < 100; j++) {
            slow_count += sqrt(i * j) / (j + 1);
        }
        if (num % i == 0) return 0;
    }
    return 1;
}

int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <scheduling_type#> <#_of_threads>\n", argv[0]);
        return 1;
    }

    int scheduling_type = atoi(argv[1]);
    int num_threads = atoi(argv[2]);
    int prime_count = 0;
    double start_time, end_time;

    omp_set_num_threads(num_threads);
    start_time = omp_get_wtime();

    switch (scheduling_type) {
        case 1: // static with default chunk size
            #pragma omp parallel for schedule(static) reduction(+:prime_count)
            for (int i = 1; i <= MAX_NUM; i++) {
                if (is_prime_slow(i)) prime_count++;
            }
            break;
        case 2: // dynamic with default chunk size
            #pragma omp parallel for schedule(dynamic) reduction(+:prime_count)
            for (int i = 1; i <= MAX_NUM; i++) {
                if (is_prime_slow(i)) prime_count++;
            }
            break;
        case 3: // static with chunk size 10
            #pragma omp parallel for schedule(static, 10) reduction(+:prime_count)
            for (int i = 1; i <= MAX_NUM; i++) {
                if (is_prime_slow(i)) prime_count++;
            }
            break;
        case 4: // dynamic with chunk size 10
            #pragma omp parallel for schedule(dynamic, 10) reduction(+:prime_count)
            for (int i = 1; i <= MAX_NUM; i++) {
                if (is_prime_slow(i)) prime_count++;
            }
            break;
        default:
            fprintf(stderr, "Invalid scheduling type!\n");
            return 1;
    }

    end_time = omp_get_wtime();
    printf("Number of primes: %d\n", prime_count);
    printf("Execution time: %f milliseconds\n", (end_time - start_time) * 1000);

    return 0;
}
