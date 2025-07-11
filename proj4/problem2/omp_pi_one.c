#include <omp.h>
#include <stdio.h>

long num_steps = 1000000000;
double step;

int main ()
{
	long i; double x, pi, sum = 0.0;
	double start_time, end_time;

	omp_set_num_threads(1);
	start_time = omp_get_wtime();
	step = 1.0/(double) num_steps;
	for (i=0;i< num_steps; i++) {
		x = (i+0.5)*step;
		sum = sum + 4.0/(1.0+x*x);
	}
	pi = step * sum;
	end_time = omp_get_wtime();
	double timeDiff = end_time - start_time;
    printf("Execution Time : %.10lfsec\n", timeDiff);

	printf("pi=%.10lf\n",pi);

    return 0;
}
