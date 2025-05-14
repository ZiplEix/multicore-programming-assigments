#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

#define SEQUENTIAL_CUTOFF 1000

long divide_and_conquer_sum(long* arr, int lo, int hi) {
    if ((hi - lo) < SEQUENTIAL_CUTOFF) {
        long sum = 0;
        for (int i = lo; i < hi; i++) {
            sum += arr[i];
        }
        return sum;
    } else {
        long left_sum = 0, right_sum = 0;
        int mid = (hi + lo) / 2;

        #pragma omp task shared(left_sum)
        {
            left_sum = divide_and_conquer_sum(arr, lo, mid);
        }

        #pragma omp task shared(right_sum)
        {
            right_sum = divide_and_conquer_sum(arr, mid, hi);
        }

        #pragma omp taskwait

        long total = left_sum + right_sum;
        printf("        sum(%dto%d)=%ld\n", lo, hi - 1, total);
        return total;
    }
}

int main(int argc, char* argv[]) {
    int NUM_END = 10000;
    if (argc == 2) {
        NUM_END = atoi(argv[1]);
    }

    long* int_arr = malloc(NUM_END * sizeof(long));
    for (int i = 0; i < NUM_END; i++) {
        int_arr[i] = i + 1;
    }

    long sum = 0;

    #pragma omp parallel
    {
        #pragma omp single
        {
            sum = divide_and_conquer_sum(int_arr, 0, NUM_END);
        }
    }

    printf("sum from 1 to %d=\n", NUM_END);
    printf("%ld\n", sum);

    free(int_arr);
    return 0;
}
