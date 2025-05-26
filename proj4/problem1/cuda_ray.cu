#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>

#define SPHERES 20
#define rnd(x) (x * rand() / RAND_MAX)
#define INF 2e10f
#define DIM 2048

struct Sphere
{
    float r, b, g;
    float radius;
    float x, y, z;
};

__device__ float hit(float ox, float oy, const Sphere *s, float *n)
{
    float dx = ox - s->x;
    float dy = oy - s->y;
    if (dx * dx + dy * dy < s->radius * s->radius)
    {
        float dz = sqrtf(s->radius * s->radius - dx * dx - dy * dy);
        *n = dz / sqrtf(s->radius * s->radius);
        return dz + s->z;
    }
    return -INF;
}

__global__ void rayTracingKernel(Sphere *s_dev, unsigned char *bitmap_dev)
{
    int x = blockIdx.x * blockDim.x + threadIdx.x;
    int y = blockIdx.y * blockDim.y + threadIdx.y;

    if (x < DIM && y < DIM)
    {
        int offset = x + y * DIM;
        float ox = (x - DIM / 2.0f);
        float oy = (y - DIM / 2.0f);

        float r = 0, g = 0, b = 0;
        float maxz = -INF;
        for (int i = 0; i < SPHERES; i++) {
            float n;
            float t = hit(ox, oy, &s_dev[i], &n);
            if (t > maxz) {
                float fscale = n;
                r = s_dev[i].r * fscale;
                g = s_dev[i].g * fscale;
                b = s_dev[i].b * fscale;
                maxz = t;
            }
        }

        bitmap_dev[offset * 4 + 0] = (unsigned char)(r * 255);
        bitmap_dev[offset * 4 + 1] = (unsigned char)(g * 255);
        bitmap_dev[offset * 4 + 2] = (unsigned char)(b * 255);
        bitmap_dev[offset * 4 + 3] = 255;
    }
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
    Sphere *s_host;
    Sphere *s_dev;
    unsigned char *bitmap_host;
    unsigned char *bitmap_dev;

    cudaEvent_t start, stop;
    float milliseconds = 0;

    srand(time(NULL));

    s_host = (Sphere *)malloc(sizeof(Sphere) * SPHERES);
    if (!s_host) {
        perror("Failed to allocate host memory for spheres");
        return 1;
    }

    for (int i = 0; i < SPHERES; i++) {
        s_host[i].r = rnd(1.0f);
        s_host[i].g = rnd(1.0f);
        s_host[i].b = rnd(1.0f);
        s_host[i].x = rnd(2000.0f) - 1000;
        s_host[i].y = rnd(2000.0f) - 1000;
        s_host[i].z = rnd(2000.0f) - 1000;
        s_host[i].radius = rnd(200.0f) + 40;
    }

    // Allocate device memory
    cudaMalloc((void**)&s_dev, sizeof(Sphere) * SPHERES);
    cudaMalloc((void**)&bitmap_dev, sizeof(unsigned char) * DIM * DIM * 4);

    // Copy sphere data from host to device
    cudaMemcpy(s_dev, s_host, sizeof(Sphere) * SPHERES, cudaMemcpyHostToDevice);

    // Create CUDA events for timing
    cudaEventCreate(&start);
    cudaEventCreate(&stop);

    // Define grid and block dimensions
    dim3 threadsPerBlock(16, 16);
    dim3 numBlocks( (DIM + threadsPerBlock.x - 1) / threadsPerBlock.x,
                    (DIM + threadsPerBlock.y - 1) / threadsPerBlock.y );

    cudaEventRecord(start); // Start timing

    // Launch CUDA kernel
    rayTracingKernel<<<numBlocks, threadsPerBlock>>>(s_dev, bitmap_dev);

    cudaEventRecord(stop);  // End timing
    cudaEventSynchronize(stop);
    cudaEventElapsedTime(&milliseconds, start, stop);

    // Allocate host memory for the result bitmap
    bitmap_host = (unsigned char *)malloc(sizeof(unsigned char) * DIM * DIM * 4);
    if (!bitmap_host) {
        perror("Failed to allocate host memory for bitmap");
        cudaFree(s_dev);
        cudaFree(bitmap_dev);
        free(s_host);
        return 1;
    }

    // Copy result bitmap from device to host
    cudaMemcpy(bitmap_host, bitmap_dev, sizeof(unsigned char) * DIM * DIM * 4, cudaMemcpyDeviceToHost);

    FILE *fp = fopen("result.ppm", "w");
    if (!fp) {
        perror("Failed to open result.ppm for writing");
        free(bitmap_host);
        free(s_host);
        cudaFree(s_dev);
        cudaFree(bitmap_dev);
        return 1;
    }

    ppm_write(bitmap_host, DIM, DIM, fp);
    fclose(fp);

    printf("CUDA ray tracing: %.3f sec\n", milliseconds / 1000.0f); // Convert ms to sec
    printf("[result.ppm] was generated.\n");

    free(s_host);
    free(bitmap_host);
    cudaFree(s_dev);
    cudaFree(bitmap_dev);
    cudaEventDestroy(start);
    cudaEventDestroy(stop);

    return 0;
}
