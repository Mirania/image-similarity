# Image similarity calculator

Based on the algorithm described [here](https://web.archive.org/web/20160305150423/http://www.lac.inpe.br/JIPCookbook/6050-howto-compareimages.jsp), with many modifications made.

Check the directory branch for an application that uses this module to efficiently find similarities between any amount of pictures in a directory.

## How to use?

The class **Analyzer** exposes two primary methods.

- ```compare(BufferedImage, BufferedImage)``` / ```compare(File, File)```

Returns a double value that represents the difference between the images.

This value is 0 when the images are equal, and has no upper cap.

- ```compareThreshold(BufferedImage, BufferedImage, double)``` / ```compareThreshold(File, File, double)```

Returns true if the difference between images is less than the threshold.

Functionally the same as ```compare(...) <= threshold```.
