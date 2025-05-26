#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>
#include <omp.h> // Include OpenMP header

#define SPHERES 20
#define rnd(x) (x * rand() / RAND_MAX)
#define INF 2e10f
#define DIM 2048

struct Sphere
{
    float r, b, g;
    float radius;
    float x, y, z;
    float hit(float ox, float oy, float *n)
    {
        float dx = ox - x;
        float dy = oy - y;
        if (dx * dx + dy * dy < radius * radius)
        {
            float dz = sqrtf(radius * radius - dx * dx - dy * dy);
            *n = dz / sqrtf(radius * radius);
            return dz + z;
        }
        return -INF;
    }
};

void kernel(int x, int y, Sphere *s, unsigned char *ptr)
{
    int offset = x + y * DIM;
    float ox = (x - DIM / 2.0f); // Use .0f for float division
    float oy = (y - DIM / 2.0f); // Use .0f for float division

    float r = 0, g = 0, b = 0;
    float maxz = -INF;
    for (int i = 0; i < SPHERES; i++) {
        float n;
        float t = s[i].hit(ox, oy, &n);
        if (t > maxz) {
            float fscale = n;
            r = s[i].r * fscale;
            g = s[i].g * fscale;
            b = s[i].b * fscale;
            maxz = t;
        }
    }

    ptr[offset * 4 + 0] = (unsigned char)(r * 255);
    ptr[offset * 4 + 1] = (unsigned char)(g * 255);
    ptr[offset * 4 + 2] = (unsigned char)(b * 255);
    ptr[offset * 4 + 3] = 255;
}

void ppm_write(unsigned char *bitmap, int xdim, int ydim, FILE *fp)
{
    int i, x, y;
    fprintf(fp, "P3\n");
    fprintf(fp, "%d %d\n", xdim, ydim);
    fprintf(fp, "255\n");
    for (y = 0; y < ydim; y++) {
        for (x = 0; x < xdim; x++) {
            i = x + y * xdim;
            fprintf(fp, "%d %d %d ", bitmap[4 * i], bitmap[4 * i + 1], bitmap[4 * i + 2]);
        }
        fprintf(fp, "\n");
    }
}

int main(int argc, char *argv[])
{
    int no_threads;
    unsigned char *bitmap;
    double start_time, end_time;

    srand(time(NULL));

    if (argc != 2) {
        printf("Usage: %s [number of threads]\n", argv[0]);
        printf("Example: %s 8\n", argv[0]);
        exit(0);
    }

    no_threads = atoi(argv[1]);
    if (no_threads <= 0) {
        printf("Number of threads must be a positive integer.\n");
        exit(0);
    }
    omp_set_num_threads(no_threads);

    Sphere *temp_s = (Sphere *)malloc(sizeof(Sphere) * SPHERES);
    if (!temp_s) {
        perror("Failed to allocate memory for spheres");
        return 1;
    }

    for (int i = 0; i < SPHERES; i++) {
        temp_s[i].r = rnd(1.0f);
        temp_s[i].g = rnd(1.0f);
        temp_s[i].b = rnd(1.0f);
        temp_s[i].x = rnd(2000.0f) - 1000;
        temp_s[i].y = rnd(2000.0f) - 1000;
        temp_s[i].z = rnd(2000.0f) - 1000;
        temp_s[i].radius = rnd(200.0f) + 40;
    }

    bitmap = (unsigned char *)malloc(sizeof(unsigned char) * DIM * DIM * 4);
    if (!bitmap) {
        perror("Failed to allocate memory for bitmap");
        free(temp_s);
        return 1;
    }

    start_time = omp_get_wtime(); // Start timing

    #pragma omp parallel for collapse(2)
    for (int y = 0; y < DIM; y++) {
        for (int x = 0; x < DIM; x++) {
            kernel(x, y, temp_s, bitmap);
        }
    }

    end_time = omp_get_wtime(); // End timing

    FILE *fp = fopen("result.ppm", "w");
    if (!fp) {
        perror("Failed to open result.ppm for writing");
        free(bitmap);
        free(temp_s);
        return 1;
    }

    ppm_write(bitmap, DIM, DIM, fp);
    fclose(fp);

    printf("OpenMP (%d threads) ray tracing: %.3f sec\n", no_threads, end_time - start_time);
    printf("[result.ppm] was generated.\n");

    free(bitmap);
    free(temp_s);

    return 0;
}
